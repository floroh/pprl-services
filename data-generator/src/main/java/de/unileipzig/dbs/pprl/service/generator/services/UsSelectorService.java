package de.unileipzig.dbs.pprl.service.generator.services;

import de.unileipzig.dbs.pprl.core.common.monitoring.Tag;
import de.unileipzig.dbs.pprl.service.common.data.dto.RecordDto;
import de.unileipzig.dbs.pprl.service.common.utils.ProgressLogger;
import de.unileipzig.dbs.pprl.service.generator.config.MongoConnectionConfig;
import de.unileipzig.dbs.pprl.service.generator.config.UsvrDbConfig;
import de.unileipzig.dbs.pprl.service.generator.data.dto.NcvrPanseImportRequest;
import de.unileipzig.dbs.pprl.service.generator.data.dto.TaggedDatasetDto;
import de.unileipzig.dbs.pprl.service.generator.data.dto.UsvrSelectionConfig;
import de.unileipzig.dbs.pprl.service.generator.persistence.DatabaseTemplateAccessor;
import de.unileipzig.dbs.pprl.service.generator.persistence.RepositorySelector;
import de.unileipzig.dbs.pprl.service.generator.persistence.repositories.ClusterOrderRepository;
import de.unileipzig.dbs.pprl.service.generator.persistence.repositories.ClusterRepository;
import de.unileipzig.dbs.pprl.service.generator.persistence.repositories.NcvrClusterRepository;
import de.unileipzig.dbs.pprl.service.generator.selection.RecordClusterSelector;
import de.unileipzig.dbs.pprl.service.generator.selection.model.common.*;
import de.unileipzig.dbs.pprl.service.generator.selection.model.converter.SelectionRecordConverter;
import de.unileipzig.dbs.pprl.service.generator.selection.nc.NcvrPanseImporter;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


@Service
@Slf4j
public class UsSelectorService {

  private final ClusterOrderRepository clusterOrderRepository;
  private final RepositorySelector repositorySelector;
  private final UsvrDbConfig usvrDbConfig;
  private final MongoConnectionConfig mongoConnectionConfig;
  private final DatabaseTemplateAccessor databaseTemplateAccessor;

  public UsSelectorService(ClusterOrderRepository clusterOrderRepository, RepositorySelector repositorySelector, UsvrDbConfig usvrDbConfig, MongoConnectionConfig mongoConnectionConfig, DatabaseTemplateAccessor databaseTemplateAccessor) {
    this.repositorySelector = repositorySelector;
    this.clusterOrderRepository = clusterOrderRepository;
    this.usvrDbConfig = usvrDbConfig;
    this.mongoConnectionConfig = mongoConnectionConfig;
    this.databaseTemplateAccessor = databaseTemplateAccessor;
  }

  public void importPanseNcvr(NcvrPanseImportRequest request) {
    NcvrPanseImporter importer = new NcvrPanseImporter((NcvrClusterRepository) this.repositorySelector.getRepository(ClusterType.NC), this.usvrDbConfig);
    importer.run_import(request);
  }

  public void deleteClusterOrder(ClusterType clusterType, String seed) {
    this.clusterOrderRepository.deleteClusterOrderByClusterTypeAndSeed(clusterType.getValue(), seed);
    log.info("Completed deletion");
  }

  public List<ClusterOrder> createClusterOrder(ClusterType clusterType, String seed) {
    log.info("Getting all cluster ids");
    List<ObjectId> ids = databaseTemplateAccessor.findAllClusterIds(repositorySelector.getEntityClass(clusterType))
            .stream()
            .collect(Collectors.toList());
    log.info("Got {} cluster ids", ids.size());

    if (ids.isEmpty()) {
      return Collections.emptyList();
    }

    long seedLong = deriveSeedLong(seed);
    log.info("Shuffling ids based on given seedLong {} and seed {}", seedLong, seed);
    shuffleDeterministic(ids, seedLong);

    log.info("Building the cluster order list");
    List<ClusterOrder> orders = IntStream.range(0, ids.size())
            .mapToObj(i -> ClusterOrder.builder()
                    .seed(seed)
                    .clusterType(clusterType.getValue())
                    .clusterId(ids.get(i))    // store hex string
                    .sortKey((long) i)        // unique per seed+type
                    .build())
            .collect(Collectors.toList());

    int total = orders.size();
    try (ProgressLogger pl = ProgressLogger.builder()
            .log(log)
            .total((long) total)
            .logIntervalItems(100000L)
            .logIntervalDuration(Duration.ofSeconds(30))
            .prefix("[Persist cluster order]")
            .build()) {
      for (int i = 0; i < total; i += mongoConnectionConfig.clusterOrderInsertBatchSize) {
        int end = Math.min(i + mongoConnectionConfig.clusterOrderInsertBatchSize, total);
        List<ClusterOrder> batch = orders.subList(i, end);
        clusterOrderRepository.saveAll(batch);
        pl.stepBy(batch.size());
      }
    }
    return orders;
  }

  public List<RecordCluster> getClusters(UsvrSelectionConfig configuration) {
    try (Stream<RecordCluster> clusterStream = switch (configuration.getOrderingStrategy()) {
      case SEEDED_SHUFFLE -> streamInRandomOrder(configuration.getClusterType(), configuration.getOrderingSeed());
      case DB_ORDER -> streamInDatabaseOrder(configuration.getClusterType());
    }) {
      if (configuration.getNumClusters() > 0) {
        return clusterStream.limit(configuration.getNumClusters()).toList();
      } else {
        return clusterStream.toList();
      }
    }
  }

  public TaggedDatasetDto generate(UsvrSelectionConfig configuration) {
    log.info("Received selection request with config {}", configuration);
    RecordClusterSelector recordClusterSelector = new RecordClusterSelector(configuration);

    int numDup = configuration.getNumDuplicates();
    int numA = configuration.getNumIndividualsA();
    int numB = configuration.getNumIndividualsB();

    int countDup = 0;
    int countA = 0;
    int countB = 0;

    // for producing record ids
    AtomicInteger recordIdGenDup = new AtomicInteger();
    AtomicInteger recordIdGenA = new AtomicInteger(numDup);
    AtomicInteger recordIdGenB = new AtomicInteger(numDup + numA);

    List<RecordOutput> outputs = new ArrayList<>();
    List<Tag> tags = new ArrayList<>();
    long countClusters = 0;
    try (ProgressLogger pl = ProgressLogger.builder()
            .log(log)
            .total((long) numDup)
            .logIntervalItems(1000L)
            .logIntervalDuration(Duration.ofSeconds(10))
            .prefix("[Pairs selected based on criteria]")
            .build()) {
      try (Stream<RecordCluster> clusterStream = switch (configuration.getOrderingStrategy()) {
        case SEEDED_SHUFFLE -> streamInRandomOrder(configuration.getClusterType(), configuration.getOrderingSeed());
        case DB_ORDER -> streamInDatabaseOrder(configuration.getClusterType());
      }) {
        Iterator<RecordCluster> it = clusterStream.iterator();

        while (it.hasNext()) {
          // stop early if all targets satisfied
          if (countDup >= numDup && countA >= numA && countB >= numB) break;
          countClusters++;
          if (countClusters % 100_000 == 0) {
            log.info("Checked {} clusters, got: pairs {}/{} a {}/{} b {}/{}",
                    countClusters, countDup, numDup, countA, numA, countB, numB);
          }
          RecordCluster cluster = it.next();

          // Try generator -> produce a pair
          if (countDup < numDup) {
            Optional<ClusterPairCandidate> maybePair = recordClusterSelector.generatePairFromCluster(cluster);
            if (maybePair.isPresent()) {
              ClusterPairCandidate p = maybePair.get();
              int id = recordIdGenDup.incrementAndGet();
              RecordOutput recordA = new RecordOutput(configuration.getSourceA(), String.valueOf(id), p.getLeft());
              RecordOutput recordB = new RecordOutput(configuration.getSourceB(), String.valueOf(id), p.getRight());
              outputs.add(recordA);
              outputs.add(recordB);
              countDup++;
              pl.stepBy(1);
              Set<Integer> timespanInDays = p.getTimespanInDays();
              if (timespanInDays != null) {
                timespanInDays.stream()
                        .min(Integer::compareTo).ifPresent(i -> tags.add(
                                new Tag(recordA.getUniqueLikeId(), recordB.getUniqueLikeId(),
                                        null, "TIMESPAN_DAYS_MIN", null, Double.valueOf(i),
                                        Tag.TYPE_STRUCTURE, Tag.ORIGIN_DATA_GENERATOR)));
                timespanInDays.stream()
                        .map(Math::abs)
                        .min(Integer::compareTo).ifPresent(i -> tags.add(
                                new Tag(recordA.getUniqueLikeId(), recordB.getUniqueLikeId(),
                                        null, "TIMESPAN_DAYS_MIN_ABS", null, Double.valueOf(i),
                                        Tag.TYPE_STRUCTURE, Tag.ORIGIN_DATA_GENERATOR)));
                timespanInDays.stream()
                        .max(Integer::compareTo).ifPresent(i -> tags.add(
                                new Tag(recordA.getUniqueLikeId(), recordB.getUniqueLikeId(),
                                        null, "TIMESPAN_DAYS_MAX", null, Double.valueOf(i),
                                        Tag.TYPE_STRUCTURE, Tag.ORIGIN_DATA_GENERATOR)));
              }
              continue;
            }
          }
          pl.stepBy(0);

          // Try if it fits side A selection
          if (countA < numA) {
            Optional<GenericRawRecord> maybeA = recordClusterSelector.selectRecordFromCluster(cluster, RecordClusterSelector.SIDE.A);
            if (maybeA.isPresent()) {
              int id = recordIdGenA.incrementAndGet();
              outputs.add(new RecordOutput(configuration.getSourceA(), String.valueOf(id), maybeA.get()));
              countA++;
              continue;
            }
          }

          // Try if it fits side B selection
          if (countB < numB) {
            Optional<GenericRawRecord> maybeB = recordClusterSelector.selectRecordFromCluster(cluster, RecordClusterSelector.SIDE.B);
            if (maybeB.isPresent()) {
              int id = recordIdGenB.incrementAndGet();
              outputs.add(new RecordOutput(configuration.getSourceB(), String.valueOf(id), maybeB.get()));
              countB++;
            }
          }
        }
        if (!(countDup == numDup && countA == numA && countB == numB)) {
          log.warn("Stopped stream but did not reach all targets: pairs {}/{} a {}/{} b {}/{}",
                  countDup, numDup, countA, numA, countB, numB);
        }
        log.info("Checked {} record clusters", countClusters);
        List<String> attributeColumns = configuration.getAttributeColumns();
        List<RecordDto> dtos = outputs.stream()
                .peek(ro -> {
                  if (attributeColumns != null && !attributeColumns.isEmpty()) {
                    Map<String, String> previousAttributes = ro.getRecord().getAttributes();
                    Map<String, String> newAttributes = new HashMap<>();
                    for (String attributeToKeep : configuration.getAttributeColumns()) {
                      if (previousAttributes.containsKey(attributeToKeep)) {
                        newAttributes.put(attributeToKeep, previousAttributes.get(attributeToKeep));
                      }
                    }
                    ro.getRecord().setAttributes(newAttributes);
                  }
                })
                .sorted(Comparator.comparingInt(o -> Integer.parseInt(o.getId())))
                .map(SelectionRecordConverter::toRecordDto) // your conversion method
                .toList();

        TaggedDatasetDto outputDataset = new TaggedDatasetDto();
        outputDataset.setRecords(dtos);
        return outputDataset;
      }
    }
  }


  /**
   * SEEDED_SHUFFLE: stream RecordCluster in the order of cluster_orders (seed + clusterType + sortKey).
   * Uses mongoTemplate.stream(...) which returns a Stream<ClusterOrder>. We wrap it into a Stream<RecordCluster>
   * backed by an Iterator that fetches clusters in batches preserving order. The returned Stream is closeable;
   * closing it will close the underlying order stream.
   */
  private Stream<RecordCluster> streamInRandomOrder(ClusterType clusterType, String seed) {
    ClusterRepository<? extends RecordCluster> repository = repositorySelector.getRepository(clusterType);

    boolean clusterOrderExists = clusterOrderRepository.existsBySeedAndClusterType(seed, clusterType.getValue());
    if (!clusterOrderExists) {
      log.info("No cluster orders found for seed '{}', creating it", seed);
      createClusterOrder(clusterType, seed);
    }

    Stream<ClusterOrder> orderStream = databaseTemplateAccessor.getClusterOrderStream(
            mongoConnectionConfig.importBatchSize, seed, clusterType.getValue());

    // iterator that reads orders via orderStream.iterator(), batches them and does a single findAllById per batch
    Iterator<RecordCluster> iterator = new Iterator<>() {
      final Iterator<ClusterOrder> orderIter = orderStream.iterator();
      final Deque<RecordCluster> ready = new ArrayDeque<>();
      boolean finished = false;

      private void fillBatch() {
        if (finished) return;

        List<ClusterOrder> orders = new ArrayList<>(mongoConnectionConfig.clusterOrderRetrieveBatchSize);
        while (orderIter.hasNext() && orders.size() < mongoConnectionConfig.clusterOrderRetrieveBatchSize) {
          orders.add(orderIter.next());
        }
        if (orders.isEmpty()) {
          finished = true;
          return;
        }

        List<ObjectId> ids = orders.stream()
                .map(ClusterOrder::getClusterId)
                .filter(Objects::nonNull)
                .toList();

        if (ids.isEmpty()) return;

        Iterable<? extends RecordCluster> found = repository.findAllById(ids);

        Map<String, RecordCluster> byHexId = StreamSupport.stream(found.spliterator(), false)
                .collect(Collectors.toMap(rc -> rc.getId().toHexString(), Function.identity()));

        // preserve the order from the ClusterOrder list
        for (ClusterOrder o : orders) {
          ObjectId cid = o.getClusterId();
          if (cid == null) continue;
          RecordCluster rc = byHexId.get(cid.toHexString());
          if (rc != null) ready.add(rc);
        }

        // If the orderIter had no more elements and we consumed all, mark finished next time
        if (!orderIter.hasNext() && ready.isEmpty()) finished = true;
      }

      @Override
      public boolean hasNext() {
        if (!ready.isEmpty()) return true;
        if (finished) return false;
        fillBatch();
        return !ready.isEmpty();
      }

      @Override
      public RecordCluster next() {
        if (!hasNext()) throw new NoSuchElementException();
        return ready.removeFirst();
      }
    };

    Spliterator<RecordCluster> spliterator = Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED);
    Stream<RecordCluster> result = StreamSupport.stream(spliterator, false);

    // ensure underlying orderStream is closed when returned stream is closed
    return result.onClose(() -> {
      try {
        orderStream.close();
      } catch (Exception e) {
        log.warn("Failed to close orderStream", e);
      }
    });
  }

  /**
   * DB_ORDER: stream RecordCluster documents directly from the cluster collection (natural DB order).
   * mongoTemplate.stream returns Stream<RecordCluster> which can be returned directly. Make sure to close it.
   */
  private Stream<RecordCluster> streamInDatabaseOrder(ClusterType clusterType) {
    ClusterRepository<? extends RecordCluster> repository = repositorySelector.getRepository(clusterType);
    if (repository == null) throw new IllegalArgumentException("No repo for " + clusterType);

    Class<? extends RecordCluster> clusterClass = repositorySelector.getEntityClass(clusterType);
    Stream<? extends RecordCluster> stream = databaseTemplateAccessor.getClusterSteamInDatabaseOrder(mongoConnectionConfig.clusterOrderRetrieveBatchSize, clusterClass);

    @SuppressWarnings("unchecked")
    Stream<RecordCluster> casted = (Stream<RecordCluster>) stream;
    return casted;
  }

  public static long deriveSeedLong(String seed) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      byte[] digest = md.digest(seed.getBytes(StandardCharsets.UTF_8));
      return ByteBuffer.wrap(digest).getLong(); // first 8 bytes -> long
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  private static void shuffleDeterministic(List<?> list, long seedLong) {
    // Fisher-Yates using SplittableRandom seeded deterministically
    SplittableRandom rnd = new SplittableRandom(seedLong);
    for (int i = list.size() - 1; i > 0; i--) {
      int j = rnd.nextInt(i + 1); // 0..i inclusive
      Collections.swap(list, i, j);
    }
  }
}

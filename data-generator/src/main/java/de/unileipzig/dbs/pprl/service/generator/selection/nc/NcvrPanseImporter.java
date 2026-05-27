package de.unileipzig.dbs.pprl.service.generator.selection.nc;

import com.mongodb.client.*;
import de.unileipzig.dbs.pprl.core.common.exceptions.PprlException;
import de.unileipzig.dbs.pprl.service.common.utils.ProgressLogger;
import de.unileipzig.dbs.pprl.service.generator.config.UsvrDbConfig;
import de.unileipzig.dbs.pprl.service.generator.data.dto.NcvrPanseImportRequest;
import de.unileipzig.dbs.pprl.service.generator.persistence.repositories.NcvrClusterRepository;
import de.unileipzig.dbs.pprl.service.generator.selection.model.common.DateInfo;
import de.unileipzig.dbs.pprl.service.generator.selection.model.common.Duplicate;
import de.unileipzig.dbs.pprl.service.generator.selection.model.common.GenericRawRecord;
import de.unileipzig.dbs.pprl.service.generator.selection.model.common.GenericRawRecordWithDates;
import de.unileipzig.dbs.pprl.service.generator.selection.model.ncvr.*;
import de.unileipzig.dbs.pprl.service.generator.selection.model.ncvr.panse.PanseVoterDocument;
import de.unileipzig.dbs.pprl.service.generator.selection.model.ncvr.panse.NcvrPanseConverter;
import de.unileipzig.dbs.pprl.service.generator.selection.model.ncvr.panse.NcvrRecord;
import de.unileipzig.dbs.pprl.service.generator.selection.model.ncvr.panse.SubEntry;
import lombok.extern.slf4j.Slf4j;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

@Component
@Slf4j
public class NcvrPanseImporter {

  public static final int CLEAN_CLUSTER_BATCH_SIZE = 1000;

  private final NcvrClusterRepository ncvrClusterRepository;

  private final UsvrDbConfig usvrDbConfig;

  public NcvrPanseImporter(NcvrClusterRepository ncvrClusterRepository, UsvrDbConfig usvrDbConfig) {
    this.ncvrClusterRepository = ncvrClusterRepository;
    this.usvrDbConfig = usvrDbConfig;
  }

  public void run_import(NcvrPanseImportRequest request) {

    CodecProvider pojoCodecProvider = PojoCodecProvider.builder().automatic(true).build();
    CodecRegistry pojoCodecRegistry = fromRegistries(getDefaultCodecRegistry(), fromProviders(pojoCodecProvider));

    log.info("Connecting to MongoDB");
    MongoClient mongoClient = MongoClients.create(usvrDbConfig.connectionString);

    log.info("Accessing database");
    MongoDatabase database = mongoClient.getDatabase(usvrDbConfig.database).withCodecRegistry(pojoCodecRegistry);

    log.info("Accessing unclean collection");
    MongoCollection<PanseVoterDocument> unclean = database.getCollection(usvrDbConfig.ncPanseCollection, PanseVoterDocument.class);

    MongoCursor<PanseVoterDocument> collectionIterator = unclean.find().iterator();
    long sizeUnclean = unclean.estimatedDocumentCount();

    long existingClusterCount = ncvrClusterRepository.count();
    if (existingClusterCount > 0 && !request.isForceImportEvenWhenNotEmpty()) {
      String msg = "Cluster repository is not empty. Set 'forceImportEvenWhenNotEmpty = true' if this is really intended.";
      log.error(msg);
      throw new PprlException(msg);
    }

    long cluster_limit = request.getMaxClusters() > 0 ? request.getMaxClusters() : sizeUnclean * 2;
    log.info("Starting the cleaning of {} clusters", sizeUnclean);
    Collection<NcvrRecordCluster> currentCleanClusters = new ArrayList<>();
    try (ProgressLogger pl = ProgressLogger.builder()
            .log(log)
            .total(sizeUnclean)
            .logIntervalItems(100000L)
            .logIntervalDuration(Duration.ofSeconds(30))
            .prefix("[Import Clusters from Panse DB]")
            .build()) {
      while (collectionIterator.hasNext()) {
        PanseVoterDocument cluster = collectionIterator.next();
        List<SubEntry> dups = cluster.getSubentries();

        NcvrRecordCluster cleanCluster = new NcvrRecordCluster();
        cleanCluster.setDomainId(cluster.getId());

        GenericRawRecordWithDates original = null;

        for (SubEntry subEntry : dups) {
          NcvrRecord ncvrRec = subEntry.getRecord();

          Integer age = Integer.parseInt(ncvrRec.getAge());
          Integer year = subEntry.getKey().getSnapshot_date().getYear();
          ncvrRec.setYob(String.valueOf(year - age));

          GenericRawRecord rec = NcvrPanseConverter.toGeneric(ncvrRec);
          if (original == null) {
            original = new GenericRawRecordWithDates();
            original.setRecord(rec);
            original.setOccursIn(subEntry.getOccursInAsDateInfo());
            cleanCluster.setOrigin(original);
          } else {
            Duplicate d = new Duplicate();
            d.setRecord(rec);
            d.setOccursIn(subEntry.getOccursInAsDateInfo());

            final Map<String, Boolean> changes = getChanges(original.getRecord(), rec);
            d.setChanges(changes);
            Set<Integer> timespansInDaysToOriginal = getTimespansInDays(original, d);
            d.getTimespanInDays().addAll(timespansInDaysToOriginal);

            if (changes.containsValue(Boolean.TRUE)) {
              List<Duplicate> dupli = cleanCluster.getDuplicates();
              boolean isDifferent = true;

              for (final Duplicate duplic : dupli) {
                final GenericRawRecord duplica = duplic.getRecord();
                final Map<String, Boolean> changesD = getChanges(duplica, rec);

                if (!changesD.containsValue(Boolean.TRUE)) {
                  isDifferent = false;
                  duplic.getOccursIn().addAll(subEntry.getOccursInAsDateInfo());
                  duplic.getTimespanInDays().addAll(timespansInDaysToOriginal);
                  break;
                }
              }

              if (isDifferent) {
                cleanCluster.getDuplicates().add(d);
              }
            } else {
              cleanCluster.getOrigin().getOccursIn().addAll(subEntry.getOccursInAsDateInfo());
            }
          }
        }
        currentCleanClusters.add(cleanCluster);
        if (currentCleanClusters.size() >= CLEAN_CLUSTER_BATCH_SIZE) {
          log.debug("Inserting {} clusters", currentCleanClusters.size());
          ncvrClusterRepository.insert(currentCleanClusters);
          pl.stepBy(currentCleanClusters.size());
          currentCleanClusters.clear();
          if (pl.getProcessed().get() > cluster_limit) {
            log.warn("Stop importing because stop criteria is reached.");
            return;
          }
        }
      }
      pl.stepBy(currentCleanClusters.size());
      ncvrClusterRepository.insert(currentCleanClusters);
    }
  }

  /**
   * Compare attribute maps of two GenericRawRecord objects.
   * Returns a map of attributeName -> changed (true if values differ).
   */
  public static Map<String, Boolean> getChanges(GenericRawRecord orig, GenericRawRecord rec) {
    Map<String, String> a = (orig == null || orig.getAttributes() == null) ? Collections.emptyMap() : orig.getAttributes();

    Map<String, String> b = (rec == null || rec.getAttributes() == null) ? Collections.emptyMap() : rec.getAttributes();

    // union of keys
    Set<String> keys = new HashSet<>();
    keys.addAll(a.keySet());
    keys.addAll(b.keySet());

    // remove id key if present in attributes map (optional)
    keys.remove(GenericRawRecord.ID_NAME);

    Map<String, Boolean> changes = new LinkedHashMap<>(); // preserve insertion order for readability

    for (String key : keys) {
      String va = normalize(a.get(key));
      String vb = normalize(b.get(key));
      boolean changed = !Objects.equals(va, vb);
      changes.put(key, changed);
    }
    return changes;
  }

  public static Set<Integer> getTimespansInDays(GenericRawRecordWithDates original, Duplicate d) {
    Set<Integer> result = new HashSet<>();
    if (original == null || d == null) return result;
    Set<DateInfo> origins = original.getOccursIn();
    Set<DateInfo> duplicates = d.getOccursIn();
    if (origins == null || duplicates == null) return result;

    for (DateInfo originIn : origins) {
      for (DateInfo duplicateIn : duplicates) {
        DateInfo a = resolve(originIn);
        DateInfo b = resolve(duplicateIn);
        if (a == null || b == null) continue;
        Optional<LocalDate> aDate = a.getAsDate();
        Optional<LocalDate> bDate = b.getAsDate();
        if (aDate.isEmpty() || bDate.isEmpty()) continue;
        long days = ChronoUnit.DAYS.between(aDate.get(), bDate.get());
        result.add((int)days);
      }
    }
    return result;
  }


  /**
   * If the provided DateInfo already has year/month, return it. Otherwise, if it
   * has a non-null date string, attempt to parse it via DateInfo.fromString(date).
   * If parsing yields year/month, return the parsed instance; otherwise return the original.
   */
  private static DateInfo resolve(DateInfo di) {
    if (di == null) return null;
    if (di.getYear() != null && di.getMonth() != null) return di;
    if (di.getDate() != null && !di.getDate().isEmpty()) {
      DateInfo parsed = DateInfo.fromString(di.getDate());
      if (parsed != null && parsed.getYear() != null && parsed.getMonth() != null) {
        return parsed;
      }
    }
    // fallback to original (might still be missing year/month)
    return di;
  }

  /**
   * Normalizes values before comparison. Current behavior: trim, return null if input is null.
   * Change this if you want different semantics (e.g., treat empty string equal to null).
   */
  private static String normalize(String s) {
    return s == null ? null : s.trim();
  }
}

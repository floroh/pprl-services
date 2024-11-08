package de.unileipzig.dbs.pprl.service.linkageunit.services;

import de.unileipzig.dbs.pprl.core.common.exceptions.UnexpectedRuntimeConditionException;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordId;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordIdPair;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordPair;
import de.unileipzig.dbs.pprl.core.common.model.impl.MatchGrade;
import de.unileipzig.dbs.pprl.core.common.model.impl.PersonalAttributeType;
import de.unileipzig.dbs.pprl.core.common.model.impl.RecordIdComposed;
import de.unileipzig.dbs.pprl.core.common.model.impl.RecordIdPairSimple;
import de.unileipzig.dbs.pprl.core.common.monitoring.Tag;
import de.unileipzig.dbs.pprl.core.encoder.record.SelectivePlainProvider;
import de.unileipzig.dbs.pprl.core.matcher.classification.Classifier;
import de.unileipzig.dbs.pprl.service.common.data.converter.RecordConverter;
import de.unileipzig.dbs.pprl.service.common.data.dto.EncodingIdDto;
import de.unileipzig.dbs.pprl.service.common.data.dto.RecordIdDto;
import de.unileipzig.dbs.pprl.service.common.data.mongo.MongoRecordPair;
import de.unileipzig.dbs.pprl.service.common.data.dto.RecordEncodingWishDto;
import de.unileipzig.dbs.pprl.service.linkageunit.config.DefaultLinkImprovementConfig;
import de.unileipzig.dbs.pprl.service.linkageunit.config.LinkImprovementConfig;
import de.unileipzig.dbs.pprl.service.linkageunit.config.LinkSelectionStrategy;
import de.unileipzig.dbs.pprl.service.linkageunit.data.converter.RecordPairDtoConverter;
import de.unileipzig.dbs.pprl.service.linkageunit.data.dto.RecordPairDto;
import de.unileipzig.dbs.pprl.service.linkageunit.data.mongo.BatchMatchProject;
import de.unileipzig.dbs.pprl.service.linkageunit.data.mongo.ProjectState;
import de.unileipzig.dbs.pprl.service.linkageunit.dataset.DatabaseLinkageProcessDataset;
import de.unileipzig.dbs.pprl.service.linkageunit.persistence.repositories.RecordEncodingWishRepository;
import kong.unirest.Unirest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.unileipzig.dbs.pprl.core.matcher.model.api.LinkageProcessDataSet.*;
import static de.unileipzig.dbs.pprl.service.linkageunit.services.helper.MongoRecordUtils.getRecordPairsForNew;
import static de.unileipzig.dbs.pprl.service.linkageunit.services.helper.MongoRecordUtils.removeNewFlagFromRecords;

@Slf4j
@Service
public class LinkImprovementService {

  public static final String PROPERTY_IMPROVED_LINK = "IMPROVED_LINK";
  public static final String PROPERTY_UNCERTAIN_LINK = "UNCERTAIN_LINK";
  public static final String PROPERTY_REPORTED_LINK = "REPORTED_LINK";
  public static final String PROPERTY_LINK_FROM_UPPER_LAYER = "UNREPORTABLE_LINK";
  public static final String TAG_ENCODING_METHOD = "METHOD";
  public static final String CONFIG_PROJECT_ID_TO_REPORT_TO = "PROJECT_ID_TO_REPORT_TO";
  public static final String CONFIG_LINK_SELECTION_STRATEGY = "linkSelectionStrategy";
  public static final String CONFIG_WISH_ENCODING_METHOD = "wishMethod";
  public static final String CONFIG_MIN_ATTRIBUTE_SIMILARITY_FOR_SELECTION =
    "minAttributeSimilarityForSelection";
  public static final String ENCODING_METHOD_SELECTIVE_PLAINTEXT = "DBSLeipzig/Plain/Selective";
  private final RecordEncodingWishRepository recordEncodingWishRepository;

  private final DefaultLinkImprovementConfig linkImprovementConfig;

  private final ProjectService projectService;

  public LinkImprovementService(RecordEncodingWishRepository recordEncodingWishRepository,
    DefaultLinkImprovementConfig linkImprovementConfig, ProjectService projectService) {
    this.recordEncodingWishRepository = recordEncodingWishRepository;
    this.linkImprovementConfig = linkImprovementConfig;
    this.projectService = projectService;
  }

  public void deleteEncodingWishes(ObjectId projectId) {
    recordEncodingWishRepository.deleteAllByEncodingId_Project(projectId.toHexString());
  }

  public List<MongoRecordPair> determineUncertainLinks(ObjectId projectId, int pairLimit) {
    DatabaseLinkageProcessDataset dataset = projectService.getDataset(projectId);
    Map<String, String> projectConfig = projectService.getProject(projectId).getConfig();
    final LinkImprovementConfig currentConfig = buildCurrentConfig(projectConfig);
    Collection<RecordPair> allRecordPairs = dataset.getRecordPairs();
    List<MongoRecordPair> uncertainLinks = determineUncertainLinks(allRecordPairs, currentConfig);
    if (pairLimit < 0) {
      pairLimit = Integer.MAX_VALUE;
    }
    int linkNumber = Math.min(pairLimit / 2, uncertainLinks.size());
    log.info("Creating encoding wishes for {} uncertain links (total={}, limit={}).", linkNumber,
      uncertainLinks.size(), pairLimit / 2
    );
    uncertainLinks = uncertainLinks.subList(0, linkNumber);
    markUncertainLinks(projectId, uncertainLinks);
    return uncertainLinks;
  }

  public void markUncertainLinks(ObjectId projectId, List<MongoRecordPair> uncertainLinks) {
    log.info("Reset uncertain property of all links.");
    projectService.resetUncertaintyPropertyOfAllPairs(projectId);
    log.info("Update uncertain property of {} links.", uncertainLinks.size());
    uncertainLinks.forEach(rp -> rp.addProperty(PROPERTY_UNCERTAIN_LINK));
    projectService.addRecordPairs(projectId, uncertainLinks);
  }

  public List<RecordEncodingWishDto> createEncodingWishesForUncertainLinks(ObjectId projectId,
    List<MongoRecordPair> uncertainLinks) {
    Map<String, String> projectConfig = projectService.getProject(projectId).getConfig();
    final LinkImprovementConfig currentConfig = buildCurrentConfig(projectConfig);
    return createEncodingWishesForUncertainLinks(projectId, uncertainLinks, currentConfig);
  }

  public List<RecordEncodingWishDto> createEncodingWishesForUncertainLinks(ObjectId projectId, List<MongoRecordPair> uncertainLinks,
    LinkImprovementConfig currentConfig) {
    int orderId = 0;
    List<RecordEncodingWishDto> wishes = new ArrayList<>();
    for (MongoRecordPair rp : uncertainLinks) {
      String pairSpecificSecret;
      if (currentConfig.getEncodingMethodForWishes().contains(ENCODING_METHOD_SELECTIVE_PLAINTEXT)) {
        pairSpecificSecret = createPlaintextSelectionString(currentConfig, rp);
      } else {
        pairSpecificSecret = createRandomSecret();
      }
      int finalOrderId = orderId;
      Stream.of(rp.getLeftRecordId(), rp.getRightRecordId())
        .map(RecordConverter::fromRecordId)
        .peek(recordId -> recordId.setBlocks(List.of(rp.getPairId())))
        .map(recordId -> buildWish(projectId.toHexString(),
          currentConfig.getEncodingMethodForWishes(), pairSpecificSecret, recordId,
          finalOrderId
        ))
        .forEach(wishes::add);
      orderId++;
    }
    recordEncodingWishRepository.saveAll(wishes);
    return wishes;
  }

  private LinkImprovementConfig buildCurrentConfig(Map<String, String> projectConfig) {
    LinkImprovementConfig currentConfig = linkImprovementConfig;
    if (projectConfig.get(CONFIG_LINK_SELECTION_STRATEGY) != null) {
      currentConfig = linkImprovementConfig.withLinkSelectionStrategy(
        LinkSelectionStrategy.valueOf(projectConfig.get(CONFIG_LINK_SELECTION_STRATEGY)));
    }
    if (projectConfig.get(CONFIG_WISH_ENCODING_METHOD) != null) {
      currentConfig = linkImprovementConfig.withEncodingMethodForWishes(
        projectConfig.get(CONFIG_WISH_ENCODING_METHOD));
    }
    if (projectConfig.get(CONFIG_MIN_ATTRIBUTE_SIMILARITY_FOR_SELECTION) != null) {
      currentConfig = linkImprovementConfig.withMinSimilarityForPlaintextSelection(
        Double.parseDouble(projectConfig.get(CONFIG_MIN_ATTRIBUTE_SIMILARITY_FOR_SELECTION)));
    }
    return currentConfig;
  }

  public List<RecordEncodingWishDto> getEncodingWishes(ObjectId projectId) {
    return recordEncodingWishRepository.findAllByEncodingId_ProjectOrderByOrderId(projectId.toHexString());
  }


  //  public List<RecordPairDto> getUncertainPairsForGivenRecordIds(ObjectId projectId, Set<RecordId>
  //  recordIds) {
//    List<RecordId> cleanIds = recordIds.stream().map(this::getRecordIdWithSourceAndLocalOnly).toList();
//    DatabaseLinkageProcessDataset dataset = projectService.getDataset(projectId);
//    List<RecordPairDto> pairs =
//      dataset.getRecordPairsFilteredByProperties(Set.of(PROPERTY_UNCERTAIN_LINK)).stream()
//        .map(rp -> (MongoRecordPair) rp)
//        .filter(rp -> {
//          boolean leftContains = cleanIds.contains(getRecordIdWithSourceAndLocalOnly(rp.getLeftRecordId()));
//          boolean rightContains = cleanIds.contains(getRecordIdWithSourceAndLocalOnly(rp.getRightRecordId
//          ()));
//          return leftContains && rightContains;
//        })
//        .map(RecordPairDtoConverter::convertRecordPairToDto)
//        .peek(rp -> rp.setAttributeSimilarities(null))
//        .collect(Collectors.toList());
//    log.info("Found {} uncertain pairs for {} record ids.", pairs.size(), recordIds.size());
//    return pairs;
//  }
  public Collection<RecordPairDto> getUncertainPairsForPairIds(ObjectId projectId,
    Set<RecordIdPair> recordIdPairs) {
    DatabaseLinkageProcessDataset dataset = projectService.getDataset(projectId);
    List<RecordPairDto> pairs =
      dataset.getRecordPairsFilteredByProperties(Set.of(PROPERTY_UNCERTAIN_LINK)).stream()
        .map(rp -> (MongoRecordPair) rp)
        .filter(rp -> {
          RecordIdPairSimple curIdPair = new RecordIdPairSimple(rp.getLeftRecordId(), rp.getRightRecordId());
          return recordIdPairs.contains(curIdPair);
        })
        .map(RecordPairDtoConverter::convertRecordPairToDto)
//        .peek(rp -> rp.setAttributeSimilarities(null))
        .collect(Collectors.toList());
    log.info("Found {} uncertain pairs for {} record id pairs.", pairs.size(), recordIdPairs.size());
    return pairs;
  }

  private RecordId getRecordIdWithSourceAndLocalOnly(RecordId id) {
    return RecordIdComposed.of(id.getLocalId(), id.getSourceId());
  }

  public int reportClassifiedRecordPairs(DatabaseLinkageProcessDataset dataset) {
    BatchMatchProject project = projectService.getProject(dataset.getProjectId());
    Optional<String> projectIdString = project.getConfigValue(CONFIG_PROJECT_ID_TO_REPORT_TO);
    if (projectIdString.isEmpty()) {
      log.warn("No project to report to.");
      return 0;
    }
    final ObjectId projectIdToReportTo = new ObjectId(projectIdString.get());
    Collection<RecordPair> classifiedRecordPairs = dataset.getClassifiedRecordPairs().stream()
      .filter(rp -> !((MongoRecordPair) rp).getProperties().contains(PROPERTY_LINK_FROM_UPPER_LAYER))
      .collect(Collectors.toList());
    log.info("Found {} classified record pairs not from upper layer.", classifiedRecordPairs.size());
    if (linkImprovementConfig.isReportOnlyOnce()) {
      classifiedRecordPairs = classifiedRecordPairs.stream()
        .map(rp -> (MongoRecordPair) rp)
        .filter(rp -> !rp.getProperties().contains(PROPERTY_REPORTED_LINK))
        .collect(Collectors.toList());
    }
    List<RecordPairDto> dtosToReport =
      generateDtosToReport(project, classifiedRecordPairs, projectIdString.get());
//    if (linkImprovementConfig.isReportOnlyOnce()) {
    classifiedRecordPairs = classifiedRecordPairs.stream()
      .map(rp -> (MongoRecordPair) rp)
      .peek((rp -> rp.addProperty(PROPERTY_REPORTED_LINK)))
      .peek(mrp -> {
        // Tags are added for dtos only, not for the actual record pairs
        if (project.getMethod().contains("PPCR")) {
          mrp.addProperty(LinkImprovementService.PROPERTY_IMPROVED_LINK);
        }
      })
      .collect(Collectors.toList());
    log.info("Updating REPORTED_LINK property of {} record pairs.", classifiedRecordPairs.size());
    dataset.updateRecordPairs(classifiedRecordPairs);
    reportPairDtos(dtosToReport, projectIdToReportTo);
    return dtosToReport.size();
  }

  private void reportPairDtos(List<RecordPairDto> dtosToReport, ObjectId projectIdToReportTo) {
    Map<String, Long> stats = dtosToReport.stream()
      .collect(Collectors.groupingBy(RecordPairDto::getMatchGrade, Collectors.counting()));
    log.info("Reporting {} classified record pairs: {}.", dtosToReport.size(), stats);
    if (linkImprovementConfig.getLinkageUnitEndpointToReportTo() != null) {
      log.debug("Reporting to {}.", linkImprovementConfig.getLinkageUnitEndpointToReportTo());
      Unirest.post(linkImprovementConfig.getLinkageUnitEndpointToReportTo() + "/project/pairs")
        .body(dtosToReport)
        .queryString("merge", true)
        .asEmpty();
    } else {
      log.debug("Reporting within this linkage unit.");
      List<MongoRecordPair> mongoPairs = dtosToReport.stream()
        .map(RecordPairDtoConverter::convertDtoToMongoRecordPair)
        .collect(Collectors.toList());
      projectService.mergeNewImprovedRecordPairs(projectIdToReportTo, mongoPairs);
    }
  }

  public void fetchUncertainPairsFromParent(ObjectId projectId) {
    BatchMatchProject project = projectService.getProject(projectId);
    Optional<String> projectIdString = project.getConfigValue(CONFIG_PROJECT_ID_TO_REPORT_TO);
    if (projectIdString.isEmpty()) {
      log.warn("No parent project for fetching pairs available.");
      return;
    }
    final ObjectId projectIdToReportTo = new ObjectId(projectIdString.get());
    Map<String, RecordPair> pairedNewRecordsByPairId = getRecordPairsForNew(projectService, project);
    Set<RecordIdPair> pairIds = pairedNewRecordsByPairId.values().stream()
      .map(rp -> new RecordIdPairSimple(rp.getLeftRecordId(), rp.getRightRecordId()))
      .collect(Collectors.toSet());

    if (pairedNewRecordsByPairId.isEmpty()) {
      log.info("No pairs constructed for new records.");
      return;
    }
    Collection<RecordPairDto> recordPairDtos = fetchUncertainPairDtos(projectIdToReportTo, pairIds);
    List<MongoRecordPair> mongoPairs = recordPairDtos.stream()
      .map(RecordPairDtoConverter::convertDtoToMongoRecordPair)
      .peek(mrp -> {
        RecordPair recordPair = pairedNewRecordsByPairId.get(mrp.getPairId());
        String leftUID = recordPair.getLeftRecordId().getUniqueLikeId();
        String rightUID = recordPair.getRightRecordId().getUniqueLikeId();
        if (mrp.getLeftRecordId().getUniqueLikeId().equals(leftUID) &&
          mrp.getRightRecordId().getUniqueLikeId().equals(rightUID)) {
          mrp.setLeftRecord(recordPair.getLeftRecord());
          mrp.setRightRecord(recordPair.getRightRecord());
        } else if (mrp.getLeftRecordId().getUniqueLikeId().equals(rightUID) &&
          mrp.getRightRecordId().getUniqueLikeId().equals(leftUID)) {
          mrp.setLeftRecord(recordPair.getRightRecord());
          mrp.setRightRecord(recordPair.getLeftRecord());
        } else {
          throw new RuntimeException("Record pair ids do not match!");
        }
        mrp.addProperty(PROPERTY_LINK_FROM_UPPER_LAYER);
        mrp.removeProperty(PROPERTY_REPORTED_LINK);
        mrp.removeProperty(PROPERTY_UNCERTAIN_LINK);
        mrp.addProperty(NEW);
      })
      .collect(Collectors.toList());
    removeNewFlagFromRecords(projectService, project.getDatasetId(), pairedNewRecordsByPairId.values());
    projectService.addRecordPairs(projectId, mongoPairs);
    if (project.getState().isAtMost(ProjectState.BLOCKING)) {
      project.setState(ProjectState.BLOCKING);
      projectService.save(project);
    }
  }

  private Collection<RecordPairDto> fetchUncertainPairDtos(ObjectId parentProjectId,
    Set<RecordIdPair> recordIdPairs) {
    if (linkImprovementConfig.getLinkageUnitEndpointToReportTo() != null) {
      throw new RuntimeException("Not implemented yet");
//      RecordPairDto[] pairs = Unirest.post(linkImprovementConfig.getLinkageUnitEndpointToReportTo()
//          + "/protocol/pairs/" + parentProjectId.toHexString())
//        .body(recordIdDtos)
//        .asObject(RecordPairDto[].class)
//        .getBody();
//      return Arrays.asList(pairs);
    } else {
      log.debug("Fetching within this linkage unit.");
      return getUncertainPairsForPairIds(parentProjectId, recordIdPairs);
    }
  }

  public static List<RecordPairDto> generateDtosToReport(BatchMatchProject project,
    Collection<RecordPair> classifiedRecordPairs, String projectIdToReportTo) {
    List<RecordPairDto> dtosToReport = classifiedRecordPairs.stream()
      .map(RecordPairDtoConverter::convertRecordPairToDto)
      .peek(rp -> rp.setAttributeSimilarities(null))
      .peek(rp -> rp.setProjectId(projectIdToReportTo))
      .peek(rp -> {
        Set<String> properties = rp.getProperties();
        if (properties == null) {
          properties = new HashSet<>();
        } else {
          properties = new HashSet<>(properties);
        }
        properties.add(PROPERTY_IMPROVED_LINK);
        properties.remove(PROPERTY_UNCERTAIN_LINK);
        properties.remove(PROPERTY_REPORTED_LINK);
        rp.setProperties(properties);
      })
      .peek(rp -> {
        List<Tag> tags = rp.getTags();
        if (tags == null) {
          tags = new ArrayList<>();
        } else {
          tags = new ArrayList<>(tags);
        }
        if (!tags.contains(Tag.create(TAG_ENCODING_METHOD))) {
          tags.add(Tag.create(TAG_ENCODING_METHOD, project.getMethod(), null));
        }
        rp.setTags(tags);
      })
      .collect(Collectors.toList());
    return dtosToReport;
  }

  private static RecordEncodingWishDto buildWish(String projectId, String method,
    String pairSpecificSecret, RecordIdDto id, long orderId) {
    return RecordEncodingWishDto.builder()
      .encodingId(EncodingIdDto.builder()
        .method(method)
        .project(projectId)
        .build())
      .id(id)
      .recordSecret(pairSpecificSecret)
      .orderId(orderId)
      .build();
  }

  private List<MongoRecordPair> determineUncertainLinks(Collection<RecordPair> pairs,
    LinkImprovementConfig config) {
    List<MongoRecordPair> sortedUncertainLinks = getSortedUncertainLinks(pairs, config);
    log.info("Using {} strategy for {} uncertain links.", config.getLinkSelectionStrategy(),
      sortedUncertainLinks.size()
    );
    switch (config.getLinkSelectionStrategy()) {
      case SORTED:
        return sortedUncertainLinks;
      case ALTERNATING:
        return getAlternatingUncertainLinks(sortedUncertainLinks);
      case BUCKETS:
        return getBucketBasedUncertainLinks(sortedUncertainLinks);
      default:
        throw new RuntimeException(
          "Unknown link selection strategy: " + config.getLinkSelectionStrategy());
    }
  }

  private List<MongoRecordPair> getBucketBasedUncertainLinks(List<MongoRecordPair> uncertainLinks) {
    try {
      Double min =
        uncertainLinks.stream().mapToDouble(LinkImprovementService::getUncertainty).min().getAsDouble();
      Double max =
        uncertainLinks.stream().mapToDouble(LinkImprovementService::getUncertainty).max().getAsDouble();
      int numberOfBuckets = 10;
      double bucketSize = (max - min) / numberOfBuckets;
      log.info("Uncertainty range: {} - {}, bucket size: {}", min, max, bucketSize);
      List<List<MongoRecordPair>> buckets = new ArrayList<>(numberOfBuckets);
      for (int i = 0; i < numberOfBuckets; i++) {
        buckets.add(new ArrayList<>());
      }
      uncertainLinks.forEach(rp -> {
        double uncertainty = getUncertainty(rp);
        int bucket = (int) Math.floor((uncertainty - min) / bucketSize);
        bucket = Math.min(bucket, numberOfBuckets - 1);
        bucket = Math.max(bucket, 0);
        buckets.get(bucket).add(rp);
      });
      for (List<MongoRecordPair> bucket : buckets) {
        Collections.shuffle(bucket);
      }
      List<MongoRecordPair> balancedUncertainLinks = new ArrayList<>();
      int minSize = buckets.stream().mapToInt(List::size).min().getAsInt();
      for (int i = 0; i < minSize; i++) {
        for (int bucketIndex = 9; bucketIndex >= 0; bucketIndex--) {
          MongoRecordPair curPair = buckets.get(bucketIndex).get(i);
          balancedUncertainLinks.add(curPair);
        }
      }
      log.debug("Balanced uncertain links: {}", balancedUncertainLinks.size());
      if (!linkImprovementConfig.isBalancedSelectionOnly()) {
        boolean remainingPairsAvailable;
        int pairIndexInBucket = minSize;
        do {
          remainingPairsAvailable = false;
          for (int bucketIndex = 9; bucketIndex >= 0; bucketIndex--) {
            try {
              balancedUncertainLinks.add(buckets.get(bucketIndex).get(pairIndexInBucket));
            } catch (IndexOutOfBoundsException e) {
              continue;
            }
            remainingPairsAvailable = true;
          }
          pairIndexInBucket++;
        } while (remainingPairsAvailable);
        log.debug("Balanced uncertain links (with remaining pairs): {}", balancedUncertainLinks.size());
      }
      return balancedUncertainLinks;
    } catch (NoSuchElementException e) {
      log.warn("No uncertain links found.");
      return uncertainLinks;
    }
  }

  private List<MongoRecordPair> getAlternatingUncertainLinks(List<MongoRecordPair> sortedUncertainLinks) {
    List<MongoRecordPair> matches = sortedUncertainLinks.stream()
      .filter(rp -> rp.getClassification().isAtLeast(MatchGrade.PROBABLE_MATCH))
      .toList();
    List<MongoRecordPair> nonMatches = sortedUncertainLinks.stream()
      .filter(rp -> rp.getClassification().isAtMost(MatchGrade.POSSIBLE_MATCH))
      .toList();
    log.debug("Getting alternating uncertain links from {} matches and {} non-matches.",
      matches.size(), nonMatches.size()
    );
    List<MongoRecordPair> balancedUncertainLinks = new ArrayList<>();
    int minSize = Math.min(matches.size(), nonMatches.size());
    for (int i = 0; i < minSize; i++) {
      balancedUncertainLinks.add(matches.get(i));
      balancedUncertainLinks.add(nonMatches.get(i));
    }
    if (!linkImprovementConfig.isBalancedSelectionOnly()) {
      if (minSize < matches.size()) {
        balancedUncertainLinks.addAll(matches.subList(minSize, matches.size()));
      } else if (minSize < nonMatches.size()) {
        balancedUncertainLinks.addAll(nonMatches.subList(minSize, nonMatches.size()));
      }
    }
    return balancedUncertainLinks;
  }

  private List<MongoRecordPair> getSortedUncertainLinks(Collection<RecordPair> pairs,
    LinkImprovementConfig config) {
    return pairs.stream()
      .map(rp -> (MongoRecordPair) rp)
      .filter(rp -> !rp.getProperties().contains(PROPERTY_IMPROVED_LINK))
      .filter(rp -> getUncertainty(rp) > config.getMinUncertainty())
      .sorted((o1, o2) -> Double.compare(getUncertainty(o2), getUncertainty(o1)))
      .collect(Collectors.toList());
  }

  public static double getUncertainty(RecordPair rp) {
    return getUncertainty(rp.getTags(), rp.getSimilarity());
  }

  public static double getUncertainty(Collection<Tag> tags, double similarity) {
    Optional<Tag> probabilityTag =
      tags.stream().filter(tag -> tag.getTag().equals(Classifier.TAG_PROBABILITY)).findFirst();
    if (probabilityTag.isPresent()) {
      return 1.0 - probabilityTag.get().getNumericValue();
    } else {
      log.warn("No probability tag found. Using similarity distance to fixed threshold instead!");
      double threshold = 0.8; // TODO Get actual threshold used by matcher
      return getUncertaintyBySimilarityDistance(threshold, similarity);
    }
  }

  public static double getUncertaintyBySimilarityDistance(double threshold, double similarity) {
    log.warn("Using similarity distance as uncertainty measure with fixed threshold of {}.", threshold);
    return 1.0 - Math.abs(threshold - similarity);
  }

  private String createRandomSecret() {
    return RandomStringUtils.random(32, true, false);
  }

  private String createPlaintextSelectionString(LinkImprovementConfig config, MongoRecordPair rp) {
    if (rp.getAttributeSimilarities().isEmpty()) {
//      throw new UnexpectedRuntimeConditionException(
//        "attributeSimilarities of pair " + rp.getPairId() + " are empty");
//      log.warn("attributeSimilarities of pair " + rp.getPairId() + " are empty");
      return "";
    }
    Map<String, Double> attributeSimilarities = rp.getAttributeSimilarities().get();
    StringBuilder sb = new StringBuilder();
    attributeSimilarities.keySet().stream()
      .sorted(new PersonalAttributeType.AttributeNameComparator())
      .forEach(an -> {
        Double attrSim = attributeSimilarities.get(an);
        if (attrSim > config.getMinSimilarityForPlaintextSelection() && attrSim < 1) {
          sb.append(an).append(SelectivePlainProvider.ATTRIBUTENAME_SEPARATOR);
        }
      });
//    log.info("ptSelection: {} for sims={}", sb, attributeSimilarities);
    return sb.toString();
  }
}

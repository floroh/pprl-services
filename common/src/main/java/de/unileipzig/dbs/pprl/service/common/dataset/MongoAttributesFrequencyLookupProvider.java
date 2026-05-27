package de.unileipzig.dbs.pprl.service.common.dataset;

import de.unileipzig.dbs.pprl.core.analyzer.attribute.AttributeFrequencyAnalyzer;
import de.unileipzig.dbs.pprl.core.analyzer.attribute.AttributeMostFrequent;
import de.unileipzig.dbs.pprl.core.common.frequencies.AttributeFrequencyLookup;
import de.unileipzig.dbs.pprl.core.common.frequencies.AttributesFrequencyLookup;
import de.unileipzig.dbs.pprl.core.common.frequencies.AttributesFrequencyLookupFilter;
import de.unileipzig.dbs.pprl.core.common.frequencies.AttributesFrequencyLookupProvider;
import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import de.unileipzig.dbs.pprl.service.common.SpringContext;
import de.unileipzig.dbs.pprl.service.common.data.mongo.MongoFrequencyLookup;
import de.unileipzig.dbs.pprl.service.common.persistence.repositories.mongo.MongoFrequencyLookupRepository;
import de.unileipzig.dbs.pprl.service.common.services.DatasetMongoService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import tech.tablesaw.api.Table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static de.unileipzig.dbs.pprl.core.analyzer.Analyzer.*;


/**
 * Read frequency information of attribute values from a {@link de.unileipzig.dbs.pprl.service.common.data.mongo.MongoFrequencyLookup}
 */
@Slf4j
public class MongoAttributesFrequencyLookupProvider implements AttributesFrequencyLookupProvider {

  public static final int REFERENCE_TOTAL = 200000;

  @Getter
  @Setter
  private boolean transformAttributes = false;

  @Getter
  @Setter
  private boolean useRelativeFrequencies = false;

  @Getter
  @Setter
  private long datasetId;

  @Getter
  @Setter
  private String datasetSource;

  @Getter
  @Setter
  private List<String> attributesNamesToParse;

  private MongoFrequencyLookupRepository mongoFrequencyLookupRepository;

  private DatasetMongoService datasetMongoService;

  @Getter
  @Setter
  private AttributesFrequencyLookupFilter filter = new AttributesFrequencyLookupFilter();

  public MongoAttributesFrequencyLookupProvider(MongoFrequencyLookupRepository mongoFrequencyLookupRepository,
                                                DatasetMongoService datasetMongoService) {
    this.mongoFrequencyLookupRepository = mongoFrequencyLookupRepository;
    this.datasetMongoService = datasetMongoService;
  }

  private MongoAttributesFrequencyLookupProvider() {
    datasetMongoService = SpringContext.getBean(DatasetMongoService.class);
    mongoFrequencyLookupRepository = SpringContext.getBean(MongoFrequencyLookupRepository.class);
  }

  public AttributesFrequencyLookup provide(long datasetId) {
    setDatasetId(datasetId);
    return provide();
  }

  @Override
  public AttributesFrequencyLookup provide() {
    if (datasetId == 0) {
      throw new IllegalStateException("Dataset id not provided");
    }
    boolean sourceSpecific = datasetSource != null && !datasetSource.isBlank();
    if (sourceSpecific) log.info("Providing source-specific AttributesFrequencyLookup for {}", datasetSource);

    String config = "transform=" + transformAttributes + ", relFreq=" + useRelativeFrequencies + ", " + filter.getSummary();
    Optional<MongoFrequencyLookup> possibleDbLookup = sourceSpecific
            ? mongoFrequencyLookupRepository.findByDatasetIdAndDatasetSourceAndConfig(datasetId, null, config)
            : mongoFrequencyLookupRepository.findByDatasetIdAndDatasetSourceAndConfig(datasetId, datasetSource, config);

    if (possibleDbLookup.isEmpty()) {
      List<Record> records = sourceSpecific
              ? new ArrayList<>(datasetMongoService.getRecordsBySource(datasetId, datasetSource))
              : new ArrayList<>(datasetMongoService.getAllRecords(datasetId));

      AttributeMostFrequent analyzer = new AttributeMostFrequent();
      Map<String, Table> additionalResults =
              analyzer.analyze(AttributeFrequencyAnalyzer.prepareRecords(records)).getAdditionalResults();
      final AttributesFrequencyLookup afls = new AttributesFrequencyLookup(transformAttributes);
      MongoFrequencyLookup.MongoFrequencyLookupBuilder mongoFrequencyLookupBuilder =
              MongoFrequencyLookup.builder()
                      .datasetId(datasetId)
                      .datasetSource(datasetSource)
                      .config(config);
      for (String attrName : additionalResults.keySet()) {
        Map<String, Long> curFrequencies = new HashMap<>();
        Table frequencies = additionalResults.get(attrName);
        AttributeFrequencyLookup afl = getAttributeFrequencyLookup(afls, attrName, curFrequencies, frequencies);
        mongoFrequencyLookupBuilder.frequenciesByAttribute(attrName, afl.getFrequencies());
        if (attributesNamesToParse == null || attributesNamesToParse.contains(attrName)) {
          afls.addAttributeFrequencyLookup(attrName, afl);
        }
      }
      MongoFrequencyLookup mongoFrequencyLookup = mongoFrequencyLookupBuilder.build();
      mongoFrequencyLookupRepository.save(mongoFrequencyLookup);
      return afls;
    }
    log.info("Using precomputed attribute frequency lookup for dataset {}", datasetId);
    MongoFrequencyLookup mongoFrequencyLookup = possibleDbLookup.get();
    final AttributesFrequencyLookup afls = new AttributesFrequencyLookup(transformAttributes);
    mongoFrequencyLookup.getFrequenciesByAttributes().forEach(
            (k, v) -> {
              if (attributesNamesToParse == null || attributesNamesToParse.contains(k)) {
                afls.addAttributeFrequencyLookup(k, new AttributeFrequencyLookup(v));
              }
            });
    return afls;
  }

  private AttributeFrequencyLookup getAttributeFrequencyLookup(AttributesFrequencyLookup afls,
                                                               String attrName, Map<String, Long> curFrequencies, Table frequencies) {
    frequencies.stream().forEach(r -> {
      String attrValue = r.getString(HEADER_ATTRIBUTE);
      attrValue = afls.normalizeAttributeValue(attrName, attrValue);
      long frequency;
      if (useRelativeFrequencies) {
        double relFrequency = r.getDouble(HEADER_RELATIVE_FREQUENCY);
        frequency = Double.valueOf(relFrequency * REFERENCE_TOTAL).longValue();
      } else {
        frequency = r.getLong(HEADER_ABSOLUTE_FREQUENCY);
      }
      addFrequency(curFrequencies, attrValue, frequency);
    });
    AttributeFrequencyLookup afl = getAttributeFrequencyLookup(attrName, curFrequencies);
    if (useRelativeFrequencies) {
      afl.setTotalCount(REFERENCE_TOTAL);
    }
    return afl;
  }

  private static void addFrequency(Map<String, Long> curFrequencies, String attrValue, Long frequency) {
    // Add to frequency if an entry already exists
    if (curFrequencies.containsKey(attrValue)) {
      curFrequencies.compute(attrValue, (k, tmpFrequency) -> tmpFrequency + frequency);
    } else {
      curFrequencies.put(attrValue, frequency);
    }
  }

  private AttributeFrequencyLookup getAttributeFrequencyLookup(String attrName,
                                                               Map<String, Long> frequencies) {
    log.debug("Filtering frequency lookup table for attribute: {}", attrName);
    return filter.getFilteredAttributeFrequencyLookup(frequencies);
  }

  @Override
  public String toString() {
    return "MongoAttributesFrequencyLookupProvider{" +
            "transformAttributes=" + transformAttributes +
            ", useRelativeFrequencies=" + useRelativeFrequencies +
            ", datasetId=" + datasetId +
            ", datasetSource=" + datasetSource +
            ", attributesNamesToParse=" + attributesNamesToParse +
            ", filter=" + filter +
            '}';
  }
}

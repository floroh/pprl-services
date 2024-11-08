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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
public class MongoAttributesFrequencyLookupProvider implements AttributesFrequencyLookupProvider {

  public static final int REFERENCE_TOTAL = 200000;

  private boolean transformAttributes = false;

  private boolean useRelativeFrequencies = false;

  private int datasetId;

  private MongoFrequencyLookupRepository mongoFrequencyLookupRepository;

  private DatasetMongoService datasetMongoService;
  private AttributesFrequencyLookupFilter filter = new AttributesFrequencyLookupFilter();

  private static final Logger logger = LogManager.getLogger(MongoAttributesFrequencyLookupProvider.class);

//  public MongoAttributesFrequencyLookupProvider(MongoFrequencyLookupRepository mongoFrequencyLookupRepository,
//    DatasetMongoService datasetMongoService) {
//    this.mongoFrequencyLookupRepository = mongoFrequencyLookupRepository;
//    this.datasetMongoService = datasetMongoService;
//  }


  private MongoAttributesFrequencyLookupProvider() {
    datasetMongoService = SpringContext.getBean(DatasetMongoService.class);
    mongoFrequencyLookupRepository = SpringContext.getBean(MongoFrequencyLookupRepository.class);
  }

  @Override
  public AttributesFrequencyLookup provide() {
    Optional<MongoFrequencyLookup> byDatasetId = mongoFrequencyLookupRepository.findByDatasetId(datasetId);
    if (byDatasetId.isEmpty()) {
      AttributeMostFrequent analyzer = new AttributeMostFrequent();
      List<Record> records = new ArrayList<>(datasetMongoService.getAllRecords(datasetId));
      Map<String, Table> additionalResults =
        analyzer.analyze(AttributeFrequencyAnalyzer.prepareRecords(records)).getAdditionalResults();
      final AttributesFrequencyLookup afls = new AttributesFrequencyLookup(transformAttributes);
      MongoFrequencyLookup.MongoFrequencyLookupBuilder mongoFrequencyLookupBuilder =
        MongoFrequencyLookup.builder()
          .datasetId(datasetId);
      for (String attrName : additionalResults.keySet()) {
//        if (!attrName.equals("GENDER")) continue;
        Map<String, Long> curFrequencies = new HashMap<>();
        Table frequencies = additionalResults.get(attrName);
        AttributeFrequencyLookup afl = getAttributeFrequencyLookup(afls, attrName, curFrequencies, frequencies);
        mongoFrequencyLookupBuilder.frequenciesByAttribute(attrName, afl.getFrequencies());
        afls.addAttributeFrequencyLookup(attrName, afl);
      }
      MongoFrequencyLookup mongoFrequencyLookup = mongoFrequencyLookupBuilder.build();
      mongoFrequencyLookupRepository.save(mongoFrequencyLookup);
      return afls;
    }
    logger.info("Using precomputed attribute frequency lookup for dataset " + datasetId);
    MongoFrequencyLookup mongoFrequencyLookup = byDatasetId.get();
    final AttributesFrequencyLookup afls = new AttributesFrequencyLookup(transformAttributes);
    mongoFrequencyLookup.getFrequenciesByAttributes().forEach((k,v) -> {
      afls.addAttributeFrequencyLookup(k, new AttributeFrequencyLookup(v));
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
      long tmpFrequency = curFrequencies.get(attrValue);
      curFrequencies.put(attrValue, tmpFrequency + frequency);
    } else {
      curFrequencies.put(attrValue, frequency);
    }
  }

  private AttributeFrequencyLookup getAttributeFrequencyLookup(String attrName,
    Map<String, Long> frequencies) {
    logger.debug("Filtering frequency lookup table for attribute: " + attrName);
    return filter.getFilteredAttributeFrequencyLookup(frequencies);
  }

  public int getDatasetId() {
    return datasetId;
  }

  public void setDatasetId(int datasetId) {
    this.datasetId = datasetId;
  }

  public AttributesFrequencyLookupFilter getFilter() {
    return filter;
  }

  public void setFilter(AttributesFrequencyLookupFilter filter) {
    this.filter = filter;
  }

  public boolean isTransformAttributes() {
    return transformAttributes;
  }

  public void setTransformAttributes(boolean transformAttributes) {
    this.transformAttributes = transformAttributes;
  }

  public boolean isUseRelativeFrequencies() {
    return useRelativeFrequencies;
  }

  public void setUseRelativeFrequencies(boolean useRelativeFrequencies) {
    this.useRelativeFrequencies = useRelativeFrequencies;
  }

  @Override
  public String toString() {
    return "MongoAttributesFrequencyLookupProvider{" +
      "transformAttributes=" + transformAttributes +
      ", useRelativeFrequencies=" + useRelativeFrequencies +
      ", datasetId=" + datasetId +
      ", mongoFrequencyLookupRepository=" + mongoFrequencyLookupRepository +
      ", datasetMongoService=" + datasetMongoService +
      ", filter=" + filter +
      '}';
  }
}

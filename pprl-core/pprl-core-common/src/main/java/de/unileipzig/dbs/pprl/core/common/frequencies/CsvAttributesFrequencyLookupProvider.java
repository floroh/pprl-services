package de.unileipzig.dbs.pprl.core.common.frequencies;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tech.tablesaw.api.ColumnType;
import tech.tablesaw.api.Table;
import tech.tablesaw.io.csv.CsvReadOptions;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * Read frequency information of attribute values from a folder containing csv files.
 * The name of the csv file determines the attribute name, e.g. FIRSTNAME.csv
 * The csv is expected to have at least two columns for the attribute value and its corresponding absolute
 * frequency.
 */
public class CsvAttributesFrequencyLookupProvider implements AttributesFrequencyLookupProvider {

  public static final String COLUMN_ATTRIBUTE = "attribute";
  public static final String COLUMN_ABSOLUTE_FREQUENCY = "absFrequency";
  public static final String COLUMN_RELATIVE_FREQUENCY = "relFrequency";

  public static final int REFERENCE_TOTAL = 200000;

  private String location;

  private List<String> attributesNamesToParse = new ArrayList<>();

  private boolean transformAttributes = false;

  private boolean useRelativeFrequencies = false;

  private AttributesFrequencyLookupFilter filter = new AttributesFrequencyLookupFilter();

  private static final Logger logger = LogManager.getLogger(CsvAttributesFrequencyLookupProvider.class);

  public CsvAttributesFrequencyLookupProvider(String location) {
    this.location = location;
  }

  private CsvAttributesFrequencyLookupProvider() {
  }

  @Override
  public AttributesFrequencyLookup provide() {
    final boolean filterByAttributeName = !attributesNamesToParse.isEmpty();
    try {
      Path folder = new File(location).toPath();
      List<String> attrNames = Files.list(folder)
        .filter(p -> !Files.isDirectory(p))
        .map(Path::getFileName)
        .map(Path::toString)
        .map(s -> s.replace(".csv", ""))
        .filter(s -> !filterByAttributeName || attributesNamesToParse.contains(s))
        .collect(Collectors.toList());

      final AttributesFrequencyLookup afls = new AttributesFrequencyLookup(transformAttributes);
      for (String attrName : attrNames) {
        String curPath = folder.toAbsolutePath() + File.separator + attrName + ".csv";
        Map<String, Long> curFrequencies = new HashMap<>();
        Table table = readAttributeFrequencyLookupTable(curPath);

        table.stream().forEach(r -> {
          String attrValue = r.getString(COLUMN_ATTRIBUTE);
          attrValue = afls.normalizeAttributeValue(attrName, attrValue);
          long frequency;
          if (useRelativeFrequencies) {
            double relFrequency = r.getDouble(COLUMN_RELATIVE_FREQUENCY);
            frequency = Double.valueOf(relFrequency * REFERENCE_TOTAL).longValue();
          } else {
            frequency = r.getLong(COLUMN_ABSOLUTE_FREQUENCY);
          }
          addFrequency(curFrequencies, attrValue, frequency);
        });
        AttributeFrequencyLookup afl = getAttributeFrequencyLookup(attrName, curFrequencies);
        if (useRelativeFrequencies) {
          afl.setTotalCount(REFERENCE_TOTAL);
        }
        afls.addAttributeFrequencyLookup(attrName, afl);
      }
      return afls;
    } catch (IOException e) {
      throw new RuntimeException("Could not parse attribute frequency lookup files in: " + location);
    }
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

  public List<String> getAttributesNamesToParse() {
    return attributesNamesToParse;
  }

  public void setAttributeNamesToParse(List<String> attributesNamesToParse) {
    this.attributesNamesToParse = attributesNamesToParse;
  }

  public String getLocation() {
    return location;
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

  public static Table readAttributeFrequencyLookupTable(String path) throws IOException {
    return Table.read().usingOptions(
      CsvReadOptions.builder(path)
        .columnTypes(new ColumnType[] {ColumnType.STRING, ColumnType.LONG, ColumnType.DOUBLE})
    );
  }

  @Override
  public String toString() {
    return "CsvAttributesFrequencyLookupProvider{" +
      "location='" + location + '\'' +
      ", attributesNamesToParse=" + attributesNamesToParse +
      ", filter=" + filter +
      '}';
  }
}

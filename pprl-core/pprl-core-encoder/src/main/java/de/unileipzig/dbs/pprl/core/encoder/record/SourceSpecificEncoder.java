package de.unileipzig.dbs.pprl.core.encoder.record;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.unileipzig.dbs.pprl.core.common.exceptions.PprlException;
import de.unileipzig.dbs.pprl.core.common.model.api.Record;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static de.unileipzig.dbs.pprl.core.common.HelperUtils.computeJsonDiff;
import static de.unileipzig.dbs.pprl.core.encoder.RecordEncoderSerialization.serializeJson;

public class SourceSpecificEncoder implements RecordEncoder {

  private Map<String, RecordEncoder> encoders;

  public SourceSpecificEncoder(Map<String, RecordEncoder> encoders) {
    this.encoders = encoders;
  }

  private SourceSpecificEncoder() {
  }

  @Override
  public Record encode(Record record) {
    String sourceId = record.getId().getSourceId();
    RecordEncoder encoder = encoders.get(sourceId);
    if (encoder == null) {
      throw new PprlException("Could not find encoder for source " + sourceId);
    }
    return encoder.encode(record);
  }

  public Map<String, RecordEncoder> getEncoders() {
    return encoders;
  }


  @JsonIgnore
  public String getDifferences() {
    if (encoders == null || encoders.isEmpty()) {
      return "No encoders available.";
    }

    StringBuilder diff = new StringBuilder();
    ObjectMapper mapper = new ObjectMapper();

    List<String> sources = new ArrayList<>(encoders.keySet());
    for (int i = 0; i < sources.size(); i++) {
      String src1 = sources.get(i);
      for (int j = i + 1; j < sources.size(); j++) {
        String src2 = sources.get(j);
        try {
          JsonNode node1 = mapper.readTree(serializeJson(encoders.get(src1), true));
          JsonNode node2 = mapper.readTree(serializeJson(encoders.get(src2), true));

          String pairDiff = computeJsonDiff(node1, node2, "");
          if (!pairDiff.isEmpty()) {
            diff.append("Differences between '").append(src1)
                    .append("' and '").append(src2).append("':\n");
            diff.append(pairDiff).append("\n");
          }
        } catch (Exception e) {
          diff.append("Error comparing '").append(src1).append("' and '").append(src2)
                  .append("': ").append(e.getMessage()).append("\n");
        }
      }
    }

    return diff.length() > 0 ? diff.toString() : "All encoders are identical.";
  }

  @Override
  public String toString() {
    return "SourceSpecificEncoder{" +
            "encoders=" + encoders +
            '}';
  }
}

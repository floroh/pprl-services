package de.unileipzig.dbs.pprl.service.protocol.model.mongo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import de.unileipzig.dbs.pprl.service.protocol.workflow.ProcessingStep;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Data
@Builder
@Document
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class MultiLayerProtocol {

  @Id
  @JsonIgnore
  private ObjectId objId;

  private String protocolId;

  @Singular
  private List<Layer> layers;

  private int plaintextDatasetId;

  private int initialDatasetId;

  private String lastUpdate;

  @Singular("stepHistory")
  private List<ProcessingStep> stepHistory;

  @Singular("stepQueue")
  private List<ProcessingStep> stepQueue;

  private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

  public List<ProcessingStep> getStepHistory() {
    if (stepHistory == null || stepHistory.isEmpty()) {
      stepHistory = new ArrayList<>();
    }
    return stepHistory;
  }

  public List<ProcessingStep> getStepQueue() {
    if (stepQueue == null || stepQueue.isEmpty()) {
      stepQueue = new ArrayList<>();
    }
    return stepQueue;
  }

  @JsonIgnore
  public Layer getLayerOfProject(String projectId) {
    Optional<Layer> first =
      layers.stream().filter(layer -> layer.getProjectId().equals(projectId)).findFirst();
    if (first.isEmpty()) {
      throw new RuntimeException("No layer found for project " + projectId);
    }
    return first.get();
  }
  @JsonIgnore
  public Optional<Layer> getLayerByName(String name) {
    return layers.stream().filter(layer -> layer.getName().equals(name)).findFirst();
  }

  public void updateLastUpdateToCurrentTime() {
    lastUpdate = sdf.format(System.currentTimeMillis());
  }

  @JsonIgnore
  public int getLayerId(Layer layer) {
    return layers.indexOf(layer);
  }

  @JsonIgnore
  public Optional<Layer> getNextLayer(Layer layer) {
    int i = layers.indexOf(layer);
    return i == layers.size() - 1 ? Optional.empty() : Optional.of(layers.get(i + 1));
  }

  @JsonIgnore
  public Optional<Layer> getPreviousLayer(Layer layer) {
    int i = layers.indexOf(layer);
    return i > 0 ? Optional.of(layers.get(i - 1)) : Optional.empty();
  }

  @JsonIgnore
  public boolean hasNextLayer(Layer layer) {
    int i = layers.indexOf(layer);
    return i < layers.size() - 1;
  }
}
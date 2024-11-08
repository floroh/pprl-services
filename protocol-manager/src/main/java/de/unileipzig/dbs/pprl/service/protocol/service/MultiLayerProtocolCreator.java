package de.unileipzig.dbs.pprl.service.protocol.service;

import de.unileipzig.dbs.pprl.service.linkageunit.config.LinkSelectionStrategy;
import de.unileipzig.dbs.pprl.service.linkageunit.data.dto.BatchMatchProjectDto;
import de.unileipzig.dbs.pprl.service.linkageunit.data.dto.MatcherUpdateType;
import de.unileipzig.dbs.pprl.service.linkageunit.data.dto.MatchingDto;
import de.unileipzig.dbs.pprl.service.protocol.api.MatcherApi;
import de.unileipzig.dbs.pprl.service.protocol.model.mongo.Layer;
import de.unileipzig.dbs.pprl.service.protocol.model.mongo.MultiLayerProtocol;
import de.unileipzig.dbs.pprl.service.protocol.utils.JsonModifier;
import de.unileipzig.dbs.pprl.service.protocol.utils.Utils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import org.bson.types.ObjectId;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import static de.unileipzig.dbs.pprl.service.linkageunit.services.LinkImprovementService.*;
import static de.unileipzig.dbs.pprl.service.linkageunit.services.ProjectService.CONFIG_RECORD_PAIR_LIMIT;

@Slf4j
public class MultiLayerProtocolCreator {

  public static MultiLayerProtocol getExampleMultiLayerProtocol(String type, int dataOwnerDatasetId) {
    if (type != null && type.equals("ABF-PPCR")) {
      return getExampleAttributeLevelProtocol(dataOwnerDatasetId);
    } else if (type != null && type.equals("RBF-PPCR")) {
      return getExampleRbfPpcrProtocol(dataOwnerDatasetId);
    }
    return getExampleMultiLayerProtocol(dataOwnerDatasetId);
  }

  public static MultiLayerProtocol getExampleMultiLayerProtocol(int dataOwnerDatasetId) {
    return MultiLayerProtocol.builder()
      .initialDatasetId(dataOwnerDatasetId + 200)
      .plaintextDatasetId(dataOwnerDatasetId)
      .layer(Layer.builder()
        .name("RBF")
        .encodingMethod("DBSLeipzig/RBF/NCVR-F-avg")
        .matcherMethod("DBSLeipzig/RBF/Train/PB")
        .updateMatcher(true)
        .updateType(MatcherUpdateType.IMPROVED)
        .initialThreshold(0.75)
        .build()
      )
      .layer(Layer.builder()
        .name("ABF")
        .encodingMethod("DBSLeipzig/Keyed/Freq/ABF/NoPT/NCVR-F")
        .matcherMethod("DBSLeipzig/ABF/Freq/PB/Weka/WEKA_EXP_RANDOM_FOREST")
        .updateMatcher(true)
        .updateType(MatcherUpdateType.UPPER_IMPROVED)
        .maxBatches(8)
        .batchSizeConfig(Utils.combineList(Collections.nCopies(5, 100), 1000))
        .build()
      )
      .layer(Layer.builder()
        .name("PPCR")
        .encodingMethod("DBSLeipzig/Plain/SelectiveFrequency")
        .matcherMethod("DBSLeipzig/PPCR/GT")
        .updateMatcher(false)
        .updateType(MatcherUpdateType.UPPER_IMPROVED)
        .maxBatches(2)
        .batchSizeConfig(List.of(20))
        .budget(200)
        .errorRate(0.1)
        .build()
      )
      .build();
  }

  public static MultiLayerProtocol getExampleRbfPpcrProtocol(int dataOwnerDatasetId) {
    return MultiLayerProtocol.builder()
      .initialDatasetId(dataOwnerDatasetId + 200)
      .plaintextDatasetId(dataOwnerDatasetId)
      .layer(Layer.builder()
        .name("RBF")
        .encodingMethod("DBSLeipzig/RBF/NCVR-F-avg")
        .matcherMethod("DBSLeipzig/RBF/Train/PB")
        .updateMatcher(true)
        .updateType(MatcherUpdateType.IMPROVED)
        .initialThreshold(0.75)
        .build()
      )
      .layer(Layer.builder()
        .name("PPCR")
        .encodingMethod("DBSLeipzig/Plain/SelectiveFrequency")
        .matcherMethod("DBSLeipzig/PPCR/GT-with-comparison")
        .updateMatcher(false)
        .updateType(MatcherUpdateType.UPPER_IMPROVED)
        .maxBatches(10)
        .batchSizeConfig(List.of(20))
        .budget(200)
        .errorRate(0.1)
        .build()
      )
      .build();
  }

  public static MultiLayerProtocol getExampleAttributeLevelProtocol(int dataOwnerDatasetId) {
    return MultiLayerProtocol.builder()
      .initialDatasetId(dataOwnerDatasetId + 100)
      .plaintextDatasetId(dataOwnerDatasetId)
      .layer(Layer.builder()
        .name("ABF")
        .encodingMethod("DBSLeipzig/Keyed/Freq/ABF/NCVR-F")
        .matcherMethod("DBSLeipzig/ABF/Freq/PB/Weka/WEKA_EXP_RANDOM_FOREST")
        .updateMatcher(true)
        .updateType(MatcherUpdateType.IMPROVED)
        .build()
      )
      .layer(Layer.builder()
        .name("PPCR")
        .encodingMethod("DBSLeipzig/Plain/SelectiveFrequency")
        .matcherMethod("DBSLeipzig/PPCR/GT")
        .updateMatcher(false)
        .updateType(MatcherUpdateType.UPPER_IMPROVED)
        .maxBatches(20)
        .batchSizeConfig(List.of(5))
        .budget(100)
        .errorRate(0.1)
        .build()
      )
      .build();
  }
}

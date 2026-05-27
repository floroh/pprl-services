package de.unileipzig.dbs.pprl.service.protocol.service;

import de.unileipzig.dbs.pprl.core.common.TableSerialization;
import de.unileipzig.dbs.pprl.core.common.model.impl.RecordIdComposed;
import de.unileipzig.dbs.pprl.core.common.model.impl.RecordIdPairSimple;
import de.unileipzig.dbs.pprl.core.common.model.impl.SerializableTable;
import de.unileipzig.dbs.pprl.core.common.monitoring.Tag;
import de.unileipzig.dbs.pprl.core.common.monitoring.TagTableSerialization;
import de.unileipzig.dbs.pprl.service.common.data.converter.RecordIdPairConverter;
import de.unileipzig.dbs.pprl.service.common.data.dto.RecordIdPairDto;
import de.unileipzig.dbs.pprl.service.common.data.dto.analysis.AnalysisResultDto;
import de.unileipzig.dbs.pprl.service.linkageunit.data.dto.RecordPairDto;
import de.unileipzig.dbs.pprl.service.protocol.api.EncoderApi;
import de.unileipzig.dbs.pprl.service.protocol.api.MatcherApi;
import de.unileipzig.dbs.pprl.service.protocol.model.dto.ProtocolAnalysisRequestDto;
import de.unileipzig.dbs.pprl.service.protocol.model.mongo.Layer;
import de.unileipzig.dbs.pprl.service.protocol.model.mongo.MultiLayerProtocol;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tech.tablesaw.api.Table;

import java.util.*;
import java.util.stream.Collectors;

import static de.unileipzig.dbs.pprl.core.analyzer.DataSetAnalyzer.RECORD_GROUP_ALL;
import static de.unileipzig.dbs.pprl.core.common.monitoring.TagTable.TAG_TABLE_NAME;
import static de.unileipzig.dbs.pprl.service.common.data.mongo.MongoAnalysisResult.Type.TAG_BASED_DATASET_ANALYSIS;

@Service
@Slf4j
public class AnalyzerService {

    private final DataOwnerService dataOwnerService;

    private final LinkageUnitService linkageUnitService;

    private final ProtocolService protocolService;

    public AnalyzerService(DataOwnerService dataOwnerService, LinkageUnitService linkageUnitService, ProtocolService protocolService) {
        this.dataOwnerService = dataOwnerService;
        this.linkageUnitService = linkageUnitService;
        this.protocolService = protocolService;
    }

    public SerializableTable fetchTagsAsTable(ProtocolAnalysisRequestDto request) {
        return TableSerialization.toDefaultSerializableTable(
                TagTableSerialization.convertToTable(fetchTags(request))
        );
    }

    public Collection<Tag> fetchTags(ProtocolAnalysisRequestDto request) {
        String protocolId = request.getProtocolId();
        MultiLayerProtocol protocol = protocolService.getProtocol(protocolId);
        Layer firstLayer = protocol.getLayers().getFirst();

        Collection<Tag> alltags = new ArrayList<>();
        if (!request.getParameters().getOrDefault("skipDataOwnerTags", "").isEmpty()) {
//          Collection<Tag> dataOwnerDatabaseTags = fetchDataOwnerTagsFromDatabase(protocol.getPlaintextDatasetId());
//          alltags.addAll(dataOwnerDatabaseTags);
          Collection<Tag> dataOwnerAnalysisTags = fetchDataOwnerTagsFromAnalysis(protocol.getPlaintextDatasetId());
          alltags.addAll(dataOwnerAnalysisTags);
        }

        Collection<Tag> linkageUnitRecordTags = fetchLinkageUnitRecordTags(protocol.getInitialDatasetId());

        String firstProjectId = firstLayer.getProjectId();
        Collection<Tag> linkageUnitPairTags = fetchLinkageUnitPairTags(firstProjectId);

        List<RecordIdPairDto> idPairs = linkageUnitPairTags.stream()
                .filter(t -> t.getTag().equals("SIMILARITY"))
                .map(t -> new RecordIdPairSimple(
                        RecordIdComposed.ofComposed(t.getId0()), RecordIdComposed.ofComposed(t.getId1())
                ))
                .map(RecordIdPairConverter::toDto)
                .toList();
        Collection<Tag> dataOwnerPairTags = fetchDataOwnerPairTags(protocol.getPlaintextDatasetId(), idPairs);
        alltags.addAll(dataOwnerPairTags);
        alltags.addAll(linkageUnitRecordTags);
        alltags.addAll(linkageUnitPairTags);
        log.info("Fetched in total {} tags for protocol {}", alltags.size(), protocolId);
        return alltags;
    }

    public Collection<Tag> fetchLinkageUnitPairTags(String projectId) {
        log.debug("Fetching linkage unit pair tags for project {}", projectId);
//        BatchMatchProjectDto project = linkageUnitService.getMatcherApi().getProject(projectId);
        List<RecordPairDto> recordPairs = linkageUnitService.getMatcherApi().getRecordPairs(projectId, new HashSet<>());
        List<Tag> tags = recordPairs.parallelStream()
                .map(rp -> {
                    Collection<Tag> pairTags = new ArrayList<>();
                    Double sim = rp.getSimilarity();
                    pairTags.add(Tag.create("SIMILARITY", String.format(Locale.ENGLISH, "%.2f", sim), sim));
                    pairTags.addAll(rp.getTags());
                    pairTags.forEach(t -> {
                        t.setId0(rp.getId0().getUniqueLike());
                        t.setId1(rp.getId1().getUniqueLike());
                        t.setOrigin(Tag.ORIGIN_LINKAGE_UNIT);
                        t.setType(Tag.TYPE_ENCODED);
                    });
                    return pairTags;
                })
                .flatMap(Collection::stream)
                .collect(Collectors.toUnmodifiableList());
        log.info("Fetched {} pair tags from linkage unit for project {}", tags.size(), projectId);
        return tags;
    }

    public Collection<Tag> fetchLinkageUnitRecordTags(long datasetId) {
        log.debug("Fetching linkage unit tags for dataset {}", datasetId);
        MatcherApi matcherApi = linkageUnitService.getMatcherApi();
        AnalysisResultDto analysisResult = matcherApi.getAnalysisResult(datasetId, TAG_BASED_DATASET_ANALYSIS.name());
        SerializableTable serializableTable = analysisResult.getReportGroup(RECORD_GROUP_ALL).get()
                .getReport(TAG_TABLE_NAME).get().getTable();
        Table table = TableSerialization.fromDefaultSerializableTable(serializableTable);
        Collection<Tag> tags = TagTableSerialization.convertFromTable(table);
        tags.forEach(t -> {
                    t.setOrigin(Tag.ORIGIN_LINKAGE_UNIT);
                    t.setType(Tag.TYPE_ENCODED);
                });
        log.info("Fetched {} tags from linkage unit for dataset {}", tags.size(), datasetId);
        return tags;
    }

  public Collection<Tag> fetchDataOwnerTagsFromDatabase(long datasetId) {
    log.debug("Fetching data owner tags from database for dataset {}", datasetId);
    EncoderApi encoderApi = dataOwnerService.getEncoderApi();
    List<Tag> tags = encoderApi.getTags(datasetId, null);
    tags.forEach(t -> t.setOrigin("do"));
    log.info("Fetched {} tags from data owner database for dataset {}", tags.size(), datasetId);
    return tags;
  }

  public Collection<Tag> fetchDataOwnerPairTags(long datasetId, List<RecordIdPairDto> idPairs) {
        log.info("Fetching data owner pair tags for dataset {}", datasetId);
        EncoderApi encoderApi = dataOwnerService.getEncoderApi();
        List<Tag> tags = encoderApi.getPairTags(datasetId, idPairs);
        tags.forEach(t -> {
            t.setOrigin(Tag.ORIGIN_DATA_OWNER);
            t.setType(Tag.TYPE_PLAIN);
        });
        log.info("Fetched {} pair tags for dataset {}", tags.size(), datasetId);
        return tags;
    }

    public Collection<Tag> fetchDataOwnerTagsFromAnalysis(long datasetId) {
        log.debug("Fetching data owner tags for dataset {}", datasetId);
        EncoderApi encoderApi = dataOwnerService.getEncoderApi();
        AnalysisResultDto analysisResult = encoderApi.getAnalysisResult(datasetId, TAG_BASED_DATASET_ANALYSIS.name());
        SerializableTable serializableTable = analysisResult.getReportGroup(RECORD_GROUP_ALL).get()
                .getReport(TAG_TABLE_NAME).get().getTable();
        Table table = TableSerialization.fromDefaultSerializableTable(serializableTable);
        Collection<Tag> tags = TagTableSerialization.convertFromTable(table);
        tags.forEach(t -> {
            if (t.getOrigin() == null || t.getOrigin().isEmpty()) {
                t.setOrigin(Tag.ORIGIN_DATA_OWNER);
            }
            if (t.getType() == null || t.getType().isEmpty()) {
                t.setType(Tag.TYPE_PLAIN);
            }
        });
        log.info("Fetched {} tags from data owner for dataset {}", tags.size(), datasetId);
        return tags;
    }
}

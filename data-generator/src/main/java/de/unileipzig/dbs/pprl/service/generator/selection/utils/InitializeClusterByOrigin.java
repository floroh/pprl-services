package de.unileipzig.dbs.pprl.service.generator.selection.utils;

import com.mongodb.client.MongoCollection;
import de.unileipzig.dbs.pprl.service.generator.selection.model.common.RecordCluster;
import de.unileipzig.dbs.pprl.service.generator.selection.model.common.GenericRawRecord;
import de.unileipzig.dbs.pprl.service.generator.selection.model.common.GenericRawRecordWithDates;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class InitializeClusterByOrigin implements RecordToClusterInserter {

	private List<RecordCluster> insertBatch = new ArrayList<>();

	private static final IdGenerator idGenerator = new IdGenerator(42L);
	@Override
	public boolean add(GenericRawRecord rawRecord, MongoCollection<RecordCluster> clean) {
		RecordCluster recordCluster = initializeClusterByRawRecord(rawRecord);
		insertBatch.add(recordCluster);
		if (insertBatch.size() > 1000) {
			clean.insertMany(insertBatch);
			insertBatch.clear();
		}
		return true;
	}

	@Override
	public void close(MongoCollection<RecordCluster> clean) {
		clean.insertMany(insertBatch);
	}

	public static RecordCluster initializeClusterByRawRecord(GenericRawRecord rawRecord) {
		return RecordCluster.builder().domainId(rawRecord.getVoterId())
			.origin(GenericRawRecordWithDates.builder().occursIn(Set.of(rawRecord.getSnapShot())).record(rawRecord).build()).duplicates(new ArrayList<>())
			.build();
	}
}

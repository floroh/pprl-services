package de.unileipzig.dbs.pprl.service.generator.selection.utils;

import com.mongodb.client.MongoCollection;
import de.unileipzig.dbs.pprl.service.generator.selection.model.common.RecordCluster;
import de.unileipzig.dbs.pprl.service.generator.selection.model.common.GenericRawRecord;


public interface RecordToClusterInserter {

	boolean add(GenericRawRecord rawRecord, MongoCollection<RecordCluster> clean);

	void close(MongoCollection<RecordCluster> clean);
}

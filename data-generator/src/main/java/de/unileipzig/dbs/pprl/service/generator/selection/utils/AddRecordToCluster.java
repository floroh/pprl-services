package de.unileipzig.dbs.pprl.service.generator.selection.utils;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import de.unileipzig.dbs.pprl.core.common.model.impl.PersonalAttributeType;
import de.unileipzig.dbs.pprl.service.generator.selection.model.common.*;
import org.bson.Document;

import java.util.*;

import static de.unileipzig.dbs.pprl.service.generator.selection.utils.InitializeClusterByOrigin.initializeClusterByRawRecord;


public class AddRecordToCluster implements RecordToClusterInserter {

	public static final List<String> relevantAttributes = List.of(
		PersonalAttributeType.LASTNAME.name(),
		PersonalAttributeType.FIRSTNAME.name(),
		PersonalAttributeType.MIDDLENAME.name(),
		PersonalAttributeType.NAME_SUFFIX.name(),
		PersonalAttributeType.YEAROFBIRTH.name(),
		PersonalAttributeType.SEX.name(),
		PersonalAttributeType.CITY.name(),
		PersonalAttributeType.STATE.name(),
		PersonalAttributeType.PLZ.name(),
		PersonalAttributeType.STREET.name(),
		PersonalAttributeType.STREET_NAME.name(),
		PersonalAttributeType.COUNTY_CODE.name()
	);
	private List<RecordCluster> insertBatch = new ArrayList<>();

	@Override
	public void close(MongoCollection<RecordCluster> clean) {
		if (!insertBatch.isEmpty()) {
			clean.insertMany(insertBatch);
		}
	}

	@Override
	public boolean add(GenericRawRecord rawRecord, MongoCollection<RecordCluster> clean) {
		Document byIdQuery = new Document("_id", rawRecord.getVoterId());
		MongoCursor<RecordCluster> byVoterId = clean.find(byIdQuery)
			.iterator();
		boolean isNew = true;
		if (byVoterId.hasNext()) {
			RecordCluster recordCluster = byVoterId.next();
			GenericRawRecord originRecord = recordCluster.getOrigin().getRecord();
			boolean isEqual = compareByRelevantAttributes(rawRecord, originRecord);
			if (isEqual) {
				recordCluster.getOrigin().getOccursIn().add(rawRecord.getSnapShot());
				isNew = false;
			} else {
				for (Duplicate duplicate : recordCluster.getDuplicates()) {
					GenericRawRecord duplicateRecord = duplicate.getRecord();
					boolean isDuplicate = compareByRelevantAttributes(rawRecord, duplicateRecord);
					if (isDuplicate) {
						duplicate.getOccursIn().add(rawRecord.getSnapShot());
						isNew = false;
						break;
					}
				}
				Set<DateInfo> isIn = Set.of(rawRecord.getSnapShot());
				recordCluster.getDuplicates().add(Duplicate.builder().record(rawRecord).occursIn(isIn)
					.changes(getChanges(originRecord, rawRecord)).build());
			}
			clean.replaceOne(byIdQuery, recordCluster);
		} else {
//			insertBatch.add(initializeClusterByRawRecord(rawRecord));
//			if (insertBatch.size() > 1000) {
//				clean.insertMany(insertBatch);
//				insertBatch.clear();
//			}
			clean.insertOne(initializeClusterByRawRecord(rawRecord));
		}
		return isNew;
	}

	public static boolean compareByRelevantAttributes(GenericRawRecord rawRecord, GenericRawRecord originRecord) {
		boolean isEqual = true;
		for (String attribute : relevantAttributes) {
			String originAttribute = originRecord.getAttributes().get(attribute);
			String otherAttribute = rawRecord.getAttributes().get(attribute);
			if (!Objects.equals(originAttribute, otherAttribute)) {
				isEqual = false;
				break;
			}
		}
		return isEqual;
	}

	public static Map<String, Boolean> getChanges(GenericRawRecord org, GenericRawRecord other) {
		Map<String, Boolean> changes = new HashMap<>();
		Set<String> attributeNames = new HashSet<>(org.getAttributes().keySet());
		attributeNames.addAll(other.getAttributes().keySet());
		for (String attributeName : attributeNames) {
			changes.put(attributeName,
				!Objects.equals(org.getAttributes().get(attributeName), other.getAttributes().get(attributeName)));
		}
		return changes;
	}
}

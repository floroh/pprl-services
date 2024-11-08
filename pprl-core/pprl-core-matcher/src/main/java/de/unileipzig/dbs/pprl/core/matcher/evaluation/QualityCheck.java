/*
 * Copyright © 2018 - 2020 Leipzig University (Database Research Group)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.unileipzig.dbs.pprl.core.matcher.evaluation;

import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordId;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordIdPair;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordPair;

import java.util.Collection;
import java.util.stream.Collectors;

public interface QualityCheck {
	String LINK_LABEL = "type";
	String RECALL = "Recall";
	String PRECISION = "Precision";
	String F1 = "F1-score";

	enum Label {
		TP,
		FP,
		FN,
		TN,
		FPs,
		FPd
	}

	QualityResult evaluate(Collection<RecordId> recordIds);

	QualityResult evaluatePairs(Collection<RecordIdPair> idPairs);

	default QualityResult evaluateRecordPairs(Collection<RecordPair> recordPairs) {
		Collection<RecordIdPair> ids = recordPairs.stream()
				.map(rp -> (RecordIdPair)rp)
				.collect(Collectors.toList());
		return evaluatePairs(ids);
	}

	default QualityResult evaluateRecords(Collection<Record> records) {
		Collection<RecordId> ids = records.stream()
				.map(Record::getId)
				.collect(Collectors.toList());
		return evaluate(ids);
	}
}

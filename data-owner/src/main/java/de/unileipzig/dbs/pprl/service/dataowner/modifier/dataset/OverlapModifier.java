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

package de.unileipzig.dbs.pprl.service.dataowner.modifier.dataset;


import de.unileipzig.dbs.pprl.core.common.RecordUtils;
import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordId;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OverlapModifier implements DatasetModifier {
    private double overlap = 0.5;
    private int factor = 1000;
    private int duplicatesLowerBoundary = 15;
    private int duplicatesUpperBoundary = 30;

    @Override
    public List<Record> modify(List<Record> records) {
        Map<String, List<Record>> recordsBySources = RecordUtils.groupById(records, RecordId.SOURCE_ID);
        List<Record> newRecords = new ArrayList<>();
        if (overlap > 1) {
            newRecords.addAll(recordsBySources.get("A"));
            newRecords.addAll(recordsBySources.get("B").subList(0, duplicatesUpperBoundary *factor));
            newRecords.addAll(recordsBySources.get("B").subList(duplicatesUpperBoundary *factor,
                    (int) (factor*(duplicatesUpperBoundary - duplicatesUpperBoundary *(overlap - 1)))));
        } else {
            newRecords.addAll(recordsBySources.get("A"));
            newRecords.addAll(recordsBySources.get("B")
                    .subList(0, (int) (overlap * duplicatesLowerBoundary * factor)));
            newRecords.addAll(recordsBySources.get("B")
                    .subList(duplicatesLowerBoundary * factor, duplicatesUpperBoundary * factor));
        }
        return newRecords;
    }
}

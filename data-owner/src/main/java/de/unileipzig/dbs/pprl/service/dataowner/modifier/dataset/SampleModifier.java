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

import de.unileipzig.dbs.pprl.core.common.model.api.Record;

import java.util.ArrayList;
import java.util.List;

public class SampleModifier implements DatasetModifier {

    @Override
    public List<Record> modify(List<Record> records) {
        final List<Record> sampled = new ArrayList<>();
        final int len = records.size();
        final int sampleDistance = len / 10;
        final int sampleSize = sampleDistance / 10;
        for (int i = 0; i < len; i = i + sampleDistance) {
            sampled.addAll(records.subList(i, i + sampleSize));
        }
        return sampled;
    }
}

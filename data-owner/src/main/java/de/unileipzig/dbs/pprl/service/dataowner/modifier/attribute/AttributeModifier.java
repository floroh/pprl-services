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

package de.unileipzig.dbs.pprl.service.dataowner.modifier.attribute;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import de.unileipzig.dbs.pprl.core.common.factories.AttributeFactory;
import de.unileipzig.dbs.pprl.core.common.model.api.Attribute;
import de.unileipzig.dbs.pprl.core.common.model.impl.AttributePairWithTruth;

import java.util.HashSet;
import java.util.Set;

import static de.unileipzig.dbs.pprl.core.matcher.evaluation.EvalConstants.PAIR_TYPE_MATCH;

@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, property = "@class")
public interface AttributeModifier<T> {

	T modify(T in);

	@JsonIgnore
	String getTagPostFix();

	default Attribute modify(Attribute in) {
		return modifyToAttribute((T)in.getObject());
	}

	default Attribute modifyToAttribute(T in) {
		return AttributeFactory.getAttribute(modify(in));
	}

	default Set<AttributePairWithTruth> getModifiedPairs(T in, int num) {
		Attribute raw = AttributeFactory.getAttribute(in);
		Set<AttributePairWithTruth> pairs = new HashSet<>();
		for (int i = 0; i < num; i++) {
			pairs.add(new AttributePairWithTruth(
							raw,
							modifyToAttribute(in),
							PAIR_TYPE_MATCH
			));
		}
		return pairs;
	}
}

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

import tech.tablesaw.api.Table;

public class ExtendedQualityResult extends QualityResult {
	private Table results;

	public ExtendedQualityResult(long truePos, long falsePos, long falseNeg) {
		super(truePos, falsePos, falseNeg);
	}

	public ExtendedQualityResult(QualityResult qualityResult) {
		this(qualityResult.getTruePos(), qualityResult.getFalsePos(), qualityResult.getFalseNeg());
		setFalsePosSingleton(qualityResult.getFalsePosSingleton());
		setFalsePosDuplicates(qualityResult.getFalsePosDuplicates());
	}

	public Table getResults() {
		return results;
	}

	public ExtendedQualityResult setResults(Table results) {
		this.results = results;
		return this;
	}
}

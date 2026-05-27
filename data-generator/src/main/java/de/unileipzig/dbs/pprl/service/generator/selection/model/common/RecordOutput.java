/*******************************************************************************
 *  Copyright © 2017 - 2022 Leipzig University (Database Research Group)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"). You may not
 *  use this file except in compliance with the License. You may obtain a copy of
 *  the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *  License for the specific language governing permissions and limitations under 
 * the License.
 *******************************************************************************/
package de.unileipzig.dbs.pprl.service.generator.selection.model.common;

import de.unileipzig.dbs.pprl.core.common.model.impl.PersonalAttributeType;
import de.unileipzig.dbs.pprl.core.common.model.impl.RecordIdComposed;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;
import java.util.StringJoiner;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecordOutput {

	private String party;
	private String id;
	private GenericRawRecord record;

	public String getCombinedPersonalAttributes() {
    return getCombinedPersonalAttributes(this.record);
  }

	public static String getCombinedPersonalAttributes(GenericRawRecord record) {
		final StringJoiner sj = new StringJoiner("");

		sj.add(record.getAttributes().get(PersonalAttributeType.FIRSTNAME.name()));
		sj.add(record.getAttributes().get(PersonalAttributeType.MIDDLENAME.name()));
		sj.add(record.getAttributes().get(PersonalAttributeType.LASTNAME.name()));
		String dob = record.getAttributes().get(PersonalAttributeType.DATEOFBIRTH.name());
		if (dob != null) {
			sj.add(dob);
		} else {
			sj.add(record.getAttributes().get(PersonalAttributeType.YEAROFBIRTH.name()));
		}
		sj.add(record.getAttributes().get(PersonalAttributeType.CITY.name()));
		sj.add(record.getAttributes().get(PersonalAttributeType.PLZ.name()));
//		sj.add(record.getAttributes().get(AttributeType.PLACEOFBIRTH.name()));
		return sj.toString();
	}

  public String getUniqueLikeId() {
    return RecordIdComposed.toComposedId(getId(), getParty());
  }

	@Override
	public int hashCode() {
		return Objects.hash(id, party);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RecordOutput other = (RecordOutput) obj;
		return Objects.equals(id, other.id) && Objects.equals(party, other.party);
	}


}
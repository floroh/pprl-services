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
package de.unileipzig.dbs.pprl.service.generator.selection.model.ncvr.panse;


import lombok.Data;
import org.bson.codecs.pojo.annotations.BsonProperty;

/**
 * 
 * @author mfranke
 *
 */
@Data
public class NcvrRecord {
	
	@BsonProperty(value = "_id")
	private String id = "";
	
	@BsonProperty(value = "county_id")
	private String countyId = "";
	
	@BsonProperty(value = "county_desc")
	private String countyDesc = "";
	
	@BsonProperty(value = "last_name")
	private String lastName = "";
	
	@BsonProperty(value = "first_name")
	private String firstName = "";
	
	@BsonProperty(value = "midl_name")
	private String midlName = "";
	
	@BsonProperty(value = "house_num")
	private String houseNum = "";
	
	@BsonProperty(value = "street_name")
	private String streetName = "";
	
	@BsonProperty(value = "street_type_cd")
	private String streetTypeCd = "";
	
	@BsonProperty(value = "unit_num")
	private String unitNum = "";
	
	@BsonProperty(value = "res_city_desc")
	private String resCityDesc = "";
	
	@BsonProperty(value = "state_cd")
	private String stateCd = "";
	
	@BsonProperty(value = "zip_code")
	private String zipCode = "";
	
	@BsonProperty(value = "area_cd")
	private String areaCd = "";
	
	@BsonProperty(value = "phone_num")
	private String phoneNum = "";
	
	@BsonProperty(value = "race_code")
	private String raceCode = "";
	
	@BsonProperty(value = "race_desc")
	private String raceDesc = "";
	
	@BsonProperty(value = "ethnic_code")
	private String ethnicCode = "";
	
	@BsonProperty(value = "ethnic_desc")
	private String ethnicDesc = "";
	
	@BsonProperty(value = "party_cd")
	private String partyCd = "";
	
	@BsonProperty(value = "party_desc")
	private String partyDesc = "";
	
	@BsonProperty(value = "sex_code")
	private String sexCode = "";
	
	@BsonProperty(value = "sex")
	private String sex = "";
	
	@BsonProperty(value = "age")
	private String age = "";
	
	@BsonProperty(value = "birth_place")
	private String birthPlace = "";
	
	@BsonProperty(value = "age_group")
	private String ageGroup = "";
	
	@BsonProperty(value = "yob")
	private String yob = "";
	
}
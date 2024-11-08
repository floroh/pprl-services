package de.unileipzig.dbs.pprl.service.common.csv;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import de.unileipzig.dbs.pprl.core.common.model.impl.PersonalAttributeType;
import de.unileipzig.dbs.pprl.service.common.data.dto.AttributeDto;
import de.unileipzig.dbs.pprl.service.common.data.dto.RecordDto;
import de.unileipzig.dbs.pprl.service.common.data.dto.RecordIdDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonRecord {

  @JsonUnwrapped(prefix = "id.")
  private RecordIdDto id;

  private String firstName;

  private String middleName;

  private String lastName;

  private String gender;

  private String nameAtBirth;

  private String dateOfBirth;

  private String dayOfBirth;

  private String monthOfBirth;

  private String yearOfBirth;

  private String placeOfBirth;

  private String address;

  private String plz;

  private String city;

  private String suburb;

  private String state;

  private String street;

  private String country;

  private String insuranceNumber;

  private String registrationDate;

  public RecordDto toRecordDto() {
    RecordDto.RecordDtoBuilder builder = RecordDto.builder()
      .id(this.getId());

    addIfExisting(builder, PersonalAttributeType.FIRSTNAME, this.getFirstName());
    addIfExisting(builder, PersonalAttributeType.MIDDLENAME, this.getMiddleName());
    addIfExisting(builder, PersonalAttributeType.LASTNAME, this.getLastName());
    addIfExisting(builder, PersonalAttributeType.GENDER, this.getGender());
    addIfExisting(builder, PersonalAttributeType.NAMEATBIRTH, this.getNameAtBirth());
    addIfExisting(builder, PersonalAttributeType.DATEOFBIRTH, this.getDateOfBirth());
    addIfExisting(builder, PersonalAttributeType.DAYOFBIRTH, this.getDayOfBirth());
    addIfExisting(builder, PersonalAttributeType.MONTHOFBIRTH, this.getMonthOfBirth());
    addIfExisting(builder, PersonalAttributeType.YEAROFBIRTH, this.getYearOfBirth());
    addIfExisting(builder, PersonalAttributeType.PLACEOFBIRTH, this.getPlaceOfBirth());
    addIfExisting(builder, PersonalAttributeType.ADDRESS, this.getAddress());
    addIfExisting(builder, PersonalAttributeType.PLZ, this.getPlz());
    addIfExisting(builder, PersonalAttributeType.CITY, this.getCity());
    addIfExisting(builder, PersonalAttributeType.SUBURB, this.getSuburb());
    addIfExisting(builder, PersonalAttributeType.STATE, this.getState());
    addIfExisting(builder, PersonalAttributeType.STREET, this.getStreet());
    addIfExisting(builder, PersonalAttributeType.COUNTRY, this.getCountry());
    addIfExisting(builder, PersonalAttributeType.INSURANCENUMBER, this.getInsuranceNumber());
    addIfExisting(builder, PersonalAttributeType.REGISTRATION_DATE, this.getRegistrationDate());

    return builder.build();
  }

  private static void addIfExisting(RecordDto.RecordDtoBuilder builder,
    PersonalAttributeType attrType,
    String attrValue) {
    if (attrValue != null) {
      builder.attribute(attrType.name(), toAttributeDto(attrValue));
    }
  }

  private static AttributeDto toAttributeDto(String value) {
    return AttributeDto.builder().type("STRING").value(value).build();
  }
}

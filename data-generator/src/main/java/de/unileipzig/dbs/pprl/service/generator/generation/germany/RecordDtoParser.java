package de.unileipzig.dbs.pprl.service.generator.generation.germany;

import de.unileipzig.dbs.pprl.core.common.model.impl.PersonalAttributeType;
import de.unileipzig.dbs.pprl.service.common.data.dto.AttributeDto;
import de.unileipzig.dbs.pprl.service.common.data.dto.RecordDto;
import de.unileipzig.dbs.pprl.service.common.data.dto.RecordIdDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@AllArgsConstructor
@NoArgsConstructor
public class RecordDtoParser {

  private static final Map<String, String> ATTRIBUTE_NAME_MAPPING = Map.ofEntries(
          Map.entry("gender", PersonalAttributeType.SEX.toString()),
          Map.entry("dateOfBirth", PersonalAttributeType.DATEOFBIRTH.toString()),
          Map.entry("forename", PersonalAttributeType.FIRSTNAME.toString()),
          Map.entry("surname", PersonalAttributeType.LASTNAME.toString()),
          Map.entry("zipCode", PersonalAttributeType.PLZ.toString()),
          Map.entry("location", PersonalAttributeType.CITY.toString()),
          Map.entry("street", PersonalAttributeType.STREET.toString()),
          Map.entry("placeOfBirth-FederalState", PersonalAttributeType.PLACEOFBIRTH.toString()),
          Map.entry("placeOfBirth", "CITYOFBIRTH"),
          Map.entry("federalState", PersonalAttributeType.STATE.toString()),
          Map.entry("householdRole", "HOUSEHOLD-ROLE"),
          Map.entry("householdId", "HOUSEHOLD-ID"),
          Map.entry("householdStructure", "HOUSEHOLD-STRUCTURE")
  );

  @Getter
  @Setter
  private String sourceName = "A";

  public List<RecordDto> parseRecords(List<String> csvFilePaths, List<String> attributes) {
    List<RecordDto> records = new ArrayList<>();
    int pNumber = 0;
    for (String csvFilePath : csvFilePaths) {
      log.info("Reading CSV file: " + csvFilePath);
      try (BufferedReader reader = new BufferedReader(new FileReader(csvFilePath))) {
        String line;
        while ((line = reader.readLine()) != null) {
          String[] columns = line.split(";");
          if (columns.length == 0) continue;

          // Build RecordIdDto using the first column
          RecordDto.RecordDtoBuilder builder = RecordDto.builder();

          for (int i = 0; i < attributes.size() && i < columns.length; i++) {
            String attributeName = attributes.get(i);
            String value = columns[i]; // assumes attribute values start from column 1
            if (attributeName.equals("personId")) {
              builder.id(RecordIdDto.builder()
                      .source(sourceName)
                      .local(columns[0])
                      .build()
              );
              continue;
            }
            builder.attribute(mapAttributeName(attributeName), new AttributeDto("STRING", value));
          }
          if (!attributes.contains("personId")) {
            builder.id(RecordIdDto.builder()
                    .source(sourceName)
                    .local(String.valueOf(pNumber))
                    .build());
          }
          records.add(builder.build());
          pNumber++;
        }
        log.info("Finished reading CSV file: " + csvFilePath);
      } catch (Exception e) {
        throw new RuntimeException(e.fillInStackTrace());
      }
    }
    log.info("Number of records: " + records.size());
    return records;
  }

  /**
   * Returns the enum-based attribute name if known, otherwise the original name.
   */
  public static String mapAttributeName(String originalName) {
    return Optional.ofNullable(ATTRIBUTE_NAME_MAPPING.get(originalName))
            .orElse(originalName);
  }
}

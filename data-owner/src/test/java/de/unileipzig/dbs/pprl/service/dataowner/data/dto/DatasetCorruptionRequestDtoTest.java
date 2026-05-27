package de.unileipzig.dbs.pprl.service.dataowner.data.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DatasetCorruptionRequestDtoValidationTest {

  private static Validator validator;

  @BeforeAll
  static void setup() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
  }

  @Test
  void testMissingInputDatasetId() {
    DatasetCorruptionRequestDto dto = DatasetCorruptionRequestDto.builder()
            .outputDatasetId(1L)
            .config(null)
            .configCreator(new DatasetGenerationConfigCreatorDto(0, "TEST", null))
            .build();

    Set<ConstraintViolation<DatasetCorruptionRequestDto>> violations = validator.validate(dto);
    assertEquals(1, violations.size());
    assertEquals("inputDatasetId", violations.stream().findFirst().get().getPropertyPath().toString());
  }

  @Test
  void testAtLeastOneSetValidation() {
    DatasetCorruptionRequestDto dto = DatasetCorruptionRequestDto.builder()
            .inputDatasetId(10L)
            .outputDatasetId(20L)
            .config(null)
            .configCreator(null) // neither set
            .build();

    Set<ConstraintViolation<DatasetCorruptionRequestDto>> violations = validator.validate(dto);

    assertEquals(1, violations.size());
    assertTrue(violations.stream().findFirst().get().getMessage()
            .contains("Exactly one of config or configCreator must be set"));
  }

  @Test
  void testInputDataset0() {
    DatasetCorruptionRequestDto dto = DatasetCorruptionRequestDto.builder()
            .inputDatasetId(0L)
            .configCreator(new DatasetGenerationConfigCreatorDto())
            .build();

    Set<ConstraintViolation<DatasetCorruptionRequestDto>> violations = validator.validate(dto);
    assertEquals(0, violations.size());
  }

  @Test
  void testValidDto() {
    DatasetCorruptionRequestDto dto = DatasetCorruptionRequestDto.builder()
            .inputDatasetId(10L)
            .outputDatasetId(20L)
            .configCreator(new DatasetGenerationConfigCreatorDto())
            .build();

    Set<ConstraintViolation<DatasetCorruptionRequestDto>> violations = validator.validate(dto);

    assertEquals(0, violations.size());
  }
}

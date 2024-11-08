package de.unileipzig.dbs.pprl.service.common.services;

import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import de.unileipzig.dbs.pprl.core.common.validation.api.RecordValidator;
import de.unileipzig.dbs.pprl.core.common.validation.api.ValidationResult;
import de.unileipzig.dbs.pprl.core.common.validation.impl.AttributeRegexRequirements;
import de.unileipzig.dbs.pprl.core.common.validation.impl.AvailabilityValidator;
import de.unileipzig.dbs.pprl.core.common.validation.impl.CompoundRecordValidator;
import de.unileipzig.dbs.pprl.core.common.validation.impl.RegexValidator;
import de.unileipzig.dbs.pprl.service.common.data.dto.AttributeDescriptionDto;
import de.unileipzig.dbs.pprl.service.common.data.dto.RecordRequirementsDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Validates records whether they fulfill the requirements e.g. of a specific encoding
 */
@Service
@Slf4j
public class ValidationService {

  public boolean isInvalid(Record record, RecordRequirementsDto recordValidation) {
    return !validate(record, recordValidation).isValid();
  }

  public ValidationResult validate(Record record, RecordRequirementsDto recordValidation) {
    RecordValidator recordValidator = buildValidator(recordValidation);
    return recordValidator.validate(record);
  }

  private RecordValidator buildValidator(RecordRequirementsDto validationDescription) {
    CompoundRecordValidator validator = new CompoundRecordValidator();
    validator.addRecordValidator(buildAvailabilityValidator(validationDescription));
    validator.addRecordValidator(buildRegexValidator(validationDescription));
    return validator;
  }

  private AvailabilityValidator buildAvailabilityValidator(RecordRequirementsDto validationDescription) {
    final List<String> requiredAttributes = new ArrayList<>();
    for (AttributeDescriptionDto descriptionAttribute : validationDescription.getAttributes()) {
      if (descriptionAttribute.getValidations().contains(AvailabilityValidator.MISSING)) {
        requiredAttributes.add(descriptionAttribute.getName());
      }
    }
    return new AvailabilityValidator(requiredAttributes);
  }

  private RegexValidator buildRegexValidator(RecordRequirementsDto validationDescription) {
    HashMap<String, AttributeRegexRequirements> requirements = new HashMap<>();
    for (AttributeDescriptionDto descriptionAttribute : validationDescription.getAttributes()) {
      AttributeRegexRequirements attrRequirements = new AttributeRegexRequirements();
      addPreConfigured(descriptionAttribute, attrRequirements, AttributeRegexRequirements.INCLUDES_SPACE);
      addPreConfigured(descriptionAttribute, attrRequirements, AttributeRegexRequirements.INCLUDES_HYPHEN);
      addPreConfigured(descriptionAttribute, attrRequirements, AttributeRegexRequirements.IS_EMPTY_PLACEHOLDER);
      requirements.put(descriptionAttribute.getName(), attrRequirements);
    }
    return new RegexValidator(requirements);
  }

  private void addPreConfigured(AttributeDescriptionDto descriptionAttribute,
    AttributeRegexRequirements attrRequirements, String name) {
    if (descriptionAttribute.getValidations().contains(name)) {
      attrRequirements.addReportRegex(
        AttributeRegexRequirements.PRECONFIGURED.get(name)
      );
    }
  }
}

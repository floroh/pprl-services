package de.unileipzig.dbs.pprl.core.common.validation.impl;

import de.unileipzig.dbs.pprl.core.common.model.api.Attribute;
import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import de.unileipzig.dbs.pprl.core.common.validation.api.RecordValidator;
import de.unileipzig.dbs.pprl.core.common.validation.api.ValidationResult;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexValidator implements RecordValidator {

  public static final String REQUIRED = "regex.required";
  public static final String FORBIDDEN = "regex.forbidden";
  public static final String REPORTABLE = "regex.reportable";

  private final Map<String, AttributeRegexRequirements> requirements;

  private static final Map<String, Pattern> compiledPatterns = new HashMap<>();

  public RegexValidator(Map<String, AttributeRegexRequirements> requirements) {
    this.requirements = requirements;
  }

  @Override
  public ValidationResult validate(Record record) {
    final DetailedValidationResult result = new DetailedValidationResult(true);

    for (Map.Entry<String, AttributeRegexRequirements> attributeRequirement :
      requirements.entrySet()) {
      String attributeName = attributeRequirement.getKey();
      Optional<Attribute> attribute = record.getAttribute(attributeName);
      if (attribute.isPresent()) {
        String attributeString = attribute.get().getAsString();
        AttributeRegexRequirements requirements = attributeRequirement.getValue();
        checkRequired(result, attributeName, attributeString, requirements);
        checkForbidden(result, attributeName, attributeString, requirements);
        checkReportable(result, attributeName, attributeString, requirements);
      }
    }
    return result;
  }

  /**
   * Check whether the attribute value matches a regex that must be matched
   * e.g. whether the gender attribute value is in {"m","f","d"}
   */
  private void checkRequired(DetailedValidationResult result, String attributeName, String attributeString,
    AttributeRegexRequirements requirements) {
    for (String validRegex : requirements.getValidRegex()) {
      if (!isMatch(validRegex, attributeString)) {
        result.setValid(false);
        result.addEntry(new FieldErrorCode(REQUIRED, attributeName, validRegex));
      }
    }
  }

  /**
   * Check whether the attribute value matches a regex that is not allowed
   * e.g. whether the firstname attribute does not contain spaces / consists of multiple names
   */
  private void checkForbidden(DetailedValidationResult result, String attributeName, String attributeString,
    AttributeRegexRequirements requirements) {
    for (String invalidRegex : requirements.getInvalidRegex()) {
      if (isMatch(invalidRegex, attributeString)) {
        result.setValid(false);
        result.addEntry(new FieldErrorCode(FORBIDDEN, attributeName, invalidRegex));
      }
    }
  }

  /**
   * Check whether the attribute value matches a regex that is reportable
   * e.g. if the last name contains a hyphen / is a compound name
   */
  private void checkReportable(DetailedValidationResult result, String attributeName, String attributeString,
    AttributeRegexRequirements requirements) {
    for (String reportRegex : requirements.getReportRegex()) {
      if (isMatch(reportRegex, attributeString)) {
        result.setHasReport(true);
        result.addEntry(new FieldErrorCode(REPORTABLE, attributeName, reportRegex));
      }
    }
  }

  public boolean isMatch(String regex, String value) {
    if (!compiledPatterns.containsKey(regex)) {
      compiledPatterns.put(regex, Pattern.compile(regex));
    }
    Pattern p = compiledPatterns.get(regex);
    Matcher m = p.matcher(value);
    return m.matches();
  }

}

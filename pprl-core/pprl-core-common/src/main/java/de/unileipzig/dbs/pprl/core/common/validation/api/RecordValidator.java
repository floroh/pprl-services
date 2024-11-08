package de.unileipzig.dbs.pprl.core.common.validation.api;

import de.unileipzig.dbs.pprl.core.common.model.api.Record;

public interface RecordValidator {

  ValidationResult validate(Record record);

}

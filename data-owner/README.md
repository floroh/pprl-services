# PPRL Data Owner / Encoder Service

see also swagger docs at `[url]:[port]/swagger-ui/`, e.g. for
[localhost:8081](http://localhost:8081/swagger-ui/index.html)

## Encoding

### Encode a provided plain record
POST http://localhost:8081/encode

Requestbody [EncodingRequestDto](src/main/java/de/unileipzig/dbs/pprl/service/dataowner/data/dto/EncodingRequestDto.java):
```json
{
  "encodingId": {
    "method": "DBSLeipzig/FBF/FNLNDOB",
    "project": "exampleProject"
  },
  "record": {
    "id.source": "KDLB",
    "id.local": "ID0",
    "attributes": {
      "FIRSTNAME": {
        "type": "STRING",
        "value": "Peter"
      },
      "LASTNAME": {
        "type": "STRING",
        "value": "Müller"
      },
      "DATEOFBIRTH": {
        "type": "STRING",
        "value": "06.05.1982"
      }
    }
  }
}
```

Response [RecordDto](../common/src/main/java/de/unileipzig/dbs/pprl/service/common/data/dto/RecordDto.java):
```json
{
  "id.source": "KDLB",
  "id.local": "ID0",
  "attributes": {
    "DATEOFBIRTH": {
      "type": "BITSET_BASE64",
      "value": "HcDUBU5cAaAEAgxG2Q9eUwAIMfpCUFAhcg05IaYMHE0FDYAIAYQJAAwAUaigUEoAUCAGtKkhUYBo6UnNmI0g"
    },
    "FIRSTNAME": {
      "type": "BITSET_BASE64",
      "value": "QwgkACMgAQiAQQARFAgBAEJGAAgEEUAAgQAEAQIoAJAJAJBDEZIAAAIAAAAEAEAAACAAIAAEQIuEAgAIASRBAQ=="
    },
    "LASTNAME": {
      "type": "BITSET_BASE64",
      "value": "I1AmgAoSIEACIEAwSACCZIjhAhAAUJAwDwTsAgAgMAAmAYDQgABEAEICAEHQAAAAQA1pAEEkgBCEJIEkEQBC"
    }
  },
  "encodingId": {
    "method": "DBSLeipzig/FBF/FNLNDOB",
    "project": "exampleProject"
  }
}
```

### Encode multiple provided plain records
POST http://localhost:8081/bulk-encode

Requestbody [BulkEncodingRequestDto](src/main/java/de/unileipzig/dbs/pprl/service/dataowner/data/dto/BulkEncodingRequestDto.java):
```json
{
  "encodingId": {
    "method": "DBSLeipzig/RBF/FNLNDOB",
    "project": "exampleProject"
  },
  "records": [{
    "id.source": "KDLB",
    "id.local": "ID0",
    "attributes": {
      "FIRSTNAME": {
        "type": "STRING",
        "value": "Peter"
      },
      "LASTNAME": {
        "type": "STRING",
        "value": "Müller"
      },
      "DATEOFBIRTH": {
        "type": "STRING",
        "value": "06.05.1982"
      }
    }
  },{
    "id.source": "KDLB",
    "id.local": "ID1",
    "attributes": {
      "FIRSTNAME": {
        "type": "STRING",
        "value": "Petra"
      },
      "LASTNAME": {
        "type": "STRING",
        "value": "Meier"
      },
      "DATEOFBIRTH": {
        "type": "STRING",
        "value": "06.05.1982"
      }
    }
  }]
}
```

Response: List of [RecordDto](../common/src/main/java/de/unileipzig/dbs/pprl/service/common/data/dto/RecordDto.java)
```json
[
  {
    "id.source": "KDLB",
    "id.local": "ID0",
    "attributes": {
      "RBF": {
        "type": "BITSET_BASE64",
        "value": "ogZWGgYCSRQHXIIEWpUhRfEilASABdGkOTsAFGkxzrFKIKhWYNoC72CKREVBQSkpEGEwQiRAQUAKaFk+EoFEAzKjeIhMNQQQRCB4IU6WppgxNYgQAiSAQ7EqpAPg3ASFIIEeO4SG94AUTJQzbbUOALTwQ9GEk6EVhgc1grECXQc="
      }
    },
    "encodingId": {
      "method": "DBSLeipzig/RBF/FNLNDOB",
      "project": "exampleProject"
    }
  },
  {
    "id.source": "KDLB",
    "id.local": "ID1",
    "attributes": {
      "RBF": {
        "type": "BITSET_BASE64",
        "value": "AEdCGAYCQWYHWIAAWpUphcIglACABcWEO5EAFEkgzgEIKKgWoNqK5yHABABBQSg5AEEgQiwAQQAgKAMMEoNhATKCeABMNQYUQaKYI0i05Iu5IQpSCgaUgadqNBHhVITlAIOeIIiOsoAQbPE2bDUGhLDwRwHEhymVhAcxYpCiXgU="
      }
    },
    "encodingId": {
      "method": "DBSLeipzig/RBF/FNLNDOB",
      "project": "exampleProject"
    }
  }
]
```

### Insert a plain record
Plain records can be inserted to the DO service and queried later with different
encodings.

POST http://localhost:8081/record

Requestbody [RecordDto](../common/src/main/java/de/unileipzig/dbs/pprl/service/common/data/dto/RecordDto.java):
```json
{
  "id.source": "KDLB",
  "id.local": "ID0",
  "attributes": {
    "FIRSTNAME": {
      "type": "STRING",
      "value": "Peter"
    },
    "LASTNAME": {
      "type": "STRING",
      "value": "Müller"
    },
    "DATEOFBIRTH": {
      "type": "STRING",
      "value": "06.05.1982"
    }
  }
}
```

Response [RecordIdDto](../common/src/main/java/de/unileipzig/dbs/pprl/service/common/data/dto/RecordIdDto.java):
```json
{
  "unique": "43",
  "source": "KDLB",
  "local": "ID0"
}
```

### Query an encoded record of an existing plain record

Plain records are inserted locally to the data owner service and can be
queried via their id.

POST http://localhost:8081/record/{unique-id}/encode

Requestbody [EncodingIdDto](../common/src/main/java/de/unileipzig/dbs/pprl/service/common/data/dto/EncodingIdDto.java):
```json
{
  "encodingId": {
    "method": "DBSLeipzig/RBF/FNLNDOB",
    "project": "exampleProject"
  }
}
```

Response [RecordDto](../common/src/main/java/de/unileipzig/dbs/pprl/service/common/data/dto/RecordDto.java):
```json
{
  "id.source": "KDLB",
  "id.local": "ID0",
  "attributes": {
    "RBF": {
      "type": "BITSET_BASE64",
      "value": "ogZWGgYCSRQHXIIEWpUhRfEilASABdGkOTsAFGkxzrFKIKhWYNoC72CKREVBQSkpEGEwQiRAQUAKaFk+EoFEAzKjeIhMNQQQRCB4IU6WppgxNYgQAiSAQ7EqpAPg3ASFIIEeO4SG94AUTJQzbbUOALTwQ9GEk6EVhgc1grECXQc="
    }
  },
  "encodingId": {
    "method": "DBSLeipzig/RBF/FNLNDOB",
    "project": "exampleProject"
  }
}
```

## Analysis
The service provides descriptions of the dataset in the database.

### Get supported analysis types
GET http://localhost:8081/analysis/findAll

Response:
```json
[
  "VALIDATION"
]
```

### Run a specific analysis type

POST http://localhost:8081/analysis/run

Requestbody [AnalysisRequestDto](../common/src/main/java/de/unileipzig/dbs/pprl/service/common/data/dto/analysis/AnalysisRequestDto.java):

Response [AnalysisResultDto](../common/src/main/java/de/unileipzig/dbs/pprl/service/common/data/dto/analysis/AnalysisResultDto.java):

The response contains one or more [ResultTableDto](../common/src/main/java/de/unileipzig/dbs/pprl/service/common/data/dto/analysis/ResultTableDto.java):

#### VALIDATION
Test, if the records in the database fulfill the requirements of a specific encoding method.
Two properties are given to each record:
* isValid: The record fulfills the minimal requirements, e.g. has the required attributes.
* hasReport: There are possible data quality issues with the record that should be checked, e.g. multiple 
  names in the FIRSTNAME attribute.

Requestbody:
```json
{
  "type": "VALIDATION",
  "parameters": {
    "method": "DBSLeipzig/RBF/FNLNDOB"
  }
}
```

Response contains two result tables:

1) VALIDATION_SUMMARY
  * Two columns for property name and value
  * "TOTAL": Number of records included in this analysis
  * "VALID": Number of records that are valid according to the requested encoding method and have no reports
  * "VALID_WITH_REPORT": Number of valid records that have a report
  * "INVALID": Number of records that do not fulfill the requirements of the encoding method
2) VALIDATION_RECORDS
  * Details on records that are not in category "VALID"
  * Columns:
    * "id": Unique Record id as returned when inserting a record to the database
    * "isValid": boolean
    * "hasReport": boolean
    * "errorCode": generic name of the error, e.g. "field.missing"
    * "field": name of the field with the error, e.g. "FIRSTNAME"
    * "message": optional additional description of the error
  * Note that there can be multiple rows with the same record id if that record fails multiple requirements 

```json
{
  "name": "VALIDATION",
  "tables": [
    {
      "name": "VALIDATION_SUMMARY",
      "header": [
        "name",
        "value"
      ],
      "rows": [
        {
          "values": [
            "TOTAL",
            1003
          ]
        },
        {
          "values": [
            "VALID",
            1000
          ]
        },
        {
          "values": [
            "VALID_WITH_REPORT",
            2
          ]
        },
        {
          "values": [
            "INVALID",
            1
          ]
        }
      ]
    },
    {
      "name": "VALIDATION_RECORDS",
      "header": [
        "id",
        "isValid",
        "hasReport",
        "errorCode",
        "field",
        "message"
      ],
      "rows": [
        {
          "values": [
            "6029",
            false,
            false,
            "field.missing",
            "LASTNAME",
            ""
          ]
        },
        {
          "values": [
            "6034",
            true,
            true,
            "regex.reportable",
            "FIRSTNAME",
            ".+ .+"
          ]
        },
        {
          "values": [
            "6034",
            true,
            true,
            "regex.reportable",
            "LASTNAME",
            ".+-.+"
          ]
        }
      ]
    }
  ]
}
```

## Configuration
Encoding configurations are stored in [resources/configs](src/main/resources/configs).

Each directory / matching configuration contains
- encoder.json: JSON serialisation of the
  [RecordEncoder](../pprl-core/pprl-core-encoder/src/main/java/de/unileipzig/dbs/pprl/core/encoder/record/RecordEncoder.java)
- meta.json: JSON serialisation of the corresponding
  [EncodingIdDto](../common/src/main/java/de/unileipzig/dbs/pprl/service/common/data/dto/EncodingIdDto.java)
- validation.json: optional, contains requirements for records
  ([RecordRequirementsDto](../common/src/main/java/de/unileipzig/dbs/pprl/service/common/data/dto/RecordRequirementsDto.java))

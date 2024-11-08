# PPRL Linkage Unit / Matcher Service

see also swagger docs at `[url]:[port]/swagger-ui/`, e.g. for
[localhost:8082](http://localhost:8082/swagger-ui/index.html)

## Batch matching
POST http://localhost:8082/batch/match

RequestBody [BatchMatchRequestDto](src/main/java/de/unileipzig/dbs/pprl/service/linkageunit/data/dto/BatchMatchRequestDto.java):
```json
{
    "method": "DBSLeipzig/RBF",
    "records": [{
        "id.source": "KDLB",
        "id.local": "ID123",
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
    },{
        "id.source": "KDLA",
        "id.local": "ID456",
        "attributes": {
            "RBF": {
                "type": "BITSET_BASE64",
                "value": "ogZWGgYCSRQHXIIEWpUhRfEilASABdGkOTsAFGkxzrFKIKhWYNoC72CKREVBQSkpEGEwQiRAQUAKaFk+EoFEAzaaaaaaaaAAAAAAAAAWAAAAAAAAAAAAAAAAAAAAAAAAAAAAO4SG94AUTJQzbbUOALTwQ9GEk6EVhgc1grECXQc="
            }
        },
        "encodingId": {
            "method": "DBSLeipzig/RBF/FNLNDOB",
            "project": "exampleProject"
        }
    }
    ]
}
```

Response [MatchResultDto](src/main/java/de/unileipzig/dbs/pprl/service/linkageunit/data/dto/MatchResultDto.java):
```json
{
    "recordIds": [
        {
            "source": "KDLA",
            "local": "ID123",
            "global": "0"
        },
        {
            "source": "KDLB",
            "local": "ID456",
            "global": "0"
        }
    ],
    "recordPairs": [
        {
            "id0": {
                "source": "KDLA",
                "local": "ID123",
                "global": "0"
            },
            "id1": {
                "source": "KDLB",
                "local": "ID456",
                "global": "0"
            },
            "matchGrade": "POSSIBLE_MATCH",
            "similarity": 0.858877086494689
        }
    ]
}
```

## Batch clustering
POST http://localhost:8082/batch/cluster

RequestBody [ClusteringRequestDto](src/main/java/de/unileipzig/dbs/pprl/service/linkageunit/data/dto/ClusteringRequestDto.java):
```json
{
  "method": "DBSLeipzig/RBF",
  "recordPairs": [
    {
      "id0": {
        "source": "KDLA",
        "local": "ID123"
      },
      "id1": {
        "source": "KDLB",
        "local": "ID456"
      },
      "matchGrade": "POSSIBLE_MATCH",
      "similarity": 0.858877086494689
    },
    {
      "id0": {
        "source": "KDLA",
        "local": "ID135"
      },
      "id1": {
        "source": "KDLB",
        "local": "ID246"
      },
      "matchGrade": "PROBABLE_MATCH",
      "similarity": 0.96
    }
  ]
}
```

Response [MatchResultDto](src/main/java/de/unileipzig/dbs/pprl/service/linkageunit/data/dto/MatchResultDto.java):
```json
{
  "recordIds": [
    {
      "source": "KDLB",
      "local": "ID246",
      "global": "0"
    },
    {
      "source": "KDLA",
      "local": "ID135",
      "global": "0"
    }
  ]
}
```

## Incremental matching
### Insert record
POST http://localhost:8082/

Requestbody [RecordDto](../common/src/main/java/de/unileipzig/dbs/pprl/service/common/data/dto/RecordDto.java):
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

Response [RecordIdDto](../common/src/main/java/de/unileipzig/dbs/pprl/service/common/data/dto/RecordIdDto.java):
```json
{
  "source": "KDLB",
  "local": "ID0",
  "global": "4c50ccea-0a11-4192-863b-19786018241f"
}
```

### Search record
POST http://localhost:8082/search

Requestbody [RecordDto](../common/src/main/java/de/unileipzig/dbs/pprl/service/common/data/dto/RecordDto.java):
```json
{
  "id.source": "KDLB",
  "id.local": "ID0",
  "attributes": {
    "RBF": {
      "type": "BITSET_BASE64",
      "value": "aaaaGgYCSRQHXIIEWpUhRfEilASABdGkOTsAFGkxzrFKIKhWYNoC72CKREVBQSkpEGEwQiRAQUAKaFk+EoFEAzKjeIhMNQQQRCB4IU6WppgxNYgQAiSAQ7EqpAPg3ASFIIEeO4SG94AUTJQzbbUOALTwQ9GEk6EVhgc1grECXQc="
    }
  },
  "encodingId": {
    "method": "DBSLeipzig/RBF/FNLNDOB",
    "project": "exampleProject"
  }
}
```

Response [SearchResultDto](src/main/java/de/unileipzig/dbs/pprl/service/linkageunit/data/dto/SearchResultDto.java):
```json
{
  "queryId": {
    "source": "KDLB",
    "local": "ID0"
  },
  "matches": [
    {
      "foundId": {
        "source": "KDLB",
        "local": "ID0",
        "global": "4c50ccea-0a11-4192-863b-19786018241f"
      },
      "similarity": 0.984869325997249
    }
  ]
}
```

## Configuration
Matching configurations are stored in [resources/configs](src/main/resources/configs).

Each directory / matching configuration contains
- matcher.json: JSON serialisation of the
  [Matcher](../pprl-core/pprl-core-matcher/src/main/java/de/unileipzig/dbs/pprl/core/matcher/matcher/Matcher.java)
- meta.json: JSON serialisation of the corresponding
  [MatcherIdDto](src/main/java/de/unileipzig/dbs/pprl/service/linkageunit/data/dto/MatcherIdDto.java)
- validation.json: optional, contains requirements for records 
  ([RecordRequirementsDto](../common/src/main/java/de/unileipzig/dbs/pprl/service/common/data/dto/RecordRequirementsDto.java))

The configuration to be used for batch matching must be included in the
[BatchMatchRequestDto](src/main/java/de/unileipzig/dbs/pprl/service/linkageunit/data/dto/BatchMatchRequestDto.java).

The configuration to be used for incremental matching is defined in the 
[application.yml](src/main/resources/application.yml.default) (remove .
.default suffix to use example config).

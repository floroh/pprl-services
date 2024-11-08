db = db.getSiblingDB('pprllu')
db.createCollection("meta")

db = db.getSiblingDB('pprldo')
db.createCollection("meta")

db = db.getSiblingDB('pprl-protocol')
db.createCollection("meta")

db = db.getSiblingDB('admin')
db.createUser(
  {
    user: "doadmin",
    pwd:  "doadminpw",
    roles: [ { role: "readWrite", db: "pprldo" } ]
  }
)

db.createUser(
  {
    user: "luadmin",
    pwd:  "luadminpw",
    roles: [ { role: "readWrite", db: "pprllu" } ]
  }
)
db.createUser(
  {
    user: "pmadmin",
    pwd:  "pmadminpw",
    roles: [ { role: "readWrite", db: "pprl-protocol" } ]
  }
)
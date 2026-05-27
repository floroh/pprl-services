package de.unileipzig.dbs.pprl.service.generator.selection.utils;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import de.unileipzig.dbs.pprl.service.generator.config.UsvrDbConfig;
import de.unileipzig.dbs.pprl.service.generator.selection.model.common.GenericRawRecord;
import lombok.extern.slf4j.Slf4j;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

@Slf4j
public class MongoDbUtils {

  public static MongoDatabase getMongoDatabase(String database) {
    CodecProvider pojoCodecProvider = PojoCodecProvider.builder().automatic(true).build();
    CodecRegistry pojoCodecRegistry =
      fromRegistries(getDefaultCodecRegistry(), fromProviders(pojoCodecProvider));
    log.info("Connecting to MongoDB");
    MongoClient mongoClient = MongoClients.create(new UsvrDbConfig().connectionString);

    log.info("Accessing database");
    return mongoClient.getDatabase(database).withCodecRegistry(pojoCodecRegistry);
  }

  public static MongoCollection<GenericRawRecord> getCollection(String database, String collection) {
    MongoDatabase mongoDatabase = getMongoDatabase(database);
    return mongoDatabase.getCollection(collection, GenericRawRecord.class);
  }

}

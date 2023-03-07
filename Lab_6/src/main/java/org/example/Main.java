package org.example;
import static com.mongodb.client.model.Filters.eq;

import com.mongodb.*;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

public class Main {
    public static void main(String[] args) {

        //Connecting to MongoDB Atlas
        ConnectionString connectionString = new ConnectionString("mongodb://waleed:pokemon@ac-4t9kan0-shard-00-00.p7iuvew.mongodb.net:27017,ac-4t9kan0-shard-00-01.p7iuvew.mongodb.net:27017,ac-4t9kan0-shard-00-02.p7iuvew.mongodb.net:27017/?ssl=true&replicaSet=atlas-6xg3mq-shard-0&authSource=admin&retryWrites=true&w=majority");
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .build();
        MongoClient mongoClient = MongoClients.create(settings);
        MongoDatabase database = mongoClient.getDatabase("Lab6");
        MongoCollection<Document> collection = database.getCollection("Lab6Collection");


        //Inserting document into collection
        try {
            InsertOneResult result = collection.insertOne(new Document()
                    .append("_id", new ObjectId())
                    .append("id", 2)
                    .append("name", "new name"));
            System.out.println("Success! Inserted document id: " + result.getInsertedId());
        } catch (MongoException me) {
            System.err.println("Unable to insert due to an error: " + me);
        }
        collection.find().forEach(doc -> System.out.println(doc.toJson()));


        //Updating document in collection
        Document query = new Document().append("id",  2);
        Bson updates = Updates.combine(
                Updates.set("name", "updated name"),
                Updates.addToSet("new field", "added during update"));
        UpdateOptions options = new UpdateOptions().upsert(true);
        try {
            UpdateResult result = collection.updateOne(query, updates, options);
            System.out.println("Modified document count: " + result.getModifiedCount());
            System.out.println("Upserted id: " + result.getUpsertedId());
        } catch (MongoException me) {
            System.err.println("Unable to update due to an error: " + me);
        }
        collection.find().forEach(doc -> System.out.println(doc.toJson()));


        //Deleting document in collection
        Bson deleteQuery = eq("id", 2);
        try {
            DeleteResult result = collection.deleteOne(query);
            System.out.println("Deleted document count: " + result.getDeletedCount());
        } catch (MongoException me) {
            System.err.println("Unable to delete due to an error: " + me);
        }

        //Reading documents from collection
        collection.find().forEach(doc -> System.out.println(doc.toJson()));
    }
}

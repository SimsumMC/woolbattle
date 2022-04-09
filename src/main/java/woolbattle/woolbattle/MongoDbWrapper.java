package woolbattle.woolbattle;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import static com.mongodb.client.model.Filters.eq;

public class MongoDbWrapper {

    MongoDatabase db;
    public MongoDbWrapper(MongoDatabase dbArg){
        db = dbArg;
    }

    public void set(String collectionName, Document document){
        MongoCollection<Document> collection = db.getCollection(collectionName);
        Document found = collection.find(eq("_id", document.get("_id"))).first();

        if(found == null){
            collection.insertOne(document);
            return;
        }
        collection.replaceOne(found, document);
    }

    public Document get(String collectionName, String objectID){

        return db.getCollection(collectionName).find(eq("_id", objectID)).first();
    }

}

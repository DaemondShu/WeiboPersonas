import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

/**
 * Created by 84613 on 2016/6/6.
 */
public class DataAccess {
    private MongoClient mongoClient;
    private MongoDatabase database;

    public DataAccess()
    {
        mongoClient = new MongoClient( "localhost" ,27017);
    }

    public MongoCollection<Document> getCollection(String dbName, String collectionName )
    {
        MongoDatabase database = mongoClient.getDatabase(dbName);
        return database.getCollection(collectionName);
    }


}

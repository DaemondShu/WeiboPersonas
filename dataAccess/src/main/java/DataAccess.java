import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

/**
 * Created by monkey_d_asce on 16-5-7.
 */
public class DataAccess
{
    private MongoClient mongoClient;
    private MongoDatabase database;

    public DataAccess()
    {
            mongoClient = new MongoClient( "localhost" );
    }

    public MongoCollection<Document> getCollection(String dbName, String collectionName )
    {
        MongoDatabase database = mongoClient.getDatabase(dbName);
        return database.getCollection(collectionName);
    }





}

/**
 * Created by monkey_d_asce on 16-5-7.
 */

import com.mongodb.client.MongoCollection;
import org.bson.Document;


/**
 * mongodb驱动简要使用方法http://mongodb.github.io/mongo-java-driver/3.2/driver/getting-started/quick-tour/
 */
public class TestDataAccess
{


    public static void main(String[] args)
    {
        DataAccess dao = new DataAccess();
        MongoCollection<Document> collection = dao.getCollection("local","test");

        Document doc = new Document("name", "MongoDB")
                .append("type", "database")
                .append("count", 1)
                .append("info", new Document("x", 203).append("y", 102));

        collection.insertOne(doc);
    }
}

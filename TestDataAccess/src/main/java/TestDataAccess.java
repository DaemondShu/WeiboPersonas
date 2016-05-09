/**
 * Created by monkey_d_asce on 16-5-7.
 */

import com.mongodb.client.MongoCollection;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;


/**
 * mongodb驱动简要使用方法http://mongodb.github.io/mongo-java-driver/3.2/driver/getting-started/quick-tour/
 * 这是数据库链接测试用的代码，关务逻辑
 */
public class TestDataAccess
{

    public static void main(String[] args)
    {
        DataAccess dao = new DataAccess();
        MongoCollection<Document> collection = dao.getCollection("local","test");

        ArrayList temp = new ArrayList();
        temp.add("str1");


        Document doc = new Document("name", "MongoDB")
                .append("type", "database")
                .append("count", 1)
                .append("info", new Document("x", 203).append("y", 102))
                .append("arr", temp );

        ArrayList temp2 = (ArrayList) doc.get("arr");
        temp2.add("str2");

        collection.insertOne(doc);
    }
}

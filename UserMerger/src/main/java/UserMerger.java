import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.json.JsonWriter;

import java.util.*;

/**
 * Created by monkey_d_asce on 16-5-9.
 */
public class UserMerger
{


    private MongoDatabase database;
    private Map<String,Document> userMap = new TreeMap<String, Document>();

    public UserMerger(String localhost, String local)
    {
        database = new MongoClient("localhost").getDatabase("local");
    }



    /**
     * merge by User
     * @param originCollection
     */
    public void doMerge(String originCollection)
    {


        MongoCollection<Document> collection = database.getCollection(originCollection);

        FindIterable<Document> cursor = collection.find();

        //merge(cursor.limit(1000));
        merge(cursor);
    }

    private void merge(FindIterable<Document> documents)
    {
        int i = 0;
        for (Document doc : documents )
        {
            try
            {
                //System.out.print(doc.size());
                if (doc.size() != 5) throw new Exception("bad data");
                String username = doc.getString("user");
                String content = doc.getString("content");
                String time = doc.getString("time");


                i++;
                //if (i % 1000 == 0) System.out.println(i);
                //System.out.println(doc.toJson());

                if (!userMap.containsKey(username))
                {
                    userMap.put(username,new Document()
                            .append("user",username)
                            .append("contents",new ArrayList<String>())
                            .append("times",new ArrayList<String>())
                            .append("count", 0));
                }

                Document userDoc = userMap.get(username);
                //merge content
                ArrayList contents =(ArrayList) userDoc.get("contents");
                contents.add(content);

                //merge date
                ArrayList times = (ArrayList) userDoc.get("times");
                times.add(time);

                userDoc.put("count",times.size());
            }
            catch (Exception e)
            {
                //e.printStackTrace();
                System.out.println(i);
                System.out.println(doc.toJson());
                //System.exit(0);
            }


        }
    }

    public void doSave(String destCollection)
    {
        MongoCollection collection = database.getCollection(destCollection);


        collection.insertMany(new ArrayList<Document>(userMap.values()));
    }

    public void doSaveHigher(String destCollection, int lowBound)
    {
        MongoCollection collection = database.getCollection(destCollection);
        for (Document doc : userMap.values() )
        {
            if (doc.getInteger("count") >= lowBound)
                collection.insertOne(doc);
        }

    }


}

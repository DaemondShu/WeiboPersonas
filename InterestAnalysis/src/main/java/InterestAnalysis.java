import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.corpus.tag.Nature;
import com.hankcs.hanlp.seg.Segment;
import com.hankcs.hanlp.seg.common.Term;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.geojson.Point;
import org.bson.Document;

import javax.print.Doc;
import java.util.*;

/**
 * Created by monkey_d_asce on 16-6-6.
 */
public class InterestAnalysis
{


    private MongoDatabase database;
    private Segment segment;
    private Set<Nature> natureFilter;
    private List<Document> results;

    public InterestAnalysis(String address,String databaseName)
    {
        database = new MongoClient(address).getDatabase(databaseName);
        segment = HanLP.newSegment();

        natureFilter = new HashSet<Nature>(){{
            add(Nature.nr);
            add(Nature.n);
            add(Nature.ns);
            add(Nature.nz);
            add(Nature.vn);
        }};

        results = new ArrayList<Document>();

    }


    public void doAnalysis(String originCollection)
    {
        MongoCollection<Document> collection = database.getCollection(originCollection);
        FindIterable<Document> cursor = collection.find();
        Analysis(cursor);

    }



    private void Analysis(FindIterable<Document> documents)
    {
        int i=0;
        for (Document doc : documents)
        {
            try
            {
                i++;
                if (i % 100 == 0)
                    System.out.println("do " + i + " items");
                String username = doc.getString("user");

                //Map

                Map<Nature,Map<String,Integer>> wordMap = new HashMap<Nature, Map<String, Integer>>();

                ArrayList<String> keys = (ArrayList<String>) doc.get("keyword");
                ArrayList<String> topics = (ArrayList<String>) doc.get("topic");
                ArrayList<String> interests = new ArrayList<String>();
                countByType(keys,wordMap,1);
                countByType(topics,wordMap,2);

                //System.out.println(wordMap.toString());
                for (Nature na : natureFilter)
                {
                    Map<String,Integer> type = wordMap.get(na);
                    if (type == null) continue;
                    Integer count = new Integer(0);
                    for (Map.Entry<String,Integer> item : type.entrySet())
                    {
                        count+= item.getValue();
                    }
                    int bound = (int)(count.intValue() * 0.33333333);
                    if (bound  < 2) bound = 2;

                    for (Map.Entry<String,Integer> item : type.entrySet())
                    {
                        if (item.getValue()>=bound)
                            interests.add(item.getKey());
                    }
                }
                //System.out.println(interests.toString());
                results.add(new Document().append("user",username).append("interest",interests));


            }
            catch (Exception e)
            {
                System.out.println(e.getMessage());
            }
        }
    }

    private void countByType(ArrayList<String> items,Map<Nature,Map<String,Integer>> wordMap, int rate)
    {
        for (String topic : items)
        {
            for (Term term : segment.seg(topic))
            {
                if (term.word.equals("微博")) continue; //排除“微博”
                Map<String,Integer> type = wordMap.get(term.nature);
                if (type == null)
                {
                    wordMap.put(term.nature,new HashMap<String, Integer>());
                    type = wordMap.get(term.nature);
                }

                Integer count = type.get(term.word);
                if (term.nature == Nature.n)
                {
                    type.put(term.word,count == null ? 1 : count +1);
                }
                else
                {
                    type.put(term.word,count == null ? rate : count +rate);
                }

                //System.out.printf("%s %s %d",term.word,term.nature,term.offset );
            }
            //每一个分类统计下面的
        }
    }

    public void doSave(String targetCollection)
    {
        MongoCollection<Document> collection = database.getCollection(targetCollection);
        collection.insertMany(results);

    }

}

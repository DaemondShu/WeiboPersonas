import com.hankcs.hanlp.dictionary.CoreSynonymDictionary;
import com.hankcs.hanlp.suggest.Suggester;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.whos.sa.analysis.Analysis;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
/**
 * Created by 84613 on 2016/6/6.
 */

public class EmotionAnalyse {
    public enum emotiontag {
        negetive,
        moody,
        positive,
        neutral
    };
    public static void method1(){
        //        Analysis analysis = new Analysis();
//        System.out.print(analysis.parse("我很生气。我很开心。我很生气。我很开心。我很开心。我很开心。我很生气。").getCode());


        DataAccess dao = new DataAccess();
        Analysis analysis = new Analysis();

        MongoCollection<Document> collection = dao.getCollection("mytest2","nperson2");
        MongoCollection<Document> collection1 = dao.getCollection("mytest2","nperson3");
        Document mydoc = collection.find().first();
        FindIterable<Document> findIterable = collection.find();
        MongoCursor<Document> mongoCursor = findIterable.iterator();

        int count = 0;
        int positivecount = 0;
        int negetivecount = 0;
        int neutralcount = 0;
        int moodycount = 0;

        while(mongoCursor.hasNext()) {

            Document curdata = mongoCursor.next();
            List<String> st  = (ArrayList<String>)curdata.get("comment");
            if(!st.isEmpty() && st.size() > 10) {
                count++;
                int[] totalemotion = {0, 0, 0};
                int totalcount = 0;
                for (String ncomment : st) {
                    int nemotion = analysis.parse(ncomment).getCode();
                    totalemotion[nemotion + 1]++;
                    totalcount++;
                    //System.out.println(totalemotion[0] + " " + totalemotion[1] + " " + totalemotion[2]);
//                if(totalemotion[2] > 100){
//                    int a =0;
//                }
                    //System.out.println(suggester.suggest(ncomment,1));
                    //System.out.println(ncomment + " " + nemotion);
                }
                float[] emotionrate = {0, 0, 0};
                if (totalcount != 0) {
                    emotionrate[0] = (float) totalemotion[0] / (float) totalcount;
                    emotionrate[1] = (float) totalemotion[1] / (float) totalcount;
                    emotionrate[2] = (float) totalemotion[2] / (float) totalcount;
                }
                String tag;

                if(emotionrate[0] > 0.2){
                    if(emotionrate[2] > 0.2){
                        moodycount ++;
                        tag = "喜怒无常";
                    }
                    else{
                        negetivecount ++;
                        tag = "负向消极";
                    }
                }
                else if(emotionrate[2] > 0.2){
                    positivecount++;
                    tag = "正向积极";
                }
                else{
                    neutralcount++;
                    tag = "中性";
                }

                List<Integer> ec = new ArrayList<Integer>();
                ec.add(totalemotion[0]);
                ec.add(totalemotion[1]);
                ec.add(totalemotion[2]);
                List<Float> er = new ArrayList<Float>();
                er.add(emotionrate[0]);
                er.add(emotionrate[1]);
                er.add(emotionrate[2]);


                Document document1 = new Document("_id", curdata.get("_id")).
                        append("user", curdata.get("user")).append("keyword", curdata.get("keyword")).append("topic", curdata.get("topic")).append("comment", curdata.get("comment"))
                        .append("emotioncount", ec).append("emotionrate", er).append("emotion",tag);

                List<Document> documents = new ArrayList<Document>();
                documents.add(document1);
                collection1.insertMany(documents);
            }

        }


        System.out.println("喜怒无常"+moodycount);
        System.out.println("负向消极"+negetivecount);
        System.out.println("正向积极"+positivecount);
        System.out.println("中性"+neutralcount);

        System.out.println(count + "ok");
    }

    public static void method2 () {
        DataAccess dao = new DataAccess();
        Analysis analysis = new Analysis();

        MongoCollection<Document> collection = dao.getCollection("mytest2","nperson3");
        MongoCollection<Document> collection1 = dao.getCollection("mytest2","nperson4");

        Document mydoc = collection.find().first();
        FindIterable<Document> findIterable = collection.find();
        MongoCursor<Document> mongoCursor = findIterable.iterator();

        Map<String,Double> negetiveemotionrate = new HashMap<String, Double>();
        Map<String,Double> positveemotionrate = new HashMap<String, Double>();

        int count = 0;
        while(mongoCursor.hasNext()) {
            try {
                Document curdata = mongoCursor.next();
                List<Double> st = (ArrayList<Double>) curdata.get("emotionrate");
                String id = (String) curdata.get("user");
                negetiveemotionrate.put(id, st.get(0));
                positveemotionrate.put(id, st.get(2));
            count ++;
            Document document1 = new Document("_id", curdata.get("_id")).
                       append("user", curdata.get("user")).append("keyword", curdata.get("keyword")).append("topic", curdata.get("topic")).append("comment", curdata.get("comment"))
                        .append("emotioncount", curdata.get("emotioncount")).append("emotionrate", curdata.get("emotionrate")).append("emotion","中性");
            List<Document> documents = new ArrayList<Document>();
            documents.add(document1);
            collection1.insertMany(documents);
            }
            catch(Exception e){
            }

        }

        List<Map.Entry<String, Double>> list = new ArrayList<Map.Entry<String, Double>>(positveemotionrate.entrySet());

        Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {
            //降序排序

            public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
                //return o1.getValue().compareTo(o2.getValue());
                return o2.getValue().compareTo(o1.getValue());
            }
        });

        List<Map.Entry<String, Double>> list1 = new ArrayList<Map.Entry<String, Double>>(negetiveemotionrate.entrySet());

        Collections.sort(list1, new Comparator<Map.Entry<String, Double>>() {
            //降序排序

            public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
                //return o1.getValue().compareTo(o2.getValue());
                return o2.getValue().compareTo(o1.getValue());
            }
        });
        Map<String,Integer> personemotion = new HashMap<String, Integer>();

         for(int i = 0; i < list.size() / 2 ; i++) {
             personemotion.put(list.get(i).getKey(),emotiontag.positive.ordinal());
        }
        for(int i = 0; i < list.size() / 5 ; i++) {
            if(personemotion.containsKey(list1.get(i).getKey())) {
                personemotion.put(list1.get(i).getKey(),emotiontag.moody.ordinal());
            }
            personemotion.put(list1.get(i).getKey(),emotiontag.negetive.ordinal());
        }


        int positivecount = 0;
        int negetivecount = 0;
        int neutralcount = 0;
        int moodycount = 0;

        Iterator iter = personemotion.entrySet().iterator();
        while(iter.hasNext()){

            Map.Entry entry = (Map.Entry) iter.next();
            String name = (String) entry.getKey();
            int type  = (Integer)entry.getValue();
            BasicDBObject docFind = new BasicDBObject("user", name);

            switch(emotiontag.values()[type]){
                case negetive:{
//                    BasicDBObject newDocument =new BasicDBObject().append("$set",
//                            new BasicDBObject().append("emotion","负面消极"));
//                    collection.findOneAndUpdate(new BasicDBObject().append("user", name),newDocument);

                    collection1.updateOne(Filters.eq("user", name), new Document("$set",new Document("emotion","负面消极")));
                    negetivecount ++;
                    break;
                }
                case positive:
                {
//                    BasicDBObject newDocument =new BasicDBObject().append("$set",
//                            new BasicDBObject().append("emotion","正面积极"));
//                    collection.findOneAndUpdate(new BasicDBObject().append("user", name),newDocument);
                    collection1.updateMany(Filters.eq("user", name), new Document("$set",new Document("emotion","正面积极")));
                    positivecount++;
                    break;
                }
                case moody:
                {
//                    BasicDBObject newDocument =new BasicDBObject().append("$set",
//                            new BasicDBObject().append("emotion","喜怒无常"));
                    collection1.updateMany(Filters.eq("user", name), new Document("$set",new Document("emotion","喜怒无常")));
                    moodycount++;
                    break;
                }
            }

        }
        neutralcount = count - negetivecount - positivecount - moodycount;
        System.out.println("喜怒无常"+moodycount);
        System.out.println("负向消极"+negetivecount);
        System.out.println("正向积极"+positivecount);
        System.out.println("中性"+neutralcount);

    }

    public static void main(String[] args){
        //method2();
        method1();
    }

}

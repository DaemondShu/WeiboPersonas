import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.*;

/**
 * Created by lyn on 16-5-31.
 */
public class TimeAnalysis {
    private MongoDatabase database;
    private Kmeans kmeans=new Kmeans();
    private ArrayList<Document> timeTags=new ArrayList<>();
    public TimeAnalysis(String localhost,String local)
    {

        database = new MongoClient("localhost").getDatabase("local");
    }


    private Integer getHour(String time)
    {
        String temp=time.toString();
        String timeStr=temp.split(" ")[1];
        String hourStr=timeStr.split(":")[0];
        Integer hourInt=Integer.parseInt(hourStr);
        return hourInt;
    }

    /** Hour Count
        0 : 20662
        1 : 19777
        2 : 16682
        3 : 11050
        4 : 12078
        5 : 11803
        6 : 12341
        7 : 12493
        8 : 10045
        9 : 16163
        10 : 23814
        11 : 17565
        12 : 13026
        13 : 12228
        14 : 13172
        15 : 13465
        16 : 11409
        17 : 11907
        18 : 5346
        19 : 6936
        20 : 16169
        21 : 14762
        22 : 12804
        23 : 18198
     */
    private static final String[] Tags={"夜猫子","经常值夜班或失眠","一般早起喜欢早上刷博",
            "上班空闲","习惯午餐或午休刷手机","晚餐悠闲","晚间空闲"};
    private Integer getType(Integer hour)
    {
        switch (hour)
        {
            case 23:case 0:case 1:case 2:
                return 0;
            case 3:case 4:case 5:case 6:
                return 1;
            case 7:case 8:
                return 2;
            case 9:case 10:case 14:case 15:case 16:case 17:
                return 3;
            case 11:case 12:case 13:
                return 4;
            case 18:case 19:
                return 5;
            case 20:case 21:case 22:
                return 6;
            default:
                return -1;
        }
    }
//    private static Map sortMap(Map oldMap) {
//        ArrayList<Map.Entry<String, Integer>> list = new ArrayList<>(oldMap.entrySet());
//        Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
//
//            @Override
//            public int compare(Map.Entry<String, Integer> arg0,
//                               Map.Entry<String, Integer> arg1) {
//                return arg1.getValue() - arg0.getValue();
//            }
//        });
//        Map newMap = new LinkedHashMap();
//        for (int i = 0; i < list.size(); i++) {
//            newMap.put(list.get(i).getKey(), list.get(i).getValue());
//        }
//        return newMap;
//    }

    private void makeTags(FindIterable<Document> documents)
    {

        for(Document doc:documents)
        {
            Map<String,Integer> count=new HashMap<>();
            for(int  i=0;i<7;i++)
            {
                count.put(Tags[i],0);
            }
            String username=doc.getString("user");
            ArrayList<String> tagArr=new ArrayList<>();
            for(Object time:(ArrayList)doc.get("times"))
            {

                Integer hourInt=getHour(time.toString());
                Integer type=getType(hourInt);
                count.put(Tags[type],count.get(Tags[type])+1);
            }
            Map<String,Integer> sortedcount=Kmeans.sortMap(count);
            //System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            Iterator<Map.Entry<String,Integer>> iter=sortedcount.entrySet().iterator();
            Integer index=0;
            while(iter.hasNext()&&index<3)
            {
                ++index;
                Map.Entry<String,Integer> entry=iter.next();
                if(entry.getValue()>0) {
                    tagArr.add(entry.getKey());
                    //System.out.println(entry.getKey() + " : " + entry.getValue());
                }
            }
            timeTags.add(new Document().append("user",username).append("timeTags",tagArr));


        }
    }


    public Integer[] hourCount(FindIterable<Document> documents)
    {
        Integer[] count=new Integer[24];
        for(int i=0;i<24;i++)
            count[i]=0;
        for(Document doc:documents)
        {
            try {
                for(Object time:(ArrayList)doc.get("times"))
                {
                    Integer hourInt=getHour(time.toString());
                    count[hourInt]++;
                }

            }catch(Exception e)
            {
                e.printStackTrace();
                return null;
            }

        }
        return count;
    }
    public void analyzeTime(String originCollection)
    {
        MongoCollection<Document> collection=database.getCollection(originCollection);
        FindIterable<Document> cursor=collection.find();
        kmeans.timeCluster(cursor);
        makeTags(cursor);
//        Integer[] tmp=hourCount(cursor);
//        System.out.println(" Hour Count ");
//        for(int i=0;i<tmp.length;i++)
//        {
//            System.out.println(i+" : "+tmp[i]);
//        }
    }
    public void doSave(String destCollection,String anotherDest)
    {
        MongoCollection collection=database.getCollection(destCollection);
        collection.drop();
        collection.insertMany(timeTags);
        MongoCollection another=database.getCollection(anotherDest);
        another.drop();
//        int len=kmeans.KmeansTime.size();
//        for(int i=0;i<len;i++)
//            System.out.println(kmeans.KmeansTime.toString());
        another.insertMany(kmeans.getKmeansTime());
    }

}

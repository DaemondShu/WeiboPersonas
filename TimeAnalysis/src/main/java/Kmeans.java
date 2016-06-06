import com.mongodb.client.FindIterable;
import org.bson.Document;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static java.util.Arrays.sort;

/**
 * Created by lyn on 16-6-6.
 */
public class Kmeans {
    public Kmeans(){}
    private int K=4;
    private ArrayList<ArrayList<String>> timeCluster=new ArrayList<>();
    private ArrayList<String> timeAve=new ArrayList<>();
    public ArrayList<Document> KmeansTime=new ArrayList<>();

    /**
     * Date: Long->Str
     * @param dd
     * @return
     */
    private String LongDateToStr(Long dd)
    {
        DateFormat df = new SimpleDateFormat("HH:mm");
        Date date=new Date(dd);
        return df.format(date);

    }

    /**
     * Date: Str->Long
     * @param str
     * @return
     */
    private Long StrDateToLong(String str) {
        String time=str.split(" ")[1];
        DateFormat df = new SimpleDateFormat("HH:mm");
        try{
            Date d=df.parse(time);
            Long timeLong=d.getTime();
            return timeLong;
        }catch (ParseException e)
        {
            e.printStackTrace();
            return -1L;
        }

    }

    /**
     * 找到离自己最近的中心点
     * @param distances
     * @return
     */
    private int findMin(Long[] distances)
    {
        long min=distances[0];
        int minIndex=0;
        for(int j=1;j<K;j++)
        {
            if(distances[j]<min)
            {
                min=distances[j];
                minIndex=j;
            }
        }
        return minIndex;
    }

    /**
     * kmeans的停止判断条件：形成的簇不再改变即中心点不再变化
     * @param aver1
     * @param aver2
     * @return
     */
    private boolean averageDiff(Long[] aver1,Long[] aver2)
    {
        for(int i=0;i<K;i++)
        {
            if(aver1[i]-aver2[i]!=0)
                return false;
        }
        return true;
    }

    /**
     * 将所有时间转成相应的Long型变量进行处理
     * 这样比较容易计算差值和平均值
     * @param dates
     */
    private void cluster(ArrayList<String>dates)
    {
        ArrayList<Long> dateNum=new ArrayList<>();
        for (int i=0;i<dates.size();i++)
            dateNum.add(StrDateToLong(dates.get(i)));
        if(dateNum.size()<K) {
            K = dateNum.size();
            System.out.println("K: " + K);

        }
        //初始化K个中心（初始的中心点须不同）
        //有些时间重复或小于K个
        HashSet<Long> setTmp=new HashSet<>();
        int index=0;
        while(setTmp.size()<K && index<dateNum.size())
        {
            try {
                setTmp.add(dateNum.get(index));
                index++;
            }catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        K=setTmp.size();
        Long[] distances=new Long[K];  //用于记录每个点到K个中心的dis
        Long[] averages=new Long[K];   //记录K个中心
        Long[] NewAve=new Long[K];
        Iterator<Long> iter=setTmp.iterator();
        index=0;
        while (iter.hasNext())
        {
            Long l=iter.next();
            averages[index]=l+1L;
            NewAve[index]=l;
            ++index;
        }

        //K个簇
        ArrayList<ArrayList<Long>> clusters=new ArrayList<>();
        for(int i=0;i<K;i++)
        {
            ArrayList<Long> tmp=new ArrayList<>();
            clusters.add(tmp);

        }
        while (!averageDiff(averages,NewAve)) {
            averages=NewAve.clone();
            for(int i=0;i<K;i++)
                clusters.get(i).clear();
            //计算所有点到K个中心的dis，形成新簇
            for (int i = 0; i < dateNum.size(); i++) {
                int len=averages.length;
                for (int j = 0; j < len; j++) {
                    try {
                        distances[j] = Math.abs(dateNum.get(i) - averages[j]);
                    }catch (NullPointerException e)
                    {
                        e.printStackTrace();
                        //System.out.println("j: "+j);
                    }

                }
                int minIndex = findMin(distances);
                clusters.get(minIndex).add(dateNum.get(i));
            }
            //重新计算K个中心

            for (int i = 0; i < K; i++) {
                Long sum = 0L;
                int len = clusters.get(i).size();
                for (int j = 0; j < len; j++) {
                    sum += clusters.get(i).get(j);
                }
                NewAve[i] = sum / len;

            }
        }
        timeAve.clear();timeCluster.clear();
        for(int i=0;i<clusters.size();i++)
        {
            int len = clusters.get(i).size();
            ArrayList<String> tmp=new ArrayList<>();
            for (int j = 0; j < len; j++) {
                tmp.add(LongDateToStr(clusters.get(i).get(j)));
            }
            timeCluster.add(tmp);
        }
        for(int i=0;i<K;i++)
            timeAve.add(LongDateToStr(averages[i]));
    }

    /**
     * test
     */
    private void printResult()
    {
        System.out.println("Cluster:");
        for(int i=0;i<timeCluster.size();i++)
        {
            int len = timeCluster.get(i).size();
            for(int j=0;j<len;j++)
                System.out.print(timeCluster.get(i).get(j)+"  ");
            System.out.println();
        }
        System.out.println("Average:");
        for(int i=0;i<K;i++)
            System.out.println(timeAve.get(i));
    }

    /**
     * 对map进行按值排序（因为TimeAnalysis.java也要用到，所以放在这里用static）
     * @param oldMap
     * @return
     */
    public static Map sortMap(Map oldMap) {
        ArrayList<Map.Entry<String, Integer>> list = new ArrayList<>(oldMap.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {

            @Override
            public int compare(Map.Entry<String, Integer> arg0,
                               Map.Entry<String, Integer> arg1) {
                return arg1.getValue() - arg0.getValue();
            }
        });
        Map newMap = new LinkedHashMap();
        for (int i = 0; i < list.size(); i++) {
            newMap.put(list.get(i).getKey(), list.get(i).getValue());
        }
        return newMap;
    }

    public void timeCluster(FindIterable<Document> documents)
    {
        for(Document doc:documents)
        {
            String username=doc.getString("user");
            ArrayList<String> dates=(ArrayList<String>)doc.get("times");
            cluster(dates);
            //输出的K个中心点按对应的簇的降序排列
            Map<String,Integer> map=new HashMap<>();

            for(int i=0;i<timeCluster.size();i++)
                map.put(timeAve.get(i),timeCluster.get(i).size());
            Map<String,Integer> mapTmp=sortMap(map);
            KmeansTime.add(new Document()
                    .append("user",username)
                    .append("Cluster",timeCluster.clone())
                    .append("Average",mapTmp.keySet()));
           // printResult();
        }
//        ArrayList<String> times=new ArrayList<>();
//        times.add("2 22:37");
//        times.add("2 22:38");
//        times.add("2 22:37");
//        times.add("2 22:36");
//        times.add("2 22:38");
//        times.add("2 22:36");
//        times.add("2 22:37");
//        times.add("2 22:38");
//        times.add("2 22:37");
//        times.add("2 22:37");
//        times.add("2 22:35");
//        times.add("2 22:35");
//        cluster(times);
//        printResult();
//        int len=KmeansTime.size();
//        for(int i=0;i<len;i++)
//            System.out.println(KmeansTime.toString());

    }
    public ArrayList<Document> getKmeansTime()
    {
        return KmeansTime;
    }

}

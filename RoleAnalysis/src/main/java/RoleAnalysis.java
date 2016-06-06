import com.mongodb.MongoClient;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import javax.management.relation.Role;
import javax.print.attribute.standard.DocumentName;
import java.util.*;

import static java.lang.System.out;


/**
 * Created by lyn on 16-5-17.
 * 角色分析：8-2原则 计数top20%
 */

public class RoleAnalysis {
    private MongoDatabase database;
    private static class user
    {
        public String name;
        public int pureCount;
        public int modiCount;
        public int originCount;
        user(String n,int c1,int c2,int c3){
            name=n;
            pureCount=c1;
            modiCount=c2;
            originCount=c3;
        }
    }

    public static class userCount implements Comparable<Object>
    {
        public String name;
        public int count;
        userCount(String n,int c){
            name=n;
            count=c;
        }
        public int compareTo(Object o)
        {
            userCount ucount=(userCount)o;
            if(ucount.count==count)
                return name.compareTo(ucount.name);
            else
                return Integer.valueOf(ucount.count).compareTo(Integer.valueOf(count));

        }
    }
    private int userNum;
    private ArrayList<user> record=new ArrayList<user>();
    public RoleAnalysis(String localhost,String local)
    {
        database = new MongoClient("localhost").getDatabase("local");

    }
    public void analyzeRole(String originCollection)
    {
        MongoCollection<Document> collection=database.getCollection(originCollection);
        FindIterable<Document> cursor=collection.find();
        countRepost(cursor);
    }

    public void countRepost(FindIterable<Document> documents)
    {

        int purecount=0;int modicount=0;
        userNum=0;
        for(Document doc:documents)
        {
            try {
                userNum++;
                if(doc.size()!=5)
                    throw new Exception("bad data");
                for(Object content:(ArrayList)doc.get("contents"))
                {
                    String temp=content.toString();
                    if((temp.indexOf("转发微博")==0 && temp.indexOf("【原微博】")>=0)||temp.indexOf("【原微博】")==0||temp.indexOf("//")==0)
                        purecount++;
                    else if(temp.indexOf("【原微博】")>0||temp.indexOf("//")>0)
                        modicount++;
                    else ;


                }
                int sumcount=doc.getInteger("count");
                int origincount=sumcount-purecount-modicount;
                record.add(new user(doc.getString("user"),purecount,modicount,origincount));
                purecount=0;modicount=0;

            }catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    public void doSave(String destCollection,String dest_1)
    {
        int top=(int)(userNum*0.2);
        MongoCollection collection=database.getCollection(destCollection);
        MongoCollection collection_1=database.getCollection(dest_1);
        ArrayList<Document> docs=new ArrayList<Document>();
        ArrayList<Document> tagDocs=new ArrayList<Document>();
        ArrayList<userCount> pure=new ArrayList<userCount>();
        ArrayList<userCount> modi=new ArrayList<userCount>();
        ArrayList<userCount> origin=new ArrayList<userCount>();
        for(user re:record) {
//            out.println(re.name + " : ");
//            out.println("  pureRepost: " + re.pureCount);
//            out.println("  modiRepost: " + re.modiCount);
//            out.println("  origin    : " + re.originCount);
//            out.println();
            docs.add(new Document()
                    .append("user", re.name)
                    .append("pureRepost", re.pureCount)
                    .append("modiRepost", re.modiCount)
                    .append("origin", re.originCount));
            pure.add(new userCount(re.name, re.pureCount));
            modi.add(new userCount(re.name, re.modiCount));
            origin.add(new userCount(re.name, re.originCount));
        }
        Collections.sort(pure);Collections.sort(modi);Collections.sort(origin);
        int pureNum=pure.get(top).count;
        int modiNum=modi.get(top).count;
        int originNum=origin.get(top).count;
        for (user re:record)
        {
            ArrayList<String> tags=new ArrayList<String>();
            if(re.pureCount>=pureNum) tags.add("纯转发");
            else if(re.modiCount>=modiNum) tags.add("加工转发");
            else if(re.originCount>=originNum) tags.add("原创");
            else;
            tagDocs.add(new Document().append("user",re.name).append("roleTags",tags.clone()));
        }




        collection.drop();collection_1.drop();
        collection.insertMany(docs);
        collection_1.insertMany(tagDocs);
    }




}

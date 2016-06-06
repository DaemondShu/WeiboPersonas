/**
 * Created by monkey_d_asce on 16-5-7.
 */

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.dictionary.CoreSynonymDictionary;
import com.hankcs.hanlp.seg.CRF.CRFSegment;
import com.hankcs.hanlp.seg.Dijkstra.DijkstraSegment;
import com.hankcs.hanlp.seg.NShort.NShortSegment;
import com.hankcs.hanlp.seg.Segment;
import com.hankcs.hanlp.seg.common.Term;
import com.hankcs.hanlp.suggest.Suggester;
import com.hankcs.hanlp.tokenizer.IndexTokenizer;
import com.hankcs.hanlp.tokenizer.NLPTokenizer;
import com.hankcs.hanlp.tokenizer.StandardTokenizer;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.codecs.DoubleCodec;

import javax.print.Doc;
import java.lang.reflect.Array;
import java.util.*;

import static com.hankcs.hanlp.dictionary.py.Shengmu.t;


/**
 * mongodb驱动简要使用方法http://mongodb.github.io/mongo-java-driver/3.2/driver/getting-started/quick-tour/
 * 这是数据库链接测试用的代码，关务逻辑
 */
public class WordsDivision
{

    public static void main(String[] args)
    {
        DataAccess dao = new DataAccess();

        MongoCollection<Document> collection = dao.getCollection("mytest","person");

        MongoCollection<Document> collection1 = dao.getCollection("mytest2","nperson1");


        Document mydoc = collection.find().first();
        FindIterable<Document> findIterable = collection.find();
        MongoCursor<Document> mongoCursor = findIterable.iterator();
        while(mongoCursor.hasNext()){
            //Array.gets
            Map<String,Integer> keywords = new HashMap<String,Integer>();
            //Map<String,int> topic = new Map<String,int>();
            Document curdata = mongoCursor.next();
            List<String> st  = (ArrayList<String>)curdata.get("contents");
            int count = (Integer)curdata.get("count");
            List<String> comments = new ArrayList<String>();

            List<String> topics = new ArrayList<String>();
            for(String ncountent :st) {
                int index = ncountent.indexOf("【原微博】");

                String nstring = ncountent;
                while(nstring.indexOf("#") >= 0) {
                    int index1 = nstring.indexOf("#");
                    String ntopic = nstring.substring(index1+1);
                    if(ntopic.indexOf("#") >=0) {
                        int index2 = ntopic.indexOf("#");
                        String gettopic = ntopic.substring(0, index2);
                        if(topics.contains(gettopic)){
                            nstring = ntopic.substring(index2 +1);
                            continue;
                        }
                        topics.add(gettopic);
                        nstring = ntopic.substring(index2 +1);
                    }
                    else{
                        break;
                    }
                }


                if (index <= 0) {
                    //System.out.println(ncountent);
                    comments.add(ncountent);
                    continue;
                } else {
                    String origin = ncountent.substring(ncountent.indexOf("【原微博】"));

                    //System.out.println(origin);
                    //System.out.println(ncountent.substring(0,1));
                    //System.out.println(ncountent.substring(0,3));
                    if (ncountent.substring(0, 2).equals("//") || ncountent.substring(0, 4).equals("转发微博")) {

                    } else {
                        //System.out.println(ncountent.substring(0,1));
                        // TODO: 2016/6/2
                        //System.out.println(ncountent);
                        int in = ncountent.indexOf("//");
                        if (in <= 0) {

                        } else {
                            String curcomment = ncountent.substring(0, in);
                            int nin = curcomment.indexOf("【原微博】");
                            if (nin >= 1) {
                                String ncurcomment = ncountent.substring(0, nin);
                                comments.add(ncurcomment);
                                //System.out.println(ncurcomment);
                            } else {
                                //System.out.println(curcomment);
                                comments.add(curcomment);
                            }
                        }
                    }


                    //提取关键词
                    List<String> keywordList = HanLP.extractKeyword(origin, 20);
                    //System.out.println(keywordList);


                    for (String ktemp : keywordList) {
                        //取得词性
                        List<Term> termList = StandardTokenizer.segment(ktemp);

//                    for (Term nt : termList) {
//                        System.out.println(nt);
//                    }

                        if (termList.get(0).nature.name().charAt(0) == 'n') {
                            //if(termList.get(0).nature.name().len)
                            if (termList.get(0).nature.name().length() >= 2 && termList.get(0).nature.name().charAt(1) == 'x') {
                                continue;
                            }
                            String nkeyword = termList.get(0).word;

                            if (!keywords.containsKey(nkeyword)) {
                                keywords.put(nkeyword, 1);
                            } else {
                                int s = keywords.get(nkeyword);
                                s += 1;
                                keywords.put(nkeyword, s);
                            }
                            //System.out.println(termList);
                        }
                    }

                }
            }

            List<String> kwords = new ArrayList<String>();

            Iterator iter = keywords.entrySet().iterator();
            while(iter.hasNext()){

                Map.Entry entry = (Map.Entry) iter.next();
                if((Integer)entry.getValue() <= count / 5){
                    continue;
                }
                kwords.add(entry.getKey().toString());
                //System.out.println(entry.getKey().toString());

            }
            Document document1 = new Document("_id",curdata.get("_id")).
            append("user",curdata.get("user")).append("keyword",kwords).append("topic",topics);

            List<Document> documents = new ArrayList<Document>();
            documents.add(document1);
            collection1.insertMany(documents);

        }
        System.out.println("ok\n");
//
//        MongoCollection<Document> collection1 = dao.getCollection("mytest2","nperson");
//        List<String> t = new ArrayList<String>();
//        t.add("1");
//        t.add("2");
//
//        Document document1 = new Document("_id","123").
//        append("name","moody").append("content",t).append("time","104/3/2");
//        List<Document> documents = new ArrayList<Document>();
//        documents.add(document1);
//        collection1.insertMany(documents);

//        System.out.println(mydoc.toJson());
//        System.out.println(HanLP.segment("你好" ));
//        String content = "转发微博。 【原微博】 #2016年流行发色#人的一生，便像天上的明星，上海整容整形美容医院，#隆鼻#除皱针从耀眼到陨落，都是一场尽情的驰骋，单眼皮无痕开眼角，那些灼灼其华，或是平淡静美，都会散缀在天幕，纵然永远消失在视野，也只是飘将在另一个世界，便去念想着存在，如是而已，又情何以堪呢";
//        Segment nShortSegment = new NShortSegment().enableCustomDictionary(false).enablePlaceRecognize(true).enableOrganizationRecognize(true);
//        Segment shortestSegment = new DijkstraSegment().enableCustomDictionary(false).enablePlaceRecognize(true).enableOrganizationRecognize(true);
//        System.out.println("N-最短分词：" + nShortSegment.seg(content) + "\n最短路分词：" + shortestSegment.seg(content));
//        List<String> keywordList = HanLP.extractKeyword(content,10);
//        System.out.println(keywordList);
//
//        for ( String temp: keywordList
//             ) {
//            List<Term> termList = StandardTokenizer.segment(temp);
//            System.out.println(termList);
//            for ( Term term : termList
//                 ) {
//                System.out.println(term.nature.name());
//            }
//        }
//        List<Term> termList = StandardTokenizer.segment("");
//
//        System.out.println(termList);

    }


}

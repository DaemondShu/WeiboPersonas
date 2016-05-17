import com.mongodb.MongoClient;

import com.mongodb.client.MongoDatabase;

import javax.management.relation.Role;

/**
 * Created by lyn on 16-5-17.
 * 角色分析：8-2原则 计数top20%
 */

public class RoleAnalysis {
    private MongoDatabase database;
    public RoleAnalysis(String localhost,String local)
    {
        database = new MongoClient("localhost").getDatabase("local");
    }
}

/**
 * Created by lyn on 16-5-17.
 */
public class RoleMain {
    public static void main(String args[])
    {

        RoleAnalysis roleAnalysis=new RoleAnalysis("localhost","local");
        //System.out.println("arg1: "+args[0]);
        roleAnalysis.analyzeRole(args[0]);
        //System.out.println("args2: "+args[1]);
        roleAnalysis.doSave(args[1],args[2]);
    }
}

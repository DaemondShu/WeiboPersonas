

/**
 * Created by monkey_d_asce on 16-5-9.
 */
public class Launcher
{




    public static void main(String[] args)
    {


        UserMerger userMerger = new UserMerger("localhost","local");
        long start =  System.currentTimeMillis();

        userMerger.doMerge(args[0]);
        System.out.println("merge ok:" + (System.currentTimeMillis()-start) + "ms");
        //userMerger.doSave(args[1]);
        userMerger.doSaveHigher(args[1],10);
        System.out.println("insert ok:"+ (System.currentTimeMillis()-start) + "ms");
    }






}

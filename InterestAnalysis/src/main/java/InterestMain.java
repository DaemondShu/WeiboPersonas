

/**
 * Created by monkey_d_asce on 16-6-6.
 */


public class InterestMain
{
    public static void main(String[] args)
    {
        InterestAnalysis test = new InterestAnalysis("localhost","local");
        test.doAnalysis(args[0]);
        test.doSave(args[1]);
    }

}

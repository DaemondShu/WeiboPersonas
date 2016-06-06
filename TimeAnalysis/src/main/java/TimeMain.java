/**
 * Created by lyn on 16-5-31.
 */
public class TimeMain {
    public static void main(String args[])
    {
        TimeAnalysis timeAnalysis=new TimeAnalysis("localhost","local");
        System.out.println(args[0]);
        timeAnalysis.analyzeTime(args[0]);
        timeAnalysis.doSave(args[1],args[2]);

    }
}

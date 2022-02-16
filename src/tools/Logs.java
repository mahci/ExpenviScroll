package tools;

import java.util.ArrayList;
import java.util.List;

public class Logs {

    private static List<String> toLogList = new ArrayList<>();

    static {
//        toLogList.add("Server");
//        toLogList.add("Memo");
//        toLogList.add("ExperimentPanel");
//        toLogList.add("DemoPanel");
//        toLogList.add("Controller");
//        toLogList.add("MyScrollBarUI");
        toLogList.add("VTScrollPane");
        toLogList.add("TDScrollPane");
//        toLogList.add("Experiment");
//        toLogList.add("Round");
//        toLogList.add("TechConfigPanel");
//        toLogList.add("MainFrame");
    }

    public static void d(String tag, Object... params) {
        if(params.length > 0 && showTag(tag)) {
            StringBuilder sb = new StringBuilder();
            for(Object p : params) {
                sb.append(p).append(" | ");
            }
            System.out.println(tag + " >> " + sb);
        }
    }

    private static boolean showTag(String tag) {
        return toLogList.contains(tag.split("/")[0]);
    }

}

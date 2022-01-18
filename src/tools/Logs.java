package tools;

import experiment.Experiment;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Logs {

    private static List<String> toLogList = new ArrayList<>();

    static {
        toLogList.add("Server");
        toLogList.add("ExperimentPanel");
//        toLogList.add("Controller");
//        toLogList.add("MyScrollBarUI");
//        toLogList.add("VTScrollPane");
//        toLogList.add("TDScrollPane");
//        toLogList.add("Experiment");
//        toLogList.add("Round");
//        toLogList.add("TechConfigPanel");
//        toLogList.add("MainFrame");
    }

    public static void addTag(String tag) {
        toLogList.add(tag);
        System.out.println(toLogList);
    }

    public static void error(String tag, String mssg) {
        String cName = tag.split("/")[0];
        if (toLogList.contains(cName)) System.out.println(tag + " >> " + mssg);
    }

    public static void info(String tag, String mssg) {
        String cName = tag.split("/")[0];
        if (toLogList.contains(cName)) System.out.println(tag + " >> " + mssg);
    }

    public static void d(String tag, String... params) {
        if(params.length > 0 && showTag(tag)) {
            StringBuilder sb = new StringBuilder();
            for(String p : params) {
                sb.append(p).append(" | ");
            }
            System.out.println(tag + " >> " + sb);
        }
    }

    public static void d(String tag, int... params) {
        if(params.length > 0 && showTag(tag)) {
            StringBuilder sb = new StringBuilder();
            for(int p : params) {
                sb.append(p).append(" | ");
            }
            System.out.println(tag + " >> " + sb);
        }
    }

    public static void d(String tag, Pair... params) {
        if(params.length > 0 && showTag(tag)) {
            StringBuilder sb = new StringBuilder();
            for(Pair p : params) {
                sb.append(p).append(" | ");
            }
            System.out.println(tag + " >> " + sb);
        }
    }

    public static void d(String tag, Experiment.DIRECTION... params) {
        if(params.length > 0 && showTag(tag)) {
            StringBuilder sb = new StringBuilder();
            for(Experiment.DIRECTION p : params) {
                sb.append(p).append(" | ");
            }
            System.out.println(tag + " >> " + sb);
        }
    }

    public static void d(String tag, String name, int... params) {
        if(params.length > 0 && showTag(tag)) {
            StringBuilder sb = new StringBuilder(tag)
                    .append(">>").append(name).append(": ");
            for(int p : params) {
                sb.append(p).append(" | ");
            }
            System.out.println(sb);
        }
    }

    public static void d(String tag, String name, double... params) {
        if(params.length > 0 && showTag(tag)) {
            StringBuilder sb = new StringBuilder(tag)
                    .append(">>").append(name).append(": ");
            for(double p : params) {
                sb.append(p).append(" | ");
            }
            System.out.println(sb);
        }
    }

    public static void d(String tag, boolean... params) {
        if(params.length > 0 && showTag(tag)) {
            StringBuilder sb = new StringBuilder();
            for(boolean p : params) {
                sb.append(p).append(" | ");
            }
            System.out.println(tag + " >> " + sb);
        }
    }

    public static void d(String tag, Experiment.TECHNIQUE mssg) {
        System.out.println(tag + " >> " + mssg);
    }

    public static void infoAll(String tag, String mssg) {
        System.out.println(tag + " >> " + mssg);
    }

    private static boolean showTag(String tag) {
        return toLogList.contains(tag.split("/")[0]);
    }

}

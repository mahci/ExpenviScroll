package tools;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Logs {

    private static List<String> toLogList = new ArrayList<>();

    static {
//        toLogList.add("Server");
//        toLogList.add("ExperimentPanel");
        toLogList.add("Controller");
        toLogList.add("TDScrollPane");
//        toLogList.add("MyScrollBarUI");
        toLogList.add("VTScrollPane");
        toLogList.add("Experiment");
        toLogList.add("Round");
    }

    public static void addTag(String tag) {
        toLogList.add(tag);
        System.out.println(toLogList);
    }

    public static void error(String tag, String mssg) {
        String cName = tag.split("/")[0];
        if (toLogList.contains(cName)) System.out.println(tag + " >> " + mssg);
    }

    public static void errorAll(String tag, String mssg) {
        System.out.println(tag + " >> " + mssg);
    }

    public static void info(String tag, String mssg) {
        String cName = tag.split("/")[0];
        if (toLogList.contains(cName)) System.out.println(tag + " >> " + mssg);
//        System.out.println(tag + " >> " + mssg);
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

    public static void d(String tag, Pair mssg) {
        System.out.println(tag + " >> " + mssg);
    }

    public static void infoAll(String tag, String mssg) {
        System.out.println(tag + " >> " + mssg);
    }

    public static void infoAll(String tag, String s1, String s2) {
        System.out.println(tag + " >> " + s1 + " | " + s2);
    }

    public static void infoMulti(String tag, int s1, int s2) {
        System.out.println(tag + " >> " + s1 + " | " + s2);
    }

    public static void infoAll(String tag, Dimension dim) {
        System.out.println(tag + " >> " + dim);
    }

    public static void infoAll(String tag, double d) {
        System.out.println(tag + " >> " + d);
    }

    public static void infoAll(String tag, int mssg) {
        System.out.println(tag + " >> " + mssg);
    }

    public static void infoAll(String tag, int i1, int i2) {
        System.out.println(tag + " >> " + i1 + " | " + i2);
    }

    public static void d(String tag, int mssg) {
        System.out.println(tag + " >> " + mssg);
    }

    public static void d(String tag, double mssg) {
        System.out.println(tag + " >> " + mssg);
    }

    private static boolean showTag(String tag) {
        return toLogList.contains(tag.split("/")[0]);
    }

}

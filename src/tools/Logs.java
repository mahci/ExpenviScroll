package tools;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Logs {

    private static List<String> toLogList = new ArrayList<>();

    public Logs() {
        toLogList.add("Server");
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
    }

    public static void infoAll(String tag, String mssg) {
        System.out.println(tag + " >> " + mssg);
    }

    public static void infoAll(String tag, String s1, String s2) {
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

    public static void info(String tag, int mssg) {
        System.out.println(tag + " >> " + mssg);
    }

    public static void info(String tag, double mssg) {
        System.out.println(tag + " >> " + mssg);
    }
}

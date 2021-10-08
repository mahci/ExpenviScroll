package tools;

public class Logs {

    public static void error(String clsName, String mssg) {

    }

    public static void info(String tag, String mssg) {
        System.out.println(tag + " >> " + mssg);
    }

    public static void info(String tag, int mssg) {
        System.out.println(tag + " >> " + mssg);
    }

    public static void info(String tag, double mssg) {
        System.out.println(tag + " >> " + mssg);
    }
}

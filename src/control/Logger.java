package control;

import experiment.Trial;
import gui.Main;
import tools.Logs;
import tools.Utils;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static tools.Consts.STRINGS.*;
import static experiment.Experiment.*;

public class Logger {
    private final static String NAME = "Logger/";
    // -------------------------------------------------------------------------------------------
    private static Logger self;

    private static String mLogFolderPath; // Main folder for logs

    // Different log files
    private String mTrialsLogPath;
    private PrintWriter mTrialsLogFile;

    private String mInstantsLogPath;
    private PrintWriter mInstantLogFile;

    private String mTimesLogPath;
    private PrintWriter mTimesLogFile;

    private String mEventsLogPath;
    private PrintWriter mEventsLogFile;

    private String mExpLogId;

    private long mHomingStTime;
    // -------------------------------------------------------------------------------------------

    /**
     * Get the instance
     * @return Singleton instance
     */
    public static Logger get() {
        if (self == null) self = new Logger();
        return self;
    }

    /**
     * Private constructor
     */
    private Logger() {
        // Create logging folder
        final Path parentPatn = Paths.get("").toAbsolutePath().getParent();
        mLogFolderPath = parentPatn.toAbsolutePath() + "/Expenvi-Scroll-Logs/";
        createFolder(mLogFolderPath);
    }

    /**
     * Log when a new particiapnt starts (create folder)
     * @param pId Participant's ID
     */
    public void logParticipant(int pId) {
        final String TAG = NAME + "logParticipant";

        mExpLogId = P_INIT + pId + "_" + Utils.nowDateTime(); // Experiment Id

        // Create log files for the participant
        final String logFileInit = mLogFolderPath + mExpLogId;
        mTrialsLogPath = logFileInit+ "_" + "TRIALS.txt";
        mInstantsLogPath = logFileInit + "_" + "INSTANTS.txt";
        mTimesLogPath = logFileInit + "_" + "TIMES.txt";
        mEventsLogPath = logFileInit+ "_" + "EVENTS.txt";

        // Write columns in log files
        try {
            mTrialsLogFile = new PrintWriter(new FileWriter(mTrialsLogPath));
            mTrialsLogFile.println(GeneralInfo.getLogHeader() + SP + TrialInfo.getLogHeader());
            mTrialsLogFile.flush();

            mInstantLogFile = new PrintWriter(new FileWriter(mInstantsLogPath));
            mInstantLogFile.println(GeneralInfo.getLogHeader() + SP + InstantInfo.getLogHeader());
            mInstantLogFile.flush();

            mTimesLogFile = new PrintWriter(new FileWriter(mTimesLogPath));
            mTimesLogFile.println(GeneralInfo.getLogHeader() + SP + TimeInfo.getLogHeader());
            mTimesLogFile.flush();

        } catch (IOException e) {
            e.printStackTrace();
            Main.showDialog("Problem in logging the participant!");
        }

    }

    /**
     * Log TrialInfo
     * @param genInfo GeneralInfo
     * @param trialInfo TrialInfo
     */
    public void logTrialInfo(GeneralInfo genInfo, TrialInfo trialInfo) {
        final String TAG = NAME + "logTrialInfo";

        try {
            if (mTrialsLogFile == null) { // Open only if not opened before
                mTrialsLogFile = new PrintWriter(new FileWriter(mTrialsLogPath, true));
            }

            mTrialsLogFile.println(genInfo + SP + trialInfo);
            mTrialsLogFile.flush();

        } catch (NullPointerException | IOException e) {
            Main.showDialog("Problem in logging instant!");
        }
    }

    /**
     * Log InstantInfo
     * @param genInfo GeneralInfo
     * @param instInfo InstantInfo
     */
    public void logInstantInfo(GeneralInfo genInfo, InstantInfo instInfo) {
        final String TAG = NAME + "logInstant";

        try {
            if (mInstantLogFile == null) { // Open only if not opened before
                mInstantLogFile = new PrintWriter(new FileWriter(mInstantsLogPath, true));
            }

            mInstantLogFile.println(genInfo + SP + instInfo);
            mInstantLogFile.flush();

        } catch (NullPointerException | IOException e) {
            Main.showDialog("Problem in logging instant!");
        }
    }

    /**
     * Log TimeInfo
     * @param genInfo GeneralInfo
     * @param timeInfo TimeInfo
     */
    public void logTimeInfo(GeneralInfo genInfo, TimeInfo timeInfo) {
        final String TAG = NAME + "logInstant";

        try {
            if (mTimesLogFile == null) { // Open only if not opened before
                mTimesLogFile = new PrintWriter(new FileWriter(mTimesLogPath, true));
            }

            mTimesLogFile.println(genInfo + SP + timeInfo);
            mTimesLogFile.flush();

        } catch (NullPointerException | IOException e) {
            Main.showDialog("Problem in logging instant!");
        }
    }

    public void startHomingTime() {
        mHomingStTime = Utils.nowInMillis();
    }

    public long getHomingStTime() {
        final long result = mHomingStTime;
        mHomingStTime = 0; // reset the time

        return result;
    }

    /**
     * Create a directory
     * @param path Directory path
     */
    private int createFolder(String path) {
        final String TAG = NAME + "createDir";

        Path dir = Paths.get(path);
        try {
            // Create the directory only if not existed
            if (!Files.isDirectory(dir)) Files.createDirectory(dir);
            return 0;
        } catch (IOException e) {
            Logs.d(TAG, "Problem in creating dir: " + path);
            e.printStackTrace();
            return 1;
        }
    }

    // -------------------------------------------------------------------------------------------
    /***
     * General info regarding every trial
     */
    public static class GeneralInfo {
        public TECHNIQUE tech;
        public int blockNum;
        public int trialNum;

        public static String getLogHeader() {
            return "technique" + SP +
                    "task" + SP +
                    "block_num" + SP +
                    "trial_num";
        }

        @Override
        public String toString() {
            return tech + SP +
                    blockNum + SP +
                    trialNum + SP;
        }
    }

    /**
     * All times in ms
     */
    public static class TrialInfo {
        public Trial trial;
        public int searchTime; // From the first scroll until the target appeared the last time
        public int fineTuneTime; // From the last appearance of target to the last scroll
        public int scrollTime; // searchTime + fineTuneTime
        public int result; // 1 (Hit) or 0(Miss)

        public static String getLogHeader() {
            return "direction" + SP +
                    "distance" + SP +
                    "frame_size" + SP +
                    "search_time" + SP +
                    "fine_tune_time" + SP +
                    "scroll_time" + SP +
                    "result";
        }

        @Override
        public String toString() {
            return trial.toLogString() + SP +
                    searchTime + SP +
                    fineTuneTime + SP +
                    scrollTime + SP +
                    result;
        }
    }

    /**
     * All times in system timestamp
     */
    public static class InstantInfo {
        public long trialShow;
        public long firstEntry;
        public long lastEntry;
        public long firstScroll;
        public long lastScroll;
        public long trialEnd;

        public static String getLogHeader() {
            return "trial_show" + SP +
                    "first_entry" + SP +
                    "last_entry" + SP +
                    "first_scroll" + SP +
                    "last_scroll" + SP +
                    "trial_end";
        }

        @Override
        public String toString() {
            return trialShow + SP +
                    firstEntry+ SP +
                    lastEntry + SP +
                    firstScroll + SP +
                    lastScroll + SP +
                    trialEnd;
        }
    }

    public static class TimeInfo {
        public long trialTime; // In millisec
        public long blockTime; // In millisec
        public int techTaskTime; // Each tech|task (In sec)
        public long homingTime; // In millisec
        public int techTime; // In sec
        public int experimentTime; // In sec

        public static String getLogHeader() {
            return "trial_time" + SP +
                    "block_time" + SP +
                    "tech_task_time" + SP +
                    "homing_time" + SP +
                    "tech_time" + SP +
                    "experiment_time";
        }

        @Override
        public String toString() {
            return trialTime + SP +
                    blockTime+ SP +
                    techTaskTime + SP +
                    homingTime + SP +
                    techTime + SP +
                    experimentTime;
        }
    }
}

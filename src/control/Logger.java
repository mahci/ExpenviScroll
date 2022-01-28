package control;

import experiment.Trial;
import gui.Main;
import gui.MainFrame;
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

    private static Path mLogDirectory; // Main folder for logs
    private static Path mPcLogDirectory; // Folder log path of the participant

    // Different log files
    private Path mTrialsFilePath;
    private PrintWriter mTrialsFilePW;

    private Path mInstantsLogPath;
    private PrintWriter mInstantFilePW;

    private Path mTimesFilePath;
    private PrintWriter mTimesFilePW;

    private Path mEventsFilePath;
    private PrintWriter mEventsFilePW;

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
        // Create log directory
        final Path parentPatn = Paths.get("").toAbsolutePath().getParent();
        mLogDirectory = parentPatn.resolve("Expenvi-Scroll-Logs");

        if (!Files.isDirectory(mLogDirectory)) {
            try {
                Files.createDirectory(mLogDirectory);
            } catch (IOException ioe) {
                MainFrame.get().showMessage("Problem in creating log directory!");
                ioe.printStackTrace();
            }
        }
    }

    /**
     * Log when a new particiapnt starts (create folder)
     * @param pId Participant's ID
     */
    public void logParticipant(int pId) {
        final String TAG = NAME + "logParticipant";

        final String pcLogId = P_INIT + pId;
        final String pcExpLogId = pcLogId + "_" + Utils.nowDateTime(); // Experiment Id

        // Create a folder for the participant (if not already created)
        mPcLogDirectory = mLogDirectory.resolve(pcLogId);
        if (!Files.isDirectory(mPcLogDirectory)) {
            try {
                Files.createDirectory(mPcLogDirectory);
            } catch (IOException ioe) {
                MainFrame.get().showMessage("Problem in creating log directory for participant: " + pId);
                ioe.printStackTrace();
            }
        }

        // Create log files for the participant
        mTrialsFilePath = mPcLogDirectory.resolve(pcExpLogId + "_" + "TRIALS.txt");
        mInstantsLogPath = mPcLogDirectory.resolve(pcExpLogId + "_" + "INSTANTS.txt");
        mTimesFilePath = mPcLogDirectory.resolve(pcExpLogId + "_" + "TIMES.txt");
        mEventsFilePath = mPcLogDirectory.resolve(pcExpLogId + "_" + "EVENTS.txt");

        // Write columns in log files
        try {
            mTrialsFilePW = new PrintWriter(mTrialsFilePath.toFile());
            mTrialsFilePW.println(GeneralInfo.getLogHeader() + SP + TrialInfo.getLogHeader());
            mTrialsFilePW.flush();

            mInstantFilePW = new PrintWriter(mInstantsLogPath.toFile());
            mInstantFilePW.println(GeneralInfo.getLogHeader() + SP + InstantInfo.getLogHeader());
            mInstantFilePW.flush();

            mTimesFilePW = new PrintWriter(mTimesFilePath.toFile());
            mTimesFilePW.println(GeneralInfo.getLogHeader() + SP + TimeInfo.getLogHeader());
            mTimesFilePW.flush();

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
            if (mTrialsFilePW == null) { // Open only if not opened before
                mTrialsFilePW = new PrintWriter(mTrialsFilePath.toFile());
            }

            mTrialsFilePW.println(genInfo + SP + trialInfo);
            mTrialsFilePW.flush();

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
            if (mInstantFilePW == null) { // Open only if not opened before
                mInstantFilePW = new PrintWriter(mInstantsLogPath.toFile());
            }

            mInstantFilePW.println(genInfo + SP + instInfo);
            mInstantFilePW.flush();

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
            if (mTimesFilePW == null) { // Open only if not opened before
                mTimesFilePW = new PrintWriter(mTimesFilePath.toFile());
            }

            mTimesFilePW.println(genInfo + SP + timeInfo);
            mTimesFilePW.flush();

        } catch (NullPointerException | IOException e) {
            Main.showDialog("Problem in logging instant!");
        }
    }

    /**
     * Close all log files
     */
    public void closeLogs() {
        if (mTrialsFilePW != null) mTrialsFilePW.close();
        if (mInstantFilePW != null) mInstantFilePW.close();
        if (mTimesFilePW != null) mTimesFilePW.close();
        if (mEventsFilePW != null) mEventsFilePW.close();
    }

    /**
     * Start the start of homing time
     */
    public void settHomingStTime() {
        mHomingStTime = Utils.nowInMillis();
    }

    /**
     * Get the start of himing time
     * @return Homing start time
     */
    public long getHomingStTime() {
        final long result = mHomingStTime;
        mHomingStTime = 0; // reset the time

        return result;
    }


    // -------------------------------------------------------------------------------------------
    /***
     * General info regarding every trial
     */
    public static class GeneralInfo {
        public TECHNIQUE tech;
        public int blockNum;
        public int trialNum;
        public Trial trial;

        public static String getLogHeader() {
            return "technique" + SP +
                    "block_num" + SP +
                    "trial_num" + SP +
                    Trial.getLogHeader();
        }

        @Override
        public String toString() {
            return tech + SP +
                    blockNum + SP +
                    trialNum + SP +
                    trial.toLogString();
        }
    }

    /**
     * All times in ms
     */
    public static class TrialInfo {
        public int searchTime; // From the first scroll until the target appeared the last time
        public int fineTuneTime; // From the last appearance of target to the last scroll
        public int scrollTime; // searchTime + fineTuneTime
        public int result; // 1 (Hit) or 0(Miss)

        public static String getLogHeader() {
            return "search_time" + SP +
                    "fine_tune_time" + SP +
                    "scroll_time" + SP +
                    "result";
        }

        @Override
        public String toString() {
            return searchTime + SP +
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

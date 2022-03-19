package control;

import data.Memo;
import experiment.Trial;
import gui.Main;
import gui.ExperimentFrame;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import tools.Logs;
import tools.Utils;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static data.Consts.STRINGS.*;
import static experiment.Experiment.*;

public class Logger {
    private final static String NAME = "Logger/";
    // -------------------------------------------------------------------------------------------
    private static Logger self;

    private static Path mLogDirectory; // Main folder for logs
    private static Path mPcLogDirectory; // Folder log path of the participant

    private String mPcLogId = "";
    private String mPcExpLogId = "";


    // Different log files
    private Path mTrialsFilePath;
    private PrintWriter mTrialsFilePW;

    private Path mInstantsFilePath;
    private PrintWriter mInstantFilePW;

    private Path mTimesFilePath;
    private PrintWriter mTimesFilePW;

    private Path mScrollFilePath;
    private PrintWriter mScrollFilePW;

    private Path mMoveFilePath;
    private PrintWriter mMoveFilePW;

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
        final Path parentPatn = Paths.get("").toAbsolutePath().getParent().getParent(); // Up two levels
        mLogDirectory = parentPatn.resolve("Expenvi-Scroll-Logs");

        if (!Files.isDirectory(mLogDirectory)) {
            try {
                Files.createDirectory(mLogDirectory);
            } catch (IOException ioe) {
                ExperimentFrame.get().showMessage("Problem in creating log directory!");
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

        mPcLogId = P + pId;
        mPcExpLogId = mPcLogId + "_" + Utils.nowDate(); // Experiment Id

        // Pc logging directory and files' paths
        mPcLogDirectory = mLogDirectory.resolve(mPcLogId);

        mTrialsFilePath =   mPcLogDirectory.resolve(mPcExpLogId + "_" + "TRIALS.txt");
        mInstantsFilePath = mPcLogDirectory.resolve(mPcExpLogId + "_" + "INSTANTS.txt");
        mTimesFilePath =    mPcLogDirectory.resolve(mPcExpLogId + "_" + "TIMES.txt");
        mScrollFilePath =   mPcLogDirectory.resolve(mPcExpLogId + "_" + "SCROLL.txt");
        mMoveFilePath =     mPcLogDirectory.resolve(mPcExpLogId + "_" + "MOVE.txt");

        if (!Files.isDirectory(mPcLogDirectory)) { // New logging
            createLogFiles();
        } else { // Continue logging...
            Logs.d(TAG, "Continue");
            openLogFiles();
        }

    }

    /**
     * Send the initial log info to the Moose
     */
    public void initLogOnMoose() {
        // Send the logging info to the Moose as well
        Server.get().send(new Memo(LOG, EXPID, mPcLogId, mPcExpLogId));
    }

    /**
     * Create directory and log files
     */
    private void createLogFiles() {
        try {
            Files.createDirectory(mPcLogDirectory);

            mTrialsFilePW = new PrintWriter(mTrialsFilePath.toFile());
            mTrialsFilePW.println(GeneralInfo.getLogHeader() + SP + TrialInfo.getLogHeader());
            mTrialsFilePW.flush();

            mInstantFilePW = new PrintWriter(mInstantsFilePath.toFile());
            mInstantFilePW.println(GeneralInfo.getLogHeader() + SP + InstantInfo.getLogHeader());
            mInstantFilePW.flush();

            mTimesFilePW = new PrintWriter(mTimesFilePath.toFile());
            mTimesFilePW.println(GeneralInfo.getLogHeader() + SP + TimeInfo.getLogHeader());
            mTimesFilePW.flush();

            mScrollFilePW = new PrintWriter(mScrollFilePath.toFile());
            mScrollFilePW.println(GeneralInfo.getLogHeader() + SP + ScrollInfo.getLogHeader());
            mScrollFilePW.flush();

            mMoveFilePW = new PrintWriter(mMoveFilePath.toFile());
            mMoveFilePW.println(GeneralInfo.getLogHeader() + SP + MoveInfo.getLogHeader());
            mMoveFilePW.flush();

        } catch (IOException ioe) {
            ExperimentFrame.get().showMessage("Problem in creating log dir/files");
            ioe.printStackTrace();
        }
    }

    /**
     * Open log file to write
     */
    private void openLogFiles() {
        try {
            mTrialsFilePW = new PrintWriter(new FileWriter(mTrialsFilePath.toString(), true));
            mInstantFilePW = new PrintWriter(new FileWriter(mInstantsFilePath.toString(), true));
            mTimesFilePW = new PrintWriter(new FileWriter(mTimesFilePath.toString(), true));
            mScrollFilePW = new PrintWriter(new FileWriter(mScrollFilePath.toString(), true));
            mMoveFilePW = new PrintWriter(new FileWriter(mMoveFilePath.toString(), true));

        } catch (IOException e) {
            ExperimentFrame.get().showMessage("Problem in opening log files");
            e.printStackTrace();
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
            // Open only if not opened before
            if (mTrialsFilePW == null) openLogFiles();

            mTrialsFilePW.println(genInfo + SP + trialInfo);
            mTrialsFilePW.flush();

        } catch (NullPointerException e) {
            e.printStackTrace();
            Main.showDialog("Problem in logging trial!");
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
            // Open only if not opened before
            if (mInstantFilePW == null) openLogFiles();

            mInstantFilePW.println(genInfo + SP + instInfo);
            mInstantFilePW.flush();

        } catch (NullPointerException e) {
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
            // Open only if not opened before
            if (mTimesFilePW == null) openLogFiles();

            mTimesFilePW.println(genInfo + SP + timeInfo);
            mTimesFilePW.flush();

        } catch (NullPointerException e) {
            Main.showDialog("Problem in logging time!");
        }
    }

    /**
     * Log ScrollInfo
     * @param genInfo GeneralInfo
     * @param scrollInfo ScrollInfo
     */
    public void logScrollInfo(GeneralInfo genInfo, ScrollInfo scrollInfo) {
        final String TAG = NAME + "logScrollInfo";

        try {
            // Open only if not opened before
            if (mScrollFilePW == null) openLogFiles();

            mScrollFilePW.println(genInfo + SP + scrollInfo);
            mScrollFilePW.flush();

        } catch (NullPointerException e) {
            Main.showDialog("Problem in logging scroll!");
        }
    }

    /**
     * Log MoveInfo
     * @param genInfo GeneralInfo
     * @param moveInfo MoveInfo
     */
    public void logMoveInfo(GeneralInfo genInfo, MoveInfo moveInfo) {
        final String TAG = NAME + "logScrollInfo";

        try {
            // Open only if not opened before
            if (mMoveFilePW == null) openLogFiles();

            mMoveFilePW.println(genInfo + SP + moveInfo);
            mMoveFilePW.flush();

        } catch (NullPointerException e) {
            Main.showDialog("Problem in logging scroll!");
        }
    }

    /**
     * Close all log files
     */
    public void closeLogs() {
        if (mTrialsFilePW != null) mTrialsFilePW.close();
        if (mInstantFilePW != null) mInstantFilePW.close();
        if (mTimesFilePW != null) mTimesFilePW.close();
        if (mScrollFilePW != null) mScrollFilePW.close();
        if (mMoveFilePW != null) mMoveFilePW.close();
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
    //General info regarding every trial
    @AllArgsConstructor
    @NoArgsConstructor
    public static class GeneralInfo {
        public int session;
        public int part;
        public TECHNIQUE tech;
        public int blockNum;
        public int trialNum;
        public Trial trial;

        public static String getLogHeader() {
            return "session" + SP +
                    "part" + SP +
                    "technique" + SP +
                    "block_num" + SP +
                    "trial_num" + SP +
                    Trial.getLogHeader();
        }

        @Override
        public String toString() {
            return session + SP +
                    part + SP +
                    tech + SP +
                    blockNum + SP +
                    trialNum + SP +
                    trial.toLogString();
        }
    }

    // All the times are in ms
    @AllArgsConstructor
    public static class TrialInfo {
        public int searchTime;      // From the first scroll until the last appearance of the target
        public int fineTuneTime;    // From the last appearance of target to the last scroll
        public int scrollTime;      // SearchTime + fineTuneTime (first scroll -> last scroll)
        public int finishTime;      // Last scroll -> SPACE
        public int trialTime;       // First scroll -> SPACE
        public int nTargetAppear;   // Number of target appearances
        public int vtResult;        // Vertical: 1 (Hit) or 0 (Miss)
        public int hzResult;        // Horizontal: 1 (Hit) or 0 (Miss)
        public int result;          //1 (Hit) or 0 (Miss)

        public static String getLogHeader() {
            return "search_time" + SP +
                    "fine_tune_time" + SP +
                    "scroll_time" + SP +
                    "finish_time" + SP +
                    "trial_time" + SP +
                    "n_target_appear" + SP +
                    "vt_result" + SP +
                    "hz_result" + SP +
                    "result";
        }

        @Override
        public String toString() {
            return searchTime + SP +
                    fineTuneTime + SP +
                    scrollTime + SP +
                    finishTime + SP +
                    trialTime + SP +
                    nTargetAppear + SP +
                    vtResult + SP +
                    hzResult + SP +
                    result;
        }
    }

    // All times in system timestamp (ms)
    public static class InstantInfo {
        public long trialShow;
        public long firstEntry;
        public long lastEntry;
        public long firstScroll;
        public long lastScroll;
        public long targetFirstAppear;
        public long targetLastAppear;
        public long trialEnd;

        public static String getLogHeader() {
            return "trial_show" + SP +
                    "first_entry" + SP +
                    "last_entry" + SP +
                    "first_scroll" + SP +
                    "last_scroll" + SP +
                    "target_first_appear" + SP +
                    "target_last_appear" + SP +
                    "trial_end";
        }

        @Override
        public String toString() {
            return trialShow + SP +
                    firstEntry+ SP +
                    lastEntry + SP +
                    firstScroll + SP +
                    lastScroll + SP +
                    targetFirstAppear + SP +
                    targetLastAppear + SP +
                    trialEnd;
        }
    }

    // Times info
    public static class TimeInfo {
        public long trialDispTime; // ms
        public long blockDispTime; // ms
        public long partDispTime; // sec

        public static String getLogHeader() {
            return "trial_disp_time" + SP +
                    "block_disp_ime" + SP +
                    "part_disp_time";
        }

        @Override
        public String toString() {
            return trialDispTime + SP +
                    blockDispTime + SP +
                    partDispTime;
        }
    }

    // Scrolling info
    public static class ScrollInfo {
        public int abX; // Absolute cursor position
        public int abY; // Absolute cursor position
        public int vtAmt;
        public int hzAmt;
        public double wheelRot;
        public long moment; // in ms

        public static String getLogHeader() {
            return "ab_x" + SP +
                    "ab_y" + SP +
                    "vt_amount" + SP +
                    "hz_amount" + SP +
                    "wheel_rotation" + SP +
                    "moment";
        }

        @Override
        public String toString() {
            return abX + SP +
                    abY + SP +
                    vtAmt + SP +
                    hzAmt + SP +
                    wheelRot + SP +
                    moment;
        }
    }

    // Mouse movement info
    public static class MoveInfo {
        public int x; // Relative (to the window) cursor position
        public int y; // Relative (to the window) cursor position
        public int abX; // Absolute cursor position
        public int abY; // Absolute cursor position
        public long moment; // in ms

        public static String getLogHeader() {
            return "x" + SP +
                    "y" + SP +
                    "ab_x" + SP +
                    "ab_y" + SP +
                    "moment";
        }

        @Override
        public String toString() {
            return x + SP +
                    y + SP +
                    abX + SP +
                    abY + SP +
                    moment;
        }
    }




}

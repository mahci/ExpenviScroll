package data;

import tools.Logs;

import javax.sound.sampled.*;
import java.awt.*;
import java.awt.font.TextAttribute;
import java.io.File;
import java.io.IOException;
import java.text.AttributedCharacterIterator;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Consts {

    // Display
    public static class DISP {
        public final static int PPI = 109;
//        public final static int PPI = 89;
        public final static double INCH_MM = 25.4;
        public final static double LR_MARGIN_mm = 20; // (mm) Left-right margin
    }

    // Colors
    public static class COLORS {
        public final static Color PANEL_BG = Color.decode("#F5F5F5");
        public final static Color VIEW_BORDER = Color.decode("#E7E7E7");
        public final static Color SCROLLBAR_TRACK = Color.decode("#FAFAFA");
        public final static Color SCROLLBAR_THUMB = Color.decode("#C2C2C2");
        public final static Color DARK_GREEN = Color.decode("#1B5E20");
        public final static Color GREEN = Color.decode("#08B40B");
        public final static Color GREEN_A400 = Color.decode("#00E676");
        public final static Color GREEN_A200 = Color.decode("#A5D6A7");
        public final static Color DARK_BLUE = Color.decode("#4333DB");
        public final static Color BLUE_800 = Color.decode("#0277BD");
        public final static Color DARK_RED = Color.decode("#B71C1C");
        public final static Color GRAY_900 = Color.decode("#212121");
        public final static Color GRAY_700 = Color.decode("#616161");
        public final static Color LINE_NUM_BG = Color.decode("#DDDDDD");
        public final static Color CELL_HIGHLIGHT = Color.decode("#64C0FF");
        public final static Color SCROLLBAR_HIGHLIGHT = Color.decode("#C7E1F0");
        public final static Color TABLE_GRID = Color.GRAY;
        public final static Color TABLE_TEXT = Color.DARK_GRAY;
    }

    // Fonts and character attributes
    public static class FONTS {
        // Fonts
        public static Font TABLE_FONT = new Font("Sans", Font.PLAIN, 9);

        // Font types
        public static Font SF_REGULAR = Font.getFont("serif");
        public static Font SF_LIGHT = new Font(Font.DIALOG,  Font.PLAIN, 5);

        // Font sizes
        public static final float TEXT_FONT_SIZE = 20.5f;
//        public static final float TEXT_FONT_SIZE = 19.4f;

        // Line spacing
        public static final float TEXT_LINE_SPACING = 0.193f;
//        public static final float TEXT_LINE_SPACING = 0.08f;

        // Italic attribute
        public static Map<? extends AttributedCharacterIterator.Attribute, ?>
                ATTRIB_ITALIC = Collections.singletonMap(TextAttribute.POSTURE, TextAttribute.POSTURE_OBLIQUE);

        // Methods
        static {
            try {
                File sfRegFile = new File("./res/SF-Regular.ttf");
                File sfLightFile = new File("./res/SF-Light.ttf");

                SF_REGULAR = Font.createFont(Font.TRUETYPE_FONT, sfRegFile);
                SF_LIGHT = Font.createFont(Font.TRUETYPE_FONT, sfLightFile);

            } catch (FontFormatException | IOException e) {
                Logs.d("FONTS", "Can't load the font file!");
                e.printStackTrace();
            }
        }
    }
    private static Map<String, Clip> sSounds = new HashMap<>();

    static {
        try {
            final File hitFile = new File("./res/hit.wav");
            final File missFile = new File("./res/miss.wav");
            final File techEndFile = new File("./res/end.wav");

            final Clip hitClip = AudioSystem.getClip();
            hitClip.open(AudioSystem.getAudioInputStream(hitFile));

            final Clip missClip = AudioSystem.getClip();
            missClip.open(AudioSystem.getAudioInputStream(missFile));

            final Clip techClip = AudioSystem.getClip();
            techClip.open(AudioSystem.getAudioInputStream(techEndFile));

            sSounds.put(STRINGS.HIT, hitClip);
            sSounds.put(STRINGS.MISS, missClip);
            sSounds.put(STRINGS.TASK_END, techClip);

        } catch (NullPointerException | IOException | UnsupportedAudioFileException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    /**
     * Play a sound
     * @param soundKey Name of the sound
     */
    public static void play(String soundKey) {
        if (sSounds.containsKey(soundKey)) {
            sSounds.get(soundKey).setMicrosecondPosition(0); // Reset to the start of the file
            sSounds.get(soundKey).start();
        }
    }
    public static class SOUNDS {
        private static Map<String, Clip> sSounds = new HashMap<>();

        static {
            try {
                final File hitFile = new File("./res/hit.wav");
                final File missFile = new File("./res/miss.wav");
                final File techEndFile = new File("./res/end.wav");

                final Clip hitClip = AudioSystem.getClip();
                hitClip.open(AudioSystem.getAudioInputStream(hitFile));

                final Clip missClip = AudioSystem.getClip();
                missClip.open(AudioSystem.getAudioInputStream(missFile));

                final Clip techClip = AudioSystem.getClip();
                techClip.open(AudioSystem.getAudioInputStream(techEndFile));

                sSounds.put(STRINGS.HIT, hitClip);
                sSounds.put(STRINGS.MISS, missClip);
                sSounds.put(STRINGS.TASK_END, techClip);

            } catch (NullPointerException | IOException | UnsupportedAudioFileException | LineUnavailableException e) {
                e.printStackTrace();
            }
        }

        /**
         * Play a sound
         * @param soundKey Name of the sound
         */
        public static void play(String soundKey) {
            if (sSounds.containsKey(soundKey)) {
                sSounds.get(soundKey).setMicrosecondPosition(0); // Reset to the start of the file
                sSounds.get(soundKey).start();
            }
        }


    }

    public static class STRINGS {
        public final static String SP = ";";
        public static final String MEMOSP = ",";
        public final static String TECH = "TECH";
        public final static String SCROLL = "SCROLL";
        public final static String STOP = "STOP";
        public final static String CONFIG = "CONFIG";
        public final static String SENSITIVITY = "SENSITIVITY";
        public final static String GAIN = "GAIN";
        public final static String DENOM = "DENOM";
        public final static String COEF = "COEF";
        public final static String LOG = "LOG";
        public final static String EXPID = "EXPID"; // Id for an experiment
        public final static String BLOCK = "BLOCK";
        public final static String TRIAL = "TRIAL";
        public final static String TSK = "TSK";
        public final static String P = "P";
        public final static String END = "END";
        public final static String KEEP_ALIVE = "KEEP_ALIVE";
        public final static String CONNECTION = "CONNECTION";
        public final static String HIT = "HIT";
        public final static String MISS = "MISS";
        public final static String TASK_END = "TECH_END";
        public final static String GENINFO = "GENINFO";

        public final static String DEMO_TITLE =
                "Welcome to the scrolling experiment!";
        public final static String DEMO_NEXT =
                "First, let's have a demo >";

        public final static String SHORT_BREAK_TEXT =
                "<html>Take a quick break! To continue, press <B>ENTER</B>.</html>";

        public static final String DLG_BREAK_TITLE  = "Time for a break between tasks!";
        public static final String DLG_BREAK_TEXT   =
                "<html>When ready, press <B>BLUE + RED</B> keys on the keyboard to continue.</html>";

        public final static String EXP_START_MESSAGE =
                "To begin the experiment, press SPACE.";
        public final static String TECH_START_MESSAGE =
                "When ready, press SPACE to start with ";

        public final static String[] END_TECH_MESSAGES = new String[2];
        public final static String END_EXPERIMENT_MESSAGE =
                "All finished! Thank you for participating in this experiment!";

        static {
            END_TECH_MESSAGES[0] = "Thank you! The first technique is done.";
            END_TECH_MESSAGES[1] = "Thank you! The second technique is done.";
        }



    }



}

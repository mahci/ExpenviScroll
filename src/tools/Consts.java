package tools;

import java.awt.*;
import java.awt.font.TextAttribute;
import java.io.File;
import java.io.IOException;
import java.text.AttributedCharacterIterator;
import java.util.Collections;
import java.util.Map;

public class Consts {

    // Display
    public static class DISP {
        public final static int PPI = 109;
//        public final static int PPI = 89;
        public final static double INCH_MM = 25.4;
    }

    // Colors
    public static class COLORS {
        public final static Color LINE_NUM_BG = new Color(221, 221, 221);
        public final static Color CELL_HIGHLIGHT = new Color(100, 192, 245);
        public final static Color SCROLLBAR_HIGHLIGHT = new Color(199, 225, 240);
        public final static Color TABLE_GRID = Color.GRAY;
        public final static Color TABLE_TEXT = Color.DARK_GRAY;
        public final static Color SCROLLBAR_TRACK = new Color(240, 240, 240);
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
                Logs.error("FONTS", "Can't load the font file!");
                e.printStackTrace();
            }
        }
    }

    public static class STRINGS {
        public final static String SP = ";";
        public final static String TECHNIQUE = "TECHNIQUE";
        public final static String SCROLL = "SCROLL";
        public final static String DRAG = "DRAG";
        public final static String RB = "RABA";
        public final static String STOP = "STOP";
        public final static String CONFIG = "CONFIG";
        public final static String SENSITIVITY = "SENSITIVITY";
        public final static String GAIN = "GAIN";
        public final static String DENOM = "DENOM";

    }



}

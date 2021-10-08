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
        public final static double INCH_MM = 25.4;
    }

    // Colors
    public static class COLORS {
        public final static Color LINE_NUM_BG = new Color(221, 221, 221);
        public final static Color LINE_HIGHLIGHT = new Color(100, 192, 245);
        public final static Color SCROLLBAR_HIGHLIGHT = new Color(199, 225, 240);
    }

    // Fonts and character attributes
    public static class FONTS {
        // Fonts
        public static Font SF_REGULAR = Font.getFont("serif");
        public static Font SF_LIGHT = Font.getFont("serif");
        public static Font ARIAL = new Font("Arial", Font.PLAIN, 13);

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




}

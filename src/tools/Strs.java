package tools;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Strs {

    private static ArrayList<Integer> lineCharCountList = new ArrayList<>();

    /**
     * Manually wrap the text (add \n) and write to outFileName
     * @param txtFileName - name of the text file
     * @param outFileName - name of the output file
     * @param wrapWidth - max width to wrap
     * @return ArrayList<Integer></> - number of chraacters in each line
     */
    public static ArrayList<Integer> wrapFile(String txtFileName, String outFileName, int wrapWidth) throws IOException {
        String mName = "wrapFile";

        // Read and get the paragraphs
        String content = Files.readString(Path.of(txtFileName));
        String[] paragraphs = content.split("\n");

        // Wrap each paragraph and write to file
        PrintWriter outFilePW = new PrintWriter(new FileWriter(outFileName));
        int nParagraphs = paragraphs.length;
        for (int pi = 0; pi < nParagraphs - 1; pi++) {
            outFilePW.println(wrapParagraph(paragraphs[pi], wrapWidth));
        }
        outFilePW.print(wrapParagraph(paragraphs[nParagraphs - 1], wrapWidth)); // write the last paragraph
        outFilePW.close();

        return lineCharCountList;
    }

    /**
     * Wrap a paragraph
     * @param paragraph - input paragraph
     * @param wrapWidth - width of wrap
     * @return wrapped paraagraph
     */
    public static String wrapParagraph(String paragraph, int wrapWidth) {
        String mName = "wrapText";

        // Special cases
        if (paragraph == null) return "";
        if (paragraph.length() <= wrapWidth) {
            lineCharCountList.add(paragraph.length()); // add the count of chars to the list
            return paragraph;
        }

        // Normal cases
        StringBuilder outStr = new StringBuilder();
        ArrayList<String> words = new ArrayList<>(Arrays.asList(paragraph.split(" ")));

        StringBuilder line = new StringBuilder();
        while (words.size() > 0) {
            if ((line + words.get(0)).length() + 1 < wrapWidth) { // +1 for the last space
                // add if doesn't exceed the limit
                line.append(words.remove(0)).append(" ");
            } else {
                outStr.append(line).append("\n");
                // add the count of chars to the list
                lineCharCountList.add(line.length());
                // reset the line
                line = new StringBuilder();
            }
        }
        outStr.append(line); // append the last line (bc the words is now empty)
        lineCharCountList.add(line.length()); // add the count of chars to the list

        return outStr.toString();
    }

}

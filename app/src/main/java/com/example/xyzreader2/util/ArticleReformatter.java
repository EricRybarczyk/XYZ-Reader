package com.example.xyzreader2.util;

// simple container class for a method to reformat article text
public class ArticleReformatter {

    private static final String DOUBLE_LINEBREAK = "\r\n\r\n";
    private static final String SINGLE_LINEBREAK = "\r\n";
    private static final String PLACEHOLDER = "<br><br>";
    private static final String EMPTY_STRING = "";

    private ArticleReformatter() {
        // prevent instantiation
    }

    public static String reformatText(String sourceText) {
        // text examples in the demo app are full of extra line breaks
        // this method will attempt to clean it up

        // first replace the double line breaks with a placeholder
        String reworkText = sourceText.replace(DOUBLE_LINEBREAK, PLACEHOLDER);

        // replace the single line breaks that are the biggest problem
        reworkText = reworkText.replace(SINGLE_LINEBREAK, EMPTY_STRING);

        // now flip placeholders back to line breaks and text should be a lot better
        reworkText = reworkText.replace(PLACEHOLDER, DOUBLE_LINEBREAK);

        return reworkText;
    }
}

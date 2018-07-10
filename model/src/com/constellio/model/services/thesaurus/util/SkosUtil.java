package com.constellio.model.services.thesaurus.util;

import com.constellio.data.utils.AccentApostropheCleaner;

import java.util.HashSet;
import java.util.Set;

public class SkosUtil {
    public static String normaliseTextForMatching(String text) {
        String normalised = text.replaceAll("\\r?\\n", " ")
                .replaceAll(" +", " ").toUpperCase();
        return " " + normalised + " ";
    }

    public static String getSkosConceptId(String text){
        return text.substring(text.lastIndexOf("id=") + 3, text.length());
    }

    /**
     * Compares two strings after parsing.
     * @param s1
     * @param s2
     * @return true if parsed strings are equal
     */
    public static boolean equalsWithParsing(String s1, String s2){
        return s1 != null && s2 != null ? parseForSearch(s1).equals(parseForSearch(s2)) : false;
    }

    /**
     * Check if string contains another after parsing.
     * @param container
     * @param content
     * @return true if parsed strings are equal
     */
    public static boolean containsWithParsing(String container, String content){
        return container != null && content != null ? parseForSearch(container).contains(parseForSearch(content)) : false;
    }

    /**
     * Remove accents, trim whitespaces and standardize case.
     * @param input
     * @return the parsed input
     */
    public static String parseForSearch(String input) {
        return input != null ? AccentApostropheCleaner.removeAccents(input.trim().toLowerCase()) : null;
    }

    /**
     * Returns a collection with lowercase strings only.
     * @param strings
     * @return a collection with lowercase strings only
     */
    public static Set<String> getToLowerCase(Set<String> strings) {

        Set<String> lowerCaseStrings = new HashSet<>();

        for(String string : strings){
            lowerCaseStrings.add(string.toLowerCase());
        }

        return lowerCaseStrings;
    }
}

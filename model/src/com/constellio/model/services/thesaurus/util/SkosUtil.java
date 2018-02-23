package com.constellio.model.services.thesaurus.util;

public class SkosUtil {
    public static String normaliseTextForMatching(String text) {
        String normalised = text.replaceAll("\\r?\\n", " ")
                .replaceAll(" +", " ").toUpperCase();
        return " " + normalised + " ";
    }

    public static String getSkosConceptId(String text){
        return text.substring(text.lastIndexOf("id=") + 3, text.length());
    }
}

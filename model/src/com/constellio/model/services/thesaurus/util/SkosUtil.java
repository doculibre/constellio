package com.constellio.model.services.thesaurus.util;

public class SkosUtil {
    public static String normaliseTextForMatching(String text) {
        return text.replaceAll("\\r?\\n", " ").trim()
                .replaceAll(" +", " ").toUpperCase();
    }

    public static String getSkosConceptId(String text){
        return text.substring(text.lastIndexOf("id=") + 3, text.length());
    }
}

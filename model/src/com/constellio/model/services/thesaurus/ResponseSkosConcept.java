package com.constellio.model.services.thesaurus;

import com.constellio.model.entities.Language;
import org.apache.commons.codec.language.bm.Lang;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class ResponseSkosConcept {
    Map<Language, List> suggestion;
    Map<Language, List> disambiguations;

    public ResponseSkosConcept() {
        suggestion = new HashMap<>();
        disambiguations = new HashMap<>();
    }

    public Map<Language, List> getSuggestion() {
        return suggestion;
    }


    public Map<Language, List> getDisambiguations() {
        return disambiguations;
    }
}

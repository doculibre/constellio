package com.constellio.model.services.thesaurus;

import com.constellio.model.entities.Language;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.List;

public class ResponseSkosConcept {
    Map<Locale, List> suggestions;
    Map<Locale, List> disambiguations;

    public ResponseSkosConcept() {
        suggestions = new HashMap<>();
        disambiguations = new HashMap<>();
    }

    public Map<Locale, List> getSuggestions() {
        return suggestions;
    }

    public Map<Locale, List> getDisambiguations() {
        return disambiguations;
    }
}

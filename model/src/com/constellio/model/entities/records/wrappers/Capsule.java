package com.constellio.model.entities.records.wrappers;

import com.constellio.data.utils.AccentApostropheCleaner;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

import java.util.ArrayList;
import java.util.List;

public class Capsule extends RecordWrapper {

    public static final String SCHEMA_TYPE = "capsule";

    public static final String DEFAULT_SCHEMA = SCHEMA_TYPE + "_default";

    public static final String CODE = "code";

    public static final String HTML = "html";

    public static final String KEYWORDS = "keywords";

    public Capsule(Record record, MetadataSchemaTypes types, String typeRequirement) {
        super(record, types, typeRequirement);
    }

    public String getCode(){
        return get(CODE);
    }

    public Capsule setCode(String code) {
        set(CODE, code);
        return this;
    }

    public String getHTML(){
        return get(HTML);
    }

    public Capsule setHTML(String html) {
        set(HTML, html);
        return this;
    }

    public List<String> getKeywords(){
        return getList(KEYWORDS);
    }

    public Capsule setKeywords(List<String> keywords){
        keywords = trimKeyWords(keywords);
        set(KEYWORDS, keywords);
        return this;
    }

    private  List<String> trimKeyWords(List<String> keyswords) {
        List<String> trimmedKeyWords = new ArrayList<>();
        for(String keyword : keyswords){
            String lowerCase = keyword.toLowerCase();
            String accentRemoved = AccentApostropheCleaner.removeAccents(lowerCase);
            trimmedKeyWords.add(accentRemoved);
        }
        return trimmedKeyWords;
    }
}

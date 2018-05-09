package com.constellio.model.services.search;

import com.constellio.model.services.search.Elevations.QueryElevation;
import com.constellio.model.services.search.Elevations.QueryElevation.DocElevation;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.Document;
import org.jdom2.Element;

import java.util.ArrayList;
import java.util.List;

public class SynonymsReader extends SynonymsXml {

    public SynonymsReader(Document document) {
        super(document);
    }

    public List<String> load() {
        List<String> synonyms = new ArrayList<>();

        Element root = document.getRootElement();

        // Synonyms
        List<Element> docs = root.getChildren(DOC);
        if (docs != null) {
            for(Element doc:docs) {
                String synonym = doc.getText();
                if(StringUtils.isNotBlank(synonym)) {
                    synonyms.add(synonym);
                }
            }
        }

        return synonyms;
    }
}

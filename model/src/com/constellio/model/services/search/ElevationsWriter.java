package com.constellio.model.services.search;

import com.constellio.model.services.search.Elevations.QueryElevation;
import com.constellio.model.services.search.Elevations.QueryElevation.DocElevation;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ElevationsWriter extends ElevationsXml {
    public ElevationsWriter(Document document) {
        super(document);
    }

    public void update(Elevations elevations) {
        Element rootElement = initRootElement();
        rootElement.removeContent();

        List<QueryElevation> queryElevations = elevations.getQueryElevations();
        for (QueryElevation queryElevation:queryElevations) {
            Element queryElement = new Element(QUERY);
            queryElement.setAttribute(QUERY_TEXT_ATTR, StringUtils.defaultString(queryElevation.getQuery(), ""));

            List<DocElevation> docElevations = queryElevation.getDocElevations();
            for (DocElevation docElevation:docElevations) {
                Element docElement = new Element(DOC);
                docElement.setAttribute(DOC_EXCLUDE_ATTR, String.valueOf(docElevation.isExclude()));
                docElement.setAttribute(DOC_ID_ATTR, StringUtils.defaultString(docElevation.getId(), ""));

                queryElement.addContent(docElement);
            }

            rootElement.addContent(queryElement);
        }
    }

    @NotNull
    public Element initRootElement() {
        Element rootElement;
        if(!document.hasRootElement()) {
            rootElement = new Element(ROOT);
            document.setRootElement(rootElement);

        } else {
            rootElement = document.getRootElement();
        }
        return rootElement;
    }
}

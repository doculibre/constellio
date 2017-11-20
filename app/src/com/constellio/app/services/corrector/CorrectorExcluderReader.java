package com.constellio.app.services.corrector;

import org.jdom2.Document;
import org.jdom2.Element;

import java.util.ArrayList;
import java.util.List;

public class CorrectorExcluderReader {
    private Document document;

    public CorrectorExcluderReader(Document document) {
        this.document = document;
    }

    public List<CorrectorExclusion> getAllCorrection() {
        List<CorrectorExclusion> exclusion = new ArrayList<>();

        Element root = document.getRootElement();
        for (Element child : root.getChildren()) {
            exclusion.add(getException(child));
        }

        return exclusion;
    }

    private CorrectorExclusion getException(Element element) {
        CorrectorExclusion correctorExclusion = new CorrectorExclusion();

        correctorExclusion.setCollection(element.getAttributeValue(CorrectorExcluderWriter.COLLECTION));
        correctorExclusion.setExclusion(element.getAttributeValue(CorrectorExcluderWriter.EXCLUSION));

        return correctorExclusion;
    }

}

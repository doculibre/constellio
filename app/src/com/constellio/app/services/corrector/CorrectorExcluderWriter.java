package com.constellio.app.services.corrector;

import com.constellio.model.entities.security.Role;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.Document;
import org.jdom2.Element;

import java.util.Iterator;

public class CorrectorExcluderWriter {
    private static final String EXCEPTION = "exception";
    private static final String COLLECTION = "collection";
    private static final String EXCEPTION_ROOT = "exceptions";
    private Document document;

    public CorrectorExcluderWriter(Document document) {
        this.document = document;
    }

    public void createEmptyExceltion() {
        Element roles = new Element(EXCEPTION);
        document.setRootElement(roles);
    }

    public void addExclusion(final CorrectorExclusion addExceptions) {
        Element rootElement = document.getRootElement();
        Element roleElement = new Element(EXCEPTION);
        roleElement.setAttribute(EXCEPTION_ROOT, addExceptions.getExclusion());
        roleElement.setAttribute(COLLECTION, addExceptions.getCollection());

        rootElement.addContent(roleElement);
    }

    public void updateExclusion(final CorrectorExclusion exclusion, CorrectorExclusion oldExclusion) {
        Element rootElement = document.getRootElement();

        Iterator<Element> iterator = rootElement.getChildren().listIterator();
        while (iterator.hasNext()) {
            Element nextValue = iterator.next();
            if (nextValue.getAttribute(EXCEPTION_ROOT).equals(oldExclusion.collection)
                    && oldExclusion.getCollection().equals(oldExclusion.getCollection())) {
                iterator.remove();
                Element element = new Element(EXCEPTION);
                rootElement.addContent(element);
                break;
            }
        }
    }

    public void deleteExclusion(final CorrectorExclusion exclusion) {
        Element rootElement = document.getRootElement();
        Iterator<Element> iterator = rootElement.getChildren().listIterator();

        while (iterator.hasNext()) {
            Element child = iterator.next();
            if (child.getAttribute(EXCEPTION).getValue().equals(exclusion.getExclusion())
                    && child.getAttribute(COLLECTION).getValue().equals(exclusion.getCollection())) {
                iterator.remove();
                break;
            }
        }
    }
}

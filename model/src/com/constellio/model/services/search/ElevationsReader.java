package com.constellio.model.services.search;

import com.constellio.model.services.search.Elevations.QueryElevation;
import com.constellio.model.services.search.Elevations.QueryElevation.DocElevation;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.Document;
import org.jdom2.Element;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class ElevationsReader extends ElevationsXml {

    public ElevationsReader(Document document) {
        super(document);
    }

    public Elevations load() {
        Elevations elevations = new Elevations();

        Element root = document.getRootElement();

        List<Element> children = root.getChildren(QUERY);
        if (children != null) {
            Iterator<Element> iteratorQuery = children.listIterator();
            while (iteratorQuery.hasNext()) {
                Element childQuery = iteratorQuery.next();
                QueryElevation queryElevation = new QueryElevation(StringUtils.defaultIfEmpty(childQuery.getAttributeValue(QUERY_TEXT_ATTR), null));

                List<Element> queryChildren = childQuery.getChildren();
                if (queryChildren != null) {
                    for (Element child : queryChildren) {
                        boolean exclude = Boolean.parseBoolean(child.getAttributeValue(DOC_EXCLUDE_ATTR));
                        String id = StringUtils.defaultIfEmpty(child.getAttributeValue(DOC_ID_ATTR), null);

                        DocElevation docElevation = new DocElevation(id, exclude);
                        docElevation.setQuery(queryElevation.getQuery());

                        queryElevation.addDocElevation(docElevation);
                    }
                }

                elevations.addOrUpdate(queryElevation);
            }
        }

        return elevations;
    }
}

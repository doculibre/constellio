package com.constellio.model.services.search;

import com.constellio.model.services.search.Elevations.QueryElevation;
import com.constellio.model.services.search.Elevations.QueryElevation.DocElevation;
import org.jdom2.Document;
import org.jdom2.Element;
import org.junit.Test;

import static com.constellio.model.services.search.ElevationsXml.*;
import static org.assertj.core.api.Assertions.assertThat;

public class ElevationsReaderTest {
    public static final String ELEVATION_QUERY = "elevationQuery";
    public static final String ELEVATION_ID = "elevationId";
    public static final String EXCLUSION_ID = "exclusionId";

    protected ElevationsReader reader;

    @Test
    public void givenDocumentWithAllThenElevationsLoadedWithAll() {
        reader = new ElevationsReader(buildDocumentWithAll());

        Elevations elevations = reader.load();
        assertThat(elevations).isNotNull();
        assertThat(elevations.getQueryElevations()).isNotEmpty().hasSize(1);

        QueryElevation queryElevation = elevations.getQueryElevations().get(0);
        assertThat(queryElevation.getQuery()).isNotNull().isEqualTo(ELEVATION_QUERY);

        assertThat(queryElevation.getDocElevations()).isNotEmpty().hasSize(1);

        DocElevation docElevation = queryElevation.getDocElevations().get(0);
        assertThat(docElevation.getQuery()).isNotNull().isEqualTo(ELEVATION_QUERY);
        assertThat(docElevation.getId()).isNotNull().isEqualTo(ELEVATION_ID);

        assertThat(elevations.getDocExclusions()).isNotEmpty().hasSize(1);

        String exclusion = elevations.getDocExclusions().get(0);
        assertThat(exclusion).isNotNull().isEqualTo(EXCLUSION_ID);
    }

    @Test
    public void givenDocumentWithOnlyElevationThenElevationsLoadedWithOnlyElevation() {
        reader = new ElevationsReader(buildDocumentWithOnlyElevation());

        Elevations elevations = reader.load();
        assertThat(elevations).isNotNull();
        assertThat(elevations.getQueryElevations()).isNotEmpty().hasSize(1);

        QueryElevation queryElevation = elevations.getQueryElevations().get(0);
        assertThat(queryElevation.getQuery()).isNotNull().isEqualTo(ELEVATION_QUERY);

        assertThat(queryElevation.getDocElevations()).isNotEmpty().hasSize(1);

        DocElevation docElevation = queryElevation.getDocElevations().get(0);
        assertThat(docElevation.getQuery()).isNotNull().isEqualTo(ELEVATION_QUERY);
        assertThat(docElevation.getId()).isNotNull().isEqualTo(ELEVATION_ID);

        assertThat(elevations.getDocExclusions()).isNullOrEmpty();
    }

    @Test
    public void givenDocumentWithOnlyExclusionThenElevationsLoadedWithOnlyExclusion() {
        reader = new ElevationsReader(buildDocumentWithOnlyExclusion());

        Elevations elevations = reader.load();
        assertThat(elevations).isNotNull();
        assertThat(elevations.getQueryElevations()).isNullOrEmpty();

        assertThat(elevations.getDocExclusions()).isNotEmpty().hasSize(1);

        String exclusion = elevations.getDocExclusions().get(0);
        assertThat(exclusion).isNotNull().isEqualTo(EXCLUSION_ID);
    }

    private Document buildDocumentWithAll() {
        Element rootElement = new Element(ROOT);

        Element elevation = new Element(ELEVATION);

        Element queryElement = new Element(QUERY);
        queryElement.setAttribute(QUERY_TEXT_ATTR, ELEVATION_QUERY);

        Element docElement = new Element(DOC);
        docElement.setAttribute(DOC_ID_ATTR, ELEVATION_ID);

        queryElement.addContent(docElement);

        elevation.addContent(queryElement);
        rootElement.addContent(elevation);

        Element exclusion = new Element(EXCLUSION);

        docElement = new Element(DOC);
        docElement.setAttribute(DOC_ID_ATTR, EXCLUSION_ID);

        exclusion.addContent(docElement);

        rootElement.addContent(exclusion);

        Document document = new Document();
        document.setRootElement(rootElement);

        return document;
    }

    private Document buildDocumentWithOnlyElevation() {
        Element rootElement = new Element(ROOT);

        Element elevation = new Element(ELEVATION);

        Element queryElement = new Element(QUERY);
        queryElement.setAttribute(QUERY_TEXT_ATTR, ELEVATION_QUERY);

        Element docElement = new Element(DOC);
        docElement.setAttribute(DOC_ID_ATTR, ELEVATION_ID);

        queryElement.addContent(docElement);

        elevation.addContent(queryElement);
        rootElement.addContent(elevation);

        Document document = new Document();
        document.setRootElement(rootElement);

        return document;
    }

    private Document buildDocumentWithOnlyExclusion() {
        Element rootElement = new Element(ROOT);

        Element exclusion = new Element(EXCLUSION);

        Element docElement = new Element(DOC);
        docElement.setAttribute(DOC_ID_ATTR, EXCLUSION_ID);

        exclusion.addContent(docElement);

        rootElement.addContent(exclusion);

        Document document = new Document();
        document.setRootElement(rootElement);

        return document;
    }
}

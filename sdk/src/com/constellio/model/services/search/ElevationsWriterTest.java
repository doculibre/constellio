package com.constellio.model.services.search;

import com.constellio.model.services.search.QueryElevation.DocElevation;
import org.jdom2.Document;
import org.jdom2.Element;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.constellio.model.services.search.ElevationsXml.DOC;
import static com.constellio.model.services.search.ElevationsXml.DOC_ID_ATTR;
import static com.constellio.model.services.search.ElevationsXml.ELEVATION;
import static com.constellio.model.services.search.ElevationsXml.EXCLUSION;
import static com.constellio.model.services.search.ElevationsXml.QUERY;
import static com.constellio.model.services.search.ElevationsXml.QUERY_TEXT_ATTR;
import static com.constellio.model.services.search.ElevationsXml.ROOT;
import static org.assertj.core.api.Assertions.assertThat;

public class ElevationsWriterTest {
	public static final String ELEVATION_QUERY = "query";
	public static final String ELEVATION_ID = "idElevation";
	public static final String EXCLUSION_ID = "idExclusion";
	protected Document document;
	protected ElevationsWriter writer;

	@Before
	public void setUp() {
		document = new Document();
		writer = new ElevationsWriter(document);
	}

	@Test
	public void givenEmptyDocumentThenRootElementInitialized() {
		Element element = writer.initRootElement();

		assertThat(element).isNotNull();
		assertThat(element.getName()).isEqualTo(ROOT);
	}

	@Test
	public void givenNotEmptyDocumentWhenRootElementInitializedThenEmpty() {
		Element element = writer.initRootElement();

		Element elevation = new Element(ELEVATION);
		element.addContent(elevation);

		element = writer.initRootElement();

		assertThat(element).isNotNull();
		assertThat(element.getName()).isEqualTo(ROOT);
		assertThat(element.getChild(ELEVATION)).isNull();
	}

	@Test
	public void givenElevationsWithAllThenWriteAll() {
		Elevations elevations = buildElevationsWithAll();
		writer.update(elevations);

		Element root = document.getRootElement();

		// Elevation
		Element elevation = root.getChild(ELEVATION);
		assertThat(elevation).isNotNull();

		List<Element> queries = elevation.getChildren(QUERY);
		assertThat(queries).isNotNull().isNotEmpty().hasSize(1);

		Element query = queries.get(0);
		assertThat(query).isNotNull();

		assertThat(query.getAttributeValue(QUERY_TEXT_ATTR)).isNotNull().isEqualTo(ELEVATION_QUERY);

		List<Element> docs = query.getChildren(DOC);
		assertThat(docs).isNotNull().isNotEmpty().hasSize(1);

		Element doc = docs.get(0);
		assertThat(doc.getAttributeValue(DOC_ID_ATTR)).isNotNull().isEqualTo(ELEVATION_ID);

		// Exclusion
		Element exclusion = root.getChild(EXCLUSION);
		assertThat(exclusion).isNotNull();

		docs = exclusion.getChildren(DOC);
		assertThat(docs).isNotNull().isNotEmpty().hasSize(1);

		doc = docs.get(0);
		assertThat(doc.getAttributeValue(DOC_ID_ATTR)).isNotNull().isEqualTo(EXCLUSION_ID);
	}

	@Test
	public void givenElevationsWithOnlyElevationThenWriteOnlyElevation() {
		Elevations elevations = buildElevationsWithOnlyElevation();
		writer.update(elevations);

		Element root = document.getRootElement();

		// Elevation
		Element elevation = root.getChild(ELEVATION);
		assertThat(elevation).isNotNull();

		List<Element> queries = elevation.getChildren(QUERY);
		assertThat(queries).isNotNull().isNotEmpty().hasSize(1);

		Element query = queries.get(0);
		assertThat(query).isNotNull();

		assertThat(query.getAttributeValue(QUERY_TEXT_ATTR)).isNotNull().isEqualTo(ELEVATION_QUERY);

		List<Element> docs = query.getChildren(DOC);
		assertThat(docs).isNotNull().isNotEmpty().hasSize(1);

		Element doc = docs.get(0);
		assertThat(doc.getAttributeValue(DOC_ID_ATTR)).isNotNull().isEqualTo(ELEVATION_ID);

		// Exclusion
		Element exclusion = root.getChild(EXCLUSION);
		assertThat(exclusion).isNotNull();

		docs = exclusion.getChildren(DOC);
		assertThat(docs).isNullOrEmpty();
	}

	@Test
	public void givenElevationsWithOnlyExclusionThenWriteOnlyExclusion() {
		Elevations elevations = buildElevationsWithOnlyExclusion();
		writer.update(elevations);

		Element root = document.getRootElement();

		// Elevation
		Element elevation = root.getChild(ELEVATION);
		assertThat(elevation).isNotNull();

		List<Element> queries = elevation.getChildren(QUERY);
		assertThat(queries).isNullOrEmpty();

		// Exclusion
		Element exclusion = root.getChild(EXCLUSION);
		assertThat(exclusion).isNotNull();

		List<Element> docs = exclusion.getChildren(DOC);
		assertThat(docs).isNotNull().isNotEmpty().hasSize(1);

		Element doc = docs.get(0);
		assertThat(doc.getAttributeValue(DOC_ID_ATTR)).isNotNull().isEqualTo(EXCLUSION_ID);
	}

	private Elevations buildElevationsWithAll() {
		Elevations elevations = new Elevations();
		elevations.addOrUpdate(buildQueryElevation(ELEVATION_QUERY, ELEVATION_ID));

		elevations.addDocExclusion(EXCLUSION_ID);

		return elevations;
	}

	private Elevations buildElevationsWithOnlyElevation() {
		Elevations elevations = new Elevations();
		elevations.addOrUpdate(buildQueryElevation(ELEVATION_QUERY, ELEVATION_ID));

		return elevations;
	}

	private Elevations buildElevationsWithOnlyExclusion() {
		Elevations elevations = new Elevations();
		elevations.addDocExclusion(EXCLUSION_ID);

		return elevations;
	}

	private QueryElevation buildQueryElevation(String query, String id) {
		QueryElevation elevation = new QueryElevation(query);
		elevation.addDocElevation(new DocElevation(id, query));

		return elevation;
	}
}

package com.constellio.model.services.search;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.constellio.model.services.search.Elevations.QueryElevation;

public class ElevationViewTest {
	private static final String SAMPLE_XML_ELEVATION = "<elevate> "
			+ "<query text=\"foo bar\"> <doc id=\"1\" exclude=\"true\"/> <doc id=\"2\" exclude=\"false\"/> </query> "
			+ "<query text=\"ipod\"> <doc id=\"1\" exclude=\"false\"/> </query> "
			+ "</elevate>";

	@Test
	public void givenAnElevationsConfigurationWhenMarshelingElevationToXmlThenXmlContainsValidElements() {
		//given
		Elevations ELEVATION = getSampleElevations();

		//when
		ElevationsView elevationView = new ElevationsView();
		elevationView.setData(ELEVATION);
		String xmlContent = new String(elevationView.toBytes());

		//then
		String simpleXml = xmlContent.replaceAll("\\s+", " ");
		assertThat(simpleXml).contains(SAMPLE_XML_ELEVATION);
	}

	private Elevations getSampleElevations() {
		Elevations ELEVATION = new Elevations();
		QueryElevation QUERY_ELEVATION = new QueryElevation("foo bar");
		QUERY_ELEVATION.getDocElevations().add(new QueryElevation.DocElevation("1", true));
		QUERY_ELEVATION.getDocElevations().add(new QueryElevation.DocElevation("2", false));
		ELEVATION.getQueryElevations().add(QUERY_ELEVATION);

		QUERY_ELEVATION = new QueryElevation("ipod");
		QUERY_ELEVATION.getDocElevations().add(new QueryElevation.DocElevation("1", false));
		ELEVATION.getQueryElevations().add(QUERY_ELEVATION);
		return ELEVATION;
	}

	@Test
	public void givenAnXMLFileWhenConstructingAnElevationsObjectFromItThenTheElevationsObjectIsInitializedCorrectly() {
		//when
		ElevationsView elevationView = new ElevationsView();
		elevationView.init(SAMPLE_XML_ELEVATION.getBytes());
		Elevations elevations = elevationView.getData();

		//then
		assertThat(elevations).isEqualTo(getSampleElevations());

	}
}

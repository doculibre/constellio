package com.constellio.model.services.search.elevations;

import com.constellio.model.services.search.QueryElevation;
import com.constellio.model.services.search.QueryElevation.DocElevation;
import org.assertj.core.api.Condition;
import org.assertj.core.data.Index;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

public class QueryElevationTest {
	public static final String QUERY = "query";
	public static final String ID_DOC_ELEVATION = "id";
	public static final String QUERY_DOC_ELEVATION = "query1";
	public static final String QUERY_DOC_ELEVATION_2 = "query2";

	private QueryElevation queryElevation;

	@Before
	public void setUp() {
		queryElevation = new QueryElevation(QUERY);
	}

	@Test
	public void givenNoDocElevationAddedThenEmptyDocElevation() {
		assertThat(queryElevation.getDocElevations()).isEmpty();
	}

	@Test
	public void givenAddDocElevationThenAdded() {
		DocElevation docElevation = new DocElevation(ID_DOC_ELEVATION, QUERY_DOC_ELEVATION);

		queryElevation.addDocElevation(docElevation);
		assertThat(queryElevation.getDocElevations()).isNotEmpty().hasSize(1).contains(docElevation);
	}

	@Test
	public void givenAddTwiceSameDocElevationIdThenAddedOnce() {
		DocElevation docElevation = new DocElevation(ID_DOC_ELEVATION, QUERY_DOC_ELEVATION);

		queryElevation.addDocElevation(docElevation);
		assertThat(queryElevation.getDocElevations()).isNotEmpty().hasSize(1).has(new Condition<DocElevation>() {
			@Override
			public boolean matches(DocElevation value) {
				return Objects.equals(QUERY_DOC_ELEVATION, value.getQuery());
			}
		}, Index.atIndex(0));

		docElevation = new DocElevation(ID_DOC_ELEVATION, QUERY_DOC_ELEVATION_2);

		queryElevation.addDocElevation(docElevation);
		assertThat(queryElevation.getDocElevations()).isNotEmpty().hasSize(1).has(new Condition<DocElevation>() {
			@Override
			public boolean matches(DocElevation value) {
				return Objects.equals(QUERY_DOC_ELEVATION_2, value.getQuery());
			}
		}, Index.atIndex(0));
	}

	@Test
	public void givenUpdateDocElevationThenUpdated() {
		DocElevation docElevation = new DocElevation(ID_DOC_ELEVATION, QUERY_DOC_ELEVATION);

		queryElevation.addDocElevation(docElevation);
		assertThat(queryElevation.getDocElevations()).isNotEmpty().hasSize(1).has(new Condition<DocElevation>() {
			@Override
			public boolean matches(DocElevation value) {
				return Objects.equals(QUERY_DOC_ELEVATION, value.getQuery());
			}
		}, Index.atIndex(0));

		docElevation = new DocElevation(ID_DOC_ELEVATION, QUERY_DOC_ELEVATION_2);

		queryElevation.addUpdate(Arrays.asList(docElevation));
		assertThat(queryElevation.getDocElevations()).isNotEmpty().hasSize(1).has(new Condition<DocElevation>() {
			@Override
			public boolean matches(DocElevation value) {
				return Objects.equals(QUERY_DOC_ELEVATION_2, value.getQuery());
			}
		}, Index.atIndex(0));
	}
}

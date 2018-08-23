package com.constellio.model.services.search.elevations.queryElevation;

import com.constellio.model.services.search.QueryElevation.DocElevation;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DocElevationTest extends ConstellioTest {
	private DocElevation docElevation;

	@Test
	public void givenInputValuesThenOutputCorresponds() {
		String id = "id";
		String query = "query";

		docElevation = new DocElevation(id, query);

		assertThat(docElevation.getId()).isEqualTo(id);
		assertThat(docElevation.getQuery()).isEqualTo(query);
	}
}

package com.constellio.model.services.search;

import com.constellio.model.services.search.QueryElevation.DocElevation;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

public class ElevationsTest extends ConstellioTest {
	protected Elevations elevations;

	@Before
	public void setUp() {
		elevations = new Elevations();
	}

	@Test
	public void givenQueryElevationAddedThenFound() {
		String id = "id";
		String query = "query";

		QueryElevation queryElevation = buildQueryElevation(query, id);
		elevations.addOrUpdate(queryElevation);

		QueryElevation added = elevations.getQueryElevation(query);
		assertThat(added).isNotNull().isEqualTo(queryElevation);
	}

	@Test
	public void givenRemoveNotExistentQueryThenFalse() {
		String id = "id";
		String query = "query";

		QueryElevation queryElevation = buildQueryElevation(query, id);
		elevations.addOrUpdate(queryElevation);

		QueryElevation added = elevations.getQueryElevation(query);
		assertThat(added).isNotNull().isEqualTo(queryElevation);

		boolean unknownQuery = elevations.removeQueryElevation("unknownQuery");
		assertThat(unknownQuery).isFalse();
	}

	@Test
	public void givenGetNotExistentQueryThenNull() {
		String id = "id";
		String query = "query";

		QueryElevation queryElevation = buildQueryElevation(query, id);
		elevations.addOrUpdate(queryElevation);

		QueryElevation added = elevations.getQueryElevation(query);
		assertThat(added).isNotNull().isEqualTo(queryElevation);

		added = elevations.getQueryElevation("unknownQuery");
		assertThat(added).isNull();
	}

	@Test
	public void givenQueryElevationWhenAddedTwiceThenUpdated() {
		String query = "query";

		String id = "id";
		QueryElevation queryElevation = buildQueryElevation(query, id);
		elevations.addOrUpdate(queryElevation);

		QueryElevation added = elevations.getQueryElevation(query);
		assertThat(added).isNotNull().isEqualTo(queryElevation);

		queryElevation = buildQueryElevation(query, id);
		elevations.addOrUpdate(queryElevation);

		added = elevations.getQueryElevation(query);
		assertThat(added).isNotNull();
		assertThat(added.getDocElevations()).hasSize(1);
	}

	@Test
	public void givenQueryElevationWhenAddNewIdThenUpdated() {
		String query = "query";

		String id = "id";
		QueryElevation queryElevation = buildQueryElevation(query, id);
		elevations.addOrUpdate(queryElevation);

		QueryElevation added = elevations.getQueryElevation(query);
		assertThat(added).isNotNull().isEqualTo(queryElevation);

		String newId = "newId";
		queryElevation = buildQueryElevation(query, newId);
		elevations.addOrUpdate(queryElevation);

		added = elevations.getQueryElevation(query);
		assertThat(added).isNotNull();
		assertThat(added.getDocElevations()).hasSize(2);
	}

	@Test
	public void givenQueryElevationRemovedThenNotFound() {
		String id = "id";
		String query = "query";

		QueryElevation queryElevation = buildQueryElevation(query, id);
		elevations.addOrUpdate(queryElevation);

		QueryElevation added = elevations.getQueryElevation(query);
		assertThat(added).isNotNull().isEqualTo(queryElevation);

		boolean removeQueryElevation = elevations.removeQueryElevation(query);
		assertThat(removeQueryElevation).isTrue();

		added = elevations.getQueryElevation(query);
		assertThat(added).isNull();
	}

	@Test
	public void givenMultipleQueryElevationAddedThenFound() {
		Random random = new Random();

		List<QueryElevation> queryElevations = buildMultipleQueryElevations(random.nextInt(10) + 1);
		for (QueryElevation qe : queryElevations) {
			elevations.addOrUpdate(qe);
		}

		List<QueryElevation> added = elevations.getQueryElevations();
		assertThat(added).isNotNull().isNotEmpty().containsAll(queryElevations);
	}

	@Test
	public void givenAddedQueryElevationRemovedThenNotFound() {
		Random random = new Random();

		List<QueryElevation> queryElevations = buildMultipleQueryElevations(10);
		for (QueryElevation qe : queryElevations) {
			elevations.addOrUpdate(qe);
		}

		List<QueryElevation> added = elevations.getQueryElevations();
		assertThat(added).isNotNull().isNotEmpty().containsAll(queryElevations);

		QueryElevation queryElevation = queryElevations.remove(random.nextInt(queryElevations.size()));
		assertThat(queryElevation).isNotNull();

		elevations.removeDocElevation(queryElevation.getQuery(), queryElevation.getDocElevations().get(0).getId());

		added = elevations.getQueryElevations();
		assertThat(added).isNotNull().isNotEmpty().containsAll(queryElevations).doesNotContain(queryElevation);
	}

	@Test
	public void givenQueryElevationWithMoreDocElevationThenNotRemoved() {
		Random random = new Random();

		List<QueryElevation> queryElevations = buildMultipleQueryElevations(random.nextInt(10) + 1);
		for (QueryElevation qe : queryElevations) {
			elevations.addOrUpdate(qe);
		}

		List<QueryElevation> added = elevations.getQueryElevations();
		assertThat(added).isNotNull().isNotEmpty().containsAll(queryElevations);

		QueryElevation queryElevation = queryElevations.get(random.nextInt(queryElevations.size()));
		assertThat(queryElevation).isNotNull();

		queryElevation.addDocElevation(new DocElevation("newId", queryElevation.getQuery()));

		elevations.removeDocElevation(queryElevation.getQuery(), queryElevation.getDocElevations().get(0).getId());

		added = elevations.getQueryElevations();
		assertThat(added).isNotNull().isNotEmpty().containsAll(queryElevations);
	}

	@Test
	public void givenMultipleQueryElevationWhenRemoveAllThenRemoved() {
		Random random = new Random();

		List<QueryElevation> queryElevations = buildMultipleQueryElevations(random.nextInt(10) + 1);
		for (QueryElevation qe : queryElevations) {
			elevations.addOrUpdate(qe);
		}

		List<QueryElevation> added = elevations.getQueryElevations();
		assertThat(added).isNotNull().isNotEmpty().containsAll(queryElevations);

		elevations.removeAllElevation();

		added = elevations.getQueryElevations();
		assertThat(added).isNullOrEmpty();
	}

	@Test
	public void givenDocExclusionWhenAddedThenTrue() {
		boolean exclusion = elevations.addDocExclusion("id");
		assertThat(exclusion).isTrue();
	}

	@Test
	public void givenDocExclusionWhenAddedThenFound() {
		String id = "id";
		boolean exclusion = elevations.addDocExclusion(id);
		assertThat(exclusion).isTrue();

		List<String> exclusions = elevations.getDocExclusions();
		assertThat(exclusions).isNotEmpty().hasSize(1).contains(id);
	}

	@Test
	public void givenAddedDocExclusionWhenRemovedThenNotFound() {
		String id = "id";
		boolean exclusion = elevations.addDocExclusion(id);
		assertThat(exclusion).isTrue();

		List<String> exclusions = elevations.getDocExclusions();
		assertThat(exclusions).isNotEmpty().contains(id);

		id = "newId";
		exclusion = elevations.addDocExclusion(id);
		assertThat(exclusion).isTrue();

		exclusions = elevations.getDocExclusions();
		assertThat(exclusions).isNotEmpty().contains(id);

		elevations.removeDocExclusion(id);
		assertThat(exclusions).isNotEmpty().doesNotContain(id);
	}

	@Test
	public void givenWhenRemoveAllDocExclusionThenEmpty() {
		String id = "id";
		boolean exclusion = elevations.addDocExclusion(id);
		assertThat(exclusion).isTrue();

		List<String> exclusions = elevations.getDocExclusions();
		assertThat(exclusions).isNotEmpty().contains(id);

		id = "newId";
		exclusion = elevations.addDocExclusion(id);
		assertThat(exclusion).isTrue();

		elevations.removeAllDocExclusion();

		exclusions = elevations.getDocExclusions();
		assertThat(exclusions).isNullOrEmpty();
	}

	@Test
	public void givenDocExclusionWhenAddedTwiceThenFalse() {
		String id = "id";
		boolean exclusion = elevations.addDocExclusion(id);
		assertThat(exclusion).isTrue();

		exclusion = elevations.addDocExclusion(id);
		assertThat(exclusion).isFalse();

		List<String> exclusions = elevations.getDocExclusions();
		assertThat(exclusions).isNotEmpty().hasSize(1).contains(id);
	}

	public List<QueryElevation> buildMultipleQueryElevations(int number) {
		String id = "id";
		String query = "query";
		List<QueryElevation> res = new ArrayList<>(number);

		for (int i = 0; i < number; i++) {
			res.add(buildQueryElevation(query + i, id + i));
		}

		return res;
	}

	public QueryElevation buildQueryElevation(String query, String id) {
		QueryElevation elevation = new QueryElevation(query);
		elevation.addDocElevation(new DocElevation(id, query));

		return elevation;
	}
}

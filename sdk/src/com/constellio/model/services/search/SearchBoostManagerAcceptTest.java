package com.constellio.model.services.search;

import com.constellio.app.services.collections.CollectionsManager;
import com.constellio.model.services.search.entities.SearchBoost;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

/**
 * Created by Patrick on 2015-11-05.
 */
public class SearchBoostManagerAcceptTest extends ConstellioTest {

	public static final String METADATA = "metadata";
	public static final String QUERY = "query";
	CollectionsManager collectionsManager;
	private SearchBoost searchBoost1, searchBoost2, searchBoost3, searchBoost4;

	private SearchBoostManager manager;
	private UserServices userServices;
	private String anotherCollection = "anotherCollection";

	Users users = new Users();

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(
				withZeCollection().withAllTest(users),
				withCollection(anotherCollection).withAllTestUsers()
		);
		collectionsManager = getAppLayerFactory().getCollectionsManager();

		userServices = getModelLayerFactory().newUserServices();
		users = new Users().setUp(userServices, zeCollection);
		manager = getModelLayerFactory().getSearchBoostManager();
		manager.initialize();
	}

	@Test
	public void givenFieldsInBothCollectionsThenAllIndependents()
			throws Exception {

		givenFieldsInBothCollections();

		assertThat(manager.getAllSearchBoostsByMetadataType(zeCollection)).hasSize(2);
		assertThat(manager.getAllSearchBoostsByMetadataType(zeCollection)).extracting("key", "label", "value").containsOnly(
				tuple(searchBoost1.getKey(), searchBoost1.getLabel(), searchBoost1.getValue()),
				tuple(searchBoost2.getKey(), searchBoost2.getLabel(), searchBoost2.getValue()));
		assertThat(manager.getAllSearchBoostsByQueryType(zeCollection)).hasSize(2);
		assertThat(manager.getAllSearchBoostsByQueryType(zeCollection)).extracting("key", "label", "value").containsOnly(
				tuple(searchBoost3.getKey(), searchBoost3.getLabel(), searchBoost3.getValue()),
				tuple(searchBoost4.getKey(), searchBoost4.getLabel(), searchBoost4.getValue()));
	}

	@Test
	public void givenFieldsWhenDeleteFieldThenRemoveFromCollection()
			throws Exception {

		givenFieldsInBothCollections();

		manager.delete(zeCollection, searchBoost1);

		assertThat(manager.getAllSearchBoostsByMetadataType(zeCollection)).hasSize(1);
		assertThat(manager.getAllSearchBoostsByMetadataType(zeCollection)).extracting("key", "label", "value").containsOnly(
				tuple(searchBoost2.getKey(), searchBoost2.getLabel(), searchBoost2.getValue()));
		assertThat(manager.getAllSearchBoostsByQueryType(anotherCollection)).hasSize(2);
		assertThat(manager.getAllSearchBoostsByQueryType(anotherCollection)).extracting("key", "label", "value").containsOnly(
				tuple(searchBoost3.getKey(), searchBoost3.getLabel(), searchBoost3.getValue()),
				tuple(searchBoost4.getKey(), searchBoost4.getLabel(), searchBoost4.getValue()));
	}

	@Test
	public void givenFieldsWhenAddAlreadyExistentFieldThenUpdateInCollection()
			throws Exception {

		givenFieldsInBothCollections();

		SearchBoost searchBoost = new SearchBoost(METADATA, "title_s", "label1", 1d);

		manager.add(zeCollection, searchBoost);

		assertThat(manager.getAllSearchBoostsByMetadataType(zeCollection)).hasSize(2);
		assertThat(manager.getAllSearchBoostsByMetadataType(zeCollection)).extracting("key", "label", "value").containsOnly(
				tuple(searchBoost1.getKey(), "label1", 1d),
				tuple(searchBoost2.getKey(), searchBoost2.getLabel(), searchBoost2.getValue()));
		assertThat(manager.getAllSearchBoostsByQueryType(anotherCollection)).hasSize(2);
		assertThat(manager.getAllSearchBoostsByQueryType(anotherCollection)).extracting("key", "label", "value").containsOnly(
				tuple(searchBoost3.getKey(), searchBoost3.getLabel(), searchBoost3.getValue()),
				tuple(searchBoost4.getKey(), searchBoost4.getLabel(), searchBoost4.getValue()));
	}

	@Test
	public void givenBothTypesFieldsWhenGetByTypeThenOk()
			throws Exception {

		givenFieldsInBothCollections();

		assertThat(manager.getAllSearchBoostsByMetadataType(zeCollection)).hasSize(2);
		assertThat(manager.getAllSearchBoostsByMetadataType(zeCollection)).extracting("key", "label", "value").containsOnly(
				tuple(searchBoost1.getKey(), searchBoost1.getLabel(), searchBoost1.getValue()),
				tuple(searchBoost2.getKey(), searchBoost2.getLabel(), searchBoost2.getValue()));
		assertThat(manager.getAllSearchBoostsByQueryType(zeCollection)).hasSize(2);
		assertThat(manager.getAllSearchBoostsByQueryType(zeCollection)).extracting("key", "label", "value").containsOnly(
				tuple(searchBoost3.getKey(), searchBoost3.getLabel(), searchBoost3.getValue()),
				tuple(searchBoost4.getKey(), searchBoost4.getLabel(), searchBoost4.getValue()));
	}

	private void givenFieldsInBothCollections() {

		searchBoost1 = new SearchBoost(METADATA, "title_s", "labelTitle", 1d);
		searchBoost2 = new SearchBoost(METADATA, "description_s", "labelDescription", 2d);
		searchBoost3 = new SearchBoost(QUERY, "title_s", "labelTitle", 1d);
		searchBoost4 = new SearchBoost(QUERY, "code_s", "labelCode", 2d);

		manager.add(zeCollection, searchBoost1);
		manager.add(zeCollection, searchBoost2);
		manager.add(zeCollection, searchBoost3);
		manager.add(zeCollection, searchBoost4);
		manager.add(anotherCollection, searchBoost3);
		manager.add(anotherCollection, searchBoost4);
	}
}
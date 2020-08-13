package com.constellio.app.services.collections;

import com.constellio.app.services.collections.CollectionsManagerRuntimeException.CollectionsManagerRuntimeException_CollectionNotFound;
import com.constellio.app.services.collections.CollectionsManagerRuntimeException.CollectionsManagerRuntimeException_InvalidCode;
import com.constellio.data.dao.managers.config.values.PropertiesConfiguration;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.MetadataSchemasManagerRuntimeException;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.security.AuthorizationsServices;
import com.constellio.model.services.security.roles.RolesManager;
import com.constellio.model.services.security.roles.RolesManagerRuntimeException;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.model.services.taxonomies.TaxonomiesSearchServices;
import com.constellio.model.services.users.SystemWideUserInfos;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestRecord;
import com.constellio.sdk.tests.setups.TwoTaxonomiesContainingFolderAndDocumentsSetup;
import com.constellio.sdk.tests.setups.TwoTaxonomiesContainingFolderAndDocumentsSetup.FolderSchema;
import com.constellio.sdk.tests.setups.TwoTaxonomiesContainingFolderAndDocumentsSetup.TaxonomyRecords;
import com.constellio.sdk.tests.setups.Users;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.constellio.model.entities.records.wrappers.Collection.SYSTEM_COLLECTION;
import static com.constellio.model.entities.security.global.AuthorizationAddRequest.authorizationInCollection;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasIn;
import static org.assertj.core.api.Assertions.assertThat;

public class CollectionsAcceptanceTest extends ConstellioTest {

	Users users = new Users();
	CollectionsManager collectionsManager;
	UserServices userServices;
	RolesManager rolesManager;
	SearchServices searchServices;
	MetadataSchemasManager metadataSchemasManager;
	AuthorizationsServices authorizationsServices;
	RecordServices recordServices;
	TaxonomiesManager taxonomiesManager;
	TwoTaxonomiesContainingFolderAndDocumentsSetup constellioSchemas = new TwoTaxonomiesContainingFolderAndDocumentsSetup(
			"constellio");
	FolderSchema constellioFolderSchema = constellioSchemas.new FolderSchema();
	TwoTaxonomiesContainingFolderAndDocumentsSetup doculibreSchemas = new TwoTaxonomiesContainingFolderAndDocumentsSetup(
			"doculibre");
	FolderSchema doculibreFolderSchema = doculibreSchemas.new FolderSchema();
	TaxonomyRecords constellioTaxos;
	TaxonomyRecords doculibreTaxos;
	TaxonomiesSearchServices taxonomiesSearchServices;

	@Before
	public void setUp()
			throws Exception {
		collectionsManager = getAppLayerFactory().getCollectionsManager();
		userServices = getModelLayerFactory().newUserServices();
		rolesManager = getModelLayerFactory().getRolesManager();
		searchServices = getModelLayerFactory().newSearchServices();
		taxonomiesManager = getModelLayerFactory().getTaxonomiesManager();
		metadataSchemasManager = getModelLayerFactory().getMetadataSchemasManager();
		authorizationsServices = getModelLayerFactory().newAuthorizationsServices();
		recordServices = getModelLayerFactory().newRecordServices();
		taxonomiesSearchServices = getModelLayerFactory().newTaxonomiesSearchService();
	}

	@Test(expected = CollectionsManagerRuntimeException_InvalidCode.class)
	public void whenCreateCollectionWithInvalidNameThenException()
			throws Exception {
		givenCollection("constellio.com");
	}

	@Test(expected = CollectionsManagerRuntimeException_InvalidCode.class)
	public void whenCreateCollectionWithAnotherInvalidNameThenException()
			throws Exception {
		givenCollection("constellio com");
	}

	@Test
	public void whenCreateCollectionThenCollectionRecordCreated()
			throws Exception {

		givenConstellioAndDoculibreCollectionsWithBobAndLegendsInConstellioAndLegendsAndHeroesInDoculibre();

		Collection constellioCollection = collectionsManager.getCollection("constellio");
		Collection doculibreCollection = collectionsManager.getCollection("doculibre");
		recordServices.update(constellioCollection.setName("Constellio 5").getWrappedRecord());
		recordServices.update(doculibreCollection.setName("Doculibre").getWrappedRecord());

		assertThat(collectionsManager.getCollection("constellio").getName()).isEqualTo("Constellio 5");
		assertThat(collectionsManager.getCollection("doculibre").getName()).isEqualTo("Doculibre");

	}

	@Test
	public void whenDeletingAndRecreatingACollectionWithSameCodeThenOk()
			throws Exception {

		givenCollection(zeCollection).withConstellioRMModule().withConstellioESModule();
		givenCollection("anotherCollection").withConstellioRMModule().withConstellioESModule();
		UserServices userServices = getModelLayerFactory().newUserServices();
		userServices.execute(admin, (req) -> req.addToCollection(zeCollection));
		userServices.execute(admin, (req) -> req.addToCollection("anotherCollection"));
		assertThat(getAppLayerFactory().getModulesManager().getEnabledModules(zeCollection)).extracting("class.name")
				.containsOnly("com.constellio.app.modules.tasks.TaskModule", "com.constellio.app.modules.es.ConstellioESModule",
						"com.constellio.app.modules.rm.ConstellioRMModule");

		collectionsManager.deleteCollection(zeCollection);
		givenCollection(zeCollection).withConstellioRMModule().withRobotsModule();
		assertThat(getAppLayerFactory().getModulesManager().getEnabledModules(zeCollection)).extracting("class.name")
				.containsOnly("com.constellio.app.modules.tasks.TaskModule",
						"com.constellio.app.modules.robots.ConstellioRobotsModule",
						"com.constellio.app.modules.rm.ConstellioRMModule");

		assertThat(userServices.getUserInfos(admin).getCollections()).containsOnly("anotherCollection");
		userServices.execute(admin, (req) -> req.addToCollection(zeCollection));
	}

	@Test
	public void whenCreatingCollectionThenAsSpecifiedLanguages()
			throws Exception {

		String mainDataLanguage = getModelLayerFactory().getConfiguration().getMainDataLanguage();

		givenSpecialCollection("constellio", Arrays.asList("fr", "en"));
		givenSpecialCollection("doculibre", Arrays.asList(mainDataLanguage));

		assertThat(collectionsManager.getCollection(SYSTEM_COLLECTION).getLanguages()).containsOnly(mainDataLanguage);
		assertThat(collectionsManager.getCollection("constellio").getLanguages()).isEqualTo(Arrays.asList("fr", "en"));
		assertThat(collectionsManager.getCollection("doculibre").getLanguages()).isEqualTo(Arrays.asList(mainDataLanguage));

	}

	@Test(expected = CollectionsManagerRuntimeException_CollectionNotFound.class)
	public void whenGetInexistentCollectionThenException()
			throws Exception {

		collectionsManager.getCollection("InexistentCollection");
	}

	@Test
	public void givenTwoCollectionsWhenAssignAuthorizationsThenOnlyWorkForGivenCollection()
			throws Exception {
		givenConstellioAndDoculibreCollectionsWithBobAndLegendsInConstellioAndLegendsAndHeroesInDoculibre();
		givenConstellioUserAuthorizationForChuckNorrisHeroesAndLegendsInTaxo1FirstTypeItems1And2();
		givenDoculibreUserAuthorizationForChuckNorrisHeroesAndLegendsInTaxo1FirstTypeItems1And2();
		waitForBatchProcess();

		givenFolderInConstellio();
		givenFolderInDoculibre();

		assertThat(resultCountWhenSearchingHas(users.aliceIn("constellio"))).isEqualTo(1);
		assertThat(resultCountWhenSearchingHas(users.bobIn("constellio"))).isEqualTo(0);
		assertThat(users.charles().getCollections()).doesNotContain("constellio").doesNotContain(SYSTEM_COLLECTION);
		assertThat(users.dakotaLIndien().getCollections()).doesNotContain("constellio").doesNotContain(SYSTEM_COLLECTION);
		assertThat(resultCountWhenSearchingHas(users.edouardLechatIn("constellio"))).isEqualTo(1);
		assertThat(resultCountWhenSearchingHas(users.gandalfLeblancIn("constellio"))).isEqualTo(1);
		assertThat(resultCountWhenSearchingHas(users.chuckNorrisIn("constellio"))).isEqualTo(1);

		assertThat(resultCountWhenSearchingHas(users.aliceIn("doculibre"))).isEqualTo(1);
		assertThat(users.bob().getCollections()).doesNotContain("doculibre").doesNotContain(SYSTEM_COLLECTION);
		assertThat(resultCountWhenSearchingHas(users.charlesIn("doculibre"))).isEqualTo(1);
		assertThat(resultCountWhenSearchingHas(users.dakotaLIndienIn("doculibre"))).isEqualTo(1);
		assertThat(resultCountWhenSearchingHas(users.edouardLechatIn("doculibre"))).isEqualTo(1);
		assertThat(resultCountWhenSearchingHas(users.gandalfLeblancIn("doculibre"))).isEqualTo(1);
		assertThat(resultCountWhenSearchingHas(users.chuckNorrisIn("doculibre"))).isEqualTo(1);
	}

	@Test
	public void givenTwoCollectionsWhenDeleteCollectionThenOnlyDeleteTheCollection()
			throws Exception {
		givenConstellioAndDoculibreCollectionsWithBobAndLegendsInConstellioAndLegendsAndHeroesInDoculibre();
		givenConstellioUserAuthorizationForChuckNorrisHeroesAndLegendsInTaxo1FirstTypeItems1And2();
		givenDoculibreUserAuthorizationForChuckNorrisHeroesAndLegendsInTaxo1FirstTypeItems1And2();
		waitForBatchProcess();

		givenFolderInConstellio();
		givenFolderInDoculibre();
		Set<String> collectionsInUserCredentialFile = getAllCollectionsInUserCredentialFile();
		Set<String> collectionsInVersionProperties = getAllCollectionsInVersionPropertiesFile();

		assertThat(collectionsInUserCredentialFile).doesNotContain("_system_");
		assertThat(collectionsInVersionProperties).contains("constellio_version");
		recordServices.flush();
		assertThat(getDataLayerFactory().getConfigManager().exist("/_system_/schemas.xml")).isTrue();
		assertThat(getDataLayerFactory().getConfigManager().exist("/_system_/roles.xml")).isTrue();
		assertThat(getDataLayerFactory().getConfigManager().exist("/_system_/taxonomies.xml")).isTrue();

		assertThat(collectionsInUserCredentialFile).contains("constellio");
		assertThat(collectionsInVersionProperties).contains("constellio_version");
		recordServices.flush();
		assertThat(getDataLayerFactory().getConfigManager().exist("/constellio/schemas.xml")).isTrue();
		assertThat(getDataLayerFactory().getConfigManager().exist("/constellio/roles.xml")).isTrue();
		assertThat(getDataLayerFactory().getConfigManager().exist("/constellio/taxonomies.xml")).isTrue();
		assertThat(userServices.getUserInfos(bobGratton).getCollections()).contains("constellio");

		collectionsManager.deleteCollection("constellio");
		recordServices.flush();

		try {
			searchServices.getResultsCount(fromAllSchemasIn("constellio").returnAll());
		} catch (MetadataSchemasManagerRuntimeException.MetadataSchemasManagerRuntimeException_NoSuchCollection e) {
			// collection deleted so no such collection.
		}

		assertThat(getDataLayerFactory().getConfigManager().exist("/constellio/authorizations.xml")).isFalse();
		assertThat(getDataLayerFactory().getConfigManager().exist("/constellio/schemas.xml")).isFalse();
		assertThat(getDataLayerFactory().getConfigManager().exist("/constellio/roles.xml")).isFalse();
		assertThat(getDataLayerFactory().getConfigManager().exist("/constellio/taxonomies.xml")).isFalse();
		collectionsInUserCredentialFile = getAllCollectionsInUserCredentialFile();
		collectionsInVersionProperties = getAllCollectionsInVersionPropertiesFile();
		assertThat(collectionsInUserCredentialFile).doesNotContain("constellio");
		assertThat(collectionsInVersionProperties).doesNotContain("constellio_version");

		assertThat(userServices.getUserInfos(bobGratton).getCollections()).doesNotContain("constellio");
		assertThat(userServices.getGroup("legends").getCollections()).doesNotContain("constellio")
				.contains("doculibre");
	}

	private Set<String> getAllCollectionsInUserCredentialFile() {
		Set<String> collections = new HashSet<>();
		List<SystemWideUserInfos> userCredentials = getModelLayerFactory().newUserServices().getActiveUserCredentials();
		for (SystemWideUserInfos userCredential : userCredentials) {
			collections.addAll(userCredential.getCollections());
		}
		return collections;
	}

	private Set<String> getAllCollectionsInVersionPropertiesFile() {
		PropertiesConfiguration propertiesConfiguration = getDataLayerFactory().getConfigManager()
				.getProperties("version.properties");
		Set<String> collectionsInVersionProperties = propertiesConfiguration.getProperties().keySet();
		return collectionsInVersionProperties;
	}

	private int resultCountWhenSearchingHas(User user) {
		MetadataSchema folderSchema = metadataSchemasManager.getSchemaTypes(user.getCollection()).getSchema("zefolder_default");
		LogicalSearchCondition condition = LogicalSearchQueryOperators.from(folderSchema).returnAll();
		LogicalSearchQuery query = new LogicalSearchQuery(condition);
		query.filteredWithUser(user);
		return searchServices.searchRecordIds(query).size();
	}

	private void givenFolderInConstellio()
			throws RecordServicesException {
		TestRecord constellioFolderInItem2 = new TestRecord(constellioFolderSchema.instance(), "constellioFolderInItem2");
		constellioFolderInItem2.set(constellioFolderSchema.taxonomy1(), constellioTaxos.taxo1_firstTypeItem2_secondTypeItem1);
		constellioFolderInItem2.set(constellioFolderSchema.title(), "My constellio folder");
		recordServices.add(constellioFolderInItem2);
	}

	private void givenFolderInDoculibre()
			throws RecordServicesException {
		TestRecord doculibreFolderInItem2 = new TestRecord(doculibreFolderSchema.instance(), "doculibreFolderInItem2");
		doculibreFolderInItem2.set(doculibreFolderSchema.taxonomy1(), doculibreTaxos.taxo1_firstTypeItem2_secondTypeItem1);
		doculibreFolderInItem2.set(doculibreFolderSchema.title(), "My doculibre folder");
		recordServices.add(doculibreFolderInItem2);
	}

	private void givenDoculibreUserAuthorizationForChuckNorrisHeroesAndLegendsInTaxo1FirstTypeItems1And2()
			throws RolesManagerRuntimeException {
		List<String> doculibreUserAuthorizationPrincipals = Arrays.asList(users.chuckNorrisIn("doculibre").getId(), users
				.legendsIn("doculibre").getId(), users.heroesIn("doculibre").getId());

		authorizationsServices.add(authorizationInCollection("doculibre").givingReadWriteAccess()
				.forPrincipalsIds(doculibreUserAuthorizationPrincipals).on(doculibreTaxos.taxo1_firstTypeItem1));

		authorizationsServices.add(authorizationInCollection("doculibre").givingReadWriteAccess()
				.forPrincipalsIds(doculibreUserAuthorizationPrincipals).on(doculibreTaxos.taxo1_firstTypeItem2));
	}

	private void givenConstellioUserAuthorizationForChuckNorrisHeroesAndLegendsInTaxo1FirstTypeItems1And2()
			throws RolesManagerRuntimeException {

		List<String> constellioUserAuthorizationPrincipals = Arrays.asList(users.chuckNorrisIn("constellio").getId(), users
				.legendsIn("constellio").getId(), users.heroesIn("constellio").getId());

		authorizationsServices.add(authorizationInCollection("constellio").givingReadAccess()
				.forPrincipalsIds(constellioUserAuthorizationPrincipals).on(constellioTaxos.taxo1_firstTypeItem1));

		authorizationsServices.add(authorizationInCollection("constellio").givingReadAccess()
				.forPrincipalsIds(constellioUserAuthorizationPrincipals).on(constellioTaxos.taxo1_firstTypeItem2));

	}

	private void givenConstellioAndDoculibreCollectionsWithBobAndLegendsInConstellioAndLegendsAndHeroesInDoculibre() {


		givenCollection("constellio");
		givenCollection("doculibre");
		users.setUp(userServices, "doculibre");
		users.setUp(userServices, "constellio");

		userServices.execute(users.bob().getUsername(), (req) -> req.addToCollection("constellio"));
		userServices.execute(users.chuckNorris().getUsername(), (req) -> req.addToCollection("constellio"));
		userServices.execute(users.chuckNorris().getUsername(), (req) -> req.addToCollection("doculibre"));

		userServices.execute(users.legendsRequest().addCollections(
				Arrays.asList("constellio", "doculibre")));
		userServices
				.execute(users.heroesRequest().addCollections("constellio", "doculibre"));

		userServices.streamUserInfos("constellio").forEach((u) -> {
			if (!u.getUsername().equals("bob") && !u.getUsername().equals("chuck") && !u.getGroupCodes("constellio").contains("legends")) {
				userServices.execute(u.getUsername(), req -> req.removeFromCollection("constellio"));
			}
		});
		userServices.execute(dakota, req -> req.removeFromCollection("constellio"));
		userServices.execute(bob, req -> req.removeFromCollection("doculibre"));

		defineSchemasManager().using(constellioSchemas);
		defineSchemasManager().using(doculibreSchemas);
		taxonomiesManager.addTaxonomy(constellioSchemas.getTaxo1(), metadataSchemasManager);
		taxonomiesManager.setPrincipalTaxonomy(constellioSchemas.getTaxo1(), metadataSchemasManager);
		taxonomiesManager.addTaxonomy(doculibreSchemas.getTaxo1(), metadataSchemasManager);
		taxonomiesManager.setPrincipalTaxonomy(doculibreSchemas.getTaxo1(), metadataSchemasManager);
		constellioTaxos = constellioSchemas.givenTaxonomyRecords(recordServices);
		doculibreTaxos = doculibreSchemas.givenTaxonomyRecords(recordServices);
	}
}

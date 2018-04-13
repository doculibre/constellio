package com.constellio.app.services.collections;

import static junit.framework.TestCase.fail;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.apache.solr.common.params.ModifiableSolrParams;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;

import com.constellio.app.services.collections.CollectionsManagerRuntimeException.CollectionsManagerRuntimeException_CollectionLanguageMustIncludeSystemMainDataLanguage;
import com.constellio.app.services.collections.CollectionsManagerRuntimeException.CollectionsManagerRuntimeException_CollectionWithGivenCodeAlreadyExists;
import com.constellio.app.services.collections.CollectionsManagerRuntimeException.CollectionsManagerRuntimeException_InvalidCode;
import com.constellio.app.services.collections.CollectionsManagerRuntimeException.CollectionsManagerRuntimeException_InvalidLanguage;
import com.constellio.app.services.extensions.ConstellioModulesManagerImpl;
import com.constellio.app.services.extensions.plugins.ConstellioPluginManager;
import com.constellio.app.services.migrations.MigrationServices;
import com.constellio.app.services.systemSetup.SystemGlobalConfigsManager;
import com.constellio.data.dao.dto.records.TransactionDTO;
import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.data.dao.services.records.RecordDao;
import com.constellio.data.utils.Delayed;
import com.constellio.model.conf.ModelLayerConfiguration;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.cache.RecordsCaches;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchBoostManager;
import com.constellio.model.services.security.AuthorizationDetailsManager;
import com.constellio.model.services.security.roles.RolesManager;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.model.services.users.GlobalGroupsManager;
import com.constellio.model.services.users.UserCredentialsManager;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;

public class CollectionsManagerTest extends ConstellioTest {

	@Mock RecordsCaches caches;
	@Mock ConstellioPluginManager pluginManager;
	@Mock CollectionsListManager collectionsListManager;
	@Mock SearchBoostManager searchBoostManager;
	@Mock ModelLayerFactory modelLayerFactory;
	@Mock ModelLayerConfiguration modelLayerConfiguration;

	@Mock SystemGlobalConfigsManager systemGlobalConfigsManager;
	@Mock GlobalGroupsManager globalGroupsManager;
	@Mock MetadataSchemasManager metadataSchemasManager;
	@Mock TaxonomiesManager taxonomiesManager;
	@Mock AuthorizationDetailsManager authorizationDetailsManager;
	@Mock RolesManager rolesManager;
	@Mock MigrationServices migrationServices;
	@Mock ConstellioModulesManagerImpl modulesManager;
	@Mock UserServices userServices;
	@Mock RecordServices recordServices;
	@Mock Record record;
	@Mock MetadataSchema metadataSchema;
	@Mock DataLayerFactory dataLayerFactory;
	@Mock RecordDao recordDao;
	@Mock TransactionDTO transactionDTO;
	@Mock ModifiableSolrParams params;
	@Mock UserCredentialsManager userCredentialsManager;
	@Mock ConfigManager configManager;
	@Mock Record aNewCollection, anotherNewCollection;

	com.constellio.app.services.collections.CollectionsManager collectionsManager;

	@Before
	public void setUp()
			throws Exception {

		when(modelLayerFactory.getMetadataSchemasManager()).thenReturn(metadataSchemasManager);
		when(modelLayerFactory.getTaxonomiesManager()).thenReturn(taxonomiesManager);
		when(modelLayerFactory.getAuthorizationDetailsManager()).thenReturn(authorizationDetailsManager);
		when(modelLayerFactory.getRolesManager()).thenReturn(rolesManager);
		when(modelLayerFactory.getGlobalGroupsManager()).thenReturn(globalGroupsManager);
		when(modelLayerFactory.newUserServices()).thenReturn(userServices);
		when(modelLayerFactory.getConfiguration()).thenReturn(modelLayerConfiguration);
		when(modelLayerFactory.getDataLayerFactory()).thenReturn(dataLayerFactory);
		when(modelLayerFactory.getConfiguration()).thenReturn(modelLayerConfiguration);
		when(modelLayerFactory.getCollectionsListManager()).thenReturn(collectionsListManager);
		when(modelLayerFactory.getSearchBoostManager()).thenReturn(searchBoostManager);
		when(modelLayerConfiguration.getMainDataLanguage()).thenReturn("fr");

		collectionsManager = spy(new com.constellio.app.services.collections.CollectionsManager(
				modelLayerFactory, modulesManager, new Delayed<>(migrationServices), systemGlobalConfigsManager));
	}

	@Test
	public void whenAddingCollectionThenCreateConfigFilesBeforeAddingItInList()
			throws Exception {

		doNothing().when(collectionsManager).createCollectionConfigs("zeCollection");
		doReturn(aNewCollection).when(collectionsManager)
				.createCollectionRecordWithCode("zeCollection", "zeCollection", Arrays.asList("fr"));
		doNothing().when(collectionsManager).initializeCollection(anyString());

		collectionsManager.createCollectionInCurrentVersion("zeCollection", Arrays.asList("fr"));

		verify(collectionsManager).createCollectionConfigs("zeCollection");
		verify(collectionsListManager).addCollection("zeCollection", Arrays.asList("fr"));
		verify(migrationServices).migrate("zeCollection", null, true);
		verify(userServices).addGlobalGroupsInCollection("zeCollection");
		verify(collectionsManager).initializeCollection("zeCollection");
	}

	public void whenAddingCollectionWithNonUniqueCodeThenException()
			throws Exception {

		when(collectionsListManager.getCollections()).thenReturn(Arrays.asList("zeCollection", "anotherCollection"));

		try {
			collectionsManager.createCollectionInCurrentVersion("zeCollection", Arrays.asList("fr"));
			fail("CollectionsServicesRuntimeException_CollectionWithGivenCodeAlreadyExists expected");
		} catch (CollectionsManagerRuntimeException_CollectionWithGivenCodeAlreadyExists e) {
			// OK
		}
		verify(collectionsManager, never()).createCollectionConfigs("zeCollection");
		verify(collectionsListManager, never()).addCollection("zeCollection", Arrays.asList("fr"));
	}

	@Test
	public void givenExceptionWhileCreatingConfigFilesWhenAddingCollectionThenNotAddedAndExceptionThrown()
			throws Exception {

		Throwable otherManagerException = new RuntimeException("anException");
		doThrow(otherManagerException).when(collectionsManager).createCollectionConfigs("zeCollection");

		try {
			collectionsManager.createCollectionInCurrentVersion("zeCollection", Arrays.asList("fr"));
		} catch (Exception thrown) {
			assertThat(thrown).isSameAs(otherManagerException);
		}
		verify(collectionsListManager, never()).addCollection(anyString(), eq(Arrays.asList("fr")));

	}

	@Test
	public void whenCreatingConfigFilesThenCallMultipleManagers()
			throws Exception {

		collectionsManager.createCollectionConfigs(zeCollection);

		verify(metadataSchemasManager).createCollectionSchemas(zeCollectionInfo);
		verify(taxonomiesManager).createCollectionTaxonomies(zeCollection);
		verify(authorizationDetailsManager).createCollectionAuthorizationDetail(zeCollection);
		verify(rolesManager).createCollectionRole(zeCollection);
		verify(searchBoostManager).createCollectionSearchBoost(zeCollection);
	}

	@Test
	public void whenDeleteCollectionCallMethods()
			throws Exception {
		when(collectionsManager.newTransactionDTO()).thenReturn(transactionDTO);
		when(collectionsManager.newModifiableSolrParams()).thenReturn(params);
		when(transactionDTO.withDeletedByQueries(params)).thenReturn(transactionDTO);
		when(params.get("q")).thenReturn("collection_s:" + zeCollection);
		when(dataLayerFactory.newRecordDao()).thenReturn(recordDao);
		when(dataLayerFactory.getConfigManager()).thenReturn(configManager);
		when(modelLayerFactory.getUserCredentialsManager()).thenReturn(userCredentialsManager);
		when(modelLayerFactory.getRecordsCaches()).thenReturn(caches);

		collectionsManager.deleteCollection(zeCollection);

		InOrder inOrder = inOrder(collectionsListManager, userCredentialsManager, configManager, transactionDTO, recordDao,
				modulesManager, globalGroupsManager, caches);
		inOrder.verify(userCredentialsManager).removeCollection(zeCollection);
		inOrder.verify(globalGroupsManager).removeCollection(zeCollection);
		inOrder.verify(collectionsListManager).remove(zeCollection);
		inOrder.verify(transactionDTO).withDeletedByQueries(params);
		inOrder.verify(recordDao).execute(transactionDTO);
		inOrder.verify(modulesManager).removeCollectionFromVersionProperties(zeCollection, configManager);
		inOrder.verify(caches).invalidate(zeCollection);

	}

	@Test
	public void whenCreatingCollectionWithoutSystemDataLanguagethenException()
			throws Exception {

		try {
			collectionsManager.createCollectionInCurrentVersion("zeCollection", Arrays.asList("en"));
			fail("CollectionsServicesRuntimeException_CollectionLanguageMustIncludeSystemMainDataLanguage expected");
		} catch (CollectionsManagerRuntimeException_CollectionLanguageMustIncludeSystemMainDataLanguage e) {
			//OK
		}

		verify(collectionsManager, never()).createCollectionConfigs("zeCollection");

	}

	@Test
	public void whenCreatingCollectionWithInvalidLanguageSystemDataLanguagethenException()
			throws Exception {

		try {
			collectionsManager.createCollectionInCurrentVersion("zeCollection", Arrays.asList("fr", "klingon"));
			fail("CollectionsServicesRuntimeException_CollectionLanguageMustIncludeSystemMainDataLanguage expected");
		} catch (CollectionsManagerRuntimeException_InvalidLanguage e) {
			//OK
		}

		verify(collectionsManager, never()).createCollectionConfigs("zeCollection");

	}

	@Test
	public void whenValidatingCollectionCodeThenCodeMustBeAlphanumericAndStartWithALetter()
			throws Exception {

		try {
			collectionsManager.validateCode("2");
			fail("Invalid code");
		} catch (CollectionsManagerRuntimeException_InvalidCode e) {
			//OK
		}

		try {
			collectionsManager.validateCode("Un code avec des espaces");
			fail("Invalid code");
		} catch (CollectionsManagerRuntimeException_InvalidCode e) {
			//OK
		}

		try {
			collectionsManager.validateCode("Un-code-avec-des-tirets");
			fail("Invalid code");
		} catch (CollectionsManagerRuntimeException_InvalidCode e) {
			//OK
		}

		try {
			collectionsManager.validateCode("Un_code_avec_des_tirets");
			fail("Invalid code");
		} catch (CollectionsManagerRuntimeException_InvalidCode e) {
			//OK
		}

		try {
			collectionsManager.validateCode("UnCodeAvecDesAccentsèÉ");
			fail("Invalid code");
		} catch (CollectionsManagerRuntimeException_InvalidCode e) {
			//OK
		}

		collectionsManager.validateCode("valideCode42");
	}
}

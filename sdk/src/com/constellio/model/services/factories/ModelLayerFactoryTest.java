package com.constellio.model.services.factories;

import static com.constellio.data.conf.HashingEncoding.BASE64_URL_ENCODED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;

import org.jdom2.Document;
import org.jdom2.Element;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.constellio.app.services.extensions.plugins.ConstellioPluginManager;
import com.constellio.data.conf.DataLayerConfiguration;
import com.constellio.data.dao.managers.StatefullServiceDecorator;
import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.data.dao.managers.config.values.XMLConfiguration;
import com.constellio.data.dao.services.DataStoreTypesFactory;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.data.io.IOServicesFactory;
import com.constellio.data.utils.Delayed;
import com.constellio.model.conf.FoldersLocator;
import com.constellio.model.conf.ModelLayerConfiguration;
import com.constellio.model.services.batch.controller.BatchProcessController;
import com.constellio.model.services.batch.manager.BatchProcessesManager;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.extensions.ConstellioModulesManager;
import com.constellio.model.services.parser.ForkParsers;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.security.AuthorizationDetailsManager;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.model.services.users.GlobalGroupsManager;
import com.constellio.model.services.users.UserCredentialsManager;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.schemas.FakeDataStoreTypeFactory;

public class ModelLayerFactoryTest extends ConstellioTest {

	int numberOfRecordsPerTask = anInteger();
	int batchProcessPartSize = anInteger();
	String zeComputer = aString();

	@Mock DataLayerConfiguration dataLayerConfiguration;
	@Mock ConfigManager configManager;
	@Mock DataLayerFactory dataLayerFactory;
	@Mock IOServicesFactory ioServicesFactory;
	@Mock ForkParsers forkParsers;
	@Mock FoldersLocator foldersLocator;
	@Mock ModelLayerConfiguration modelLayerConfiguration;
	@Mock File profiles;
	DataStoreTypesFactory typesFactory = new FakeDataStoreTypeFactory();
	@Mock Delayed<ConstellioPluginManager> constellioPluginManager;
	@Mock ConstellioModulesManager constellioModulesManager;
	ModelLayerFactory modelLayerFactory;
	StatefullServiceDecorator statefullServiceDecorator = new StatefullServiceDecorator();

	@Before
	public void setUp() {

		XMLConfiguration xmlConfiguration = Mockito.mock(XMLConfiguration.class);
		Document document = Mockito.mock(Document.class);
		when(configManager.getXML("/authorizations.xml")).thenReturn(xmlConfiguration);
		when(configManager.getXML("/userCredentialsConfig.xml")).thenReturn(xmlConfiguration);
		when(xmlConfiguration.getDocument()).thenReturn(document);
		when(document.getRootElement()).thenReturn(new Element("authorizations"));

		when(dataLayerFactory.getDataLayerConfiguration()).thenReturn(dataLayerConfiguration);
		when(dataLayerConfiguration.getHashingEncoding()).thenReturn(BASE64_URL_ENCODED);

		when(dataLayerFactory.getIOServicesFactory()).thenReturn(ioServicesFactory);
		when(dataLayerFactory.newTypesFactory()).thenReturn(typesFactory);
		when(dataLayerFactory.getConfigManager()).thenReturn(configManager);
		when(foldersLocator.getLanguageProfiles()).thenReturn(profiles);
		when(profiles.listFiles()).thenReturn(new File[0]);

		modelLayerFactory = spy(
				new ModelLayerFactory(dataLayerFactory, foldersLocator, modelLayerConfiguration,
						statefullServiceDecorator, new Delayed<>(constellioModulesManager)));
	}

	@Test
	public void whenNewRecordServicesCreatedThenNotNull() {
		when(dataLayerFactory.newTypesFactory()).thenReturn(typesFactory);

		assertTrue(modelLayerFactory.newRecordServices() != null);
	}

	@Test
	public void whenNewSearchServicesThenNotNull()
			throws Exception {

		assertThat(modelLayerFactory.newSearchServices()).isNotNull();
	}

	@Test
	public void whenNewTaxonomiesSearchServicesThenUseDao()
			throws Exception {

		assertThat(modelLayerFactory.newTaxonomiesSearchService()).isNotNull();
	}

	@Test
	public void whenNewUserServicesThenNotNull()
			throws Exception {

		assertThat(modelLayerFactory.newUserServices()).isNotNull();
	}

	@Test
	public void whenGettingSchemaManagerThenAlwaysSameInstance()
			throws Exception {
		MetadataSchemasManager schemasManager1 = modelLayerFactory.getMetadataSchemasManager();
		MetadataSchemasManager schemasManager2 = modelLayerFactory.getMetadataSchemasManager();

		assertThat(schemasManager1).isNotNull().isSameAs(schemasManager2);
	}

	@Test
	public void whenGettingCollectionsListManagerThenAlwaysSameInstance()
			throws Exception {
		CollectionsListManager collectionsManager1 = modelLayerFactory.getCollectionsListManager();
		CollectionsListManager collectionsManager2 = modelLayerFactory.getCollectionsListManager();

		assertThat(collectionsManager1).isNotNull().isSameAs(collectionsManager2);
	}

	@Test
	public void whenGettingTaxonomiesManagerThenAlwaysSameInstance()
			throws Exception {
		TaxonomiesManager taxonomiesManager1 = modelLayerFactory.getTaxonomiesManager();
		TaxonomiesManager taxonomiesManager2 = modelLayerFactory.getTaxonomiesManager();

		assertThat(taxonomiesManager1).isNotNull().isSameAs(taxonomiesManager2);
	}

	@Test
	public void whenGettingBatchProcessesManagerThenAlwaysSameInstance()
			throws Exception {
		BatchProcessesManager processesManager1 = modelLayerFactory.getBatchProcessesManager();
		BatchProcessesManager processesManager2 = modelLayerFactory.getBatchProcessesManager();

		assertThat(processesManager1).isNotNull().isSameAs(processesManager2);
	}

	@Test
	public void whenGettingBatchProcessesControllerThenAlwaysSameInstance()
			throws Exception {
		BatchProcessController controler1 = modelLayerFactory.getBatchProcessesController();
		BatchProcessController controler2 = modelLayerFactory.getBatchProcessesController();

		assertThat(controler1).isNotNull().isSameAs(controler2);
	}

	@Test
	public void whenGetAuthorizationDetailsManagerThenSameInstance()
			throws Exception {

		AuthorizationDetailsManager authorizationDetailsManager1 = modelLayerFactory.getAuthorizationDetailsManager();
		AuthorizationDetailsManager authorizationDetailsManager2 = modelLayerFactory.getAuthorizationDetailsManager();

		assertThat(authorizationDetailsManager1).isNotNull().isSameAs(authorizationDetailsManager2);

	}

	@Test
	public void whenGetGlobalGroupsManagerThenSameInstance()
			throws Exception {

		GlobalGroupsManager globalGroupsManager1 = modelLayerFactory.getGlobalGroupsManager();
		GlobalGroupsManager globalGroupsManager2 = modelLayerFactory.getGlobalGroupsManager();

		assertThat(globalGroupsManager1).isNotNull().isSameAs(globalGroupsManager2);

	}

	@Test
	public void whenGetUserCredentialsManagerThenSameInstance()
			throws Exception {

		UserCredentialsManager userCredentialsManager1 = modelLayerFactory.getUserCredentialsManager();
		UserCredentialsManager userCredentialsManager2 = modelLayerFactory.getUserCredentialsManager();

		assertThat(userCredentialsManager1).isNotNull().isSameAs(userCredentialsManager2);

	}

	@Test
	public void whenNewAuthorizationsServicesThenNotNull() {
		assertThat(modelLayerFactory.newAuthorizationsServices()).isNotNull();
		verify(modelLayerFactory).getAuthorizationDetailsManager();
		verify(modelLayerFactory, times(2)).getRolesManager();
		verify(modelLayerFactory).getTaxonomiesManager();
	}

	@Test
	public void whenGetPasswordFileAuthenticationServiceThenNotNull()
			throws Exception {

		assertThat(modelLayerFactory.newAuthenticationService()).isNotNull();
	}

	@Test
	public void whenGetSearchBoostManagerThenNotNull()
			throws Exception {

		assertThat(modelLayerFactory.getSearchBoostManager()).isNotNull();
	}
}

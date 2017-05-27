package com.constellio.app.modules.es.ui.pages;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.constellio.app.modules.es.navigation.ESNavigationConfiguration;
import com.constellio.sdk.tests.MockedNavigation;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.app.modules.es.model.connectors.ConnectorInstance;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbDocument;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbFolder;
import com.constellio.app.modules.es.services.ConnectorManager;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.CoreViews;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.data.RecordVOWithDistinctSchemasDataProvider;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.services.records.RecordServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;

/**
 * Created by Patrick on 2015-12-01.
 */
public class ConnectorReportPresenterAcceptTest extends ConstellioTest {

	RecordToVOBuilder voBuilder = new RecordToVOBuilder();
	@Mock ConnectorReportView view;
	MockedNavigation navigator;
	SessionContext sessionContext;
	RecordServices recordServices;
	ConnectorInstance connectorInstance;
	ConnectorManager connectorManager;
	ESSchemasRecordsServices es;
	ConnectorSmbDocument connectorSmbDocument1, connectorSmbDocument2;
	ConnectorSmbFolder connectorSmbFolder1, connectorSmbFolder2;
	ConnectorReportPresenter presenter;
	RecordVOWithDistinctSchemasDataProvider dataProvider;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioESModule().withAllTestUsers()
		);

		ConstellioFactories constellioFactories = getConstellioFactories();

		sessionContext = FakeSessionContext.adminInCollection(zeCollection);
		sessionContext.setCurrentLocale(Locale.FRENCH);

		when(view.getSessionContext()).thenReturn(sessionContext);
		when(view.getCollection()).thenReturn(zeCollection);
		when(view.getConstellioFactories()).thenReturn(constellioFactories);
		when(view.navigate()).thenReturn(navigator);

		inCollection(zeCollection).setCollectionTitleTo("Collection de test");

		recordServices = getModelLayerFactory().newRecordServices();
		es = new ESSchemasRecordsServices(zeCollection, getAppLayerFactory());
		recordServices = getModelLayerFactory().newRecordServices();
		connectorManager = es.getConnectorManager();

		connectorInstance = connectorManager.createConnector(es.newConnectorSmbInstance()
				.setTitle("zeConnectorSMB")
				.setCode("zeConnectorSMB")
				.setEnabled(false)
				.setSeeds(asList("share")).setUsername("username").setPassword("password").setDomain("domain")
				.setTraversalCode("zeTraversal"));

		connectorSmbDocument1 = es.newConnectorSmbDocumentWithId("doc1", connectorInstance).setUrl("1.txt")
				.setParsedContent("Document 1 content").setParent("folder1");
		connectorSmbDocument2 = es.newConnectorSmbDocumentWithId("doc2", connectorInstance).setUrl("2.txt")
				.setParsedContent("Document 2 content").setParent("folder2");
		connectorSmbFolder1 = es.newConnectorSmbFolderWithId("folder1", connectorInstance).setUrl("folder1");
		connectorSmbFolder2 = es.newConnectorSmbFolderWithId("folder2", connectorInstance).setUrl("folder2");

		Transaction transaction = new Transaction();

		transaction.add(connectorSmbFolder1);
		transaction.add(connectorSmbFolder2);
		transaction.add(connectorSmbDocument1);
		transaction.add(connectorSmbDocument2);
		recordServices.execute(transaction);

		presenter = new ConnectorReportPresenter(view);
		Map<String, String> params = new HashMap<>();
		params.put(ConnectorReportView.CONNECTOR_ID, connectorInstance.getId());
		params.put(ConnectorReportView.REPORT_MODE, ConnectorReportView.INDEXING);
		String viewPath = ParamUtils.addParams(ESNavigationConfiguration.CONNECTOR_REPORT, params);
		presenter.forParams(viewPath);
		dataProvider = presenter.getDataProvider();
	}

	@Test
	public void whenGetDataProviderThenOk()
			throws Exception {

		assertThat(presenter.getDataProvider().size()).isEqualTo(4);
		assertThat(presenter.getDataProvider().listRecordVOs(0, 4)).extracting("id")
				.contains("folder1", "folder2", "doc1", "doc2");
		assertThat(presenter.getDataProvider().getRecordVO(0).getId()).isEqualTo("doc1");
	}

	@Test
	public void whenGetFilteredDataProviderThenOk()
			throws Exception {

		assertThat(presenter.getFilteredDataProvider("1.txt").size()).isEqualTo(1);
		assertThat(presenter.getFilteredDataProvider("1").size()).isEqualTo(2);
		assertThat(presenter.getFilteredDataProvider("txt").listRecordVOs(0, 4)).extracting("id")
				.contains("doc1", "doc2");
		assertThat(presenter.getFilteredDataProvider("1.txt").getRecordVO(0).getId()).isEqualTo("doc1");
	}

	@Test
	public void givenDataProviderWhenSortThenOk()
			throws Exception {

		RecordVOWithDistinctSchemasDataProvider dataProvider = presenter.getDataProvider();

		MetadataSchemaVO schemaVO = dataProvider.getSchemas().get(0);
		Object[] sortPropertyIds = new Object[1];
		sortPropertyIds[0] = "url";
		List<MetadataVO> sortMetadatas = new ArrayList<MetadataVO>();
		String sortMetadataCode = (String) sortPropertyIds[0];
		sortMetadatas.add(schemaVO.getMetadata(sortMetadataCode));
		boolean[] sort = new boolean[1];
		sort[0] = true;
		try {
			dataProvider.sort(sortMetadatas.toArray(new MetadataVO[0]), sort);
		} catch (Exception e) {
			fail("Cannot sort", e);
		}
	}

	@Test
	public void givenReportModeIndexationWhenGetReportMetadataListThenOk()
			throws Exception {

		assertThat(presenter.getReportMetadataList())
				.isEqualTo(Arrays.asList(ConnectorSmbDocument.URL, ConnectorSmbDocument.FETCHED_DATETIME));
	}

	@Test
	public void whenGetTotalDocumentsCountThenOk()
			throws Exception {

		assertThat(presenter.getTotalDocumentsCount()).isEqualTo(4);
	}

	@Test
	public void whenGetFetchedDocumentsCountThenOk()
			throws Exception {

		assertThat(presenter.getFetchedDocumentsCount()).isEqualTo(4);
	}

	@Test
	public void whenGetUnfetchedDocumentsCountThenOk()
			throws Exception {

		recordServices.update(connectorSmbDocument1.setFetched(false));
		recordServices.update(connectorSmbFolder1.setFetched(false));
		assertThat(presenter.getUnfetchedDocumentsCount()).isEqualTo(2);
	}
}

package com.constellio.app.ui.pages.management.facet;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.management.facet.AddEditFacetConfigurationPresenter.AvailableFacetFieldMetadata;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Facet;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class FacetConfigurationServiceAcceptTest extends ConstellioTest {
	@Mock AddEditFacetConfigurationView view;
	FacetConfigurationPresenterService service;
	SearchServices searchServices;
	MetadataSchemasManager schemasManager;

	RMTestRecords records = new RMTestRecords(zeCollection);
	private SchemasRecordsServices schemasRecords;
	private RecordServices recordServices;
	private SessionContext sessionContext;

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(
				withZeCollection().withConstellioRMModule().withRMTest(records).withFoldersAndContainersOfEveryStatus()
		);

		when(view.getConstellioFactories()).thenReturn(getConstellioFactories());
		sessionContext = FakeSessionContext.adminInCollection(zeCollection);
		when(view.getSessionContext()).thenReturn(sessionContext);

		service = spy(new FacetConfigurationPresenterService(getConstellioFactories(),
				FakeSessionContext.adminInCollection(zeCollection)));
		schemasRecords = new SchemasRecordsServices(zeCollection, getModelLayerFactory());

		recordServices = schemasRecords.getModelLayerFactory().newRecordServices();
		searchServices = getModelLayerFactory().newSearchServices();
		schemasManager = getModelLayerFactory().getMetadataSchemasManager();

		recordServices.update(records.getFolder_A04().setKeywords(asList("King Dedede", "La passe de la baleine échouée")));
		recordServices.update(records.getFolder_A07().setKeywords(asList("aKeyword")));
	}

	@Test
	public void whenGetAvailableDataStoreCodeThenReturnGoodValues()
			throws Exception {

		List<AvailableFacetFieldMetadata> availableDataStoreCodes = service.getAvailableDataStoreCodes();
		assertThat(availableDataStoreCodes).extracting("code")
				.containsOnlyOnce("createdById_s", "retentionRuleId_s", "keywords_ss", "copyStatus_s", "borrowed_s", "schema_s")
				.doesNotContain(Schemas.TITLE.getDataStoreCode(), Schemas.CREATED_ON.getDataStoreCode(),
						Schemas.TOKENS.getDataStoreCode(), "title_s", "createdOn_dt", "content_s", "tokens_ss", "username_s",
						"description_txt", "pendingAlerts_ss");

	}

	@Test
	public void whenDataStoreCodeDoesntContainsIdThenAllowsLabel()
			throws Exception {
		assertThat(service.isDataStoreCodeSupportingLabelValues("schema_s")).isFalse();
		assertThat(service.isDataStoreCodeSupportingLabelValues("createdById_s")).isFalse();
		assertThat(service.isDataStoreCodeSupportingLabelValues("retentionRuleId_s")).isFalse();
		assertThat(service.isDataStoreCodeSupportingLabelValues("keywords_ss")).isTrue();
		assertThat(service.isDataStoreCodeSupportingLabelValues("copyStatus_s")).isFalse();
		assertThat(service.isDataStoreCodeSupportingLabelValues("borrowed_s")).isTrue();

	}

	@Test
	public void whenValidQueryThenEmpty() {
		Map<Integer, Map<String, String>> values = new HashMap<>();
		Map<String, String> map = new HashMap<>();
		map.put("zeLabel", "*:*");

		Map<String, String> map1 = new HashMap<>();
		map1.put("zeLabel", "*:*");

		values.put(1, map);
		values.put(2, map1);

		assertThat(service.getInvalidQuery(values)).hasSize(0);
	}

	@Test
	public void whenInvalidQueryThenListed() {
		Map<Integer, Map<String, String>> values = new HashMap<>();
		Map<String, String> map = new HashMap<>();
		map.put("zeLabel", "thisisinvalid");

		Map<String, String> map1 = new HashMap<>();
		map1.put("zeLabel", "*:*");

		values.put(1, map);
		values.put(2, map1);

		assertThat(service.getInvalidQuery(values)).hasSize(1);
	}

	@Test
	public void whenGetFieldFacetValuesThenObtainValidecordValues()
			throws Exception {
		assertThat(service.getFieldFacetValues("keywords_ss"))
				.containsOnly("King Dedede", "La passe de la baleine échouée", "aKeyword");
		assertThat(service.getFieldFacetValues("borrowed_s")).containsOnly("__TRUE__", "__FALSE__");

	}

	@Test
	public void givenActiveFacetWhenIsActiveThenTrue()
			throws Exception {

		Record record = givenActiveFacetRecord();

		RecordVO recordVO = buildRecordVO(record);
		assertThat(service.isActive(recordVO)).isTrue();
	}

	@Test
	public void givenActiveFacetWhenDeactivateThenIsActiveFalse()
			throws Exception {

		Record record = givenActiveFacetRecord();

		service.deactivate(record.getId());

		RecordVO recordVO = buildRecordVO(record);
		assertThat(service.isActive(recordVO)).isFalse();
	}

	@Test
	public void givenDeactivateFacetWhenActivateThenIsActiveTrue()
			throws Exception {

		Record record = givenActiveFacetRecord();
		service.deactivate(record.getId());
		RecordVO recordVO = buildRecordVO(record);
		assertThat(service.isActive(recordVO)).isFalse();

		service.activate(record.getId());
		recordVO = new RecordToVOBuilder()
				.build(recordServices.getDocumentById(record.getId()), VIEW_MODE.DISPLAY, sessionContext);
		assertThat(service.isActive(recordVO)).isTrue();
	}

	//
	private RecordVO buildRecordVO(Record record) {
		return new RecordToVOBuilder()
				.build(recordServices.getDocumentById(record.getId()), VIEW_MODE.DISPLAY, sessionContext);
	}

	private Record givenActiveFacetRecord() {
		MetadataSchemaType type = schemasManager.getSchemaTypes(zeCollection).getSchemaType(Facet.SCHEMA_TYPE);
		LogicalSearchCondition condition = LogicalSearchQueryOperators.from(type).where(schemasRecords.facet.active()).isTrue();
		LogicalSearchQuery query = new LogicalSearchQuery();
		query.setCondition(condition);
		query.setNumberOfRows(1);
		query.sortAsc(Schemas.IDENTIFIER);
		List<Record> records = searchServices.search(query);
		assertThat(records.get(0).getSchemaCode()).startsWith(Facet.SCHEMA_TYPE);
		return records.get(0);
	}
}

package com.constellio.app.ui.pages.management.facet;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.pages.base.BasePresenterUtils;
import com.constellio.app.ui.pages.base.SchemaPresenterUtils;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.management.facet.AddEditFacetConfigurationPresenter.AvailableFacetFieldMetadata;
import com.constellio.data.dao.dto.records.FacetValue;
import com.constellio.data.dao.services.records.RecordDao;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.data.utils.LangUtils;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Facet;
import com.constellio.model.entities.records.wrappers.structure.FacetType;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.structures.MapStringStringStructure;
import com.constellio.model.frameworks.validation.OptimisticLockException;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.search.SPEQueryResponse;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import org.apache.solr.common.params.ModifiableSolrParams;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.entities.schemas.MetadataValueType.BOOLEAN;
import static com.constellio.model.entities.schemas.MetadataValueType.ENUM;
import static com.constellio.model.entities.schemas.MetadataValueType.INTEGER;
import static com.constellio.model.entities.schemas.MetadataValueType.NUMBER;
import static com.constellio.model.entities.schemas.MetadataValueType.REFERENCE;
import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static com.constellio.model.entities.schemas.Schemas.CODE;
import static com.constellio.model.entities.schemas.Schemas.CREATED_BY;
import static com.constellio.model.entities.schemas.Schemas.MODIFIED_BY;
import static com.constellio.model.entities.schemas.Schemas.SCHEMA;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;

public class FacetConfigurationPresenterService extends BasePresenterUtils {

	private static List<String> FIELD_WITH_AUTOMATIC_LABELS = asList(Schemas.SCHEMA.getDataStoreCode());

	private static List<String> ALLOWED_GLOBAL_METADATAS_FOR_FIELD_FACETS = asList(
			CREATED_BY.getLocalCode(),
			MODIFIED_BY.getLocalCode(),
			SCHEMA.getLocalCode(),
			CODE.getLocalCode());
	private SchemasDisplayManager schemasDisplayManager;
	private SchemasRecordsServices schemasRecords;
	private String collection;
	private ModelLayerFactory modelLayerFactory;
	private SessionContext sessionContext;

	public FacetConfigurationPresenterService(ConstellioFactories factories, SessionContext sessionContext) {
		super(factories, sessionContext);
		collection = getCollection();
		schemasRecords = new SchemasRecordsServices(collection, appLayerFactory().getModelLayerFactory());
		schemasDisplayManager = appLayerFactory().getMetadataSchemasDisplayManager();
		modelLayerFactory = factories.getModelLayerFactory();
		this.sessionContext = sessionContext;
	}

	public MapStringStringStructure getStructureValue(Map<Integer, Map<String, String>> values) {
		MapStringStringStructure structure = new MapStringStringStructure();

		for (Entry<Integer, Map<String, String>> value : values.entrySet()) {
			Map<String, String> map = value.getValue();
			for (Entry<String, String> labels : map.entrySet()) {
				structure.put(labels.getValue(), labels.getKey());
			}
		}

		return structure;
	}

	public RecordVO getVOForType(FacetType type) {
		Facet facet;
		switch (type) {
			case FIELD:
				facet = schemasRecords.newFacetField();
				break;
			case QUERY:
				facet = schemasRecords.newFacetQuery();
				break;
			default:
				throw new ImpossibleRuntimeException("Unknown type");
		}

		return getVOForRecord(facet.getWrappedRecord());
	}

	public List<Integer> getInvalidQuery(Map<Integer, Map<String, String>> values) {
		List<Integer> invalids = new ArrayList<>();

		for (Entry<Integer, Map<String, String>> entry : values.entrySet()) {
			Map<String, String> queries = entry.getValue();
			for (Entry<String, String> query : queries.entrySet()) {
				if (invalidSolrQuery(query.getValue())) {
					invalids.add(entry.getKey());
				}
			}
		}

		return invalids;
	}

	private boolean invalidSolrQuery(String query) {
		RecordDao recordDao = modelLayerFactory().getDataLayerFactory().newRecordDao();
		try {
			ModifiableSolrParams solrParams = new ModifiableSolrParams();
			solrParams.set("q", "*:*");
			solrParams.set("fq", query);
			recordDao.searchQuery(solrParams);
		} catch (RuntimeException se) {
			return true;
		}

		return false;
	}

	public RecordVO getVOForRecord(Record record) {
		return new RecordToVOBuilder().build(record, VIEW_MODE.FORM, sessionContext);
	}

	public List<AvailableFacetFieldMetadata> getAvailableDataStoreCodes() {
		List<String> codes = new ArrayList<>();
		List<AvailableFacetFieldMetadata> availableFacetFieldMetadatas = new ArrayList<>();
		for (MetadataSchemaType type : schemasDisplayManager.getAllowedSchemaTypesForSimpleSearch(collection)) {
			for (Metadata metadata : type.getAllMetadatas().onlyWithType(BOOLEAN, ENUM, INTEGER, REFERENCE, STRING, NUMBER)) {
				String localCode = metadata.getLocalCode();
				if (!codes.contains(metadata.getDataStoreCode()) && (!Schemas.isGlobalMetadata(localCode)
																	 || ALLOWED_GLOBAL_METADATAS_FOR_FIELD_FACETS.contains(localCode))) {
					String label = getFieldFacetLabel(metadata);
					codes.add(metadata.getDataStoreCode());
					availableFacetFieldMetadatas.add(new AvailableFacetFieldMetadata(metadata.getDataStoreCode(), label));
				}
			}
		}
		Collections.sort(availableFacetFieldMetadatas, new Comparator<AvailableFacetFieldMetadata>() {
			@Override
			public int compare(AvailableFacetFieldMetadata o1, AvailableFacetFieldMetadata o2) {
				return LangUtils.compareStrings(o1.getLabel(), o2.getLabel());
			}
		});
		return availableFacetFieldMetadatas;
	}

	private String getFieldFacetLabel(Metadata metadata) {
		String label = $("init.allTypes.allSchemas." + metadata.getLocalCode());

		if (label.startsWith("init.")) {
			label = metadata.getLabel(Language.withCode(sessionContext.getCurrentLocale().getLanguage()));
		}

		return label;
	}

	public List<String> getFieldFacetValues(String datastoreCode) {
		List<MetadataSchemaType> types = schemasDisplayManager.getAllowedSchemaTypesForSimpleSearch(collection);
		for (MetadataSchemaType schemaType : types) {
			for (Metadata metadata : schemaType.getAllMetadatas().onlyWithType(BOOLEAN)) {
				if (metadata.getDataStoreCode().equals(datastoreCode)) {
					return asList("__TRUE__", "__FALSE__");
				}
			}
		}

		LogicalSearchQuery query = new LogicalSearchQuery(from(types).returnAll());
		query.setNumberOfRows(0);
		query.addFieldFacet(datastoreCode);
		query.setFieldFacetLimit(1000);

		SPEQueryResponse response = searchServices().query(query);

		List<String> values = new ArrayList<>();
		for (FacetValue facetValue : response.getFieldFacetValues().get(datastoreCode)) {
			values.add(facetValue.getValue());
		}
		values.remove("__NULL__");
		return values;
	}

	public boolean isDataStoreCodeSupportingLabelValues(String datastoreCode) {
		for (MetadataSchemaType type : schemasDisplayManager.getAllowedSchemaTypesForSimpleSearch(collection)) {
			for (Metadata metadata : type.getAllMetadatas().onlyWithType(BOOLEAN, INTEGER, STRING, NUMBER)) {
				if (metadata.getDataStoreCode().equals(datastoreCode) &&
					!FIELD_WITH_AUTOMATIC_LABELS.contains(metadata.getDataStoreCode())) {
					return true;
				}
			}
		}

		return false;
	}

	public int getNextOrderValue() {
		SchemasRecordsServices schemasRecords = new SchemasRecordsServices(collection, modelLayerFactory());
		LogicalSearchQuery query = new LogicalSearchQuery();
		query.setCondition(from(schemasRecords.facetSchemaType()).returnAll())
				.sortDesc(schemasRecords.defaultFacet().get(Facet.ORDER));

		List<Record> records = searchServices().search(query);
		return schemasRecords.wrapFacet(records.get(records.size() - 1)).getOrder() + 1;
	}

	public Record toRecord(RecordVO recordVO) throws OptimisticLockException {
		SchemaPresenterUtils schemaPresenterUtils = new SchemaPresenterUtils(recordVO.getSchema().getCode(),
				getConstellioFactories(), sessionContext);
		return schemaPresenterUtils.toRecord(recordVO);
	}

	public void activate(String id) {
		toggleActivateDeactivateFacetMetadata(id, true);
	}

	public void deactivate(String id) {
		toggleActivateDeactivateFacetMetadata(id, false);
	}

	private Metadata getFacetMetadata(Record record, String metadataCode) {
		return modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection).getSchema(
				record.getSchemaCode()).getMetadata(metadataCode);
	}

	private void toggleActivateDeactivateFacetMetadata(String id, boolean isActivate) {
		Record record = recordServices().getDocumentById(id);
		Metadata metadata = getFacetMetadata(record, Facet.ACTIVE);
		record.set(metadata, isActivate);
		try {
			recordServices().update(record);
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}
	}

	public boolean isActive(RecordVO recordVO) {
		return recordVO.getMetadataValue(recordVO.getMetadata(Facet.ACTIVE)).getValue() != null ?
			   (Boolean) recordVO.getMetadataValue(recordVO.getMetadata(Facet.ACTIVE)).getValue() :
			   false;
	}
}

package com.constellio.app.ui.pages.management.facet;

import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.Facet;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.records.wrappers.structure.FacetType;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.structures.MapStringStringStructure;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.ongoing.OngoingLogicalSearchConditionWithDataStoreFields;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class AddEditFacetConfigurationPresenter extends BasePresenter<AddEditFacetConfigurationView> {

	private RecordVO recordVO;
	private FacetType facetType;
	private String dataFieldCode;
	private FacetConfigurationPresenterService service;
	private boolean edit;
	private Map<Integer, Map<String, String>> values;

	public AddEditFacetConfigurationPresenter(AddEditFacetConfigurationView view) {
		super(view);
		init();
	}

	public void forParams(String recordId, boolean edit) {
		if (recordId != null && !recordId.isEmpty() && edit) {
			this.recordVO = service.getVOForRecord(recordServices().getDocumentById(recordId));
			this.facetType = recordVO.get(Facet.FACET_TYPE);
			this.dataFieldCode = recordVO.get(Facet.FIELD_DATA_STORE_CODE);
		}
		this.edit = edit;
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		init();
	}

	private void init() {
		service = new FacetConfigurationPresenterService(view.getConstellioFactories(), view.getSessionContext());
		recordVO = service.getVOForType(FacetType.FIELD);
		facetType = FacetType.FIELD;
		values = new HashMap<>();
	}

	public void setFacetType(FacetType type) {
		this.facetType = type;
		if (!edit) {
			recordVO = service.getVOForType(type);
		}
	}

	public FacetType getFacetType() {
		return facetType;
	}

	public void setDataFieldCode(String dataFieldCode) {
		this.dataFieldCode = dataFieldCode;
	}

	public String getDataFieldCode() {
		return dataFieldCode;
	}

	void reloadForm() {

		if (!edit) {
			RecordVO newRecordVO = service.getVOForType(facetType);
			view.getForm().commit();
			for (MetadataVO metadataVO : recordVO.getMetadatas()) {
				if (recordVO.getMetadataValue(metadataVO) != null
					&& recordVO.getMetadataValue(metadataVO).getValue() != null) {
					newRecordVO.set(metadataVO, recordVO.getMetadataValue(metadataVO).getValue());
				}
			}
			recordVO = newRecordVO;
			view.getForm().reload();
		}
	}

	public void reloadForm(String dataFieldCode) {
		this.dataFieldCode = dataFieldCode;
		reloadForm();
	}

	public String getTypePostfix() {
		return facetType.getCode();
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return user.has(CorePermissions.MANAGE_FACETS).globally();
	}

	public Map<Integer, Map<String, String>> getValues() {
		return values;
	}

	public Map<String, String> getValueForFacet() {
		Map<String, String> values = new HashMap<>();

		MapStringStringStructure valuesMap;
		switch (facetType) {
			case FIELD:
				valuesMap = recordVO.get(Facet.FIELD_VALUES_LABEL);
				break;
			case QUERY:
				valuesMap = recordVO.get(Facet.LIST_QUERIES);
				break;
			default:
				throw new ImpossibleRuntimeException("Unknown type");
		}
		if (valuesMap != null) {
			for (String key : valuesMap.keySet()) {
				Map<String, String> tmp = new HashMap<>();
				tmp.put(valuesMap.get(key), key);
				values.put(key, valuesMap.get(key));
			}
		}
		return values;
	}

	public Map<String, String> getAutomaticValueForFacet(String dataFieldCode, List<String> valuesInTable) {
		Map<String, String> valuesMap = new HashMap<>();

		List<String> values = new ArrayList<>();
		switch (facetType) {
			case FIELD:
				values = service.getFieldFacetValues(dataFieldCode);
				break;
			case QUERY:
				break;
			default:
				throw new ImpossibleRuntimeException("Unknown type");
		}
		for (String value : values) {
			if (!valuesInTable.contains(value)) {
				valuesMap.put(value, "");
			}
		}
		return valuesMap;
	}

	public void removeValue(int index) {
		values.remove(index);
	}

	public void addValue(String label, String value, int id) {
		Map<String, String> map = new HashMap<>();
		map.put(label, value);
		values.put(id, map);
	}

	public RecordVODataProvider getDataProvider() {
		RecordVODataProvider dataProvider;
		final MetadataSchema defaultSchema = schema(Facet.DEFAULT_SCHEMA);
		MetadataSchemaVO facetDefaultVO = new MetadataSchemaToVOBuilder()
				.build(defaultSchema, VIEW_MODE.FORM, view.getSessionContext());

		dataProvider = new RecordVODataProvider(facetDefaultVO, new RecordToVOBuilder(), modelLayerFactory,
				view.getSessionContext()) {
			@Override
			public LogicalSearchQuery getQuery() {
				LogicalSearchQuery query = new LogicalSearchQuery();
				OngoingLogicalSearchConditionWithDataStoreFields condition = from(schema(Facet.FIELD_SCHEMA))
						.where(defaultSchema.get(Facet.FACET_TYPE));
				switch (facetType) {
					case FIELD:
						query.setCondition(condition.isEqualTo(FacetType.FIELD));
						break;
					case QUERY:
						query.setCondition(condition.isEqualTo(FacetType.QUERY));
						break;
					default:
						throw new ImpossibleRuntimeException("Unknown type");
				}

				return query;
			}
		};

		return dataProvider;
	}

	public RecordVO getRecordVO() {
		return recordVO;
	}

	public void saveButtonClicked(RecordVO recordVO) {

		filterValues(values);
		switch (facetType) {
			case FIELD:
				if (recordVO.get(Facet.FIELD_DATA_STORE_CODE) == null) {
					throw new RuntimeException("no data field");
				}
				recordVO.set(Facet.FIELD_VALUES_LABEL, service.getStructureValue(values));
				break;
			case QUERY:
				List<Integer> invalidQuery = service.getInvalidQuery(values);
				if (invalidQuery.isEmpty()) {
					recordVO.set(Facet.LIST_QUERIES, service.getStructureValue(values));
				} else {
					view.displayInvalidQuery(invalidQuery);
					return;
				}
				break;
			default:
				throw new ImpossibleRuntimeException("Unknown type");
		}

		if (!edit) {
			recordVO.set(Facet.ORDER, service.getNextOrderValue());
		}

		Record recordToSave = service.toRecord(recordVO);
		Transaction transaction = new Transaction();
		transaction.addUpdate(recordToSave);

		try {
			recordServices().execute(transaction);
			this.recordVO = service.getVOForRecord(recordToSave);
			view.navigate().to().listFacetConfiguration();
		} catch (RecordServicesException ex) {
			throw new RuntimeException();
		}
	}

	private void filterValues(Map<Integer, Map<String, String>> values) {
		List<Integer> valuesToRemove = new ArrayList<>();
		for (Integer integer : values.keySet()) {
			for (Entry<String, String> entry : values.get(integer).entrySet()) {
				if (StringUtils.isBlank(entry.getKey()) || StringUtils.isBlank(entry.getValue())) {
					valuesToRemove.add(integer);
				}
			}
		}
		for (Integer integer : valuesToRemove) {
			values.remove(integer);
		}
	}

	public List<AvailableFacetFieldMetadata> getAvailableDataStoreCodes() {
		return service.getAvailableDataStoreCodes();
	}

	public boolean isDataStoreCodeNeeded() {
		return !edit && facetType == FacetType.FIELD;
	}

	public boolean isDataStoreCodeSupportingLabelValues(String datastoreCode) {
		return service.isDataStoreCodeSupportingLabelValues(datastoreCode);
	}

	public void cancelButtonClicked() {
		view.navigate().to().listFacetConfiguration();
	}

	public static class AvailableFacetFieldMetadata {

		private String code;
		private String label;

		public AvailableFacetFieldMetadata(String code, String label) {
			this.code = code;
			this.label = label;
		}

		public String getCode() {
			return code;
		}

		public String getLabel() {
			return label;
		}
	}
}

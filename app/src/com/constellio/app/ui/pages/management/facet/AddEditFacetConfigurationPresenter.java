/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.ui.pages.management.facet;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.constellio.app.ui.entities.MetadataSchemaVO;
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

public class AddEditFacetConfigurationPresenter extends BasePresenter<AddEditFacetConfigurationView> {

	private RecordVO recordVO;
	private FacetType facetType;
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

	public void typeChanged(String dataFieldCode) {
		switch (facetType) {
		case FIELD:
			displayCorrectTab(dataFieldCode);
			break;
		case QUERY:
			view.refreshValuesTab();
			break;
		default:
			throw new ImpossibleRuntimeException("Unknown type");
		}
		view.refreshProfileTab();
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

	public List<Map<String, String>> getValueForFacet(String dataFieldCode) {
		List<Map<String, String>> values = new ArrayList<>();

		MapStringStringStructure valuesMap;
		List<String> labels = new ArrayList<>();
		switch (facetType) {
		case FIELD:
			valuesMap = recordVO.get(Facet.FIELD_VALUES_LABEL);
			labels = service.getFieldFacetValues(dataFieldCode);
			break;
		case QUERY:
			valuesMap = recordVO.get(Facet.LIST_QUERIES);
			break;
		default:
			throw new ImpossibleRuntimeException("Unknown type");
		}

		if (valuesMap == null || valuesMap.isEmpty()) {
			for (String label : labels) {
				Map<String, String> value = new HashMap<>();
				value.put("", label);
				values.add(value);
			}
		} else {
			for (String key : valuesMap.keySet()) {
				Map<String, String> tmp = new HashMap<>();
				tmp.put(valuesMap.get(key), key);
				values.add(tmp);
			}
		}

		return values;
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
			protected LogicalSearchQuery getQuery() {
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

		switch (facetType) {
		case FIELD:
			if (recordVO.get(Facet.FIELD_DATA_STORE_CODE) == null) {
				throw new RuntimeException("no data field");
			}
			recordVO.set(Facet.FIELD_VALUES_LABEL, service.getStructureValue(values));
			break;
		case QUERY:
			List<Integer> invalidQuery = service.getInvalidQuery(values);
			if (invalidQuery.isEmpty())
				recordVO.set(Facet.LIST_QUERIES, service.getStructureValue(values));
			else {
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
			view.navigateTo().listFacetConfiguration();
		} catch (RecordServicesException ex) {
			throw new RuntimeException();
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

	public void displayCorrectTab(String dataFieldCode) {
		if (dataFieldCode != null && !dataFieldCode.isEmpty()) {
			if (isDataStoreCodeSupportingLabelValues(dataFieldCode)) {
				view.refreshValuesTab();
			} else {
				view.removeValuesTab();
			}
		} else {
			view.removeValuesTab();
		}
	}

	public void cancelButtonClicked() {
		view.navigateTo().listFacetConfiguration();
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

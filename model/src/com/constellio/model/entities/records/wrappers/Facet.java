package com.constellio.model.entities.records.wrappers;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.structure.FacetOrderType;
import com.constellio.model.entities.records.wrappers.structure.FacetType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.structures.MapStringStringStructure;

public class Facet extends RecordWrapper {
	public static final String SCHEMA_TYPE = "facet";
	public static final String DEFAULT_SCHEMA = SCHEMA_TYPE + "_default";
	public static final String FACET_TYPE = "facetType";
	public static final String FIELD_DATA_STORE_CODE = "fieldDatastoreCode";
	public static final String FIELD_VALUES_LABEL = "fieldValuesLabel";
	public static final String LIST_QUERIES = "listQueries";
	public static final String ELEMENT_PER_PAGE = "elementPerPage";
	public static final String PAGES = "pages";
	public static final String ORDER_RESULT = "orderResult";
	public static final String ORDER = "order";
	public static final String QUERY_SCHEMA = SCHEMA_TYPE + "_query";
	public static final String QUERY_LOCAL_CODE = "query";
	public static final String FIELD_SCHEMA = SCHEMA_TYPE + "_field";
	public static final String FIELD_LOCAL_CODE = "field";
	public static final String ACTIVE = "active";
	public static final String OPEN_BY_DEFAULT = "openByDefault";
	public static final String USED_BY_MODULE = "usedByModule";

	public Facet(Record record, MetadataSchemaTypes types) {
		super(record, types, SCHEMA_TYPE);
	}

	public FacetType getFacetType() {
		return get(FACET_TYPE);
	}

	public String getFieldDataStoreCode() {
		return get(FIELD_DATA_STORE_CODE);
	}

	public MapStringStringStructure getFieldValuesLabel() {
		return get(FIELD_VALUES_LABEL);
	}

	public String getFieldValueLabel(String value) {
		MapStringStringStructure structure = get(FIELD_VALUES_LABEL);
		if (structure == null) {
			return null;
		} else {
			return structure.get(value);
		}
	}

	public MapStringStringStructure getListQueries() {
		MapStringStringStructure queries = get(LIST_QUERIES);
		if (queries == null) {
			queries = new MapStringStringStructure();
			setListQueries(queries);
		}
		return queries;
	}

	public int getElementPerPage() {
		Double tmp = get(ELEMENT_PER_PAGE);
		return tmp.intValue();
	}

	public int getPages() {
		Double tmp = get(PAGES);
		return tmp.intValue();
	}

	public FacetOrderType getOrderResult() {
		return get(ORDER_RESULT);
	}

	public int getOrder() {
		Integer order = getInteger(ORDER);
		return order == null ? 999 : order;
	}

	public Facet setTitle(String title) {
		set(TITLE, title);
		return this;
	}

	public Facet setFacetType(FacetType type) {
		set(FACET_TYPE, type);
		return this;
	}

	public Facet setFieldDataStoreCode(String field) {
		set(FIELD_DATA_STORE_CODE, field);
		return this;
	}

	public Facet setFieldValuesLabel(MapStringStringStructure listValue) {
		set(FIELD_VALUES_LABEL, listValue);
		return this;
	}

	public Facet setListQueries(MapStringStringStructure listQueries) {
		set(LIST_QUERIES, listQueries);
		return this;
	}

	public Facet withQuery(String query, String label) {
		getListQueries().put(query, label);
		return this;
	}

	public Facet setElementPerPage(int elementPerPage) {
		set(ELEMENT_PER_PAGE, elementPerPage);
		return this;
	}

	public Facet setPages(int pages) {
		set(PAGES, pages);
		return this;
	}

	public Facet setOrderResult(FacetOrderType type) {
		set(ORDER_RESULT, type);
		return this;
	}

	public Facet setOrder(int order) {
		set(ORDER, order);
		return this;
	}

	public String getQueryLabel(String queryFacet) {
		return getListQueries().get(queryFacet);
	}

	public Facet withLabel(String value, String label) {
		if (getFieldValuesLabel() == null) {
			setFieldValuesLabel(new MapStringStringStructure());
		}
		getFieldValuesLabel().put(value, label);
		return this;
	}

	public Boolean isActive() {
		return getBooleanWithDefaultValue(ACTIVE, true);
	}

	public Facet setActive(Boolean active) {
		set(ACTIVE, active);
		return this;
	}

	public Boolean isOpenByDefault() {
		return get(OPEN_BY_DEFAULT);
	}

	public Facet setOpenByDefault(Boolean openByDefault) {
		set(OPEN_BY_DEFAULT, openByDefault);
		return this;
	}

	public String getUsedByModule() {
		return get(USED_BY_MODULE);
	}

	public Facet setUsedByModule(String usedByModule) {
		set(USED_BY_MODULE, usedByModule);
		return this;
	}
}


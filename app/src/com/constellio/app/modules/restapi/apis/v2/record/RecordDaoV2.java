package com.constellio.app.modules.restapi.apis.v2.record;

import com.constellio.app.modules.restapi.apis.v2.core.BaseDaoV2;
import com.constellio.app.modules.restapi.apis.v2.record.dto.FacetMode;
import com.constellio.app.modules.restapi.apis.v2.record.dto.FilterMode;
import com.constellio.app.modules.restapi.apis.v2.record.dto.QueryDtoV2;
import com.constellio.app.modules.restapi.apis.v2.record.dto.SortDtoV2;
import com.constellio.app.modules.restapi.core.util.ListUtils;
import com.constellio.app.modules.restapi.core.util.SetUtils;
import com.constellio.app.ui.framework.data.AutocompleteQuery;
import com.constellio.app.ui.util.FacetUtils;
import com.constellio.app.ui.util.SimpleSearchUtils;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Facet;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.search.SPEQueryResponse;
import com.constellio.model.services.search.StatusFilter;
import com.constellio.model.services.search.query.ReturnedMetadatasFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasIn;
import static org.apache.commons.lang.StringUtils.stripToNull;

public class RecordDaoV2 extends BaseDaoV2 {

	public List<Record> getAutocompleteSuggestions(User user, String schemaType, String expression,
												   FilterMode filterMode) {
		AutocompleteQuery query = AutocompleteQuery.builder()
				.appLayerFactory(appLayerFactory)
				.schemaTypeCode(schemaType)
				.hasSecurity(true)
				.expression(expression)
				.user(user)
				.writeAccessRequired(true)
				.startRow(0)
				.rowCount(systemConfigurationsManager.getValue(ConstellioEIMConfigs.AUTOCOMPLETE_SIZE))
				.metadataFilter(filterMode == FilterMode.SUMMARY ?
								ReturnedMetadatasFilter.onlySummaryFields() : ReturnedMetadatasFilter.all())
				.build();

		return searchServices.search(query.searchQuery());
	}

	public SPEQueryResponse search(User user, QueryDtoV2 queryDto, FilterMode filterMode) {
		String collection = queryDto.getCollection();

		int rowsStart = queryDto.getRowsStart() != null ? queryDto.getRowsStart() : 0;
		int rowsLimit = queryDto.getRowsLimit() != null ? queryDto.getRowsLimit() : 100;

		LogicalSearchQuery query = new LogicalSearchQuery();
		query.setCondition(getCondition(user, queryDto));
		query.setStartRow(rowsStart);
		query.setNumberOfRows(rowsLimit);

		String expression = stripToNull(queryDto.getExpression());
		if (expression != null) {
			query.setFreeTextQuery(expression);
		}

		if (queryDto.getFacetMode() == FacetMode.CONSTELLIO) {
			SchemasRecordsServices schemas = new SchemasRecordsServices(collection, modelLayerFactory);
			query.addFieldFacets(FacetUtils.getActiveFacets(schemas, searchServices).stream()
					.map(Facet::getFieldDataStoreCode).collect(Collectors.toList()));
		} else if (queryDto.getFacetMode() == FacetMode.SPECIFIC) {
			for (String facetValueId : queryDto.getFacetValueIds()) {
				String[] parts = facetValueId.split(":");
				query.getFacetFilters().selectedFieldFacetValue(parts[0], parts[1]);
			}
		}

		for (SortDtoV2 sort : ListUtils.nullToEmpty(queryDto.getSorting())) {
			if (sort.isAscending()) {
				query.sortAsc(getGlobalMetadata(sort.getMetadata()));
			} else {
				query.sortDesc(getGlobalMetadata(sort.getMetadata()));
			}
		}
		query.filteredWithUserRead(user);
		query.filteredByStatus(StatusFilter.ACTIVES);
		if (queryDto.isRequireWriteAccess()) {
			query.filteredWithUserWrite(user);
		}
		if (filterMode == FilterMode.SUMMARY) {
			query.setReturnedMetadatas(ReturnedMetadatasFilter.onlySummaryFields());
		}

		return searchServices.query(query);
	}

	public Facet getFacet(String fieldDatastoreCode, String collection) {
		SchemasRecordsServices schemas = new SchemasRecordsServices(collection, modelLayerFactory);
		return FacetUtils.getActiveFacets(schemas, searchServices).stream()
				.filter(facet -> facet.getFieldDataStoreCode().equals(fieldDatastoreCode)).findAny()
				.orElseThrow(() -> new RuntimeException("No facet with field datastore code : " + fieldDatastoreCode));
	}

	public String getFacetValueLabel(Facet facet, String value, Locale locale) {
		return FacetUtils.getFacetValueLabel(facet, value, locale, metadataSchemasManager, recordServices);
	}

	private LogicalSearchCondition getCondition(User user, QueryDtoV2 queryDto) {
		String collection = queryDto.getCollection();
		Set<String> schemaTypes = SetUtils.nullToEmpty(queryDto.getSchemaTypes());
		if (schemaTypes.isEmpty() && queryDto.getExpression() != null) {
			MetadataSchemaTypes metadataSchemaTypes = metadataSchemasManager.getSchemaTypes(collection);
			List<MetadataSchemaType> allowedSchemaTypes = SimpleSearchUtils.allowedSchemaTypes(user, collection,
					metadataSchemaTypes, schemasDisplayManager);
			if (!allowedSchemaTypes.isEmpty()) {
				return from(allowedSchemaTypes).returnAll();
			}
		}

		return !schemaTypes.isEmpty() ?
			   from(new ArrayList<>(schemaTypes), collection).returnAll() :
			   fromAllSchemasIn(collection).returnAll();
	}
}

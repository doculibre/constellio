package com.constellio.app.modules.rm.services.mediumType;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.type.MediumType;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.dao.dto.records.FacetValue;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.ReturnedMetadatasFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.google.common.collect.ImmutableMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.ALL;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class MediumTypeService {

	private RMSchemasRecordsServices rm;
	private RecordServices recordServices;
	private SearchServices searchServices;

	private MediumType digitalMediumType;

	public MediumTypeService(String collection, AppLayerFactory appLayerFactory) {
		rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();
		recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();

		LogicalSearchQuery query = new LogicalSearchQuery(from(rm.mediumTypeSchemaType()).where(ALL));
		for (MediumType mediumType :
				rm.wrapMediumTypes(appLayerFactory.getModelLayerFactory().newSearchServices().search(query))) {
			if (!mediumType.isAnalogical()) {
				digitalMediumType = mediumType;
				break;
			}
		}
	}

	public List<String> getHierarchicalMediumTypes(String recordId) {
		return getHierarchicalMediumTypes(recordServices.getDocumentById(recordId));
	}

	public List<String> getHierarchicalMediumTypes(Record record) {
		LogicalSearchQuery query = new LogicalSearchQuery(from(rm.folderSchemaType(), rm.documentSchemaType())
				.where(Schemas.PATH).isStartingWithText(record.getList(Schemas.PATH).get(0) + "/")
				.andWhere(Schemas.LOGICALLY_DELETED_STATUS).isFalseOrNull())
				.addFieldFacet(rm.folder.mediumTypes().getDataStoreCode())
				.addFieldFacet(rm.document.content().getDataStoreCode())
				.setReturnedMetadatas(ReturnedMetadatasFilter.onlyMetadatas(rm.folder.mediumTypes(), rm.document.content()))
				.setNumberOfRows(0)
				.setOverridedQueryParams(ImmutableMap.of("facet.minCount", new String[]{"1"}));

		Map<String, List<FacetValue>> facetValues = searchServices.query(query).getFieldFacetValues();
		return new ArrayList<>(getMediumTypesFromFacetValues(facetValues));
	}

	public MediumType getDigitalMediumType() {
		return digitalMediumType;
	}

	private Collection<String> getMediumTypesFromFacetValues(Map<String, List<FacetValue>> facetValues) {
		Set<String> mediumTypes = new HashSet<>();

		for (String facet : facetValues.keySet()) {
			if (facet.equals(rm.document.content().getDataStoreCode()) && facetValues.get(facet).size() > 0) {
				mediumTypes.add(digitalMediumType.getId());
			} else if (facet.equals(rm.folder.mediumTypes().getDataStoreCode())) {
				for (FacetValue value : facetValues.get(facet)) {
					if (value.getQuantity() > 0) {
						mediumTypes.add(value.getValue());
					}
				}
			}
		}
		return mediumTypes;
	}

}

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
package com.constellio.app.ui.pages.search;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.lang.Math.min;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.entities.FacetVO;
import com.constellio.app.ui.entities.FacetValueVO;
import com.constellio.data.dao.dto.records.FacetValue;
import com.constellio.data.utils.Factory;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Facet;
import com.constellio.model.entities.records.wrappers.structure.FacetOrderType;
import com.constellio.model.entities.records.wrappers.structure.FacetType;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.search.SPEQueryResponse;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.utils.EnumWithSmallCodeUtils;

public class SearchPresenterService {

	private static int MAX_FACETS_COUNT_INCLUDING_SCHEMAS = 7;

	private String collection;

	private SchemasRecordsServices schemas;
	private AppLayerFactory appLayerFactory;
	private SearchServices searchServices;
	private RecordServices recordServices;
	private Factory<MetadataSchemaTypes> types;

	public SearchPresenterService(final String collection, AppLayerFactory appLayerFactory) {
		this.appLayerFactory = appLayerFactory;
		final ModelLayerFactory modelLayerFactory = appLayerFactory.getModelLayerFactory();
		this.searchServices = modelLayerFactory.newSearchServices();
		this.recordServices = modelLayerFactory.newRecordServices();
		this.schemas = new SchemasRecordsServices(collection, appLayerFactory.getModelLayerFactory());
		this.collection = collection;
		types = new Factory<MetadataSchemaTypes>() {
			@Override
			public MetadataSchemaTypes get() {
				return modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection);
			}
		};
	}

	List<FacetVO> getFacets(LogicalSearchQuery query) {
		LogicalSearchQuery facetQuery = prepareFacetQuery(query);

		SPEQueryResponse response = searchServices.query(facetQuery);

		return buildFacetVOs(query, response);
	}

	private List<FacetVO> buildFacetVOs(LogicalSearchQuery query, SPEQueryResponse response) {
		List<FacetVO> facetVOs = new ArrayList<>();
		for (Facet facet : getAvailableFacets()) {
			List<FacetValueVO> facetValueVOs = new ArrayList<>();
			FacetVO facetVO = new FacetVO(facet.getId(), facet.getFieldDataStoreCode(), facet.getTitle(),
					facet.getFacetType(), facetValueVOs);
			if (facet.getFacetType() == FacetType.FIELD) {
				buildFieldFacet(query, response, facet, facetValueVOs);
			} else {
				buildQueryFacet(query, response, facet, facetValueVOs);
			}

			if (!facetVO.getValues().isEmpty()) {
				if (facet.getOrderResult() == FacetOrderType.ALPHABETICAL) {
					Collections.sort(facetVO.getValues(), new ComparatorByLabel());
				} else {
					Collections.sort(facetVO.getValues(), new ComparatorByQuantity());
				}
				int maximum = facet.getElementPerPage() * facet.getPages();
				facetVO.setValues(facetVO.getValues().subList(0, min(facetVO.getValues().size(), maximum)));
				facetVOs.add(facetVO);
			}
		}

		return facetVOs.subList(0, min(facetVOs.size(), MAX_FACETS_COUNT_INCLUDING_SCHEMAS));

	}

	private void buildQueryFacet(LogicalSearchQuery query, SPEQueryResponse response, Facet facet,
			List<FacetValueVO> facetValueVOs) {
		Set<String> selectedQueryFacets = query.getFacetFilters().getSelectedQueryFacets(facet.getId());
		for (Entry<String, String> queryFacet : facet.getListQueries().entrySet()) {
			FacetValue facetValue = response.getQueryFacetValue(queryFacet.getKey());
			boolean quantityIsFilteringResults = facetValue.getQuantity() != response.getNumFound()
					&& facetValue.getQuantity() != 0;
			boolean selectedFacetValue = selectedQueryFacets.contains(facetValue.getValue());
			if (selectedFacetValue || quantityIsFilteringResults) {
				facetValueVOs.add(new FacetValueVO(facet.getId(), facetValue, queryFacet.getValue()));
			}
		}
	}

	private void buildFieldFacet(LogicalSearchQuery query, SPEQueryResponse response, Facet facet,
			List<FacetValueVO> facetValueVOs) {
		Set<String> selectedValues = query.getFacetFilters().getSelectedFieldValues(facet.getFieldDataStoreCode());
		for (FacetValue facetValue : response.getFieldFacetValues(facet.getFieldDataStoreCode())) {
			boolean quantityIsFilteringResults = facetValue.getQuantity() != response.getNumFound()
					&& facetValue.getQuantity() != 0;
			boolean nullValue = !"__NULL__".equals(facetValue.getValue());
			boolean selectedFacetValue = selectedValues.contains(facetValue.getValue());
			if (selectedFacetValue || (quantityIsFilteringResults && nullValue)) {
				facetValueVOs.add(new FacetValueVO(facet.getId(), facetValue));
			}
		}

		setFieldFacetValuesLabels(facet, facetValueVOs);
	}

	private LogicalSearchQuery prepareFacetQuery(LogicalSearchQuery query) {
		LogicalSearchQuery facetQuery = new LogicalSearchQuery(query);
		facetQuery.setNumberOfRows(0);

		for (Facet facet : getAvailableFacets()) {
			if (facet.getFacetType() == FacetType.FIELD) {
				facetQuery.addFieldFacet(facet.getFieldDataStoreCode());
			} else {
				for (Entry<String, String> entry : facet.getListQueries().entrySet()) {
					facetQuery.addQueryFacet(facet.getId(), entry.getKey());
				}
			}
		}
		return facetQuery;
	}

	private static class ComparatorByQuantity implements Comparator<FacetValueVO> {

		@Override
		public int compare(FacetValueVO o1, FacetValueVO o2) {
			Integer count1 = o1.getCount();
			Integer count2 = o2.getCount();
			int countCompare = -1 * count1.compareTo(count2);
			if (countCompare == 0) {
				String label1 = o1.getLabel();
				String label2 = o2.getLabel();
				return label1.compareTo(label2);
			} else {
				return countCompare;
			}
		}
	}

	private static class ComparatorByLabel implements Comparator<FacetValueVO> {

		@Override
		public int compare(FacetValueVO o1, FacetValueVO o2) {
			String label1 = o1.getLabel();
			String label2 = o2.getLabel();
			return label1.compareTo(label2);
		}
	}

	private void setFieldFacetValuesLabels(Facet facet, List<FacetValueVO> facetValueVOs) {

		Map<String, String> enumMetadatas = findEnumMetadatasLabels(facet.getFieldDataStoreCode());

		String datastoreCode = facet.getFieldDataStoreCode();

		for (FacetValueVO facetValueVO : facetValueVOs) {
			String value = facetValueVO.getValue();

			if (datastoreCode.equals(Schemas.SCHEMA.getDataStoreCode())) {
				facetValueVO.setLabel(types.get().getSchema(value).getLabel());

			} else if (datastoreCode.endsWith("Id_s") || datastoreCode.endsWith("Id_ss")) {
				Record record = recordServices.getDocumentById(value);
				facetValueVO.setLabel(record.<String>get(Schemas.TITLE));

			} else if (enumMetadatas.containsKey(value)) {
				facetValueVO.setLabel(enumMetadatas.get(value));

			} else {
				facetValueVO.setLabel(facet.getFieldValueLabel(facetValueVO.getValue()));
			}

			if (facetValueVO.getLabel() == null) {
				facetValueVO.setLabel(facetValueVO.getValue());
			}

		}
	}

	private Map<String, String> findEnumMetadatasLabels(String fieldDataStoreCode) {
		Map<String, String> enumMetadatasLabels = new HashMap<>();

		for (Metadata metadata : schemas.getTypes().getAllMetadatas().onlyWithType(MetadataValueType.ENUM)) {
			if (metadata.getDataStoreCode().equals(fieldDataStoreCode)) {
				for (String code : EnumWithSmallCodeUtils.toSmallCodeList(metadata.getEnumClass())) {
					String label = $(metadata.getEnumClass().getSimpleName() + "." + code);
					enumMetadatasLabels.put(code, label);
				}
			}
		}

		return enumMetadatasLabels;
	}

	List<Facet> getAvailableFacets() {
		LogicalSearchQuery findAllFacetsQuery = new LogicalSearchQuery(from(schemas.facetSchemaType()).returnAll());
		findAllFacetsQuery.sortAsc(schemas.defaultFacet().getMetadata(Facet.ORDER));
		return schemas.wrapFacets(searchServices.search(findAllFacetsQuery));
	}
}

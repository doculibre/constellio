package com.constellio.app.ui.pages.search;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;

import com.constellio.app.ui.entities.FacetVO;
import com.constellio.app.ui.entities.FacetValueVO;
import com.constellio.data.dao.dto.records.FacetValue;
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
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SPEQueryResponse;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.utils.EnumWithSmallCodeUtils;

public class SearchPresenterService {
	private SchemasRecordsServices schemas;
	private SearchServices searchServices;
	private RecordServices recordServices;
	private MetadataSchemasManager metadataSchemasManager;

	public SearchPresenterService(final String collection, ModelLayerFactory modelLayerFactory) {
		this.searchServices = modelLayerFactory.newSearchServices();
		this.recordServices = modelLayerFactory.newRecordServices();
		this.schemas = new SchemasRecordsServices(collection, modelLayerFactory);
		this.metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
	}

	List<FacetVO> getFacets(LogicalSearchQuery query, Map<String, Boolean> facetStatus) {
		LogicalSearchQuery facetQuery = prepareFacetQuery(query);
		return buildFacetVOs(searchServices.query(facetQuery), facetStatus);
	}

	private List<FacetVO> buildFacetVOs(SPEQueryResponse response, Map<String, Boolean> facetStatus) {
		List<FacetVO> result = new ArrayList<>();

		for (Facet facet : getActiveFacets()) {
			List<FacetValueVO> values = new ArrayList<>();

			if (facet.getFacetType() == FacetType.FIELD) {
				buildFieldFacet(response, facet, values);
			} else {
				buildQueryFacet(response, facet, values);
			}

			if (!values.isEmpty()) {
				if (facet.getOrderResult() == FacetOrderType.ALPHABETICAL) {
					Collections.sort(values, new ComparatorByLabel());
				}
				boolean open = Boolean.TRUE.equals(facetStatus.containsKey(facet.getId())
						? facetStatus.get(facet.getId()) : facet.isOpenByDefault());
				result.add(new FacetVO(facet.getId(), facet.getTitle(), values, open, facet.getElementPerPage()));
			}
		}

		return result;

	}

	private void buildQueryFacet(SPEQueryResponse response, Facet facet, List<FacetValueVO> values) {
		for (Entry<String, String> queryFacet : facet.getListQueries().entrySet()) {
			FacetValue facetValue = response.getQueryFacetValue(queryFacet.getKey());
			if (facetValue.getQuantity() > 0) {
				values.add(new FacetValueVO(facet.getId(), facetValue, queryFacet.getValue()));
			}
		}
	}

	private void buildFieldFacet(SPEQueryResponse response, Facet facet, List<FacetValueVO> values) {
		for (FacetValue facetValue : response.getFieldFacetValues(facet.getFieldDataStoreCode())) {
			values.add(new FacetValueVO(facet.getId(), facetValue));
		}
		setFieldFacetValuesLabels(facet, values);
	}

	private LogicalSearchQuery prepareFacetQuery(LogicalSearchQuery query) {
		LogicalSearchQuery facetQuery = new LogicalSearchQuery(query).setNumberOfRows(0);

		for (Facet facet : getActiveFacets()) {
			if (facet.getFacetType() == FacetType.FIELD) {
				facetQuery.addFieldFacet(facet.getFieldDataStoreCode());
			} else {
				for (Entry<String, String> entry : facet.getListQueries().entrySet()) {
					facetQuery.addQueryFacet(facet.getId(), entry.getKey());
				}
			}
		}
		facetQuery.setFieldFacetLimit(100);
		return facetQuery;
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
		MetadataSchemaTypes types = metadataSchemasManager.getSchemaTypes(facet.getCollection());

		for (FacetValueVO facetValueVO : facetValueVOs) {
			String value = facetValueVO.getValue();

			if (datastoreCode.equals(Schemas.SCHEMA.getDataStoreCode())) {
				facetValueVO.setLabel(types.getSchema(value).getLabel());
			} else if (datastoreCode.endsWith("Id_s") || datastoreCode.endsWith("Id_ss")) {
				Record record = recordServices.getDocumentById(value);
				facetValueVO.setLabel(record.<String>get(Schemas.TITLE));
			} else if (enumMetadatas.containsKey(value)) {
				facetValueVO.setLabel(enumMetadatas.get(value));
			} else {
				facetValueVO.setLabel(facet.getFieldValueLabel(facetValueVO.getValue()));
			}

			if (StringUtils.isBlank(facetValueVO.getLabel())) {
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

	List<Facet> getActiveFacets() {
		LogicalSearchQuery query = new LogicalSearchQuery(from(schemas.facetSchemaType()).where(schemas.facetActive()).isTrue())
				.sortAsc(schemas.facetOrder());
		return schemas.wrapFacets(searchServices.search(query));
	}
}

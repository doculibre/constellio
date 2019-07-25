package com.constellio.app.ui.pages.search;

import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.ui.entities.FacetVO;
import com.constellio.app.ui.entities.FacetValueVO;
import com.constellio.app.ui.util.SchemaCaptionUtils;
import com.constellio.data.dao.dto.records.FacetValue;
import com.constellio.data.utils.comparators.AbstractTextComparator;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Facet;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.records.wrappers.structure.FacetOrderType;
import com.constellio.model.entities.records.wrappers.structure.FacetType;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
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
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.app.ui.i18n.i18n.getLocale;
import static com.constellio.model.entities.Language.withLocale;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class SearchPresenterService {
	private SchemasRecordsServices schemas;
	private SearchServices searchServices;
	private RecordServices recordServices;
	private MetadataSchemasManager metadataSchemasManager;
	private List<MetadataSchemaType> metadataSchemaTypesList;
	private String collection;
	private User user;

	public SearchPresenterService(final String collection, User user, ModelLayerFactory modelLayerFactory, List<MetadataSchemaType> metadataSchemaTypesList) {
		this.searchServices = modelLayerFactory.newSearchServices();
		this.recordServices = modelLayerFactory.newRecordServices();
		this.schemas = new SchemasRecordsServices(collection, modelLayerFactory);
		this.metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
		this.metadataSchemaTypesList = metadataSchemaTypesList;
		this.collection = collection;
		this.user = user;
	}

	public SearchPresenterService setMetadataSchemaTypesList(List<MetadataSchemaType> metadataSchemaTypesList) {
		this.metadataSchemaTypesList = metadataSchemaTypesList;
		return this;
	}

	public List<FacetVO> getFacets(LogicalSearchQuery query, Map<String, Boolean> facetStatus, Locale locale) {
		LogicalSearchQuery facetQuery = cloneQueryReturningOneWithFacetQuery(query);
		SPEQueryResponse response = searchServices.query(facetQuery);
		return buildFacetVOs(response.getFieldFacetValues(), response.getQueryFacetsValues(), facetStatus, locale);
	}

	public List<FacetVO> buildFacetVOs(Map<String, List<FacetValue>> fieldFacetValues,
									   Map<String, Integer> queryFacetsValues,
									   Map<String, Boolean> facetStatus, Locale locale) {
		List<FacetVO> result = new ArrayList<>();

		for (Facet facet : getActiveFacets()) {
			List<FacetValueVO> values = new ArrayList<>();

			if (facet.getFacetType() == FacetType.FIELD) {

				List<FacetValue> facetValues;
				if (fieldFacetValues.containsKey(facet.getFieldDataStoreCode())) {
					facetValues = fieldFacetValues.get(facet.getFieldDataStoreCode());
				} else {
					facetValues = Collections.emptyList();
				}
				buildFieldFacet(facetValues, facet, values, locale);
			} else {

				buildQueryFacet(queryFacetsValues, facet, values);
			}

			if (!values.isEmpty()) {
				if (facet.getOrderResult() == FacetOrderType.ALPHABETICAL) {
					Collections.sort(values, new ComparatorByLabel());
				}
				boolean open = Boolean.TRUE.equals(facetStatus.containsKey(facet.getId())
												   ? facetStatus.get(facet.getId()) : facet.isOpenByDefault());
				result.add(new FacetVO(facet.getId(), facet.getTitle(getLocale()), values, open, facet.getElementPerPage()));
			}
		}

		return result;

	}

	private void buildQueryFacet(Map<String, Integer> queryFacetsValues, Facet facet, List<FacetValueVO> values) {
		for (Entry<String, String> queryFacet : facet.getListQueries().entrySet()) {
			int count = !queryFacetsValues.containsKey(queryFacet.getKey()) ? 0 : queryFacetsValues.get(queryFacet.getKey());
			FacetValue facetValue = new FacetValue(queryFacet.getKey(), count);
			if (facetValue.getQuantity() > 0) {
				values.add(new FacetValueVO(facet.getId(), facetValue, queryFacet.getValue()));
			}
		}
	}

	private void buildFieldFacet(List<FacetValue> facetValues, Facet facet, List<FacetValueVO> values, Locale locale) {
		for (FacetValue facetValue : facetValues) {
			values.add(new FacetValueVO(facet.getId(), facetValue));
		}
		setFieldFacetValuesLabels(facet, values, locale);
	}

	private LogicalSearchQuery cloneQueryReturningOneWithFacetQuery(LogicalSearchQuery query) {
		LogicalSearchQuery facetQuery = new LogicalSearchQuery(query).setNumberOfRows(0);
		configureQueryToComputeFacets(facetQuery);
		return facetQuery;
	}

	public void configureQueryToComputeFacets(LogicalSearchQuery facetQuery) {

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
	}

	private static class ComparatorByLabel extends AbstractTextComparator<FacetValueVO> {

		@Override
		protected String getText(FacetValueVO object) {
			return object.getLabel();
		}
	}

	private void setFieldFacetValuesLabels(Facet facet, List<FacetValueVO> facetValueVOs, Locale locale) {
		Map<String, String> enumMetadatas = findEnumMetadatasLabels(facet.getFieldDataStoreCode());

		String datastoreCode = facet.getFieldDataStoreCode();
		MetadataSchemaTypes types = metadataSchemasManager.getSchemaTypes(facet.getCollection());

		for (FacetValueVO facetValueVO : facetValueVOs) {
			String value = facetValueVO.getValue();

			if (datastoreCode.equals(Schemas.SCHEMA.getDataStoreCode())) {
				List<Language> languages = metadataSchemasManager.getSchemaTypes(facet.getCollection()).getLanguages();
				facetValueVO.setLabel(types.getSchema(value).getLabel(withLocale(locale)));

				for (Language language : languages) {
					if (facetValueVO.getValue() == null) {
						facetValueVO.setLabel(types.getSchema(value).getLabel(language));
					}
				}

			} else if (datastoreCode.endsWith("Id_s") || datastoreCode.endsWith("Id_ss")) {
				Record record = recordServices.getDocumentById(value);
				String keyShort = "caption." + record.getTypeCode() + ".record.short";
				String caption = SchemaCaptionUtils.getShortCaptionForRecord(record, getLocale());
				if (keyShort.equals(caption)) {
					String key = "caption." + record.getTypeCode() + ".record";

					if (key.equals(caption)) {
						if (Category.SCHEMA_TYPE.equals(record.getTypeCode())) {
							facetValueVO.setLabel(record.<String>get(Schemas.CODE) + " - " + record.<String>get(Schemas.TITLE));
						} else {
							facetValueVO.setLabel(record.<String>get(Schemas.TITLE));
						}
					} else {
						facetValueVO.setLabel(caption);
					}
				} else {
					facetValueVO.setLabel(caption);
				}
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
		return schemas.wrapFacets(searchServices.cachedSearch(query));
	}
}

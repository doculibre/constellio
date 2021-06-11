package com.constellio.app.ui.util;

import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Facet;
import com.constellio.model.entities.records.wrappers.structure.FacetType;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.utils.EnumWithSmallCodeUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.app.ui.i18n.i18n.getLocale;
import static com.constellio.model.entities.Language.withLocale;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class FacetUtils {

	public static void configureQueryToComputeFacets(LogicalSearchQuery facetQuery, SchemasRecordsServices schemas,
													 SearchServices searchServices) {

		for (Facet facet : getActiveFacets(schemas, searchServices)) {
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

	public static List<Facet> getActiveFacets(SchemasRecordsServices schemas, SearchServices searchServices) {
		LogicalSearchQuery query = new LogicalSearchQuery(from(schemas.facetSchemaType()).where(schemas.facetActive()).isTrue())
				.sortAsc(schemas.facetOrder());
		return schemas.wrapFacets(searchServices.cachedSearch(query));
	}

	public static String getFacetValueLabel(Facet facet, String facetValue, Locale locale,
											MetadataSchemasManager metadataSchemasManager,
											RecordServices recordServices) {
		String facetValueLabel;

		MetadataSchemaTypes types = metadataSchemasManager.getSchemaTypes(facet.getCollection());
		String datastoreCode = facet.getFieldDataStoreCode();

		Map<String, String> enumMetadatas = findEnumMetadatasLabels(facet.getFieldDataStoreCode(), types);

		if (datastoreCode.equals(Schemas.SCHEMA.getDataStoreCode())) {
			List<Language> languages = metadataSchemasManager.getSchemaTypes(facet.getCollection()).getLanguages();
			facetValueLabel = types.getSchema(facetValue).getLabel(withLocale(locale));

			if (facetValue == null) {
				for (Language language : languages) {
					facetValueLabel = types.getSchema(facetValue).getLabel(language);
				}
			}

		} else if (datastoreCode.endsWith("Id_s") || datastoreCode.endsWith("Id_ss")) {
			Record record = recordServices.getDocumentById(facetValue);
			String keyShort = "caption." + record.getTypeCode() + ".record.short";
			String caption = SchemaCaptionUtils.getShortCaptionForRecord(record, getLocale(), true);
			if (keyShort.equals(caption)) {
				String key = "caption." + record.getTypeCode() + ".record";

				if (key.equals(caption)) {
					if (Category.SCHEMA_TYPE.equals(record.getTypeCode())) {
						facetValueLabel = record.<String>get(Schemas.CODE) + " - " + record.<String>get(Schemas.TITLE);
					} else {
						facetValueLabel = record.<String>get(Schemas.TITLE);
					}
				} else {
					facetValueLabel = caption;
				}
			} else {
				facetValueLabel = caption;
			}
		} else if (enumMetadatas.containsKey(facetValue)) {
			facetValueLabel = enumMetadatas.get(facetValue);
		} else {
			facetValueLabel = facet.getFieldValueLabel(facetValue);
		}

		return facetValueLabel != null ? facetValueLabel : facetValue;
	}

	private static Map<String, String> findEnumMetadatasLabels(String fieldDataStoreCode, MetadataSchemaTypes types) {
		Map<String, String> enumMetadatasLabels = new HashMap<>();

		for (Metadata metadata : types.getAllMetadatas().onlyWithType(MetadataValueType.ENUM)) {
			if (metadata.getDataStoreCode().equals(fieldDataStoreCode)) {
				for (String code : EnumWithSmallCodeUtils.toSmallCodeList(metadata.getEnumClass())) {
					String label = $(metadata.getEnumClass().getSimpleName() + "." + code);
					enumMetadatasLabels.put(code, label);
				}
			}
		}

		return enumMetadatasLabels;
	}

}

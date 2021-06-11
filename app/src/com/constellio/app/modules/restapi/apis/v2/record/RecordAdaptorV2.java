package com.constellio.app.modules.restapi.apis.v2.record;

import com.constellio.app.modules.restapi.apis.v2.core.BaseAdaptorV2;
import com.constellio.app.modules.restapi.apis.v2.core.BaseDaoV2;
import com.constellio.app.modules.restapi.apis.v2.record.dto.FacetDtoV2;
import com.constellio.app.modules.restapi.apis.v2.record.dto.FacetValueDtoV2;
import com.constellio.app.modules.restapi.apis.v2.record.dto.FilterMode;
import com.constellio.app.modules.restapi.apis.v2.record.dto.RecordDtoV2;
import com.constellio.app.modules.restapi.apis.v2.record.dto.RecordsResultDtoV2;
import com.constellio.app.modules.restapi.apis.v2.record.dto.ReferenceDtoV2;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Facet;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.search.SPEQueryResponse;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

public class RecordAdaptorV2 extends BaseAdaptorV2 {

	@Inject
	RecordDaoV2 recordDao;

	public RecordsResultDtoV2 adapt(SPEQueryResponse queryResponse, String collection, List<Locale> acceptLanguages) {
		List<RecordDtoV2> recordDtos = new ArrayList<>();
		List<ReferenceDtoV2> references = new ArrayList<>();

		adaptRecordsAndReferences(queryResponse.getRecords(), recordDtos, references);

		Locale locale = recordDao.getPreferredLocale(acceptLanguages);
		List<FacetDtoV2> facets = new ArrayList<>();
		queryResponse.getFieldFacetValues().forEach((key, facetValues) -> {
			Facet facet = recordDao.getFacet(key, collection);
			facets.add(FacetDtoV2.builder()
					.facetId(facet.getFieldDataStoreCode())
					.facetName(facet.getTitle(locale))
					.values(facetValues.stream()
							.map(facetValue -> FacetValueDtoV2.builder()
									.id(key.concat(":").concat(facetValue.getValue()))
									.name(recordDao.getFacetValueLabel(facet, facetValue.getValue(), locale))
									.count(facetValue.getQuantity())
									.build())
							.collect(Collectors.toList()))
					.build());
		});

		return RecordsResultDtoV2.builder().records(recordDtos).facets(!facets.isEmpty() ? facets : null)
				.references(references).build();
	}

	private void adaptRecordsAndReferences(List<Record> records, List<RecordDtoV2> recordDtos,
										   List<ReferenceDtoV2> references) {
		Set<String> referenceIds = new HashSet<>();

		records.forEach(record -> {
			RecordDtoV2 recordDto = adaptRecord(record);
			recordDtos.add(recordDto);

			MetadataSchema schema = recordDao.getMetadataSchema(record);
			schema.getAllReferences().forEach(metadata -> {
				if (recordDto.getMetadatas().containsKey(metadata.getLocalCode())) {
					record.<String>getValues(metadata).forEach(value -> {
						if (!referenceIds.contains(value)) {
							Record referenceRecord = recordDao.getRecordById(value, FilterMode.SUMMARY);
							references.add(ReferenceDtoV2.builder()
									.id(referenceRecord.getId())
									.schemaType(referenceRecord.getTypeCode())
									.code(getRecordMetadataValue(referenceRecord, Schemas.CODE))
									.title(referenceRecord.getTitle())
									.description(getRecordMetadataValue(referenceRecord, asList(Schemas.DESCRIPTION_STRING, Schemas.DESCRIPTION_TEXT)))
									.build());
							referenceIds.add(value);
						}
					});
				}
			});
		});
	}

	@Override
	public BaseDaoV2 getDao() {
		return recordDao;
	}
}

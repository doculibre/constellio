package com.constellio.model.services.records.aggregations;

import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.schemas.entries.InMemoryAggregatedValuesParams;
import com.constellio.model.entities.schemas.entries.SearchAggregatedValuesParams;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.allConditions;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.anyConditions;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.where;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.whereAny;

public class LogicalAndMetadataAggregationHandler implements MetadataAggregationHandler {

	@Override
	public Object calculate(SearchAggregatedValuesParams params) {
		List<LogicalSearchCondition> conditions = new ArrayList<>();

		Map<String, List<Metadata>> map = params.getInputMetatasBySchemaType();

		for (String metadataSchemaTypeCode : map.keySet()) {
			MetadataSchemaType metadataSchemaType = params.getTypes().getSchemaType(metadataSchemaTypeCode);
			LogicalSearchCondition metadataCondition =
					whereAny(params.getInputMetatasBySchemaType().get(metadataSchemaTypeCode)).isFalseOrNull();
			LogicalSearchCondition schemaCondition =
					where(Schemas.SCHEMA).isStartingWithText(metadataSchemaTypeCode);

			conditions.add(allConditions(schemaCondition, metadataCondition));
		}

		LogicalSearchCondition queryCondition = params.getCombinedQuery().getCondition();
		List<String> schemaTypeCodes = new ArrayList<>(params.getInputMetatasBySchemaType().keySet());
		List<MetadataSchemaType> schemaTypes = params.getTypes().getSchemaTypesWithCode(schemaTypeCodes);
		LogicalSearchQuery query = new LogicalSearchQuery(from(schemaTypes)
				.whereAllConditions(queryCondition, anyConditions(conditions)));

		return !params.getSearchServices().hasResults(query);
	}

	@Override
	public Object calculate(InMemoryAggregatedValuesParams params) {
		for (Boolean value : params.<Boolean>getValues()) {
			if (Boolean.FALSE.equals(value)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public List<Metadata> getMetadatasUsedToCalculate(GetMetadatasUsedToCalculateParams params) {
		if (params.getCurrentReferenceMetadata() == null) {
			return params.getInputMetadatas();
		}
		return params.getInputMetadatas(params.getCurrentReferenceMetadata());
	}
}

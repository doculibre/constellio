package com.constellio.model.services.records.aggregations;

import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.schemas.entries.InMemoryAggregatedValuesParams;
import com.constellio.model.entities.schemas.entries.SearchAggregatedValuesParams;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.allConditions;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.anyConditions;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.where;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.whereAny;

public class LogicalAndMetadataAggregationHandler implements MetadataAggregationHandler {

	@Override
	public Object calculate(SearchAggregatedValuesParams params) {
		List<LogicalSearchCondition> conditions = new ArrayList<>();
		for (MetadataSchemaType metadataSchemaType : params.getInputMetatasBySchemaType().keySet()) {
			LogicalSearchCondition metadataCondition =
					whereAny(params.getInputMetatasBySchemaType().get(metadataSchemaType)).isFalseOrNull();
			LogicalSearchCondition schemaCondition =
					where(Schemas.SCHEMA).isStartingWithText(metadataSchemaType.getCode());

			conditions.add(allConditions(schemaCondition, metadataCondition));
		}

		LogicalSearchCondition queryCondition = params.getQuery().getCondition();
		List<MetadataSchemaType> metadataSchemaTypes = new ArrayList<>(params.getInputMetatasBySchemaType().keySet());
		LogicalSearchQuery query = new LogicalSearchQuery(LogicalSearchQueryOperators.from(metadataSchemaTypes)
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

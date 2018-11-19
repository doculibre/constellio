package com.constellio.model.services.records.aggregations;

import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.entries.AggregatedDataEntry;

public class MetadataAggregationHandlerFactory {

	public static MetadataAggregationHandler getHandlerFor(Metadata metadata) {
		AggregatedDataEntry dataEntry = (AggregatedDataEntry) metadata.getDataEntry();
		switch (dataEntry.getAgregationType()) {

			case SUM:
				return new SumMetadataAggregationHandler();

			case REFERENCE_COUNT:
				return new ReferenceCountMetadataAggregationHandler();

			case MIN:
				return new MinMetadataAggregationHandler();

			case MAX:
				return new MaxMetadataAggregationHandler();

			case CALCULATED:
				return new CalculatorMetadataAggregationHandler();

			case VALUES_UNION:
				return new UnionMetadataAggregationHandler();

			case LOGICAL_AND:
				return new LogicalAndMetadataAggregationHandler();

			case LOGICAL_OR:
				return new LogicalOrMetadataAggregationHandler();

			default:
				throw new ImpossibleRuntimeException("Aggregation type is not yet supported : " + dataEntry.getAgregationType());
		}

	}

}

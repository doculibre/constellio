package com.constellio.model.entities.schemas.entries;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordImpl;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class SearchAggregatedValuesParams {
	LogicalSearchQuery combinedQuery;
	List<SearchAggregatedValuesParamsQuery> queries;
	RecordImpl record;
	Metadata metadata;
	AggregatedDataEntry aggregatedDataEntry;
	MetadataSchemaTypes types;
	SearchServices searchServices;
	ModelLayerFactory modelLayerFactory;

	public SearchAggregatedValuesParams(LogicalSearchQuery combinedQuery,
										List<SearchAggregatedValuesParamsQuery> queries,
										RecordImpl record,
										Metadata metadata,
										AggregatedDataEntry aggregatedDataEntry, MetadataSchemaTypes types,
										SearchServices searchServices, ModelLayerFactory modelLayerFactory) {
		this.combinedQuery = combinedQuery;
		this.queries = queries;
		this.record = record;
		this.metadata = metadata;
		this.aggregatedDataEntry = aggregatedDataEntry;
		this.types = types;
		this.searchServices = searchServices;
		this.modelLayerFactory = modelLayerFactory;
	}

	public LogicalSearchQuery getCombinedQuery() {
		return combinedQuery;
	}

	public List<SearchAggregatedValuesParamsQuery> getQueries() {
		return queries;
	}

	public RecordImpl getRecord() {
		return record;
	}

	public Metadata getMetadata() {
		return metadata;
	}

	public AggregatedDataEntry getAggregatedDataEntry() {
		return aggregatedDataEntry;
	}

	public MetadataSchemaTypes getTypes() {
		return types;
	}

	public SearchServices getSearchServices() {
		return searchServices;
	}

	public List<Metadata> getInputMetadatas() {
		return types.getMetadatas(getAggregatedDataEntry().getInputMetadatas());
	}

	public boolean isSummaryCacheEnabled() {
		return modelLayerFactory.getConfiguration().isSummaryCacheEnabled();
	}

	public RecordServices getRecordServices() {
		return modelLayerFactory.newRecordServices();
	}

	public Map<String, List<Metadata>> getInputMetatasBySchemaType() {
		Map<String, List<Metadata>> inputMetatasBySchemaType = new HashMap<>();
		for (String referenceMetadata : aggregatedDataEntry.getReferenceMetadatas()) {
			MetadataSchemaType schemaType = types.getSchemaType(SchemaUtils.getSchemaTypeCode(referenceMetadata));
			List<Metadata> metadatas = types.getMetadatas(aggregatedDataEntry.getInputMetadatas(referenceMetadata));
			inputMetatasBySchemaType.put(schemaType.getCode(), metadatas);
		}

		return inputMetatasBySchemaType;
	}

	public static class SearchAggregatedValuesParamsQuery {
		LogicalSearchQuery query;
		List<Metadata> metadatas;
		MetadataSchemaType metadataSchemaType;
		Supplier<Stream<Record>> streamSupplier;

		public SearchAggregatedValuesParamsQuery(LogicalSearchQuery query,
												 List<Metadata> metadatas,
												 MetadataSchemaType metadataSchemaType,
												 Supplier<Stream<Record>> streamSupplier) {
			this.query = query;
			this.metadatas = metadatas;
			this.metadataSchemaType = metadataSchemaType;
			this.streamSupplier = streamSupplier;
		}

		public Supplier<Stream<Record>> getStreamSupplier() {
			return streamSupplier;
		}

		public LogicalSearchQuery getQuery() {
			return query;
		}

		public List<Metadata> getMetadatas() {
			return metadatas;
		}

		public MetadataSchemaType getMetadataSchemaType() {
			return metadataSchemaType;
		}
	}
}

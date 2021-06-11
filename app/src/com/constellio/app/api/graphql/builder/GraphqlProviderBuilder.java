package com.constellio.app.api.graphql.builder;

import com.constellio.app.api.graphql.GraphqlDataFetchers;
import com.constellio.app.api.graphql.builder.binding.StructureBindings;
import graphql.schema.DataFetcher;
import graphql.schema.idl.RuntimeWiring;

import java.util.Set;

import static graphql.schema.idl.TypeRuntimeWiring.newTypeWiring;
import static org.apache.commons.lang.StringUtils.capitalize;

public class GraphqlProviderBuilder {

	public RuntimeWiring build(GraphqlDataFetchers graphqlDataFetchers, final Set<WiringInfo> wiringInfoToProcess,
							   final Set<String> schemaTypeCodes) {
		RuntimeWiring.Builder builder = RuntimeWiring.newRuntimeWiring();

		wiringInfoToProcess.forEach(wiringInfo -> {
			DataFetcher dataFetcher;
			if (wiringInfo.isReference()) {
				dataFetcher = graphqlDataFetchers.getRecordsByReferenceDataFetcher(wiringInfo.getConstellioName());
			} else if (wiringInfo.getName().contains("ByCode")) {
				dataFetcher = graphqlDataFetchers.getRecordByCodeDataFetcher(wiringInfo.getSchemaType());
			} else if (wiringInfo.getName().contains("ById")) {
				dataFetcher = graphqlDataFetchers.getRecordByIdDataFetcher();
			} else {
				dataFetcher = graphqlDataFetchers.searchRecordsByExpressionDataFetcher(wiringInfo.getSchemaType());
			}
			builder.type(newTypeWiring(wiringInfo.getType())
					.dataFetcher(wiringInfo.getName(), dataFetcher));
		});

		schemaTypeCodes.forEach(schemaTypeCode -> {
			builder.type(newTypeWiring(capitalize(schemaTypeCode))
					.defaultDataFetcher(graphqlDataFetchers.getMetadataDataFetcher()));
		});
		StructureBindings.getSupportedStructures().forEach(structureClass -> {
			builder.type(newTypeWiring(structureClass.getSimpleName())
					.defaultDataFetcher(graphqlDataFetchers.getStructureDataFetcher()));
		});

		return builder.build();
	}
}

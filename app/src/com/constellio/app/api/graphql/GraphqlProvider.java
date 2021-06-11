package com.constellio.app.api.graphql;

import com.constellio.app.api.graphql.builder.GraphqlBuilder;
import com.constellio.app.services.factories.AppLayerFactory;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;

public class GraphqlProvider {

	private final GraphqlDataFetchers graphqlDataFetchers;
	private final GraphQL graphQL;

	public GraphqlProvider(AppLayerFactory appLayerFactory, String collection) {
		graphqlDataFetchers = new GraphqlDataFetchers(appLayerFactory, collection);
		GraphqlBuilder graphqlGenerator = new GraphqlBuilder(appLayerFactory, collection);
		GraphQLSchema graphQLSchema = buildSchema(graphqlGenerator.buildSchemas(), graphqlGenerator);
		graphQL = GraphQL.newGraphQL(graphQLSchema).build();
	}

	public GraphQL getGraphQL() {
		return graphQL;
	}

	private GraphQLSchema buildSchema(String schemasData, GraphqlBuilder graphqlGenerator) {
		TypeDefinitionRegistry typeRegistry = new SchemaParser().parse(schemasData);
		RuntimeWiring runtimeWiring = graphqlGenerator.buildDataFetchers(graphqlDataFetchers);
		SchemaGenerator schemaGenerator = new SchemaGenerator();
		return schemaGenerator.makeExecutableSchema(typeRegistry, runtimeWiring);
	}
}

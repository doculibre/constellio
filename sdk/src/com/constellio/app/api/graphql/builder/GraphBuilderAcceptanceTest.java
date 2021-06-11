package com.constellio.app.api.graphql.builder;

import com.constellio.app.api.graphql.GraphqlDataFetchers;
import com.constellio.sdk.tests.ConstellioTest;
import graphql.schema.idl.RuntimeWiring;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Ignore
public class GraphBuilderAcceptanceTest extends ConstellioTest {

	@Before
	public void setUp() {
		prepareSystem(withZeCollection().withConstellioRMModule());
	}

	@Test
	public void testBuildSchemas() {
		String schemasData = new GraphqlBuilder(getAppLayerFactory(), zeCollection).buildSchemas();
		assertThat(schemasData).isNotEmpty();
	}

	@Test
	public void testBuildDataFetchers() {
		GraphqlDataFetchers graphqlDataFetchers = new GraphqlDataFetchers(getAppLayerFactory(), zeCollection);
		RuntimeWiring runtimeWiring = new GraphqlBuilder(getAppLayerFactory(), zeCollection).buildDataFetchers(graphqlDataFetchers);
		assertThat(runtimeWiring.getDataFetchers()).isNotEmpty();
	}

}

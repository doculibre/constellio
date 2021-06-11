package com.constellio.app.api.graphql;

import com.constellio.app.api.graphql.utils.GraphqlGson;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.data.services.tenant.TenantLocal;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.HttpMethod;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class GraphqlServlet extends HttpServlet {

	static TenantLocal<Map<String, GraphqlProvider>> graphqlProvidersByCollection = new TenantLocal<>();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		doRequest(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		doRequest(request, response);
	}

	private void doRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
		try {
			processRequest(request, response);
		} catch (Exception e) {
			log.error("Error while processing graphql request", e);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unknown error");
		}
	}

	private void processRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
		// TODO validate token and restrict to admin

		String collection = request.getParameter("collection");
		if (getGraphProvider(collection) == null) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid collection");
			return;
		}

		GraphqlQueryParameters parameters = GraphqlQueryParameters.from(request);
		if (parameters.getQuery() == null) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid query");
			return;
		}

		ExecutionInput.Builder executionInput = ExecutionInput.newExecutionInput()
				.query(parameters.getQuery())
				.operationName(parameters.getOperationName())
				.variables(parameters.getVariables() != null ? parameters.getVariables() : Collections.emptyMap());

		Map<String, Object> context = new HashMap<>();
		executionInput.context(context);

		GraphQL graphQL = getGraphProvider(collection).getGraphQL();
		ExecutionResult executionResult = graphQL.execute(executionInput.build());

		writeResponse(response, executionResult);
	}

	private void writeResponse(HttpServletResponse response, ExecutionResult executionResult) throws IOException {
		response.setContentType("application/json");
		response.setStatus(HttpServletResponse.SC_OK);
		GraphqlGson.writeJson(response, executionResult.toSpecification());
	}

	private GraphqlProvider getGraphProvider(String collection) {
		if (graphqlProvidersByCollection.get() == null) {
			AppLayerFactory appLayerFactory = ConstellioFactories.getInstance().getAppLayerFactory();
			Map<String, GraphqlProvider> providersByCollection = new HashMap<>();
			appLayerFactory.getCollectionsManager().getCollectionCodesExcludingSystem().forEach(currentCollection -> {
				providersByCollection.put(currentCollection, new GraphqlProvider(appLayerFactory, currentCollection));
			});
			graphqlProvidersByCollection.set(providersByCollection);
		}
		return graphqlProvidersByCollection.get().get(collection);
	}

	@Getter
	private static class GraphqlQueryParameters {
		private String query;
		private String operationName;
		private Map<String, Object> variables;

		static GraphqlQueryParameters from(HttpServletRequest request) throws IOException {
			GraphqlQueryParameters parameters = new GraphqlQueryParameters();
			if (request.getMethod().equals(HttpMethod.POST)) {
				Map<String, Object> json = readBody(request);
				parameters.query = (String) json.get("query");
				parameters.operationName = (String) json.get("operationName");
				parameters.variables = getVariables(json.get("variables"));
			} else {
				parameters.query = request.getParameter("query");
				parameters.operationName = request.getParameter("operationName");
				parameters.variables = getVariables(request.getParameter("variables"));
			}
			return parameters;
		}


		private static Map<String, Object> getVariables(Object variables) {
			if (variables instanceof Map) {
				Map<?, ?> inputVars = (Map<?, ?>) variables;
				Map<String, Object> vars = new HashMap<>();
				inputVars.forEach((k, v) -> vars.put(String.valueOf(k), v));
				return vars;
			}
			return GraphqlGson.toMap(String.valueOf(variables));
		}

		private static Map<String, Object> readBody(HttpServletRequest request) throws IOException {
			String requestData = request.getReader().lines().collect(Collectors.joining());
			return GraphqlGson.toMap(requestData);
		}
	}
}

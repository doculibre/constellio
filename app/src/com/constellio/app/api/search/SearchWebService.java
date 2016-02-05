package com.constellio.app.api.search;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.request.LocalSolrQueryRequest;
import org.apache.solr.response.JSONResponseWriter;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.response.XMLResponseWriter;
import org.apache.solr.servlet.SolrRequestParsers;

import com.constellio.app.api.HttpServletRequestAuthenticator;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.search.FreeTextSearchServices;
import com.constellio.model.services.search.query.logical.FreeTextQuery;

public class SearchWebService extends HttpServlet {

	private static synchronized ConstellioFactories getConstellioFactories() {
		return ConstellioFactories.getInstance();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		HttpServletRequestAuthenticator authenticator = new HttpServletRequestAuthenticator(modelLayerFactory());
		UserCredential user = authenticator.authenticate(request);
		if (user == null) {
			throw new RuntimeException("Invalid serviceKey/token");
		}
		boolean searchingInEvents = "true".equals(request.getParameter("searchEvents"));
		ModifiableSolrParams solrParams = new ModifiableSolrParams(
				SolrRequestParsers.parseQueryString(request.getQueryString()));
		solrParams.remove("searchEvents");
		solrParams.remove(HttpServletRequestAuthenticator.USER_SERVICE_KEY);
		solrParams.remove(HttpServletRequestAuthenticator.USER_TOKEN);

		QueryResponse queryResponse;
		if (searchingInEvents) {
			if (!user.isSystemAdmin()) {
				throw new RuntimeException("You need system admin privileges");
			}

			queryResponse = getEventQueryResponse(solrParams);

		} else {
			queryResponse = getQueryResponse(solrParams, user);
		}

		writeResponse(response, solrParams, queryResponse);
	}

	private ModelLayerFactory modelLayerFactory() {
		return ConstellioFactories.getInstance().getModelLayerFactory();
	}

	private QueryResponse getQueryResponse(ModifiableSolrParams solrParams, UserCredential user) {
		ModelLayerFactory modelLayerFactory = getConstellioFactories().getModelLayerFactory();
		FreeTextSearchServices freeTextSearchServices = modelLayerFactory.newFreeTextSearchServices();
		return freeTextSearchServices.search(new FreeTextQuery(solrParams).filteredByUser(user));
	}

	private QueryResponse getEventQueryResponse(ModifiableSolrParams solrParams) {
		ModelLayerFactory modelLayerFactory = getConstellioFactories().getModelLayerFactory();
		FreeTextSearchServices freeTextSearchServices = modelLayerFactory.newFreeTextSearchServices();
		return freeTextSearchServices.search(new FreeTextQuery(solrParams).searchEvents());
	}

	private void writeResponse(HttpServletResponse response, ModifiableSolrParams solrParams, QueryResponse queryResponse) {
		response.setContentType("application/xml; charset=UTF-8");
		OutputStream outputStream;
		try {
			outputStream = response.getOutputStream();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		SolrQueryResponse sResponse = new SolrQueryResponse();
		sResponse.setAllValues(queryResponse.getResponse());

		XMLResponseWriter xmlWriter = new XMLResponseWriter();

		try (OutputStreamWriter out = new OutputStreamWriter(outputStream)) {
			if (("json").equals(solrParams.get("wt"))) {
				response.setCharacterEncoding("UTF-8");
				response.setContentType("application/json; charset=UTF-8");
				JSONResponseWriter jsonWriter = new JSONResponseWriter();
				jsonWriter.write(out, new LocalSolrQueryRequest(null, solrParams), sResponse);
			} else {
				xmlWriter.write(out, new LocalSolrQueryRequest(null, solrParams), sResponse);
			}
			out.flush();
		} catch (IOException e) {
			throw new RuntimeException("Unable to convert Solr response into XML", e);
		}
	}
}

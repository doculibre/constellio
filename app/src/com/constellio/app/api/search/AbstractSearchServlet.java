package com.constellio.app.api.search;

import com.constellio.app.api.HttpServletRequestAuthenticator;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.model.entities.Language;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.search.FreeTextSearchServices;
import com.constellio.model.services.search.query.logical.FreeTextQuery;
import com.constellio.model.services.users.SystemWideUserInfos;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.LocalSolrQueryRequest;
import org.apache.solr.response.JSONResponseWriter;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.response.XMLResponseWriter;
import org.apache.solr.servlet.SolrRequestParsers;
import org.jetbrains.annotations.NotNull;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public abstract class AbstractSearchServlet extends HttpServlet {
	public static final String THESAURUS_VALUE = "thesaurusValue";
	public static final String SKOS_CONCEPTS = "skosConcepts";
	public static final String SEARCH_EVENTS = "searchEvents";
	public static final String CORE = "core";
	public static final String FEATURED_LINKS = "featuredLinks";
	public static final String FACET_LABELS = "facet_labels";
	public static final String AUTOCOMPLETE = "autocomplete";
	public static final String SEARCH_EVENT = "searchEvent";

	@Override
	protected final void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			doGet(authenticate(req), req, resp);
		} catch (isNotAuthenticatedException e) {
			resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED, "invalid authentification information");
		}
	}

	protected abstract void doGet(SystemWideUserInfos user, HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException;

	protected synchronized ConstellioFactories getConstellioFactories() {
		return ConstellioFactories.getInstance();
	}

	@NotNull
	protected ModifiableSolrParams getModifiableSolrParams(String queryString) {
		ModifiableSolrParams solrParams = new ModifiableSolrParams(SolrRequestParsers.parseQueryString(queryString));

		solrParams.remove(SEARCH_EVENTS);
		solrParams.remove(CORE);
		solrParams.remove(THESAURUS_VALUE);
		solrParams.remove(HttpServletRequestAuthenticator.USER_SERVICE_KEY);
		solrParams.remove(HttpServletRequestAuthenticator.USER_TOKEN);

		return solrParams;
	}

	protected String getCollection(ModifiableSolrParams solrParams) {
		String[] strings = solrParams.getParams("fq");
		String prefix = "collection_s:";

		String collection = "";

		for (String param : strings) {
			if (StringUtils.startsWith(param, prefix)) {
				collection = StringUtils.removeAll(param, prefix);
				break;
			}
		}

		return collection;
	}

	protected Language getLanguage(ModifiableSolrParams solrParams) {
		String[] strings = solrParams.getParams("fq");
		String prefix = "language_s:";

		String langue = "";

		for (String param : strings) {
			if (StringUtils.startsWith(param, prefix)) {
				langue = StringUtils.removeAll(param, prefix);
				break;
			}
		}

		return Language.withCode(langue);
	}

	@NotNull
	protected SystemWideUserInfos authenticate(HttpServletRequest request) {
		HttpServletRequestAuthenticator authenticator = new HttpServletRequestAuthenticator(modelLayerFactory());
		SystemWideUserInfos user = authenticator.authenticate(request);
		if (user == null) {
			throw new isNotAuthenticatedException();
		}

		return user;
	}

	protected ModelLayerFactory modelLayerFactory() {
		return getConstellioFactories().getModelLayerFactory();
	}

	protected AppLayerFactory appLayerFactory() {
		return getConstellioFactories().getAppLayerFactory();
	}

	protected QueryResponse getQueryResponse(String core, ModifiableSolrParams solrParams, SystemWideUserInfos user) {
		FreeTextSearchServices freeTextSearchServices = modelLayerFactory().newFreeTextSearchServices();
		return freeTextSearchServices.search(core, new FreeTextQuery(solrParams).filteredByUser(user));
	}

	protected QueryResponse getEventQueryResponse(ModifiableSolrParams solrParams) {
		FreeTextSearchServices freeTextSearchServices = modelLayerFactory().newFreeTextSearchServices();
		return freeTextSearchServices.search(new FreeTextQuery(solrParams).searchEvents());
	}

	@SuppressWarnings("unchecked")
	protected void writeResponse(HttpServletResponse response, ModifiableSolrParams solrParams,
								 QueryResponse queryResponse, NamedList skosConceptsNL, NamedList featuredLinkNL,
								 NamedList facetLabelsNL, NamedList autocompleteSuggestionsNL,
								 NamedList searchEventNL) {
		response.setContentType("application/xml; charset=UTF-8");
		OutputStream outputStream;
		try {
			outputStream = response.getOutputStream();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		SolrQueryResponse sResponse = new SolrQueryResponse();
		if (queryResponse != null) {
			sResponse.setAllValues(queryResponse.getResponse());
		}

		if (skosConceptsNL != null && skosConceptsNL.size() > 0) {
			sResponse.getValues().add(SKOS_CONCEPTS, skosConceptsNL);
		}

		if (featuredLinkNL != null && featuredLinkNL.size() > 0) {
			sResponse.getValues().add(FEATURED_LINKS, featuredLinkNL);
		}

		if (facetLabelsNL != null && facetLabelsNL.size() > 0) {
			sResponse.getValues().add(FACET_LABELS, facetLabelsNL);
		}

		if (autocompleteSuggestionsNL != null && autocompleteSuggestionsNL.size() > 0) {
			sResponse.getValues().add(AUTOCOMPLETE, autocompleteSuggestionsNL);
		}

		if (searchEventNL != null && searchEventNL.size() > 0) {
			sResponse.getValues().add(SEARCH_EVENT, searchEventNL);
		}

		XMLResponseWriter xmlWriter = new XMLResponseWriter();

		try (OutputStreamWriter out = new OutputStreamWriter(outputStream)) {
			if (("json").equals(solrParams.get("wt"))) {
				response.setCharacterEncoding("UTF-8");
				response.setHeader("Access-Control-Allow-Origin", "*");
				response.setContentType("application/json; charset=UTF-8");
				JSONResponseWriter jsonWriter = new JSONResponseWriter();
				jsonWriter.write(out, new LocalSolrQueryRequest(null, solrParams), sResponse);
			} else {
				response.setCharacterEncoding("UTF-8");
				response.setHeader("Access-Control-Allow-Origin", "*");
				response.setContentType("application/xml; charset=UTF-8");
				xmlWriter.write(out, new LocalSolrQueryRequest(null, solrParams), sResponse);
			}
			out.flush();
		} catch (IOException e) {
			throw new RuntimeException("Unable to convert Solr response into XML", e);
		}
	}
}

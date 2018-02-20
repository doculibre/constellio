package com.constellio.app.api.search;

import com.constellio.app.api.HttpServletRequestAuthenticator;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.model.entities.records.wrappers.SearchEvent;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.logging.SearchEventServices;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.search.FreeTextSearchServices;
import com.constellio.model.services.search.query.logical.FreeTextQuery;
import com.constellio.model.services.thesaurus.ThesaurusManager;
import com.constellio.model.services.thesaurus.ThesaurusService;
import com.google.common.base.Strings;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.request.LocalSolrQueryRequest;
import org.apache.solr.response.JSONResponseWriter;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.response.XMLResponseWriter;
import org.apache.solr.servlet.SolrRequestParsers;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

public class SearchWebService extends HttpServlet {

	private static synchronized ConstellioFactories getConstellioFactories() {
		return ConstellioFactories.getInstance();
	}
	public static final String THESAURUS_VALUE = "thesaurusValue";


	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		List<String> desanbiguation = null;
		List<String> suggestion = null;

		HttpServletRequestAuthenticator authenticator = new HttpServletRequestAuthenticator(modelLayerFactory());
		UserCredential user = authenticator.authenticate(request);
		if (user == null) {
			throw new RuntimeException("Invalid serviceKey/token");
		}

		boolean searchingInEvents = "true".equals(request.getParameter("searchEvents"));
		ModifiableSolrParams solrParams = new ModifiableSolrParams(
				SolrRequestParsers.parseQueryString(request.getQueryString()));
		String thesaurusValue = solrParams.get(THESAURUS_VALUE);
		solrParams.remove("searchEvents");
		solrParams.remove(THESAURUS_VALUE);
		solrParams.remove(HttpServletRequestAuthenticator.USER_SERVICE_KEY);
		solrParams.remove(HttpServletRequestAuthenticator.USER_TOKEN);
		solrParams.add("fq", "-type_s:index");

		String[] strings = solrParams.getParams("fq");

		String collection = "";

		for(String param : strings) {
			if(param.startsWith("collection_s")) {
				collection = param.replace("collection_s:", "");
				break;
			}
		}



		QueryResponse queryResponse;
		if(!Strings.isNullOrEmpty(thesaurusValue) && searchingInEvents) {
			throw new RuntimeException("You cannot search event and have a thesaurusValue");
		}
		else if (searchingInEvents) {
			if (!user.isSystemAdmin()) {
				throw new RuntimeException("You need system admin privileges");
			}

			queryResponse = getEventQueryResponse(solrParams);

		} else {
			SchemasRecordsServices schemasRecordsServices = null;
			ArrayList<String> paramList = new ArrayList<>();
			SearchEvent searchEvent = null;

			if(!Strings.isNullOrEmpty(thesaurusValue)) {
				ThesaurusManager thesaurusManager = modelLayerFactory().getThesaurusManager();
				ThesaurusService thesaurusService;
				if((thesaurusService = thesaurusManager.get()) != null) {
					suggestion = thesaurusService.getSkosConcepts(thesaurusValue).getAll(ThesaurusService.SUGGESTION);
					desanbiguation = thesaurusService.getSkosConcepts(thesaurusValue).getAll(ThesaurusService.DESAMBIUGATION);
				}
			}

			if(!Strings.isNullOrEmpty(collection)) {
				schemasRecordsServices = new SchemasRecordsServices(collection, modelLayerFactory());
				searchEvent = schemasRecordsServices.newSearchEvent();

				for (String paramName : solrParams.getParameterNames()) {
					if (!paramName.equals("qf") && !paramName.equals("pf")
							&& !paramName.equals("fl")) {
						if (paramName.equals("q")) {
							searchEvent.setQuery(StringUtils.stripAccents(solrParams.get(paramName).toLowerCase()));
						} else {
							String[] values = solrParams.getParams(paramName);

							if (values.length == 1) {
								paramList.add(paramName + "=" + values[0]);
							} else if (values.length > 1) {
								StringBuilder valuesAsOneStringBuilder = new StringBuilder();
								for (String value : values) {
									valuesAsOneStringBuilder.append(paramName).append("=").append(value).append(";");
								}
								paramList.add(valuesAsOneStringBuilder.toString());
							}

						}
					}
				}
			}

			queryResponse = getQueryResponse(solrParams, user);

			if(schemasRecordsServices != null) {
				searchEvent.setParams(paramList);
				searchEvent.setQTime(queryResponse.getQTime());
				searchEvent.setNumFound(queryResponse.getResults().getNumFound());

				SearchEventServices searchEventServices = new SearchEventServices(collection, modelLayerFactory());
				searchEventServices.save(searchEvent);
			}
		}

		if(suggestion != null && suggestion.size() > 0) {
			solrParams.add(ThesaurusService.SUGGESTION, suggestion.toArray(new String[0]));
		}

		if(desanbiguation != null && suggestion.size() > 0) {
			solrParams.add(ThesaurusService.DESAMBIUGATION, desanbiguation.toArray(new String[0]));
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

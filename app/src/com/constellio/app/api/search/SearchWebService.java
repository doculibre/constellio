package com.constellio.app.api.search;

import com.constellio.data.utils.dev.Toggle;
import com.constellio.model.entities.records.wrappers.SearchEvent;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.logging.SearchEventServices;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.thesaurus.ResponseSkosConcept;
import com.constellio.model.services.thesaurus.ThesaurusService;
import com.google.common.base.Strings;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.util.NamedList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SearchWebService extends AbstractSearchServlet {
	@Override
	protected void doGet(UserCredential user, HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		ModifiableSolrParams solrParams = getModifiableSolrParams(req.getQueryString());
		boolean searchingInEvents = "true".equals(req.getParameter(SEARCH_EVENTS));
		String thesaurusValue = req.getParameter(THESAURUS_VALUE);
		String core = req.getParameter(CORE);

		solrParams.add("fq", "-type_s:index");

		solrParams.set("rows", StringUtils.defaultString(solrParams.get("rows"), "" + 10));
		solrParams.set("start", StringUtils.defaultString(solrParams.get("start"), "" + 0));

		String[] strings = solrParams.getParams("fq");

		String collection = "";

		ResponseSkosConcept responseSkosConcept = null;

		for (String param : strings) {
			if (param.startsWith("collection_s")) {
				collection = param.replace("collection_s:", "");
				break;
			}
		}

		QueryResponse queryResponse;
		if (!Strings.isNullOrEmpty(thesaurusValue) && searchingInEvents) {
			throw new RuntimeException("You cannot search event and have a thesaurusValue");
		} else if (searchingInEvents) {
			if (!user.isSystemAdmin()) {
				throw new RuntimeException("You need system admin privileges");
			}

			queryResponse = getEventQueryResponse(solrParams);

		} else {
			SchemasRecordsServices schemasRecordsServices = null;
			ArrayList<String> paramList = new ArrayList<>();
			SearchEvent searchEvent = null;

			if (!Strings.isNullOrEmpty(collection)) {
				schemasRecordsServices = new SchemasRecordsServices(collection, modelLayerFactory());
				searchEvent = schemasRecordsServices.newSearchEvent();
				searchEvent.setClickCount(0);

				for (String paramName : solrParams.getParameterNames()) {
					if (!paramName.equals("qf") && !paramName.equals("pf")
						&& !paramName.equals("fl")) {
						if (paramName.equals("q")) {
							String query = StringUtils.stripAccents(solrParams.get(paramName).toLowerCase());

							searchEvent.setQuery(query);
							searchEvent.setOriginalQuery(query);
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

			queryResponse = getQueryResponse(core, solrParams, user);

			if (schemasRecordsServices != null) {

				if (solrParams.get("rows") == null) {
					paramList.add("rows=10");
				}

				if (solrParams.get("start") == null) {
					paramList.add("start=0");
				}

				searchEvent.setParams(paramList);
				searchEvent.setQTime(queryResponse.getQTime());
				searchEvent.setNumFound(queryResponse.getResults().getNumFound());

				if (Toggle.ADVANCED_SEARCH_CONFIGS.isEnabled()) {
					SearchEventServices searchEventServices = new SearchEventServices(collection, modelLayerFactory());
					searchEventServices.save(searchEvent);
				}
			}
		}

		NamedList skosConceptsNL = new NamedList();

		if (responseSkosConcept != null && responseSkosConcept.getSuggestions().size() > 0) {
			NamedList suggestionsNL = new NamedList();

			for (Locale locale : responseSkosConcept.getSuggestions().keySet()) {
				List<String> suggestionList = responseSkosConcept.getSuggestions().get(locale);
				NamedList localeSuggestionsNL = new NamedList();
				for (String suggestion : suggestionList) {
					localeSuggestionsNL.add("label", suggestion);
				}

				suggestionsNL.add(locale.getLanguage(), localeSuggestionsNL);
			}

			skosConceptsNL.add(ThesaurusService.SUGGESTIONS, suggestionsNL);
		}

		if (responseSkosConcept != null && responseSkosConcept.getDisambiguations().size() > 0) {
			NamedList disambiguationsNL = new NamedList();

			for (Locale locale : responseSkosConcept.getDisambiguations().keySet()) {
				List<String> disambiguationList = responseSkosConcept.getDisambiguations().get(locale);
				NamedList localeDisambiguationsNL = new NamedList();
				for (String disambiguation : disambiguationList) {
					localeDisambiguationsNL.add("label", disambiguation);
				}

				disambiguationsNL.add(locale.getLanguage(), localeDisambiguationsNL);
			}

			skosConceptsNL.add(ThesaurusService.DISAMBIGUATIONS, disambiguationsNL);
		}

		writeResponse(resp, solrParams, queryResponse, skosConceptsNL, null, null, null, null);
	}
}

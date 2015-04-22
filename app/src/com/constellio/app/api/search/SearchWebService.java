/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
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
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.response.XMLResponseWriter;
import org.apache.solr.servlet.SolrRequestParsers;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.search.FreeTextSearchServices;
import com.constellio.model.services.search.query.logical.FreeTextQuery;
import com.constellio.model.services.security.authentification.AuthenticationService;
import com.constellio.model.services.users.UserCredentialsManager;
import com.constellio.model.services.users.UserServices;
import com.constellio.model.services.users.UserServicesRuntimeException;

public class SearchWebService extends HttpServlet {

	private static synchronized ConstellioFactories getConstellioFactories() {
		return ConstellioFactories.getInstance();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		UserCredential user = getAuthenticatedUser(request);
		boolean searchingInEvents = "true".equals(request.getParameter("searchEvents"));
		ModifiableSolrParams solrParams = new ModifiableSolrParams(
				SolrRequestParsers.parseQueryString(request.getQueryString()));
		//		solrParams.remove("username");
		//		solrParams.remove("password");
		solrParams.remove("searchEvents");
		solrParams.remove("serviceKey");
		solrParams.remove("token");

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

	private UserCredential getAuthenticatedUserOld(HttpServletRequest request) {
		String username = request.getParameter("username");
		String password = request.getParameter("password");
		if (username == null || password == null) {
			throw new RuntimeException("'username' and 'password' parameters required");
		}
		ModelLayerFactory modelLayerFactory = getConstellioFactories().getModelLayerFactory();
		UserCredentialsManager userCredentialsManager = modelLayerFactory.getUserCredentialsManager();
		AuthenticationService authenticationService = modelLayerFactory
				.newAuthenticationService();

		if (authenticationService.authenticate(username, password)) {
			return userCredentialsManager.getUserCredential(username);
		} else {

			throw new RuntimeException("Invalid login/password");
		}
	}

	private UserCredential getAuthenticatedUser(HttpServletRequest request) {
		String token = request.getParameter("token");
		String serviceKey = request.getParameter("serviceKey");
		if (token == null || serviceKey == null) {
			throw new RuntimeException("'token' and 'serviceKey' parameters required");
		}
		ModelLayerFactory modelLayerFactory = getConstellioFactories().getModelLayerFactory();
		UserServices userServices = modelLayerFactory.newUserServices();

		try {
			userServices.getServiceKeyByToken(token);
			String username = userServices.getUserCredentialByServiceKey(serviceKey);
			return userServices.getUserCredential(username);
		} catch (UserServicesRuntimeException e) {
			throw new RuntimeException("Invalid login/password");
		}
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
			xmlWriter.write(out, new LocalSolrQueryRequest(null, solrParams), sResponse);
			out.flush();
		} catch (IOException e) {
			throw new RuntimeException("Unable to convert Solr response into XML", e);
		}
	}
}

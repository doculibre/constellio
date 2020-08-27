package com.constellio.model.conf.ldap.services;

import com.constellio.model.conf.ldap.services.AzureAdClient.AzureAdClientException;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.microsoft.aad.adal4j.AuthenticationContext;
import com.microsoft.aad.adal4j.AuthenticationResult;
import com.microsoft.aad.adal4j.ClientCredential;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class AzureRequestHelper {

	private static final Logger LOGGER = LoggerFactory.getLogger(AzureRequestHelper.class);

	// TODO : Use "graph.microsoft.com/v1.0" instead as recommended in https://blogs.msdn.microsoft.com/aadgraphteam/2016/07/08/microsoft-graph-or-azure-ad-graph/
	private static final String GRAPH_API_URL = "https://graph.windows.net/";

	private static final String GRAPH_API_VERSION = "1.6";

	private static final String AUTHORITY_BASE_URL = "https://login.microsoftonline.com/";

	@VisibleForTesting
	static int maxResults = 150;

	Map<String, JSONObject> cacheObjects = new HashMap<>();

	private static String getResponseText(final Response response) {
		return response.readEntity(String.class).replace("\uFEFF", "");
	}

	private static String getSkipToken(final String responseText) {
		if (responseText.contains("odata.nextLink")) {
			for (String string : new JSONObject(responseText).getString("odata.nextLink").split("\\?")) {
				for (String stringOfString : string.split("&")) {
					if (stringOfString.startsWith("$skiptoken")) {
						return stringOfString.split("=")[1];
					}
				}
			}

			return null;
		} else {
			return null;
		}
	}

	private String tenantName;

	private String clientId;

	private String clientSecret;

	private Client client;

	private WebTarget webTarget;

	private AuthenticationResult authenticationResult;

	public AzureRequestHelper(final String tenantName, final String clientId, final String clientSecret) {
		this.tenantName = tenantName;

		this.clientId = clientId;

		this.clientSecret = clientSecret;

		client = JerseyClientBuilder.newClient();
		client.property(ClientProperties.CONNECT_TIMEOUT, 300000);
		client.property(ClientProperties.READ_TIMEOUT, 300000);

		webTarget = client.target(GRAPH_API_URL + tenantName);
	}

	private void acquireAccessToken() {
		acquireAccessToken(authenticationResult == null || authenticationResult.getAccessToken() == null);
	}

	private void acquireAccessToken(boolean ignoreCurrent) {
		if (ignoreCurrent) {
			String authority = AUTHORITY_BASE_URL + tenantName;
			ExecutorService executorService = Executors.newSingleThreadExecutor();

			try {
				AuthenticationContext authenticationContext = new AuthenticationContext(authority, true, executorService);

				Future<AuthenticationResult> authenticationResultFuture = authenticationContext.acquireToken(
						GRAPH_API_URL,
						new ClientCredential(clientId, clientSecret),
						null
				);

				authenticationResult = authenticationResultFuture.get();
			} catch (MalformedURLException mue) {
				throw new AzureAdClientException("Malformed Azure AD authority URL " + authority);
			} catch (ExecutionException ee) {
				throw new AzureAdClientException(
						"Can't acquire an Azure AD token for client " + clientId + " with the provided secret key", ee);
			} catch (InterruptedException ignored) {
			} finally {
				executorService.shutdown();
			}
		}
	}

	private void refreshAccessToken() {
		if (authenticationResult == null || authenticationResult.getRefreshToken() == null) {
			acquireAccessToken(true);
		} else {
			String authority = AUTHORITY_BASE_URL + tenantName;
			ExecutorService executorService = Executors.newSingleThreadExecutor();

			try {
				AuthenticationContext authenticationContext = new AuthenticationContext(authority, true, executorService);

				Future<AuthenticationResult> authenticationResultFuture = authenticationContext.acquireTokenByRefreshToken(
						authenticationResult.getRefreshToken(),
						new ClientCredential(clientId, clientSecret),
						null
				);

				authenticationResult = authenticationResultFuture.get();
			} catch (MalformedURLException mue) {
				throw new AzureAdClientException("Malformed Azure AD authority URL " + authority);
			} catch (ExecutionException ee) {
				throw new AzureAdClientException(
						"Can't acquire an Azure AD token for client " + clientId + " with the provided secret key", ee);
			} catch (InterruptedException ignored) {
			} finally {
				executorService.shutdown();
			}
		}
	}

	private Invocation.Builder completeQueryBuilding(WebTarget webTarget, String filter) {
		WebTarget newWebTarget = webTarget.queryParam("api-version", GRAPH_API_VERSION);

		if (StringUtils.isNotEmpty(filter)) {
			return newWebTarget
					.queryParam("$filter", filter)
					.request(MediaType.APPLICATION_JSON)
					.header(HttpHeaders.AUTHORIZATION, authenticationResult.getAccessToken());
		}

		return newWebTarget
				.request(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, authenticationResult.getAccessToken());
	}

	private Invocation.Builder completeQueryBuilding(WebTarget webTarget, String filter, String skipToken) {
		WebTarget newWebTarget = webTarget.queryParam("api-version", GRAPH_API_VERSION).queryParam("$top", maxResults);

		if (StringUtils.isNotEmpty(filter)) {
			newWebTarget = newWebTarget.queryParam("$filter", filter);
		}

		if (skipToken == null) {
			return newWebTarget
					.request(MediaType.APPLICATION_JSON)
					.header(HttpHeaders.AUTHORIZATION, authenticationResult.getAccessToken());
		}

		return newWebTarget
				.queryParam("$skiptoken", skipToken)
				.request(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, authenticationResult.getAccessToken());
	}

	private JSONArray submitQueryWithoutPagination(WebTarget webTarget, String filter) {
		String responseText;

		acquireAccessToken();

		Response response = completeQueryBuilding(webTarget, filter).get();

		if (response.getStatus() == HttpURLConnection.HTTP_UNAUTHORIZED) {
			refreshAccessToken();

			response = completeQueryBuilding(webTarget, filter).get();
		}

		if (new Integer(response.getStatus()).toString().startsWith("5")) {
			try {
				Thread.sleep(5000);
			} catch (InterruptedException ignored) {
			}

			response = completeQueryBuilding(webTarget, filter).get();
		}

		responseText = getResponseText(response);

		if (response.getStatus() == HttpURLConnection.HTTP_OK) {
			return new JSONObject(responseText).getJSONArray("value");
		} else if (new Integer(response.getStatus()).toString().startsWith("5")) {
			LOGGER.error(responseText);
			throw new AzureAdClientException("Unexpected Azure AD Graph API server error");
		} else {
			throw new AzureAdClientException(
					new JSONObject(responseText).optJSONObject("odata.error").optJSONObject("message").optString("value"));
		}
	}

	private List<JSONArray> submitQueryWithPagination(WebTarget webTarget, String filter) {
		List<JSONArray> result = new ArrayList<>();

		String responseText;
		String skipToken = null;

		do {
			acquireAccessToken();

			Response response = completeQueryBuilding(webTarget, filter, skipToken).get();

			if (response.getStatus() == HttpURLConnection.HTTP_UNAUTHORIZED) {
				refreshAccessToken();

				response = completeQueryBuilding(webTarget, filter, skipToken).get();
			}

			if (new Integer(response.getStatus()).toString().startsWith("5")) {
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					break;
				}

				response = completeQueryBuilding(webTarget, filter, skipToken).get();
			}

			responseText = getResponseText(response);
			skipToken = getSkipToken(responseText);

			if (response.getStatus() == HttpURLConnection.HTTP_OK) {
				result.add(new JSONObject(responseText).getJSONArray("value"));
			} else if (new Integer(response.getStatus()).toString().startsWith("5")) {
				LOGGER.error(responseText);
				throw new AzureAdClientException("Unexpected Azure AD Graph API server error");
			} else {
				throw new AzureAdClientException(
						new JSONObject(responseText).optJSONObject("odata.error").optJSONObject("message")
								.optString("value"));
			}
		} while (skipToken != null);

		return result;
	}

	@VisibleForTesting
	List<JSONArray> getAllUsersResponse(List<String> userGroups, String usersFilter) {
		List<JSONArray> jsonArrayList;

		if (CollectionUtils.isNotEmpty(userGroups)) {
			jsonArrayList = new ArrayList<>();

			for (JSONArray jsonArray : getAllGroupsResponse(buildUserGroupsFilter(userGroups))) {
				for (int i = 0, length = jsonArray.length(); i < length; i++) {
					String objectId = jsonArray.getJSONObject(i).optString("objectId");

					jsonArrayList.addAll(getGroupMembersResponse(objectId));
				}
			}
		} else {
			jsonArrayList = getAllUsersResponse(usersFilter);
		}

		return jsonArrayList;
	}

	private String buildUserGroupsFilter(List<String> userGroups) {
		return Joiner.on(" and ").join(CollectionUtils.collect(userGroups, new Transformer() {
			@Override
			public Object transform(Object input) {
				return "(displayName eq '" + input + "')";
			}
		}));
	}

	List<JSONArray> getAllUsersResponse(String usersFilter) {
		return submitQueryWithPagination(webTarget.path("users"), usersFilter);
	}

	@VisibleForTesting
	List<JSONArray> getUserGroupsResponse(final String userObjectId) {
		return submitQueryWithPagination(webTarget.path("users").path(userObjectId).path("$links").path("memberOf"), null);
	}

	@VisibleForTesting
	List<JSONArray> getAllGroupsResponse(String groupsFilter) {
		return submitQueryWithPagination(webTarget.path("groups"), groupsFilter);
	}

	@VisibleForTesting
	List<JSONArray> getGroupMembersResponse(final String groupObjectId) {
		return submitQueryWithPagination(webTarget.path("groups").path(groupObjectId).path("$links").path("members"), null);
	}

	@VisibleForTesting
	List<JSONArray> getGroupMemberOfResponse(final String groupObjectId) {
		return submitQueryWithPagination(webTarget.path("groups").path(groupObjectId).path("$links").path("memberOf"), null);
	}

	public JSONObject getObjectResponseByUrl(final String objectUrl) {
		if (cacheObjects.containsKey(objectUrl)) {
			return cacheObjects.get(objectUrl);
		}

		String responseText;

		acquireAccessToken();

		Response response = getObjectByUrl(objectUrl);

		if (response.getStatus() == HttpURLConnection.HTTP_UNAUTHORIZED) {
			refreshAccessToken();

			response = getObjectByUrl(objectUrl);
		}

		if (new Integer(response.getStatus()).toString().startsWith("5")) {
			try {
				Thread.sleep(5000);
			} catch (InterruptedException ignored) {
			}

			response = getObjectByUrl(objectUrl);
		}

		responseText = getResponseText(response);

		if (response.getStatus() == HttpURLConnection.HTTP_OK) {
			JSONObject responseObject = new JSONObject(responseText);
			cacheObjects.put(objectUrl, responseObject);
			return responseObject;
		} else if (new Integer(response.getStatus()).toString().startsWith("5")) {
			LOGGER.error(responseText);
			throw new AzureAdClientException("Unexpected Azure AD Graph API server error");
		} else {
			throw new AzureAdClientException(
					new JSONObject(responseText).optJSONObject("odata.error").optJSONObject("message").optString("value"));
		}
	}

	private Response getObjectByUrl(final String objectUrl) {
		return completeQueryBuilding(client.target(objectUrl), null).get();
	}

}
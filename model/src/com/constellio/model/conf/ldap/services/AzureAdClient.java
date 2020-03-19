package com.constellio.model.conf.ldap.services;

import com.constellio.data.utils.dev.Toggle;
import com.constellio.model.conf.ldap.config.LDAPServerConfiguration;
import com.constellio.model.conf.ldap.config.LDAPUserSyncConfiguration;
import com.constellio.model.conf.ldap.services.LDAPServicesException.CouldNotConnectUserToLDAP;
import com.constellio.model.conf.ldap.user.LDAPGroup;
import com.constellio.model.conf.ldap.user.LDAPUser;
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
import org.joda.time.DateTime;
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
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 */
public class AzureAdClient {

	public static class AzureAdClientException extends RuntimeException {

		public AzureAdClientException(String message) {
			super(message);
		}

		public AzureAdClientException(String message, Throwable cause) {
			super(message, cause);
		}

	}

	static class RequestHelper {

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

		public RequestHelper(final String tenantName, final String clientId, final String clientSecret) {
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

		private JSONObject getObjectResponseByUrl(final String objectUrl) {
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

	private static final Logger LOGGER = LoggerFactory.getLogger(AzureAdClient.class);

	// TODO : Use "graph.microsoft.com/v1.0" instead as recommended in https://blogs.msdn.microsoft.com/aadgraphteam/2016/07/08/microsoft-graph-or-azure-ad-graph/
	private static final String GRAPH_API_URL = "https://graph.windows.net/";

	private static final String GRAPH_API_VERSION = "1.6";

	private static final String AUTHORITY_BASE_URL = "https://login.microsoftonline.com/";

	private LDAPServerConfiguration ldapServerConfiguration;

	private LDAPUserSyncConfiguration ldapUserSyncConfiguration;

	public AzureAdClient(final LDAPServerConfiguration ldapServerConfiguration,
						 final LDAPUserSyncConfiguration ldapUserSyncConfiguration) {
		this.ldapServerConfiguration = ldapServerConfiguration;
		this.ldapUserSyncConfiguration = ldapUserSyncConfiguration;
	}

	public Set<String> getUserNameList() {
		LOGGER.info("Getting user name list - start");

		Set<String> results = new HashSet<>();

		RequestHelper requestHelper = new RequestHelper(
				ldapServerConfiguration.getTenantName(),
				ldapUserSyncConfiguration.getClientId(),
				ldapUserSyncConfiguration.getClientSecret());

		List<JSONArray> jsonArrayList = requestHelper
				.getAllUsersResponse(ldapUserSyncConfiguration.getUserGroups(), ldapUserSyncConfiguration.getUsersFilter());

		for (JSONArray jsonArray : jsonArrayList) {
			for (int i = 0, length = jsonArray.length(); i < length; i++) {
				String userName = jsonArray.getJSONObject(i).optString("userPrincipalName");

				if (ldapUserSyncConfiguration.isUserAccepted(userName)) {
					results.add(userName);
				}
			}
		}

		LOGGER.info("Getting user name list - end");

		return results;
	}

	public Set<String> getGroupNameList() {
		LOGGER.info("Getting group name list - start");

		Set<String> results = new HashSet<>();

		RequestHelper requestHelper = new RequestHelper(
				ldapServerConfiguration.getTenantName(),
				ldapUserSyncConfiguration.getClientId(),
				ldapUserSyncConfiguration.getClientSecret());

		for (JSONArray jsonArray : requestHelper.getAllGroupsResponse(ldapUserSyncConfiguration.getGroupsFilter())) {
			for (int i = 0, length = jsonArray.length(); i < length; i++) {
				String groupName = jsonArray.getJSONObject(i).optString("displayName");

				if (ldapUserSyncConfiguration.isGroupAccepted(groupName)) {
					results.add(groupName);
				}
			}
		}

		LOGGER.info("Getting group name list - end");

		return results;
	}

	public void getGroupsAndTheirUsers(final Map<String, LDAPGroup> ldapGroups, final Map<String, LDAPUser> ldapUsers) {
		LOGGER.info("Getting groups and their members - start");

		RequestHelper requestHelper = new RequestHelper(
				ldapServerConfiguration.getTenantName(),
				ldapUserSyncConfiguration.getClientId(),
				ldapUserSyncConfiguration.getClientSecret());

		int pageNum = 1;

		List<JSONArray> stuff = requestHelper.getAllGroupsResponse(ldapUserSyncConfiguration.getGroupsFilter());
		for (JSONArray groupsArray : stuff) {
			int groupsPageSize = groupsArray.length();

			LOGGER.info("Processing groups page " + pageNum++ + " having " + groupsPageSize + " items");

			for (int ig = 0; ig < groupsPageSize; ig++) {
				LDAPGroup ldapGroup = buildLDAPGroupFromJsonObject(groupsArray.getJSONObject(ig));

				if (ldapUserSyncConfiguration.isGroupAccepted(ldapGroup.getSimpleName())) {
					if (ldapGroups.containsKey(ldapGroup.getDistinguishedName())) {
						ldapGroup = ldapGroups.get(ldapGroup.getDistinguishedName());
					} else {
						ldapGroups.put(ldapGroup.getDistinguishedName(), ldapGroup);
					}

					for (JSONArray membersArray : requestHelper.getGroupMembersResponse(ldapGroup.getDistinguishedName())) {
						for (int im = 0, membersCount = membersArray.length(); im < membersCount; im++) {
							String objectUrl = membersArray.getJSONObject(im).optString("url");

							if (objectUrl.endsWith("Microsoft.DirectoryServices.User")) {
								JSONObject jsonObject = requestHelper.getObjectResponseByUrl(objectUrl);

								LDAPUser ldapUser = buildLDAPUserFromJsonObject(jsonObject);

								if (Toggle.IGNORE_CONFIGS_WHEN_SYNCHRONIZING_AZURE_RELATED_USERS_AND_GROUPS.isEnabled() || ldapUserSyncConfiguration.isUserAccepted(ldapUser.getName())) {
									if (ldapUsers.containsKey(ldapUser.getId())) {
										ldapUser = ldapUsers.get(ldapUser.getId());
									} else {
										ldapUsers.put(ldapUser.getId(), ldapUser);
									}

									ldapGroup.addUser(ldapUser.getId());
									ldapUser.addGroup(ldapGroup);
								}
							}
						}
					}
				}
			}
		}

		LOGGER.info("Getting groups and their members - end");
	}

	private LDAPGroup buildLDAPGroupFromJsonObject(JSONObject groupJsonObject) {
		String groupObjectId = groupJsonObject.optString("objectId");
		String groupDisplayName = groupJsonObject.optString("displayName");
		return new LDAPGroup(groupDisplayName, groupObjectId);
	}

	private LDAPUser buildLDAPUserFromJsonObject(JSONObject userJsonObject) {
		LDAPUser ldapUser = new LDAPUser();
		ldapUser.setId(userJsonObject.optString("objectId"));
		ldapUser.setName(userJsonObject.optString("mailNickname"));//not displayName
		ldapUser.setFamilyName(userJsonObject.optString("surname"));
		ldapUser.setGivenName(userJsonObject.optString("givenName"));
		//ldapUser.setEmail(userJsonObject.optString("email")); there mail but with several values instead we ll use userPrincipalName
		ldapUser.setEmail(userJsonObject.optString("userPrincipalName"));
		ldapUser.setEnabled(Boolean.valueOf(userJsonObject.optString("accountEnabled")));
		ldapUser.setLieuTravail(userJsonObject.optString("department"));
		ldapUser.setMsExchDelegateListBL(null); // TODO
		String refreshTokensValidFromDateTime = userJsonObject.optString("refreshTokensValidFromDateTime");
		if (!StringUtils.isEmpty(refreshTokensValidFromDateTime) && !"null".equals(refreshTokensValidFromDateTime)) {
			ldapUser.setLastLogon(new DateTime(refreshTokensValidFromDateTime).toDate());
		}

		return ldapUser;
	}

	public void getUsersAndTheirGroups(final Map<String, LDAPGroup> ldapGroups, final Map<String, LDAPUser> ldapUsers) {
		LOGGER.info("Getting users and their memberships - start");

		RequestHelper requestHelper = new RequestHelper(
				ldapServerConfiguration.getTenantName(),
				ldapUserSyncConfiguration.getClientId(),
				ldapUserSyncConfiguration.getClientSecret());

		int pageNum = 1;

		List<JSONArray> jsonArrayList = requestHelper
				.getAllUsersResponse(ldapUserSyncConfiguration.getUserGroups(), ldapUserSyncConfiguration.getUsersFilter());

		for (JSONArray userArray : jsonArrayList) {
			int groupsPageSize = userArray.length();

			LOGGER.info("Processing users page " + pageNum++ + " having " + groupsPageSize + " items");

			for (int iu = 0; iu < groupsPageSize; iu++) {
				JSONObject jsonObject = userArray.getJSONObject(iu);
				if (jsonObject.has("url")) {
					jsonObject = requestHelper.getObjectResponseByUrl(jsonObject.optString("url"));
				}

				LDAPUser ldapUser = buildLDAPUserFromJsonObject(jsonObject);

				if (ldapUserSyncConfiguration.isUserAccepted(ldapUser.getName())) {
					if (ldapUsers.containsKey(ldapUser.getId())) {
						ldapUser = ldapUsers.get(ldapUser.getId());
					} else {
						ldapUsers.put(ldapUser.getId(), ldapUser);
					}
					//}

					for (JSONArray membershipsArray : requestHelper.getUserGroupsResponse(ldapUser.getId())) {
						for (int im = 0, membershipsCount = membershipsArray.length(); im < membershipsCount; im++) {
							String objectUrl = membershipsArray.getJSONObject(im).optString("url");

							if (objectUrl.endsWith("Microsoft.DirectoryServices.Group")) {
								jsonObject = requestHelper.getObjectResponseByUrl(objectUrl);

								LDAPGroup ldapGroup = buildLDAPGroupFromJsonObject(jsonObject);

								if (Toggle.IGNORE_CONFIGS_WHEN_SYNCHRONIZING_AZURE_RELATED_USERS_AND_GROUPS.isEnabled() || ldapUserSyncConfiguration.isGroupAccepted(ldapGroup.getSimpleName())) {
									if (ldapGroups.containsKey(ldapGroup.getDistinguishedName())) {
										ldapGroup = ldapGroups.get(ldapGroup.getDistinguishedName());
									} else {
										ldapGroups.put(ldapGroup.getDistinguishedName(), ldapGroup);
									}

									ldapGroup.addUser(ldapUser.getId());
									ldapUser.addGroup(ldapGroup);
								}
							}
						}
					}
				}
			}
		}

		LOGGER.info("Getting users and their memberships - end");
	}

	public void authenticate(final String user, final String password)
			throws CouldNotConnectUserToLDAP {
		String authority = AUTHORITY_BASE_URL + ldapServerConfiguration.getTenantName();
		ExecutorService executorService = Executors.newSingleThreadExecutor();
		AuthenticationResult authenticationResult = null;

		try {
			AuthenticationContext authenticationContext = new AuthenticationContext(authority, true, executorService);
			Future<AuthenticationResult> authenticationResultFuture = authenticationContext.acquireToken(
					GRAPH_API_URL,
					ldapServerConfiguration.getClientId(),
					user,
					password,
					null);
			authenticationResult = authenticationResultFuture.get();
		} catch (MalformedURLException mue) {
			LOGGER.error("Malformed Azure AD authority URL " + authority);

			throw new CouldNotConnectUserToLDAP();
		} catch (ExecutionException ee) {
			LOGGER.error("Can't authenticate user " + user);

			throw new CouldNotConnectUserToLDAP();
		} catch (InterruptedException ignored) {
		} finally {
			executorService.shutdown();
		}

		if (authenticationResult == null) {
			throw new CouldNotConnectUserToLDAP();
		}
	}

}

package com.constellio.model.conf.ldap.services;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.model.conf.ldap.config.AzureADServerConfig;
import com.constellio.model.conf.ldap.config.LDAPServerConfiguration;
import com.constellio.model.conf.ldap.config.LDAPUserSyncConfiguration;
import com.constellio.model.conf.ldap.services.LDAPServicesException.CouldNotConnectUserToLDAP;
import com.constellio.model.conf.ldap.user.LDAPGroup;
import com.constellio.model.conf.ldap.user.LDAPUser;
import com.microsoft.aad.adal4j.AuthenticationContext;
import com.microsoft.aad.adal4j.AuthenticationResult;
import com.microsoft.aad.adal4j.ClientCredential;

public class AzureADServices implements LDAPServices {
	Logger LOGGER = LoggerFactory.getLogger(LDAPServicesImpl.class);

	@Override
	public void authenticateUser(LDAPServerConfiguration ldapServerConfiguration, String user, String password)
			throws CouldNotConnectUserToLDAP {
		String authority = ldapServerConfiguration.getAuthorityUrl() + ldapServerConfiguration.getTenantName();
		ExecutorService executorService = Executors.newSingleThreadExecutor();
		AuthenticationResult authenticationResult = null;

		try {
			AuthenticationContext authenticationContext = new AuthenticationContext(authority, true, executorService);
			Future<AuthenticationResult> authenticationResultFuture = authenticationContext
					.acquireToken(ldapServerConfiguration.getResource(),
							ldapServerConfiguration.getClientId(),
							user,
							password, null);
			authenticationResult = authenticationResultFuture.get();
		} catch (final MalformedURLException mue) {
			LOGGER.warn("Malformed Azure AD authority URL " + authority);
			throw new CouldNotConnectUserToLDAP(mue);
		} catch (final ExecutionException ee) {
			LOGGER.warn(ee.getMessage(), ee);
			throw new CouldNotConnectUserToLDAP(ee);
		} catch (final InterruptedException ignored) {
		} finally {
			executorService.shutdown();
		}

		if (authenticationResult == null) {
			throw new CouldNotConnectUserToLDAP();
		}
	}

	@Override
	public List<String> getTestSynchronisationUsersNames(final LDAPServerConfiguration ldapServerConfiguration,
			final LDAPUserSyncConfiguration ldapUserSyncConfiguration) {
		Set<String> results = new HashSet<>();

		String accessToken;
		try {
			accessToken = getAccessToken(ldapServerConfiguration, ldapUserSyncConfiguration);
		} catch (Throwable t) {
			LOGGER.error("Can't acquire an Azure AD token for client id " + ldapServerConfiguration.getClientId(), t);
			return new ArrayList<>(results);
		}

		Client client = ClientBuilder.newClient();
		Response response = client.target(AzureADServerConfig.GRAPH_API_URL + ldapServerConfiguration.getTenantName())
				.path("/users")
				.queryParam("api-version", AzureADServerConfig.GRAPH_API_VERSION)
				.request(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, accessToken)
				.get();
		String responseText = response.readEntity(String.class);
		if (response.getStatus() == HttpURLConnection.HTTP_OK) {
			JSONArray jsonArray = new JSONObject(responseText).getJSONArray("value");

			for (int i = 0, jsonArrayLength = jsonArray.length(); i < jsonArrayLength; i++) {
				try {
					results.add(jsonArray.getJSONObject(i).optString("userPrincipalName"));
				} catch (JSONException e) {
					// TODO
				}
			}
		} else {
			results.add(new JSONObject(responseText).optJSONObject("odata.error").optJSONObject("message").optString("value"));
		}

		CollectionUtils.filter(results, new Predicate() {
			@Override
			public boolean evaluate(Object object) {
				return ldapUserSyncConfiguration.isUserAccepted((String) object);
			}
		});

		return new ArrayList<>(results);
	}

	@Override
	public List<String> getTestSynchronisationGroups(final LDAPServerConfiguration ldapServerConfiguration,
			final LDAPUserSyncConfiguration ldapUserSyncConfiguration) {
		Set<String> results = new HashSet<>();

		String accessToken;
		try {
			accessToken = getAccessToken(ldapServerConfiguration, ldapUserSyncConfiguration);
		} catch (Throwable t) {
			LOGGER.error("Can't acquire an Azure AD token for client id " + ldapServerConfiguration.getClientId(), t);
			return new ArrayList<>(results);
		}

		Client client = ClientBuilder.newClient();
		Response response = client.target(AzureADServerConfig.GRAPH_API_URL + ldapServerConfiguration.getTenantName())
				.path("/groups")
				.queryParam("api-version", AzureADServerConfig.GRAPH_API_VERSION)
				.request(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, accessToken)
				.get();
		String responseText = response.readEntity(String.class);
		if (response.getStatus() == HttpURLConnection.HTTP_OK) {
			JSONArray jsonArray = new JSONObject(responseText).getJSONArray("value");

			for (int i = 0, jsonArrayLength = jsonArray.length(); i < jsonArrayLength; i++) {
				try {
					results.add(jsonArray.getJSONObject(i).optString("displayName"));
				} catch (JSONException e) {
					// TODO
				}
			}
		} else {
			results.add(new JSONObject(responseText).optJSONObject("odata.error").optJSONObject("message").optString("value"));
		}

		CollectionUtils.filter(results, new Predicate() {
			@Override
			public boolean evaluate(Object object) {
				return ldapUserSyncConfiguration.isGroupAccepted((String) object);
			}
		});

		return new ArrayList<>(results);
	}

	@Override
	public LDAPUsersAndGroups importUsersAndGroups(final LDAPServerConfiguration ldapServerConfiguration,
			final LDAPUserSyncConfiguration ldapUserSyncConfiguration, final String url) {
		Map<String, LDAPGroup> ldapGroups = new HashMap<>();
		Map<String, LDAPUser> ldapUsers = new HashMap<>();

		String accessToken;
		try {
			accessToken = getAccessToken(ldapServerConfiguration, ldapUserSyncConfiguration);
		} catch (Throwable t) {
			LOGGER.error("Can't acquire an Azure AD token for client id " + ldapServerConfiguration.getClientId(), t);
			return new LDAPUsersAndGroups(new HashSet<>(ldapUsers.values()), new HashSet<>(ldapGroups.values()));
		}

		Client client = ClientBuilder.newClient();

		WebTarget webTarget = client.target(AzureADServerConfig.GRAPH_API_URL + ldapServerConfiguration.getTenantName());
		Response response = getAllGroupsResponse(webTarget, accessToken);
		String responseText = response.readEntity(String.class);
		if (response.getStatus() == HttpURLConnection.HTTP_OK) {
			JSONArray groupsJsonArray = new JSONObject(responseText).getJSONArray("value");

			for (int i = 0, jsonArrayLength = groupsJsonArray.length(); i < jsonArrayLength; i++) {
				try {
					JSONObject groupJsonObject = groupsJsonArray.getJSONObject(i);
					LDAPGroup ldapGroup = createLDAPGroupFromJsonObject(groupJsonObject);

					if (ldapUserSyncConfiguration.isGroupAccepted(ldapGroup.getSimpleName())) {
						String groupObjectId = ldapGroup.getDistinguishedName();
						ldapGroups.put(groupObjectId, ldapGroup);

						response = getGroupMembersResponse(webTarget, groupObjectId, accessToken);
						responseText = response.readEntity(String.class);
						if (response.getStatus() == HttpURLConnection.HTTP_OK) {
							JSONArray groupMembersJsonArray = new JSONObject(responseText).getJSONArray("value");

							for (int j = 0, groupMembersJsonArrayLength = groupMembersJsonArray.length();
								 j < groupMembersJsonArrayLength; j++) {
								String groupMemberUrl = groupMembersJsonArray.getJSONObject(i).optString("url");
								if (groupMemberUrl.endsWith("Microsoft.DirectoryServices.User")) {
									response = getObjectResponseByUrl(client, groupMemberUrl, accessToken);
									responseText = response.readEntity(String.class);
									if (response.getStatus() == HttpURLConnection.HTTP_OK) {
										JSONObject groupUserJsonObject = new JSONObject(responseText);
										LDAPUser ldapUser = createLDAPUserFromJsonObject(groupUserJsonObject);
										if (ldapUserSyncConfiguration.isUserAccepted(ldapUser.getName())) {
											//FIXME ldapUser not added to answer!
											ldapUser.addGroup(ldapGroup);
										}
									}
								}
							}
						} else {
							LOGGER.error(new JSONObject(responseText).optJSONObject("odata.error").optJSONObject("message")
									.optString("value"));
						}
					}
				} catch (JSONException e) {
					// TODO
				}
			}
		} else {
			LOGGER.error(new JSONObject(responseText).optJSONObject("odata.error").optJSONObject("message").optString("value"));
		}

		response = getAllUsersResponse(webTarget, accessToken);
		responseText = response.readEntity(String.class);
		if (response.getStatus() == HttpURLConnection.HTTP_OK) {
			JSONArray usersJsonArray = new JSONObject(responseText).getJSONArray("value");

			for (int i = 0, jsonArrayLength = usersJsonArray.length(); i < jsonArrayLength; i++) {
				try {
					JSONObject userJsonObject = usersJsonArray.getJSONObject(i);
					LDAPUser ldapUser = createLDAPUserFromJsonObject(userJsonObject);
					ldapUsers.put(ldapUser.getId(), ldapUser);

					if (ldapUserSyncConfiguration.isUserAccepted(ldapUser.getName())) {

						String userObjectId = ldapUser.getId();

						response = getUserGroupsResponse(webTarget, userObjectId, accessToken);
						responseText = response.readEntity(String.class);
						if (response.getStatus() == HttpURLConnection.HTTP_OK) {
							JSONArray userGroupJsonArray = new JSONObject(responseText).getJSONArray("value");

							for (int j = 0;	 j < userGroupJsonArray.length(); j++) {
								String userGroupUrl = "";
								try{
									JSONObject object = userGroupJsonArray.getJSONObject(i);
									userGroupUrl = object.optString("url");
								}catch(JSONException e){
									//FIXME na b some groups are not added
									e.printStackTrace();
								}

								if (userGroupUrl.endsWith("Microsoft.DirectoryServices.Group")) {
									response = getObjectResponseByUrl(client, userGroupUrl, accessToken);
									responseText = response.readEntity(String.class);
									if (response.getStatus() == HttpURLConnection.HTTP_OK) {
										JSONObject userGroupJsonObject = new JSONObject(responseText);
										LDAPGroup ldapGroup = createLDAPGroupFromJsonObject(userGroupJsonObject);

										if (ldapUserSyncConfiguration.isGroupAccepted(ldapGroup.getSimpleName())) {
											Set<LDAPGroup> userGroups = new HashSet<>(ldapUser.getUserGroups());
											userGroups.add(ldapGroup);
											ldapUser.setUserGroups(new ArrayList<>(userGroups));
										}
									}
								}
							}
						} else {
							LOGGER.error(new JSONObject(responseText).optJSONObject("odata.error").optJSONObject("message")
									.optString("value"));
						}
					}
				} catch (JSONException e) {
					//FIXME
					e.printStackTrace();
				}
			}
		} else {
			LOGGER.error(new JSONObject(responseText).optJSONObject("odata.error").optJSONObject("message").optString("value"));
		}

		return new LDAPUsersAndGroups(new HashSet<>(ldapUsers.values()), new HashSet<>(ldapGroups.values()));
	}

	private Response getUserGroupsResponse(WebTarget webTarget, String userObjectId, String accessToken) {
		return webTarget
				.path("/users")
				.path(userObjectId)
				.path("$links/memberOf")
				.queryParam("api-version", AzureADServerConfig.GRAPH_API_VERSION)
				.request(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, accessToken)
				.get();
	}

	private Response getAllUsersResponse(WebTarget webTarget, String accessToken) {
		return webTarget
				.path("/users")
				.queryParam("api-version", AzureADServerConfig.GRAPH_API_VERSION)
				.request(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, accessToken)
				.get();
	}

	private Response getObjectResponseByUrl(Client client, String objectUrl, String accessToken) {
		return client.target(objectUrl)
				.queryParam("api-version", AzureADServerConfig.GRAPH_API_VERSION)
				.request(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, accessToken)
				.get();
	}

	private Response getAllGroupsResponse(WebTarget webTarget, String accessToken) {
		return webTarget
				.path("/groups")
				.queryParam("api-version", AzureADServerConfig.GRAPH_API_VERSION)
				.request(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, accessToken)
				.get();
	}

	private Response getGroupMembersResponse(WebTarget webTarget, String groupObjectId, String accessToken) {
		return webTarget
				.path("/groups")
				.path(groupObjectId)
				.path("$links/members")
				.queryParam("api-version", AzureADServerConfig.GRAPH_API_VERSION)
				.request(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, accessToken)
				.get();
	}

	private LDAPGroup createLDAPGroupFromJsonObject(JSONObject groupJsonObject) {
		String groupObjectId = groupJsonObject.optString("objectId");
		String groupDisplayName = groupJsonObject.optString("displayName");
		return new LDAPGroup(groupDisplayName, groupObjectId);
	}

	private LDAPUser createLDAPUserFromJsonObject(JSONObject userJsonObject) {
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
		ldapUser.setLastLogon(new DateTime(userJsonObject.optString("refreshTokensValidFromDateTime")).toDate());

		return ldapUser;
	}

	private String getAccessToken(LDAPServerConfiguration ldapServerConfiguration, LDAPUserSyncConfiguration synchConf)
			throws Throwable {
		String authority = ldapServerConfiguration.getAuthorityUrl() + ldapServerConfiguration.getTenantName();
		ExecutorService executorService = Executors.newSingleThreadExecutor();

		try {
			AuthenticationContext authenticationContext = new AuthenticationContext(authority, true, executorService);
			Future<AuthenticationResult> authenticationResultFuture = authenticationContext
					.acquireToken(ldapServerConfiguration.
									getResource(),
							new ClientCredential(synchConf.getClientId(),
									synchConf.getClientSecret()),
							null);
			AuthenticationResult authenticationResult = authenticationResultFuture.get();
			if (authenticationResult == null) {
				return null;
			} else {
				return authenticationResult.getAccessToken();
			}
		} finally {
			executorService.shutdown();
		}
	}

}

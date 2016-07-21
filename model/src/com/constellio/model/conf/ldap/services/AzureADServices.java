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
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.model.conf.ldap.config.AzureADServerConfig;
import com.constellio.model.conf.ldap.config.AzureADUserSynchConfig;
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
		Response response = webTarget
				.path("/groups")
				.queryParam("api-version", AzureADServerConfig.GRAPH_API_VERSION)
				.request(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, accessToken)
				.get();
		String responseText = response.readEntity(String.class);
		if (response.getStatus() == HttpURLConnection.HTTP_OK) {
			JSONArray groupsJsonArray = new JSONObject(responseText).getJSONArray("value");

			for (int i = 0, jsonArrayLength = groupsJsonArray.length(); i < jsonArrayLength; i++) {
				try {
					JSONObject groupJsonObject = groupsJsonArray.getJSONObject(i);
					String groupObjectId = groupJsonObject.optString("objectId");
					String groupDisplayName = groupJsonObject.optString("displayName");

					if (ldapUserSyncConfiguration.isGroupAccepted(groupDisplayName)) {
						LDAPGroup ldapGroup = new LDAPGroup(groupDisplayName, groupObjectId);
						ldapGroups.put(groupObjectId, ldapGroup);

						response = webTarget
								.path("/groups")
								.path(groupObjectId)
								.path("$links/members")
								.queryParam("api-version", AzureADServerConfig.GRAPH_API_VERSION)
								.request(MediaType.APPLICATION_JSON)
								.header(HttpHeaders.AUTHORIZATION, accessToken)
								.get();
						responseText = response.readEntity(String.class);
						if (response.getStatus() == HttpURLConnection.HTTP_OK) {
							JSONArray groupMembersJsonArray = new JSONObject(responseText).getJSONArray("value");

							for (int j = 0, groupMembersJsonArrayLength = groupMembersJsonArray.length();
								 j < groupMembersJsonArrayLength; j++) {
								String groupMemberUrl = groupMembersJsonArray.getJSONObject(i).optString("url");
								if (groupMemberUrl.endsWith("Microsoft.DirectoryServices.User")) {
									response = client.target(groupMemberUrl)
											.queryParam("api-version", AzureADServerConfig.GRAPH_API_VERSION)
											.request(MediaType.APPLICATION_JSON)
											.header(HttpHeaders.AUTHORIZATION, accessToken)
											.get();
									responseText = response.readEntity(String.class);
									if (response.getStatus() == HttpURLConnection.HTTP_OK) {
										JSONObject groupUserJsonObject = new JSONObject(responseText);

										LDAPUser ldapUser = new LDAPUser();
										ldapUser.addGroup(ldapGroup);
										ldapUser.setId(groupUserJsonObject.optString("userPrincipalName"));
										ldapUser.setName(groupUserJsonObject.optString("displayName"));
										ldapUser.setFamilyName(groupUserJsonObject.optString("surname"));
										ldapUser.setGivenName(groupUserJsonObject.optString("givenName"));
										ldapUser.setEmail(groupUserJsonObject.optString("email"));
										ldapUser.setEnabled(Boolean.valueOf(groupUserJsonObject.optString("accountEnabled")));
										ldapUser.setLieuTravail(groupUserJsonObject.optString("department"));
										ldapUser.setMsExchDelegateListBL(null); // TODO
										ldapUser.setLastLogon(
												new DateTime(groupUserJsonObject.optString("refreshTokensValidFromDateTime"))
														.toDate());
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

		response = webTarget
				.path("/users")
				.queryParam("api-version", AzureADServerConfig.GRAPH_API_VERSION)
				.request(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, accessToken)
				.get();
		responseText = response.readEntity(String.class);
		if (response.getStatus() == HttpURLConnection.HTTP_OK) {
			JSONArray usersJsonArray = new JSONObject(responseText).getJSONArray("value");

			for (int i = 0, jsonArrayLength = usersJsonArray.length(); i < jsonArrayLength; i++) {
				try {
					JSONObject userJsonObject = usersJsonArray.getJSONObject(i);

					String userObjectId = userJsonObject.optString("objectId");
					String userDisplayName = userJsonObject.optString("displayName");

					if (ldapUserSyncConfiguration.isUserAccepted(userDisplayName)) {
						LDAPUser ldapUser = new LDAPUser();
						ldapUsers.put(userObjectId, ldapUser);
						ldapUser.setId(userJsonObject.optString("objectId"));
						ldapUser.setEmail(userJsonObject.optString("userPrincipalName"));
						ldapUser.setName(userJsonObject.optString("mailNickname"));
						ldapUser.setFamilyName(userJsonObject.optString("surname"));
						ldapUser.setGivenName(userJsonObject.optString("givenName"));
						//ldapUser.setEmail(userJsonObject.optString("email")); there mail
						ldapUser.setEnabled(Boolean.valueOf(userJsonObject.optString("accountEnabled")));
						ldapUser.setLieuTravail(userJsonObject.optString("department"));
						ldapUser.setMsExchDelegateListBL(null); // TODO
						ldapUser.setLastLogon(new DateTime(userJsonObject.optString("refreshTokensValidFromDateTime")).toDate());

						response = webTarget
								.path("/users")
								.path(userObjectId)
								.path("$links/memberOf")
								.queryParam("api-version", AzureADServerConfig.GRAPH_API_VERSION)
								.request(MediaType.APPLICATION_JSON)
								.header(HttpHeaders.AUTHORIZATION, accessToken)
								.get();
						responseText = response.readEntity(String.class);
						if (response.getStatus() == HttpURLConnection.HTTP_OK) {
							JSONArray userGroupJsonArray = new JSONObject(responseText).getJSONArray("value");

							for (int j = 0, userGroupsJsonArrayLength = userGroupJsonArray.length();
								 j < userGroupsJsonArrayLength; j++) {
								String userGroupUrl = userGroupJsonArray.getJSONObject(i).optString("url");
								if (userGroupUrl.endsWith("Microsoft.DirectoryServices.Group")) {
									response = client.target(userGroupUrl)
											.queryParam("api-version", AzureADServerConfig.GRAPH_API_VERSION)
											.request(MediaType.APPLICATION_JSON)
											.header(HttpHeaders.AUTHORIZATION, accessToken)
											.get();
									responseText = response.readEntity(String.class);
									if (response.getStatus() == HttpURLConnection.HTTP_OK) {
										JSONObject userGroupJsonObject = new JSONObject(responseText);

										LDAPGroup ldapGroup = new LDAPGroup(userGroupJsonObject.optString("displayName"),
												userGroupJsonObject.optString("objectId"));
										Set<LDAPGroup> userGroups = new HashSet<>(ldapUser.getUserGroups());
										userGroups.add(ldapGroup);
										ldapUser.setUserGroups(new ArrayList<>(userGroups));
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

		return new LDAPUsersAndGroups(new HashSet<>(ldapUsers.values()), new HashSet<>(ldapGroups.values()));
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

package com.constellio.model.conf.ldap.services;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashSet;
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

import com.constellio.model.conf.ldap.config.LDAPServerConfiguration;
import com.constellio.model.conf.ldap.config.LDAPUserSyncConfiguration;
import com.constellio.model.conf.ldap.services.LDAPServicesException.CouldNotConnectUserToLDAP;
import com.constellio.model.conf.ldap.user.LDAPGroup;
import com.constellio.model.conf.ldap.user.LDAPUser;
import com.microsoft.aad.adal4j.AuthenticationContext;
import com.microsoft.aad.adal4j.AuthenticationResult;
import com.microsoft.aad.adal4j.ClientCredential;

/**
 */
public class AzureAdClient implements AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureAdClient.class);

    private static final String GRAPH_API_URL = "https://graph.windows.net/";

    private static final String GRAPH_API_VERSION = "1.6";

    private static final String AUTHORITY_BASE_URL = "https://login.microsoftonline.com/";

    private LDAPServerConfiguration ldapServerConfiguration;

    private LDAPUserSyncConfiguration ldapUserSyncConfiguration;

    private Client client;

    private WebTarget webTarget;

    private String accessToken;

    public AzureAdClient(final LDAPServerConfiguration ldapServerConfiguration, final LDAPUserSyncConfiguration ldapUserSyncConfiguration) {
        this.ldapServerConfiguration = ldapServerConfiguration;

        this.ldapUserSyncConfiguration = ldapUserSyncConfiguration;
    }

    public void init() {
        client = ClientBuilder.newClient();

        webTarget = client.target(GRAPH_API_URL + ldapServerConfiguration.getTenantName());

        accessToken = getAccessToken();
    }

    private String getAccessToken() {
        final String authority = AUTHORITY_BASE_URL + ldapServerConfiguration.getTenantName();
        final ExecutorService executorService = Executors.newSingleThreadExecutor();

        try {
            final AuthenticationContext authenticationContext = new AuthenticationContext(authority, true, executorService);

            final Future<AuthenticationResult> authenticationResultFuture = authenticationContext.acquireToken(
                    GRAPH_API_URL,
                    new ClientCredential(
                            ldapUserSyncConfiguration.getClientId(),
                            ldapUserSyncConfiguration.getClientSecret()
                    ),
                    null
            );

            final AuthenticationResult authenticationResult = authenticationResultFuture.get();

            if (authenticationResult == null) {
                return null;
            } else {
                return authenticationResult.getAccessToken();
            }
        } catch (final MalformedURLException mue) {
            LOGGER.error("Malformed Azure AD authority URL " + authority);
        } catch (final ExecutionException ee) {
            LOGGER.error("Can't acquire an Azure AD token for client " + ldapUserSyncConfiguration.getClientId() + " with the provided secret key");
        } catch (final InterruptedException ignored) {
        } finally {
            executorService.shutdown();
        }

        return null;
    }

    public Set<String> getUserNameList() {
        final Set<String> results = new HashSet<>();

        if (accessToken != null) {
            final Response response = getAllUsersResponse();

            final String responseText = getResponseText(response);

            if (response.getStatus() == HttpURLConnection.HTTP_OK) {
                final JSONArray jsonArray = new JSONObject(responseText).getJSONArray("value");

                for (int userIndex = 0, jsonArrayLength = jsonArray.length(); userIndex < jsonArrayLength; userIndex++) {
                    try {
                        results.add(jsonArray.getJSONObject(userIndex).optString("userPrincipalName"));
                    } catch (final JSONException e) {
                        LOGGER.error("Error in processing user list at index " + userIndex, e);
                    }
                }
            } else {
                try {
                    results.add(new JSONObject(responseText).optJSONObject("odata.error").optJSONObject("message").optString("value"));
                } catch (final JSONException e) {
                    LOGGER.error(responseText);
                    LOGGER.error("Error reading response error message", e);
                }
            }

            CollectionUtils.filter(results, new Predicate() {
                @Override
                public boolean evaluate(Object object) {
                    return ldapUserSyncConfiguration.isUserAccepted((String) object);
                }
            });
        }

        return results;
    }

    private String getResponseText(final Response response) {
        return response.readEntity(String.class).replace("\uFEFF", "");
    }

    private Response getAllUsersResponse() {
        return webTarget
                .path("/users")
                .queryParam("api-version", GRAPH_API_VERSION)
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .get();
    }

    public Set<String> getGroupNameList() {
        Set<String> results = new HashSet<>();

        if (accessToken != null) {
            final Response response = getAllGroupsResponse();

            final String responseText = getResponseText(response);

            if (response.getStatus() == HttpURLConnection.HTTP_OK) {
                final JSONArray jsonArray = new JSONObject(responseText).getJSONArray("value");

                for (int groupIndex = 0, jsonArrayLength = jsonArray.length(); groupIndex < jsonArrayLength; groupIndex++) {
                    try {
                        results.add(jsonArray.getJSONObject(groupIndex).optString("displayName"));
                    } catch (final JSONException e) {
                        LOGGER.error("Error in processing group list at index " + groupIndex, e);
                    }
                }
            } else {
                try {
                    results.add(new JSONObject(responseText).optJSONObject("odata.error").optJSONObject("message").optString("value"));
                } catch (final JSONException e) {
                    LOGGER.error(responseText);
                    LOGGER.error("Error in reading response error message", e);
                }
            }

            CollectionUtils.filter(results, new Predicate() {
                @Override
                public boolean evaluate(Object object) {
                    return ldapUserSyncConfiguration.isGroupAccepted((String) object);
                }
            });
        }

        return results;
    }

    private Response getAllGroupsResponse() {
        return webTarget
                .path("/groups")
                .queryParam("api-version", GRAPH_API_VERSION)
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .get();
    }

    public void getGroupsAndTheirUsers(final Map<String, LDAPGroup> ldapGroups, final Map<String, LDAPUser> ldapUsers) {
        if (accessToken != null) {
            Response response = getAllGroupsResponse();

            String responseText = getResponseText(response);
            if (response.getStatus() == HttpURLConnection.HTTP_OK) {
                final JSONArray groupsJsonArray = new JSONObject(responseText).getJSONArray("value");

                for (int groupIndex = 0, groupsJsonArrayLength = groupsJsonArray.length(); groupIndex < groupsJsonArrayLength; groupIndex++) {
                    try {
                        LDAPGroup ldapGroup = buildLDAPGroupFromJsonObject(groupsJsonArray.getJSONObject(groupIndex));

                        if (ldapUserSyncConfiguration.isGroupAccepted(ldapGroup.getSimpleName())) {
                            final String groupObjectId = ldapGroup.getDistinguishedName();

                            if (ldapGroups.containsKey(groupObjectId)) {
                                ldapGroup = ldapGroups.get(groupObjectId);
                            } else {
                                ldapGroups.put(groupObjectId, ldapGroup);
                            }

                            response = getGroupMembersResponse(groupObjectId);

                            responseText = getResponseText(response);
                            if (response.getStatus() == HttpURLConnection.HTTP_OK) {
                                final JSONArray groupMembersJsonArray = new JSONObject(responseText).getJSONArray("value");

                                for (int userIndex = 0, groupMembersJsonArrayLength = groupMembersJsonArray.length(); userIndex < groupMembersJsonArrayLength; userIndex++) {
                                    final String groupMemberUrl = groupMembersJsonArray.getJSONObject(userIndex).optString("url");

                                    if (groupMemberUrl.endsWith("Microsoft.DirectoryServices.User")) {
                                        response = getObjectResponseByUrl(groupMemberUrl);
                                        responseText = getResponseText(response);
                                        if (response.getStatus() == HttpURLConnection.HTTP_OK) {
                                            final JSONObject groupUserJsonObject = new JSONObject(responseText);
                                            LDAPUser ldapUser = buildLDAPUserFromJsonObject(groupUserJsonObject);
                                            if (ldapUserSyncConfiguration.isUserAccepted(ldapUser.getName())) {
                                                if (ldapUsers.containsKey(ldapUser.getId())) {
                                                    ldapUser = ldapUsers.get(ldapUser.getId());
                                                } else {
                                                    ldapUsers.put(ldapUser.getId(), ldapUser);
                                                }

                                                ldapUser.addGroup(ldapGroup);
                                            }
                                        } else {
                                            LOGGER.error("can't read member at index " + userIndex + " of group " + groupObjectId);
                                        }
                                    }
                                }
                            } else {
                                logResponseErrorMessage(responseText);
                            }
                        }
                    } catch (final JSONException e) {
                        LOGGER.error("Error in processing group list at index " + groupIndex, e);
                    }
                }
            } else {
                logResponseErrorMessage(responseText);
            }
        }
    }

    private LDAPGroup buildLDAPGroupFromJsonObject(JSONObject groupJsonObject) {
        final String groupObjectId = groupJsonObject.optString("objectId");
        final String groupDisplayName = groupJsonObject.optString("displayName");
        return new LDAPGroup(groupDisplayName, groupObjectId);
    }

    private Response getGroupMembersResponse(String groupObjectId) {
        return webTarget
                .path("/groups")
                .path(groupObjectId)
                .path("$links/members")
                .queryParam("api-version", GRAPH_API_VERSION)
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .get();
    }

    private Response getObjectResponseByUrl(String objectUrl) {
        return client.target(objectUrl)
                .queryParam("api-version", GRAPH_API_VERSION)
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .get();
    }

    private LDAPUser buildLDAPUserFromJsonObject(JSONObject userJsonObject) {
        final LDAPUser ldapUser = new LDAPUser();
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


    private void logResponseErrorMessage(String responseText) {
        try {
            LOGGER.error(new JSONObject(responseText).optJSONObject("odata.error").optJSONObject("message").optString("value"));
        } catch (final JSONException e) {
            LOGGER.error(responseText);
            LOGGER.error("Error in reading response error message", e);
        }
    }

    public void getUsersAndTheirGroups(final Map<String, LDAPGroup> ldapGroups, final Map<String, LDAPUser> ldapUsers) {
        if (accessToken != null) {
            Response response = getAllUsersResponse();

            String responseText = getResponseText(response);
            if (response.getStatus() == HttpURLConnection.HTTP_OK) {
                final JSONArray usersJsonArray = new JSONObject(responseText).getJSONArray("value");

                for (int userIndex = 0, usersJsonArrayLength = usersJsonArray.length(); userIndex < usersJsonArrayLength; userIndex++) {
                    try {
                        LDAPUser ldapUser = buildLDAPUserFromJsonObject(usersJsonArray.getJSONObject(userIndex));

                        if (ldapUserSyncConfiguration.isUserAccepted(ldapUser.getName())) {
                            final String userObjectId = ldapUser.getId();

                            if (ldapUsers.containsKey(userObjectId)) {
                                ldapUser = ldapUsers.get(userObjectId);
                            } else {
                                ldapUsers.put(userObjectId, ldapUser);
                            }

                            response = getUserGroupsResponse(userObjectId);

                            responseText = getResponseText(response);
                            if (response.getStatus() == HttpURLConnection.HTTP_OK) {
                                final JSONArray userGroupJsonArray = new JSONObject(responseText).getJSONArray("value");

                                for (int groupIndex = 0, userGroupJsonArrayLength = userGroupJsonArray.length(); groupIndex < userGroupJsonArrayLength; groupIndex++) {
                                    final String userGroupUrl = userGroupJsonArray.getJSONObject(groupIndex).optString("url");

                                    if (userGroupUrl.endsWith("Microsoft.DirectoryServices.Group")) {
                                        response = getObjectResponseByUrl(userGroupUrl);
                                        responseText = getResponseText(response);
                                        if (response.getStatus() == HttpURLConnection.HTTP_OK) {
                                            final JSONObject userGroupJsonObject = new JSONObject(responseText);
                                            LDAPGroup ldapGroup = buildLDAPGroupFromJsonObject(userGroupJsonObject);

                                            if (ldapUserSyncConfiguration.isGroupAccepted(ldapGroup.getSimpleName())) {
                                                if (ldapGroups.containsKey(ldapGroup.getDistinguishedName())) {
                                                    ldapGroup = ldapGroups.get(ldapGroup.getDistinguishedName());
                                                } else {
                                                    ldapGroups.put(ldapGroup.getDistinguishedName(), ldapGroup);
                                                }

                                                final Set<LDAPGroup> userGroups = new HashSet<>(ldapUser.getUserGroups());
                                                userGroups.add(ldapGroup);
                                                ldapUser.setUserGroups(new ArrayList<>(userGroups));
                                            }
                                        } else {
                                            LOGGER.error("can't read group at index " + groupIndex + " of user " + userObjectId);
                                        }
                                    }
                                }
                            } else {
                                logResponseErrorMessage(responseText);
                            }
                        }
                    } catch (final JSONException e) {
                        LOGGER.error("Error in processing user list at index " + userIndex, e);
                    }
                }
            } else {
                logResponseErrorMessage(responseText);
            }
        }
    }

    private Response getUserGroupsResponse(final String userObjectId) {
        return webTarget
                .path("/users")
                .path(userObjectId)
                .path("$links/memberOf")
                .queryParam("api-version", GRAPH_API_VERSION)
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .get();
    }

    public void authenticiate(final String user, final String password) throws CouldNotConnectUserToLDAP {
        final String authority = AUTHORITY_BASE_URL + ldapServerConfiguration.getTenantName();
        final ExecutorService executorService = Executors.newSingleThreadExecutor();
        AuthenticationResult authenticationResult = null;

        try {
            final AuthenticationContext authenticationContext = new AuthenticationContext(authority, true, executorService);
            final Future<AuthenticationResult> authenticationResultFuture = authenticationContext.acquireToken(
                    GRAPH_API_URL,
                    ldapServerConfiguration.getClientId(),
                    user,
                    password,
                    null);
            authenticationResult = authenticationResultFuture.get();
        } catch (final MalformedURLException mue) {
            LOGGER.error("Malformed Azure AD authority URL " + authority);

            throw new LDAPServicesException.CouldNotConnectUserToLDAP();
        } catch (final ExecutionException ee) {
            LOGGER.error("Can't authenticate user " + user);

            throw new LDAPServicesException.CouldNotConnectUserToLDAP();
        } catch (final InterruptedException ignored) {
        } finally {
            executorService.shutdown();
        }

        if (authenticationResult == null) {
            throw new LDAPServicesException.CouldNotConnectUserToLDAP();
        }
    }

    @Override
    public void close() {
        if (client != null) {
            client.close();
        }
    }

}

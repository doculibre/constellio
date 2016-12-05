package com.constellio.model.conf.ldap.services;

import com.constellio.model.conf.ldap.config.LDAPServerConfiguration;
import com.constellio.model.conf.ldap.config.LDAPUserSyncConfiguration;
import com.constellio.model.conf.ldap.services.LDAPServicesException.CouldNotConnectUserToLDAP;
import com.constellio.model.conf.ldap.user.LDAPGroup;
import com.constellio.model.conf.ldap.user.LDAPUser;

import com.google.common.annotations.VisibleForTesting;
import com.microsoft.aad.adal4j.AuthenticationContext;
import com.microsoft.aad.adal4j.AuthenticationResult;
import com.microsoft.aad.adal4j.ClientCredential;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
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
public class AzureAdClient implements AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureAdClient.class);

    private static final String GRAPH_API_URL = "https://graph.windows.net/";

    private static final String GRAPH_API_VERSION = "1.6";

    private static final String AUTHORITY_BASE_URL = "https://login.microsoftonline.com/";

    @VisibleForTesting
    static int maxResults = 100;

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
        String authority = AUTHORITY_BASE_URL + ldapServerConfiguration.getTenantName();
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        try {
            AuthenticationContext authenticationContext = new AuthenticationContext(authority, true, executorService);

            Future<AuthenticationResult> authenticationResultFuture = authenticationContext.acquireToken(
                    GRAPH_API_URL,
                    new ClientCredential(
                            ldapUserSyncConfiguration.getClientId(),
                            ldapUserSyncConfiguration.getClientSecret()
                    ),
                    null
            );

            AuthenticationResult authenticationResult = authenticationResultFuture.get();

            if (authenticationResult == null) {
                return null;
            } else {
                return authenticationResult.getAccessToken();
            }
        } catch (MalformedURLException mue) {
            LOGGER.error("Malformed Azure AD authority URL " + authority);
        } catch (ExecutionException ee) {
            LOGGER.error("Can't acquire an Azure AD token for client " + ldapUserSyncConfiguration.getClientId() + " with the provided secret key");
        } catch (InterruptedException ignored) {
        } finally {
            executorService.shutdown();
        }

        return null;
    }

    public Set<String> getUserNameList() {
        LOGGER.info("Getting user name list - start");

        Set<String> results = new HashSet<>();

        if (accessToken != null) {
            Response response = getAllUsersResponse("");

            while (response != null) {
                String responseText = getResponseText(response);

                String skipToken = getSkipToken(responseText);

                if (response.getStatus() == HttpURLConnection.HTTP_OK) {
                    JSONArray jsonArray = new JSONObject(responseText).getJSONArray("value");

                    for (int i = 0, jsonArrayLength = jsonArray.length(); i < jsonArrayLength; i++) {
                        try {
                            results.add(jsonArray.getJSONObject(i).optString("userPrincipalName"));
                        } catch (JSONException e) {
                            LOGGER.error("Error in processing user list response at position " + i, e);
                        }
                    }

                    response = getAllUsersResponse(skipToken);
                } else {
                    try {
                        results.add(getResponseErrorMessage(responseText));
                    } catch (JSONException e) {
                        LOGGER.error(responseText);
                        LOGGER.error("Error reading response error message", e);
                    }

                    break;
                }
            }

            CollectionUtils.filter(results, new Predicate() {
                @Override
                public boolean evaluate(Object object) {
                    return ldapUserSyncConfiguration.isUserAccepted((String) object);
                }
            });
        }

        LOGGER.info("Getting user name list - end");

        return results;
    }

    @VisibleForTesting
    Response getAllUsersResponse(final String skipToken) {
        if (skipToken == null) {
            return null;
        }

        WebTarget webTarget = this.webTarget
                .path("/users")
                .queryParam("api-version", GRAPH_API_VERSION)
                .queryParam("$top", maxResults);

        if ("".equals(skipToken)) {
            return webTarget
                    .request(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, accessToken)
                    .get();
        }

        return webTarget
                .queryParam("$skiptoken", skipToken)
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .get();
    }

    private String getResponseText(final Response response) {
        return response.readEntity(String.class).replace("\uFEFF", "");
    }

    private String getResponseErrorMessage(final String responseText) {
        return new JSONObject(responseText).optJSONObject("odata.error").optJSONObject("message").optString("value");
    }

    public Set<String> getGroupNameList() {
        LOGGER.info("Getting group name list - start");

        Set<String> results = new HashSet<>();

        if (accessToken != null) {
            Response response = getAllGroupsResponse("");

            while (response != null) {
                String responseText = getResponseText(response);

                String skipToken = getSkipToken(responseText);

                if (response.getStatus() == HttpURLConnection.HTTP_OK) {
                    JSONArray jsonArray = new JSONObject(responseText).getJSONArray("value");

                    for (int i = 0, jsonArrayLength = jsonArray.length(); i < jsonArrayLength; i++) {
                        try {
                            results.add(jsonArray.getJSONObject(i).optString("displayName"));
                        } catch (JSONException e) {
                            LOGGER.error("Error in processing group list response at position " + i, e);
                        }
                    }

                    response = getAllGroupsResponse(skipToken);
                } else {
                    try {
                        results.add(getResponseErrorMessage(responseText));
                    } catch (JSONException e) {
                        LOGGER.error(responseText);
                        LOGGER.error("Error in reading response error message", e);
                    }

                    break;
                }
            }

            CollectionUtils.filter(results, new Predicate() {
                @Override
                public boolean evaluate(Object object) {
                    return ldapUserSyncConfiguration.isGroupAccepted((String) object);
                }
            });
        }

        LOGGER.info("Getting group name list - end");

        return results;
    }

    @VisibleForTesting
    Response getAllGroupsResponse(final String skipToken) {
        if (skipToken == null) {
            return null;
        }

        WebTarget webTarget = this.webTarget
                .path("/groups")
                .queryParam("api-version", GRAPH_API_VERSION)
                .queryParam("$top", maxResults);

        if ("".equals(skipToken)) {
            return webTarget
                    .request(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, accessToken)
                    .get();
        }

        return webTarget
                .queryParam("$skiptoken", skipToken)
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .get();
    }

    public void getGroupsAndTheirUsers(final Map<String, LDAPGroup> ldapGroups, final Map<String, LDAPUser> ldapUsers) {
        LOGGER.info("Getting groups and their members - start");

        if (accessToken != null) {
            Response response = getAllGroupsResponse("");

            int pageNum = 1;

            while (response != null) {
                String responseText = getResponseText(response);

                String skipToken = getSkipToken(responseText);

                if (response.getStatus() == HttpURLConnection.HTTP_OK) {
                    JSONArray groupsArray = new JSONObject(responseText).getJSONArray("value");

                    for (int i = 0, groupsArrayLength = groupsArray.length(); i < groupsArrayLength; i++) {
                        LOGGER.info("Processing group " + (i + 1) + "/" + groupsArrayLength + " of group page " + pageNum);

                        try {
                            LDAPGroup ldapGroup = buildLDAPGroupFromJsonObject(groupsArray.getJSONObject(i));

                            if (ldapUserSyncConfiguration.isGroupAccepted(ldapGroup.getSimpleName())) {
                                String groupObjectId = ldapGroup.getDistinguishedName();

                                if (!ldapGroups.containsKey(groupObjectId)) {
                                    ldapGroups.put(groupObjectId, ldapGroup);
                                }
                            }
                        } catch (JSONException e) {
                            LOGGER.error("Error in processing group list response at position " + i + " in page " + pageNum, e);
                        }
                    }

                    response = getAllGroupsResponse(skipToken);

                    pageNum++;
                } else {
                    logResponseErrorMessage(responseText);

                    break;
                }
            }

            getGroupMembers(ldapGroups, ldapUsers);
        }

        LOGGER.info("Getting groups and their members - end");
    }

    private String getSkipToken(final String responseText) {
        if (responseText.contains("odata.nextLink")) {
            for (String element : new JSONObject(responseText).getString("odata.nextLink").split("\\?")) {
                if (element.startsWith("$skiptoken")) {
                    return element.split("=")[1];
                }
            }

            return null;
        } else {
            return null;
        }
    }

    private LDAPGroup buildLDAPGroupFromJsonObject(JSONObject groupJsonObject) {
        String groupObjectId = groupJsonObject.optString("objectId");
        String groupDisplayName = groupJsonObject.optString("displayName");
        return new LDAPGroup(groupDisplayName, groupObjectId);
    }

    private void getGroupMembers(final Map<String, LDAPGroup> ldapGroups, final Map<String, LDAPUser> ldapUsers) {
        for (LDAPGroup ldapGroup : ldapGroups.values()) {
            Response response = getGroupMembersResponse(ldapGroup.getDistinguishedName(), "");

            int pageNum = 1;

            while (response != null) {
                String responseText = getResponseText(response);

                String skipToken = getSkipToken(responseText);

                if (response.getStatus() == HttpURLConnection.HTTP_OK) {
                    JSONArray membersArray = new JSONObject(responseText).getJSONArray("value");

                    for (int i = 0, membersArrayLength = membersArray.length(); i < membersArrayLength; i++) {
                        LOGGER.info("Processing group member " + (i + 1) + "/" + membersArrayLength + " of " + ldapGroup.getDistinguishedName() + " group page " + pageNum);

                        try {
                            String groupMemberUrl = membersArray.getJSONObject(i).optString("url");

                            if (groupMemberUrl.endsWith("Microsoft.DirectoryServices.User")) {
                                response = getObjectResponseByUrl(groupMemberUrl);
                                responseText = getResponseText(response);

                                if (response.getStatus() == HttpURLConnection.HTTP_OK) {
                                    JSONObject memberJsonObject = new JSONObject(responseText);
                                    LDAPUser ldapUser = buildLDAPUserFromJsonObject(memberJsonObject);
                                    if (ldapUserSyncConfiguration.isUserAccepted(ldapUser.getName())) {
                                        if (!ldapUsers.containsKey(ldapUser.getId())) {
                                            ldapUsers.put(ldapUser.getId(), ldapUser);
                                        }

                                        ldapUser.addGroup(ldapGroup);
                                        ldapGroup.addUser(ldapUser.getId());
                                    }
                                } else {
                                    LOGGER.error("can't read members at position " + i + " of group " + ldapGroup.getDistinguishedName());
                                }
                            }
                        } catch (JSONException e) {
                            LOGGER.error("Error in processing members list response at position " + i, e);
                        }
                    }

                    response = getGroupMembersResponse(ldapGroup.getDistinguishedName(), skipToken);

                    pageNum++;
                } else {
                    logResponseErrorMessage(responseText);

                    break;
                }
            }
        }
    }

    @VisibleForTesting
    Response getGroupMembersResponse(final String groupObjectId, final String skipToken) {
        if (skipToken == null) {
            return null;
        }

        WebTarget webTarget = this.webTarget
                .path("/groups")
                .path(groupObjectId)
                .path("$links/members")
                .queryParam("api-version", GRAPH_API_VERSION)
                .queryParam("$top", maxResults);

        if ("".equals(skipToken)) {
            return webTarget
                    .request(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, accessToken)
                    .get();
        }

        return webTarget
                .queryParam("$skiptoken", skipToken)
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


    private void logResponseErrorMessage(String responseText) {
        try {
            LOGGER.error(getResponseErrorMessage(responseText));
        } catch (JSONException e) {
            LOGGER.error(responseText);
            LOGGER.error("Error in reading response error message", e);
        }
    }

    public void getUsersAndTheirGroups(final Map<String, LDAPGroup> ldapGroups, final Map<String, LDAPUser> ldapUsers) {
        LOGGER.info("Getting users and their memberships - start");

        if (accessToken != null) {
            Response response = getAllUsersResponse("");

            int pageNum = 1;

            while (response != null) {
                String responseText = getResponseText(response);

                String skipToken = getSkipToken(responseText);

                if (response.getStatus() == HttpURLConnection.HTTP_OK) {
                    JSONArray usersArray = new JSONObject(responseText).getJSONArray("value");

                    for (int i = 0, usersArrayLength = usersArray.length(); i < usersArrayLength; i++) {
                        LOGGER.info("Processing user " + (i + 1) + "/" + usersArrayLength + " of user page " + pageNum);

                        try {
                            LDAPUser ldapUser = buildLDAPUserFromJsonObject(usersArray.getJSONObject(i));

                            if (ldapUserSyncConfiguration.isUserAccepted(ldapUser.getName())) {
                                String userObjectId = ldapUser.getId();

                                if (!ldapUsers.containsKey(userObjectId)) {
                                    ldapUsers.put(userObjectId, ldapUser);
                                }
                            }
                        } catch (JSONException e) {
                            LOGGER.error("Error in processing user list response at position " + i, e);
                        }
                    }

                    response = getAllUsersResponse(skipToken);

                    pageNum++;
                } else {
                    logResponseErrorMessage(responseText);

                    break;
                }
            }

            getUserGroups(ldapGroups, ldapUsers);
        }

        LOGGER.info("Getting users and their memberships - end");
    }

    private void getUserGroups(final Map<String, LDAPGroup> ldapGroups, final Map<String, LDAPUser> ldapUsers) {
        for (LDAPUser ldapUser : ldapUsers.values()) {
            Response response = getUserGroupsResponse(ldapUser.getId(), "");

            int pageNum = 1;

            while (response != null) {
                String responseText = getResponseText(response);

                String skipToken = getSkipToken(responseText);

                if (response.getStatus() == HttpURLConnection.HTTP_OK) {
                    JSONArray groupsArray = new JSONObject(responseText).getJSONArray("value");

                    for (int i = 0, groupsArrayLength = groupsArray.length(); i < groupsArrayLength; i++) {
                        LOGGER.info("Processing user group " + (i + 1) + "/" + groupsArrayLength + " of " + ldapUser.getId() + " user page " + pageNum);

                        try {
                            String groupUrl = groupsArray.getJSONObject(i).optString("url");

                            if (groupUrl.endsWith("Microsoft.DirectoryServices.Group")) {
                                response = getObjectResponseByUrl(groupUrl);
                                responseText = getResponseText(response);
                                if (response.getStatus() == HttpURLConnection.HTTP_OK) {
                                    JSONObject groupJsonObject = new JSONObject(responseText);
                                    LDAPGroup ldapGroup = buildLDAPGroupFromJsonObject(groupJsonObject);

                                    if (ldapUserSyncConfiguration.isGroupAccepted(ldapGroup.getSimpleName())) {
                                        if (!ldapGroups.containsKey(ldapGroup.getDistinguishedName())) {
                                            ldapGroups.put(ldapGroup.getDistinguishedName(), ldapGroup);
                                        }

                                        Set<LDAPGroup> userGroups = new HashSet<>(ldapUser.getUserGroups());
                                        userGroups.add(ldapGroup);
                                        ldapUser.setUserGroups(new ArrayList<>(userGroups));
                                        ldapGroup.addUser(ldapUser.getId());
                                    }
                                } else {
                                    LOGGER.error("can't read group at position " + i + " of user " + ldapUser.getId());
                                }
                            }
                        } catch (JSONException e) {
                            LOGGER.error("Error in processing group list response at position " + i, e);
                        }
                    }

                    response = getUserGroupsResponse(ldapUser.getId(), skipToken);

                    pageNum++;
                } else {
                    logResponseErrorMessage(responseText);

                    break;
                }
            }
        }
    }

    @VisibleForTesting
    Response getUserGroupsResponse(final String userObjectId, final String skipToken) {
        if (skipToken == null) {
            return null;
        }

        WebTarget webTarget = this.webTarget
                .path("/users")
                .path(userObjectId)
                .path("$links/memberOf")
                .queryParam("api-version", GRAPH_API_VERSION)
                .queryParam("$top", maxResults);

        if ("".equals(skipToken)) {
            return webTarget
                    .request(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, accessToken)
                    .get();
        }

        return webTarget
                .queryParam("$skiptoken", skipToken)
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .get();
    }

    public void authenticiate(final String user, final String password) throws CouldNotConnectUserToLDAP {
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

            throw new LDAPServicesException.CouldNotConnectUserToLDAP();
        } catch (ExecutionException ee) {
            LOGGER.error("Can't authenticate user " + user);

            throw new LDAPServicesException.CouldNotConnectUserToLDAP();
        } catch (InterruptedException ignored) {
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

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
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.*;
import java.util.concurrent.*;

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
        static int maxResults = 100;

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

            client = ClientBuilder.newClient();

            webTarget = client.target(GRAPH_API_URL + tenantName);
        }

        private void acquireAccessToken() {
            if (authenticationResult == null || authenticationResult.getAccessToken() == null) {
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
                    throw new AzureAdClientException("Can't acquire an Azure AD token for client " + clientId + " with the provided secret key", ee);
                } catch (InterruptedException ignored) {
                } finally {
                    executorService.shutdown();
                }
            }
        }

        private void refreshAccessToken() {
            if (authenticationResult == null || authenticationResult.getRefreshToken() == null) {
                acquireAccessToken();
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
                    throw new AzureAdClientException("Can't acquire an Azure AD token for client " + clientId + " with the provided secret key", ee);
                } catch (InterruptedException ignored) {
                } finally {
                    executorService.shutdown();
                }
            }
        }

        private Invocation.Builder completeQueryBuilding(WebTarget webTarget) {
            return webTarget
                    .queryParam("api-version", GRAPH_API_VERSION)
                    .request(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, authenticationResult.getAccessToken());
        }

        private Invocation.Builder completeQueryBuilding(WebTarget webTarget, String skipToken) {
            if (skipToken == null) {
                return webTarget
                        .queryParam("$top", maxResults)
                        .queryParam("api-version", GRAPH_API_VERSION)
                        .request(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, authenticationResult.getAccessToken());
            }

            return webTarget
                    .queryParam("$top", maxResults)
                    .queryParam("$skiptoken", skipToken)
                    .queryParam("api-version", GRAPH_API_VERSION)
                    .request(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, authenticationResult.getAccessToken());
        }

        private JSONArray submitQueryWithoutPagination(WebTarget webTarget) {
            String responseText;

            acquireAccessToken();

            Response response = completeQueryBuilding(webTarget).get();

            if (response.getStatus() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                refreshAccessToken();

                response = completeQueryBuilding(webTarget).get();
            }

            if (new Integer(response.getStatus()).toString().startsWith("5")){
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ignored) {
                }

                response = completeQueryBuilding(webTarget, null).get();
            }

            responseText = getResponseText(response);

            if (response.getStatus() == HttpURLConnection.HTTP_OK) {
                return new JSONObject(responseText).getJSONArray("value");
            } else if (new Integer(response.getStatus()).toString().startsWith("5")){
                LOGGER.error(responseText);
                throw new AzureAdClientException("Unexpected Azure AD Graph API server error");
            } else {
                throw new AzureAdClientException(new JSONObject(responseText).optJSONObject("odata.error").optJSONObject("message").optString("value"));
            }
        }

        private List<JSONArray> submitQueryWithPagination(WebTarget webTarget) {
            List<JSONArray> result = new ArrayList<>();

            String responseText;
            String skipToken = null;

            do {
                acquireAccessToken();

                Response response = completeQueryBuilding(webTarget, skipToken).get();

                if (response.getStatus() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    refreshAccessToken();

                    response = completeQueryBuilding(webTarget, skipToken).get();
                }

                if (new Integer(response.getStatus()).toString().startsWith("5")){
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        break;
                    }

                    response = completeQueryBuilding(webTarget, skipToken).get();
                }

                responseText = getResponseText(response);
                skipToken = getSkipToken(responseText);

                if (response.getStatus() == HttpURLConnection.HTTP_OK) {
                    result.add(new JSONObject(responseText).getJSONArray("value"));
                } else if (new Integer(response.getStatus()).toString().startsWith("5")){
                    LOGGER.error(responseText);
                    throw new AzureAdClientException("Unexpected Azure AD Graph API server error");
                } else {
                    throw new AzureAdClientException(new JSONObject(responseText).optJSONObject("odata.error").optJSONObject("message").optString("value"));
                }
            } while (skipToken != null);

            return result;
        }

        @VisibleForTesting
        List<JSONArray> getAllUsersResponse() {
            return submitQueryWithPagination(webTarget.path("users"));
        }

        @VisibleForTesting
        JSONArray getUserGroupsResponse(final String userObjectId) {
            // Paging is not supported for link searches, cf. https://graph.microsoft.io/en-us/docs/overview/paging
            return submitQueryWithoutPagination(webTarget.path("users").path(userObjectId).path("$links").path("memberOf"));
        }

        @VisibleForTesting
        List<JSONArray> getAllGroupsResponse() {
            return submitQueryWithPagination(webTarget.path("groups"));
        }

        @VisibleForTesting
        JSONArray getGroupMembersResponse(final String groupObjectId) {
            // Paging is not supported for link searches, cf. https://graph.microsoft.io/en-us/docs/overview/paging
            return submitQueryWithoutPagination(webTarget.path("groups").path(groupObjectId).path("$links").path("members"));
        }

        private JSONObject getObjectResponseByUrl(final String objectUrl) {
            String responseText;

            acquireAccessToken();

            Response response = getObjectByUrl(objectUrl);

            if (response.getStatus() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                refreshAccessToken();

                response = getObjectByUrl(objectUrl);
            }

            if (new Integer(response.getStatus()).toString().startsWith("5")){
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ignored) {
                }

                response = getObjectByUrl(objectUrl);
            }

            responseText = getResponseText(response);

            if (response.getStatus() == HttpURLConnection.HTTP_OK) {
                return new JSONObject(responseText);
            } else if (new Integer(response.getStatus()).toString().startsWith("5")){
                LOGGER.error(responseText);
                throw new AzureAdClientException("Unexpected Azure AD Graph API server error");
            } else {
                throw new AzureAdClientException(new JSONObject(responseText).optJSONObject("odata.error").optJSONObject("message").optString("value"));
            }
        }

        private Response getObjectByUrl(final String objectUrl) {
            return completeQueryBuilding(client.target(objectUrl)).get();
        }

    }

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureAdClient.class);

    private static final String GRAPH_API_URL = "https://graph.windows.net/";

    private static final String GRAPH_API_VERSION = "1.6";

    private static final String AUTHORITY_BASE_URL = "https://login.microsoftonline.com/";

    private LDAPServerConfiguration ldapServerConfiguration;

    private LDAPUserSyncConfiguration ldapUserSyncConfiguration;

    public AzureAdClient(final LDAPServerConfiguration ldapServerConfiguration, final LDAPUserSyncConfiguration ldapUserSyncConfiguration) {
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

        for (JSONArray jsonArray : requestHelper.getAllUsersResponse()) {
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

        for (JSONArray jsonArray : requestHelper.getAllGroupsResponse()) {
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

        for (JSONArray groupsArray : requestHelper.getAllGroupsResponse()) {
            int groupsPageSize = groupsArray.length();

            LOGGER.info("Processing groups page " + pageNum++ + " having " + groupsPageSize + " items");

            for (int ig = 0; ig < groupsPageSize; ig++) {
                LDAPGroup ldapGroup = buildLDAPGroupFromJsonObject(groupsArray.getJSONObject(ig));

                if (ldapUserSyncConfiguration.isGroupAccepted(ldapGroup.getSimpleName())) {
                    if (ldapGroups.containsKey(ldapGroup.getDistinguishedName())) {
                        ldapGroup = ldapGroups.get(ldapGroup.getDistinguishedName());
                    } else {
                        ldapGroups.put(ldapGroup.getDistinguishedName(), ldapGroup);
                    }                }

                JSONArray membersArray = requestHelper.getGroupMembersResponse(ldapGroup.getDistinguishedName());

                for (int im = 0, membersCount = membersArray.length(); im < membersCount; im++) {
                    String objectUrl = membersArray.getJSONObject(im).optString("url");

                    if (objectUrl.endsWith("Microsoft.DirectoryServices.User")) {
                        JSONObject jsonObject = requestHelper.getObjectResponseByUrl(objectUrl);

                        LDAPUser ldapUser = buildLDAPUserFromJsonObject(jsonObject);

                        if (ldapUserSyncConfiguration.isUserAccepted(ldapUser.getName())) {
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
        if (!StringUtils.isEmpty(userJsonObject.optString("refreshTokensValidFromDateTime"))) {
            ldapUser.setLastLogon(new DateTime(userJsonObject.optString("refreshTokensValidFromDateTime")).toDate());
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

        for (JSONArray userArray : requestHelper.getAllUsersResponse()) {
            int groupsPageSize = userArray.length();

            LOGGER.info("Processing groups page " + pageNum++ + " having " + groupsPageSize + " items");

            for (int iu = 0; iu < groupsPageSize; iu++) {
                LDAPUser ldapUser = buildLDAPUserFromJsonObject(userArray.getJSONObject(iu));

                if (ldapUserSyncConfiguration.isUserAccepted(ldapUser.getName())) {
                    if (ldapUsers.containsKey(ldapUser.getId())) {
                        ldapUser = ldapUsers.get(ldapUser.getId());
                    } else {
                        ldapUsers.put(ldapUser.getId(), ldapUser);
                    }
                }

                JSONArray membershipsArray = requestHelper.getUserGroupsResponse(ldapUser.getId());

                for (int im = 0, membershipsCount = membershipsArray.length(); im < membershipsCount; im++) {
                    String objectUrl = membershipsArray.getJSONObject(im).optString("url");

                    if (objectUrl.endsWith("Microsoft.DirectoryServices.Group")) {
                        JSONObject jsonObject = requestHelper.getObjectResponseByUrl(objectUrl);

                        LDAPGroup ldapGroup = buildLDAPGroupFromJsonObject(jsonObject);

                        if (ldapUserSyncConfiguration.isGroupAccepted(ldapGroup.getSimpleName())) {
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

        LOGGER.info("Getting users and their memberships - end");
    }

    public void authenticate(final String user, final String password) throws CouldNotConnectUserToLDAP {
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

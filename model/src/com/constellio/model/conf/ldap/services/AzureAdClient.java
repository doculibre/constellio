package com.constellio.model.conf.ldap.services;

import com.constellio.data.utils.dev.Toggle;
import com.constellio.model.conf.ldap.config.LDAPServerConfiguration;
import com.constellio.model.conf.ldap.config.LDAPUserSyncConfiguration;
import com.constellio.model.conf.ldap.services.LDAPServicesException.CouldNotConnectUserToLDAP;
import com.constellio.model.conf.ldap.user.LDAPGroup;
import com.constellio.model.conf.ldap.user.LDAPUser;
import com.microsoft.aad.adal4j.AuthenticationContext;
import com.microsoft.aad.adal4j.AuthenticationResult;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 *
 */
public class AzureAdClient {

	private final AzureRequestHelper azureRequestHelper;

	public static class AzureAdClientException extends RuntimeException {

		public AzureAdClientException(String message) {
			super(message);
		}

		public AzureAdClientException(String message, Throwable cause) {
			super(message, cause);
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
		this.azureRequestHelper = new AzureRequestHelper(
				ldapServerConfiguration.getTenantName(),
				ldapUserSyncConfiguration.getClientId(),
				ldapUserSyncConfiguration.getClientSecret());
	}

	public AzureAdClient(final LDAPServerConfiguration ldapServerConfiguration,
						 final LDAPUserSyncConfiguration ldapUserSyncConfiguration,
						 AzureRequestHelper azureRequestHelper) {
		this.ldapServerConfiguration = ldapServerConfiguration;
		this.ldapUserSyncConfiguration = ldapUserSyncConfiguration;
		this.azureRequestHelper = azureRequestHelper;
	}

	public Set<String> getUserNameList() {
		LOGGER.info("Getting user name list - start");

		Set<String> results = new HashSet<>();

		List<JSONArray> jsonArrayList = azureRequestHelper
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

		for (JSONArray jsonArray : azureRequestHelper.getAllGroupsResponse(ldapUserSyncConfiguration.getGroupsFilter())) {
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

	public void getGroupsParent(String groupId, final Map<String, LDAPGroup> ldapGroups,
								AzureRequestHelper azureRequestHelper) {

		List<JSONArray> stuff = azureRequestHelper.getGroupMemberOfResponse(groupId);
		int pageNum = 1;
		for (JSONArray groupsArray : stuff) {
			int groupsPageSize = groupsArray.length();

			LOGGER.info("Processing parents of " + groupId + " page " + pageNum++ + " having " + groupsPageSize + " items");

			for (int ig = 0; ig < groupsPageSize; ig++) {

				LDAPGroup ldapGroup = buildLDAPGroupFromJsonObject(groupsArray.getJSONObject(ig));
				if (ldapUserSyncConfiguration.isGroupAccepted(ldapGroup.getSimpleName())) {
					if (!ldapGroups.containsKey(ldapGroup.getDistinguishedName())) {
						ldapGroups.put(ldapGroup.getDistinguishedName(), ldapGroup);
						getGroupsParent(ldapGroup.getDistinguishedName(), ldapGroups, azureRequestHelper);
					}
				}
			}
		}
	}

	public void getGroupsAndTheirUsers(final Map<String, LDAPGroup> ldapGroups, final Map<String, LDAPUser> ldapUsers) {
		LOGGER.info("Getting groups and their members - start");

		int pageNum = 1;

		List<JSONArray> stuff = azureRequestHelper.getAllGroupsResponse(ldapUserSyncConfiguration.getGroupsFilter());
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
						//getGroupsParent(ldapGroup.getDistinguishedName(), ldapGroups, requestHelper);
					}

					for (JSONArray membersArray : azureRequestHelper.getGroupMembersResponse(ldapGroup.getDistinguishedName())) {
						for (int im = 0, membersCount = membersArray.length(); im < membersCount; im++) {
							String objectUrl = membersArray.getJSONObject(im).optString("url");

							if (objectUrl.endsWith("Microsoft.DirectoryServices.User")) {
								JSONObject jsonObject = azureRequestHelper.getObjectResponseByUrl(objectUrl);

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
							} else if (objectUrl.endsWith("Microsoft.DirectoryServices.Group")) {
								JSONObject jsonObject = azureRequestHelper.getObjectResponseByUrl(objectUrl);
								LDAPGroup ldapSubGroup = buildLDAPGroupFromJsonObject(jsonObject, ldapGroup);
								getSubGroupsAndTheirUsers(ldapSubGroup, ldapGroups, ldapUsers);
							}
						}
					}
				}
			}
		}

		LOGGER.info("Getting groups and their members - end");
	}

	public void getSubGroupsAndTheirUsers(LDAPGroup subgroup, final Map<String, LDAPGroup> ldapGroups,
										  final Map<String, LDAPUser> ldapUsers) {
		LOGGER.info("Getting groups and their members - start");

		int pageNum = 1;

		LOGGER.info("Processing sub group");

		if (ldapUserSyncConfiguration.isGroupAccepted(subgroup.getSimpleName())) {
			ldapGroups.put(subgroup.getDistinguishedName(), subgroup);

			for (JSONArray membersArray : azureRequestHelper.getGroupMembersResponse(subgroup.getDistinguishedName())) {
				for (int im = 0, membersCount = membersArray.length(); im < membersCount; im++) {
					String objectUrl = membersArray.getJSONObject(im).optString("url");

					if (objectUrl.endsWith("Microsoft.DirectoryServices.User")) {
						JSONObject jsonObject = azureRequestHelper.getObjectResponseByUrl(objectUrl);

						LDAPUser ldapUser = buildLDAPUserFromJsonObject(jsonObject);

						if (Toggle.IGNORE_CONFIGS_WHEN_SYNCHRONIZING_AZURE_RELATED_USERS_AND_GROUPS.isEnabled() || ldapUserSyncConfiguration.isUserAccepted(ldapUser.getName())) {
							if (ldapUsers.containsKey(ldapUser.getId())) {
								ldapUser = ldapUsers.get(ldapUser.getId());
							} else {
								ldapUsers.put(ldapUser.getId(), ldapUser);
							}

							subgroup.addUser(ldapUser.getId());
							ldapUser.addGroup(subgroup);
						}
					} else if (objectUrl.endsWith("Microsoft.DirectoryServices.Group")) {
						JSONObject jsonObject = azureRequestHelper.getObjectResponseByUrl(objectUrl);
						LDAPGroup ldapSubGroup = buildLDAPGroupFromJsonObject(jsonObject, subgroup);
						if (!ldapGroups.containsKey(jsonObject.optString("objectId"))) {
							getSubGroupsAndTheirUsers(ldapSubGroup, ldapGroups, ldapUsers);
						}
					}
				}
			}

		}

		LOGGER.info("Getting sub group and their members - end");
	}

	private LDAPGroup buildLDAPGroupFromJsonObject(JSONObject groupJsonObject) {
		String groupObjectId = groupJsonObject.optString("objectId");
		String groupDisplayName = groupJsonObject.optString("displayName");
		return new LDAPGroup(groupDisplayName, groupObjectId);
	}

	private LDAPGroup buildLDAPGroupFromJsonObject(JSONObject groupJsonObject, final LDAPGroup ldapGroup) {
		String groupObjectId = groupJsonObject.optString("objectId");
		String groupDisplayName = groupJsonObject.optString("displayName");
		LDAPGroup ldap = new LDAPGroup(groupDisplayName, groupObjectId);
		ldap.addParent(ldapGroup.getDistinguishedName());
		return ldap;
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
		ldapUser.setWorkAddress(userJsonObject.optString("department"));
		ldapUser.setMsExchDelegateListBL(null); // TODO
		String refreshTokensValidFromDateTime = userJsonObject.optString("refreshTokensValidFromDateTime");
		if (!StringUtils.isEmpty(refreshTokensValidFromDateTime) && !"null".equals(refreshTokensValidFromDateTime)) {
			ldapUser.setLastLogon(new DateTime(refreshTokensValidFromDateTime).toDate());
		}

		return ldapUser;
	}

	public void getUsersAndTheirGroups(final Map<String, LDAPGroup> ldapGroups, final Map<String, LDAPUser> ldapUsers) {
		LOGGER.info("Getting users and their memberships - start");

		int pageNum = 1;

		List<JSONArray> jsonArrayList = azureRequestHelper
				.getAllUsersResponse(ldapUserSyncConfiguration.getUserGroups(), ldapUserSyncConfiguration.getUsersFilter());

		for (JSONArray userArray : jsonArrayList) {
			int groupsPageSize = userArray.length();

			LOGGER.info("Processing users page " + pageNum++ + " having " + groupsPageSize + " items");

			for (int iu = 0; iu < groupsPageSize; iu++) {
				JSONObject jsonObject = userArray.getJSONObject(iu);
				if (jsonObject.has("url")) {
					jsonObject = azureRequestHelper.getObjectResponseByUrl(jsonObject.optString("url"));
				}

				LDAPUser ldapUser = buildLDAPUserFromJsonObject(jsonObject);

				if (ldapUserSyncConfiguration.isUserAccepted(ldapUser.getName())) {
					if (ldapUsers.containsKey(ldapUser.getId())) {
						ldapUser = ldapUsers.get(ldapUser.getId());
					} else {
						ldapUsers.put(ldapUser.getId(), ldapUser);
					}
					//}

					for (JSONArray membershipsArray : azureRequestHelper.getUserGroupsResponse(ldapUser.getId())) {
						for (int im = 0, membershipsCount = membershipsArray.length(); im < membershipsCount; im++) {
							String objectUrl = membershipsArray.getJSONObject(im).optString("url");

							if (objectUrl.endsWith("Microsoft.DirectoryServices.Group")) {
								jsonObject = azureRequestHelper.getObjectResponseByUrl(objectUrl);

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

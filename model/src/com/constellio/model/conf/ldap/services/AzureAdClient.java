package com.constellio.model.conf.ldap.services;

import com.constellio.model.conf.ldap.config.LDAPServerConfiguration;
import com.constellio.model.conf.ldap.config.LDAPUserSyncConfiguration;
import com.constellio.model.conf.ldap.config.UserNameType;
import com.constellio.model.conf.ldap.services.LDAPServicesException.CouldNotConnectUserToLDAP;
import com.constellio.model.conf.ldap.user.LDAPGroup;
import com.constellio.model.conf.ldap.user.LDAPUser;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.microsoft.aad.msal4j.PublicClientApplication;
import com.microsoft.aad.msal4j.UserNamePasswordParameters;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
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

	private static final String AUTHORITY_BASE_URL = "https://login.microsoftonline.com/";

	private LDAPServerConfiguration ldapServerConfiguration;

	private LDAPUserSyncConfiguration ldapUserSyncConfiguration;

	public AzureAdClient(final LDAPServerConfiguration ldapServerConfiguration) {
		this.ldapServerConfiguration = ldapServerConfiguration;
		this.ldapUserSyncConfiguration = null;
		azureRequestHelper = null;
	}

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
				String userConfigValidationName = getUserNameForConfigValidation(jsonArray.getJSONObject(i));
				if (ldapUserSyncConfiguration.isUserAccepted(userConfigValidationName)) {
					results.add(userConfigValidationName);
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
				String groupConfigValidationName = getGroupNameForConfigValidation(jsonArray.getJSONObject(i));
				if (ldapUserSyncConfiguration.isGroupAccepted(groupConfigValidationName)) {
					results.add(groupConfigValidationName);
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

		boolean fetchSubGroups = ldapUserSyncConfiguration.isFetchSubGroups();
		boolean ignoreRegexForSubGroups = ldapUserSyncConfiguration.isIgnoreRegexForSubGroups();
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
					List<JSONArray> membersArrays = new ArrayList<>();
					try {
						membersArrays = azureRequestHelper.getGroupMembersResponse(ldapGroup.getDistinguishedName());
					} catch (Exception ex) {
						LOGGER.error("Error occured during get group " + ldapGroup.getDistinguishedName() + " members", ex);
					}
					for (JSONArray membersArray : membersArrays) {
						for (int im = 0, membersCount = membersArray.length(); im < membersCount; im++) {
							String objectUrl = membersArray.getJSONObject(im).optString("url");

							if (objectUrl.endsWith("Microsoft.DirectoryServices.User")) {
								JSONObject userJsonObject;
								try {
									userJsonObject = azureRequestHelper.getObjectResponseByUrl(objectUrl);
								} catch (Exception ex) {
									LOGGER.error("Error occured during get object url " + objectUrl, ex);
									continue;
								}

								String userConfigValidationName = getUserNameForConfigValidation(userJsonObject);
								if (ldapUserSyncConfiguration.isUserAccepted(userConfigValidationName)) {
									LDAPUser ldapUser = buildLDAPUserFromJsonObject(userJsonObject);
									if (ldapUsers.containsKey(ldapUser.getId())) {
										ldapUser = ldapUsers.get(ldapUser.getId());
									} else {
										ldapUsers.put(ldapUser.getId(), ldapUser);
									}

									ldapGroup.addUser(ldapUser.getId());
									ldapUser.addGroup(ldapGroup);
								}
							} else if (objectUrl.endsWith("Microsoft.DirectoryServices.Group") && fetchSubGroups) {
								JSONObject jsonObject = azureRequestHelper.getObjectResponseByUrl(objectUrl);
								LDAPGroup ldapSubGroup = buildLDAPGroupFromJsonObject(jsonObject, ldapGroup);
								if (ignoreRegexForSubGroups || ldapUserSyncConfiguration.isGroupAccepted(ldapSubGroup.getSimpleName())) {
									ldapGroups.put(ldapSubGroup.getDistinguishedName(), ldapSubGroup);
									getSubGroupsAndTheirUsers(ldapSubGroup, ldapGroups, ldapUsers);
								}
							}
						}
					}
				}
			}
		}

		LOGGER.info("Getting groups and their members - end");
	}

	public void getSubGroupsAndTheirUsers(LDAPGroup ldapGroup, final Map<String, LDAPGroup> ldapGroups,
										  final Map<String, LDAPUser> ldapUsers) {
		boolean fetchSubGroups = ldapUserSyncConfiguration.isFetchSubGroups();
		boolean ignoreRegexForSubGroups = ldapUserSyncConfiguration.isIgnoreRegexForSubGroups();
		if (fetchSubGroups) {
			LOGGER.info("Getting sub groups and their members - start");
			LOGGER.info("Processing sub groups");

			// Getting all subgroups rather than only those matching the regular expression because we cannot reach this method unless the parent matched the regular expression
			String groupConfigValidationName = getGroupNameForConfigValidation(ldapGroup);
			if (ignoreRegexForSubGroups || ldapUserSyncConfiguration.isGroupAccepted(groupConfigValidationName)) {
				List<JSONArray> groupMembersResponse = new ArrayList<>();
				try {
					groupMembersResponse = azureRequestHelper.getGroupMembersResponse(ldapGroup.getDistinguishedName());
				} catch (Exception ex) {
					LOGGER.error("Error occured during get group " + ldapGroup.getDistinguishedName() + " members", ex);
				}
				for (JSONArray membersArray : groupMembersResponse) {
					for (int im = 0, membersCount = membersArray.length(); im < membersCount; im++) {
						String objectUrl = membersArray.getJSONObject(im).optString("url");

						if (objectUrl.endsWith("Microsoft.DirectoryServices.User")) {
							JSONObject userJsonObject;
							try {
								userJsonObject = azureRequestHelper.getObjectResponseByUrl(objectUrl);
							} catch (Exception ex) {
								LOGGER.error("Error occured during get object url " + objectUrl, ex);
								continue;
							}

							String userConfigValidationName = getUserNameForConfigValidation(userJsonObject);
							if (ldapUserSyncConfiguration.isUserAccepted(userConfigValidationName)) {
								LDAPUser ldapUser = buildLDAPUserFromJsonObject(userJsonObject);
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
							String subGroupConfigValidationName = getGroupNameForConfigValidation(jsonObject);
							if (!ldapGroups.containsKey(jsonObject.optString("objectId")) &&
								(ignoreRegexForSubGroups || ldapUserSyncConfiguration.isGroupAccepted(subGroupConfigValidationName))) {
								ldapGroups.put(ldapSubGroup.getDistinguishedName(), ldapSubGroup);
								getSubGroupsAndTheirUsers(ldapSubGroup, ldapGroups, ldapUsers);
							}
						}
					}
				}
			}
			LOGGER.info("Getting sub groups and their members - end");
		}
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
		String username = userJsonObject.optString("mailNickname");
		LDAPUser ldapUser = new LDAPUser();
		ldapUser.setId(userJsonObject.optString("objectId"));
		ldapUser.setName(username);
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

	private String getGroupNameForConfigValidation(JSONObject groupJsonObject) {
		return groupJsonObject.optString("displayName");
	}

	private String getGroupNameForConfigValidation(LDAPGroup ldapGroup) {
		return ldapGroup.getSimpleName();
	}

	private String getUserNameForConfigValidation(JSONObject userJsonObject) {
		String userNameForConfigValidation;
		if (UserNameType.EMAIL.getCode().equals(ldapUserSyncConfiguration.getUsernameType())) {
			userNameForConfigValidation = userJsonObject.optString("userPrincipalName");
		} else {
			userNameForConfigValidation = userJsonObject.optString("mailNickname");
		}
		return userNameForConfigValidation;
	}

	private String getUserNameForConfigValidation(LDAPUser ldapUser) {
		String userNameForConfigValidation;
		if (UserNameType.EMAIL.getCode().equals(ldapUserSyncConfiguration.getUsernameType())) {
			userNameForConfigValidation = ldapUser.getEmail();
		} else {
			userNameForConfigValidation = ldapUser.getName();
		}
		return userNameForConfigValidation;
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
				JSONObject userJsonObject = userArray.getJSONObject(iu);
				if (userJsonObject.has("url")) {
					try {
						userJsonObject = azureRequestHelper.getObjectResponseByUrl(userJsonObject.optString("url"));
					} catch (Exception e) {
						LOGGER.error("Error occured during get object url " + userJsonObject.optString("url"), e);
						continue;
					}
				}

				String userConfigValidationName = getUserNameForConfigValidation(userJsonObject);
				if (ldapUserSyncConfiguration.isUserAccepted(userConfigValidationName)) {
					LDAPUser ldapUser = buildLDAPUserFromJsonObject(userJsonObject);
					if (ldapUsers.containsKey(ldapUser.getId())) {
						ldapUser = ldapUsers.get(ldapUser.getId());
					} else {
						ldapUsers.put(ldapUser.getId(), ldapUser);
					}
					//}

					List<JSONArray> userGroupsResponse = new ArrayList<>();
					try {
						userGroupsResponse = azureRequestHelper.getUserGroupsResponse(ldapUser.getId());
					} catch (Exception e) {
						LOGGER.error("Error occured during get user " + ldapUser.getName() + " groups", e);
					}
					for (JSONArray membershipsArray : userGroupsResponse) {
						for (int im = 0, membershipsCount = membershipsArray.length(); im < membershipsCount; im++) {
							String objectUrl = membershipsArray.getJSONObject(im).optString("url");

							if (objectUrl.endsWith("Microsoft.DirectoryServices.Group")) {
								JSONObject groupJsonObject;
								try {
									groupJsonObject = azureRequestHelper.getObjectResponseByUrl(objectUrl);
								} catch (Exception e) {
									LOGGER.error("Error occured during get object url " + objectUrl, e);
									continue;
								}

								String groupConfigValidationName = getGroupNameForConfigValidation(groupJsonObject);
								if (ldapUserSyncConfiguration.isGroupAccepted(groupConfigValidationName)) {
									LDAPGroup ldapGroup = buildLDAPGroupFromJsonObject(groupJsonObject);
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
		IAuthenticationResult authenticationResult = null;

		try {
			PublicClientApplication clientApplication = PublicClientApplication
					.builder(ldapServerConfiguration.getClientId())
					.authority(authority)
					.build();
			UserNamePasswordParameters parameters = UserNamePasswordParameters
					.builder(Collections.singleton("User.Read"), user, password.toCharArray()).build();
			Future<IAuthenticationResult> authenticationResultFuture = clientApplication.acquireToken(parameters);
			authenticationResult = authenticationResultFuture.get();
		} catch (MalformedURLException mue) {
			LOGGER.error("Malformed Azure AD authority URL " + authority);

			throw new CouldNotConnectUserToLDAP();
		} catch (ExecutionException ee) {
			LOGGER.error("Can't authenticate user " + user);

			throw new CouldNotConnectUserToLDAP();
		} catch (InterruptedException ignored) {
		}

		if (authenticationResult == null) {
			throw new CouldNotConnectUserToLDAP();
		}
	}

}

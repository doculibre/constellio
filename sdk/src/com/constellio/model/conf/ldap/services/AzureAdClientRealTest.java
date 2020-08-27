package com.constellio.model.conf.ldap.services;

import com.constellio.data.utils.dev.Toggle;
import com.constellio.model.conf.ldap.config.LDAPServerConfiguration;
import com.constellio.model.conf.ldap.config.LDAPUserSyncConfiguration;
import com.constellio.model.conf.ldap.user.LDAPGroup;
import com.constellio.model.conf.ldap.user.LDAPUser;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class AzureAdClientRealTest {
	@Mock
	LDAPServerConfiguration ldapServerConfiguration;

	@Mock
	LDAPUserSyncConfiguration ldapUserSyncConfiguration;

	@Mock
	AzureRequestHelper azureRequestHelper;

	@Before
	public void setUp()
			throws Exception {
		MockitoAnnotations.initMocks(this);
		when(ldapServerConfiguration.getClientId()).thenReturn("69ab5806-25cf-4d80-a818-5b7cb7df1681");
		when(ldapServerConfiguration.getTenantName()).thenReturn("adgrics.onmicrosoft.com");
		when(ldapUserSyncConfiguration.getClientId()).thenReturn("bec3eab8-7c58-4263-b439-71ae66faa656");
		when(ldapUserSyncConfiguration.getClientSecret()).thenReturn("");

		when(ldapUserSyncConfiguration.isGroupAccepted(any("".getClass()))).thenReturn(true);
		when(ldapUserSyncConfiguration.isUserAccepted(any("".getClass()))).thenReturn(true);
	}

	@Test
	public void whenGroupHasTwoDepthSubGroupThenReturnGroupsAndUsersFromDepth() {
		Toggle.ALLOW_LDAP_FETCH_SUB_GROUPS.enable();
		when(azureRequestHelper.getGroupMembersResponse(anyString())).thenReturn(subGroupArrayObjectDepth0())
				.thenReturn(asList(new JSONArray("[]")))
				.thenReturn(subGroupArrayObjectDepth1()).thenReturn(asList(new JSONArray("[]")));
		when(azureRequestHelper.getObjectResponseByUrl(anyString())).thenReturn(subGroupObjectDepth0())
				.thenReturn(userObject1()).thenReturn(subGroupObjectDepth2()).thenReturn(subGroupObjectDepth1())
				.thenReturn(userObject2()).thenReturn(subGroupObjectDepth3()).thenReturn(userObject3());

		AzureAdClient azureAdClientSpy = spy(new AzureAdClient(ldapServerConfiguration, ldapUserSyncConfiguration, azureRequestHelper));

		final Map<String, LDAPGroup> ldapGroups = new HashMap<>();
		final Map<String, LDAPUser> ldapUsers = new HashMap<>();

		azureAdClientSpy.getSubGroupsAndTheirUsers(mainGroupObject(), ldapGroups, ldapUsers);
		ldapGroups.put(mainGroupObject().getDistinguishedName(), mainGroupObject());

		assertThat(ldapGroups).hasSize(5);
		assertThat(ldapGroups.get("ebe521da-209b-492a-a6b6-7099dfbb2e49").getSimpleName()).isEqualTo("subgroupAD0");
		assertThat(ldapGroups.get("ebe521da-209b-492a-a6b6-7044dfbb2e49").getSimpleName()).isEqualTo("subgroupAD3");
		assertThat(ldapGroups.get("ebe521da-209b-492a-a6b6-7033dfbb2e49").getSimpleName()).isEqualTo("subgroupAD1");

		assertThat(ldapGroups.get("ebe521da-209b-492a-a6b6-7099dfbb2e49").getMemberOf())
				.containsAll(asList("ebe521da-209b-492a-a6b6-7088dfbb2e49"));
		assertThat(ldapGroups.get("ebe521da-209b-492a-a6b6-7055dfbb2e49").getMemberOf())
				.containsAll(asList("ebe521da-209b-492a-a6b6-7088dfbb2e49"));
		assertThat(ldapGroups.get("ebe521da-209b-492a-a6b6-7033dfbb2e49").getMemberOf())
				.containsAll(asList("ebe521da-209b-492a-a6b6-7055dfbb2e49"));
		assertThat(ldapGroups.get("ebe521da-209b-492a-a6b6-7044dfbb2e49").getMemberOf())
				.containsAll(asList("ebe521da-209b-492a-a6b6-7055dfbb2e49"));

		assertThat(ldapUsers.get("c7a50570-11c7-40de-9665-f24d3f34cff4").getUserGroups().get(0).getDistinguishedName())
				.isEqualTo("ebe521da-209b-492a-a6b6-7055dfbb2e49");

		assertThat(ldapUsers).hasSize(3);
		assertThat(ldapGroups.get("ebe521da-209b-492a-a6b6-7099dfbb2e49").getSimpleName()).isEqualTo("subgroupAD0");
		assertThat(ldapGroups.get("ebe521da-209b-492a-a6b6-7044dfbb2e49").getSimpleName()).isEqualTo("subgroupAD3");
		assertThat(ldapGroups.get("ebe521da-209b-492a-a6b6-7033dfbb2e49").getSimpleName()).isEqualTo("subgroupAD1");

	}

	@Test
	public void whenGroupHasTwoDepthSubGroupButToggleIsFalse() {
		Toggle.ALLOW_LDAP_FETCH_SUB_GROUPS.disable();
		when(azureRequestHelper.getGroupMembersResponse(anyString())).thenReturn(subGroupArrayObjectDepth0())
				.thenReturn(asList(new JSONArray("[]")))
				.thenReturn(subGroupArrayObjectDepth1()).thenReturn(asList(new JSONArray("[]")));
		when(azureRequestHelper.getObjectResponseByUrl(anyString())).thenReturn(subGroupObjectDepth0())
				.thenReturn(userObject1()).thenReturn(subGroupObjectDepth2()).thenReturn(subGroupObjectDepth1())
				.thenReturn(userObject2()).thenReturn(subGroupObjectDepth3()).thenReturn(userObject3());

		AzureAdClient azureAdClientSpy = spy(new AzureAdClient(ldapServerConfiguration, ldapUserSyncConfiguration, azureRequestHelper));

		final Map<String, LDAPGroup> ldapGroups = new HashMap<>();
		final Map<String, LDAPUser> ldapUsers = new HashMap<>();

		azureAdClientSpy.getSubGroupsAndTheirUsers(mainGroupObject(), ldapGroups, ldapUsers);
		ldapGroups.put(mainGroupObject().getDistinguishedName(), mainGroupObject());

		assertThat(ldapGroups).hasSize(1);
		assertThat(ldapGroups.get("ebe521da-209b-492a-a6b6-7088dfbb2e49").getSimpleName()).isEqualTo("groupADMain");

		assertThat(ldapUsers).hasSize(0);
	}

	private LDAPGroup mainGroupObject() {
		return new LDAPGroup("groupADMain", "ebe521da-209b-492a-a6b6-7088dfbb2e49");
	}

	private List<JSONArray> subGroupArrayObjectDepth0() {
		String groupJson = "[{\"url\": \"Microsoft.DirectoryServices.Group\", \"objectId\": \"ebe521da-209b-492a-a6b6-7099dfbb2e49\"," +
						   " \"displayName\": \"subgroupAD0\"}," +
						   "{\"url\": \"Microsoft.DirectoryServices.User\", \"objectId\": \"d328706d-7abc-4229-be52-172bd5e7aa2d\"," +
						   " \"displayName\": \"userAD1\"}," +
						   "{\"url\": \"Microsoft.DirectoryServices.Group\", \"objectId\": \"ebe521da-209b-492a-a6b6-7055dfbb2e49\"," +
						   " \"displayName\": \"subgroupAD2\"}]";
		return asList(new JSONArray(groupJson));
	}

	private List<JSONArray> subGroupArrayObjectDepth1() {
		String groupJson = "[{\"url\": \"Microsoft.DirectoryServices.Group\", \"objectId\": \"ebe521da-209b-492a-a6b6-7033dfbb2e49\"," +
						   " \"displayName\": \"subgroupAD1\"}," +
						   "{\"url\": \"Microsoft.DirectoryServices.User\", \"objectId\": \"c7a50570-11c7-40de-9665-f24d3f34cff4\"," +
						   " \"displayName\": \"userAD2\"}," +
						   "{\"url\": \"Microsoft.DirectoryServices.Group\", \"objectId\": \"ebe521da-209b-492a-a6b6-7044dfbb2e49\"," +
						   " \"displayName\": \"subgroupAD3\"}," +
						   "{\"url\": \"Microsoft.DirectoryServices.User\", \"objectId\": \"1f38c9fd-dabb-481e-a5a9-0064745c1843\"," +
						   " \"displayName\": \"userAD3\"}" +
						   "]";
		return asList(new JSONArray(groupJson));
	}

	private JSONObject subGroupObjectDepth0() {
		String groupJson = "{\"url\": \"Microsoft.DirectoryServices.Group\", " +
						   "\"objectId\": \"ebe521da-209b-492a-a6b6-7099dfbb2e49\", \"displayName\": \"subgroupAD0\"}";
		return new JSONObject(groupJson);
	}

	private JSONObject subGroupObjectDepth2() {
		String groupJson = "{\"url\": \"Microsoft.DirectoryServices.Group\", " +
						   "\"objectId\": \"ebe521da-209b-492a-a6b6-7055dfbb2e49\", \"displayName\": \"subgroupAD2\"}";
		return new JSONObject(groupJson);
	}

	private JSONObject subGroupObjectDepth3() {
		String groupJson = "{\"url\": \"Microsoft.DirectoryServices.Group\", " +
						   "\"objectId\": \"ebe521da-209b-492a-a6b6-7044dfbb2e49\", \"displayName\": \"subgroupAD3\"}";
		return new JSONObject(groupJson);
	}

	private JSONObject subGroupObjectDepth1() {
		String groupJson = "{\"url\": \"Microsoft.DirectoryServices.Group\", " +
						   "\"objectId\": \"ebe521da-209b-492a-a6b6-7033dfbb2e49\", \"displayName\": \"subgroupAD1\"}";
		return new JSONObject(groupJson);
	}

	private JSONObject userObject1() {
		String groupJson = "{\"url\": \"Microsoft.DirectoryServices.User\", " +
						   "\"objectId\": \"d328706d-7abc-4229-be52-172bd5e7aa2d\", \"displayName\": \"userAD1\"," +
						   "\"mailNickname\": \"mailNameUserAD1\", \"surname\": \"surnameUserAD1\"," +
						   "\"givenName\": \"givenNameUserAD1\", \"userPrincipalName\": \"UserAD1@doculibre.ca\"," +
						   "\"accountEnabled\": \"true\", \"department\": \"dep1\"," +
						   "\"refreshTokensValidFromDateTime\": \"2020-01-01\"" +
						   "}";
		return new JSONObject(groupJson);
	}

	private JSONObject userObject2() {
		String groupJson = "{\"url\": \"Microsoft.DirectoryServices.User\", " +
						   "\"objectId\": \"c7a50570-11c7-40de-9665-f24d3f34cff4\", \"displayName\": \"userAD2\"," +
						   "\"mailNickname\": \"mailNameUserAD2\", \"surname\": \"surnameUserAD2\"," +
						   "\"givenName\": \"givenNameUserAD2\", \"userPrincipalName\": \"UserAD1@doculibre.ca\"," +
						   "\"accountEnabled\": \"true\", \"department\": \"dep1\"," +
						   "\"refreshTokensValidFromDateTime\": \"2020-01-01\"" +
						   "}";
		return new JSONObject(groupJson);
	}

	private JSONObject userObject3() {
		String groupJson = "{\"url\": \"Microsoft.DirectoryServices.User\", " +
						   "\"objectId\": \"1f38c9fd-dabb-481e-a5a9-0064745c1843\", \"displayName\": \"userAD3\"," +
						   "\"mailNickname\": \"mailNameUserAD3\", \"surname\": \"surnameUserAD3\"," +
						   "\"givenName\": \"givenNameUserAD3\", \"userPrincipalName\": \"UserAD1@doculibre.ca\"," +
						   "\"accountEnabled\": \"true\", \"department\": \"dep1\"," +
						   "\"refreshTokensValidFromDateTime\": \"2020-01-01\"" +
						   "}";
		return new JSONObject(groupJson);
	}

	//Utilities

	private LDAPGroup buildLDAPGroupFromJsonObject(JSONObject groupJsonObject) {
		String groupObjectId = groupJsonObject.optString("objectId");
		String groupDisplayName = groupJsonObject.optString("displayName");
		return new LDAPGroup(groupDisplayName, groupObjectId);
	}
}

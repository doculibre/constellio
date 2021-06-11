package com.constellio.model.conf.ldap.services;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.naming.ldap.LdapContext;

import org.junit.Test;

import com.constellio.model.conf.LDAPTestConfig;
import com.constellio.model.conf.ldap.LDAPDirectoryType;
import com.constellio.model.conf.ldap.user.LDAPGroup;
import com.constellio.model.conf.ldap.user.LDAPUser;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.InDevelopmentTest;
import com.constellio.sdk.tests.annotations.InternetTest;

@InternetTest
public class LDAPServicesAcceptanceTest extends ConstellioTest {

	@Test
	@InDevelopmentTest
	public void givenValidLdapConnexionInfoThenConnectToServer() {
		LdapContext ldapContext = getValidContext();
		assertThat(ldapContext).isNotNull();
	}

	private LdapContext getValidContext() {
		return new LDAPServicesImpl()
				.connectToLDAP(LDAPTestConfig.getDomains(), LDAPTestConfig.getLDAPDevServerUrl(), LDAPTestConfig.getUser(),
						LDAPTestConfig.getPassword(), false, true);
	}

	@Test
	@InDevelopmentTest
	public void whenSearchingGroupThenReturnValidGroupAttributes()
			throws Exception {
		boolean fetchSubGroupsEnabled = false;
		LdapContext ldapContext = getValidContext();
		String ouWithGroup1AndGroup2 = "OU=ouWithGroup1AndGroup2,OU=testSynchronization,DC=test,DC=doculibre,DC=ca";
		String ouWithGroup3AndGroup4 = "OU=ouWithGroup3AndGroup4,OU=testSynchronization,DC=test,DC=doculibre,DC=ca";
		Set<LDAPGroup> groups = new LDAPServicesImpl()
				.getAllGroups(ldapContext, Arrays.asList(new String[]{ouWithGroup1AndGroup2, ouWithGroup3AndGroup4}), fetchSubGroupsEnabled);
		assertThat(groups.size()).isEqualTo(4);
		List<String> groupsNames = new ArrayList<>();
		for (LDAPGroup group : groups) {
			groupsNames.add(group.getSimpleName());
		}
		assertThat(groupsNames).contains("group1", "group2", "group3", "group4");
	}

	@Test
	@InDevelopmentTest
	public void whenGetRootContextsGetValidRootContext()
			throws Exception {
		LdapContext ldapContext = getValidContext();
		assertThat(new LDAPServicesImpl().getRootContexts(ldapContext)).contains("DC=test,DC=doculibre,DC=ca");
	}


	@Test
	@InDevelopmentTest
	public void whenSearchingMoreThan1000GroupsThenReturnAllGroups()
			throws Exception {
		boolean fetchSubGroupsEnabled = false;
		LdapContext ldapContext = getValidContext();
		String ouWith2997groups = "OU=Departement2,OU=doculibre,DC=test,DC=doculibre,DC=ca";
		Set<LDAPGroup> groups = new LDAPServicesImpl().getAllGroups(ldapContext, Arrays.asList(new String[]{ouWith2997groups}), fetchSubGroupsEnabled);
		assertThat(groups.size()).isEqualTo(2997);
	}

	@Test
	@InDevelopmentTest
	public void whenSearchingMoreThan1000UsersThenReturnAllUsers()
			throws Exception {
		boolean fetchSubGroupsEnabled = true;
		LdapContext ldapContext = getValidContext();
		String ouWith3001Users = "OU=Departement1,OU=doculibre,DC=test,DC=doculibre,DC=ca";
		List<String> users = new LDAPServicesImpl()
				.searchUsersIdsFromContext(LDAPDirectoryType.ACTIVE_DIRECTORY, ldapContext, ouWith3001Users, LDAPTestConfig.getUserFilterGroupsList());
		assertThat(users.size()).isEqualTo(3001);
	}

	@Test
	@InDevelopmentTest
	public void whenSearchingGroupsFromTwoContextsThenReturnAllGroupsFromBothContexts()
			throws Exception {
		boolean fetchSubGroupsEnabled = true;
		LdapContext ldapContext = getValidContext();
		String allTestGroupsOU = "OU=testSynchronization,DC=test,DC=doculibre,DC=ca";
		Set<LDAPGroup> groups = new LDAPServicesImpl().getAllGroups(ldapContext, Arrays.asList(new String[]{allTestGroupsOU}), fetchSubGroupsEnabled);
		LDAPGroup subgroupLevel1 = null;
		for (LDAPGroup group : groups) {

			if (group.getSimpleName().equals("subgroupLevel1")) {
				subgroupLevel1 = group;
				break;
			}
		}
		assertThat(subgroupLevel1).isNotNull();
		assertThat(subgroupLevel1.getDistinguishedName())
				.isEqualTo("CN=subgroupLevel1,OU=testSynchronization,DC=test,DC=doculibre,DC=ca");
		for (String userId : subgroupLevel1.getMembers()) {
			System.out.println("===========================user:" + userId);
			LDAPUser ldapUser = new LDAPServicesImpl().getUser(LDAPDirectoryType.ACTIVE_DIRECTORY, userId,
					ldapContext);
			System.out.println(ldapUser);
		}

		System.out.println(subgroupLevel1);
	}

	@Test
	@InDevelopmentTest
	public void whenDnForUserThenOk()
			throws Exception {
		LdapContext ldapContext = getValidContext();
		String ouWith3001Users = "OU=Departement1,OU=doculibre,DC=test,DC=doculibre,DC=ca";
		String dn = new LDAPServicesImpl().dnForUser(ldapContext, "username0", Arrays.asList(ouWith3001Users));
		assertThat(dn).isEqualTo("CN=username0,OU=Departement1,OU=doculibre,DC=test,DC=doculibre,DC=ca");
		System.out.println(dn);
	}
}

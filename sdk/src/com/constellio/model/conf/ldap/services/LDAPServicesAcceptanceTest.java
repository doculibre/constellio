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
import com.constellio.sdk.tests.annotations.InternetTest;

@InternetTest
public class LDAPServicesAcceptanceTest {

	@Test
	public void givenValidLdapConnexionInfoThenConnectToServer() {
		LdapContext ldapContext = getValidContext();
		assertThat(ldapContext).isNotNull();
	}

	private LdapContext getValidContext() {
		return new LDAPServices()
				.connectToLDAP(LDAPTestConfig.getDomains(), LDAPTestConfig.getLDAPDevServerUrl(), LDAPTestConfig.getUser(),
						LDAPTestConfig.getPassword(), false, true);
	}

	@Test
	public void whenSearchingGroupThenReturnValidGroupAttributes()
			throws Exception {
		LdapContext ldapContext = getValidContext();
		String ouWithGroup1AndGroup2 = "OU=ouWithGroup1AndGroup2,OU=testSynchronization,DC=test,DC=doculibre,DC=ca";
		String ouWithGroup3AndGroup4 = "OU=ouWithGroup3AndGroup4,OU=testSynchronization,DC=test,DC=doculibre,DC=ca";
		Set<LDAPGroup> groups = new LDAPServices()
				.getAllGroups(ldapContext, Arrays.asList(new String[] { ouWithGroup1AndGroup2, ouWithGroup3AndGroup4 }));
		assertThat(groups.size()).isEqualTo(4);
		List<String> groupsNames = new ArrayList<>();
		for (LDAPGroup group : groups) {
			groupsNames.add(group.getSimpleName());
		}
		assertThat(groupsNames).contains("group1", "group2", "group3", "group4");
	}

   /* @Test
	public void testMFA()
            throws Exception{
        LdapContext ldapContext = new LDAPServices().connectToLDAP(Arrays.asList(new String[]{"DC=mes,DC=reseau,DC=intra"}),//mes.reseau.intra
                "ldap://127.0.0.1:3389",
                "cs_065_IntelliGid_de",
                "R*tsQ5yzt2Zfb#Xd");
        String groupCN = "CN=guMFA,OU=Groupes Users (gu),OU=Groupes,DC=mes,DC=reseau,DC=intra";
        Set<LDAPGroup> groups = new LDAPServices().getAllGroups(ldapContext, Arrays.asList(new String[]{groupCN}));
        List<String> groupsNames = new ArrayList<>();
        for(LDAPGroup group : groups){
            groupsNames.add(group.getSimpleName());
            System.out.println(group.getSimpleName());
            System.out.println(group.getMembers().size());
            if(!group.getMembers().isEmpty()){
                System.out.println(group);
            }
        }
        ldapContext.close();
    }

    @Test
    public void testUsersMFA()
            throws Exception{
        LdapContext ldapContext = new LDAPServices().connectToLDAP(Arrays.asList(new String[]{"DC=mes,DC=reseau,DC=intra"}),//mes.reseau.intra
                "ldap://127.0.0.1:3389",
                "cs_065_IntelliGid_de",
                "R*tsQ5yzt2Zfb#Xd");
        String groupCN = "CN=guMFA,OU=Groupes Users (gu),OU=Groupes,DC=mes,DC=reseau,DC=intra";
        Set<String> usersIds = new HashSet<>();
        LDAPServices ldapServices = new LDAPServices();
        for(String baseContextName : getBaseContextList()){
            List<String> currentUsersIds;
            try{
                currentUsersIds = ldapServices.searchUsersIdsFromContext(LDAPDirectoryType.ACTIVE_DIRECTORY, ldapContext, baseContextName);
            } catch (NamingException e) {
                throw new RuntimeException(e);
            }
            usersIds.addAll(currentUsersIds);
        }
        System.out.println(usersIds.size());
        ldapContext.close();
    }

    private String[] getBaseContextList() {
        return new String[]{
            "OU=Utilisateurs,DC=mes,DC=reseau,DC=intra",
                "OU=A_B,OU=Utilisateurs,DC=mes,DC=reseau,DC=intra"
        };
    }

    @Test
    public void testAcceptedUserMFA()
            throws Exception{
        LdapContext ldapContext = new LDAPServices().connectToLDAP(Arrays.asList(new String[]{"DC=mes,DC=reseau,DC=intra"}),//mes.reseau.intra
                "ldap://127.0.0.1:3389",
                "cs_065_IntelliGid_de",
                "R*tsQ5yzt2Zfb#Xd");//
        boolean isUser = new LDAPServices().isUser(LDAPDirectoryType.ACTIVE_DIRECTORY, "CN=Boulanger\\\\, Sylvain,OU=A_B,OU=Utilisateurs,DC=mes,DC=reseau,DC=intra",
                //
                ldapContext);
        System.out.println(isUser);
    }*/

	@Test
	public void whenSearchingMoreThan1000GroupsThenReturnAllGroups()
			throws Exception {
		LdapContext ldapContext = getValidContext();
		String ouWith2997groups = "OU=Departement2,OU=doculibre,DC=test,DC=doculibre,DC=ca";
		Set<LDAPGroup> groups = new LDAPServices().getAllGroups(ldapContext, Arrays.asList(new String[] { ouWith2997groups }));
		assertThat(groups.size()).isEqualTo(2997);
	}

	@Test
	public void whenSearchingMoreThan1000UsersThenReturnAllUsers()
			throws Exception {
		LdapContext ldapContext = getValidContext();
		String ouWith3001Users = "OU=Departement1,OU=doculibre,DC=test,DC=doculibre,DC=ca";
		List<String> users = new LDAPServices()
				.searchUsersIdsFromContext(LDAPDirectoryType.ACTIVE_DIRECTORY, ldapContext, ouWith3001Users);
		assertThat(users.size()).isEqualTo(3001);
	}

	@Test
	public void whenSearchingGroupsFromTwoContextsThenReturnAllGroupsFromBothContexts()
			throws Exception {
		LdapContext ldapContext = getValidContext();
		String allTestGroupsOU = "OU=testSynchronization,DC=test,DC=doculibre,DC=ca";
		Set<LDAPGroup> groups = new LDAPServices().getAllGroups(ldapContext, Arrays.asList(new String[] { allTestGroupsOU }));
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
			LDAPUser ldapUser = new LDAPServices().getUser(LDAPDirectoryType.ACTIVE_DIRECTORY, userId,
					ldapContext);
			System.out.println(ldapUser);
		}

		System.out.println(subgroupLevel1);
	}
}

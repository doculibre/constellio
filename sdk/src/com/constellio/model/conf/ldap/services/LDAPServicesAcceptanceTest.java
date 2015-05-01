/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.model.conf.ldap.services;

import com.constellio.model.conf.LDAPTestConfig;
import com.constellio.model.conf.ldap.LDAPDirectoryType;
import com.constellio.model.conf.ldap.user.LDAPGroup;
import com.constellio.model.conf.ldap.user.LDAPUser;
import org.junit.Test;

import javax.naming.directory.DirContext;
import javax.naming.ldap.LdapContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class LDAPServicesAcceptanceTest {
    //TODO Nouha

    @Test
    public void givenValidLdapConnexionInfoThenConnectToServer(){
        LdapContext ldapContext = getValidContext();
        assertThat(ldapContext).isNotNull();
    }

    private LdapContext getValidContext() {
        return new LDAPServices().connectToLDAP(LDAPTestConfig.getDomains(), LDAPTestConfig.getLDAPDevServerUrl(), LDAPTestConfig.getUser(),
                LDAPTestConfig.getPassword());
    }

    @Test
         public void whenSearchingGroupThenReturnValidGroupAttributes()
            throws Exception{
        LdapContext ldapContext = getValidContext();
        String ouWithGroup1AndGroup2 = "OU=ouWithGroup1AndGroup2,OU=testSynchronization,DC=test,DC=doculibre,DC=ca";
        String ouWithGroup3AndGroup4 = "OU=ouWithGroup3AndGroup4,OU=testSynchronization,DC=test,DC=doculibre,DC=ca";
        Set<LDAPGroup> groups = new LDAPServices().getAllGroups(ldapContext, Arrays.asList(new String[]{ouWithGroup1AndGroup2, ouWithGroup3AndGroup4}));
        assertThat(groups.size()).isEqualTo(4);
        List<String> groupsNames = new ArrayList<>();
        for(LDAPGroup group : groups){
            groupsNames.add(group.getSimpleName());
        }
        assertThat(groupsNames).contains("group1", "group2", "group3", "group4");
    }

    @Test
    public void whenSearchingMoreThan1000GroupsThenReturnAllGroups()
            throws Exception{
        LdapContext ldapContext = getValidContext();
        String ouWith2997groups = "OU=Departement2,OU=doculibre,DC=test,DC=doculibre,DC=ca";
        Set<LDAPGroup> groups = new LDAPServices().getAllGroups(ldapContext, Arrays.asList(new String[]{ouWith2997groups}));
        assertThat(groups.size()).isEqualTo(2997);
    }

    @Test
    public void whenSearchingMoreThan1000UsersThenReturnAllUsers()
            throws Exception{
        LdapContext ldapContext = getValidContext();
        String ouWith3001Users = "OU=Departement1,OU=doculibre,DC=test,DC=doculibre,DC=ca";
        List<String> users = new LDAPServices().searchUsersIdsFromContext(LDAPDirectoryType.ACTIVE_DIRECTORY, ldapContext, ouWith3001Users);
        assertThat(users.size()).isEqualTo(3001);
    }

    @Test
    public void whenSearchingGroupsFromTwoContextsThenReturnAllGroupsFromBothContexts()
            throws Exception{
        LdapContext ldapContext = getValidContext();
        String allTestGroupsOU = "OU=testSynchronization,DC=test,DC=doculibre,DC=ca";
        Set<LDAPGroup> groups = new LDAPServices().getAllGroups(ldapContext, Arrays.asList(new String[]{allTestGroupsOU}));
        LDAPGroup subgroupLevel1 = null;
        for(LDAPGroup group : groups){

            if(group.getSimpleName().equals("subgroupLevel1")){
                subgroupLevel1 = group;
                break;
            }
        }
        assertThat(subgroupLevel1).isNotNull();
        assertThat(subgroupLevel1.getDistinguishedName()).isEqualTo("CN=subgroupLevel1,OU=testSynchronization,DC=test,DC=doculibre,DC=ca");
        for(String userId : subgroupLevel1.getMembers()){
            System.out.println("===========================user:" + userId);
            LDAPUser ldapUser = new LDAPServices().getUser(LDAPDirectoryType.ACTIVE_DIRECTORY, userId,
                    ldapContext);
            System.out.println(ldapUser);
        }

        System.out.println(subgroupLevel1);
    }
}

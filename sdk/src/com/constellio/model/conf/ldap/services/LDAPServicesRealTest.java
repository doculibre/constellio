package com.constellio.model.conf.ldap.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.naming.NamingEnumeration;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.constellio.model.conf.ldap.Filter;
import com.constellio.model.conf.ldap.user.LDAPGroup;

public class LDAPServicesRealTest {
	@Mock
	InitialLdapContext ldapContext;

	@Before
	public void setUp()
			throws Exception {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void whenGroupHasTwoDepthSubGroupThenReturnGroupsAndUsersFromDepth() throws Exception {
		boolean fetchSubGroupsEnabled = true;
		when(ldapContext.search(anyString(), anyString(), any(SearchControls.class))).thenReturn(contextSearchSpoofResults1())
				.thenReturn(contextSearchSpoofResults2()).thenReturn(contextSearchSpoofResults3());

		LDAPServicesImpl ldapClientSpy = spy(new LDAPServicesImpl());

		Set<LDAPGroup> ldapGroups = ldapClientSpy.getAllGroups(ldapContext, getTestGroupsContainers(), fetchSubGroupsEnabled);

		List<String> arrayIds = Arrays.asList("test1ID", "test2ID", "test3ID");

		assertThat(ldapGroups).hasSize(3);
		for (LDAPGroup group :
				ldapGroups) {
			if (group.getDistinguishedName().equals("test1ID")) {
				assertThat(group.getMembers()).containsAll(Arrays.asList("test2ID"));
				assertThat(group.getMemberOf()).isEmpty();
			} else if (group.getDistinguishedName().equals("test2ID")) {
				assertThat(group.getMembers()).containsAll(Arrays.asList("test3ID"));
				assertThat(group.getMemberOf()).containsAll(Arrays.asList("test1ID"));
			} else if (group.getDistinguishedName().equals("test3ID")) {
				assertThat(group.getMembers()).isEmpty();
				assertThat(group.getMemberOf()).containsAll(Arrays.asList("test2ID"));
			} else {
				fail("Unknown or empty spoof groups.");
			}
		}
	}

	@Test
	public void whenGroupHasTwoDepthSubGroupButSubGroupToggleIsDisabledThenDoNotReturnSubGroups() throws Exception {
		boolean fetchSubGroupsEnabled = false;
		when(ldapContext.search(anyString(), anyString(), any(SearchControls.class))).thenReturn(contextSearchSpoofResults1())
				.thenReturn(contextSearchSpoofResults2()).thenReturn(contextSearchSpoofResults3());

		LDAPServicesImpl ldapClientSpy = spy(new LDAPServicesImpl());

		Set<LDAPGroup> ldapGroups = ldapClientSpy.getAllGroups(ldapContext, getTestGroupsContainers(), fetchSubGroupsEnabled);

		List<String> arrayIds = Arrays.asList("test1ID", "test2ID", "test3ID");

		assertThat(ldapGroups).hasSize(1);
		for (LDAPGroup group :
				ldapGroups) {
			if (group.getDistinguishedName().equals("test1ID")) {
				assertThat(group.getMemberOf()).isEmpty();
			} else {
				fail("Unknown or empty spoof groups.");
			}
		}
	}

	@Test
	public void givenIgnoreRegexForSubGroupsEnabledWhenGroupMatchesRegexButSubGroupDoesntThenReturnSubGroup() throws Exception {
		boolean fetchSubGroups = true;
		boolean ignoreRegexForSubGroups = true;
		when(ldapContext.search(anyString(), anyString(), any(SearchControls.class))).thenReturn(contextSearchSpoofResults1())
				.thenReturn(contextSearchSpoofResults2()).thenReturn(contextSearchSpoofResults3());

		LDAPServicesImpl ldapClientSpy = spy(new LDAPServicesImpl());
		
		Filter regexFilter = new Filter() {
			@Override
			public Boolean isAccepted(String name) {
				return "test1".equals(name);
			}
		};

		Set<LDAPGroup> ldapGroups = ldapClientSpy.getGroupsUsingFilter(ldapContext, getTestGroupsContainers(), regexFilter, fetchSubGroups, ignoreRegexForSubGroups);

		assertThat(ldapGroups).hasSize(3);
		for (LDAPGroup group :
				ldapGroups) {
			if (group.getDistinguishedName().equals("test1ID")) {
				assertThat(group.getMembers()).containsAll(Arrays.asList("test2ID"));
				assertThat(group.getMemberOf()).isEmpty();
			} else if (group.getDistinguishedName().equals("test2ID")) {
				assertThat(group.getMembers()).containsAll(Arrays.asList("test3ID"));
				assertThat(group.getMemberOf()).containsAll(Arrays.asList("test1ID"));
			} else if (group.getDistinguishedName().equals("test3ID")) {
				assertThat(group.getMembers()).isEmpty();
				assertThat(group.getMemberOf()).containsAll(Arrays.asList("test2ID"));
			} else {
				fail("Unknown or empty spoof groups.");
			}
		}
	}

	@Test
	public void givenIgnoreRegexForSubGroupsDisabledWhenGroupMatchesRegexButSubGroupDoesntThenReturnNoSubGroup() throws Exception {
		boolean fetchSubGroups = true;
		boolean ignoreRegexForSubGroups = false;
		when(ldapContext.search(anyString(), anyString(), any(SearchControls.class))).thenReturn(contextSearchSpoofResults1())
				.thenReturn(contextSearchSpoofResults2()).thenReturn(contextSearchSpoofResults3());

		LDAPServicesImpl ldapClientSpy = spy(new LDAPServicesImpl());
		
		Filter regexFilter = new Filter() {
			@Override
			public Boolean isAccepted(String name) {
				return "test1".equals(name);
			}
		};

		Set<LDAPGroup> ldapGroups = ldapClientSpy.getGroupsUsingFilter(ldapContext, getTestGroupsContainers(), regexFilter, fetchSubGroups, ignoreRegexForSubGroups);

		assertThat(ldapGroups).hasSize(1);
		for (LDAPGroup group : ldapGroups) {
			if (group.getDistinguishedName().equals("test1ID")) {
				assertThat(group.getMembers()).hasSize(1);
				assertThat(group.getMembers()).containsAll(Arrays.asList("test2ID"));
				assertThat(group.getMemberOf()).isEmpty();
			} else {
				fail("Unknown or empty spoof groups.");
			}
		}
	}

	private LDAPGroup mainGroupObject() {
		return new LDAPGroup("groupADMain", "ebe521da-209b-492a-a6b6-7088dfbb2e49");
	}

	private List<String> getTestGroupsContainers() {
		return Arrays.asList("OU=ouWithGroup1AndGroup2,OU=testSynchronization,DC=test,DC=doculibre,DC=ca");
	}

	private NamingEnumeration<SearchResult> contextSearchSpoofResults1() {
		NamingEnumerationSpoofTest enumeration = new NamingEnumerationSpoofTest();
		enumeration.init();
		return enumeration;
	}

	private NamingEnumeration<SearchResult> contextSearchSpoofResults2() {
		NamingEnumerationSpoofTest enumeration = new NamingEnumerationSpoofTest();
		enumeration.init2();
		return enumeration;
	}

	private NamingEnumeration<SearchResult> contextSearchSpoofResults3() {
		NamingEnumerationSpoofTest enumeration = new NamingEnumerationSpoofTest();
		enumeration.init3();
		return enumeration;
	}

	//Utilities


}

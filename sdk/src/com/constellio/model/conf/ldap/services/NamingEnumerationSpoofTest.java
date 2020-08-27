package com.constellio.model.conf.ldap.services;

import com.constellio.model.conf.ldap.user.LDAPGroup;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.SearchResult;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NamingEnumerationSpoofTest implements NamingEnumeration<SearchResult> {

	private int counter = 0;
	private List<SearchResult> results = new ArrayList<>();

	public void init() {
		counter = 1;
		SearchResult searchResult1 = new SearchResult("OU=ouWithGroup1AndGroup2,OU=testSynchronization,DC=test,DC=doculibre,DC=ca",
				null, buildSpoofAttributes1());

		results = Arrays.asList(searchResult1);
	}

	public void init2() {
		counter = 1;
		SearchResult searchResult2 = new SearchResult("OU=ouWithGroup1AndGroup2,OU=testSynchronization,DC=test,DC=doculibre,DC=ca",
				null, buildSpoofAttributes2());

		results = Arrays.asList(searchResult2);
	}

	public void init3() {
		counter = 1;
		SearchResult searchResult3 = new SearchResult("OU=ouWithGroup1AndGroup2,OU=testSynchronization,DC=test,DC=doculibre,DC=ca",
				null, buildSpoofAttributes3());

		results = Arrays.asList(searchResult3);
	}

	@Override
	public boolean hasMoreElements() {
		return counter > 0;
	}

	@Override
	public SearchResult nextElement() {
		if (counter == 0) {
			return null;
		}
		return results.get(counter - 1);
	}

	@Override
	public SearchResult next() throws NamingException {
		counter = counter - 1;
		return results.get(counter);
	}

	@Override
	public boolean hasMore() throws NamingException {
		return counter > 0;
	}

	@Override
	public void close() throws NamingException {

	}

	private BasicAttributes buildSpoofAttributes1() {
		BasicAttributes basicAttributes = new BasicAttributes();
		basicAttributes.put(LDAPGroup.COMMON_NAME, "test1");
		basicAttributes.put(LDAPGroup.DISTINGUISHED_NAME, "test1ID");
		basicAttributes.put(LDAPGroup.MEMBER, "test2ID");
		return basicAttributes;
	}

	private BasicAttributes buildSpoofAttributes2() {
		BasicAttributes basicAttributes = new BasicAttributes();
		basicAttributes.put(LDAPGroup.COMMON_NAME, "test2");
		basicAttributes.put(LDAPGroup.DISTINGUISHED_NAME, "test2ID");
		basicAttributes.put(LDAPGroup.MEMBER, "test3ID");
		basicAttributes.put(LDAPGroup.MEMBER_OF, "test1ID");
		return basicAttributes;
	}

	private BasicAttributes buildSpoofAttributes3() {
		BasicAttributes basicAttributes = new BasicAttributes();
		basicAttributes.put(LDAPGroup.COMMON_NAME, "test3");
		basicAttributes.put(LDAPGroup.DISTINGUISHED_NAME, "test3ID");
		basicAttributes.put(LDAPGroup.MEMBER_OF, "test2ID");
		return basicAttributes;
	}

}

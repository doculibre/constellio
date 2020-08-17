package com.constellio.model.conf.ldap.user;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

abstract public class CommonUserBuilder implements LDAPUserBuilder {

	private String[] fetchedAttributes = null;

	@Override
	public String[] getFetchedAttributes() {
		if (fetchedAttributes == null) {
			buildFetchedAttributes();
		}
		return fetchedAttributes;
	}

	private void buildFetchedAttributes() {
		List<String> nonNullAttributes = new ArrayList<>();
		nonNullAttributes.add(getNameAttributeName());

		nonNullAttributes.add(getGroupAttributeName());

		if (getEmailAttributeName() != null) {
			nonNullAttributes.add(getEmailAttributeName());
		}

		if (getGivenNameAttributeName() != null) {
			nonNullAttributes.add(getGivenNameAttributeName());
		}

		if (getFamilyNameAttributeName() != null) {
			nonNullAttributes.add(getFamilyNameAttributeName());
		}

		if (getLastLoginAttributeName() != null) {
			nonNullAttributes.add(getLastLoginAttributeName());
		}

		if (getCompanyAttributeName() != null) {
			nonNullAttributes.add(getCompanyAttributeName());
		}

		if (getEnabledAttributeName() != null) {
			nonNullAttributes.add(getEnabledAttributeName());
		}

		if (getMsExchDelegateListBl() != null) {
			nonNullAttributes.add(getMsExchDelegateListBl());
		}

		fetchedAttributes = new String[nonNullAttributes.size()];

		for (int i = 0; i < nonNullAttributes.size(); i++) {
			fetchedAttributes[i] = nonNullAttributes.get(i);
		}
	}

	@Override
	public LDAPUser buildUser(String userId, Attributes attrs)
			throws NamingException {
		LDAPUser returnUser = new LDAPUser();

		returnUser.setId(userId);

		Attribute nameAttribute = attrs.get(getNameAttributeName());
		String name = buildName(nameAttribute);
		returnUser.setName(name);

		if (getEnabledAttributeName() != null) {
			Attribute enabledAttribute = attrs.get(getEnabledAttributeName());
			Boolean enabled = buildEnabled(enabledAttribute);
			returnUser.setEnabled(enabled);
		} else {
			returnUser.setEnabled(getDefaultValueIfIsEnabledAttributeNull());
		}

		if (getEmailAttributeName() != null) {
			Attribute emailAttribute = attrs.get(getEmailAttributeName());
			String email = buildEmail(emailAttribute);
			returnUser.setEmail(email);
		}

		if (getGivenNameAttributeName() != null) {
			Attribute givenNameAttribute = attrs.get(getGivenNameAttributeName());
			String givenName = buildGivenName(givenNameAttribute);
			returnUser.setGivenName(givenName);
		}

		if (getFamilyNameAttributeName() != null) {
			Attribute familyNameAttribute = attrs.get(getFamilyNameAttributeName());
			String familyName = buildFamilyName(familyNameAttribute);
			returnUser.setFamilyName(familyName);
		}

		if (getLastLoginAttributeName() != null) {
			Attribute lastLogonAttribute = attrs.get(getLastLoginAttributeName());
			Date lastLogon = buildLastLogonAttribute(lastLogonAttribute);
			returnUser.setLastLogon(lastLogon);
		}

		if (getCompanyAttributeName() != null) {
			Attribute lieuTravailAttribute = attrs.get(getCompanyAttributeName());
			String lieuTravail = buildCompany(lieuTravailAttribute);
			returnUser.setWorkAddress(lieuTravail);
		}

		if (getMsExchDelegateListBl() != null) {
			Attribute msExchDelegateListBlAttribute = attrs.get(getMsExchDelegateListBl());
			List<String> msExchDelegateListBl = buildMsExchDelegateListBL(msExchDelegateListBlAttribute);
			returnUser.setMsExchDelegateListBL(msExchDelegateListBl);
		}

		Attribute groupsAttribute = attrs.get(getGroupAttributeName());
		List<String> groupsDN = buildGroups(groupsAttribute);
		for (String group : groupsDN) {
			returnUser.addGroup(new LDAPGroup(group));
		}
		return returnUser;
	}

	protected List<String> buildGroups(Attribute groupsAttribute)
			throws NamingException {
		List<String> returnList = new ArrayList<>();
		if (groupsAttribute != null) {
			for (int i = 0; i < groupsAttribute.size(); i++) {
				String group = (String) groupsAttribute.get(i);
				returnList.add(group);
			}
		}
		return returnList;
	}

	protected List<String> buildMsExchDelegateListBL(Attribute groupsAttribute)
			throws NamingException {
		List<String> returnList = new ArrayList<>();
		if (groupsAttribute != null) {
			for (int i = 0; i < groupsAttribute.size(); i++) {
				String group = (String) groupsAttribute.get(i);
				returnList.add(group);
			}
		}
		return returnList;
	}

	protected String buildCompany(Attribute lieuTravailAttribute)
			throws NamingException {
		return getFirstString(lieuTravailAttribute);
	}

	protected String buildName(Attribute nameAttribute)
			throws NamingException {
		return getFirstString(nameAttribute);
	}

	protected String getFirstString(Attribute attribute)
			throws NamingException {
		if (attribute == null || attribute.size() == 0) {
			return "";
		}
		return (String) attribute.get(0);
	}

	protected String buildGivenName(Attribute givenNameAttribute)
			throws NamingException {
		return getFirstString(givenNameAttribute);
	}

	protected String buildEmail(Attribute emailAttribute)
			throws NamingException {
		return getFirstString(emailAttribute);
	}

	protected Boolean buildEnabled(Attribute enabledAttribute)
			throws NamingException {
		String enabledAsString = getFirstString(enabledAttribute);
		Boolean enabled;
		try {
			enabled = Boolean.getBoolean(enabledAsString);
		} catch (NumberFormatException e) {
			e.printStackTrace();
			enabled = true;
		}
		return enabled;
	}

	protected String buildFamilyName(Attribute nameAttribute)
			throws NamingException {
		return getFirstString(nameAttribute);
	}

	protected String buildAccount(Attribute accountAttribute)
			throws NamingException {
		return getFirstString(accountAttribute);
	}

	abstract protected Date buildLastLogonAttribute(Attribute lastLogonAttribute)
			throws NamingException;

	protected String getEmailAttributeName() {
		return "mail";
	}

	protected String getGivenNameAttributeName() {
		return "givenName";
	}

	protected String getFamilyNameAttributeName() {
		return "sn";
	}

	abstract protected String getLastLoginAttributeName();

	abstract protected String getCompanyAttributeName();

	abstract protected String getGroupAttributeName();

	abstract protected String getNameAttributeName();

	abstract protected String getEnabledAttributeName();

	abstract protected Boolean getDefaultValueIfIsEnabledAttributeNull();

	abstract protected String getMsExchDelegateListBl();

}

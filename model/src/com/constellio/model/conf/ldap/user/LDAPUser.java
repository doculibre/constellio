package com.constellio.model.conf.ldap.user;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LDAPUser {
	private String id;
	private String name;
	private Boolean enabled = true;
	private String email;
	private String givenName;
	private String familyName;
	private Date lastLogon;
	private String workAddress;
	private List<String> msExchDelegateListBL;
	//private String primaryGroupID;
	private List<LDAPGroup> userGroups = new ArrayList<>();

	public LDAPUser addGroup(LDAPGroup group) {
		userGroups.add(group);
		return this;
	}

	public LDAPUser addGroups(LDAPGroup... groups) {
		for (LDAPGroup group : groups) {
			addGroup(group);
		}
		return this;
	}

	public Boolean getEnabled() {
		return enabled;
	}

	public LDAPUser setEnabled(Boolean enabled) {
		this.enabled = enabled;
		return this;
	}

	public String getName() {
		return name;
	}

	public LDAPUser setName(String name) {
		this.name = name;
		return this;
	}

	public String getEmail() {
		return email;
	}

	public LDAPUser setEmail(String email) {
		this.email = email;
		return this;
	}

	public List<LDAPGroup> getUserGroups() {
		return userGroups;
	}

	public LDAPUser setUserGroups(List<LDAPGroup> userGroups) {
		this.userGroups = userGroups;
		return this;
	}

	public String getId() {
		return this.id;
	}

	public LDAPUser setId(String id) {
		this.id = id;
		return this;
	}

	public String getGivenName() {
		return givenName;
	}

	public LDAPUser setGivenName(String givenName) {
		this.givenName = givenName;
		return this;
	}

	public String getFamilyName() {
		return familyName;
	}

	public LDAPUser setFamilyName(String familyName) {
		this.familyName = familyName;
		return this;
	}

	public Date getLastLogon() {
		return lastLogon;
	}

	public LDAPUser setLastLogon(Date lastLogon) {
		this.lastLogon = lastLogon;
		return this;
	}

	public String getWorkAddress() {
		return workAddress;
	}

	public LDAPUser setWorkAddress(String workAddress) {
		this.workAddress = workAddress;
		return this;
	}

	public List<String> getMsExchDelegateListBL() {
		return msExchDelegateListBL;
	}

	public LDAPUser setMsExchDelegateListBL(List<String> msExchDelegateListBL) {
		this.msExchDelegateListBL = msExchDelegateListBL;
		return this;
	}

	@Override
	public String toString() {
		StringBuilder strb = new StringBuilder();
		strb.append("id :" + id + "\n");
		strb.append("name :" + name + "\n");
		strb.append("givenName :" + givenName + "\n");
		strb.append("familyName :" + familyName + "\n");
		strb.append("email :" + email + "\n");
		strb.append("enabled :" + enabled + "\n");
		strb.append("workAddress :" + workAddress + "\n");
		strb.append("userGroups :\n" + StringUtils.join(userGroups.toArray(), "\n"));
		if (msExchDelegateListBL != null) {
			strb.append("msExchDelegateListBL :\n" + StringUtils.join(msExchDelegateListBL.toArray()) + "\n");
		}
		return strb.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (obj.getClass() != getClass()) {
			return false;
		}
		LDAPUser rhs = (LDAPUser) obj;
		return new EqualsBuilder()
				.appendSuper(super.equals(obj))
				.append(id, rhs.id)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return id == null ? 0 : id.hashCode();
	}
}

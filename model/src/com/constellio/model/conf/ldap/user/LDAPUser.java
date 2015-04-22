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
package com.constellio.model.conf.ldap.user;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;

public class LDAPUser {
	private String id;
	private String name;
	private Boolean enabled = true;
	private String email;
	private String givenName;
	private String familyName;
	private Date lastLogon;
	private String lieuTravail;
	//private String primaryGroupID;
	private List<LDAPGroup> userGroups = new ArrayList<>();

	public void addGroup(LDAPGroup group){
		userGroups.add(group);
	}

	public Boolean getEnabled() {
		return enabled;
	}
	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public List<LDAPGroup> getUserGroups() {
		return userGroups;
	}
	public void setUserGroups(List<LDAPGroup> userGroups) {
		this.userGroups = userGroups;
	}
	public String getId() {
		return this.id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getGivenName() {
		return givenName;
	}
	public void setGivenName(String givenName) {
		this.givenName = givenName;
	}
	public String getFamilyName() {
		return familyName;
	}
	public void setFamilyName(String familyName) {
		this.familyName = familyName;
	}
	public Date getLastLogon() {
		return lastLogon;
	}
	public void setLastLogon(Date lastLogon) {
		this.lastLogon = lastLogon;
	}
	public String getLieuTravail() {
		return lieuTravail;
	}
	public void setLieuTravail(String lieuTravail) {
		this.lieuTravail = lieuTravail;
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
		strb.append("lieuTravail :" + lieuTravail + "\n");
		strb.append("userGroups :\n" + StringUtils.join(userGroups.toArray(), "\n") );
		return strb.toString();
	}

}

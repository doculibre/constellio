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
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;

public class LDAPGroup {
	//TODO hierarchie de groupes (plus cn mais plutot distinguishedName)
	public static final String COMMON_NAME = "cn";
	public static final String DISTINGUISHED_NAME = "distinguishedName";
	//FIXME Important member ne suffit pas lorsque le groupe est le primaryGroupID de l'utilisateur car ni le groupe n apparaitra dans l utilisateur ni l inverse :
	//Users don't have a memberOf property for their primary group, and the primary group won't have a member property listing them.
	//requete ldap (&(objectCategory=person)(objectClass=user)(primaryGroupID=XXXX))
	public static final String MEMBER = "member";
	public static final String MEMBER_OF = "memberof";
	public static final String[] FETCHED_ATTRIBUTES = {DISTINGUISHED_NAME, COMMON_NAME, MEMBER, MEMBER_OF};

	private String distinguishedName;
	private String simpleName;

	private List<String> ldapUsers = new ArrayList<>();


	public LDAPGroup(String simpleName, String distinguishedName) {
		super();
		this.simpleName = simpleName;
		this.distinguishedName = distinguishedName;
	}

	public LDAPGroup(String distinguishedName) {
		super();
		this.simpleName = extractSimpleName(distinguishedName);
		this.distinguishedName = distinguishedName;
	}

	private String extractSimpleName(String distinguishedName) {
		//CN=Denied RODC Password Replication Group,CN=Users,DC=test,DC=doculibre,DC=ca
		return StringUtils.substringBetween(distinguishedName, "CN=", ",");
	}

	public String getDistinguishedName() {
		return distinguishedName;
	}

	public String getSimpleName() {
		return simpleName;
	}

	public void addUser(String userId){
		if(!this.ldapUsers.contains(userId)){
			this.ldapUsers.add(userId);
		}
	}

	public List<String> getMembers() {
		return Collections.unmodifiableList(this.ldapUsers);
	}

	@Override
	public String toString() {
		return "\t" + distinguishedName + "\n\tUsers :\n\t" + StringUtils.join(ldapUsers.toArray(), "\n\t");
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		LDAPGroup ldapGroup = (LDAPGroup) o;

		if (distinguishedName != null ? !distinguishedName.equals(ldapGroup.distinguishedName) : ldapGroup.distinguishedName != null)
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		return distinguishedName != null ? distinguishedName.hashCode() : 0;
	}
}

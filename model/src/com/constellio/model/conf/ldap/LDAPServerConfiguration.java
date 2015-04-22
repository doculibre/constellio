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
package com.constellio.model.conf.ldap;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public class LDAPServerConfiguration implements Serializable{
	private Boolean ldapAuthenticationActive;

	private List<String> urls;

	private List<String> domains;

	private LDAPDirectoryType directoryType;

	public LDAPServerConfiguration(List<String> urls, List<String> domains, LDAPDirectoryType directoryType, Boolean ldapAuthenticationActive) {
		this.urls = Collections.unmodifiableList(urls);
		this.domains = Collections.unmodifiableList(domains);
		this.directoryType = directoryType;
		this.ldapAuthenticationActive = ldapAuthenticationActive;
	}

	public List<String> getUrls() {
		return urls;
	}

	public List<String> getDomains() {
		return domains;
	}

	public LDAPDirectoryType getDirectoryType() {
		return directoryType;
	}

	public Boolean getLdapAuthenticationActive() {
		return ldapAuthenticationActive;
	}
}

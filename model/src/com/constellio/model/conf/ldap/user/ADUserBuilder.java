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

import java.util.Date;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;

public class ADUserBuilder extends CommonUserBuilder implements LDAPUserBuilder {
	private static final long WINDOWS_FILE_TIME_FORMAT_BASE_DATE = 11644473600000L;
	
	private static final String MEMBER_OF = "memberOf";
	private static final String USER_ACCOUNT_CONTROL = "userAccountControl";
	private static final String NAME = "sAMAccountName";
	private static final String LAST_LOGON = "lastlogon";
	private static final String LIEU_TRAVAIL = "department"; // Peut etre department ou company
	

	@Override
	protected String getEnabledAttributeName() {
		return USER_ACCOUNT_CONTROL;
	}
	

	@Override
	protected Boolean buildEnabled(Attribute enabledAttribute)
			throws NamingException {
		Boolean enabled = null;
		if (enabledAttribute != null) {
			long lng = Long.parseLong(enabledAttribute.get(0).toString());
			long secondBit = lng & 2; // get bit 2
			if (secondBit == 0) {
				enabled = true;
			}
		}
		if (enabled == null){
			System.out.println("Utilisateur sans enabled mis comme inactif");
			enabled = false;
		}
		return enabled;
	}


	@Override
	protected String getLastLoginAttributeName() {
		return LAST_LOGON;
	}

	@Override
	protected String getCompanyAttributeName() {
		return LIEU_TRAVAIL;
	}

	@Override
	protected String getGroupAttributeName() {
		return MEMBER_OF;
	}
	
	@Override
	protected String getNameAttributeName() {
		return NAME;
	}



	@Override
	protected Date buildLastLogonAttribute(Attribute lastLogonAttribute) throws NamingException {
		if (lastLogonAttribute != null) {
			for (int i = 0; i < lastLogonAttribute.size(); i++) {
				long date = Long.parseLong((String) lastLogonAttribute.get(i));
				if(date != 0L){
					Date lastLogon = new Date(date/10000 - WINDOWS_FILE_TIME_FORMAT_BASE_DATE);
					return lastLogon;
				}
			}
		}
		return null;
	}


	@Override
	public String getUserIdAttribute() {
		return "distinguishedName";
	}


	@Override
	protected Boolean getDefaultValueIfIsEnabledAttributeNull() {
		return false;
	}
	

}

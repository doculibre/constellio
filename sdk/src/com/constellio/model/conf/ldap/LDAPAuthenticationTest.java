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

import com.constellio.model.services.users.sync.LDAPFastBind;

public class LDAPAuthenticationTest {
	public static void main(String[] args) {

		if (args.length != 3) {
			System.out.println("Usage : LDAPTest url user@domain password");
			return;
		}
		String url = args[0];
		String user = args[1];
		String password = args[2];

		System.out.println("Url : " + url);
		System.out.println("User : " + user);
		System.out.println("Password : " + password);

		LDAPFastBind ldapFastBind = new LDAPFastBind(url);
		try {
			System.out.println("Authentification reussie ? : " + ldapFastBind.authenticate(user, password));
		} catch (Exception e) {
			e.printStackTrace();

		} finally {
			ldapFastBind.close();
		}

	}
}

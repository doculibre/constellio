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

		LDAPFastBind ldapFastBind = new LDAPFastBind(url, false, true);
		try {
			System.out.println("Authentification reussie ? : " + ldapFastBind.authenticate(user, password));
		} catch (Exception e) {
			e.printStackTrace();

		} finally {
			ldapFastBind.close();
		}

	}
}

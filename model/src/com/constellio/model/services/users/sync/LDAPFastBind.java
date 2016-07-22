/**
 * IntelliGID, Open Source Enterprise Search
 * Copyright (C) 2010 DocuLibre inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.constellio.model.services.users.sync;

/**
 * ldapfastbind.java
 *
 * Sample JNDI application to use Active Directory LDAP_SERVER_FAST_BIND connection control
 *
 */

import java.util.Hashtable;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.ldap.Control;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

import org.apache.commons.lang3.StringUtils;

@SuppressWarnings("serial") class FastBindConnectionControl implements Control {
	public byte[] getEncodedValue() {
		return null;
	}

	public String getID() {
		return "1.2.840.113556.1.4.1781";
	}

	public boolean isCritical() {
		return true;
	}
}

@SuppressWarnings("rawtypes")
public class LDAPFastBind {

	public Hashtable env = null;
	public LdapContext ctx = null;
	public Control[] connCtls = null;

	@SuppressWarnings("unchecked")
	public LDAPFastBind(String ldapurl, Boolean followReferences, boolean activeDirectory) {
		env = new Hashtable();
		//This can make LDAP search slow : http://stackoverflow.com/questions/16412236/how-to-resolve-javax-naming-partialresultexception
		//env.put(Context.REFERRAL, "follow");
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		env.put(Context.SECURITY_AUTHENTICATION, "simple");
		env.put(Context.PROVIDER_URL, ldapurl);
		env.put("java.naming.ldap.attributes.binary", "tokenGroups objectSid");
		if (followReferences) {
			env.put(Context.REFERRAL, "follow");
		}

		if (StringUtils.startsWith(ldapurl, "ldaps")) {
			//env.put(Context.SECURITY_PROTOCOL, "ssl");
			env.put("java.naming.ldap.factory.socket", "com.constellio.model.services.users.sync.ldaps.DummySSLSocketFactory");
		}

		if (activeDirectory) {
			connCtls = new Control[] { new FastBindConnectionControl() };
		} else {
			connCtls = new Control[] {};
		}

		//first time we initialize the context, no credentials are supplied
		//therefore it is an anonymous bind.		

		/*try {
			ctx = new InitialLdapContext(env, connCtls);

		} catch (NamingException e) {
			throw new RuntimeNamingException(e.getMessage());
		}*/
		//FIX de Vincent pour o a q
		try {
			ctx = new InitialLdapContext(env, connCtls);
		} catch (NamingException e) {
			if (activeDirectory) {
				connCtls = new Control[] {};
				try {
					ctx = new InitialLdapContext(env, connCtls);
				} catch (NamingException e2) {
					throw new RuntimeException(e);
				}
			} else {
				throw new RuntimeException(e);
			}
		}
	}

	public boolean authenticate(String username, String password) {
		try {
			ctx.addToEnvironment(Context.SECURITY_PRINCIPAL, username);
			ctx.addToEnvironment(Context.SECURITY_CREDENTIALS, password);
			ctx.reconnect(connCtls);
			//			System.out.println(username + " is authenticated");
			return true;
		} catch (AuthenticationException e) {
			//			System.out.println(username + " is not authenticated");
			return false;
		} catch (NamingException e) {
			//			System.out.println(username + " is not authenticated");
			return false;
		}
	}

	public void close() {
		try {
			ctx.close();
		} catch (NamingException e) {
			throw new RuntimeNamingException(e.getMessage());
		}
	}

}

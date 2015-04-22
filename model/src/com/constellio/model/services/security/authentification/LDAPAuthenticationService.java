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
package com.constellio.model.services.security.authentification;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.ldap.Control;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.data.utils.hashing.HashingService;
import com.constellio.model.conf.ldap.LDAPConfigurationManager;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.data.dao.managers.StatefulService;
import com.constellio.model.conf.ldap.LDAPDirectoryType;
import com.constellio.model.conf.ldap.LDAPServerConfiguration;

public class LDAPAuthenticationService implements AuthenticationService, StatefulService {

	public static final String ADMIN_USERNAME = "admin";
	private LDAPServerConfiguration ldapServerConfiguration;
	private PasswordFileAuthenticationService adminAuthenticationService;
	private static final Logger LOGGER = LoggerFactory.getLogger(LDAPAuthenticationService.class);
	private LDAPConfigurationManager ldapConfigurationManager;
	private ConfigManager configManager;
	private HashingService hashingService;

	public Map<String, LdapContext> ldapContexts = new HashMap<>();
	public Control[] connCtls = null;

	public LDAPAuthenticationService(LDAPConfigurationManager ldapConfigurationManager, ConfigManager configManager, HashingService hashingService) {
		this.ldapConfigurationManager = ldapConfigurationManager;
		this.configManager = configManager;
		this.hashingService = hashingService;
	}

	@Override
	public void initialize() {
		this.adminAuthenticationService = new PasswordFileAuthenticationService(configManager, hashingService);
		this.ldapServerConfiguration = ldapConfigurationManager.getLDAPServerConfiguration();
	}

	@Override
	public void close() {
		for(LdapContext ctx:ldapContexts.values()){
			if (ctx != null) {
				try {
					ctx.close();
				}
				catch (NamingException e) {
					throw new RuntimeException("Context close failure ", e);
				}
			}
		}
	}

	@Override
	public boolean authenticate(String username, String password) {
		if (username.equals(ADMIN_USERNAME)){
			return adminAuthenticationService.authenticate(username, password);
		}
		boolean authenticated = false;
		for (String url : ldapServerConfiguration.getUrls()) {
			authenticated = authenticate(username, password, url);

			if (!authenticated) {
				for (String domain : ldapServerConfiguration.getDomains()) {
					String userAtDomain = username + "@" + domain;
					authenticated = authenticate(
							userAtDomain, password, url);
					if (authenticated) {
						break;
					}
				}
			}
			if (authenticated) {
				break;
			}
		}
		return authenticated;
	}

	private boolean authenticate(String username, String password, String url) {
		String domain = StringUtils.substringAfter(username, "@");

		if (!ldapContexts.containsKey(url)) {
			if (ldapServerConfiguration.getDirectoryType() == LDAPDirectoryType.ACTIVE_DIRECTORY) {
				connCtls = new Control[] { new FastBindConnectionControl() };
			} else {
				connCtls = new Control[] { };
			}

			//first time we initialize the context, no credentials are supplied
			//therefore it is an anonymous bind.

			try {
				Hashtable env = new Hashtable();
				env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
				env.put(Context.SECURITY_AUTHENTICATION, "simple");
				env.put(Context.PROVIDER_URL, url);
				InitialLdapContext context = new InitialLdapContext(env, connCtls);
				ldapContexts.put(url, context);
			} catch (NamingException e) {
				//				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}

		String[] securityPrincipals;
		if (ldapServerConfiguration.getDirectoryType() == LDAPDirectoryType.ACTIVE_DIRECTORY) {
			securityPrincipals = new String[] { username };

		} else {
			String[] prefixes = new String[] { "uid=", "cn=" };
			securityPrincipals = new String[prefixes.length];
			for (int i = 0; i < prefixes.length; i++) {
				String prefix = prefixes[i];
				String usernameBeforeDomain = StringUtils.substringBefore(username, "@");
				StringBuffer securityPrincipalSB = new StringBuffer();
				securityPrincipalSB.append(prefix);
				securityPrincipalSB.append(usernameBeforeDomain);
				securityPrincipalSB.append(",");
				securityPrincipalSB.append(domain);

				securityPrincipals[i] = securityPrincipalSB.toString();
			}
		}

		try {
			LdapContext context = ldapContexts.get(url);
			for (String securityPrincipal : securityPrincipals) {
				context.addToEnvironment(Context.SECURITY_PRINCIPAL, securityPrincipal);
				context.addToEnvironment(Context.SECURITY_CREDENTIALS, password);
				try {
					context.reconnect(connCtls);
					return true;
				} catch (Exception e) {
					LOGGER.warn("Exception when reconnecting", e);
				}
			}
			return false;

		} catch (AuthenticationException e) {
			LOGGER.warn("Exception when authenticating", e);
			return false;

		} catch (NamingException e) {
			LOGGER.warn("Naming exception", e);
			return false;
		}
	}

	@Override
	public boolean supportPasswordChange() {
		//Passwords are managed by an other server, Constellio cannot change them
		return false;
	}

	@Override
	public void changePassword(String username, String oldPassword, String newPassword) {
		throw new UnsupportedOperationException("Password modification is not supported when using LDAP");
	}

	@Override
	public void changePassword(String username, String newPassword) {
		throw new UnsupportedOperationException("Password modification is not supported when using LDAP");
	}

	@Override
	public void reloadServiceConfiguration() {
		this.ldapServerConfiguration = ldapConfigurationManager.getLDAPServerConfiguration();
	}

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
}

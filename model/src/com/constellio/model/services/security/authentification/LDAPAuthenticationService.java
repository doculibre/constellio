package com.constellio.model.services.security.authentification;

import com.constellio.data.dao.managers.StatefulService;
import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.data.utils.hashing.HashingService;
import com.constellio.model.conf.ldap.LDAPConfigurationManager;
import com.constellio.model.conf.ldap.LDAPDirectoryType;
import com.constellio.model.conf.ldap.config.LDAPServerConfiguration;
import com.constellio.model.conf.ldap.services.LDAPServices;
import com.constellio.model.conf.ldap.services.LDAPServicesFactory;
import com.constellio.model.conf.ldap.services.LDAPServicesImpl;
import com.constellio.model.services.users.SystemWideUserInfos;
import com.constellio.model.services.users.UserServices;
import com.constellio.model.services.users.UserServicesRuntimeException.UserServicesRuntimeException_NoSuchUser;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.ldap.Control;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import java.util.Collections;
import java.util.Hashtable;

public class LDAPAuthenticationService implements AuthenticationService, StatefulService {

	public static final String ADMIN_USERNAME = "admin";
	private LDAPServerConfiguration ldapServerConfiguration;
	private PasswordFileAuthenticationService adminAuthenticationService;
	private static final Logger LOGGER = LoggerFactory.getLogger(LDAPAuthenticationService.class);
	private LDAPConfigurationManager ldapConfigurationManager;
	private ConfigManager configManager;
	private HashingService hashingService;
	private final UserServices userServices;

	public Control[] connCtls = null;

	public LDAPAuthenticationService(LDAPConfigurationManager ldapConfigurationManager, ConfigManager configManager,
									 HashingService hashingService, UserServices userServices) {
		this.ldapConfigurationManager = ldapConfigurationManager;
		this.configManager = configManager;
		this.hashingService = hashingService;
		this.userServices = userServices;
	}

	@Override
	public void initialize() {
		this.adminAuthenticationService = new PasswordFileAuthenticationService(configManager, hashingService);
		this.ldapServerConfiguration = ldapConfigurationManager.getLDAPServerConfiguration();
	}

	@Override
	public void close() {
	}

	@Override
	public boolean authenticate(String username, String password) {
		if (username.equals(ADMIN_USERNAME)) {
			return adminAuthenticationService.authenticate(username, password);
		}
		if (StringUtils.isBlank(password)) {
			LOGGER.info("invalid blank password");
			return false;
		}
		return authenticateLDAPUser(username, password);
	}

	private boolean authenticateLDAPUser(String username, String password) {
		LDAPDirectoryType directoryType = ldapServerConfiguration.getDirectoryType();
		if (ldapServerConfiguration.getDirectoryType() == LDAPDirectoryType.AZURE_AD) {
			LDAPServices ldapServices = new LDAPServicesFactory().newLDAPServices(directoryType);
			String userEmail = userServices.getUserInfos(username).getEmail();
			try {
				ldapServices.authenticateUser(ldapServerConfiguration, userEmail, password);
				return true;
			} catch (Throwable e) {
				LOGGER.info("Error when trying to authenticate user " + username + " with email " + userEmail, e);
				return false;
			}
		} else {
			return authenticateDefaultLDAPUser(username, password);
		}
	}

	//TODO : refactoring move to LDAPServicesImpl
	private boolean authenticateDefaultLDAPUser(String username, String password) {
		boolean authenticated = false;
		for (String url : ldapServerConfiguration.getUrls()) {
			authenticated = authenticate(username, password, url);

			if (!authenticated) {
				/*if(ldapServerConfiguration.getDomains().size() != 1) {
					String searchedDomain = getUserDomain(username, url);
					if(StringUtils.isNotBlank(searchedDomain)){
						authenticated = authenticate(
								username + "@" + searchedDomain, password, url);
						if (authenticated) {
							break;
						}
					}
				//}*/

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
		try {
			String domain = StringUtils.substringAfter(username, "@");

			String[] securityPrincipals = getSecurityPrincipals(username, domain, url);

			Hashtable env = new Hashtable();
			env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
			env.put(Context.SECURITY_AUTHENTICATION, "simple");
			env.put(Context.PROVIDER_URL, url);
			env.put("java.naming.ldap.attributes.binary", "tokenGroups objectSid");
			if (this.ldapServerConfiguration.getFollowReferences()) {
				env.put(Context.REFERRAL, "follow");
			}
			if (StringUtils.startsWith(url, "ldaps")) {
				//env.put(Context.SECURITY_PROTOCOL, "ssl");
				env.put("java.naming.ldap.factory.socket",
						"com.constellio.model.services.users.sync.ldaps.DummySSLSocketFactory");
			}
			InitialLdapContext context = new InitialLdapContext(env, connCtls);
			for (String securityPrincipal : securityPrincipals) {
				context.addToEnvironment(Context.SECURITY_PRINCIPAL, securityPrincipal);
				context.addToEnvironment(Context.SECURITY_CREDENTIALS, password);
				try {
					context.reconnect(connCtls);
					return true;
				} catch (Exception e) {
					LOGGER.warn("Exception when reconnecting", e);
				} finally {
					context.close();
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

	private String[] getSecurityPrincipals(String username, String domain, String url)
			throws NamingException {
		String[] securityPrincipals;
		if (ldapServerConfiguration.getDirectoryType() == LDAPDirectoryType.ACTIVE_DIRECTORY) {
			return new String[]{username};
		} else if (ldapServerConfiguration.getDirectoryType() == LDAPDirectoryType.E_DIRECTORY) {
			String userDn = getUserDn(username, domain, url);
			return new String[]{userDn};
		} else {
			String[] prefixes = new String[]{"uid=", "cn="};
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
		return securityPrincipals;
	}

	private String getUserDn(String username, String domain, String url)
			throws NamingException {
		try {
			SystemWideUserInfos user = userServices.getUserInfos(username);
			if (StringUtils.isNotBlank(user.getDn())) {
				return user.getDn();
			}
		} catch (UserServicesRuntimeException_NoSuchUser e) {
			LOGGER.warn("Trying to authenticate non constellio user " + username, e);
		}

		String ldapUser = this.ldapConfigurationManager.getLDAPUserSyncConfiguration().getUser();
		String ldapPassword = this.ldapConfigurationManager.getLDAPUserSyncConfiguration().getPassword();

		LdapContext ldapContext = null;
		try {
			ldapContext = new LDAPServicesImpl()
					.connectToLDAP(Collections.EMPTY_LIST, url, ldapUser,
							ldapPassword, ldapServerConfiguration.getFollowReferences(), false);

			String usernameBeforeDomain = StringUtils.substringBefore(username, "@");
			return new LDAPServicesImpl().dnForEdirectoryUser(ldapContext, domain, usernameBeforeDomain);
		} finally {
			if (ldapContext != null) {
				ldapContext.close();
			}
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

	@SuppressWarnings("serial")
	class FastBindConnectionControl implements Control {

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

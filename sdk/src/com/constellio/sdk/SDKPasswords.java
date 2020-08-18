package com.constellio.sdk;

import com.constellio.data.utils.ConsoleLogger;
import com.constellio.data.utils.PropertyFileUtils;
import com.constellio.data.conf.FoldersLocator;
import org.junit.internal.AssumptionViolatedException;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

public class SDKPasswords {

	static Map<String, String> sdkPasswords;

	// SMB
	public static String testSmbServer() {
		loadCorrectIfRequired();
		return sdkPasswords.get("testSmb.server");
	}

	public static String testSmbShare() {
		loadCorrectIfRequired();
		return sdkPasswords.get("testSmb.share");
	}

	public static String testSmbDomain() {
		loadCorrectIfRequired();
		return sdkPasswords.get("testSmb.user.domain");
	}

	public static String testSmbUsername() {
		loadCorrectIfRequired();
		return sdkPasswords.get("testSmb.user.login");
	}

	public static String testSmbPassword() {
		loadCorrectIfRequired();
		return sdkPasswords.get("testSmb.user.password");
	}

	public static String testSmbShareA() {
		loadCorrectIfRequired();
		return sdkPasswords.get("testSmb.share.a");
	}

	public static String testSmbShareB() {
		loadCorrectIfRequired();
		return sdkPasswords.get("testSmb.share.b");
	}

	/*
	public static String testSmbLDAPServer() {
		loadCorrectIfRequired();
		return sdkPasswords.get("testSmb.LDAP.server");
	}
	*/

	public static String testSmbLDAPAllowTokens() {
		loadCorrectIfRequired();
		return sdkPasswords.get("testSmb.LDAP.tokens");
	}

	//
	// Exchange
	public static String testExchangeServer() {
		loadCorrectIfRequired();
		return sdkPasswords.get("testExchange.server");
	}

	public static String testExchangeLDAPPassword() {
		return sdkPasswords.get("testExchange.ldap.password");
	}

	public static String testExchangeLDAPServer() {
		return sdkPasswords.get("testExchange.ldap.server");
	}

	public static String testExchangeIndexingUsername() {
		loadCorrectIfRequired();
		return sdkPasswords.get("testExchange.indexing.username");
	}

	public static String testExchangeIndexingPassword() {
		loadCorrectIfRequired();
		return sdkPasswords.get("testExchange.indexing.password");
	}

	public static String testExchangeUsername() {
		loadCorrectIfRequired();
		return sdkPasswords.get("testExchange.user1.username");
	}

	public static String testExchangePassword() {
		loadCorrectIfRequired();
		return sdkPasswords.get("testExchange.user1.password");
	}

	public static String testExchangeEmail() {
		loadCorrectIfRequired();
		return sdkPasswords.get("testExchange.user1.emailAddress");
	}

	public static String testExchangeName() {
		loadCorrectIfRequired();
		return sdkPasswords.get("testExchange.user1.name");
	}

	public static String testExchangeSecondUsername() {
		loadCorrectIfRequired();
		return sdkPasswords.get("testExchange.user2.username");
	}

	public static String testExchangeSecondPassword() {
		loadCorrectIfRequired();
		return sdkPasswords.get("testExchange.user2.password");
	}

	public static String testExchangeSecondEmail() {
		loadCorrectIfRequired();
		return sdkPasswords.get("testExchange.user2.emailAddress");
	}

	//
	// IMAP
	public static String testIMAPUsername() {
		loadCorrectIfRequired();
		return sdkPasswords.get("testIMAP.login");
	}

	public static String testIMAPPassword() {
		loadCorrectIfRequired();
		return sdkPasswords.get("testIMAP.password");
	}

	public static String testPOP3Password() {
		loadCorrectIfRequired();
		return sdkPasswords.get("testPOP3.password");
	}

	public static String testPOP3Username() {
		loadCorrectIfRequired();
		return sdkPasswords.get("testPOP3.login");
	}

	public static String testSMTPUsername() {
		loadCorrectIfRequired();
		return sdkPasswords.get("testSmtp.login");
	}

	public static String testSMTPPassword() {
		loadCorrectIfRequired();
		return sdkPasswords.get("testSmtp.password");
	}

	public static String testSMTPOldPassword() {
		loadCorrectIfRequired();
		return sdkPasswords.get("testSmtp.oldPassword");
	}

	public static String testSMTPOldUsername() {
		loadCorrectIfRequired();
		return sdkPasswords.get("testSmtp.oldUsername");
	}

	public static String testIMAPExtractionPassword() {
		loadCorrectIfRequired();
		return sdkPasswords.get("testIMAPExtraction.password");
	}

	public static String testIMAPExtractionUsername() {
		loadCorrectIfRequired();
		return sdkPasswords.get("testIMAPExtraction.login");
	}

	//
	// LDAP
	public static String testLDAPServer() {
		loadCorrectIfRequired();
		return sdkPasswords.get("testLDAP.server");
	}

	public static String testLDAPPassword() {
		loadCorrectIfRequired();
		return sdkPasswords.get("testLDAP.password");
	}

	//
	// Sharepoint
	public static String testSharepointHost() {
		loadCorrectIfRequired();
		return sdkPasswords.get("testSharepoint.host");
	}

	public static String testSharepointPort() {
		loadCorrectIfRequired();
		return sdkPasswords.get("testSharepoint.port");
	}

	public static String testSharepointDomain() {
		loadCorrectIfRequired();
		return sdkPasswords.get("testSharepoint.domain");
	}

	public static String testSharepointUsername() {
		loadCorrectIfRequired();
		return sdkPasswords.get("testSharepoint.username");
	}

	public static String testSharepointPassword() {
		loadCorrectIfRequired();
		return sdkPasswords.get("testSharepoint.password");
	}

	public static String testSharepointSiteUrl() {
		loadCorrectIfRequired();
		return sdkPasswords.get("testSharepoint.siteUrl");
	}

	public static String testSharepointUsernameWithLimitedPermissions() {
		loadCorrectIfRequired();
		return sdkPasswords.get("testSharepoint.username.limited");
	}

	public static String testSharepointPasswordWithLimitedPermissions() {
		loadCorrectIfRequired();
		return sdkPasswords.get("testSharepoint.password.limited");
	}


	public static String testLDAPSServer() {
		loadCorrectIfRequired();
		return sdkPasswords.get("testLDAPS.server");
	}

	// SSL
	public static String sslPort() {
		loadCorrectIfRequired();
		return sdkPasswords.get("ssl.port");
	}

	public static String sslKeystorePassword() {
		loadCorrectIfRequired();
		return sdkPasswords.get("sslKeystore.password");
	}

	//Azure AD
	public static String testAzureSynchClientId() {
		loadCorrectIfRequired();
		return sdkPasswords.get("testAzure.synch.clientId");
	}

	public static String testAzureTenantName() {
		loadCorrectIfRequired();
		return sdkPasswords.get("testAzure.tenantName");
	}

	public static String testAzureSynchApplicationKey() {
		loadCorrectIfRequired();
		return sdkPasswords.get("testAzure.synch.applicationKey");
	}

	public static String testAzureAuthenticationApplicationId() {
		loadCorrectIfRequired();
		return sdkPasswords.get("testAzure.authentication.clientId");
	}

	// Azure AD SSO
	public static String testAzureAdSSOClientId() {
		return getValue("testAzureAdSSO.clientId");
	}

	public static String testAzureAdSSOClientSecret() {
		return getValue("testAzureAdSSO.clientSecret");
	}

	public static String testAzureAdSSOTenant() {
		return getValue("testAzureAdSSO.tenant");
	}

	//
	// Utils

	// Azure Account credentials
	public static String testAzureAccountName() {
		return getValue("testAzure.accountName");
	}

	public static String testAzureAccountPassword() {
		return getValue("testAzure.accountPassword");
	}

	public static String testAzureConnectionString() {
		return getValue("testAzure.connectionString");
	}
	//

	private synchronized static void loadCorrectIfRequired() {
		if (sdkPasswords == null) {
			File sdkPasswordsFile = new File(new FoldersLocator().getPluginsSDKProject(), "sdkpasswords.properties");
			if (sdkPasswordsFile.exists()) {
				sdkPasswords = PropertyFileUtils.loadKeyValues(sdkPasswordsFile);
				loadOrCreateUserPasswords();

			} else {
				throw new AssumptionViolatedException("Test is skipped");
			}
		}
	}

	private synchronized static void loadOrCreateUserPasswords() {
		File sdkPasswordsUserFile = new File(new FoldersLocator().getSDKProject(), "sdkpasswords_user.properties");
		if (sdkPasswordsUserFile.exists()) {
			sdkPasswords.putAll(PropertyFileUtils.loadKeyValues(sdkPasswordsUserFile));
		} else {
			try {
				PropertyFileUtils.store(new Properties(), sdkPasswordsUserFile);
			} catch (IOException ioe) {
				ConsoleLogger.log("Couldn't create file " + sdkPasswordsUserFile.getPath());
			}
		}
	}

	private static String getValue(String key) {
		loadCorrectIfRequired();
		return sdkPasswords.get(key);
	}
}

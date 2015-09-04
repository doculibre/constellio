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
package com.constellio.sdk;

import java.io.File;
import java.util.Map;

import com.constellio.data.utils.PropertyFileUtils;
import com.constellio.model.conf.FoldersLocator;

public class SDKPasswords {

	static Map<String, String> sdkPasswords;

	public static String testLDAPServer() {
		loadIfRequired();
		return sdkPasswords.get("testLdapServer");
	}

	public static String testSMTPServerPassword() {
		loadIfRequired();
		return sdkPasswords.get("testSmtpServer.password");
	}

	private synchronized static void loadIfRequired() {
		if (sdkPasswords == null) {
			File sdkPasswordsFile = new File(new FoldersLocator().getSDKProject(), "sdkpasswords.properties");
			sdkPasswords = PropertyFileUtils.loadKeyValues(sdkPasswordsFile);
		}
	}

	public static String testPOP3Server() {
		loadIfRequired();
		return sdkPasswords.get("testPOP3Server");
	}

	public static String testSMTPServerUsername() {
		loadIfRequired();
		return sdkPasswords.get("testSmtpServer.login");
	}

	public static String testEmailAccount() {
		loadIfRequired();
		return sdkPasswords.get("testEmailAccount");
	}

	public static String testIMAPServerUsername() {
		loadIfRequired();
		return sdkPasswords.get("testPOP3Server.login");
	}
	
	public static String testSmbShare() {
		loadIfRequired();
		return sdkPasswords.get("testSmbShare");
	}
	
	public static String testSmbDomain() {
		loadIfRequired();
		return sdkPasswords.get("testSmbUser.domain");
	}
	
	public static String testSmbUsername() {
		loadIfRequired();
		return sdkPasswords.get("testSmbUser.login");
	}
	
	public static String testSmbPassword() {
		loadIfRequired();
		return sdkPasswords.get("testSmbUser.password");
	}
}

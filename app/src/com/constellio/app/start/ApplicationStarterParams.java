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
package com.constellio.app.start;

import java.io.File;

public class ApplicationStarterParams {

	boolean joinServerThread;
	File webContentDir;
	int port;

	String keystorePassword;

	public boolean isJoinServerThread() {
		return joinServerThread;
	}

	public ApplicationStarterParams setJoinServerThread(boolean joinServerThread) {
		this.joinServerThread = joinServerThread;
		return this;
	}

	public File getWebContentDir() {
		return webContentDir;
	}

	public ApplicationStarterParams setWebContentDir(File webContentDir) {
		this.webContentDir = webContentDir;
		return this;
	}

	public int getPort() {
		return port;
	}

	public ApplicationStarterParams setPort(int port) {
		this.port = port;
		return this;
	}

	public boolean isSSL() {
		return keystorePassword != null;
	}

	public String getKeystorePassword() {
		return keystorePassword;
	}

	public ApplicationStarterParams setSSLWithKeystorePassword(String keystorePassword) {
		this.keystorePassword = keystorePassword;
		return this;
	}
}

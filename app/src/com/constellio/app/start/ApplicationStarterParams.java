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

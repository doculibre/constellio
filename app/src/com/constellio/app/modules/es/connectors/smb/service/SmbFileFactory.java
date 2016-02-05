package com.constellio.app.modules.es.connectors.smb.service;

import java.net.MalformedURLException;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

public interface SmbFileFactory {
	SmbFile getSmbFile(String url, NtlmPasswordAuthentication auth)
			throws MalformedURLException;

	SmbFile createSmbFile(String url, NtlmPasswordAuthentication auth)
			throws MalformedURLException, SmbException;

	SmbFile createSmbFolder(String url, NtlmPasswordAuthentication auth)
			throws MalformedURLException, SmbException;
}
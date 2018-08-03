package com.constellio.app.modules.es.connectors.smb.service;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

import java.net.MalformedURLException;

public interface SmbFileFactory {
	SmbFile getSmbFile(String url, NtlmPasswordAuthentication auth)
			throws MalformedURLException;

	SmbFile createSmbFile(String url, NtlmPasswordAuthentication auth)
			throws MalformedURLException, SmbException;

	SmbFile createSmbFolder(String url, NtlmPasswordAuthentication auth)
			throws MalformedURLException, SmbException;
}
package com.constellio.app.modules.es.connectors.smb.service;

import java.net.MalformedURLException;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

public class SmbFileFactoryImpl implements SmbFileFactory {
	@Override
	public SmbFile getSmbFile(String url, NtlmPasswordAuthentication auth)
			throws MalformedURLException {
		return new SmbFile(url, auth);
	}

	@Override
	public SmbFile createSmbFile(String url, NtlmPasswordAuthentication auth)
			throws MalformedURLException, SmbException {
		SmbFile file = new SmbFile(url, auth);

		if (!file.exists()) {
			file.createNewFile();
		}
		return file;
	}

	@Override
	public SmbFile createSmbFolder(String url, NtlmPasswordAuthentication auth)
			throws MalformedURLException, SmbException {
		SmbFile file = new SmbFile(url, auth);
		if (!file.exists()) {
			file.mkdir();
		}
		return file;
	}
}
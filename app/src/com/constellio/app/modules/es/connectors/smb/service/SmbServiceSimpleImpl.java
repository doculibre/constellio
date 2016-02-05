package com.constellio.app.modules.es.connectors.smb.service;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileFilter;

import org.apache.commons.lang3.StringUtils;

import com.constellio.app.modules.es.connectors.smb.config.SmbRetrievalConfiguration;
import com.constellio.app.modules.es.connectors.smb.security.Credentials;
import com.constellio.app.modules.es.connectors.smb.security.TrusteeManager;
import com.constellio.app.modules.es.connectors.smb.security.WindowsPermissions;
import com.constellio.app.modules.es.connectors.smb.security.WindowsPermissionsFactory;
import com.constellio.app.modules.es.connectors.smb.security.WindowsPermissionsFactoryImpl;
import com.constellio.app.modules.es.connectors.smb.service.SmbFileDTO.SmbFileDTOStatus;
import com.constellio.app.modules.es.connectors.smb.utils.ConnectorSmbUtils;
import com.constellio.app.modules.es.connectors.smb.utils.SmbUrlComparator;
import com.constellio.app.modules.es.connectors.spi.ConnectorLogger;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;

public class SmbServiceSimpleImpl implements SmbService {
	private NtlmPasswordAuthentication auth;
	private SmbFileFactory smbFactory;
	private SmbRetrievalConfiguration smbRetrievalConfiguration;
	private ConnectorSmbUtils smbUtils;
	private ConnectorLogger logger;
	private ESSchemasRecordsServices es;
	private WindowsPermissionsFactory permissionsFactory;
	private SmbUrlComparator urlComparator;

	public SmbServiceSimpleImpl(Credentials credentials, SmbRetrievalConfiguration smbRetrievalConfiguration, ConnectorSmbUtils smbUtils,
			ConnectorLogger logger, ESSchemasRecordsServices es) {
		this(credentials, smbRetrievalConfiguration, smbUtils, logger, es, new WindowsPermissionsFactoryImpl(new TrusteeManager()), new SmbFileFactoryImpl());
	}

	public SmbServiceSimpleImpl(Credentials credentials, SmbRetrievalConfiguration smbRetrievalConfiguration, ConnectorSmbUtils smbUtils,
			ConnectorLogger logger, ESSchemasRecordsServices es, WindowsPermissionsFactory permissionsFactory, SmbFileFactory smbFactory) {
		this.auth = new NtlmPasswordAuthentication(credentials.getDomain(), credentials.getUsername(), credentials.getPassword());
		this.smbFactory = smbFactory;
		this.smbRetrievalConfiguration = smbRetrievalConfiguration;
		this.smbUtils = smbUtils;
		this.logger = logger;
		this.es = es;
		this.permissionsFactory = permissionsFactory;
		this.urlComparator = new SmbUrlComparator();
	}

	@Override
	public SmbFileDTO getSmbFileDTO(String url) {
		return getSmbFileDTO(url, true);
	}

	@Override
	public SmbFileDTO getSmbFileDTO(String url, boolean withAttachment) {
		SmbFileDTOSimpleBuilder dtoBuilder = new SmbFileDTOSimpleBuilder(logger, es, permissionsFactory);
		SmbFileDTO smbFileDTO = new SmbFileDTO();
		try {
			SmbFile smbFile = smbFactory.getSmbFile(url, auth);
			smbFileDTO = dtoBuilder.build(smbFile, withAttachment);

			if (StringUtils.isEmpty(smbFileDTO.getUrl())) {
				smbFileDTO.setUrl(url);
			}
		} catch (MalformedURLException e) {
			logger.error("getSmbFileDTO : ", smbUtils.getStackTrace(e), new LinkedHashMap<String, String>());
			smbFileDTO.setUrl(url);
			smbFileDTO.setStatus(SmbFileDTOStatus.FAILED_DTO);
		}

		return smbFileDTO;
	}

	@Override
	public List<String> getChildrenUrlsFor(String url) {
		List<String> urls = new ArrayList<>();

		SmbFile smbFile;
		try {
			smbFile = smbFactory.getSmbFile(url, auth);
			SmbFile[] filesAndFolders = smbFile.listFiles(new CustomSmbFileFilter());
			for (SmbFile fileOrFolder : filesAndFolders) {
				urls.add(fileOrFolder.getCanonicalPath());
			}
			Collections.sort(urls, urlComparator);
		} catch (MalformedURLException e) {
			logger.error("getChildrenUrlsFor : ", smbUtils.getStackTrace(e), new LinkedHashMap<String, String>());
		} catch (SmbException e) {
			logger.error("getChildrenUrlsFor : ", smbUtils.getStackTrace(e), new LinkedHashMap<String, String>());
		} catch (Exception e) {
			logger.error("getChildrenUrlsFor : ", smbUtils.getStackTrace(e), new LinkedHashMap<String, String>());
		}

		return urls;
	}

	private class CustomSmbFileFilter implements SmbFileFilter {
		@Override
		public boolean accept(SmbFile file)
				throws SmbException {
			boolean result = false;
			try {
				String url = file.getCanonicalPath();
				result = smbUtils.isAccepted(url, smbRetrievalConfiguration);
				// logger.info("SmbServiceImpl isAccepted value : " + result, url, new LinkedHashMap<String, String>());
			} catch (Exception e) {
				logger.error("Filtering at SmbService level failed", "", new LinkedHashMap<String, String>());
			}

			return result;
		}
	}

	@Override
	public SmbModificationIndicator getModificationIndicator(String url) {
		SmbModificationIndicator smbModificationIndicator = new SmbModificationIndicator();
		SmbFile smbFile;
		try {
			smbFile = smbFactory.getSmbFile(url, auth);

			smbFile.exists();

			smbModificationIndicator.setLastModified(smbFile.getLastModified());

			WindowsPermissions windowsPermissions = permissionsFactory.newWindowsPermissions(smbFile);
			windowsPermissions.process();
			smbModificationIndicator.setPermissionsHash(windowsPermissions.getPermissionsHash());
			smbModificationIndicator.setSize(smbFile.length());

		} catch (MalformedURLException e) {
			logger.error("getModificationInformation : ", smbUtils.getStackTrace(e), new LinkedHashMap<String, String>());
		} catch (SmbException e) {
			logger.error("getModificationInformation : ", smbUtils.getStackTrace(e), new LinkedHashMap<String, String>());
		} catch (Exception e) {
			logger.error("getModificationInformation : ", smbUtils.getStackTrace(e), new LinkedHashMap<String, String>());
		}

		return smbModificationIndicator;
	}
}
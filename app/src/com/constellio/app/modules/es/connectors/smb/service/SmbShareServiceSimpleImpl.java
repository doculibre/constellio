package com.constellio.app.modules.es.connectors.smb.service;

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
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileFilter;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

public class SmbShareServiceSimpleImpl implements SmbShareService {
	private NtlmPasswordAuthentication auth;
	private SmbFileFactory smbFactory;
	private SmbRetrievalConfiguration smbRetrievalConfiguration;
	private ConnectorSmbUtils smbUtils;
	private ConnectorLogger logger;
	private ESSchemasRecordsServices es;
	private WindowsPermissionsFactory permissionsFactory;
	private SmbUrlComparator urlComparator;

	public SmbShareServiceSimpleImpl(Credentials credentials, SmbRetrievalConfiguration smbRetrievalConfiguration,
									 ConnectorSmbUtils smbUtils,
									 ConnectorLogger logger, ESSchemasRecordsServices es) {
		this(credentials, smbRetrievalConfiguration, smbUtils, logger, es,
				new WindowsPermissionsFactoryImpl(new TrusteeManager(), smbRetrievalConfiguration.isSkipSharePermissions(), smbRetrievalConfiguration.isSkipContentAndAcl()),
				new SmbFileFactoryImpl());
	}

	public SmbShareServiceSimpleImpl(Credentials credentials, SmbRetrievalConfiguration smbRetrievalConfiguration,
									 ConnectorSmbUtils smbUtils,
									 ConnectorLogger logger, ESSchemasRecordsServices es,
									 WindowsPermissionsFactory permissionsFactory, SmbFileFactory smbFactory) {
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
		return getSmbFileDTO(url, !smbRetrievalConfiguration.isSkipContentAndAcl());
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
				//http://issues.constellio.com/browse/CONSTELLIOEIM-933
				Object value = getUNC(smbFile, fileOrFolder);
				String fileOrFolderUrl = fileOrFolder.getCanonicalPath();
				if (value != null) {
					String realName = StringUtils.substringAfterLast(value.toString(), "\\");
					if (fileOrFolderUrl.endsWith("/")) {
						realName += "/";
					}
					if (!fileOrFolderUrl.endsWith(realName)) {
						String correctedUrl = StringUtils.removeEnd(fileOrFolderUrl, "/");
						correctedUrl = StringUtils.substringBeforeLast(correctedUrl, "/");
						correctedUrl += "/" + realName;
						urls.add(correctedUrl);
					} else {
						urls.add(fileOrFolderUrl);
					}
				} else {
					urls.add(fileOrFolderUrl);
				}
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

	Object getUNC(SmbFile smbFile, SmbFile fileOrFolder)
			throws NoSuchFieldException, IllegalAccessException {
		Field field = smbFile.getClass().getDeclaredField("unc");
		field.setAccessible(true);
		return field.get(fileOrFolder);
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
		long lastModified = 0;
		String permissionHash = "";
		double size = -4;

		SmbFile smbFile;
		try {
			smbFile = smbFactory.getSmbFile(url, auth);
			smbFile.exists();
			lastModified = smbFile.getLastModified();

			WindowsPermissions windowsPermissions = permissionsFactory.newWindowsPermissions(smbFile);
			windowsPermissions.process();
			permissionHash = windowsPermissions.getPermissionsHash();
			if (smbFile.isDirectory()) {
				size = 0;
			} else {
				size = smbFile.length();
			}

			return new SmbModificationIndicator(permissionHash, size, lastModified);
		} catch (Exception e) {
			logger.error("getModificationIndicator : ", smbUtils.getStackTrace(e), new LinkedHashMap<String, String>());
			return null;
		}
	}
}
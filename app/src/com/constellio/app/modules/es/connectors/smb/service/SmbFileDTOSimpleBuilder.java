package com.constellio.app.modules.es.connectors.smb.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.constellio.app.modules.es.connectors.smb.security.WindowsPermissions;
import com.constellio.app.modules.es.connectors.smb.security.WindowsPermissionsFactory;
import com.constellio.app.modules.es.connectors.smb.service.SmbFileDTO.SmbFileDTOStatus;
import com.constellio.app.modules.es.connectors.spi.ConnectorLogger;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.ParsedContent;
import com.constellio.model.services.parser.FileParser;
import com.constellio.model.services.parser.FileParserException;

import jcifs.smb.SmbFile;

public class SmbFileDTOSimpleBuilder {
	private static final long MAX_FILE_SIZE_IN_BYTES = 1024 * 1024 * 1024;
	private static final String ERROR_TOO_BIG = "File exceeds maximum size (in bytes): " + MAX_FILE_SIZE_IN_BYTES;

	private final ConnectorLogger logger;
	private final ESSchemasRecordsServices es;
	private final WindowsPermissionsFactory permissionsFactory;

	public SmbFileDTOSimpleBuilder(ConnectorLogger logger, ESSchemasRecordsServices es, WindowsPermissionsFactory permissionsFactory) {
		this.logger = logger;
		this.es = es;
		this.permissionsFactory = permissionsFactory;
	}

	public SmbFileDTO build(SmbFile smbFile, boolean withContent) {
		SmbFileDTO smbFileDTO = new SmbFileDTO();
		InputStream inputStream = null;
		String url = null;
		try {
			smbFileDTO.setStatus(SmbFileDTOStatus.FULL_DTO);
			smbFileDTO.setLastFetchAttempt(TimeProvider.getLocalDateTime());
			url = smbFile.getCanonicalPath();
			smbFileDTO.setUrl(url);

			if (!smbFile.exists()) {
				smbFileDTO.setStatus(SmbFileDTOStatus.DELETE_DTO);
			} else {
				smbFileDTO.setCreateTime(smbFile.createTime());
				smbFileDTO.setLastModified(smbFile.getLastModified());

				WindowsPermissions windowsPermissions = getWindowsPermissions(smbFile);
				windowsPermissions.process();
				if (!StringUtils.isBlank(windowsPermissions.getErrors())) {
					throw new IOException(windowsPermissions.getErrors());
				}
				List<String> allowTokens = prependedTokenListOrNullOnEmptyList(windowsPermissions.getAllowTokenDocument());
				smbFileDTO.setAllowTokens(allowTokens);
				List<String> denyTokens = prependedTokenListOrNullOnEmptyList(windowsPermissions.getDenyTokenDocument());
				smbFileDTO.setDenyTokens(denyTokens);
				List<String> allowShareTokens = prependedTokenListOrNullOnEmptyList(windowsPermissions.getAllowTokenShare());
				smbFileDTO.setAllowShareTokens(allowShareTokens);
				List<String> denyShareTokens = prependedTokenListOrNullOnEmptyList(windowsPermissions.getDenyTokenShare());
				smbFileDTO.setDenyShareTokens(denyShareTokens);
				smbFileDTO.setPermissionsHash(windowsPermissions.getPermissionsHash());

				smbFileDTO.setIsFile(smbFile.isFile());
				smbFileDTO.setIsDirectory(smbFile.isDirectory());

				if (smbFileDTO.isFile()) {
					smbFileDTO.setExtension(StringUtils.defaultIfBlank(StringUtils.substringAfterLast(smbFile.getName(), "."), ""));
					smbFileDTO.setName(smbFile.getName());
					smbFileDTO.setLength(smbFile.length());

					if (withContent) {
						if (smbFileDTO.getLength() > 0 && smbFileDTO.getLength() <= MAX_FILE_SIZE_IN_BYTES) {
							inputStream = smbFile.getInputStream();
							try {
								ParsedContent parsedContent = updateParsedContent(smbFileDTO, inputStream);
								smbFileDTO.setLanguage(parsedContent.getLanguage());
							} catch (Throwable t) {
								smbFileDTO.setParsedContent("");
								smbFileDTO.setLanguage("");
							}
						} else {
							if (smbFileDTO.getLength() > MAX_FILE_SIZE_IN_BYTES) {
								smbFileDTO.setErrorMessage(ERROR_TOO_BIG);
								smbFileDTO.setStatus(SmbFileDTOStatus.FAILED_DTO);
							} else {
								smbFileDTO.setParsedContent("");
								smbFileDTO.setLanguage("");
							}
						}
					}
				} else {
					String folderNameWithoutTrailingSlash = StringUtils.removeEnd(smbFile.getName(), "/");
					smbFileDTO.setName(folderNameWithoutTrailingSlash);
					smbFileDTO.setLength(0);
					smbFileDTO.setParsedContent("");
					smbFileDTO.setLanguage("");
				}
			}
		} catch (IOException e) {
			logger.error(new SmbLoggedException(url, e));
			smbFileDTO.setStatus(SmbFileDTOStatus.FAILED_DTO);
			smbFileDTO.setErrorMessage(e.getMessage());
		} catch (Throwable t) {
			logger.errorUnexpected(t);
			smbFileDTO.setStatus(SmbFileDTOStatus.FAILED_DTO);
			smbFileDTO.setErrorMessage(t.getMessage());
		} finally {
			es.getIOServices()
					.closeQuietly(inputStream);
		}

		return smbFileDTO;
	}

	protected WindowsPermissions getWindowsPermissions(SmbFile smbFile) {
		return permissionsFactory.newWindowsPermissions(smbFile);
	}

	private List<String> prependedTokenListOrNullOnEmptyList(List<String> initialList) {
		if (initialList == null || initialList.isEmpty() || initialList.contains("DEAD_AUTHORITY")) {
			return null;
		} else {
			return prependReadAndAuthorizationTypeToAllSids(initialList);
		}
	}

	private List<String> prependReadAndAuthorizationTypeToAllSids(List<String> initialSids) {
		String read = "r";
		String authorizationType = "ad";

		if (initialSids != null) {
			List<String> modifiedSids = new ArrayList<String>();
			for (String sid : initialSids) {
				modifiedSids.add(read + "," + authorizationType + "," + sid);
			}
			return modifiedSids;
		} else {
			return null;
		}
	}

	private ParsedContent updateParsedContent(SmbFileDTO smbFileDTO, InputStream inputStream)
			throws FileParserException {
		FileParser fileParser = es.getModelLayerFactory()
				.newFileParser();
		ParsedContent parsedContent = fileParser.parse(inputStream, true);

		smbFileDTO.setParsedContent(parsedContent.getParsedContent());

		return parsedContent;
	}
}
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
package com.constellio.app.modules.es.connectors.smb;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileFilter;

import org.apache.commons.lang.StringUtils;

import com.constellio.app.modules.es.connectors.spi.ConnectorLogger;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.model.entities.records.ParsedContent;
import com.constellio.model.services.parser.FileParser;
import com.constellio.model.services.parser.FileParserException.FileParserException_CannotParse;

public class SmbService {
	private static final long MAX_FILE_SIZE_IN_BYTES = 1024 * 1024 * 5;
	private NtlmPasswordAuthentication auth;
	private ESSchemasRecordsServices es;
	private TrusteeManager trusteeManager;
	private ConnectorLogger logger;
	private ConnectorSmbUtils smbUtils;
	private List<String> seeds;
	private List<String> inclusions;
	private List<String> exclusions;

	public static enum SmbStatus {
		OK, GONE, UNKNOWN, FAIL, PARTIAL;
	};

	public SmbService(String domain, String username, String password, List<String> seeds, List<String> inclusions, List<String> exclusions,
			ESSchemasRecordsServices es, ConnectorLogger logger) {
		auth = new NtlmPasswordAuthentication(domain, username, password);
		this.es = es;
		trusteeManager = new TrusteeManager();
		this.logger = logger;
		smbUtils = new ConnectorSmbUtils(es);
		this.seeds = seeds;
		this.inclusions = inclusions;
		this.exclusions = exclusions;
	}

	public SmbFileDTO getSmbFileDTO(String smbUrl) {
		try {
			SmbFile smbFile = getSmbFile(smbUrl);
			return getSmbFileDTO(smbFile, true);
		} catch (MalformedURLException e) {
			logger.error("SmbFile creation failed for url : " + smbUrl, e.getMessage(), new LinkedHashMap<String, String>());
			return new SmbFileDTO().setUrl(smbUrl)
					.setStatus(SmbStatus.FAIL);
		}
	}

	protected SmbFileDTO getSmbFileDTO(SmbFile smbFile, boolean withContent) {
		SmbFileDTO smbFileDTO = new SmbFileDTO();
		smbFileDTO.setLastFetchAttempt(System.currentTimeMillis());
		// TODO Benoit. Look into CopyInputStreamFactory
		InputStream inputStream = null;
		try {
			setUrl(smbFile, smbFileDTO);

			SmbStatus smbStatus = chooseStatus(smbFile);
			smbFileDTO.setStatus(smbStatus);

			setLastModified(smbFile, smbFileDTO);

			setPermissionsHash(smbFile, smbFileDTO);

			if (smbFile.isFile()) {
				setIsFile(smbFileDTO, true);
				setIsDirectory(smbFileDTO, false);

				long size = setSize(smbFile, smbFileDTO);

				setExtension(smbFile, smbFileDTO);

				setName(smbFile, smbFileDTO);

				if (withContent && size <= MAX_FILE_SIZE_IN_BYTES) {
					// TODO. Benoit/Francis/Nicolas. Check if parsing can be done outside connector.
					inputStream = smbFile.getInputStream();

					ParsedContent parsedContent = setParsedContent(smbFile, smbFileDTO, inputStream);

					setLanguage(smbFileDTO, parsedContent);
				} else {
					setEmptyParsedContent(smbFileDTO);
				}
			} else {
				smbFileDTO.getMissingMetadatas()
						.remove(SmbFileDTO.LANGUAGE);
				smbFileDTO.getMissingMetadatas()
						.remove(SmbFileDTO.EXTENSION);

				setIsFile(smbFileDTO, false);
				setIsDirectory(smbFileDTO, true);

				setZeroSize(smbFileDTO);

				setName(smbFile, smbFileDTO);

				setEmptyParsedContent(smbFileDTO);
			}

		} catch (Exception e) {
			if (smbFileDTO.getMissingMetadatas()
					.contains(SmbFileDTO.PERMISSIONS_HASH)) {
				smbFileDTO.setStatus(SmbStatus.FAIL);
				logger.error("SmbFileDTO creation failed for : " + smbFileDTO.getUrl(), "With " + e.getMessage(), new LinkedHashMap<String, String>());
			} else {
				smbFileDTO.setStatus(SmbStatus.PARTIAL);
				logger.info("Partial SmbFileDTO  for : " + smbFileDTO.getUrl(), "With " + e.getMessage() + " Also, the following metadatas are missing : "
						+ smbFileDTO.getMissingMetadatas(), new LinkedHashMap<String, String>());
			}
			smbFileDTO.setErrorMessage(e.getMessage());
		} finally {
			es.getIOServices()
					.closeQuietly(inputStream);
		}

		if (smbFileDTO.getStatus() != SmbStatus.FAIL && !smbFileDTO.getMissingMetadatas()
				.isEmpty()) {
			smbFileDTO.setStatus(SmbStatus.PARTIAL);
		}
		return smbFileDTO;
	}

	private ParsedContent setParsedContent(SmbFile smbFile, SmbFileDTO smbFileDTO, InputStream inputStream)
			throws FileParserException_CannotParse {
		FileParser fileParser = es.getModelLayerFactory()
				.newFileParser();
		int contentLength = smbFile.getContentLength();
		ParsedContent parsedContent = fileParser.parse(inputStream, contentLength);

		smbFileDTO.setParsedContent(parsedContent.getParsedContent());
		smbFileDTO.getMissingMetadatas()
				.remove(SmbFileDTO.PARSED_CONTENT);
		return parsedContent;
	}

	private void setLanguage(SmbFileDTO smbFileDTO, ParsedContent parsedContent) {
		smbFileDTO.setLanguage(parsedContent.getLanguage());
		smbFileDTO.getMissingMetadatas()
				.remove(SmbFileDTO.LANGUAGE);
	}

	private void setEmptyParsedContent(SmbFileDTO smbFileDTO) {
		smbFileDTO.setParsedContent("");
		smbFileDTO.getMissingMetadatas()
				.remove(SmbFileDTO.PARSED_CONTENT);
	}

	private void setZeroSize(SmbFileDTO smbFileDTO) {
		smbFileDTO.setLength(0L);
		smbFileDTO.getMissingMetadatas()
				.remove(SmbFileDTO.SIZE);
	}

	private void setUrl(SmbFile smbFile, SmbFileDTO smbFileDTO) {
		String canonicalPath = smbFile.getCanonicalPath();
		smbFileDTO.setUrl(canonicalPath);
		smbFileDTO.getMissingMetadatas()
				.remove(SmbFileDTO.URL);
	}

	private void setLastModified(SmbFile smbFile, SmbFileDTO smbFileDTO) {
		smbFileDTO.setLastModified(smbFile.getLastModified());
		smbFileDTO.getMissingMetadatas()
				.remove(SmbFileDTO.LAST_MODIFIED);
	}

	private void setPermissionsHash(SmbFile smbFile, SmbFileDTO smbFileDTO) {
		WindowsPermissions windowsPermissions = new WindowsPermissions(smbFile, trusteeManager);
		smbFileDTO.setPermissionsHash(windowsPermissions.getPermissionsHash());
		smbFileDTO.getMissingMetadatas()
				.remove(SmbFileDTO.PERMISSIONS_HASH);
	}

	private void setIsFile(SmbFileDTO smbFileDTO, boolean value) {
		smbFileDTO.setIsFile(value);
		smbFileDTO.getMissingMetadatas()
				.remove(SmbFileDTO.IS_FILE);
	}

	private void setIsDirectory(SmbFileDTO smbFileDTO, boolean value) {
		smbFileDTO.setIsDirectory(value);
		smbFileDTO.getMissingMetadatas()
				.remove(SmbFileDTO.IS_DIRECTORY);
	}

	private long setSize(SmbFile smbFile, SmbFileDTO smbFileDTO)
			throws SmbException {
		long size = smbFile.length();
		smbFileDTO.setLength(size);
		smbFileDTO.getMissingMetadatas()
				.remove(SmbFileDTO.SIZE);
		return size;
	}

	private void setExtension(SmbFile smbFile, SmbFileDTO smbFileDTO) {
		String extension = StringUtils.defaultIfBlank(StringUtils.substringAfterLast(smbFile.getName(), "."), "");
		smbFileDTO.setExtension(extension);
		smbFileDTO.getMissingMetadatas()
				.remove(SmbFileDTO.EXTENSION);
	}

	private void setName(SmbFile smbFile, SmbFileDTO smbFileDTO) {
		smbFileDTO.setName(smbFile.getName());
		smbFileDTO.getMissingMetadatas()
				.remove(SmbFileDTO.NAME);
	}

	// Smb error codes
	// https://msdn.microsoft.com/en-us/library/ee441884.aspx
	private SmbStatus chooseStatus(SmbFile smbFile) {
		SmbStatus smbStatus;
		try {
			boolean exists = smbFile.exists();
			if (exists) {
				smbStatus = SmbStatus.OK;
			} else {
				smbStatus = SmbStatus.GONE;
			}
		} catch (SmbException smbex) {
			if (StringUtils.contains(smbex.getMessage(), "The network name cannot be found")) {
				smbStatus = SmbStatus.GONE;
			} else {
				smbStatus = SmbStatus.UNKNOWN;
			}
		} catch (Exception ex) {
			smbStatus = SmbStatus.UNKNOWN;
		}
		return smbStatus;
	}

	public List<SmbFileDTO> getChildrenIn(SmbFileDTO startingSmbFileDTO) {
		List<SmbFileDTO> smbFileDTOs = new ArrayList<>();
		if (startingSmbFileDTO.isDirectory()) {
			try {
				SmbFile smbFile = getSmbFile(startingSmbFileDTO.getUrl());
				SmbFile[] filesAndFolders = smbFile.listFiles(new CustomSmbFileFilter());
				if (filesAndFolders != null) {
					for (SmbFile fileOrFolder : filesAndFolders) {
						SmbFileDTO smbFileDTO = getSmbFileDTO(fileOrFolder, false);
						smbFileDTOs.add(smbFileDTO);
					}
				}
			} catch (MalformedURLException e) {
				logger.error("SmbFile creation failed for url (used in children) : " + startingSmbFileDTO.getUrl(), e.getMessage(),
						new LinkedHashMap<String, String>());
			} catch (SmbException e) {
				logger.error("Unable to list files in : " + startingSmbFileDTO.getUrl(), "", new LinkedHashMap<String, String>());
			} catch (Exception e) {
				logger.error("Unexpected exception while listing files in : " + startingSmbFileDTO.getUrl(), e.getMessage(),
						new LinkedHashMap<String, String>());
			}
		}

		return smbFileDTOs;
	}

	private class CustomSmbFileFilter implements SmbFileFilter {
		@Override
		public boolean accept(SmbFile file)
				throws SmbException {
			boolean result = false;
			try {
				String url = file.getCanonicalPath();
				result = smbUtils.isAccepted(url, seeds, inclusions, exclusions);
			} catch (Exception e) {
				logger.error("Filtering at SmbService level failed", "", new LinkedHashMap<String, String>());
			}

			return result;
		}
	}

	protected SmbFile getSmbFile(String url)
			throws MalformedURLException {
		return new SmbFile(url, auth);
	}
}
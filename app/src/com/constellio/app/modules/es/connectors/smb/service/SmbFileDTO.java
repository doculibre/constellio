package com.constellio.app.modules.es.connectors.smb.service;

import org.joda.time.LocalDateTime;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SmbFileDTO {
	public static enum SmbFileDTOStatus {
		FULL_DTO, PARTIAL_DTO, UNKNOWN_DTO, DELETE_DTO, FAILED_DTO;
	}

	;

	public static final String URL = "url";
	public static final String CREATE_TIME = "createTime";
	public static final String LAST_MODIFIED = "lastModified";
	public static final String IS_FILE = "isFile";
	public static final String IS_DIRECTORY = "isDirectory";
	public static final String SIZE = "size";
	public static final String PERMISSIONS_HASH = "permissionsHash";
	public static final String EXTENSION = "extension";
	public static final String NAME = "name";
	public static final String PARSED_CONTENT = "parsedContent";
	public static final String LANGUAGE = "language";
	public static final String ALLOW_TOKENS = "allowTokens";
	public static final String DENY_TOKENS = "denyTokens";
	public static final String ALLOW_SHARE_TOKENS = "allowShareTokens";
	public static final String DENY_SHARE_TOKENS = "denyShareTokens";

	private String url = "";
	private long createTime = -10;
	private long lastModified = -10;
	private LocalDateTime lastFetched = new LocalDateTime();
	private long length = -10;
	private String permissionsHash = null;
	private String name = "";
	private boolean isFile = false;
	private boolean isDirectory = false;
	private SmbFileDTOStatus status = SmbFileDTOStatus.UNKNOWN_DTO;
	private String parsedContent = "";
	private String language = "";
	private String extension = "";
	private Map<String, String> missingMetadatasAndErrorMessages = new LinkedHashMap<>();
	private List<String> allowTokens = new ArrayList<>();
	private List<String> denyTokens = new ArrayList<>();
	private List<String> allowShareTokens = new ArrayList<>();
	private List<String> denyShareTokens = new ArrayList<>();

	private String errorMessage = "";

	public SmbFileDTO() {
		missingMetadatasAndErrorMessages.put(URL, "");
		missingMetadatasAndErrorMessages.put(CREATE_TIME, "");
		missingMetadatasAndErrorMessages.put(LAST_MODIFIED, "");
		missingMetadatasAndErrorMessages.put(IS_FILE, "");
		missingMetadatasAndErrorMessages.put(IS_DIRECTORY, "");
		missingMetadatasAndErrorMessages.put(SIZE, "");
		missingMetadatasAndErrorMessages.put(EXTENSION, "");
		missingMetadatasAndErrorMessages.put(NAME, "");
		missingMetadatasAndErrorMessages.put(LANGUAGE, "");
		missingMetadatasAndErrorMessages.put(PARSED_CONTENT, "");
		missingMetadatasAndErrorMessages.put(PERMISSIONS_HASH, "");
	}

	public String getUrl() {
		return url;
	}

	public SmbFileDTO setUrl(String url) {
		this.url = url;
		return this;
	}

	public long getCreateTime() {
		return createTime;
	}

	public SmbFileDTO setCreateTime(long createTime) {
		this.createTime = createTime;
		return this;
	}

	public long getLastModified() {
		return lastModified;
	}

	public SmbFileDTO setLastModified(long lastModified) {
		this.lastModified = lastModified;
		return this;
	}

	public LocalDateTime getLastFetchAttempt() {
		return lastFetched;
	}

	public SmbFileDTO setLastFetchAttempt(LocalDateTime lastFetched) {
		this.lastFetched = lastFetched;
		return this;
	}

	public long getLength() {
		return length;
	}

	public SmbFileDTO setLength(long length) {
		this.length = length;
		return this;
	}

	public String getPermissionsHash() {
		return permissionsHash;
	}

	public SmbFileDTO setPermissionsHash(String permissionsHash) {
		this.permissionsHash = permissionsHash;
		return this;
	}

	public String getName() {
		return name;
	}

	public SmbFileDTO setName(String filename) {
		this.name = filename;
		return this;
	}

	public boolean isFile() {
		return isFile;
	}

	public SmbFileDTO setIsFile(boolean isFile) {
		this.isFile = isFile;
		return this;
	}

	public boolean isDirectory() {
		return isDirectory;
	}

	public SmbFileDTO setIsDirectory(boolean isDirectory) {
		this.isDirectory = isDirectory;
		return this;
	}

	public SmbFileDTOStatus getStatus() {
		return status;
	}

	public SmbFileDTO setStatus(SmbFileDTOStatus status) {
		this.status = status;
		return this;
	}

	public String getParsedContent() {
		return parsedContent;
	}

	public SmbFileDTO setParsedContent(String parsedContent) {
		this.parsedContent = parsedContent;
		return this;
	}

	public String getLanguage() {
		return language;
	}

	public SmbFileDTO setLanguage(String language) {
		this.language = language;
		return this;
	}

	public String getExtension() {
		return extension;
	}

	public SmbFileDTO setExtension(String extension) {
		this.extension = extension;
		return this;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public SmbFileDTO setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
		return this;
	}

	@Deprecated
	public Map<String, String> getMissingMetadatasAndErrorMessages() {
		return missingMetadatasAndErrorMessages;
	}

	public List<String> getAllowTokens() {
		return allowTokens;
	}

	public SmbFileDTO setAllowTokens(List<String> allowTokens) {
		this.allowTokens = allowTokens;
		return this;
	}

	public List<String> getDenyTokens() {
		return denyTokens;
	}

	public SmbFileDTO setDenyTokens(List<String> denyTokens) {
		this.denyTokens = denyTokens;
		return this;
	}

	public List<String> getAllowShareTokens() {
		return allowShareTokens;
	}

	public SmbFileDTO setAllowShareTokens(List<String> allowShareTokens) {
		this.allowShareTokens = allowShareTokens;
		return this;
	}

	public List<String> getDenyShareTokens() {
		return denyShareTokens;
	}

	public SmbFileDTO setDenyShareTokens(List<String> denyShareTokens) {
		this.denyShareTokens = denyShareTokens;
		return this;
	}

}
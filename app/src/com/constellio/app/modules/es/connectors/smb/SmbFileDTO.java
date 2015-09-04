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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.constellio.app.modules.es.connectors.smb.SmbService.SmbStatus;

public class SmbFileDTO {
	public static final String URL = "url";
	public static final String LAST_MODIFIED = "lastModified";
	public static final String IS_FILE = "isFile";
	public static final String IS_DIRECTORY = "isDirectory";
	public static final String SIZE = "size";
	public static final String PERMISSIONS_HASH = "permissionsHash";
	public static final String EXTENSION = "extension";
	public static final String NAME = "name";
	public static final String PARSED_CONTENT = "parsedContent";
	public static final String LANGUAGE = "language";

	private String url; // File and Directory
	private long lastModified; // File and possibly Directory
	private long lastFetched; // File and possibly Directory
	private long length; // File
	private String permissionsHash; // File and most likely Directory
	private String name; // File and Directory
	private boolean isFile = false;
	private boolean isDirectory = false;
	private SmbStatus status = SmbStatus.UNKNOWN;
	private String parsedContent = "";
	private String language = "";
	private String extension = "";
	private List<String> missingMetadatas = new ArrayList<>(Arrays.asList(URL, LAST_MODIFIED, IS_FILE, IS_DIRECTORY, SIZE, PERMISSIONS_HASH, EXTENSION, NAME,
			LANGUAGE, PARSED_CONTENT));
	private String errorMessage = "";

	public String getUrl() {
		return url;
	}

	public SmbFileDTO setUrl(String url) {
		this.url = url;
		return this;
	}

	public long getLastModified() {
		return lastModified;
	}

	public SmbFileDTO setLastModified(long lastModified) {
		this.lastModified = lastModified;
		return this;
	}

	public long getLastFetchAttempt() {
		return lastFetched;
	}

	public SmbFileDTO setLastFetchAttempt(long lastFetched) {
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

	public SmbStatus getStatus() {
		return status;
	}

	public SmbFileDTO setStatus(SmbStatus status) {
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

	public List<String> getMissingMetadatas() {
		return missingMetadatas;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public SmbFileDTO setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
		return this;
	}

}
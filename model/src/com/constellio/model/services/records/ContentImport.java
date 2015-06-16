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
package com.constellio.model.services.records;

import java.util.ArrayList;
import java.util.List;

public class ContentImport {

	private List<ContentImportVersion> versions = new ArrayList<>();

	public ContentImport(List<ContentImportVersion> versions) {
		this.versions = versions;
	}

	public ContentImport(String url, String fileName, boolean major) {
		versions.add(new ContentImportVersion(url, fileName, major));
	}

	public String getUrl() {
		return versions.get(0).getUrl();
	}

	public String getFileName() {
		return versions.get(0).getFileName();
	}

	public boolean isMajor() {
		return versions.get(0).isMajor();
	}

	public List<ContentImportVersion> getVersions() {
		return versions;
	}
}

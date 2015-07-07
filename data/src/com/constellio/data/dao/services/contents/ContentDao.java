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
package com.constellio.data.dao.services.contents;

import java.io.InputStream;
import java.util.List;

import com.constellio.data.dao.services.contents.ContentDaoException.ContentDaoException_NoSuchContent;

public interface ContentDao {

	void add(String newContentId, InputStream newInputStream);

	void delete(List<String> contentIds);

	InputStream getContentInputStream(String contentId, String streamName)
			throws ContentDaoException_NoSuchContent;

	List<String> getFolderContents(String folderId);

	boolean isFolderExisting(String folderId);

	long getContentLength(String vaultContentId);

	void moveFolder(String folderId, String newFolderId);

	void deleteFolder(String folderId);
}
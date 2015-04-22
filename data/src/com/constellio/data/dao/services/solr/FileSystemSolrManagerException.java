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
package com.constellio.data.dao.services.solr;

import java.io.File;

@SuppressWarnings("serial")
public final class FileSystemSolrManagerException extends RuntimeException {

	private FileSystemSolrManagerException(String message) {
		super(message);
	}

	public static RuntimeException noSuchFolder(File defaultCoreFolder) {
		String message = "Folder '" + defaultCoreFolder.getAbsolutePath() + "' doesn't exist.";
		return new FileSystemSolrManagerException(message);
	}

	public static RuntimeException noSuchSolrConfig(File defaultCoreFolder) {
		String message = "No solrconfig.xml in folder '" + defaultCoreFolder.getAbsolutePath() + "'";
		return new FileSystemSolrManagerException(message);
	}

	public static RuntimeException noSuchSchema(File defaultCoreFolder) {
		String message = "No schema.xml in folder '" + defaultCoreFolder.getAbsolutePath() + "'";
		return new FileSystemSolrManagerException(message);
	}

	public static RuntimeException cannotCopyCoreInTempWorkFolder(File tempWorkFolder) {
		String message = "No schema.xml in folder '" + tempWorkFolder.getAbsolutePath() + "'";
		return new FileSystemSolrManagerException(message);
	}

}

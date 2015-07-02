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
package com.constellio.app.modules.rm.ui.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import com.constellio.data.io.streams.factories.StreamsServices;
import com.constellio.model.conf.FoldersLocator;

public class NewFileUtils {

	private static final File getNewFileFolder() {
		FoldersLocator foldersLocator = new FoldersLocator();
		File resourcesFolder = foldersLocator.getModuleResourcesFolder("rm");
		return new File(resourcesFolder, "newFile");
	}

	public static List<String> getSupportedExtensions() {
		return Arrays.asList("docx", "doc", "xlsx", "xls", "pptx", "ppt", "odt", "ods", "odp");
	}

	public static InputStream newFile(String extension) {
		File newFileFolder = getNewFileFolder();
		File newEmptyFile = new File(newFileFolder, "NewFile." + extension);
		StreamsServices streamsServices = new StreamsServices(null);
		try {
			return streamsServices.newFileInputStream(newEmptyFile, NewFileUtils.class + ".newFile." + extension);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

}

package com.constellio.app.modules.rm.ui.util;

import com.constellio.data.io.streams.factories.StreamsServices;
import com.constellio.data.conf.FoldersLocator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

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

package com.constellio.app.services.systemProperties;

import java.io.File;

import com.constellio.data.io.services.facades.IOServices;
import com.constellio.model.conf.FoldersLocator;

public class SystemPropertiesServices {
	private static final String TEST_FOLDER_NAME = SystemPropertiesServices.class.getName() + "-testFolderName";
	final FoldersLocator foldersLocator;
	final IOServices ioServices;

	public SystemPropertiesServices(FoldersLocator foldersLocator, IOServices ioServices) {
		this.foldersLocator = foldersLocator;
		this.ioServices = ioServices;
	}

	public boolean isFreeSpaceInSettingsFolderGreaterOrEqualTo(long spaceInMo) {
		return isFreeSpaceLowerThan(this.foldersLocator.getDefaultSettingsFolder(), spaceInMo);
	}

	public boolean isFreeSpaceInTempFolderLowerThan(double spaceInGig) {
		File folder = ioServices.newTemporaryFolder(TEST_FOLDER_NAME);
		boolean returnBoolean = isFreeSpaceLowerThan(folder, spaceInGig);
		ioServices.deleteQuietly(folder);
		return returnBoolean;
	}

	public boolean isFreeSpaceLowerThan(File existingFile, double spaceInGig){
		long availableSpaceInGig = getAvailableSpaceInGig(existingFile);
		return availableSpaceInGig < spaceInGig;
	}

	public long getAvailableSpaceInGig(File existingFile) {
		if(!existingFile.exists()){
			throw new RuntimeException("File does not exist " + existingFile.getPath());
		}
		long availableSpaceInBytes = existingFile.getUsableSpace();
		return availableSpaceInBytes/1073741824;//1024*1024*1024
	}

	public boolean isAvailableMemoryLowerThan(long requiredMemoryInMo) {
		long freeMemoryInMo = getFreeMemoryInMo();
		return freeMemoryInMo < requiredMemoryInMo;
	}

	public long getFreeMemoryInMo() {
		long freeMemoryInBytes = Runtime.getRuntime().freeMemory();
		return freeMemoryInBytes/1048576;//1024*1024
	}
}

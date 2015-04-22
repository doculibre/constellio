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
package com.constellio.app.start;

import java.io.File;
import java.io.IOException;

import com.constellio.data.io.services.facades.FileService;
import com.constellio.data.io.services.zip.ZipServiceException;
import com.constellio.model.conf.FoldersLocator;
import com.constellio.model.services.appManagement.InstallationService;

public final class MainConstellio {

	private MainConstellio() {

	}

	public static void main(String[] args)
			throws IOException, InterruptedException, ZipServiceException {
		File constellioInstallationDir = new FoldersLocator().getWrapperInstallationFolder();

		FileService fileService = new FileService(null);

		try {
			fileService.ensureWritePermissions(constellioInstallationDir);
		} catch (IOException e) {
			throw new MainConstellioRuntimeException("No write permissions in installation dir '" + constellioInstallationDir
					+ "'", e);
		}

		if (isInInstallationMode()) {
			installApplication(constellioInstallationDir);
		} else {
			runApplication();
		}
	}

	private static boolean isInInstallationMode() {
		File configFile = new FoldersLocator().getWrapperConf();
		return configFile.exists() && configFile.length() == 0;
	}

	private static void installApplication(File constellioInstallationDir)
			throws IOException, InterruptedException, ZipServiceException {

		InstallationService installationService = new InstallationService(constellioInstallationDir);
		installationService.launchInstallation();
	}

	private static void runApplication()
			throws IOException {
		ApplicationStarter.startApplication(true, new FoldersLocator().getConstellioWebappFolder(), 8080);
	}

}
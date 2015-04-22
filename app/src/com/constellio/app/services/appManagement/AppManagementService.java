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
package com.constellio.app.services.appManagement;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.input.CountingInputStream;
import org.apache.tika.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.entities.modules.ProgressInfo;
import com.constellio.app.services.appManagement.AppManagementServiceRuntimeException.WarFileNotFound;
import com.constellio.app.services.appManagement.AppManagementServiceRuntimeException.WarFileVersionMustBeHigher;
import com.constellio.app.services.migrations.VersionsComparator;
import com.constellio.data.io.IOServicesFactory;
import com.constellio.data.io.services.facades.FileService;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.io.services.zip.ZipService;
import com.constellio.data.io.services.zip.ZipServiceException;
import com.constellio.data.io.streamFactories.StreamFactory;
import com.constellio.model.conf.FoldersLocator;
import com.constellio.model.conf.FoldersLocatorRuntimeException;

public class AppManagementService {

	private static final Logger LOGGER = LoggerFactory.getLogger(AppManagementService.class);

	static final String TEMP_DEPLOY_FOLDER = "AppManagementService-TempDeployFolder";
	static final String WRITE_WAR_FILE_STREAM = "AppManagementService-WriteWarFile";
	public static final String UPDATE_COMMAND = "UPDATE";
	public static final String RESTART_COMMAND = "RESTART";
	public static final String URL_CHANGELOG = "http://update.constellio.com/changelog";
	public static final String URL_WAR = "http://update.constellio.com/constellio.war";

	private final FileService fileService;
	private final ZipService zipService;
	private final IOServices ioServices;
	private final FoldersLocator foldersLocator;

	public AppManagementService(IOServicesFactory ioServicesFactory, FoldersLocator foldersLocator) {
		this.fileService = ioServicesFactory.newFileService();
		this.zipService = ioServicesFactory.newZipService();
		this.foldersLocator = foldersLocator;
		this.ioServices = ioServicesFactory.newIOServices();
	}

	public void restart()
			throws AppManagementServiceException {
		File commandFile = foldersLocator.getWrapperCommandFile();
		LOGGER.info("Sending command '" + RESTART_COMMAND + "' to wrapper command file '" + commandFile.getAbsolutePath() + "'");
		try {
			writeCommand(commandFile, RESTART_COMMAND);
		} catch (IOException e) {
			throw new AppManagementServiceException.CannotWriteInCommandFile(commandFile, e);
		}
	}

	public void update(ProgressInfo progressInfo) 
			throws AppManagementServiceException {

		File warFile = foldersLocator.getUploadConstellioWarFile();
		File tempFolder = fileService.newTemporaryFolder(TEMP_DEPLOY_FOLDER);

		if (!warFile.exists()) {
			throw new WarFileNotFound();
		}
		
		String task = "Updating web application using war '" + warFile.getAbsolutePath() + "' with size " + warFile.length();
		progressInfo.reset();
		progressInfo.setEnd(1);
		progressInfo.setTask(task);
		LOGGER.info(task);

		try {
			String currentStep = "Unzipping war in temp folder '" + tempFolder + "'";
			progressInfo.setProgressMessage(currentStep);
			LOGGER.info(currentStep);
			try {
				zipService.unzip(warFile, tempFolder);
			} catch (ZipServiceException e) {
				throw new RuntimeException(e);
			}
			String warVersion = findWarVersion(tempFolder);
			String currentWarVersion = getWarVersion();
			
			currentStep = "Based on jar file, the version of the new war is '" + warVersion + "', current version is '" + currentWarVersion + "'";
			progressInfo.setProgressMessage(currentStep);
			LOGGER.info(currentStep);
			if (VersionsComparator.isFirstVersionBeforeOrEqualToSecond(warVersion, currentWarVersion)) {
				throw new WarFileVersionMustBeHigher();
			}

			File currentAppFolder = foldersLocator.getConstellioWebappFolder().getAbsoluteFile();
			File deployFolder = new File(currentAppFolder.getParentFile(), "webapp-" + warVersion);

			currentStep = "Moving new webapp version in '" + deployFolder + "'";
			progressInfo.setProgressMessage(currentStep);
			LOGGER.info(currentStep);
			tempFolder.renameTo(deployFolder);

			currentStep = "Deleting war file";
			progressInfo.setProgressMessage(currentStep);
			LOGGER.info(currentStep);
			warFile.delete();

			currentStep = "Updating wrapper conf to boot on new version";
			progressInfo.setProgressMessage(currentStep);
			LOGGER.info(currentStep);
			updateWrapperConf(warVersion);
		} finally {
			fileService.deleteQuietly(tempFolder);
		}
		progressInfo.setCurrentState(1);
	}

	private void updateWrapperConf(String warVersion) {
		File wrapperConf = foldersLocator.getWrapperConf();
		List<String> lines = fileService.readFileToLinesWithoutExpectableIOException(wrapperConf);
		for (int i = 0; i < lines.size(); i++) {
			String line = lines.get(i);
			if (line.startsWith("wrapper.java.classpath.2=")) {
				lines.set(i, "wrapper.java.classpath.2=./webapp-" + warVersion + "/WEB-INF/lib/*.jar");
			}
			if (line.startsWith("wrapper.java.classpath.3=")) {
				lines.set(i, "wrapper.java.classpath.3=./webapp-" + warVersion + "/WEB-INF/classes");
			}
			if (line.startsWith("wrapper.commandfile=")) {
				lines.set(i, "wrapper.commandfile=./webapp-" + warVersion + "/WEB-INF/command/cmd");
			}
		}
		fileService.writeLinesToFile(wrapperConf, lines);
	}

	private String findWarVersion(File tempFolder) {
		File webInf = new File(tempFolder, "WEB-INF");
		File libs = new File(webInf, "lib");
		if (libs.listFiles() != null) {
			for (File lib : libs.listFiles()) {
				if (lib.getName().startsWith("core-model-")) {
					return lib.getName().replace("core-model-", "").replace(".jar", "");
				}
			}
		}
		throw new RuntimeException("Cannot recover war version in " + libs.getAbsolutePath());
	}

	private void writeCommand(File file, String command)
			throws IOException {
		fileService.replaceFileContent(file, command);
	}

	public boolean isWarFileUploaded() {
		File warFile = foldersLocator.getUploadConstellioWarFile();
		return warFile.exists();
	}

	public StreamFactory<OutputStream> getWarFileDestination() {
		File warFile = foldersLocator.getUploadConstellioWarFile();
		return ioServices.newOutputStreamFactory(warFile, WRITE_WAR_FILE_STREAM);
	}

	public String getWarVersion() {
		try {
			File webappLibs = foldersLocator.getLibFolder();
			if (webappLibs.exists()) {
				for (File lib : webappLibs.listFiles()) {
					if (lib.getName().startsWith("core-model-")) {
						return lib.getName().replace("core-model-", "").replace(".jar", "");
					}
				}
			}
		} catch (FoldersLocatorRuntimeException.NotAvailableInGitMode e) {
		}

		return "5.0.0";
	}

	public String getChangelogFromServer()
			throws AppManagementServiceRuntimeException.CannotConnectToServer {
		String changelog = "";
		try {
			InputStream stream = getStreamForURL(URL_CHANGELOG);

			if (stream == null) {
				throw new AppManagementServiceRuntimeException.CannotConnectToServer(URL_CHANGELOG);
			}

			BufferedReader in = new BufferedReader(new InputStreamReader(stream));
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				changelog += inputLine;
			}
		} catch (IOException io) {
			throw new AppManagementServiceRuntimeException.CannotConnectToServer(URL_CHANGELOG, io);
		}

		return changelog;
	}

	InputStream getStreamForURL(String url) {
		try {
			URL changelogURL = new URL(url);
			return changelogURL.openConnection().getInputStream();
		} catch (IOException ioe) {
			return null;
		}
	}

	public void getWarFromServer(ProgressInfo progressInfo)
			throws AppManagementServiceRuntimeException.CannotConnectToServer {
		try {
			progressInfo.reset();
			progressInfo.setTask("Getting WAR from server");
			progressInfo.setEnd(1);

			progressInfo.setProgressMessage("Downloading WAR");
			InputStream input = getStreamForURL(URL_WAR);
			if (input == null) {
				throw new AppManagementServiceRuntimeException.CannotConnectToServer(URL_CHANGELOG);
			}
			CountingInputStream countingInputStream = new CountingInputStream(input);

			progressInfo.setProgressMessage("Creating WAR file");
			OutputStream warFileOutput = getWarFileDestination().create("war upload");
			
			try {
				progressInfo.setProgressMessage("Copying downloaded WAR");
				
				byte[] buffer = new byte[8 * 1024];
				int bytesRead;
				while ((bytesRead = countingInputStream.read(buffer)) != -1) {
					warFileOutput.write(buffer, 0, bytesRead);
					long totalBytesRead = countingInputStream.getByteCount();
					String progressMessage = FileUtils.byteCountToDisplaySize(totalBytesRead);
					progressInfo.setProgressMessage(progressMessage);
				}
			} finally {
				IOUtils.closeQuietly(countingInputStream);
				IOUtils.closeQuietly(warFileOutput);
			}
			progressInfo.setCurrentState(1);

		} catch (IOException ioe) {
			throw new AppManagementServiceRuntimeException.CannotConnectToServer(URL_CHANGELOG, ioe);
		}
	}

	public String getWebappFolderName() {
		return foldersLocator.getConstellioWebappFolder().getName();
	}

	boolean isValidDeployFolder(File deployFolder) {
		boolean isValid = false;
		try {
			File webInf = new File(deployFolder, "WEB-INF");
			File lib = new File(webInf, "lib");
			for (File file : lib.listFiles()) {
				if (file.getName().startsWith("core-")) {
					isValid = true;
					break;
				}
			}
		} catch (Exception e) {
			return isValid;
		}
		return isValid;
	}
}

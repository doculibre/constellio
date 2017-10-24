package com.constellio.app.services.appManagement;

import static com.constellio.app.services.extensions.plugins.pluginInfo.ConstellioPluginStatus.ENABLED;
import static com.constellio.app.services.extensions.plugins.pluginInfo.ConstellioPluginStatus.INVALID;
import static com.constellio.app.services.extensions.plugins.pluginInfo.ConstellioPluginStatus.READY_TO_INSTALL;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.input.CountingInputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.io.IOUtils;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.entities.modules.ProgressInfo;
import com.constellio.app.services.appManagement.AppManagementServiceException.CannotSaveOldPlugins;
import com.constellio.app.services.appManagement.AppManagementServiceRuntimeException.AppManagementServiceRuntimeException_SameVersionsInDifferentFolders;
import com.constellio.app.services.appManagement.AppManagementServiceRuntimeException.WarFileNotFound;
import com.constellio.app.services.appManagement.AppManagementServiceRuntimeException.WarFileVersionMustBeHigher;
import com.constellio.app.services.extensions.plugins.ConstellioPluginManager;
import com.constellio.app.services.extensions.plugins.InvalidPluginJarException;
import com.constellio.app.services.extensions.plugins.JSPFConstellioPluginManager;
import com.constellio.app.services.extensions.plugins.JSPFPluginServices;
import com.constellio.app.services.extensions.plugins.PluginActivationFailureCause;
import com.constellio.app.services.extensions.plugins.PluginServices;
import com.constellio.app.services.extensions.plugins.pluginInfo.ConstellioPluginInfo;
import com.constellio.app.services.extensions.plugins.utils.PluginManagementUtils;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.services.migrations.VersionValidator;
import com.constellio.app.services.migrations.VersionsComparator;
import com.constellio.app.services.recovery.ConstellioVersionInfo;
import com.constellio.app.services.recovery.UpgradeAppRecoveryService;
import com.constellio.app.services.recovery.UpgradeAppRecoveryServiceImpl;
import com.constellio.app.services.systemSetup.SystemGlobalConfigsManager;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.data.io.services.facades.FileService;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.io.services.zip.ZipService;
import com.constellio.data.io.services.zip.ZipServiceException;
import com.constellio.data.io.streamFactories.StreamFactory;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.conf.FoldersLocator;

public class AppManagementService {

	private static final Logger LOGGER = LoggerFactory.getLogger(AppManagementService.class);

	static final String TEMP_DEPLOY_FOLDER = "AppManagementService-TempDeployFolder";
	static final String WRITE_WAR_FILE_STREAM = "AppManagementService-WriteWarFile";
	public static final String UPDATE_COMMAND = "UPDATE";
	public static final String RESTART_COMMAND = "RESTART";
	//public static final String URL_CHANGELOG = "http://update.constellio.com/changelog5_1";
	//public static final String URL_WAR = "http://update.constellio.com/constellio5_1.war";
	private static String SERVER_URL = "http://updatecenter.constellio.com:8080";
	private static final int MAX_VERSION_TO_KEEP = 4;

	private final PluginServices pluginServices;
	private final ConstellioPluginManager pluginManager;
	private final SystemGlobalConfigsManager systemGlobalConfigsManager;
	private final FileService fileService;
	private final ZipService zipService;
	private final IOServices ioServices;
	private final FoldersLocator foldersLocator;
	private final UpgradeAppRecoveryService upgradeAppRecoveryService;

	public static AppManagementService createFailsafeService() {
		DataLayerFactory dataLayerFactory = null;
		SystemGlobalConfigsManager systemGlobalConfigsManager = null;
		ConstellioPluginManager constellioPluginManager = null;

		try {
			dataLayerFactory = ConstellioFactories.getInstance().getDataLayerFactory();
			systemGlobalConfigsManager = ConstellioFactories.getInstance().getAppLayerFactory().getSystemGlobalConfigsManager();
			constellioPluginManager = ConstellioFactories.getInstance().getAppLayerFactory().getPluginManager();
		} catch (Exception e) {
			//Situation seems very bad, new managers will be created
		}

		if (dataLayerFactory == null) {
			dataLayerFactory = DataLayerFactory.getLastCreatedInstance();
		}

		if (systemGlobalConfigsManager == null) {
			systemGlobalConfigsManager = new SystemGlobalConfigsManager(dataLayerFactory.getConfigManager());
		}

		if (constellioPluginManager == null) {
			constellioPluginManager = JSPFConstellioPluginManager.constructWithRegisteredModules(dataLayerFactory);
		}

		return new AppManagementService(dataLayerFactory, systemGlobalConfigsManager, constellioPluginManager);
	}

	public AppManagementService(DataLayerFactory dataLayerFactory,
			SystemGlobalConfigsManager systemGlobalConfigsManager, ConstellioPluginManager constellioPluginManager) {

		this.foldersLocator = new FoldersLocator();
		this.ioServices = new IOServices(foldersLocator.getDefaultTempFolder());
		this.fileService = new FileService(foldersLocator.getDefaultTempFolder());
		this.zipService = new ZipService(ioServices);
		this.pluginServices = new JSPFPluginServices(ioServices);

		this.systemGlobalConfigsManager = systemGlobalConfigsManager;
		this.pluginManager = constellioPluginManager;
		this.upgradeAppRecoveryService = new UpgradeAppRecoveryServiceImpl(dataLayerFactory);
	}

	public AppManagementService(AppLayerFactory appLayerFactory, FoldersLocator foldersLocator) {
		this.systemGlobalConfigsManager = appLayerFactory.getSystemGlobalConfigsManager();
		this.pluginManager = appLayerFactory.getPluginManager();
		this.fileService = appLayerFactory.getModelLayerFactory().getIOServicesFactory().newFileService();
		this.zipService = appLayerFactory.getModelLayerFactory().getIOServicesFactory().newZipService();
		this.foldersLocator = foldersLocator;
		this.ioServices = appLayerFactory.getModelLayerFactory().getIOServicesFactory().newIOServices();
		this.upgradeAppRecoveryService = appLayerFactory.newUpgradeAppRecoveryService();
		this.pluginServices = new JSPFPluginServices(ioServices);
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

	public void installPlugin(File tempPluginFile) {
		PluginActivationFailureCause cause = pluginManager.prepareInstallablePlugin(tempPluginFile);
		if (cause == null) {
			systemGlobalConfigsManager.setRestartRequired(true);
		}
	}

	public void update(ProgressInfo progressInfo)
			throws AppManagementServiceException {
		ConstellioVersionInfo currentInstalledVersionInfo = getCurrentInstalledVersionInfo();

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
			String currentWarVersion = GetWarVersionUtils.getWarVersionUsingGradleAsFallback(null);

			currentStep = "Based on jar file, the version of the new war is '" + warVersion + "', current version is '"
					+ currentWarVersion + "'";
			progressInfo.setProgressMessage(currentStep);
			LOGGER.info(currentStep);
			if (VersionsComparator.isFirstVersionBeforeSecond(warVersion, currentWarVersion)) {
				LOGGER.warn("Trying to install lower version " + warVersion + "\n\tCurrent version is " + currentWarVersion);
				throw new WarFileVersionMustBeHigher();
			}

			currentStep = "Saving existing plugins to new version directory";
			progressInfo.setProgressMessage(currentStep);
			LOGGER.info(currentStep);

			File oldPluginsFolder = foldersLocator.getPluginsJarsFolder();
			copyCurrentPlugins(oldPluginsFolder, tempFolder);
			movePluginsToNewLib(oldPluginsFolder, tempFolder);
			updatePluginsWithThoseInWar(tempFolder);

			File currentAppFolder = foldersLocator.getConstellioWebappFolder().getAbsoluteFile();
			File deployFolder = findDeployFolder(currentAppFolder.getParentFile(), warVersion);

			currentStep = "Moving new webapp version in '" + deployFolder + "'";
			progressInfo.setProgressMessage(currentStep);
			LOGGER.info(currentStep);
			tempFolder.renameTo(deployFolder);

			currentStep = "Deleting war file";
			progressInfo.setProgressMessage(currentStep);
			LOGGER.info(currentStep);
			warFile.delete();

			currentStep = "Deleting older versions";
			progressInfo.setProgressMessage(currentStep);
			LOGGER.info(currentStep);
			keepOnlyLastFiveVersionsAndAtLeastOneVersionModifiedBeforeLastWeek(currentAppFolder.getParentFile(),
					deployFolder);

			currentStep = "Updating wrapper conf to boot on new version";
			progressInfo.setProgressMessage(currentStep);
			LOGGER.info(currentStep);
			newWrapperConfServices().updateWrapperConf(deployFolder);
			upgradeAppRecoveryService.afterWarUpload(currentInstalledVersionInfo,
					new ConstellioVersionInfo(warVersion, deployFolder.getAbsolutePath()));
		} catch (AppManagementServiceException e) {
			//FIXME delete deployFolder if created and revert to previous wrapper conf then throw exception
			throw e;
		} finally {
			fileService.deleteQuietly(tempFolder);
		}
		progressInfo.setCurrentState(1);

	}

	private WrapperConfService newWrapperConfServices() {
		return new WrapperConfService(fileService, foldersLocator);
	}

	private void updatePluginsWithThoseInWar(File nextWebapp) {
		updatePlugins(nextWebapp);
		installPlugins(nextWebapp);
	}

	private void installPlugins(File nextWebapp) {
		File pluginsFolder = new File(nextWebapp, "plugins-to-install");
		if (pluginsFolder.exists() && pluginsFolder.listFiles() != null) {
			for (File pluginFile : pluginsFolder.listFiles()) {
				if (pluginFile.getName().toLowerCase().endsWith(".jar")) {
					LOGGER.info(pluginsFolder.getName() + "/" + pluginFile.getName() + ".jar : installed");
					pluginManager.prepareInstallablePluginInNextWebapp(pluginFile, nextWebapp);
				}
			}
		}
	}

	private void updatePlugins(File nextWebapp) {
		File pluginsFolder = new File(nextWebapp, "plugins-to-update");
		if (pluginsFolder.exists() && pluginsFolder.listFiles() != null) {
			Set<String> alreadyInstalledPlugins = new HashSet<>();

			for (ConstellioPluginInfo info : pluginManager.getPlugins(ENABLED, READY_TO_INSTALL, INVALID)) {
				alreadyInstalledPlugins.add(info.getCode());
			}

			for (File pluginFile : pluginsFolder.listFiles()) {
				if (pluginFile.getName().toLowerCase().endsWith(".jar")) {
					try {
						ConstellioPluginInfo info = pluginServices.extractPluginInfo(pluginFile);

						if (alreadyInstalledPlugins.contains(info.getCode())) {
							LOGGER.info(pluginsFolder.getName() + "/" + pluginFile.getName() + ".jar : installed");
							pluginManager.prepareInstallablePluginInNextWebapp(pluginFile, nextWebapp);
						} else {
							LOGGER.info(pluginsFolder.getName() + "/" + pluginFile.getName() + ".jar : deleted");
							pluginFile.delete();
						}

					} catch (InvalidPluginJarException e) {
						throw new RuntimeException(e);
					}
				}
			}
		}
	}

	private void movePluginsToNewLib(File oldPluginsFolder, File newWebAppFolder)
			throws CannotSaveOldPlugins {
		File newLibsFolder = foldersLocator.getLibFolder(newWebAppFolder);
		LOGGER.info("plugins : copy to lib " + newLibsFolder.getPath());
		File pluginsToMoveFile = foldersLocator.getPluginsToMoveOnStartupFile(newWebAppFolder);
		PluginManagementUtils utils = new PluginManagementUtils(oldPluginsFolder, newLibsFolder, pluginsToMoveFile);

		try {
			utils.movePlugins(pluginManager.getPluginsOfEveryStatus());
		} catch (IOException e) {
			throw new CannotSaveOldPlugins(e);
		}
	}

	private void copyCurrentPlugins(File oldPluginsFolder, File newWebAppFolder)
			throws CannotSaveOldPlugins {
		if (oldPluginsFolder.exists()) {
			try {
				LOGGER.info("plugins : copy to " + newWebAppFolder);
				LOGGER.info("plugins : copy from ",
						oldPluginsFolder.getPath() + "to " + foldersLocator.getPluginsJarsFolder(newWebAppFolder).getPath());
				FileUtils.copyDirectory(oldPluginsFolder, foldersLocator.getPluginsJarsFolder(newWebAppFolder));
			} catch (IOException e) {
				throw new CannotSaveOldPlugins(e);
			}
		}
	}

	private void keepOnlyLastFiveVersionsAndAtLeastOneVersionModifiedBeforeLastWeek(File webAppsFolder, File deployFolder) {
		Map<String, File> existingWebAppsMappedByVersion = getExistingVersionsFoldersMappedByVersion(webAppsFolder, deployFolder);

		if (existingWebAppsMappedByVersion.size() > MAX_VERSION_TO_KEEP) {
			Set<String> orderedVersions = new TreeSet<>(new VersionsComparator());
			orderedVersions.addAll(existingWebAppsMappedByVersion.keySet());
			int versionsToRemoveCount = existingWebAppsMappedByVersion.size() - MAX_VERSION_TO_KEEP;
			Iterator<String> versionsIterator = orderedVersions.iterator();
			int handledVersions = 0;
			boolean atLeastOneVersionBeforeLastWeek = false;
			while (versionsIterator.hasNext()) {
				String version = versionsIterator.next();
				handledVersions++;
				if (handledVersions > versionsToRemoveCount) {
					File webAppToRemove = existingWebAppsMappedByVersion.get(version);
					existingWebAppsMappedByVersion.remove(version);
					if (isModifiedBeforeLastWeek(webAppToRemove)) {
						atLeastOneVersionBeforeLastWeek = true;
					}
				}
			}
			if (!atLeastOneVersionBeforeLastWeek) {
				removeAllFilesAndKeepTheNewestOneBeforeLastWeek(existingWebAppsMappedByVersion.values());
			} else {
				removeAllFiles(existingWebAppsMappedByVersion.values());
			}
		}
	}

	private void removeAllFilesAndKeepTheNewestOneBeforeLastWeek(Collection<File> files) {
		List<File> filesList = new ArrayList<>(files);
		Collections.sort(filesList, new Comparator<File>() {
			@Override
			public int compare(File o1, File o2) {
				LocalDate modificationDate1 = new LocalDate(o1.lastModified());
				LocalDate modificationDate2 = new LocalDate(o1.lastModified());
				return modificationDate1.compareTo(modificationDate2);
			}
		});
		for (int i = 0; i < filesList.size() - 1; i++) {
			File file = filesList.get(i);
			FileUtils.deleteQuietly(file);
		}
		File lastFile = filesList.get(filesList.size() - 1);
		if (!isModifiedBeforeLastWeek(lastFile)) {
			FileUtils.deleteQuietly(lastFile);
		}
	}

	private void removeAllFiles(Collection<File> files) {
		for (File file : files) {
			FileUtils.deleteQuietly(file);
		}
	}

	boolean isModifiedBeforeLastWeek(File webAppsFolder) {
		return new LocalDate(webAppsFolder.lastModified()).isBefore(TimeProvider.getLocalDate().minusDays(7));
	}

	private Map<String, File> getExistingVersionsFoldersMappedByVersion(File webAppsFolder, File deployFolder) {
		Map<String, File> existingWebAppsMappedByVersion = new HashMap<>();
		for (File webApp : webAppsFolder.listFiles(new WebAppFileNameFilter())) {
			String version = GetWarVersionUtils.getWarVersionFromFileName(webApp);
			if (VersionValidator.isValidVersion(version) && !webApp.getName().equals(deployFolder.getName())) {
				File associatedWebAppFolder = existingWebAppsMappedByVersion.get(version);
				if (associatedWebAppFolder != null) {
					throw new AppManagementServiceRuntimeException_SameVersionsInDifferentFolders(version, webApp.getName(),
							associatedWebAppFolder.getName());
				} else {
					existingWebAppsMappedByVersion.put(version, webApp);
				}
			}
		}
		return existingWebAppsMappedByVersion;
	}

	File findDeployFolder(File parent, String version) {
		File deployFolder = null;
		String mostRecentVersion = "";
		for (File currentWebApp : parent.listFiles(new WebAppWithValidSubversionFilenameFilter(version))) {
			String currentVersion = StringUtils.substringAfter(currentWebApp.getName(), "webapp-");
			if (mostRecentVersion.isEmpty()) {
				deployFolder = currentWebApp;
				mostRecentVersion = currentVersion;
			} else {
				if (VersionsComparator.isFirstVersionBeforeSecond(mostRecentVersion, currentVersion)) {
					deployFolder = currentWebApp;
					mostRecentVersion = currentVersion;
				}
			}
		}

		int nextSubVersion;
		if (mostRecentVersion.isEmpty()) {
			deployFolder = new File(parent, "webapp-" + version);
			nextSubVersion = 0;
		} else if (mostRecentVersion.contains("-")) {
			nextSubVersion = Integer.valueOf(mostRecentVersion.split("-")[1]);
		} else {
			nextSubVersion = 0;
		}
		nextSubVersion++;

		while (deployFolder.exists()) {
			deployFolder = new File(parent, "webapp-" + version + "-" + nextSubVersion);
			nextSubVersion++;
		}
		return deployFolder;
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
		return GetWarVersionUtils.getWarVersion(null);
	}

	public String getChangelogURLFromServer()
			throws AppManagementServiceRuntimeException.CannotConnectToServer {
		String serverUrl = SERVER_URL + "/changelog/";
		String changelogURL;
		try {
			changelogURL = sendPost(serverUrl, getInfosToSend());
		} catch (IOException ioe) {
			throw new AppManagementServiceRuntimeException.CannotConnectToServer(serverUrl);
		}

		return changelogURL;
	}

	public String getWarURLFromServer()
			throws AppManagementServiceRuntimeException.CannotConnectToServer {
		String serverUrl = SERVER_URL + "/url/";
		String warURL;
		try {
			warURL = sendPost(serverUrl, getInfosToSend());
		} catch (IOException ioe) {
			throw new AppManagementServiceRuntimeException.CannotConnectToServer(serverUrl);
		}

		return warURL;
	}

	String getInfosToSend() {
		String delimiter = "*";
		return getLicenseInfo().getSignature() + delimiter + getCurrentInstalledVersionInfo().getVersion();
	}

	String sendPost(String url, String infoSent)
			throws IOException {
		StringBuilder response = new StringBuilder();

		BufferedReader in = new BufferedReader(new InputStreamReader(getInputForPost(url, infoSent)));
		String inputLine;

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		return response.toString();
	}

	InputStream getInputForPost(String url, String infoSent)
			throws IOException {
		URL obj = new URL(url);

		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("POST");
		con.setDoOutput(true);

		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.writeBytes(infoSent);
		wr.flush();
		wr.close();

		return con.getInputStream();
	}

	public String getChangelogFromServer()
			throws AppManagementServiceRuntimeException.CannotConnectToServer {
		String URL_CHANGELOG = getChangelogURLFromServer();

		String changelog = "";
		try {
			InputStream stream = getInputForPost(URL_CHANGELOG, getLicenseInfo().getSignature());

			if (stream == null) {
				throw new AppManagementServiceRuntimeException.CannotConnectToServer(URL_CHANGELOG);
			}

			BufferedReader in = new BufferedReader(new InputStreamReader(stream));
			try {
				String inputLine;
				while ((inputLine = in.readLine()) != null) {
					changelog += inputLine;
				}

				if (this.isProxyPage(changelog)) {
					throw new AppManagementServiceRuntimeException.CannotConnectToServer(URL_CHANGELOG);
				}
			} finally {
				IOUtils.closeQuietly(in);
			}

		} catch (IOException | RuntimeException io) {
			throw new AppManagementServiceRuntimeException.CannotConnectToServer(URL_CHANGELOG, io);
		}

		return changelog;
	}

	public String getVersionFromServer()
			throws AppManagementServiceRuntimeException.CannotConnectToServer {
		String serverUrl = SERVER_URL + "/version/";

		try {
			serverUrl = sendPost(serverUrl, getInfosToSend());
		} catch (IOException ioe) {
			throw new AppManagementServiceRuntimeException.CannotConnectToServer(serverUrl);
		}

		return serverUrl;
	}

	boolean isProxyPage(String changelog) {
		return !changelog.contains("<version>");
	}

	public void getWarFromServer(ProgressInfo progressInfo)
			throws AppManagementServiceRuntimeException.CannotConnectToServer {
		String URL_WAR = getWarURLFromServer();

		System.out.println("URL FOR WAR => " + URL_WAR);

		try {
			progressInfo.reset();
			progressInfo.setTask("Getting WAR from server");
			progressInfo.setEnd(1);

			progressInfo.setProgressMessage("Downloading WAR");
			InputStream input = getInputForPost(URL_WAR, getLicenseInfo().getSignature());
			if (input == null) {
				throw new AppManagementServiceRuntimeException.CannotConnectToServer(URL_WAR);
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
			throw new AppManagementServiceRuntimeException.CannotConnectToServer(URL_WAR, ioe);
		}
	}

	public String getWebappFolderName() {
		return foldersLocator.getConstellioWebappFolder().getName();
	}

	public void markForReindexing() {
		systemGlobalConfigsManager.setMarkedForReindexing(true);
	}

	public void unmarkForReindexing() {
		systemGlobalConfigsManager.setMarkedForReindexing(false);
	}

	public boolean isLicensedForAutomaticUpdate() {
		return getLicenseInfo() != null;
	}

	public void storeLicense(File uploadLicenseFile) {
		File licenseFile = foldersLocator.getLicenseFile();
		ioServices.copyFileWithoutExpectableIOException(uploadLicenseFile, licenseFile);
	}

	public LicenseInfo getLicenseInfo() {
		LicenseInfo license = null;
		try {
			String licenseString = ioServices.readFileToString(foldersLocator.getLicenseFile());
			SAXBuilder builder = new SAXBuilder();
			Document document = builder.build(new StringReader(licenseString));

			String name = document.getRootElement().getChild("name").getContent().get(0).getValue();
			LocalDate date = new LocalDate(document.getRootElement().getChild("date").getContent().get(0).getValue());
			String signature = document.getRootElement().getChild("signature").getContent().get(0).getValue();

			license = new LicenseInfo(name, date, signature);
		} catch (IOException ioe) {
		} catch (JDOMException joe) {
		}

		return license;
	}

	private ConstellioVersionInfo getCurrentInstalledVersionInfo() {
		File versionDirectory = foldersLocator.getConstellioWebappFolder();
		String version = GetWarVersionUtils.getWarVersion(versionDirectory);
		return new ConstellioVersionInfo(version, versionDirectory.getAbsolutePath());
	}

	private class WebAppFileNameFilter implements FilenameFilter {

		@Override
		public boolean accept(File dir, String name) {
			return name.startsWith("webapp-");
		}
	}

	private class WebAppWithValidSubversionFilenameFilter implements FilenameFilter {
		final String version;

		public WebAppWithValidSubversionFilenameFilter(
				String version) {
			this.version = version;
		}

		@Override
		public boolean accept(File dir, String name) {
			if (name.equals("webapp-" + version)) {
				return true;
			}
			if (name.startsWith("webapp-" + version + "-")) {
				String currentVersion = StringUtils.substringAfter(name, "webapp-");
				if (VersionValidator.isValidVersion(currentVersion)) {
					return true;

				}
			}
			return false;
		}
	}

	public static class LicenseInfo implements Serializable {
		private final String clientName;
		private final LocalDate expirationDate;
		private final String signature;

		public LicenseInfo(String clientName, LocalDate expirationDate, String signature) {
			this.clientName = clientName;
			this.expirationDate = expirationDate;
			this.signature = signature;
		}

		public String getClientName() {
			return clientName;
		}

		public LocalDate getExpirationDate() {
			return expirationDate;
		}

		public String getSignature() {
			return signature;
		}
	}

}

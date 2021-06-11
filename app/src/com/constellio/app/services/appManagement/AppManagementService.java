package com.constellio.app.services.appManagement;

import com.constellio.app.entities.modules.ProgressInfo;
import com.constellio.app.entities.support.SupportPlan;
import com.constellio.app.services.appManagement.AppManagementServiceException.CannotSaveOldPlugins;
import com.constellio.app.services.appManagement.AppManagementServiceRuntimeException.AppManagementServiceRuntimeException_SameVersionsInDifferentFolders;
import com.constellio.app.services.appManagement.AppManagementServiceRuntimeException.CannotConnectToServer;
import com.constellio.app.services.appManagement.AppManagementServiceRuntimeException.InvalidLicenseInstalled;
import com.constellio.app.services.appManagement.AppManagementServiceRuntimeException.WarFileNotFoundException;
import com.constellio.app.services.appManagement.AppManagementServiceRuntimeException.WarFileVersionMustBeHigher;
import com.constellio.app.services.background.UpdateServerPingBackgroundAction.UpdateServerPingUpdates;
import com.constellio.app.services.extensions.plugins.ConstellioPluginManager;
import com.constellio.app.services.extensions.plugins.InvalidPluginJarException;
import com.constellio.app.services.extensions.plugins.JSPFPluginServices;
import com.constellio.app.services.extensions.plugins.PluginServices;
import com.constellio.app.services.extensions.plugins.pluginInfo.ConstellioPluginInfo;
import com.constellio.app.services.extensions.plugins.utils.PluginManagementUtils;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.services.migrations.VersionValidator;
import com.constellio.app.services.migrations.VersionsComparator;
import com.constellio.app.services.recovery.ConstellioVersionInfo;
import com.constellio.app.services.recovery.UpgradeAppRecoveryService;
import com.constellio.app.services.systemSetup.SystemGlobalConfigsManager;
import com.constellio.app.services.systemSetup.SystemLocalConfigsManager;
import com.constellio.app.servlet.ConstellioMonitoringServlet;
import com.constellio.app.utils.ScriptsUtils;
import com.constellio.data.conf.FoldersLocator;
import com.constellio.data.conf.FoldersLocatorMode;
import com.constellio.data.io.services.facades.FileService;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.io.services.zip.ZipService;
import com.constellio.data.io.services.zip.ZipServiceException;
import com.constellio.data.io.streamFactories.StreamFactory;
import com.constellio.data.utils.PropertyFileUtils;
import com.constellio.data.utils.TenantUtils;
import com.constellio.data.utils.TimeProvider;
import com.constellio.data.utils.dev.Toggle;
import com.constellio.data.utils.systemLogger.SystemLogger;
import com.constellio.model.services.encrypt.EncryptionServices;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.utils.EnumWithSmallCodeUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.UriBuilder;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static com.constellio.app.services.extensions.plugins.pluginInfo.ConstellioPluginStatus.ENABLED;
import static com.constellio.app.services.extensions.plugins.pluginInfo.ConstellioPluginStatus.INVALID;
import static com.constellio.app.services.extensions.plugins.pluginInfo.ConstellioPluginStatus.READY_TO_INSTALL;

public class AppManagementService {

	private static final Logger LOGGER = LoggerFactory.getLogger(AppManagementService.class);

	static final String TEMP_DEPLOY_FOLDER = "AppManagementService-TempDeployFolder";
	static final String WRITE_WAR_FILE_STREAM = "AppManagementService-WriteWarFile";
	public static final String UPDATE_COMMAND = "UPDATE";
	public static final String RESTART_COMMAND = "RESTART";
	public static final String DUMP_COMMAND = "DUMP";
	private static final int MAX_VERSION_TO_KEEP = 4;

	private final PluginServices pluginServices;
	private final ConstellioPluginManager pluginManager;
	private final SystemGlobalConfigsManager systemGlobalConfigsManager;
	private final SystemLocalConfigsManager systemLocalConfigsManager;
	private final FileService fileService;
	private final ZipService zipService;
	private final IOServices ioServices;
	private final FoldersLocator foldersLocator;
	protected final ConstellioEIMConfigs eimConfigs;
	private final UpgradeAppRecoveryService upgradeAppRecoveryService;
	private final AppLayerFactory appLayerFactory;

	public AppManagementService(AppLayerFactory appLayerFactory, FoldersLocator foldersLocator) {

		this.systemGlobalConfigsManager = appLayerFactory.getSystemGlobalConfigsManager();
		this.systemLocalConfigsManager = appLayerFactory.getSystemLocalConfigsManager();
		this.pluginManager = appLayerFactory.getPluginManager();
		this.fileService = appLayerFactory.getModelLayerFactory().getIOServicesFactory().newFileService();
		this.zipService = appLayerFactory.getModelLayerFactory().getIOServicesFactory().newZipService();
		this.foldersLocator = foldersLocator;
		this.ioServices = appLayerFactory.getModelLayerFactory().getIOServicesFactory().newIOServices();
		this.eimConfigs = new ConstellioEIMConfigs(appLayerFactory.getModelLayerFactory().getSystemConfigurationsManager());
		this.upgradeAppRecoveryService = appLayerFactory.newUpgradeAppRecoveryService();
		this.pluginServices = new JSPFPluginServices(ioServices);
		this.appLayerFactory = appLayerFactory;
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

		ConstellioMonitoringServlet.systemRestarting = true;
	}

	public void restartTenant() {
		ConstellioFactories.clear();
		ConstellioFactories.getInstance();

		ConstellioMonitoringServlet.tenantRestarting.set(true);
	}

	public void dump()
			throws AppManagementServiceException {
		File commandFile = foldersLocator.getWrapperCommandFile();
		LOGGER.info("Sending command '" + DUMP_COMMAND + "' to wrapper command file '" + commandFile.getAbsolutePath() + "'");
		try {
			writeCommand(commandFile, DUMP_COMMAND);
		} catch (IOException e) {
			throw new AppManagementServiceException.CannotWriteInCommandFile(commandFile, e);
		}
	}

	public void update(ProgressInfo progressInfo)
			throws AppManagementServiceException {
		update(progressInfo, true);
	}

	public void update(ProgressInfo progressInfo, boolean enableRollback)
			throws AppManagementServiceException {
		ConstellioVersionInfo currentInstalledVersionInfo = getCurrentInstalledVersionInfo();

		File warFile = foldersLocator.getUploadConstellioWarFile();
		File tempFolder = fileService.newTemporaryFolder(TEMP_DEPLOY_FOLDER);

		if (!warFile.exists()) {
			throw new WarFileNotFoundException();
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
			if (VersionsComparator.isFirstVersionBeforeSecond(warVersion, currentWarVersion)
				&& !Toggle.DANGER_DANGER_DANGER___ALLOW_UPDATE_TO_OLDER_VERSION___DANGER_DANGER_DANGER.isEnabled()) {
				LOGGER.warn("Trying to install lower version " + warVersion + "\n\tCurrent version is " + currentWarVersion);
				throw new WarFileVersionMustBeHigher();
			}

			currentStep = "Saving existing plugins to new version directory";
			progressInfo.setProgressMessage(currentStep);
			LOGGER.info(currentStep);

			File oldPluginsFolder = foldersLocator.getPluginsJarsFolder();
			copyCurrentLibsIfPatchWar(foldersLocator.getConstellioWebappFolder(), tempFolder);
			copyCurrentPlugins(oldPluginsFolder, tempFolder);
			movePluginsToNewLib(oldPluginsFolder, tempFolder);
			updatePluginsWithThoseInWar(tempFolder);

			currentStep = "Selecting SMB Library";
			selectSmbLibrary(tempFolder);
			LOGGER.info(currentStep);

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

			if (eimConfigs.isCleanDuringInstall()) {
				currentStep = "Deleting older versions";
				progressInfo.setProgressMessage(currentStep);
				LOGGER.info(currentStep);
				keepOnlyLastFiveVersionsAndAtLeastOneVersionModifiedBeforeLastWeek(currentAppFolder.getParentFile(),
						deployFolder);
			}

			currentStep = "Updating wrapper conf to boot on new version";
			progressInfo.setProgressMessage(currentStep);
			LOGGER.info(currentStep);
			updateWrapperConf(deployFolder);
			if (enableRollback) {
				upgradeAppRecoveryService.afterWarUpload(currentInstalledVersionInfo,
						new ConstellioVersionInfo(warVersion, deployFolder.getAbsolutePath()));
			}
		} catch (AppManagementServiceException e) {
			//FIXME delete deployFolder if created and revert to previous wrapper conf then throw exception
			throw e;
		} finally {
			fileService.deleteQuietly(tempFolder);
		}
		progressInfo.setCurrentState(1);

	}

	private void copyCurrentLibsIfPatchWar(File constellioWebappFolder, File tempFolder) {
		File newAppLibs = new File(new File(tempFolder, "WEB-INF"), "lib");
		File currentAppLibs = new File(new File(constellioWebappFolder, "WEB-INF"), "lib");

		if (newAppLibs.listFiles() != null && currentAppLibs.listFiles() != null && newAppLibs.listFiles().length < 10) {
			for (File currentJarFile : currentAppLibs.listFiles()) {
				if (!currentJarFile.getName().startsWith("core-")
					&& !currentJarFile.getName().startsWith("plugin")) {
					try {
						FileUtils.copyFile(currentJarFile, new File(newAppLibs, currentJarFile.getName()));
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}
			}
		}

	}

	private void updatePluginsWithThoseInWar(File nextWebapp) {

		if (TenantUtils.isSupportingTenants()) {

			String currentTenant = TenantUtils.getTenantId();
			LOGGER.warn("Was tenant '" + currentTenant + "'");
			try {

				ScriptsUtils.forEachAvailableAndFailedTenants((tenantId, applicationLayerFactory) -> {
					ConstellioPluginManager pluginManager = applicationLayerFactory.getPluginManager();
					updatePluginsOfCurrentTenant(nextWebapp, pluginManager, pluginServices, currentTenant);
					installPluginsForCurrentTenant(nextWebapp, pluginManager);
				});
			} finally {
				TenantUtils.setTenant(currentTenant);
			}
			LOGGER.warn("Now tenant '" + TenantUtils.getTenantId() + "'");
		} else {
			updatePluginsOfCurrentTenant(nextWebapp, pluginManager, pluginServices, "main");
			installPluginsForCurrentTenant(nextWebapp, pluginManager);
		}
	}

	private static void installPluginsForCurrentTenant(File nextWebapp, ConstellioPluginManager pluginManager) {
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

	private static void updatePluginsOfCurrentTenant(File nextWebapp, ConstellioPluginManager pluginManager,
													 PluginServices pluginServices, String tenantId) {
		File pluginsFolder = new File(nextWebapp, "plugins-to-update");
		if (pluginsFolder.exists() && pluginsFolder.listFiles() != null) {
			Set<String> alreadyInstalledPluginsForTenant = pluginManager.getPlugins(ENABLED, READY_TO_INSTALL, INVALID)
					.stream().map(ConstellioPluginInfo::getCode).collect(Collectors.toSet());
			List<String> alreadyInstalledPluginsForAnyTenant = pluginManager.getPluginsFromAnyTenants(ENABLED, READY_TO_INSTALL, INVALID);


			for (File pluginFile : pluginsFolder.listFiles()) {
				if (pluginFile.getName().toLowerCase().endsWith(".jar")) {
					try {
						ConstellioPluginInfo info = pluginServices.extractPluginInfo(pluginFile);


						if (alreadyInstalledPluginsForTenant.contains(info.getCode())) {
							LOGGER.info(pluginsFolder.getName() + "/" + pluginFile.getName() + ".jar : installed for tenant " + tenantId);
							pluginManager.prepareInstallablePluginInNextWebapp(pluginFile, nextWebapp);
						} else if (!alreadyInstalledPluginsForAnyTenant.contains(info.getCode())) {
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
			utils.movePlugins(pluginManager.getPluginsOfEveryStatusFromAnyTenants());
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

	private void keepOnlyLastFiveVersionsAndAtLeastOneVersionModifiedBeforeLastWeek(File webAppsFolder,
																					File deployFolder) {
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

	private void updateWrapperConf(File deployFolder) {

		LOGGER.info("New webapp path is '" + deployFolder.getAbsolutePath() + "'");
		File wrapperConf = foldersLocator.getWrapperConf();
		if (foldersLocator.getFoldersLocatorMode().equals(FoldersLocatorMode.PROJECT) && !wrapperConf.exists()) {
			return;
		}
		List<String> lines = fileService.readFileToLinesWithoutExpectableIOException(wrapperConf);
		for (int i = 0; i < lines.size(); i++) {

			String line = lines.get(i);
			if (line.startsWith("wrapper.java.classpath.2=")) {
				lines.set(i, "wrapper.java.classpath.2=" + deployFolder.getAbsolutePath() + "/WEB-INF/lib/*.jar");
			}
			if (line.startsWith("wrapper.java.classpath.3=")) {
				lines.set(i, "wrapper.java.classpath.3=" + deployFolder.getAbsolutePath() + "/WEB-INF/classes");
			}
			if (line.startsWith("wrapper.commandfile=")) {
				lines.set(i, "wrapper.commandfile=" + deployFolder.getAbsolutePath() + "/WEB-INF/command/cmd");
			}
		}
		fileService.writeLinesToFile(wrapperConf, lines);
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

	public StreamFactory<OutputStream> getLastAlertFileDestination() {
		File lastAlertFile = foldersLocator.getLastAlertFile();
		return ioServices.newOutputStreamFactory(lastAlertFile, "alert");
	}

	public String getWarVersion() {
		return GetWarVersionUtils.getWarVersion(null);
	}

	public File getLastAlertFromServer() throws CannotConnectToServer {
		String serverUrl = null;
		try {
			serverUrl = getInternalServerUrl("alert");

			InputStream lastAlertFileInput = null;
			OutputStream lastAlertFileOutput = null;
			try {
				LicenseInfo licenseInfo = getLicenseInfo();
				if (licenseInfo != null) {
					Map<String, String> params = new HashMap<>();
					params.put("client", licenseInfo.getClientName());
					params.put("signature", licenseInfo.getSignature());
					lastAlertFileInput = getInputForGet(serverUrl, params);

					if (lastAlertFileInput == null) {
						throw new AppManagementServiceRuntimeException.CannotConnectToServer(serverUrl);
					}

					lastAlertFileOutput = getLastAlertFileDestination().create("alert download");
					IOUtils.copy(lastAlertFileInput, lastAlertFileOutput);
				}
			} catch (IOException | RuntimeException e) {
				throw new AppManagementServiceRuntimeException.CannotConnectToServer(serverUrl, e);
			} finally {
				IOUtils.closeQuietly(lastAlertFileInput);
				IOUtils.closeQuietly(lastAlertFileOutput);
			}
		} catch (IllegalArgumentException iae) {
			throw new AppManagementServiceRuntimeException.CannotConnectToServer(serverUrl, iae);
		}

		return foldersLocator.getLastAlertFile();
	}

	public File getNewLicenseFromServer() throws CannotConnectToServer {
		String serverUrl = null;
		try {
			serverUrl = getInternalServerUrl("license");

			InputStream newLicenceFileInput = null;
			File newTempLicence = null;
			try {
				newTempLicence = ioServices.newTemporaryFile("temp-licence", "xml");
				LicenseInfo licenseInfo = getLicenseInfo();
				if (licenseInfo != null) {
					Map<String, String> params = new HashMap<>();
					params.put("client", licenseInfo.getClientName());
					params.put("signature", licenseInfo.getSignature());
					newLicenceFileInput = getInputForGet(serverUrl, params);

					if (newLicenceFileInput == null) {
						throw new AppManagementServiceRuntimeException.CannotConnectToServer(serverUrl);
					}

					FileUtils.copyInputStreamToFile(newLicenceFileInput, newTempLicence);
				}
			} catch (IOException | RuntimeException e) {
				throw new AppManagementServiceRuntimeException.CannotConnectToServer(serverUrl, e);
			} finally {
				IOUtils.closeQuietly(newLicenceFileInput);
			}

			return newTempLicence;

		} catch (IllegalArgumentException iae) {
			throw new AppManagementServiceRuntimeException.CannotConnectToServer(serverUrl, iae);
		}
	}

	private String getInternalServerUrl(String servletPath) {
		return UriBuilder.fromUri(eimConfigs.getInternalServerUrl()).path(servletPath).toString();
	}

	public UpdateServerPingUpdates getUpdateServerPingUpdates() throws CannotConnectToServer {
		String serverUrl = null;
		UpdateServerPingUpdates updates = null;
		try {
			serverUrl = getInternalServerUrl("updateServerPing");
			LicenseInfo licenseInfo = getLicenseInfo();
			if (licenseInfo != null) {
				Map<String, String> params = new HashMap<>();
				params.put("client", licenseInfo.getClientName());
				params.put("signature", licenseInfo.getSignature());
				params.put("version", getWarVersion());
				String dailyPingUpdates = sendGet(serverUrl, params);
				ObjectMapper mapper = new ObjectMapper();
				updates = mapper.readValue(dailyPingUpdates, UpdateServerPingUpdates.class);
			}
		} catch (IllegalArgumentException | IOException e) {
			/* JsonParseException could mean the sever was reached but the servlet was not
			 (ex: the servlet was not running on the server). */
			throw new AppManagementServiceRuntimeException.CannotConnectToServer(serverUrl, e);
		}

		return updates != null ? updates : new UpdateServerPingUpdates();
	}

	public String getReleaseNoteFromServer(String version, Locale currentLocale) throws CannotConnectToServer {
		String serverUrl = null;
		String releaseNote = null;

		try {
			serverUrl = getInternalServerUrl("releaseNotes");

			InputStream releaseNoteInputStream = null;
			try {
				LicenseInfo licenseInfo = getLicenseInfo();
				if (licenseInfo != null) {
					Map<String, String> params = new HashMap<>();
					params.put("client", licenseInfo.getClientName());
					params.put("signature", licenseInfo.getSignature());
					params.put("version", getWarVersion());
					params.put("release-version", version);
					params.put("language", currentLocale.toLanguageTag());
					releaseNoteInputStream = getInputForPost(serverUrl, params);

					if (releaseNoteInputStream == null) {
						throw new AppManagementServiceRuntimeException.CannotConnectToServer(serverUrl);
					}

					releaseNote = IOUtils.toString(releaseNoteInputStream, StandardCharsets.UTF_8);
				}
			} catch (IOException | RuntimeException e) {
				throw new AppManagementServiceRuntimeException.CannotConnectToServer(serverUrl, e);
			} finally {
				IOUtils.closeQuietly(releaseNoteInputStream);
			}
		} catch (IllegalArgumentException iae) {
			throw new AppManagementServiceRuntimeException.CannotConnectToServer(serverUrl, iae);
		}

		return releaseNote;
	}

	public String getWarDownloadLinkFromServer(String version) throws CannotConnectToServer {
		String serverUrl = null;
		String warDownloadLink = null;

		try {
			serverUrl = getInternalServerUrl("createCustomWar");

			try {
				LicenseInfo licenseInfo = getLicenseInfo();
				if (licenseInfo != null) {
					Map<String, String> params = new HashMap<>();
					params.put("client", licenseInfo.getClientName());
					params.put("signature", licenseInfo.getSignature());
					params.put("version", version);
					warDownloadLink = sendPost(serverUrl, params);
				}
			} catch (IOException | RuntimeException e) {
				throw new AppManagementServiceRuntimeException.CannotConnectToServer(serverUrl, e);
			}
		} catch (IllegalArgumentException iae) {
			throw new AppManagementServiceRuntimeException.CannotConnectToServer(serverUrl, iae);
		}

		return warDownloadLink;
	}

	String sendPost(String url, Map<String, String> params)
			throws IOException {
		try (InputStream is = getInputForPost(url, params)) {
			return IOUtils.toString(is, StandardCharsets.UTF_8);
		}
	}

	InputStream getInputForPost(String url, Map<String, String> params)
			throws IOException {
		URL obj = new URL(url);

		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("POST");
		con.setDoOutput(true);
		con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		con.setConnectTimeout(10000);

		StringBuilder fromParams = new StringBuilder("");
		params.forEach((key, value) -> {
			if (fromParams.length() != 0) {
				fromParams.append("&");
			}
			fromParams.append(key + "=" + value);
		});
		byte[] postData = fromParams.toString().getBytes(StandardCharsets.UTF_8);
		con.setRequestProperty("Content-Length", Integer.toString(postData.length));

		try (OutputStream os = con.getOutputStream()) {
			IOUtils.write(postData, os);
		}

		int responseCode = con.getResponseCode();
		if (responseCode == HttpServletResponse.SC_NOT_FOUND ||
			responseCode == HttpServletResponse.SC_BAD_REQUEST ||
			responseCode == HttpServletResponse.SC_INTERNAL_SERVER_ERROR) {
			throw new AppManagementServiceRuntimeException.ErrorResponseCodeException(responseCode);
		}

		return con.getInputStream();
	}

	String sendGet(String url, Map<String, String> params)
			throws IOException {
		try (InputStream is = getInputForGet(url, params)) {
			return IOUtils.toString(is, StandardCharsets.UTF_8);
		}
	}

	InputStream getInputForGet(String url, Map<String, String> params)
			throws IOException {
		URL obj = new URL(url);

		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("GET");
		con.setConnectTimeout(10000);
		params.forEach((key, value) -> con.setRequestProperty(key, value));

		int responseCode = con.getResponseCode();
		if (responseCode == HttpServletResponse.SC_NOT_FOUND ||
			responseCode == HttpServletResponse.SC_BAD_REQUEST) {
			throw new AppManagementServiceRuntimeException.ErrorResponseCodeException(responseCode);
		}

		return con.getInputStream();
	}

	boolean isProxyPage(String changelog) {
		return !changelog.contains("<version>");
	}

	public String getWebappFolderName() {
		return foldersLocator.getConstellioWebappFolder().getName();
	}

	public void markForReindexing() {
		systemLocalConfigsManager.setMarkedForReindexing(true);
	}

	public void markCacheForRebuildIfRequired() {
		if (systemLocalConfigsManager.isCacheRebuildRequired()) {
			systemLocalConfigsManager.setMarkedForCacheRebuild(true);
		}
	}

	public void markCacheForRebuild() {
		systemLocalConfigsManager.setMarkedForCacheRebuild(true);
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
			validateLicense(document);

			Element rootElement = document.getRootElement();
			String name = rootElement.getChildText("name");
			LocalDate date = DateTimeFormat.forPattern("yyyy-MM-dd")
					.parseLocalDate(rootElement.getChildText("date"));
			String signature = rootElement.getChildText("signature");
			String supportPlanCode = rootElement.getChildText("plan");
			SupportPlan supportPlan = (SupportPlan) EnumWithSmallCodeUtils.toEnumWithSmallCode(SupportPlan.class, supportPlanCode);
			Map<String, Entry<String, String>> plugins = rootElement.getChild("plugins").getChildren("plugin").stream()
					.collect(Collectors.toMap(element -> element.getAttributeValue("number"),
							element -> new SimpleEntry(element.getAttributeValue("id"), element.getText())));
			Map<String, String> additionalInfo = rootElement.getChild("additionalInfo").getChildren("info").stream()
					.collect(Collectors.toMap(element -> element.getAttributeValue("name"), Element::getText));
			long vaultQuota = Long.valueOf(rootElement.getChildText("vault"));
			long users = Long.valueOf(rootElement.getChildText("users"));
			byte servers = Byte.valueOf(rootElement.getChildText("servers"));

			license = new LicenseInfo(name, date, signature, supportPlan, plugins, additionalInfo, vaultQuota, users, servers);
		} catch (IOException | JDOMException | InvalidLicenseInstalled e) {
			SystemLogger.error("Error encountered when loading the license", e);
		}

		return license;
	}

	private void validateLicense(Document document) {
		Document clone = document.clone();
		Element rootElement = clone.getRootElement();
		String expectedSignature = rootElement.getChildText("signature");
		rootElement.getChild("signature").detach();

		EncryptionServices encryptionServices = appLayerFactory.getModelLayerFactory().newEncryptionServices();
		PublicKey publicKey = encryptionServices.createPublicKeyFromFile(foldersLocator.getVerificationKey());
		if (!encryptionServices.verify(new XMLOutputter(Format.getCompactFormat()).outputString(clone), expectedSignature, publicKey)) {
			throw new InvalidLicenseInstalled();
		}
	}

	private ConstellioVersionInfo getCurrentInstalledVersionInfo() {
		File versionDirectory = foldersLocator.getConstellioWebappFolder();
		String version = GetWarVersionUtils.getWarVersion(versionDirectory);
		return new ConstellioVersionInfo(version, versionDirectory.getAbsolutePath());
	}

	public void pointToVersionDuringApplicationStartup(ConstellioVersionInfo constellioVersionInfo) {
		updateWrapperConf(new File(constellioVersionInfo.getVersionDirectoryPath()));
	}

	private void selectSmbLibrary(File newWebAppFolder) {
		Map<String, String> configs = PropertyFileUtils.loadKeyValues(foldersLocator.getConstellioProperties());
		if ("true".equals(configs.get("smb.novell"))) {
			LOGGER.info("Replacing SMB library for Novell");
			File libFolder = foldersLocator.getLibFolder(newWebAppFolder);
			try {
				File novellSmbLib = new File(libFolder, "jcifs_novell.jar.disabled");
				LOGGER.info("Enabling : " + novellSmbLib);
				if (novellSmbLib.exists()) {
					FileUtils.moveFile(novellSmbLib, new File(libFolder, "jcifs_novell.jar"));
				}
				File defaultSmbLib = new File(libFolder, "jcifs_gcm-322.jar");
				LOGGER.info("Disabling : " + defaultSmbLib);
				if (defaultSmbLib.exists()) {
					FileUtils.moveFile(defaultSmbLib, new File(libFolder, "jcifs_gcm-322.jar.disabled"));
				}
			} catch (IOException ioe) {
				LOGGER.error("Could not install Novell/SMB libraries in the lib folder.", ioe);
			}
		}
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

	@Getter
	@AllArgsConstructor
	public static class LicenseInfo implements Serializable {
		private final String clientName;
		private final LocalDate expirationDate;
		private final String signature;
		private final SupportPlan supportPlan;
		/**
		 * [number, [id, title/name]]
		 */
		private final Map<String, Entry<String, String>> plugins;
		/**
		 * [localcode, value]
		 */
		private final Map<String, String> additionalInfo;
		private final long vaultQuota;
		private final long maxUsersAllowed;
		private final byte maxServersAllowed;
	}

}

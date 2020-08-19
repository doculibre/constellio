package com.constellio.model.services.logs;

import com.constellio.data.conf.FoldersLocator;
import com.constellio.data.io.services.facades.FileService;
import com.constellio.data.io.services.zip.ZipService;
import com.constellio.data.utils.TenantUtils;
import com.constellio.data.utils.dev.Toggle;
import com.constellio.model.services.factories.ModelLayerFactory;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class LogServices {

	public static final String EXPORT_FOLDER_RESOURCE = "LogServicesFolder";

	private FoldersLocator folderLocator;
	private FileService fileService;
	private ZipService zipService;

	public LogServices(ModelLayerFactory modelLayerFactory) {
		folderLocator = modelLayerFactory.getFoldersLocator();
		fileService = modelLayerFactory.getDataLayerFactory().getIOServicesFactory().newFileService();
		zipService = modelLayerFactory.getIOServicesFactory().newZipService();
	}

	public File exportLogs(List<String> logFilenames, boolean exportLogsFolder) throws Exception {
		if (hasSysAdminFeatures()) {
			return exportLogs(folderLocator.getWrapperInstallationFolder(), logFilenames, exportLogsFolder, folderLocator.getLogsFolder());
		} else {
			File[] logsFolderFiles = folderLocator.getTenantLogsFolder().listFiles();
			List<String> filenames = Arrays.stream(logsFolderFiles).map(File::getName).collect(Collectors.toList());
			return exportLogs(folderLocator.getTenantLogsFolder(), filenames, false, null);
		}
	}

	private File exportLogs(File rootFolder, List<String> logFilenames, boolean exportLogsFolder, File logsFolder)
			throws Exception {
		String filename = "logs-" + new SimpleDateFormat("yyyy-MM-dd HH-mm-ss").format(new Date()) + ".zip";
		File folder = fileService.newTemporaryFolder(EXPORT_FOLDER_RESOURCE);
		File zipFile = new File(folder, filename);

		List<File> logFiles = new ArrayList<>();

		for (String logFilename : logFilenames) {
			File logFile = new File(rootFolder, logFilename);
			if (logFile.exists()) {
				logFiles.add(logFile);
			}

		}

		if (exportLogsFolder) {
			if (logsFolder.exists()) {
				File[] logsFolderFiles = logsFolder.listFiles();
				if (logsFolderFiles != null) {
					logFiles.add(logsFolder);
				}
			}
		}

		if (logFiles.isEmpty()) {
			return null;
		} else {
			zipService.zip(zipFile, logFiles);
			return zipFile;
		}
	}

	private boolean hasSysAdminFeatures() {
		if (TenantUtils.isSupportingTenants()) {
			return Toggle.ENABLE_CLOUD_SYSADMIN_FEATURES.isEnabled();
		}
		return true;
	}
}

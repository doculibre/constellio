package com.constellio.model.services.logs;

import com.constellio.data.conf.FoldersLocator;
import com.constellio.data.io.services.facades.FileService;
import com.constellio.data.io.services.zip.ZipService;
import com.constellio.data.utils.TenantUtils;
import com.constellio.data.utils.dev.Toggle;
import com.constellio.model.services.factories.ModelLayerFactory;
import lombok.AllArgsConstructor;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.Zip64Mode;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

	public File exportLogs(boolean includeArchiveLogs) throws Exception {
		return exportLogs(includeArchiveLogs, null);
	}

	public File exportLogs(boolean includeArchiveLogs, File zipFolder) throws Exception {
		if (hasSysAdminFeatures()) {

			List<LogFolderToInclude> logFolderToIncludes = new ArrayList<>();
			logFolderToIncludes.add(new LogFolderToInclude(getLogFolder(), null));

			return exportAllLogs(logFolderToIncludes, true, includeArchiveLogs, zipFolder);

		} else {

			List<LogFolderToInclude> logFolderToIncludes = new ArrayList<>();
			logFolderToIncludes.add(new LogFolderToInclude(folderLocator.getTenantLogsFolder(), null));

			return exportAllLogs(logFolderToIncludes, false, includeArchiveLogs, zipFolder);
		}
	}

	private static Boolean logsDirectlyInWrapperInstallationFolder;

	private static File getLogFolder() {
		if (FoldersLocator.usingAppWrapper()) {
			if (logsDirectlyInWrapperInstallationFolder == null) {
				try {
					logsDirectlyInWrapperInstallationFolder = !FileUtils.readFileToString(new FoldersLocator().getWrapperConf(), "UTF-8").contains("wrapper.logfile=./logs/wrapper.log");
				} catch (IOException e) {
					e.printStackTrace();
					logsDirectlyInWrapperInstallationFolder = true;
				}
			}
			File wrapperInstall = new FoldersLocator().getWrapperInstallationFolder();
			return logsDirectlyInWrapperInstallationFolder ? wrapperInstall : new File(wrapperInstall, "logs");
		} else {
			return new File(new FoldersLocator().getConstellioProject(), "logs");
		}

	}


	private File exportLogs(File rootFolder, List<String> logFilenames, boolean exportLogsFolder, File logsFolder,
							File tempFolder)
			throws Exception {
		String filename = "logs-" + new SimpleDateFormat("yyyy-MM-dd HH-mm-ss").format(new Date()) + ".zip";
		File folder = tempFolder != null ? tempFolder : fileService.newTemporaryFolder(EXPORT_FOLDER_RESOURCE);
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

	@AllArgsConstructor
	public static class LogFolderToInclude {

		File logFolder;

		String subFolderInZip;

	}

	private File exportAllLogs(List<LogFolderToInclude> logFolders, boolean includeRootLogs,
							   boolean includeArchive, File zipFolder) {
		String filename = "logs-" + new SimpleDateFormat("yyyy-MM-dd HH-mm-ss").format(new Date()) + ".zip";

		if (zipFolder == null || !zipFolder.exists()) {
			zipFolder = fileService.newTemporaryFolder(EXPORT_FOLDER_RESOURCE);
		}
		File zipFile = new File(zipFolder, filename);


		OutputStream zipFileOutputStream = null;
		try {
			zipFileOutputStream = new FileOutputStream(zipFile);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
		ZipArchiveOutputStream zipOutputStream = new ZipArchiveOutputStream(zipFileOutputStream);
		zipOutputStream.setUseZip64(Zip64Mode.AsNeeded);

		FoldersLocator foldersLocator = new FoldersLocator();

		Set<String> logFiles = new HashSet<>();

		for (LogFolderToInclude logFolder : logFolders) {

			if (logFolder.logFolder.exists()
				&& !logFolder.logFolder.equals(foldersLocator.getWrapperInstallationFolder())) {

				try {
					Files.walk(logFolder.logFolder.toPath(), 2).map(Path::toFile).forEach(childFile -> {

						LocalDateTime time = Instant.ofEpochMilli(childFile.lastModified())
								.atZone(ZoneId.systemDefault())
								.toLocalDateTime();

						if (!childFile.isDirectory()
							&& childFile.length() > 0
							&& (childFile.getName().endsWith(".log") || childFile.getName().contains(".log."))
							&& (time.isAfter(LocalDateTime.now().minusDays(90)) || childFile.getName().equals("system.log"))
							&& (includeArchive || !childFile.getAbsolutePath().contains(File.separator + "archive" + File.separator))) {


							String zipFilename;
							if (logFolder.subFolderInZip == null) {
								zipFilename = childFile.getAbsolutePath().replace(logFolder.logFolder.getAbsolutePath() + File.separator, "");
							} else {
								zipFilename = childFile.getAbsolutePath().replace(logFolder.logFolder.getAbsolutePath(), logFolder.subFolderInZip);
							}
							logFiles.add(zipFilename);

							try {
								ArchiveEntry entry = zipOutputStream.createArchiveEntry(childFile, zipFilename);
								zipOutputStream.putArchiveEntry(entry);
								try (InputStream inputStream = new BufferedInputStream(new FileInputStream(childFile))) {
									IOUtils.copy(inputStream, zipOutputStream);
								}
								zipOutputStream.closeArchiveEntry();
							} catch (Throwable t) {
								t.printStackTrace();
							}

						}

					});
				} catch (IOException e) {
					e.printStackTrace();
				}

			}

		}

		if (includeRootLogs) {
			if (foldersLocator.getWrapperInstallationFolder().listFiles() != null) {
				for (File file : foldersLocator.getWrapperInstallationFolder().listFiles()) {
					if (file.getName().endsWith(".log") || file.getName().contains(".log.")) {
						LocalDateTime time = Instant.ofEpochMilli(file.lastModified())
								.atZone(ZoneId.systemDefault())
								.toLocalDateTime();
						if (file.length() > 0 && time.isAfter(LocalDateTime.now().minusDays(90))) {
							String zipFileName = logFiles.contains(file.getName()) ? ("root-" + file.getName()) : file.getName();
							try {
								ArchiveEntry entry = zipOutputStream.createArchiveEntry(file, zipFileName);
								zipOutputStream.putArchiveEntry(entry);
								try (InputStream inputStream = new BufferedInputStream(new FileInputStream(file))) {
									IOUtils.copy(inputStream, zipOutputStream);
								}
								zipOutputStream.closeArchiveEntry();
							} catch (Throwable t) {
								t.printStackTrace();
							}
						}
					}
				}
			}
		}

		try {
			zipOutputStream.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return zipFile;
	}


	private boolean hasSysAdminFeatures() {
		if (TenantUtils.isSupportingTenants()) {
			return Toggle.ENABLE_CLOUD_SYSADMIN_FEATURES.isEnabled();
		}
		return true;
	}
}

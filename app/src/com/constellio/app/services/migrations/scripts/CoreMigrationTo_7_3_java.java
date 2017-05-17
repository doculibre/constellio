package com.constellio.app.services.migrations.scripts;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileSystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.appManagement.AppManagementService;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.io.services.zip.ZipService;
import com.constellio.data.io.services.zip.ZipServiceException;
import com.constellio.model.conf.FoldersLocator;

public class CoreMigrationTo_7_3_java implements MigrationScript {
	private static final Logger LOGGER = LoggerFactory.getLogger(CoreMigrationTo_7_1.class);

	@Override
	public String getVersion() {
		return "7.3";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
			AppLayerFactory appLayerFactory) {

		AppManagementService updateService = appLayerFactory.newApplicationService();
		FoldersLocator foldersLocator = new FoldersLocator();
		File jdkZip = new File(foldersLocator.getConstellioWebinfFolder().getAbsoluteFile() + File.separator + "jdk-1.8.zip");
		File jdkFolder = new File(foldersLocator.getWrapperInstallationFolder().getAbsolutePath() + File.separator + "jdk");
		ZipService zipService = appLayerFactory.getModelLayerFactory().getIOServicesFactory().newZipService();
		File jdkBin = new File(foldersLocator.getWrapperInstallationFolder().getAbsolutePath() + File.separator + "jdk" + File.separator + "jdk1.8" + File.separator + "bin");

		if (!jdkFolder.exists()) {
			jdkFolder.mkdir();
		}

		try {
			if (FileSystemUtils.freeSpaceKb("/opt/constellio") > ((jdkBin.length() / 1024) * 2)) {
				try {
					zipService.unzip(jdkZip, jdkFolder);
				} catch (ZipServiceException ze) {
					LOGGER.error("Error while unzipping JDK");
					throw new RuntimeException("Error while unzipping JDK", ze);
				}

				setPermission(jdkBin);

				updateService.updateJDKWrapperConf(jdkBin);
			} else {
				LOGGER.error("Need more space in the Constellio folder to upgrade");
				throw new RuntimeException("Need more space in the Constellio folder to upgrade");
			}
		} catch (IOException ioe) {
			LOGGER.error("Need more space in the Constellio folder to upgrade");
			throw new RuntimeException("Need more space in the Constellio folder to upgrade", ioe);
		}
	}

	private void setPermission(File jdkBin) {
		Set<PosixFilePermission> perms = new HashSet<>();
		perms.add(PosixFilePermission.OWNER_READ);
		perms.add(PosixFilePermission.OWNER_WRITE);
		perms.add(PosixFilePermission.OWNER_EXECUTE);
		perms.add(PosixFilePermission.GROUP_READ);
		perms.add(PosixFilePermission.GROUP_EXECUTE);
		perms.add(PosixFilePermission.OTHERS_READ);

		try {
			DirectoryStream<Path> stream = Files.newDirectoryStream(jdkBin.toPath(), "*");
			for (Path path : stream) {
				Files.setPosixFilePermissions(path, perms);
			}
		} catch (IOException ioe) {
			LOGGER.error("Cannot set permission for jdk bin folder");
			throw new RuntimeException("Cannot set permission for jdk bin folder", ioe);
		}
	}
}

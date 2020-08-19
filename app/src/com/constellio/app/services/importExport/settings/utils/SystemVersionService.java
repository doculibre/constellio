package com.constellio.app.services.importExport.settings.utils;

import com.constellio.app.entities.modules.InstallableModule;
import com.constellio.app.services.appManagement.GetWarVersionUtils;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.importExport.settings.SettingsExportOptions;
import com.constellio.app.services.importExport.settings.model.ImportedSystemVersion;
import com.constellio.data.conf.FoldersLocator;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class SystemVersionService {

	private static List<String> EXCEPTION_PLUGINS = Arrays.asList("MU", "devtools");

	AppLayerFactory appLayerFactory;

	public SystemVersionService(AppLayerFactory appLayerFactory) {
		this.appLayerFactory = appLayerFactory;
	}

	public ImportedSystemVersion getSystemVersion(SettingsExportOptions options) {

		ImportedSystemVersion importedSystemVersion = new ImportedSystemVersion();
		File versionDirectory = new FoldersLocator().getConstellioWebappFolder();
		String version = GetWarVersionUtils.getWarVersion(versionDirectory);
		importedSystemVersion.setFullVersion(version);
		String[] versions = version.split("\\.");
		importedSystemVersion.setMajorVersion(Integer.parseInt(versions[0]));
		if (versions.length > 1) {
			importedSystemVersion.setMinorVersion(Integer.parseInt(versions[1]));
			if (versions.length > 2) {
				importedSystemVersion.setMinorRevisionVersion(Integer.parseInt(versions[2]));

			} else {
				importedSystemVersion.setMinorRevisionVersion(0);

			}
		} else {
			importedSystemVersion.setMinorVersion(0);
			importedSystemVersion.setMinorRevisionVersion(0);
		}

		List<InstallableModule> plugins = new ArrayList<>();
		List<String> pluginIds = new ArrayList<>();

		plugins = appLayerFactory.getPluginManager().getActivePluginModules();
		pluginIds = plugins.stream().map(plugin -> plugin.getId()).collect(Collectors.toList());
		pluginIds.removeAll(EXCEPTION_PLUGINS);

		importedSystemVersion.setPlugins(pluginIds);

		importedSystemVersion.setOnlyUSR(options != null && options.isOnlyUSR());

		return importedSystemVersion;
	}

	public boolean compareCurrentSystemVersionToImportSystemVersion(ImportedSystemVersion importedSystemVersion) {

		ImportedSystemVersion currentSystemVersion = getSystemVersion(null);

		boolean isSameVersionMajorToRevisedMinor = currentSystemVersion.getMajorVersion() == importedSystemVersion.getMajorVersion()
												   && currentSystemVersion.getMinorVersion() == importedSystemVersion.getMinorVersion()
												   && currentSystemVersion.getMinorRevisionVersion() == importedSystemVersion.getMinorRevisionVersion();

		importedSystemVersion.getPlugins().removeAll(EXCEPTION_PLUGINS);
		currentSystemVersion.getPlugins().removeAll(EXCEPTION_PLUGINS);

		boolean HasAllPlugins = listEqualsIgnoreOrder(currentSystemVersion.getPlugins(), importedSystemVersion.getPlugins());

		return (isSameVersionMajorToRevisedMinor && HasAllPlugins) || notSpecified(importedSystemVersion);
	}

	private boolean notSpecified(ImportedSystemVersion importedSystemVersion) {
		return importedSystemVersion.getMajorVersion() == 0 && importedSystemVersion.getMinorVersion() == 0
			   && importedSystemVersion.getMinorRevisionVersion() == 0;
	}

	private static <T> boolean listEqualsIgnoreOrder(List<T> list1, List<T> list2) {
		return new HashSet<>(list1).equals(new HashSet<>(list2));
	}
}

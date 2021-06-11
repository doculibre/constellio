package com.constellio.app.entities.modules;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.migrations.VersionsComparator;
import org.apache.commons.lang3.StringUtils;

public interface MigrationScript {

	default String getI18nBundleName(String module) {
		if ("combo".equals(getVersion()) || VersionsComparator.isFirstVersionBeforeSecond(getVersion(), "9.4")) {
			return (module == null ? "core_" : module + "_") + getResourcesDirectoryName();

		} else {
			return "i18n";
		}
	}

	default String getResourcesDirectoryName() {
		if ("combo".equals(getVersion())) {
			return "combo";

		} else if (VersionsComparator.isFirstVersionBeforeSecond(getVersion(), "9.4")) {
			return getVersion().replace(".", "_");

		} else {
			String version = getVersion().replace(".", "_");
			if (getClass().getSimpleName().contains(version)) {
				String name = StringUtils.substringAfter(getClass().getSimpleName(), version);
				if (name.startsWith("_")) {
					name = name.substring(1);
				}
				return version + "_" + name;
			} else {
				return getClass().getSimpleName();
			}

		}
	}

	default String getVersion() {
		String className = getClass().getName().toLowerCase();
		int indexFrom = className.indexOf("from");

		if (indexFrom == -1) {
			throw new IllegalStateException("Migration script name is invalid : " + getClass().getName());
		}

		StringBuilder stringBuilder = new StringBuilder();

		int index = indexFrom + 4;

		boolean digits = true;
		boolean newNumber = false;
		while (digits && index < className.length()) {

			char c = className.charAt(index);

			if (c == '_') {
				if (stringBuilder.length() > 0) {
					newNumber = true;
				}
			} else if (Character.isDigit(c)) {
				if (newNumber) {
					stringBuilder.append(".");
				}
				stringBuilder.append(c);
			} else {
				digits = false;
			}

			index++;
		}


		return stringBuilder.toString();

	}

	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory)
			throws Exception;


}

package com.constellio.app.services.systemInformations;

import com.constellio.data.dao.services.factories.DataLayerFactory;
import org.apache.commons.lang3.StringUtils;

public class SystemInformationsService {

	private LinuxCommandProcessor commandHelper;

	private final static String LINUX_VERSION_REQUIRED = "862.3.2";
	private final static String JAVA_VERSION_REQUIRED = "1.11.0";
	private final static String SOLR_VERSION_REQUIRED = "1.7.0";
	private final static int DISK_USAGE_LIMIT = 80;

	public SystemInformationsService() {
		commandHelper = new LinuxCommandProcessor();
	}

	public boolean isLinuxVersionDeprecated(String linuxVersion) {
		return isVersionLower(linuxVersion, LINUX_VERSION_REQUIRED);
	}

	public boolean isJavaVersionDeprecated(String javaVersion) {
		return isVersionLower(javaVersion, JAVA_VERSION_REQUIRED);
	}

	public boolean isSolrVersionDeprecated(String solrVersion) {
		return isVersionLower(solrVersion, SOLR_VERSION_REQUIRED);
	}

	public Boolean isPrivateRepositoryInstalled() {
		try {
			String value = commandHelper.getRepository();
			if (value != null) {
				return Integer.valueOf(value) > 0;
			}
			return null;
		} catch (Exception e) {
			return null;
		}
	}

	public boolean isDiskUsageProblematic(String diskUsage) {
		return Integer.valueOf(diskUsage.replace("%", "")) > DISK_USAGE_LIMIT;
	}

	public String getJavaVersion() {
		try {
			return commandHelper.getJavaVersion();
		} catch (Exception e) {
			return null;
		}
	}

	public String getLinuxVersion() {
		try {
			return commandHelper.getLinuxVersion();
		} catch (Exception e) {
			return null;
		}
	}

	public String getSolrVersion() {
		DataLayerFactory dataLayerFactory = DataLayerFactory.getLastCreatedInstance();
		return dataLayerFactory.newRecordDao().getBigVaultServer().getVersion();
	}

	public String getConstellioUser() {
		try {
			String pid = commandHelper.getPIDConstellio();
			if (StringUtils.isNotBlank(pid)) {
				return commandHelper.getUser(pid);
			}
			return null;
		} catch (Exception e) {
			return null;
		}
	}

	public String getSolrUser() {
		try {
			String pid = commandHelper.getPIDSolr();
			if (StringUtils.isNotBlank(pid)) {
				return commandHelper.getUser(pid);
			}
			return null;
		} catch (Exception e) {
			return null;
		}
	}

	public String getWrapperJavaVersion() {
		return StringUtils.substringBefore(System.getProperty("java.version"), "_");
	}

	public String getDiskUsage(String path) {
		try {
			return commandHelper.getDiskUsage(path);
		} catch (Exception e) {
			return null;
		}
	}

	private boolean isVersionLower(String version, String requiredVersion) {
		try {
			if (StringUtils.isBlank(version)) {
				return true;
			}

			String[] version1 = version.split("\\.|_");
			String[] version2 = requiredVersion.split("\\.");

			for (int i = 0; i < version1.length; i++) {
				int versionPart = Integer.valueOf(version1[i]);
				int requiredVersionPart = Integer.valueOf(version2[i]);
				if (versionPart > requiredVersionPart) {
					return false;
				} else if (versionPart < requiredVersionPart) {
					return true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return false;
	}
}
package com.constellio.app.ui.pages.management.updates;

import com.constellio.data.dao.services.bigVault.BigVaultRecordDao;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.data.dao.services.records.RecordDao;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;

import org.apache.commons.lang3.SystemUtils;
import org.apache.solr.client.solrj.SolrClient;

public class SystemInfosService {
	String behavior;
	String command;
	int versionNumber;



	public Boolean isLinuxVersionSupported(LinuxOperation versionLinux) {

		versionNumber = compareVersion(getVersionLinux(versionLinux), "862.3.2");


		if (versionNumber == 0) {
			return true;
		} else {
			return false;
		}
	}
	public Boolean isJavaVersionSupported(LinuxOperation versionLinux) {

			versionNumber = compareVersion(getVersionJava(versionLinux), "1.11.0");


			if (versionNumber == 0) {
				return true;
			} else {
				return false;
			}
	}
	public Boolean isSolrVersionSupported(String Version) {

		versionNumber = compareVersion(getSolrVersion(), "1.7.0");


		if (versionNumber == 0) {
			return true;
		} else {
			return false;
		}
	}
	public String getVersionLinux(LinuxOperation versionLinux) {
		String subVersion1 = StringUtils.substringAfter(versionLinux.getOperationBehavior(), "-");
		String subVersion2 = StringUtils.substringBefore(subVersion1, ".el7");
		return subVersion2;
	}
	public String getVersionLinuxCommande() {

		return "uname -r";
	}
	public String getVersionJavaCommande() {

		return "java -version";
	}
	public String getRepoCommande() {

		return "yum repolist 2>&1 | grep -c \"constellio_constellio-updates\"";
	}
	public String getPIDConstellioCommand() {

		return " /opt/constellio/startup status";
	}
	public String getUSErCommand(int pid) {

		return " ps -u -p "+pid+" | cut -d \" \" -f 1 ";
	}
	public String getPIDSolrCommand() {

		return "/opt/solr/bin/solr status | cut -d \" \" -f 3";
	}

	public String getVersionJava(LinuxOperation versionLinux) {
		String subVersion1 = StringUtils.substringBefore(versionLinux.getOperationBehavior(), "_131");
		return subVersion1;
	}

	public int getRepo(LinuxOperation versionLinux) {
		int number = Integer.valueOf(versionLinux.getOperationBehavior());
		return number;
	}

	public int getUserConstellioPID(LinuxOperation userConstellio) {
		String subResult1 = StringUtils.substringAfter(userConstellio.getOperationBehavior(), "PID:");
		String subResult2= StringUtils.substringBefore(subResult1, ",");

		return Integer.valueOf(subResult2);
	}
	public String getUserConstellio(LinuxOperation userConstellio) {
		String result = userConstellio.getOperationBehavior();

		return result;
	}
	public int getUserSolrPID(LinuxOperation userConstellio) {
		String SubResult1 = StringUtils.substringAfter(userConstellio.getOperationBehavior(), "Solr ");
		String SubResult2= StringUtils.substringBefore(SubResult1, "{");

		return Integer.valueOf(SubResult2);
	}

	public String getJavaVersion(LinuxOperation userConstellio) {
		String result = userConstellio.getOperationBehavior();

		return result;
	}
	public String getSolrVersion() {
		DataLayerFactory dataLayerFactory= DataLayerFactory.getLastCreatedInstance();


		String result= dataLayerFactory.newRecordDao().getBigVaultServer().getVersion();
		return result;
	}


	public int compareVersion(String subVersion, String versionCompare) {

		String[] version1 = subVersion.split("\\.");
		String[] version2 = versionCompare.split("\\.");
		int length = Math.max(version1.length, version2.length);

		for (int i = 0; i < length; i++) {
			int thisPart = i < version1.length ?
					   Integer.parseInt(version1[i]) : 0;
			int thatPart = i < version2.length ?
						   Integer.parseInt(version2[i]) : 0;
			if (thisPart < thatPart) {
				versionNumber = 0;
			}
			if (thisPart >= thatPart) {
				versionNumber = 1;
			}
		}
		return versionNumber;
	}
	public String executCommand(String commande) throws IOException, InterruptedException {



		StringBuilder sb = new StringBuilder();
		StringBuilder eb = new StringBuilder();

		int exitVal = 10000;

		Runtime rt = Runtime.getRuntime();
		Process proc = executerCommande(commande);


		InputStream stderr = proc.getInputStream();
		InputStreamReader esr = new InputStreamReader(stderr);
		BufferedReader errorReader = new BufferedReader(esr);

		String line;

		while ( (line = errorReader.readLine()) != null) {
			eb.append(line);
		}



		exitVal = proc.waitFor();

		return eb.toString();

	}
	Process executerCommande(String command)
			throws IOException {
		if (SystemUtils.IS_OS_WINDOWS) {
			String editedCommand = "cmd.exe /c " + command.replace(";", " &");

			return Runtime.getRuntime().exec(editedCommand);
		} else {
			String[] arguments = new String[]{"/bin/sh", "-c", command};
			return Runtime.getRuntime().exec(arguments);
		}
	}


}
package com.constellio.app.ui.pages.management.updates;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;

import org.apache.commons.lang3.SystemUtils;

public class SystemInfosService {
	String Behavior;
	String command;
	int versionNumber;



	public Boolean compareVersionLinux(LinuxOperation versionLinux) {

		versionNumber = CompareVersion(getVersionLinux(versionLinux), "862.3.2");


		if (versionNumber == 0) {
			return true;
		} else {
			return false;
		}
	}
	public String getVersionLinux(LinuxOperation versionLinux) {
		String SubVersion1 = StringUtils.substringAfter(versionLinux.getOperationBehavior(), "-");
		String SubVersion2 = StringUtils.substringBefore(SubVersion1, ".el7");
		return SubVersion2;
	}

	public int getRepo(LinuxOperation versionLinux) {
		int number = Integer.valueOf(versionLinux.getOperationBehavior());
		return number;
	}

	public int getUserConstellioPID(LinuxOperation userConstellio) {
		String SubResult1 = StringUtils.substringAfter(userConstellio.getOperationBehavior(), "PID:");
		String SubResult2= StringUtils.substringAfter(SubResult1, ",");

		return Integer.valueOf(SubResult2);
	}
	public String getUserConstellio(LinuxOperation userConstellio) {
		String result = userConstellio.getOperationBehavior();

		return result;
	}
	public int getUserSolrPID(LinuxOperation userConstellio) {
		String SubResult1 = StringUtils.substringAfter(userConstellio.getOperationBehavior(), "process ");
		String SubResult2= StringUtils.substringAfter(SubResult1, "running");

		return Integer.valueOf(SubResult2);
	}
	public String getUserSolr(LinuxOperation userConstellio) {
		String result = userConstellio.getOperationBehavior();

		return result;
	}




	public void DisplayVersionSolr(String Version) {

		versionNumber = CompareVersion(Version, "7.0.0");


		if (versionNumber == 1) {
			System.out.println("\033[31;1m" + "Version du Solr............" + Version);
			System.out.println("\u001B[30m");
		} else {
			System.out.println("Version du Solr............" + Version);
		}
	}



	public void DisplayUserSolr(LinuxOperation userConstellio) {
		String user = userConstellio.getOperationBehavior();

		if (!user.equals("root")) {
			System.out.println("\033[31;1m" + "Utilisateur exécutant Solr............" + user);
			System.out.println("\u001B[30m");
		} else {
			System.out.println("Utilisateur exécutant Solr............" + user);
		}
	}
	public void DisplayVersionJava(LinuxOperation versionLinux) {

		String SubVersion1 = StringUtils.substringBefore(versionLinux.getOperationBehavior(), "_");
		versionNumber = CompareVersion(SubVersion1, "1.11.0");


		if (versionNumber == 1) {
			System.out.println("\033[31;1m" + "Version Java de linux............" + versionLinux.getOperationBehavior());
			System.out.println("\u001B[30m");
		} else {
			System.out.println("Version Java de linux............" + versionLinux.getOperationBehavior());
		}
	}
	public String DisplayVersionWrapper() {
		String result=null;
		String process;
		try {

			// getRuntime: Returns the runtime object associated with the current Java application.
			// exec: Executes the specified string command in a separate process.
			Process p = Runtime.getRuntime().exec("java -version");
			BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
			while ((process = input.readLine()) != null) {
				 // <-- Print all Process here line
				// by line
				result=result+process;
			}
			input.close();
		} catch (Exception err) {
			err.printStackTrace();
		}
		return result;
	}

	public int CompareVersion(String SubVersion, String versionCompare) {

		String[] version1 = SubVersion.split("\\.");
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
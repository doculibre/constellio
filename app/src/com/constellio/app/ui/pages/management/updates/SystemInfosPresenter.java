package com.constellio.app.ui.pages.management.updates;

import java.io.IOException;

public class SystemInfosPresenter {

	SystemInfosService service = new SystemInfosService();
	public String getLinuxVersion() throws IOException, InterruptedException {
		String commande="uname -r";
		LinuxOperation operation= new LinuxOperation(commande,service.executeCommand(commande));
		return service.executeCommand(commande);
	}

	public Boolean testLinuxVersion() throws IOException, InterruptedException {
		String commande="uname -r";
		LinuxOperation operation= new LinuxOperation(commande,service.executeCommand(commande));
		return service.getVersionLinux(operation);
	}
}

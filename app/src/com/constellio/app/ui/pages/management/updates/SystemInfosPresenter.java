package com.constellio.app.ui.pages.management.updates;

import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.wrappers.User;

import java.io.IOException;

public class SystemInfosPresenter {







	public String getLinuxVersion() throws IOException, InterruptedException {
		SystemInfosService service = new SystemInfosService();
		String commande="uname -r";
		LinuxOperation operation= new LinuxOperation(commande,service.executCommand(commande));
		return service.executCommand(commande);
	}

	public Boolean testLinuxVersion() throws IOException, InterruptedException {
		SystemInfosService service = new SystemInfosService();
		String commande="uname -r";
		LinuxOperation operation= new LinuxOperation(commande,service.executCommand(commande));
		return service.getVersionLinux(operation);
	}
}



package com.constellio.app.services.migrations;

import com.constellio.app.entities.modules.MigrationScript;

import java.util.Comparator;

public class ModuleMigrationScriptsComparator implements Comparator<MigrationScript> {

	@Override
	public int compare(MigrationScript script1, MigrationScript script2) {
		return new VersionsComparator().compare(script1.getVersion(), script2.getVersion());
	}

}

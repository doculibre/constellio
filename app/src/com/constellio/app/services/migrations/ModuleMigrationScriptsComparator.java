package com.constellio.app.services.migrations;

import java.util.Comparator;

import com.constellio.app.entities.modules.MigrationScript;

public class ModuleMigrationScriptsComparator implements Comparator<MigrationScript> {

	@Override
	public int compare(MigrationScript script1, MigrationScript script2) {
		return new VersionsComparator().compare(script1.getVersion(), script2.getVersion());
	}

}

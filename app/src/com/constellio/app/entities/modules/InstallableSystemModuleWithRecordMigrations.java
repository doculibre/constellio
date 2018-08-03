package com.constellio.app.entities.modules;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.RecordMigrationScript;

import java.util.List;

//TODO : Move in InstallableModule with "default" once compiled with java 8
public interface InstallableSystemModuleWithRecordMigrations {

	List<RecordMigrationScript> getRecordMigrationScripts(String collection, AppLayerFactory appLayerFactory);

}

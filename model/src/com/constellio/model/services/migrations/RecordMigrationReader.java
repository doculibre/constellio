package com.constellio.model.services.migrations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom2.Document;
import org.jdom2.Element;

import com.constellio.model.services.migrations.RecordMigrationsManager.SchemaTypeRecordMigration;
import com.constellio.model.services.migrations.RecordMigrationsManager.SchemaTypeRecordMigrations;
import com.constellio.model.services.migrations.RecordMigrationsManager.SchemaTypesRecordMigration;
import com.constellio.model.utils.XMLConfigReader;

public class RecordMigrationReader implements XMLConfigReader<SchemaTypesRecordMigration> {

	@Override
	public SchemaTypesRecordMigration read(String collection, Document document) {
		Map<String, SchemaTypeRecordMigrations> schemaTypesRecordMigration = new HashMap<>();

		for (Element schemaTypeElement : document.getRootElement().getChildren("schemaType")) {
			String schemaTypeCode = schemaTypeElement.getAttributeValue("code");
			List<SchemaTypeRecordMigration> recordMigrations = new ArrayList<>();
			for (Element recordMigrationElement : schemaTypeElement.getChildren("migration")) {
				int version = Integer.valueOf(recordMigrationElement.getAttributeValue("version"));
				boolean finished = !"false".equals(recordMigrationElement.getAttributeValue("finished"));

				List<String> scripts = Arrays.asList(recordMigrationElement.getAttributeValue("scripts").split(","));
				recordMigrations.add(new SchemaTypeRecordMigration(version, scripts, finished));
			}
			schemaTypesRecordMigration.put(schemaTypeCode, new SchemaTypeRecordMigrations(recordMigrations));
		}

		return new SchemaTypesRecordMigration(schemaTypesRecordMigration);
	}

}

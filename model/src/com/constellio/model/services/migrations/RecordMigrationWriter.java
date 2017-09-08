package com.constellio.model.services.migrations;

import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.jdom2.Document;
import org.jdom2.Element;

import com.constellio.model.services.migrations.RecordMigrationsManager.SchemaTypeRecordMigration;
import com.constellio.model.services.migrations.RecordMigrationsManager.SchemaTypeRecordMigrations;
import com.constellio.model.services.migrations.RecordMigrationsManager.SchemaTypesRecordMigration;

public class RecordMigrationWriter {

	public void writeEmpty(Document document) {
		Element root = new Element("recordMigrations");
		document.setRootElement(root);
	}

	public void write(Document document, SchemaTypesRecordMigration schemaTypesRecordMigration) {
		Element root = new Element("recordMigrations");
		document.setRootElement(root);

		for (Entry<String, SchemaTypeRecordMigrations> entry : schemaTypesRecordMigration.schemaTypesRecordMigration.entrySet()) {
			Element schemaTypeElement = new Element("schemaType");
			schemaTypeElement.setAttribute("code", entry.getKey());
			root.addContent(schemaTypeElement);

			for (SchemaTypeRecordMigration migration : entry.getValue().migrationScripts) {
				Element migrationElement = new Element("migration");
				migrationElement.setAttribute("version", "" + migration.dataVersion);
				if (!migration.finished) {
					migrationElement.setAttribute("finished", "false");
				}
				migrationElement.setAttribute("scripts", StringUtils.join(migration.migrationScripts, ","));
				schemaTypeElement.addContent(migrationElement);
			}

		}
	}

}

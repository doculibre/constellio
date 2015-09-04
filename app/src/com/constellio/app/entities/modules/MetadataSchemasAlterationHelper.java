/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.entities.modules;

import static com.constellio.app.ui.i18n.i18n.$;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.MetadataSchemasManagerException.OptimistickLocking;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public abstract class MetadataSchemasAlterationHelper {
	protected MetadataSchemaTypesBuilder typesBuilder;
	protected String collection;
	protected ModelLayerFactory modelLayerFactory;
	protected DataLayerFactory dataLayerFactory;
	protected MigrationResourcesProvider migrationResourcesProvider;

	protected MetadataSchemasAlterationHelper(String collection, MigrationResourcesProvider migrationResourcesProvider,
			AppLayerFactory appLayerFactory) {
		this.collection = collection;
		this.modelLayerFactory = appLayerFactory.getModelLayerFactory();
		this.dataLayerFactory = modelLayerFactory.getDataLayerFactory();
		this.migrationResourcesProvider = migrationResourcesProvider;
	}

	public final void migrate() {
		MetadataSchemasManager metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
		MetadataSchemaTypes types = metadataSchemasManager.getSchemaTypes(collection);

		typesBuilder = MetadataSchemaTypesBuilder.modify(types);
		migrate(typesBuilder);
		try {
			applyI18N(typesBuilder);
			metadataSchemasManager.saveUpdateSchemaTypes(typesBuilder);
		} catch (OptimistickLocking e) {
			migrate();
		}
	}

	private void applyI18N(MetadataSchemaTypesBuilder typesBuilder) {
		for (MetadataSchemaTypeBuilder type : typesBuilder.getTypes()) {
			applyI18N(type);
		}
	}

	private MetadataSchemaTypeBuilder applyI18N(MetadataSchemaTypeBuilder schemaType) {
		String schemaTypeKey = "init." + schemaType.getCode();
		String schemaTypeLabel = migrationResourcesProvider.getDefaultLanguageString(schemaTypeKey);
		setLabel(schemaType, schemaTypeLabel, true);

		for (MetadataSchemaBuilder schemaBuilder : schemaType.getAllSchemas()) {
			String schemaKey = schemaTypeKey + "." + schemaBuilder.getLocalCode();
			String schemaLabel = migrationResourcesProvider.getDefaultLanguageString(schemaKey);
			setLabel(schemaBuilder, schemaLabel, true);
			for (MetadataBuilder metadataBuilder : schemaBuilder.getMetadatas()) {

				boolean overwrite = true;
				String specificKey = "init." + metadataBuilder.getCode().replace("_", ".");
				String label = migrationResourcesProvider.getDefaultLanguageString(specificKey);
				if (label.equals(specificKey)) {
					overwrite = false;
					String globalKey = "init.allTypes.allSchemas." + metadataBuilder.getLocalCode();
					label = migrationResourcesProvider.getDefaultLanguageString(globalKey);
					if (label.equals(globalKey)) {
						label = $(globalKey);
					}
				}
				if (label.startsWith("init.")) {
					label = specificKey;
				}

				setLabel(metadataBuilder, label, overwrite);
			}
		}
		return schemaType;
	}

	private void setLabel(MetadataBuilder metadataBuilder, String label, boolean overwrite) {
		boolean labelDefined = metadataBuilder.getInheritance() != null || (
				metadataBuilder.getLabel() != null && !metadataBuilder.getLabel().equals(metadataBuilder.getLocalCode()));
		boolean newLabelIsHumanFriendly = label != null && !label.startsWith("init.");
		if (!labelDefined || (newLabelIsHumanFriendly && overwrite)) {
			metadataBuilder.setLabel(label);
		}
	}

	private void setLabel(MetadataSchemaTypeBuilder schemaType, String label, boolean overwrite) {
		boolean labelDefined = schemaType.getLabel() != null && !schemaType.getLabel().equals(schemaType.getCode());
		boolean newLabelIsHumanFriendly = label != null && !label.startsWith("init.");
		if (!labelDefined || (newLabelIsHumanFriendly && overwrite)) {
			schemaType.setLabel(label);
		}

		setLabel(schemaType.getDefaultSchema(), label, overwrite);
	}

	private void setLabel(MetadataSchemaBuilder schema, String label, boolean overwrite) {
		boolean labelDefined = schema.getLabel() != null && !schema.getLabel().equals(schema.getLocalCode());
		boolean newLabelIsHumanFriendly = label != null && !label.startsWith("init.");

		if (!labelDefined || (newLabelIsHumanFriendly && overwrite)) {
			schema.setLabel(label);
		}
	}

	protected abstract void migrate(MetadataSchemaTypesBuilder typesBuilder);

	public MetadataSchemaTypesBuilder types() {
		return typesBuilder;
	}

	public MetadataSchemaTypeBuilder type(String code) {
		return types().getSchemaType(code);
	}

	public MetadataBuilder defaultMetadata(String typeCode, String metadataCode) {
		return type(typeCode).getMetadata(metadataCode);
	}
}

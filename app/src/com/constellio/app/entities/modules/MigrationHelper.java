package com.constellio.app.entities.modules;

import com.constellio.app.entities.schemasDisplay.SchemaDisplayConfig;
import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataSchemasRuntimeException.InvalidCodeFormat;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.security.roles.RolesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class MigrationHelper {

	private static final Logger LOGGER = LoggerFactory.getLogger(MigrationHelper.class);

	protected SchemaDisplayConfig order(String collection, AppLayerFactory appLayerFactory, String type,
										SchemaDisplayConfig schema, String... localCodes) {

		MetadataSchemaTypes schemaTypes = appLayerFactory.getModelLayerFactory()
														 .getMetadataSchemasManager().getSchemaTypes(collection);

		List<String> visibleMetadataCodes = new ArrayList<>();
		for (String localCode : localCodes) {
			visibleMetadataCodes.add(schema.getSchemaCode() + "_" + localCode);
		}
		List<String> metadataCodes = new ArrayList<>();
		metadataCodes.addAll(visibleMetadataCodes);
		List<String> otherMetadatas = new ArrayList<>();
		List<String> retrievedMetadataCodes;
		if ("form".equals(type)) {
			retrievedMetadataCodes = schema.getFormMetadataCodes();
		} else {
			retrievedMetadataCodes = schema.getDisplayMetadataCodes();
		}
		for (String retrievedMetadataCode : retrievedMetadataCodes) {
			int index = visibleMetadataCodes.indexOf(retrievedMetadataCode);
			if (index != -1) {
				metadataCodes.set(index, retrievedMetadataCode);
			} else {
				try {
					if (!schemaTypes.getMetadata(retrievedMetadataCode)
									.isSystemReserved()) {
						otherMetadatas.add(retrievedMetadataCode);
					}
				} catch (InvalidCodeFormat e) {
					LOGGER.warn("Invalid code in Schema display list of fields '" + retrievedMetadataCode + "', it is excluded");
				}
			}
		}
		SchemaDisplayConfig newSchema;
		if ("form".equals(type)) {
			metadataCodes.addAll(otherMetadatas);
			newSchema = schema.withFormMetadataCodes(metadataCodes);

			SchemasDisplayManager manager = appLayerFactory.getMetadataSchemasDisplayManager();
			for (String invisible : otherMetadatas) {
				manager.saveMetadata(manager.getMetadata(collection, invisible).withInputType(MetadataInputType.HIDDEN));
			}
		} else {
			newSchema = schema.withDisplayMetadataCodes(metadataCodes);
		}
		return newSchema;
	}

	protected void migrateRoles(String collection, AppLayerFactory appLayerFactory, RolesAlteration rolesAlteration) {

		RolesManager rolesManager = appLayerFactory.getModelLayerFactory().getRolesManager();

		for (Role role : rolesManager.getAllRoles(collection)) {
			Role updatedRole = rolesAlteration.alter(role);
			if (updatedRole != null && !updatedRole.equals(role)) {
				rolesManager.updateRole(updatedRole);
			}
		}

	}

	public interface RolesAlteration {

		Role alter(Role role);

	}
}

package com.constellio.app.ui.util;

import com.constellio.app.entities.schemasDisplay.SchemaTypeDisplayConfig;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.StorageSpace;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

import java.util.ArrayList;
import java.util.List;

public class SimpleSearchUtils {

	public static List<MetadataSchemaType> allowedSchemaTypes(User user, String collection,
															  MetadataSchemaTypes metadataSchemaTypes,
															  SchemasDisplayManager schemasDisplayManager) {
		List<MetadataSchemaType> result = new ArrayList<>();
		if (metadataSchemaTypes != null) {
			for (MetadataSchemaType type : metadataSchemaTypes.getSchemaTypes()) {
				SchemaTypeDisplayConfig config = schemasDisplayManager.getType(collection, type.getCode());
				if (config.isSimpleSearch() && isVisibleForUser(type, user)) {
					result.add(type);
				}
			}
		}
		return result;
	}

	private static boolean isVisibleForUser(MetadataSchemaType type, User currentUser) {
		if (ContainerRecord.SCHEMA_TYPE.equals(type.getCode()) &&
			!currentUser.hasAny(RMPermissionsTo.DISPLAY_CONTAINERS, RMPermissionsTo.MANAGE_CONTAINERS).onSomething()) {
			return false;
		} else {
			return !StorageSpace.SCHEMA_TYPE.equals(type.getCode()) ||
				   currentUser.has(RMPermissionsTo.MANAGE_STORAGE_SPACES).globally();
		}
	}

}

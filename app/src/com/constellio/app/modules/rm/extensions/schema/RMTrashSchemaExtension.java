package com.constellio.app.modules.rm.extensions.schema;

import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.*;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.frameworks.extensions.ExtensionBooleanResult;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.extensions.behaviors.SchemaExtension;
import com.constellio.model.extensions.events.schemas.SchemaEvent;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.security.AuthorizationsServices;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.*;

public class RMTrashSchemaExtension extends SchemaExtension {

	String collection;
	ModelLayerFactory modelLayerFactory;
	RMSchemasRecordsServices rm;

	public RMTrashSchemaExtension(String collection, AppLayerFactory appLayerFactory) {
		this.collection = collection;
		this.modelLayerFactory = appLayerFactory.getModelLayerFactory();
		this.rm = new RMSchemasRecordsServices(collection, appLayerFactory);
	}

	public ExtensionBooleanResult isPutInTrashBeforePhysicalDelete(SchemaEvent event) {
		String schemaType = new SchemaUtils().getSchemaTypeCode(event.getSchemaCode());
		switch (schemaType) {
		case Folder.SCHEMA_TYPE:
		case RetentionRule.SCHEMA_TYPE:
		case Category.SCHEMA_TYPE:
		case ContainerRecord.SCHEMA_TYPE:
		case Document.SCHEMA_TYPE:
			return ExtensionBooleanResult.TRUE;
		}
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	public LogicalSearchCondition getPhysicallyDeletableRecordsForSchemaType(SchemaEvent event) {
		String schemaType = new SchemaUtils().getSchemaTypeCode(event.getSchemaCode());
		switch (schemaType) {
			case ContainerRecord.SCHEMA_TYPE:
				List<String> adminUnitIdsWithPermissions = getConceptsWithPermissionsForUser(event.getUser(), RMPermissionsTo.DELETE_CONTAINERS);
				if(adminUnitIdsWithPermissions.isEmpty()) {
					return null;
				}
				return fromAllSchemasIn(collection).whereAllConditions(
						anyConditions(
								where(rm.containerRecord.administrativeUnits()).isNull(),
								where(rm.containerRecord.administrativeUnits()).isIn(adminUnitIdsWithPermissions)
						),
						where(Schemas.SCHEMA).isStartingWithText(ContainerRecord.SCHEMA_TYPE+"_"),
						where(Schemas.LOGICALLY_DELETED_STATUS).isTrue()
				);
		}
		return null;
	}

	public List<String> getConceptsWithPermissionsForUser(User user, String...permissions) {
		Set<String> recordIds = new HashSet<>();
		AuthorizationsServices authorizationsServices = modelLayerFactory.newAuthorizationsServices();
		for (String permission : permissions) {
			recordIds.addAll(authorizationsServices.getConceptsForWhichUserHasPermission(permission, user));
		}
		return new ArrayList<>(recordIds);
	}
}

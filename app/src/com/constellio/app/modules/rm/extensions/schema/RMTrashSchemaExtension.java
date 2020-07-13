package com.constellio.app.modules.rm.extensions.schema;

import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.dao.dto.records.FacetValue;
import com.constellio.data.frameworks.extensions.ExtensionBooleanResult;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.extensions.behaviors.SchemaExtension;
import com.constellio.model.extensions.events.schemas.SchemaEvent;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.search.SPEQueryResponse;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.security.AuthorizationsServices;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.anyConditions;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasIn;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.where;

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
				SearchServices searchServices = modelLayerFactory.newSearchServices();

				List<String> adminUnitIdsWithPermissions = getConceptsWithPermissionsForUser(event.getUser(),
						RMPermissionsTo.DELETE_CONTAINERS);
				long totalNumberOfAdministrativeUnits = searchServices.getResultsCount(from(rm.administrativeUnit.schemaType()).returnAll());
				if (adminUnitIdsWithPermissions.isEmpty()) {
					return null;
				} else if (totalNumberOfAdministrativeUnits == adminUnitIdsWithPermissions.size()) {
					return fromAllSchemasIn(collection).whereAllConditions(
							where(Schemas.SCHEMA).isStartingWithText(ContainerRecord.SCHEMA_TYPE + "_"),
							where(Schemas.LOGICALLY_DELETED_STATUS).isTrue()
					);
				} else {
					SPEQueryResponse query = searchServices.query(new LogicalSearchQuery()
							.setCondition(from(rm.containerRecord.schemaType()).returnAll())
							.addFieldFacet(ContainerRecord.ADMINISTRATIVE_UNITS + "Id_ss")
							.setNumberOfRows(0));
					List<FacetValue> fieldFacetValues = query.getFieldFacetValues(ContainerRecord.ADMINISTRATIVE_UNITS + "Id_ss");
					Set<String> administrativeUnitsWithContainers = new HashSet<>();
					for (FacetValue facetValue : fieldFacetValues) {
						administrativeUnitsWithContainers.add(facetValue.getValue());
					}

					return fromAllSchemasIn(collection).whereAllConditions(
							anyConditions(
									where(rm.containerRecord.administrativeUnits()).isNull(),
									where(rm.containerRecord.administrativeUnits()).isIn(adminUnitIdsWithPermissions)
							),
							where(Schemas.SCHEMA).isStartingWithText(ContainerRecord.SCHEMA_TYPE + "_"),
							where(Schemas.LOGICALLY_DELETED_STATUS).isTrue()
					);
				}

			case Category.SCHEMA_TYPE:
				if (event.getUser().has(RMPermissionsTo.MANAGE_CLASSIFICATION_PLAN).onSomething()) {
					return fromAllSchemasIn(collection).whereAllConditions(
							where(Schemas.SCHEMA).isStartingWithText(Category.SCHEMA_TYPE + "_"),
							where(Schemas.LOGICALLY_DELETED_STATUS).isTrue());
				} else {
					return null;
				}

			case RetentionRule.SCHEMA_TYPE:
				if (event.getUser().has(RMPermissionsTo.MANAGE_RETENTIONRULE).onSomething()) {
					return fromAllSchemasIn(collection).whereAllConditions(
							where(Schemas.SCHEMA).isStartingWithText(RetentionRule.SCHEMA_TYPE + "_"),
							where(Schemas.LOGICALLY_DELETED_STATUS).isTrue());
				} else {
					return null;
				}

		}

		return null;
	}

	public List<String> getConceptsWithPermissionsForUser(User user, String... permissions) {
		Set<String> recordIds = new HashSet<>();
		AuthorizationsServices authorizationsServices = modelLayerFactory.newAuthorizationsServices();
		for (String permission : permissions) {
			recordIds.addAll(authorizationsServices.getConceptsForWhichUserHasPermission(permission, user));
		}
		return new ArrayList<>(recordIds);
	}
}

package com.constellio.app.services.schemas.bulkImport.authorization;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.services.schemas.bulkImport.authorization.ImportedAuthorization.ImportedAuthorizationPrincipal;
import com.constellio.app.services.schemas.bulkImport.authorization.ImportedAuthorization.ImportedAuthorizationTarget;
import com.constellio.app.services.schemas.bulkImport.authorization.ImportedAuthorizationBuilderRuntimeException.ImportedAuthorizationBuilderRuntimeException_NoValidPrincipal;
import com.constellio.app.services.schemas.bulkImport.authorization.ImportedAuthorizationBuilderRuntimeException.ImportedAuthorizationBuilderRuntimeException_NoValidTarget;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.security.Authorization;
import com.constellio.model.entities.security.AuthorizationDetails;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.security.AuthorizationsServices;
import com.constellio.model.services.users.UserServices;
import com.constellio.model.services.users.UserServicesRuntimeException.UserServicesRuntimeException_NoSuchGroup;
import com.constellio.model.services.users.UserServicesRuntimeException.UserServicesRuntimeException_NoSuchUser;

public class ImportedAuthorizationToAuthorizationBuilder {
	private static final Logger LOGGER = LogManager.getLogger(ImportedAuthorizationToAuthorizationBuilder.class);

	private final String collection;
	final AuthorizationsServices authorizationsServices;
	final SearchServices searchServices;
	final SchemasRecordsServices schemas;
	final RMSchemasRecordsServices rmSchemas;
	final UserServices userServices;
	final RecordServices recordServices;

	public ImportedAuthorizationToAuthorizationBuilder(String collection,
			ModelLayerFactory modelLayerFactory) {
		this.collection = collection;
		this.authorizationsServices = modelLayerFactory.newAuthorizationsServices();
		this.searchServices = modelLayerFactory.newSearchServices();
		schemas = new SchemasRecordsServices(collection, modelLayerFactory);
		rmSchemas = new RMSchemasRecordsServices(collection, modelLayerFactory);
		userServices = modelLayerFactory.newUserServices();
		recordServices = modelLayerFactory.newRecordServices();
	}

	public Authorization build(ImportedAuthorization importedAuthorization) {
		List<String> grantedToPrincipals = getValidPrincipals(importedAuthorization);
		if (grantedToPrincipals.isEmpty()) {
			throw new ImportedAuthorizationBuilderRuntimeException_NoValidPrincipal();
		}
		List<String> grantedOnRecords = getValidTargets(importedAuthorization);
		if (grantedOnRecords.isEmpty()) {
			throw new ImportedAuthorizationBuilderRuntimeException_NoValidTarget();
		}
		List<String> roles = getRoles(importedAuthorization);
		return new Authorization(AuthorizationDetails.create(importedAuthorization.getId(), roles, collection),
				grantedToPrincipals, grantedOnRecords);
	}

	private List<String> getRoles(ImportedAuthorization importedAuthorization) {
		List<String> returnRoles = new ArrayList<>();
		List<String> roles = importedAuthorization.getRoles();
		if (roles != null) {
			returnRoles.addAll(roles);
		}
		String access = importedAuthorization.getAccess();
		if (access != null) {
			access = access.toLowerCase().trim();
			//"r", "w", "d", "rw", "rd", "rwd"
			if (access.contains("r")) {
				returnRoles.add(Role.READ);
			}
			if (access.contains("w")) {
				returnRoles.add(Role.WRITE);
			}
			if (access.contains("d")) {
				returnRoles.add(Role.DELETE);
			}
		}

		return returnRoles;
	}

	private List<String> getValidPrincipals(ImportedAuthorization importedAuthorization) {
		List<String> validPrincipalIds = new ArrayList<>();
		List<ImportedAuthorizationPrincipal> principals = importedAuthorization.getPrincipals();
		if (principals != null) {
			for (ImportedAuthorizationPrincipal principal : principals) {
				String id = getValidPrincipal(principal);
				if (id != null) {
					validPrincipalIds.add(id);
				}
			}
		}
		return validPrincipalIds;
	}

	private String getValidPrincipal(ImportedAuthorizationPrincipal principal) {
		String type = principal.getType().toLowerCase().trim();
		if (type.equals("user")) {
			return getUserId(principal.getPrincipalId());
		} else if (type.equals("group")) {
			return getGroup(principal.getPrincipalId());
		}
		return null;
	}

	private String getGroup(String groupCode) {
		try {
			Group group = userServices.getGroupInCollection(groupCode, this.collection);
			if (group == null) {
				return null;
			} else {
				return group.getId();
			}
		} catch (UserServicesRuntimeException_NoSuchGroup e) {
			return null;
		}
	}

	private String getRecordIdOfRecordWithLegacyId(String legacyId, MetadataSchema schema) {
		LogicalSearchCondition condition = LogicalSearchQueryOperators
				.from(schema).where(Schemas.LEGACY_ID).is(legacyId);
		List<Record> result = searchServices.search(new LogicalSearchQuery(condition));
		if (result == null) {
			LOGGER.warn("no record with legacyId " + legacyId + " in schema " + schema.getCode());
			return null;
		} else if (result.size() != 1) {
			LOGGER.warn("several records with legacyId " + legacyId + " in schema " + schema.getCode());
			return null;
		} else {
			return result.get(0).getId();
		}
	}

	private String getUserId(String username) {
		try {
			User user = userServices.getUserInCollection(username, this.collection);
			if (user != null) {
				return user.getId();
			} else {
				return null;
			}
		} catch (UserServicesRuntimeException_NoSuchUser e) {
			return null;
		}
	}

	private List<String> getValidTargets(ImportedAuthorization importedAuthorization) {
		List<String> validTargetIds = new ArrayList<>();
		List<ImportedAuthorizationTarget> targets = importedAuthorization.getTargets();
		if (targets != null) {
			for (ImportedAuthorizationTarget target : targets) {
				String id = getValidTarget(target);
				if (id != null) {
					validTargetIds.add(id);
				}
			}
		}
		return validTargetIds;
	}

	private String getValidTarget(ImportedAuthorizationTarget target) {
		//"folder", "document", "administrativeUnit", "userTask"
		String type = target.getType().trim();
		if (type.equals("folder")) {
			return getFolderIdOfFolderWithLegacyId(target.getLegacyId());
		} else if (type.equals("document")) {
			return getDocumentIdOfDocumentWithLegacyId(target.getLegacyId());
		} else if (type.equals("administrativeUnit")) {
			return getAdminUnitIdOfAdminUnitWithLegacyId(target.getLegacyId());
		} else if (type.equals("userTask")) {
			return getTaskIdOfTaskWithLegacyId(target.getLegacyId());
		}
		return null;
	}

	private String getTaskIdOfTaskWithLegacyId(String legacyId) {
		return getRecordIdOfRecordWithLegacyId(legacyId, schemas.defaultSchema(Task.SCHEMA_TYPE));
	}

	private String getAdminUnitIdOfAdminUnitWithLegacyId(String legacyId) {
		return getRecordIdOfRecordWithLegacyId(legacyId, rmSchemas.administrativeUnitSchema());
	}

	private String getDocumentIdOfDocumentWithLegacyId(String legacyId) {
		return getRecordIdOfRecordWithLegacyId(legacyId, rmSchemas.defaultDocumentSchema());
	}

	private String getFolderIdOfFolderWithLegacyId(String legacyId) {
		return getRecordIdOfRecordWithLegacyId(legacyId, rmSchemas.defaultFolderSchema());
	}

}

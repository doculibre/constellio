package com.constellio.app.ui.framework.builders;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.AuthorizationVO;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Authorization;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.security.roles.RolesManager;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.app.ui.util.SchemaCaptionUtils.getCaptionForRecord;
import static com.constellio.model.entities.Language.withLocale;
import static java.util.Arrays.asList;

public class AuthorizationToVOBuilder implements Serializable {
	private static final String ENABLE = "AuthorizationsView.enable";
	private static final String DISABLE = "AuthorizationsView.disable";
	transient ModelLayerFactory modelLayerFactory;

	public AuthorizationToVOBuilder(ModelLayerFactory modelLayerFactory) {
		this.modelLayerFactory = modelLayerFactory;
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		init();
	}

	private void init() {
		modelLayerFactory = ConstellioFactories.getInstance().getModelLayerFactory();
	}

	public AuthorizationVO build(Authorization authorization) {
		return build(authorization, null, null, null);
	}

	public AuthorizationVO build(Authorization authorization, Metadata receivedFromMetadata, Record receivedFromValue,
								 SessionContext sessionContext) {
		List<String> principals = authorization.getPrincipals();
		List<String> records = asList(authorization.getTarget());
		List<String> roles = authorization.getRoles();

		List<String> users = new ArrayList<>();
		List<String> groups = new ArrayList<>();
		List<String> userRoles = new ArrayList<>();
		List<String> userRolesTitles = new ArrayList<>();
		List<String> accessRoles = new ArrayList<>();

		for (String roleCode : roles) {
			RolesManager rolesManager = modelLayerFactory.getRolesManager();
			Role role = rolesManager.getRole(authorization.getCollection(), roleCode);
			if (role.isContentPermissionRole()) {
				accessRoles.add(roleCode);
			} else {
				userRoles.add(roleCode);
				userRolesTitles.add(role.getTitle());
			}
		}

		SearchServices searchServices = modelLayerFactory.newSearchServices();

		SchemasRecordsServices schemas = new SchemasRecordsServices(authorization.getCollection(), modelLayerFactory);
		List<Record> allUsers = searchServices.getAllRecords(schemas.userSchemaType());
		List<Record> allGroups = searchServices.getAllRecords(schemas.groupSchemaType());

		if (principals != null) {
			for (Record userRecord : allUsers) {
				if (userRecord != null && principals.contains(userRecord.getId())) {
					User user = schemas.wrapUser(userRecord);
					//if (user.getStatus() == UserCredentialStatus.ACTIVE) {
					users.add(userRecord.getId());
					//}
				}
			}
			for (Record groupRecord : allGroups) {
				if (groupRecord != null && principals.contains(groupRecord.getId())) {
					Group group = schemas.wrapGroup(groupRecord);
					//if (schemas.isGroupActive(group)) {
					groups.add(groupRecord.getId());
					//}
				}
			}
		}

		String metadataLabel = receivedFromMetadata == null ? null :
							   receivedFromMetadata.getLabel(withLocale(sessionContext.getCurrentLocale()));

		String recordCaption = receivedFromValue == null ? null : getCaptionForRecord(receivedFromValue,
				sessionContext.getCurrentLocale());
		String authorizationType = authorization.isNegative() ? $(DISABLE) : $(ENABLE);
		AuthorizationVO authorizationVO = new AuthorizationVO(users, groups, records, accessRoles, userRoles, userRolesTitles,
				authorization.getId(), authorization.getStartDate(),
				authorization.getEndDate(), authorization.getSharedBy(),authorization.isSynced(), metadataLabel, recordCaption, authorizationType);

		return authorizationVO;
	}
}

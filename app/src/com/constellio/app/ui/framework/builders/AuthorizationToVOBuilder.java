package com.constellio.app.ui.framework.builders;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.AuthorizationVO;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.security.Authorization;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.security.roles.RolesManager;

public class AuthorizationToVOBuilder implements Serializable {
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
		List<String> principals = authorization.getGrantedToPrincipals();
		List<String> records = authorization.getGrantedOnRecords();
		List<String> roles = authorization.getDetail().getRoles();

		List<String> users = new ArrayList<>();
		List<String> groups = new ArrayList<>();
		List<String> userRoles = new ArrayList<>();
		List<String> userRolesTitles = new ArrayList<>();
		List<String> accessRoles = new ArrayList<>();

		for (String roleCode : roles) {
			RolesManager rolesManager = modelLayerFactory.getRolesManager();
			Role role = rolesManager.getRole(authorization.getDetail().getCollection(), roleCode);
			if (role.isContentPermissionRole()) {
				accessRoles.add(roleCode);
			} else {
				userRoles.add(roleCode);
				userRolesTitles.add(role.getTitle());
			}
		}

		SearchServices searchServices = modelLayerFactory.newSearchServices();

		SchemasRecordsServices schemas = new SchemasRecordsServices(authorization.getDetail().getCollection(), modelLayerFactory);
		List<Record> allUsers = searchServices.cachedSearch(new LogicalSearchQuery(from(schemas.userSchemaType()).returnAll()));
		List<Record> allGroups = searchServices.cachedSearch(new LogicalSearchQuery(from(schemas.groupSchemaType()).returnAll()));

		if (principals != null) {
			for (Record user : allUsers) {
				if (user != null && principals.contains(user.getId())) {
					users.add(user.getId());
				}
			}
			for (Record group : allGroups) {
				if (group != null && principals.contains(group.getId())) {
					groups.add(group.getId());
				}
			}
		}

		AuthorizationVO authorizationVO = new AuthorizationVO(users, groups, records, accessRoles, userRoles, userRolesTitles,
				authorization.getDetail().getId(), authorization.getDetail().getStartDate(),
				authorization.getDetail().getEndDate(), authorization.getDetail().isSynced());

		return authorizationVO;
	}
}

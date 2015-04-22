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
package com.constellio.app.ui.framework.builders;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.AuthorizationVO;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.security.Authorization;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
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

		SchemasRecordsServices schemas = new SchemasRecordsServices(authorization.getDetail().getCollection(), modelLayerFactory);
		LogicalSearchCondition condition = from(schemas.userSchema()).where(Schemas.IDENTIFIER).isIn(principals);
		users.addAll(modelLayerFactory.newSearchServices().searchRecordIds(new LogicalSearchQuery(condition)));
		condition = from(schemas.defaultSchema(Group.SCHEMA_TYPE)).where(Schemas.IDENTIFIER).isIn(principals);
		groups.addAll(modelLayerFactory.newSearchServices().searchRecordIds(new LogicalSearchQuery(condition)));

		AuthorizationVO authorizationVO = new AuthorizationVO(users, groups, records, accessRoles, userRoles, userRolesTitles,
				authorization.getDetail().getId(), authorization.getDetail().getStartDate(),
				authorization.getDetail().getEndDate(), authorization.getDetail().isSynced());

		return authorizationVO;
	}
}

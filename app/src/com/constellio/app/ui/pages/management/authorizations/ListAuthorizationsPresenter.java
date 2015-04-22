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
package com.constellio.app.ui.pages.management.authorizations;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.constellio.app.ui.entities.AuthorizationVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.AuthorizationToVOBuilder;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.security.Authorization;
import com.constellio.model.entities.security.AuthorizationDetails;
import com.constellio.model.entities.security.CustomizedAuthorizationsBehavior;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.security.AuthorizationsServices;

public abstract class ListAuthorizationsPresenter extends BasePresenter<ListAuthorizationsView> {
	private transient AuthorizationsServices authorizationsServices;
	private transient List<Authorization> authorizations;
	protected String recordId;

	public ListAuthorizationsPresenter(ListAuthorizationsView view) {
		super(view);
	}

	public ListAuthorizationsPresenter forRequestParams(String parameters) {
		recordId = parameters;
		return this;
	}

	public abstract void backButtonClicked(String schemaCode);

	public RecordVO getRecordVO() {
		return presenterService().getRecordVO(recordId, VIEW_MODE.DISPLAY);
	}

	public List<AuthorizationVO> getInheritedAuthorizations() {
		AuthorizationToVOBuilder builder = newAuthorizationToVOBuilder();

		List<AuthorizationVO> results = new ArrayList<>();
		for (Authorization authorization : getAuthorizations()) {
			if (!(isOwnAuthorization(authorization) || authorization.getGrantedToPrincipals().isEmpty())) {
				results.add(builder.build(authorization));
			}
		}
		return results;
	}

	public List<AuthorizationVO> getOwnAuthorizations() {
		AuthorizationToVOBuilder builder = newAuthorizationToVOBuilder();

		List<AuthorizationVO> results = new ArrayList<>();
		for (Authorization authorization : getAuthorizations()) {
			if (isOwnAuthorization(authorization) && !authorization.getGrantedToPrincipals().isEmpty()) {
				results.add(builder.build(authorization));
			}
		}
		return results;
	}

	public void authorizationCreationRequested(AuthorizationVO authorizationVO) {
		Authorization authorization = toAuthorization(authorizationVO);
		authorizationsServices().add(authorization, CustomizedAuthorizationsBehavior.KEEP_ATTACHED, getCurrentUser());
		view.addAuthorization(authorizationVO);
	}

	public void authorizationModificationRequested(AuthorizationVO authorizationVO) {
		Authorization authorization = toAuthorization(authorizationVO);
		authorizationsServices().modify(authorization, CustomizedAuthorizationsBehavior.KEEP_ATTACHED, getCurrentUser());
		if (!isOwnAuthorization(authorization)) {
			view.removeAuthorization(authorizationVO);
		}
	}

	public void deleteButtonClicked(AuthorizationVO authorizationVO) {
		Authorization authorization = authorizationsServices().getAuthorization(
				view.getCollection(), authorizationVO.getAuthId());
		removeAuthorization(authorization);
		view.removeAuthorization(authorizationVO);
	}

	public abstract List<String> getAllowedAccesses();

	public List<String> getAllowedRoles() {
		List<String> allowedRoles = new ArrayList<>();

		if (getCurrentUser().has(CorePermissions.MANAGE_ROLES).globally()) {
			for (Role role : modelLayerFactory.getRolesManager().getAllRoles(view.getCollection())) {
				allowedRoles.add(role.getCode());
			}
		} else {
			allowedRoles.addAll(getCurrentUser().getUserRoles());
		}

		return allowedRoles;
	}

	protected abstract boolean isOwnAuthorization(Authorization authorization);

	protected abstract void removeAuthorization(Authorization authorization);

	private Authorization toAuthorization(AuthorizationVO authorizationVO) {
		String code = authorizationVO.getAuthId();
		AuthorizationDetails details;
		if (StringUtils.isBlank(code)) {
			code = modelLayerFactory.getDataLayerFactory().getUniqueIdGenerator().next();

			ArrayList<String> roles = new ArrayList<>();
			roles.addAll(authorizationVO.getAccessRoles());

			for (String roleCode : authorizationVO.getUserRoles()) {
				roles.add(roleCode);
			}

			details = AuthorizationDetails.create(
					code, roles, authorizationVO.getStartDate(), authorizationVO.getEndDate(), view.getCollection());
		} else {
			details = modelLayerFactory.getAuthorizationDetailsManager().get(view.getCollection(), code);
		}

		List<String> principals = new ArrayList<>();
		principals.addAll(authorizationVO.getUsers());
		principals.addAll(authorizationVO.getGroups());

		return new Authorization(details, principals, authorizationVO.getRecords());
	}

	protected AuthorizationsServices authorizationsServices() {
		if (authorizationsServices == null) {
			authorizationsServices = modelLayerFactory.newAuthorizationsServices();
		}
		return authorizationsServices;
	}

	private List<Authorization> getAuthorizations() {
		if (authorizations == null) {
			Record record = presenterService().getRecord(recordId);
			authorizations = authorizationsServices().getRecordAuthorizations(record);
		}
		return authorizations;
	}

	AuthorizationToVOBuilder newAuthorizationToVOBuilder() {
		return new AuthorizationToVOBuilder(modelLayerFactory);
	}

	public boolean seeRolesField() {
		return new ConstellioEIMConfigs(modelLayerFactory.getSystemConfigurationsManager(),collection).seeUserRolesInAuthorizations();
	}
}

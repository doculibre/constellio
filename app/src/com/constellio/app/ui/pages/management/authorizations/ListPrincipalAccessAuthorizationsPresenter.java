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

import java.util.Arrays;
import java.util.List;

import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.Authorization;
import com.constellio.model.entities.security.CustomizedAuthorizationsBehavior;
import com.constellio.model.entities.security.Role;

public class ListPrincipalAccessAuthorizationsPresenter extends ListAuthorizationsPresenter {
	public ListPrincipalAccessAuthorizationsPresenter(ListPrincipalAccessAuthorizationsView view) {
		super(view);
	}

	@Override
	public void backButtonClicked(String schemaCode) {
		if (schemaCode.equals(Group.DEFAULT_SCHEMA)) {
			view.navigateTo().displayCollectionGroup(recordId);
		} else {
			view.navigateTo().displayCollectionUser(recordId);
		}
	}

	@Override
	public boolean isDetacheable() {
		return false;
	}

	@Override
	public boolean isAttached() {
		return true;
	}

	@Override
	public List<String> getAllowedAccesses() {
		return Arrays.asList(Role.READ, Role.WRITE, Role.DELETE);
	}

	@Override
	protected boolean isOwnAuthorization(Authorization authorization) {
		return authorization.getGrantedToPrincipals().contains(recordId);
	}

	@Override
	protected void removeAuthorization(Authorization authorization) {
		if (authorization.getGrantedToPrincipals().size() == 1) {
			authorizationsServices().delete(authorization.getDetail(), getCurrentUser());
		} else {
			authorization.getGrantedToPrincipals().remove(recordId);
			authorizationsServices().modify(authorization, CustomizedAuthorizationsBehavior.KEEP_ATTACHED, getCurrentUser());
		}
	}

	@Override
	public boolean seeRolesField() {
		return false;
	}

	@Override
	public boolean seeAccessField() {
		return true;
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return user.has(CorePermissions.MANAGE_SECURITY).globally();
	}

}

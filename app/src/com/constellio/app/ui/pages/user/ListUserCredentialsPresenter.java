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
package com.constellio.app.ui.pages.user;

import java.util.HashMap;
import java.util.Map;

import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.entities.UserCredentialVO;
import com.constellio.app.ui.framework.builders.UserCredentialToVOBuilder;
import com.constellio.app.ui.framework.data.UserCredentialVODataProvider;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.model.services.users.UserServices;

public class ListUserCredentialsPresenter extends BasePresenter<ListUsersCredentialsView> {

	private transient UserServices userServices;

	public ListUserCredentialsPresenter(ListUsersCredentialsView view) {
		super(view);
		init();
	}

	private void init() {
		userServices = modelLayerFactory.newUserServices();
	}

	public UserCredentialVODataProvider getDataProvider() {
		UserCredentialToVOBuilder voBuilder = new UserCredentialToVOBuilder();
		return new UserCredentialVODataProvider(voBuilder, modelLayerFactory, null);
	}

	public void addButtonClicked() {
		String params = ParamUtils.addParams(NavigatorConfigurationService.USER_LIST, null);
		view.navigateTo().addUserCredential(params);
	}

	public void editButtonClicked(UserCredentialVO entity) {
		String parameters = getParameters(entity);
		view.navigateTo().editUserCredential(parameters);
	}

	public void displayButtonClicked(UserCredentialVO entity) {
		String parameters = getParameters(entity);
		view.navigateTo().displayUserCredential(parameters);
	}

	public void backButtonClicked() {
		view.navigateTo().adminModule();
	}

	private String getParameters(UserCredentialVO entity) {
		Map<String, Object> params = new HashMap<>();
		params.put("username", entity.getUsername());
		return ParamUtils.addParams(NavigatorConfigurationService.USER_LIST, params);
	}

	public boolean canAndOrModify() {
		return userServices.canAddOrModifyUserAndGroup();
	}
}

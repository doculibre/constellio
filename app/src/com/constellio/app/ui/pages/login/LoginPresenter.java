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
package com.constellio.app.ui.pages.login;

import java.io.Serializable;
import java.util.List;

import org.joda.time.LocalDateTime;

import com.constellio.app.modules.rm.ui.builders.UserToVOBuilder;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.entities.UserVO;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.entities.security.global.UserCredentialStatus;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.users.UserServices;
import com.vaadin.server.Page;

public class LoginPresenter implements Serializable {

	private UserToVOBuilder voBuilder = new UserToVOBuilder();

	private LoginView view;

	public LoginPresenter(LoginView view) {
		this.view = view;
	}

	public void viewEntered(String parameters) {
		signOut();
		view.updateUIContent();
	}

	private void signOut() {
		SessionContext sessionContext = view.getSessionContext();

		ModelLayerFactory modelLayerFactory = ConstellioFactories.getInstance().getModelLayerFactory();
		UserServices userServices = modelLayerFactory.newUserServices();

		User user = userServices.getUserInCollection(
				sessionContext.getCurrentUser().getUsername(), sessionContext.getCurrentCollection());
		modelLayerFactory.newLoggingServices().logout(user);

		sessionContext.setCurrentCollection(null);
		sessionContext.setCurrentUser(null);
	}

	public void signInAttempt(String username, String password) {

		ModelLayerFactory modelLayerFactory = ConstellioFactories.getInstance().getModelLayerFactory();
		UserServices userServices = modelLayerFactory.newUserServices();
		UserCredential userCredential = userServices.getUserCredential(username);
		List<String> collections = userCredential.getCollections();
		if (userCredential.getStatus() == UserCredentialStatus.ACTIVE && modelLayerFactory.newAuthenticationService()
				.authenticate(username, password)) {
			if (!collections.isEmpty()) {

				User userInLastCollection = null;
				LocalDateTime lastLogin = null;

				for (String collection : collections) {
					User userInCollection = userServices.getUserInCollection(username, collection);
					if (userInLastCollection == null) {
						userInLastCollection = userInCollection;
						lastLogin = userInCollection.getLastLogin();
					} else {
						if (lastLogin == null && userInCollection.getLastLogin() != null) {
							userInLastCollection = userInCollection;
							lastLogin = userInCollection.getLastLogin();
						} else if (lastLogin != null && userInCollection.getLastLogin() != null && userInCollection.getLastLogin()
								.isAfter(lastLogin)) {
							userInLastCollection = userInCollection;
							lastLogin = userInCollection.getLastLogin();
						}
					}
				}

				if (userInLastCollection != null) {
					try {
						modelLayerFactory.newRecordServices().update(userInLastCollection
								.setLastLogin(TimeProvider.getLocalDateTime())
								.setLastIPAddress(view.getSessionContext().getCurrentUserIPAddress()));

					} catch (RecordServicesException e) {
						throw new RuntimeException(e);
					}

					String ipAddress = Page.getCurrent().getWebBrowser().getAddress();
					modelLayerFactory.newLoggingServices().login(userInLastCollection);
					SessionContext sessionContext = view.getSessionContext();
					UserVO currentUser = voBuilder.build(userInLastCollection.getWrappedRecord(), VIEW_MODE.DISPLAY);
					sessionContext.setCurrentUser(currentUser);
					sessionContext.setCurrentCollection(userInLastCollection.getCollection());
					view.updateUIContent();
					view.navigateTo().home();
				}
			} else {
				view.showUserHasNoCollectionMessage();
			}
		} else {
			view.showBadLoginMessage();
		}

	}

}

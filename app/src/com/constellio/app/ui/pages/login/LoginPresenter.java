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

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDateTime;

import com.constellio.app.modules.rm.ui.builders.UserToVOBuilder;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.entities.UserVO;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.app.ui.pages.base.LogoUtils;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.records.wrappers.UserDocument;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.entities.security.global.UserCredentialStatus;
import com.constellio.model.services.configs.SystemConfigurationsManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.security.authentification.AuthenticationService;
import com.constellio.model.services.users.UserServices;
import com.vaadin.server.Resource;

public class LoginPresenter extends BasePresenter<LoginView> {

	private UserToVOBuilder voBuilder = new UserToVOBuilder();

	private LoginView view;

	public LoginPresenter(LoginView view) {
		super(view);
		this.view = view;
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return true;
	}

	public void viewEntered(String parameters) {
		signOut();
		view.updateUIContent();
		System.err.println(parameters);
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

	public void signInAttempt(String enteredUsername, String password) {
		ModelLayerFactory modelLayerFactory = ConstellioFactories.getInstance().getModelLayerFactory();
		UserServices userServices = modelLayerFactory.newUserServices();
		AuthenticationService authenticationService = modelLayerFactory.newAuthenticationService();

		UserCredential userCredential = userServices.getUserCredential(enteredUsername);
		String username = userCredential != null ? userCredential.getUsername() : enteredUsername;
		List<String> collections = userCredential != null ? userCredential.getCollections() : new ArrayList<String>();
		if (userCredential != null && userCredential.getStatus() == UserCredentialStatus.ACTIVE && authenticationService
				.authenticate(username, password)) {
			if (!collections.isEmpty()) {
				String lastCollection = null;
				User userInLastCollection = null;
				LocalDateTime lastLogin = null;

				for (String collection : collections) {
					User userInCollection = userServices.getUserInCollection(username, collection);
					if (userInLastCollection == null) {
						if (userInCollection != null) {
							lastCollection = collection;
							userInLastCollection = userInCollection;
							lastLogin = userInCollection.getLastLogin();
						}
					} else {
						if (lastLogin == null && userInCollection.getLastLogin() != null) {
							lastCollection = collection;
							userInLastCollection = userInCollection;
							lastLogin = userInCollection.getLastLogin();
						} else if (lastLogin != null && userInCollection.getLastLogin() != null && userInCollection.getLastLogin()
								.isAfter(lastLogin)) {
							lastCollection = collection;
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

					modelLayerFactory.newLoggingServices().login(userInLastCollection);
					SessionContext sessionContext = view.getSessionContext();
					UserVO currentUser = voBuilder.build(userInLastCollection.getWrappedRecord(), VIEW_MODE.DISPLAY);
					sessionContext.setCurrentUser(currentUser);
					sessionContext.setCurrentCollection(userInLastCollection.getCollection());
					view.updateUIContent();
					String currentState = view.navigateTo().getState();
					if (StringUtils.contains(currentState, "/")) {
						currentState = StringUtils.substringBefore(currentState, "/");
					}
					boolean homePage = NavigatorConfigurationService.HOME.equals(currentState);
					if (homePage && hasUserDocuments(userInLastCollection, lastCollection)) {
						view.navigateTo().listUserDocuments();
					}
				}
			} else {
				view.showUserHasNoCollectionMessage();
			}
		} else {
			view.showBadLoginMessage();
		}

	}

	boolean hasUserDocuments(User user, String collection) {
		SearchServices searchServices = modelLayerFactory.newSearchServices();
		MetadataSchemasManager metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
		MetadataSchemaTypes types = metadataSchemasManager.getSchemaTypes(collection);

		MetadataSchema userDocumentsSchema = types.getSchema(UserDocument.DEFAULT_SCHEMA);
		Metadata userMetadata = userDocumentsSchema.getMetadata(UserDocument.USER);
		LogicalSearchQuery query = new LogicalSearchQuery();
		query.setCondition(from(userDocumentsSchema).where(userMetadata).is(user.getId()));
		query.sortDesc(Schemas.MODIFIED_ON);
		return searchServices.getResultsCount(query) > 0;
	}

	public Resource getLogoResource() {
		return LogoUtils.getLogoResource(modelLayerFactory);
	}

	public String getLogoTarget() {
		SystemConfigurationsManager manager = modelLayerFactory.getSystemConfigurationsManager();
		String linkTarget = manager.getValue(ConstellioEIMConfigs.LOGO_LINK);
		if (StringUtils.isBlank(linkTarget)) {
			linkTarget = "http://www.constellio.com";
		}
		return linkTarget;
	}
}

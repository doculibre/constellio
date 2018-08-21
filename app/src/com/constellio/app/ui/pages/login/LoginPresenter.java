package com.constellio.app.ui.pages.login;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.ui.builders.UserToVOBuilder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.entities.UserVO;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.components.viewers.document.DocumentViewer;
import com.constellio.app.ui.i18n.i18n;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.data.io.streamFactories.StreamFactory;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.model.entities.Language;
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
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.security.authentification.AuthenticationService;
import com.constellio.model.services.users.UserServices;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

public class LoginPresenter extends BasePresenter<LoginView> {

	private static Logger LOGGER = LoggerFactory.getLogger(LoginPresenter.class);

	private UserToVOBuilder voBuilder = new UserToVOBuilder();

	private LoginView view;

	public LoginPresenter(LoginView view) {
		super(view);
		this.view = view;

		AppLayerFactory appLayerFactory = ConstellioFactories.getInstance().getAppLayerFactory();
		String mainDataLanguage = appLayerFactory.getModelLayerFactory().getConfiguration().getMainDataLanguage();
		if (mainDataLanguage != null) {
			Locale mainDataLocale = Language.withCode(mainDataLanguage).getLocale();
			i18n.setLocale(mainDataLocale);
			view.getSessionContext().setCurrentLocale(mainDataLocale);
		}

		String usernameCookieValue = view.getUsernameCookieValue();
		if (usernameCookieValue != null) {
			view.setUsername(usernameCookieValue);
		}
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return true;
	}

	public void signInAttempt(String enteredUsername, String password, boolean rememberMe) {
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
					//FIXME Disabled / Optimistic locking in load testing
					/*
					try {
						modelLayerFactory.newRecordServices().update(userInLastCollection
								.setLastLogin(TimeProvider.getLocalDateTime())
								.setLastIPAddress(view.getSessionContext().getCurrentUserIPAddress()).getWrappedRecord(), new RecordUpdateOptions().setOptimisticLockingResolution(OptimisticLockingResolution.KEEP_OLDER));
					} catch (RecordServicesException e) {
						LOGGER.error("Unable to update user : " + username, e);
					}
					*/
					if(userCredential.hasAgreedToPrivacyPolicy() || getPrivacyPolicyConfigValue() == null) {
//						signInValidated(userInLastCollection, lastCollection);
						buildPrivacyPolicyWindow(userInLastCollection, lastCollection);
					} else {
						buildPrivacyPolicyWindow(userInLastCollection, lastCollection);
					}
				}
			} else {
				view.showUserHasNoCollectionMessage();
			}
			if (rememberMe) {
				view.setUsernameCookie(username);
			} else {
				view.setUsernameCookie(null);
			}
		} else {
			view.showBadLoginMessage();
		}
	}

	private void signInValidated(User userInLastCollection, String lastCollection) {
		modelLayerFactory.newLoggingServices().login(userInLastCollection);
		Locale userLocale = getSessionLanguage(userInLastCollection);
		SessionContext sessionContext = view.getSessionContext();
		UserVO currentUser = voBuilder
				.build(userInLastCollection.getWrappedRecord(), VIEW_MODE.DISPLAY, sessionContext);
		sessionContext.setCurrentUser(currentUser);
		sessionContext.setCurrentCollection(userInLastCollection.getCollection());
		sessionContext.setForcedSignOut(false);
		i18n.setLocale(userLocale);
		sessionContext.setCurrentLocale(userLocale);

		view.updateUIContent();
		String currentState = view.navigateTo().getState();
		if (StringUtils.contains(currentState, "/")) {
			currentState = StringUtils.substringBefore(currentState, "/");
		}
		boolean homePage = NavigatorConfigurationService.HOME.equals(currentState);
		if (homePage && hasUserDocuments(userInLastCollection, lastCollection)) {
			view.navigate().to(RMViews.class).listUserDocuments();
		}
	}

	Locale getSessionLanguage(User userInLastCollection) {
		String userPreferredLanguage = userInLastCollection.getLoginLanguageCode();
		String systemLanguage = modelLayerFactory.getConfiguration().getMainDataLanguage();
		if (StringUtils.isBlank(userPreferredLanguage)) {
			return getLocale(systemLanguage);
		} else {
			List<String> collectionLanguages = modelLayerFactory.getCollectionsListManager()
					.getCollectionLanguages(userInLastCollection.getCollection());
			if (collectionLanguages == null || collectionLanguages.isEmpty() || !collectionLanguages
					.contains(userPreferredLanguage)) {
				return getLocale(systemLanguage);
			} else {
				return getLocale(userPreferredLanguage);
			}
		}
	}

	private Locale getLocale(String languageCode) {
		for (Language language : Language.values()) {
			if (language.getCode().equals(languageCode)) {
				return new Locale(languageCode);
			}
		}
		throw new ImpossibleRuntimeException("Invalid language " + languageCode);
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

	public String getLogoTarget() {
		SystemConfigurationsManager manager = modelLayerFactory.getSystemConfigurationsManager();
		String linkTarget = manager.getValue(ConstellioEIMConfigs.LOGO_LINK);
		if (StringUtils.isBlank(linkTarget)) {
			linkTarget = "http://www.constellio.com";
		}
		return linkTarget;
	}

	public void buildPrivacyPolicyWindow(final User userInLastCollection, final String lastCollection) {
		final Window window = new Window();
		window.setWidth("90%");
		window.setHeight("90%");
		window.setModal(true);
		window.setCaption($("LoginView.privacyPolicyWindow"));
		
//		SystemConfigurationsManager manager = modelLayerFactory.getSystemConfigurationsManager();
//		StreamFactory<InputStream> streamFactory = manager.getValue(ConstellioEIMConfigs.PRIVACY_POLICY);
		
		VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setSizeFull();
		mainLayout.setSpacing(true);
		
		VerticalLayout textLayout = new VerticalLayout();
		
		HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.setSpacing(true);
		buttonLayout.setHeight("50px");

		BaseButton cancelButton = new BaseButton($("cancel")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				window.close();
			}
		};
		BaseButton acceptButton = new BaseButton($("accept")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				UserServices userServices = modelLayerFactory.newUserServices();
				userServices.addUpdateUserCredential(userServices.getUserCredential(userInLastCollection.getUsername())
						.withAgreedPrivacyPolicy(true));
				signInValidated(userInLastCollection, lastCollection);
				window.close();
			}
		};
		acceptButton.addStyleName(ValoTheme.BUTTON_PRIMARY);

		textLayout.addComponent(new DocumentViewer(getPrivacyPolicyFile()));
		buttonLayout.addComponents(acceptButton, cancelButton);
		
		mainLayout.addComponents(textLayout, buttonLayout);
		mainLayout.setComponentAlignment(buttonLayout, Alignment.BOTTOM_CENTER);
		
		window.setContent(mainLayout);
		
		ConstellioUI.getCurrent().addWindow(window);
	}

	public File getPrivacyPolicyFile() {
		SystemConfigurationsManager manager = modelLayerFactory.getSystemConfigurationsManager();
		StreamFactory<InputStream> streamFactory = manager.getValue(ConstellioEIMConfigs.PRIVACY_POLICY);
		InputStream returnStream = null;
		if (streamFactory != null) {
			try {
				returnStream = streamFactory.create("privacyPolicy_eimUSR");
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}
		if (returnStream == null) {
			return null;
		}

		File file = new File("privacyPolicy_eimUSR");
		try {
			FileUtils.copyInputStreamToFile(returnStream, file);
			//TODO Francis file created by resource is not removed from file system
			modelLayerFactory.getDataLayerFactory().getIOServicesFactory().newIOServices().closeQuietly(returnStream);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} finally {
			IOUtils.closeQuietly(returnStream);
		}
		return file;
	}

	public Object getPrivacyPolicyConfigValue() {
		SystemConfigurationsManager manager = modelLayerFactory.getSystemConfigurationsManager();
		return manager.getValue(ConstellioEIMConfigs.PRIVACY_POLICY);
	}
}

package com.constellio.app.ui.pages.profile;

import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.model.enums.DefaultTabInFolderDisplay;
import com.constellio.app.modules.rm.ui.builders.UserToVOBuilder;
import com.constellio.app.modules.rm.wrappers.RMUser;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.entities.UserVO;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.enums.SearchPageLength;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.security.global.AgentStatus;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.configs.SystemConfigurationsManager;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.security.authentification.AuthenticationService;
import com.constellio.model.services.users.UserPhotosServices;
import com.constellio.model.services.users.UserPhotosServicesRuntimeException.UserPhotosServicesRuntimeException_UserHasNoPhoto;
import com.constellio.model.services.users.UserServices;
import com.google.common.base.Joiner;
import org.apache.commons.collections.CollectionUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ModifyProfilePresenter extends BasePresenter<ModifyProfileView> {
	public static final String CHANGE_PHOTO_STREAM = "ConstellioMenuPresenter-ChangePhotoStream";
	public static final String SHOW_PICTURE_STREAM = "ConstellioMenuPresenter-ShowPicture";
	public static final String ADMIN = "admin";

	private transient UserServices userServices;
	private transient AuthenticationService authenticationService;
	private transient RecordServices recordServices;
	private transient UserPhotosServices userPhotosServices;

	private String username;
	private String parameters;
	private Language language;

	public ModifyProfilePresenter(ModifyProfileView view) {
		super(view);
		init();
		language = Language.withCode(view.getSessionContext().getCurrentLocale().getLanguage());
	}

	public void saveButtonClicked(ProfileVO profileVO, HashMap<String, Object> additionnalMetadataValues) {
		User user = userServices.getUserInCollection(profileVO.getUsername(), view.getCollection());
		user.setPhone(profileVO.getPhone());
		user.setJobTitle(profileVO.getJobTitle());
		user.setAddress(profileVO.getAddress());
		user.setFax(profileVO.getFax());
		user.setDefaultPageLength(profileVO.getDefaultPageLength());
		if (profileVO.getStartTab() == null) {
			user.setStartTab(getDefaultStartTab());
		} else {
			user.setStartTab(profileVO.getStartTab());
		}
		if (profileVO.getDefaultTabInFolderDisplay() == null) {
			user.setDefaultTabInFolderDisplay(DefaultTabInFolderDisplay.METADATA.getCode());
		} else {
			user.setDefaultTabInFolderDisplay(profileVO.getDefaultTabInFolderDisplay().getCode());
		}
		user.setDefaultTaxonomy(profileVO.getDefaultTaxonomy());
		user.setLoginLanguageCode(profileVO.getLoginLanguageCode());

		if (isRMModuleActivated()) {
			user.set(RMUser.DEFAULT_ADMINISTRATIVE_UNIT, profileVO.getDefaultAdministrativeUnit());
			user.set(RMUser.HIDE_NOT_ACTIVE, profileVO.isHideNotActive());
		}

		try {
			if (profileVO.getPassword() != null && profileVO.getPassword().equals(profileVO.getConfirmPassword())) {
				authenticationService.changePassword(profileVO.getUsername(), profileVO.getOldPassword(), profileVO.getPassword());
			}

			MetadataSchema userSchema = user.getSchema();
			Iterator<Entry<String, Object>> additionnalMetadatasIterator = additionnalMetadataValues.entrySet().iterator();
			while (additionnalMetadatasIterator.hasNext()) {
				Map.Entry<String, Object> metadataValue = additionnalMetadatasIterator.next();
				if (userSchema.hasMetadataWithCode(metadataValue.getKey())) {
					user.set(metadataValue.getKey(), metadataValue.getValue());
				}
			}

			recordServices.update(user.getWrappedRecord());

			changePhoto(profileVO.getImage());

			updateUserCredential(profileVO, additionnalMetadataValues);

			view.updateUI();
		} catch (RecordServicesException e) {
			e.printStackTrace();
			return;
		}
		navigateToBackPage();

	}

	private void updateUserCredential(final ProfileVO profileVO,
									  HashMap<String, Object> additionnalMetadataValues) {
		String username = profileVO.getUsername();
		UserCredential userCredential = (UserCredential) userServices.getUserCredential(username);

		userCredential = (UserCredential) userCredential.
				setFirstName(profileVO.getFirstName())
				.setLastName(profileVO.getLastName())
				.setEmail(profileVO.getEmail())
				.setJobTitle(profileVO.getJobTitle())
				.setPhone(profileVO.getPhone())
				.setAddress(profileVO.getAddress())
				.setFax(profileVO.getFax());

		if (profileVO.getPersonalEmails() != null) {
			userCredential = (UserCredential) userCredential.setPersonalEmails(Arrays.asList(profileVO.getPersonalEmails().split("\n")));
		}

		MetadataSchema userCredentialSchema = userCredential.getSchema();
		Iterator<Entry<String, Object>> additionnalMetadatasIterator = additionnalMetadataValues.entrySet().iterator();
		while (additionnalMetadatasIterator.hasNext()) {
			Map.Entry<String, Object> metadataValue = additionnalMetadatasIterator.next();
			if (userCredentialSchema.hasMetadataWithCode(metadataValue.getKey())) {
				userCredential.set(metadataValue.getKey(), metadataValue.getValue());
			}
		}

		userServices.addUpdateUserCredential(userCredential);

		SessionContext sessionContext = view.getSessionContext();
		String collection = view.getCollection();
		User user = userServices.getUserInCollection(username, collection);

		UserToVOBuilder voBuilder = new UserToVOBuilder();
		UserVO userVO = voBuilder
				.build(user.getWrappedRecord(), VIEW_MODE.DISPLAY, sessionContext);
		sessionContext.setCurrentUser(userVO);
	}

	private String getDefaultStartTab() {
		return presenterService().getSystemConfigs().getDefaultStartTab();
	}

	void changePhoto(ContentVersionVO image) {
		if (image != null) {
			userPhotosServices.changePhoto(image.getInputStreamProvider().getInputStream(CHANGE_PHOTO_STREAM), username);
		}
	}

	public ProfileVO getProfileVO(String username) {
		UserCredential userCredential = userServices.getUserCredential(username);
		String firstName = userCredential.getFirstName();
		String lastName = userCredential.getLastName();
		String email = userCredential.getEmail();
		List<String> personalEmails = userCredential.getPersonalEmails();

		User user = userServices.getUserInCollection(username, view.getCollection());
		String phone = user.getPhone();
		String fax = user.getFax();
		String jobTitle = user.getJobTitle();
		String address = user.getAddress();
		String loginLanguage = user.getLoginLanguageCode();
		String defaultAdministrativeUnit = null;
		boolean hideNotActive = false;
		if (isRMModuleActivated()) {
			defaultAdministrativeUnit = user.get(RMUser.DEFAULT_ADMINISTRATIVE_UNIT);
			try {
				recordServices().getDocumentById(defaultAdministrativeUnit);
			} catch (Exception e) {
				defaultAdministrativeUnit = null;
			}
			Boolean hideNotActiveUserParam = user.get(RMUser.HIDE_NOT_ACTIVE);
			if (Boolean.TRUE.equals(hideNotActiveUserParam)) {
				hideNotActive = true;
			}
		}
		if (loginLanguage == null || loginLanguage.isEmpty()) {
			loginLanguage = view.getSessionContext().getCurrentLocale().getLanguage();
		}
		String startTab = user.getStartTab();
		if (startTab == null) {
			startTab = getDefaultStartTab();
		}

		SystemConfigurationsManager systemConfigurationsManager = modelLayerFactory.getSystemConfigurationsManager();
		RMConfigs rmConfigs = new RMConfigs(systemConfigurationsManager);

		Map<String, DefaultTabInFolderDisplay> defaultTabInFolderDisplayOptions = new HashMap<>();
		for (DefaultTabInFolderDisplay retrievedDefaultTabInFolderDisplay : DefaultTabInFolderDisplay.values()) {
			defaultTabInFolderDisplayOptions.put(retrievedDefaultTabInFolderDisplay.getCode(), retrievedDefaultTabInFolderDisplay);
		}

		DefaultTabInFolderDisplay defaultTabInFolderDisplay = null;
		if (user.getDefaultTabInFolderDisplay() != null) {
			for (DefaultTabInFolderDisplay retrievedDefaultTabInFolderDisplay : DefaultTabInFolderDisplay.values()) {
				if (user.getDefaultTabInFolderDisplay().equals(retrievedDefaultTabInFolderDisplay.getCode())) {
					defaultTabInFolderDisplay = retrievedDefaultTabInFolderDisplay;
					break;
				}
			}
		}
		if (defaultTabInFolderDisplay == null) {
			String configDefaultTabInFolderDisplayCode = rmConfigs.getDefaultTabInFolderDisplay();
			if (configDefaultTabInFolderDisplayCode != null) {
				defaultTabInFolderDisplay = defaultTabInFolderDisplayOptions.get(configDefaultTabInFolderDisplayCode);
			}
		}

		String defaultTaxonomy = user.getDefaultTaxonomy();
		if (defaultTaxonomy == null) {
			defaultTaxonomy = presenterService().getSystemConfigs().getDefaultTaxonomy();
		}

		UserCredential userCredentials = (UserCredential) userServices.getUser(username);
		AgentStatus agentStatus = userCredentials.getAgentStatus();
		boolean agentManuallyDisabled = agentStatus == AgentStatus.MANUALLY_DISABLED;

		SearchPageLength defaultPageLength = user.getDefaultPageLength();

		ProfileVO profileVO = newProfileVO(username, firstName, lastName, email, personalEmails, phone, fax, jobTitle, address, startTab, defaultTabInFolderDisplay,
				defaultTaxonomy, agentManuallyDisabled, hideNotActive, defaultAdministrativeUnit, defaultPageLength);
		profileVO.setLoginLanguageCode(loginLanguage);
		return profileVO;
	}

	ProfileVO newProfileVO(String username, String firstName, String lastName, String email,
						   List<String> personalEmails, String phone,
						   String fax, String jobTitle, String address, String startTab,
						   DefaultTabInFolderDisplay defaultTabInFolderDisplay, String defaultTaxonomy,
						   boolean agentManuallyDisabled, boolean hideNotActive, String defaultAdministrativeUnit,
						   SearchPageLength defaultPageLength) {
		String personalEmailsPresentation = null;
		if (!CollectionUtils.isEmpty(personalEmails)) {
			personalEmailsPresentation = Joiner.on("\n").join(personalEmails);
		}

		return new ProfileVO(username, firstName, lastName, email, personalEmailsPresentation, phone, fax, jobTitle, address, startTab, defaultTabInFolderDisplay,
				defaultTaxonomy, defaultPageLength, null, null, null, agentManuallyDisabled, hideNotActive, defaultAdministrativeUnit);
	}

	public void cancelButtonClicked() {
		navigateToBackPage();
	}

	public InputStream newUserPhotoInputStream() {
		String username = getUsername();
		UserPhotosServices photosServices = ConstellioFactories.getInstance().getModelLayerFactory().newUserPhotosServices();

		try {
			return photosServices.getPhotoInputStream(username).create(SHOW_PICTURE_STREAM);
		} catch (UserPhotosServicesRuntimeException_UserHasNoPhoto u) {
			return null;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public boolean hasCurrentUserPhoto() {
		UserVO currentUser = view.getSessionContext().getCurrentUser();
		UserPhotosServices photosServices = ConstellioFactories.getInstance().getModelLayerFactory().newUserPhotosServices();
		return photosServices.hasPhoto(currentUser.getUsername());
	}

	public void setParameters(String parameters) {
		this.parameters = parameters;
	}

	public String getParameters() {
		return parameters;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getUsername() {
		return username;
	}

	void navigateToBackPage() {
		view.navigate().to().url(parameters);
	}

	public boolean canModify() {
		if (username.equals(ADMIN)) {
			return true;
		} else {
			return userServices.canAddOrModifyUserAndGroup();
		}
	}

	public boolean canModifyPassword() {
		UserCredential user = userServices.getUserCredential(username);
		return userServices.canModifyPassword(user, user);
	}

	public boolean isLDAPAuthentication() {
		return userServices.isLDAPAuthentication();
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return true;
	}

	private void init() {
		userServices = modelLayerFactory.newUserServices();
		authenticationService = modelLayerFactory.newAuthenticationService();
		recordServices = modelLayerFactory.newRecordServices();
		userPhotosServices = modelLayerFactory.newUserPhotosServices();
	}


	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		init();
	}

	public List<String> getCurrentCollectionLanguagesCodes() {
		return modelLayerFactory.getCollectionsListManager().getCollectionLanguages(collection);
	}

	public boolean isRMModuleActivated() {
		return appLayerFactory.getModulesManager().isModuleEnabled(collection, new ConstellioRMModule());
	}

	public boolean isPasswordChangeEnabled() {
		return !ADMIN.equals(username) || new ConstellioEIMConfigs(modelLayerFactory).isAdminPasswordChangeEnabled();
	}

	public User getUserRecord() {
		return getCurrentUser();
	}
}

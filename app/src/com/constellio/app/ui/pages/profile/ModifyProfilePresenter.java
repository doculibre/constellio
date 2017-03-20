package com.constellio.app.ui.pages.profile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;

import com.constellio.app.entities.navigation.PageItem;
import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.model.enums.DefaultTabInFolderDisplay;
import com.constellio.app.modules.rm.ui.util.ConstellioAgentUtils;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.TaxonomyVO;
import com.constellio.app.ui.framework.builders.TaxonomyToVOBuilder;
import com.constellio.app.ui.framework.data.TaxonomyVODataProvider;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.app.ui.pages.home.HomeView;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.global.AgentStatus;
import com.constellio.model.entities.security.global.SolrUserCredential;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.configs.SystemConfigurationsManager;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.security.authentification.AuthenticationService;
import com.constellio.model.services.users.UserPhotosServices;
import com.constellio.model.services.users.UserPhotosServicesRuntimeException.UserPhotosServicesRuntimeException_UserHasNoPhoto;
import com.constellio.model.services.users.UserServices;
import com.google.common.base.Joiner;

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

	public ModifyProfilePresenter(ModifyProfileView view) {
		super(view);
		init();
	}

	public List<String> getAvailableHomepageTabs() {
		List<String> result = new ArrayList<>();
		for (PageItem tab : navigationConfig().getFragments(HomeView.TABS)) {
			result.add(tab.getCode());
		}
		return result;
	}

	public void saveButtonClicked(ProfileVO profileVO) {
		User user = userServices.getUserInCollection(profileVO.getUsername(), view.getCollection());
		user.setPhone(profileVO.getPhone());
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

		try {
			if (profileVO.getPassword() != null && profileVO.getPassword().equals(profileVO.getConfirmPassword())) {
				authenticationService.changePassword(profileVO.getUsername(), profileVO.getOldPassword(), profileVO.getPassword());
			}

			recordServices.update(user.getWrappedRecord());

			changePhoto(profileVO.getImage());

            updateUserCredential(profileVO);

			view.updateUI();
		} catch (RecordServicesException e) {
			e.printStackTrace();
			return;
		}
		navigateToBackPage();

	}

    private void updateUserCredential(final ProfileVO profileVO) {
    	String username = profileVO.getUsername();
        SolrUserCredential userCredential = (SolrUserCredential) userServices.getUserCredential(username);

        userCredential = (SolrUserCredential) userCredential.
                withFirstName(profileVO.getFirstName())
                .withLastName(profileVO.getLastName())
                .withEmail(profileVO.getEmail());

        if (profileVO.getPersonalEmails() != null) {
            userCredential = (SolrUserCredential) userCredential.withPersonalEmails(Arrays.asList(profileVO.getPersonalEmails().split("\n")));
        }

    	boolean agentManuallyDisabled = profileVO.isAgentManuallyDisabled();
    	AgentStatus previousAgentStatus = userCredential.getAgentStatus();
    	if (previousAgentStatus == AgentStatus.MANUALLY_DISABLED && !agentManuallyDisabled) {
    		userCredential.setAgentStatus(AgentStatus.ENABLED);
    	} else if (previousAgentStatus != AgentStatus.MANUALLY_DISABLED && agentManuallyDisabled) {
    		userCredential.setAgentStatus(AgentStatus.MANUALLY_DISABLED);
    	}

        userServices.addUpdateUserCredential(userCredential);
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
		String loginLanguage = user.getLoginLanguageCode();
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
			if (configDefaultTabInFolderDisplayCode != null){
				defaultTabInFolderDisplay = defaultTabInFolderDisplayOptions.get(configDefaultTabInFolderDisplayCode);
			}
		}
		
		String defaultTaxonomy = user.getDefaultTaxonomy();
		if (defaultTaxonomy == null) {
			defaultTaxonomy = presenterService().getSystemConfigs().getDefaultTaxonomy();
		}
		
		SolrUserCredential userCredentials = (SolrUserCredential) userServices.getUser(username);
		AgentStatus agentStatus = userCredentials.getAgentStatus();
		boolean agentManuallyDisabled = agentStatus == AgentStatus.MANUALLY_DISABLED;

		ProfileVO profileVO = newProfileVO(username, firstName, lastName, email, personalEmails, phone, startTab, defaultTabInFolderDisplay,
				defaultTaxonomy, agentManuallyDisabled);
		profileVO.setLoginLanguageCode(loginLanguage);
		return profileVO;
	}

	ProfileVO newProfileVO(String username, String firstName, String lastName, String email, List<String> personalEmails, String phone,
			String startTab, DefaultTabInFolderDisplay defaultTabInFolderDisplay, String defaultTaxonomy, boolean agentManuallyDisabled) {
        String personalEmailsPresentation = null;
        if (!CollectionUtils.isEmpty(personalEmails)) {
            personalEmailsPresentation = Joiner.on("\n").join(personalEmails);
        }

		return new ProfileVO(username, firstName, lastName, email, personalEmailsPresentation, phone, startTab, defaultTabInFolderDisplay,
				defaultTaxonomy, null, null, null, agentManuallyDisabled);
	}

	public void cancelButtonClicked() {
		navigateToBackPage();
	}

	List<TaxonomyVO> getEnabledTaxonomies() {
		TaxonomyVODataProvider provider = newDataProvider();
		return provider.getTaxonomyVOs();
	}

	TaxonomyVODataProvider newDataProvider() {
		return new TaxonomyVODataProvider(newVoBuilder(), modelLayerFactory,
				view.getCollection(), view.getSessionContext().getCurrentUser().getUsername());
	}

	private TaxonomyToVOBuilder newVoBuilder() {
		return new TaxonomyToVOBuilder();
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
		
		view.setAgentManuallyDisabledVisible(isAgentManuallyDisabledVisible());
	}
	
	private boolean isAgentManuallyDisabledVisible() {
		UserServices userServices = modelLayerFactory.newUserServices();
		SystemConfigurationsManager systemConfigurationsManager = modelLayerFactory.getSystemConfigurationsManager();
		
		RMConfigs rmConfigs = new RMConfigs(systemConfigurationsManager);

		String username = view.getSessionContext().getCurrentUser().getUsername();
		SolrUserCredential userCredentials = (SolrUserCredential) userServices.getUser(username);
		AgentStatus agentStatus = userCredentials.getAgentStatus();
		if (agentStatus == AgentStatus.DISABLED && !rmConfigs.isAgentDisabledUntilFirstConnection()) {
			agentStatus = AgentStatus.ENABLED;
		}
		
		return rmConfigs.isAgentEnabled() && ConstellioAgentUtils.isAgentSupported() && agentStatus != AgentStatus.DISABLED;
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		init();
	}

	public List<String> getCurrentCollectionLanguagesCodes() {
		return modelLayerFactory.getCollectionsListManager().getCollectionLanguages(collection);
	}
}

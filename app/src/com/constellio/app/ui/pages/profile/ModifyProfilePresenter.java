package com.constellio.app.ui.pages.profile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.constellio.app.entities.navigation.PageItem;
import com.constellio.app.modules.rm.model.enums.DefaultTabInFolderDisplay;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.TaxonomyVO;
import com.constellio.app.ui.framework.builders.TaxonomyToVOBuilder;
import com.constellio.app.ui.framework.data.TaxonomyVODataProvider;
import com.constellio.app.ui.i18n.i18n;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.app.ui.pages.home.HomeView;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.security.authentification.AuthenticationService;
import com.constellio.model.services.users.UserPhotosServices;
import com.constellio.model.services.users.UserPhotosServicesRuntimeException.UserPhotosServicesRuntimeException_UserHasNoPhoto;
import com.constellio.model.services.users.UserServices;
import com.google.common.base.Joiner;
import org.apache.commons.collections.CollectionUtils;

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

	public void saveButtonClicked(ProfileVO entity) {
		User user = userServices.getUserInCollection(entity.getUsername(), view.getCollection());
		user.setPhone(entity.getPhone());
		if (entity.getStartTab() == null) {
			user.setStartTab(getDefaultHomepageTab());
		} else {
			user.setStartTab(entity.getStartTab());
		}
		if (entity.getDefaultTabInFolderDisplay() == null) {
			user.setDefaultTabInFolderDisplay(DefaultTabInFolderDisplay.METADATA.getCode());
		} else {
			user.setDefaultTabInFolderDisplay(entity.getDefaultTabInFolderDisplay().getCode());
		}
		user.setDefaultTaxonomy(entity.getDefaultTaxonomy());
		user.setLoginLanguageCode(entity.getLoginLanguageCode());

		try {
			if (entity.getPassword() != null && entity.getPassword().equals(entity.getConfirmPassword())) {
				authenticationService.changePassword(entity.getUsername(), entity.getOldPassword(), entity.getPassword());
			}

			recordServices.update(user.getWrappedRecord());

			changePhoto(entity.getImage());

            updateUserCredential(entity);

			view.updateUI();
		} catch (RecordServicesException e) {
			e.printStackTrace();
			return;
		}
		navigateToBackPage();

	}

    private void updateUserCredential(final ProfileVO entity) {
        UserCredential userCredential = userServices.getUserCredential(entity.getUsername());

        userCredential = userCredential.
                withFirstName(entity.getFirstName())
                .withLastName(entity.getLastName())
                .withEmail(entity.getEmail());

        if (entity.getPersonalEmails() != null) {
            userCredential = userCredential.withPersonalEmails(Arrays.asList(entity.getPersonalEmails().split("\n")));
        }

        userServices.addUpdateUserCredential(userCredential);
    }

	private String getDefaultHomepageTab() {
		List<String> tabs = getAvailableHomepageTabs();
		return tabs.isEmpty() ? null : tabs.get(0);
	}

	void changePhoto(ContentVersionVO image) {
		if (image != null) {
			userPhotosServices.changePhoto(image.getInputStreamProvider().getInputStream(CHANGE_PHOTO_STREAM), username);
		}
	}

	public ProfileVO getProfilVO(String username) {
		UserCredential userCredential = userServices.getUserCredential(username);
		String firstName = userCredential.getFirstName();
		String lastName = userCredential.getLastName();
		String email = userCredential.getEmail();
		List<String> personalEmails = userCredential.getPersonalEmails();

		User user = userServices.getUserInCollection(username, view.getCollection());
		String phone = user.getPhone();
		String loginLanguage = user.getLoginLanguageCode();
		String startTab = user.getStartTab();
		if (startTab == null) {
			startTab = getDefaultHomepageTab();
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
			defaultTabInFolderDisplay = DefaultTabInFolderDisplay.METADATA;
		}
		String defaultTaxonomy = user.getDefaultTaxonomy();

		ProfileVO profileVO = newProfilVO(username, firstName, lastName, email, personalEmails, phone, startTab, defaultTabInFolderDisplay,
				defaultTaxonomy);
		profileVO.setLoginLanguageCode(loginLanguage);
		return profileVO;
	}

	ProfileVO newProfilVO(String username, String firstName, String lastName, String email, List<String> personalEmails, String phone,
			String startTab, DefaultTabInFolderDisplay defaultTabInFolderDisplay, String defaultTaxonomy) {
        String personalEmailsPresentation = null;
        if (!CollectionUtils.isEmpty(personalEmails)) {
            personalEmailsPresentation = Joiner.on("\n").join(personalEmails);
        }

		return new ProfileVO(username, firstName, lastName, email, personalEmailsPresentation, phone, startTab, defaultTabInFolderDisplay,
				defaultTaxonomy, null, null, null);
	}

	public void cancelButtonClicked() {
		navigateToBackPage();
	}

	List<TaxonomyVO> getEnableTaxonomies() {
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

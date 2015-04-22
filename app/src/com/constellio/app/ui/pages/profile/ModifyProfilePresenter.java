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
package com.constellio.app.ui.pages.profile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.modules.rm.model.enums.StartTab;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.TaxonomyVO;
import com.constellio.app.ui.framework.builders.TaxonomyToVOBuilder;
import com.constellio.app.ui.framework.data.TaxonomyVODataProvider;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.app.ui.pages.globalGroup.AddEditGlobalGroupPresenter;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.security.authentification.AuthenticationService;
import com.constellio.model.services.users.UserPhotosServices;
import com.constellio.model.services.users.UserPhotosServicesRuntimeException.UserPhotosServicesRuntimeException_UserHasNoPhoto;
import com.constellio.model.services.users.UserServices;

public class ModifyProfilePresenter extends BasePresenter<ModifyProfileView> {

	private static final String CHANGE_PHOTO_STREAM = "ConstellioMenuPresenter-ChangePhotoStream";
	private static final String SHOW_PICTURE_STREAM = "ConstellioMenuPresenter-ShowPicture";
	private static final Logger LOGGER = LoggerFactory.getLogger(AddEditGlobalGroupPresenter.class);
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

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		init();
	}

	private void init() {
		userServices = modelLayerFactory.newUserServices();
		authenticationService = modelLayerFactory.newAuthenticationService();
		recordServices = modelLayerFactory.newRecordServices();
		userPhotosServices = modelLayerFactory.newUserPhotosServices();
	}

	public void saveButtonClicked(ProfileVO entity) {

		User user = userServices.getUserInCollection(entity.getUsername(), view.getCollection());
		user.setPhone(entity.getPhone());
		if (entity.getStartTab() == null) {
			user.setStartTab(StartTab.RECENT_FOLDERS.getCode());
		} else {
			user.setStartTab(entity.getStartTab().getCode());
		}
		user.setDefaultTaxonomy(entity.getDefaultTaxonomy());

		try {
			if (entity.getPassword() != null && entity.getPassword().equals(entity.getConfirmPassword())) {
				authenticationService.changePassword(entity.getUsername(), entity.getOldPassword(), entity.getPassword());
			}

			recordServices.update(user.getWrappedRecord());

			ContentVersionVO image = entity.getImage();

			changePhoto(image);

			UserCredential userCredential = userServices.getUserCredential(entity.getUsername());
			userCredential = userCredential.withFirstName(entity.getFirstName())
					.withLastName(entity.getLastName())
					.withEmail(entity.getEmail());
			userServices.addUpdateUserCredential(userCredential);

			view.updateUI();
		} catch (RecordServicesException e) {
			return;
		}
		navigateToBackPage();

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

		User user = userServices.getUserInCollection(username, view.getCollection());
		String phone = user.getPhone();
		StartTab startTab = null;
		if (user.getStartTab() != null) {
			for (StartTab retrievedStartTab : StartTab.values()) {
				if (user.getStartTab().equals(retrievedStartTab.getCode())) {
					startTab = retrievedStartTab;
					break;
				}
			}
		}
		if (startTab == null) {
			startTab = StartTab.RECENT_FOLDERS;
		}
		String defaultTaxonomy = user.getDefaultTaxonomy();

		ProfileVO profileVO = newProfilVO(username, firstName, lastName, email, phone, startTab, defaultTaxonomy);
		return profileVO;
	}

	ProfileVO newProfilVO(String username, String firstName, String lastName, String email, String phone,
			StartTab startTab, String defaultTaxonomy) {
		return new ProfileVO(username, firstName, lastName, email, phone, startTab,
				defaultTaxonomy, null, null, null);
	}

	public void cancelButtonClicked() {
		navigateToBackPage();
	}

	List<String> getEnableTaxonomiesCodes() {
		TaxonomyVODataProvider provider = newDataProvider();
		return provider.getTaxonomyVOsCodes();
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
		view.navigateTo().url(parameters);
	}

	public boolean canAndOrModify() {
		return userServices.canAddOrModifyUserAndGroup();
	}

	public boolean canModifyPassword() {
		UserCredential userCredential = userServices.getUserCredential(view.getSessionContext().getCurrentUser().getUsername());
		return userServices.canModifyPassword(userCredential);
	}
}



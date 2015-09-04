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

import static com.constellio.app.ui.i18n.i18n.$;

import java.io.InputStream;

import org.apache.commons.lang.StringUtils;

import com.constellio.app.modules.rm.model.enums.DefaultTabInFolderDisplay;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.TaxonomyVO;
import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.app.ui.framework.components.converters.TempFileUploadToContentVersionVOConverter;
import com.constellio.app.ui.framework.components.fields.ListOptionGroup;
import com.constellio.app.ui.framework.components.fields.enumWithSmallCode.EnumWithSmallCodeOptionGroup;
import com.constellio.app.ui.framework.components.fields.upload.BaseUploadField;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.model.frameworks.validation.ValidationException;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.Validator;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.data.validator.EmailValidator;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.StreamResource;
import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.ui.Component;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

public class ModifyProfileViewImpl extends BaseViewImpl implements ModifyProfileView {
	public static final String UPDATE_PICTURE_STREAM_SOURCE = "ModifyProfileViewImpl-UpdatePictureStreamSource";

	private ProfileVO profileVO;
	private VerticalLayout mainLayout;
	private Panel panel;
	private StreamResource imageResource;
	private Embedded image;
	private BaseForm<ProfileVO> form;

	@PropertyId("username")
	private TextField usernameField;
	@PropertyId("image")
	private BaseUploadField imageField;
	@PropertyId("firstName")
	private TextField firstNameField;
	@PropertyId("lastName")
	private TextField lastNameField;
	@PropertyId("email")
	private TextField emailField;
	@PropertyId("phone")
	private TextField phoneField;
	@PropertyId("password")
	private PasswordField passwordField;
	@PropertyId("confirmPassword")
	private PasswordField confirmPasswordField;
	@PropertyId("oldPassword")
	private PasswordField oldPasswordField;
	@PropertyId("startTab")
	private OptionGroup startTabField;
	@PropertyId("defaultTabInFolderDisplay")
	private EnumWithSmallCodeOptionGroup defaultTabInFolderDisplay;
	@PropertyId("defaultTaxonomy")
	private ListOptionGroup taxonomyField;

	ModifyProfilePresenter presenter;

	public ModifyProfileViewImpl() {
		this.presenter = new ModifyProfilePresenter(this);
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeEvent event) {
		String parameters = event.getParameters();
		presenter.setParameters(parameters);

		String username = getSessionContext().getCurrentUser().getUsername();
		presenter.setUsername(username);
		profileVO = presenter.getProfilVO(username);
	}

	@Override
	protected String getTitle() {
		return $("ModifyProfileView.viewTitle");
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		mainLayout = new VerticalLayout();
		mainLayout.setSizeFull();
		mainLayout.setSpacing(true);

		usernameField = new TextField();
		usernameField.setCaption($("ModifyProfileView.username"));
		usernameField.setRequired(true);
		usernameField.setNullRepresentation("");
		usernameField.setId("username");
		usernameField.addStyleName("username");
		usernameField.setVisible(true);
		usernameField.setEnabled(false);

		imageResource = new StreamResource(readSourceStream(), presenter.getUsername() + ".png");
		image = new Embedded("", imageResource);
		panel = new Panel("", image);
		panel.setWidth("150");
		panel.setHeight("150");
		setupImageSize(image);

		imageField = new BaseUploadField();
		imageField.setId("image");
		imageField.addStyleName("image");
		imageField.setCaption($("ModifyProfileView.image"));
		imageField.setUploadButtonCaption($("ModifyProfileView.upload"));
		imageField.setMultiValue(false);
		imageField.setConverter(new TempFileUploadToContentVersionVOConverter());
		imageField.addValidator(new Validator() {
			@Override
			public void validate(Object value)
					throws InvalidValueException {
				if (value != null) {
					ContentVersionVO contentVersionVO = (ContentVersionVO) value;
					if (!contentVersionVO.getMimeType().contains("image")) {
						throw new InvalidValueException($("invalid image"));
					}
				}
			}
		});
		imageField.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				ContentVersionVO contentVersionVO = (ContentVersionVO) imageField.getValue();
				if (contentVersionVO != null) {
					updatePicture(new StreamSource() {
						@Override
						public InputStream getStream() {
							ContentVersionVO contentVersionVO = (ContentVersionVO) imageField.getValue();
							return contentVersionVO.getInputStreamProvider().getInputStream(UPDATE_PICTURE_STREAM_SOURCE);
						}
					});
				}
			}
		});
		imageField.setEnabled(presenter.canModify());

		firstNameField = new TextField();
		firstNameField.setCaption($("ModifyProfileView.firstName"));
		firstNameField.setRequired(!presenter.isLDAPAuthentication());
		firstNameField.setNullRepresentation("");
		firstNameField.setId("firstName");
		firstNameField.addStyleName("firstName");
		firstNameField.setEnabled(presenter.canModify());

		lastNameField = new TextField();
		lastNameField.setCaption($("ModifyProfileView.lastName"));
		lastNameField.setRequired(!presenter.isLDAPAuthentication());
		lastNameField.setNullRepresentation("");
		lastNameField.setId("lastName");
		lastNameField.addStyleName("lastName");
		lastNameField.setEnabled(presenter.canModify());

		emailField = new TextField();
		emailField.setCaption($("ModifyProfileView.email"));
		emailField.setRequired(!presenter.isLDAPAuthentication());
		emailField.setNullRepresentation("");
		emailField.setId("email");
		emailField.addStyleName("email");
		emailField.addValidator(new EmailValidator($("ModifyProfileView.invalidEmail")));
		emailField.setEnabled(presenter.canModify());

		phoneField = new TextField();
		phoneField.setCaption($("ModifyProfileView.phone"));
		phoneField.setRequired(false);
		phoneField.setNullRepresentation("");
		phoneField.setId("phone");
		phoneField.addStyleName("phone");
		phoneField.setEnabled(presenter.canModify());

		passwordField = new PasswordField();
		passwordField.setCaption($("ModifyProfileView.password"));
		passwordField.setNullRepresentation("");
		passwordField.setId("password");
		passwordField.addStyleName("password");
		passwordField.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				if (passwordField.getValue() != null && StringUtils.isNotBlank(passwordField.getValue())) {
					confirmPasswordField.setRequired(true);
					oldPasswordField.setRequired(true);
				} else {
					passwordField.setValue(null);
					confirmPasswordField.setRequired(false);
					oldPasswordField.setRequired(false);
				}
			}
		});
		passwordField.setEnabled(presenter.canModifyPassword());

		confirmPasswordField = new PasswordField();
		confirmPasswordField.setCaption($("ModifyProfileView.confirmPassword"));
		confirmPasswordField.setNullRepresentation("");
		confirmPasswordField.setId("confirmPassword");
		confirmPasswordField.addStyleName("confirmPassword");
		Validator passwordFieldsValidator = new Validator() {
			@Override
			public void validate(Object value)
					throws InvalidValueException {
				if (passwordField.getValue() != null && !passwordField.getValue().equals(confirmPasswordField.getValue())) {
					confirmPasswordField.focus();
					throw new InvalidValueException($("ModifyProfileView.passwordsFieldsMustBeEquals"));
				}
			}
		};
		confirmPasswordField.addValidator(passwordFieldsValidator);
		confirmPasswordField.setEnabled(presenter.canModifyPassword());

		oldPasswordField = new PasswordField();
		oldPasswordField.setCaption($("ModifyProfileView.oldPassword"));
		oldPasswordField.setNullRepresentation("");
		oldPasswordField.setId("oldPassword");
		oldPasswordField.addStyleName("oldPassword");
		oldPasswordField.setEnabled(presenter.canModifyPassword());

		startTabField = new OptionGroup($("ModifyProfileView.startTab"));
		startTabField.setId("startTab");
		for (String tab : presenter.getAvailableHomepageTabs()) {
			startTabField.addItem(tab);
			startTabField.setItemCaption(tab, $("HomeView.tab." + tab));
		}

		defaultTabInFolderDisplay = new EnumWithSmallCodeOptionGroup(DefaultTabInFolderDisplay.class);
		defaultTabInFolderDisplay.setCaption($("ModifyProfileView.defaultTabInFolderDisplay"));
		defaultTabInFolderDisplay.setId("defaultTabInFolderDisplay");
		defaultTabInFolderDisplay.setItemCaption(DefaultTabInFolderDisplay.SUB_FOLDERS,
				$("defaultTabInFolderDisplay." + DefaultTabInFolderDisplay.SUB_FOLDERS));
		defaultTabInFolderDisplay.setItemCaption(DefaultTabInFolderDisplay.DOCUMENTS,
				$("defaultTabInFolderDisplay." + DefaultTabInFolderDisplay.DOCUMENTS));
		defaultTabInFolderDisplay.setItemCaption(DefaultTabInFolderDisplay.METADATA,
				$("defaultTabInFolderDisplay." + DefaultTabInFolderDisplay.METADATA));

		taxonomyField = new ListOptionGroup($("ModifyProfileView.defaultTaxonomy"));
		taxonomyField.addStyleName("defaultTaxonomy");
		taxonomyField.setId("defaultTaxonomy");
		taxonomyField.setMultiSelect(false);
		taxonomyField.setRequired(false);
		for (TaxonomyVO value : presenter.getEnableTaxonomies()) {
			taxonomyField.addItem(value.getCode());
			taxonomyField.setItemCaption(value.getCode(), value.getTitle());
		}

		form = new BaseForm<ProfileVO>(profileVO, this, imageField, usernameField, firstNameField, lastNameField, emailField,
				phoneField, passwordField, confirmPasswordField, oldPasswordField, startTabField, defaultTabInFolderDisplay,
				taxonomyField) {
			@Override
			protected void saveButtonClick(ProfileVO profileVO)
					throws ValidationException {
				presenter.saveButtonClicked(profileVO);
			}

			@Override
			protected void cancelButtonClick(ProfileVO profileVO) {
				presenter.cancelButtonClicked();
			}
		};

		mainLayout.addComponents(panel, form);
		return mainLayout;
	}

	private void setupImageSize(Embedded image) {
		image.setHeight("148");
		image.setWidth("148");
	}

	private void updatePicture(StreamSource streamSource) {
		imageResource.setStreamSource(streamSource);
		Embedded newImage = new Embedded("", imageResource);
		setupImageSize(newImage);
		panel.setContent(newImage);
		image = newImage;
	}

	private StreamSource readSourceStream() {
		return new StreamSource() {
			@Override
			public InputStream getStream() {
				return presenter.newUserPhotoInputStream();
			}
		};
	}

	@Override
	public void updateUI() {
		ConstellioUI.getCurrent().updateContent();
	}

}

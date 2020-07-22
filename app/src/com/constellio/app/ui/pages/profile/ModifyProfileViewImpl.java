package com.constellio.app.ui.pages.profile;

import com.constellio.app.api.extensions.params.RecordFieldsExtensionParams;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.app.ui.framework.components.converters.TempFileUploadToContentVersionVOConverter;
import com.constellio.app.ui.framework.components.fields.AdditionnalRecordField;
import com.constellio.app.ui.framework.components.fields.BaseComboBox;
import com.constellio.app.ui.framework.components.fields.EditablePasswordField;
import com.constellio.app.ui.framework.components.fields.SignatureRecordField;
import com.constellio.app.ui.framework.components.fields.upload.BaseUploadField;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.model.frameworks.validation.ValidationException;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.Validator;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.data.validator.EmailValidator;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.Resource;
import com.vaadin.server.StreamResource;
import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Field;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import org.apache.commons.lang.StringUtils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;
import static java.util.Arrays.asList;

public class ModifyProfileViewImpl extends BaseViewImpl implements ModifyProfileView {
	public static final String UPDATE_PICTURE_STREAM_SOURCE = "ModifyProfileViewImpl-UpdatePictureStreamSource";

	private ProfileVO profileVO;
	private VerticalLayout mainLayout;
	private Resource imageResource;
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
	@PropertyId("personalEmails")
	private TextArea personalEmailsField;
	@PropertyId("phone")
	private TextField phoneField;
	@PropertyId("fax")
	private TextField faxField;
	@PropertyId("address")
	private TextField addressField;
	@PropertyId("jobTitle")
	private TextField jobTitleField;
	@PropertyId("password")
	private EditablePasswordField passwordField;
	@PropertyId("confirmPassword")
	private EditablePasswordField confirmPasswordField;
	@PropertyId("oldPassword")
	private EditablePasswordField oldPasswordField;
	@PropertyId("loginLanguageCode")
	private ComboBox loginLanguageCodeField;

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
		profileVO = presenter.getProfileVO(username);
	}

	@Override
	protected String getTitle() {
		return $("ModifyProfileView.viewTitle");
	}

	@Override
	protected Component buildMainComponent(final ViewChangeEvent event) {
		mainLayout = new VerticalLayout();
		mainLayout.setWidth("100%");
		mainLayout.setSpacing(true);

		usernameField = new TextField();
		usernameField.setCaption($("ModifyProfileView.username"));
		usernameField.setRequired(true);
		usernameField.setNullRepresentation("");
		usernameField.setId("username");
		usernameField.addStyleName("username");
		usernameField.setVisible(true);
		usernameField.setEnabled(false);

		if (presenter.hasCurrentUserPhoto()) {
			imageResource = new StreamResource(readSourceStream(), presenter.getUsername() + ".png");
		} else {
			imageResource = new ThemeResource("images/profiles/default.jpg");
		}
		image = new Embedded("", imageResource);
		image.addStyleName("modify-profile-image");
		image.setWidth("150px");
		image.setHeight("150px");

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

		personalEmailsField = new TextArea();
		personalEmailsField.setCaption($("ModifyProfileView.personalEmails"));
		personalEmailsField.setRequired(false);
		personalEmailsField.setNullRepresentation("");
		personalEmailsField.setId("personalEmails");
		personalEmailsField.addStyleName("email");
		personalEmailsField.addValidator(new Validator() {
			private Validator emailValidator = new EmailValidator($("ModifyProfileView.invalidEmail"));

			@Override
			public void validate(Object value) throws InvalidValueException {
				if (value != null) {
					for (final String email : ((String) value).split("\n")) {
						emailValidator.validate(email);
					}
				}
			}
		});

		phoneField = new TextField();
		phoneField.setCaption($("ModifyProfileView.phone"));
		phoneField.setRequired(false);
		phoneField.setNullRepresentation("");
		phoneField.setId("phone");
		phoneField.addStyleName("phone");
		phoneField.setEnabled(presenter.canModify());

		faxField = new TextField();
		faxField.setCaption($("UserCredentialView.fax"));
		faxField.setRequired(false);
		faxField.setNullRepresentation("");
		faxField.setId("phone");
		faxField.addStyleName("phone");
		faxField.setEnabled(presenter.canModify());

		jobTitleField = new TextField();
		jobTitleField.setCaption($("UserCredentialView.jobTitle"));
		jobTitleField.setRequired(false);
		jobTitleField.setNullRepresentation("");
		jobTitleField.setId("phone");
		jobTitleField.addStyleName("phone");
		jobTitleField.setEnabled(presenter.canModify());

		addressField = new TextField();
		addressField.setCaption($("ModifyProfileView.address"));
		addressField.setRequired(false);
		addressField.setNullRepresentation("");
		addressField.setId("phone");
		addressField.addStyleName("phone");
		addressField.setEnabled(presenter.canModify());

		passwordField = new EditablePasswordField();
		passwordField.setCaption($("ModifyProfileView.password"));
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
		passwordField.setReadOnly(!presenter.isPasswordChangeEnabled());

		confirmPasswordField = new EditablePasswordField();
		confirmPasswordField.setCaption($("ModifyProfileView.confirmPassword"));
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
		confirmPasswordField.setReadOnly(!presenter.isPasswordChangeEnabled());

		oldPasswordField = new EditablePasswordField();
		oldPasswordField.setCaption($("ModifyProfileView.oldPassword"));
		oldPasswordField.setId("oldPassword");
		oldPasswordField.addStyleName("oldPassword");
		oldPasswordField.setEnabled(presenter.canModifyPassword());
		oldPasswordField.setReadOnly(!presenter.isPasswordChangeEnabled());

		loginLanguageCodeField = new BaseComboBox($("ModifyProfileView.loginLanguageCode"));
		loginLanguageCodeField.setId("loginLanguageCode");
		loginLanguageCodeField.setRequired(true);
		loginLanguageCodeField.setNullSelectionAllowed(false);
		for (String code : presenter.getCurrentCollectionLanguagesCodes()) {
			loginLanguageCodeField.addItem(code);
			loginLanguageCodeField.setItemCaption(code, $("Language." + code));
		}
		loginLanguageCodeField.setEnabled(true);

		List<Field> allFields = new ArrayList<Field>(asList(imageField, usernameField, firstNameField, lastNameField, emailField, personalEmailsField,
				phoneField, faxField, jobTitleField, addressField, passwordField, confirmPasswordField, oldPasswordField, loginLanguageCodeField));

		final List<AdditionnalRecordField> configFields = getAdditionnalFields();
		allFields.addAll(configFields);

		final List<SignatureRecordField> signatureFields = getSignatureFields();
		allFields.addAll(signatureFields);

		form = new BaseForm<ProfileVO>(profileVO, this, allFields.toArray(new Field[0])) {
			@Override
			protected void saveButtonClick(ProfileVO profileVO)
					throws ValidationException {
				HashMap<String, Object> additionnalMetadataValues = new HashMap<>();
				for(AdditionnalRecordField field: configFields) {
					field.commit();
					additionnalMetadataValues.put(field.getMetadataLocalCode(), field.getCommittableValue());
				}

				for (SignatureRecordField field : signatureFields) {
					field.commit();
					additionnalMetadataValues.put(field.getMetadataLocalCode(), field.getCommittableValue());
				}
				presenter.saveButtonClicked(profileVO, additionnalMetadataValues);
			}

			@Override
			protected void cancelButtonClick(ProfileVO profileVO) {
				presenter.cancelButtonClicked();
			}

			@Override
			protected String getTabCaption(Field<?> field, Object propertyId) {
				if(field instanceof AdditionnalRecordField) {
					return $("ModifyProfileView.configsTab");
				} else if (field instanceof SignatureRecordField) {
					return $("ModifyProfileView.signatureTab");
				} else {
					return $("ModifyProfileView.profileTab");
				}
			}
		};

		mainLayout.addComponents(image, form);
		mainLayout.setComponentAlignment(image, Alignment.TOP_CENTER);
		return mainLayout;
	}

	private void updatePicture(StreamSource streamSource) {
		imageResource = new StreamResource(streamSource, presenter.getUsername() + ".png");
		image.setSource(imageResource);
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

	public List<AdditionnalRecordField> getAdditionnalFields() {
		RecordFieldsExtensionParams params = new RecordFieldsExtensionParams(this, presenter.getUserRecord().getWrappedRecord());
		return getConstellioFactories().getAppLayerFactory().getExtensions().forCollection(getCollection()).getAdditionnalFields(params);
	}

	private List<SignatureRecordField> getSignatureFields() {
		RecordFieldsExtensionParams params = new RecordFieldsExtensionParams(this, presenter.getUserRecord().getWrappedRecord());
		return getConstellioFactories().getAppLayerFactory().getExtensions().forCollection(getCollection()).getSignatureFields(params);
	}
}

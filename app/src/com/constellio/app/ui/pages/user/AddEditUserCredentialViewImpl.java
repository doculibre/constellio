package com.constellio.app.ui.pages.user;

import com.constellio.app.ui.entities.UserCredentialVO;
import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.app.ui.framework.components.fields.EditablePasswordField;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.model.entities.security.global.UserCredentialStatus;
import com.constellio.model.entities.security.global.UserSyncMode;
import com.constellio.model.frameworks.validation.ValidationException;
import com.vaadin.data.Validator;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.data.validator.EmailValidator;
import com.vaadin.event.FieldEvents.BlurEvent;
import com.vaadin.event.FieldEvents.BlurListener;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;

import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;

public class AddEditUserCredentialViewImpl extends BaseViewImpl implements AddEditUserCredentialView {

	private AddEditUserCredentialPresenter presenter;

	private UserCredentialVO userCredentialVO;

	private Map<String, String> paramsMap;

	private boolean addActionMode = true;

	@PropertyId("username")
	private TextField usernameField;

	@PropertyId("firstName")
	private TextField firstNameField;

	@PropertyId("lastName")
	private TextField lastNameField;

	@PropertyId("email")
	private TextField emailField;

	@PropertyId("jobTitle")
	private TextField jobTitle;

	@PropertyId("phone")
	private TextField phone;

	@PropertyId("fax")
	private TextField fax;

	@PropertyId("address")
	private TextField address;

	@PropertyId("personalEmails")
	private TextArea personalEmailsField;

	@PropertyId("password")
	private EditablePasswordField passwordField;

	@PropertyId("confirmPassword")
	private EditablePasswordField confirmPasswordField;

	@PropertyId("collections")
	private OptionGroup collectionsField;

	@PropertyId("status")
	private OptionGroup statusField;

	public AddEditUserCredentialViewImpl() {
		this.presenter = new AddEditUserCredentialPresenter(this);
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeEvent event) {
		setupParamsAndVO(event);
	}

	private void setupParamsAndVO(ViewChangeEvent event) {
		String parameters = event.getParameters();

		paramsMap = ParamUtils.getParamsMap(parameters);
		if (paramsMap != null && paramsMap.get("username") != null) {
			userCredentialVO = presenter.getUserCredentialVO(paramsMap.get("username"));
			addActionMode = false;
		} else {
			userCredentialVO = new UserCredentialVO();
		}
	}

	@Override
	protected String getTitle() {
		return $("AddEditUserCredentialView.viewTitle");
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		usernameField = new TextField();
		usernameField.setCaption($("UserCredentialView.username"));
		usernameField.setRequired(true);
		usernameField.setNullRepresentation("");
		usernameField.setId("username");
		usernameField.addStyleName("username");
		usernameField.setEnabled(addActionMode && presenter.canAndOrModify(userCredentialVO.getUsername()));
		usernameField.addBlurListener(new BlurListener() {
			@Override
			public void blur(BlurEvent event) {
				presenter.validateUsername(usernameField.getValue());
			}
		});

		firstNameField = new TextField();
		firstNameField.setCaption($("UserCredentialView.firstName"));
		firstNameField.setRequired(true);
		firstNameField.setNullRepresentation("");
		firstNameField.setId("firstName");
		firstNameField.addStyleName("firstName");
		firstNameField.setEnabled(presenter.canAndOrModify(userCredentialVO.getUsername())
								  && (addActionMode || userCredentialVO.getSyncMode() == UserSyncMode.LOCALLY_CREATED));

		lastNameField = new TextField();
		lastNameField.setCaption($("UserCredentialView.lastName"));
		lastNameField.setRequired(true);
		lastNameField.setNullRepresentation("");
		lastNameField.setId("lastName");
		lastNameField.addStyleName("lastName");
		lastNameField.setEnabled(presenter.canAndOrModify(userCredentialVO.getUsername())
								 && (addActionMode || userCredentialVO.getSyncMode() == UserSyncMode.LOCALLY_CREATED));

		emailField = new TextField();
		emailField.setCaption($("UserCredentialView.email"));
		emailField.setRequired(true);
		emailField.setNullRepresentation("");
		emailField.setId("email");
		emailField.addStyleName("email");
		emailField.addValidator(new EmailValidator($("AddEditUserCredentialView.invalidEmail")));
		emailField.setEnabled(presenter.canAndOrModify(userCredentialVO.getUsername())
							  && (addActionMode || userCredentialVO.getSyncMode() == UserSyncMode.LOCALLY_CREATED));

		jobTitle = new TextField();
		jobTitle.setCaption($("UserCredentialView.jobTitle"));
		jobTitle.setNullRepresentation("");
		jobTitle.setId("jobTitle");
		jobTitle.addStyleName("jobTitle");
		jobTitle.setEnabled(presenter.canAndOrModify(userCredentialVO.getUsername()));

		phone = new TextField();
		phone.setCaption($("UserCredentialView.phone"));
		phone.setNullRepresentation("");
		phone.setId("phone");
		phone.addStyleName("phone");
		phone.setEnabled(presenter.canAndOrModify(userCredentialVO.getUsername()));

		fax = new TextField();
		fax.setCaption($("UserCredentialView.fax"));
		fax.setNullRepresentation("");
		fax.setId("fax");
		fax.addStyleName("fax");
		fax.setEnabled(presenter.canAndOrModify(userCredentialVO.getUsername()));

		address = new TextField();
		address.setCaption($("UserCredentialView.address"));
		address.setNullRepresentation("");
		address.setId("address");
		address.addStyleName("address");
		address.setEnabled(presenter.canAndOrModify(userCredentialVO.getUsername()));

		personalEmailsField = new TextArea();
		personalEmailsField.setCaption($("UserCredentialView.personalEmails"));
		personalEmailsField.setRequired(false);
		personalEmailsField.setNullRepresentation("");
		personalEmailsField.setId("personalEmails");
		personalEmailsField.addStyleName("email");
		personalEmailsField.setEnabled(presenter.canAndOrModify(userCredentialVO.getUsername()));
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

		passwordField = new EditablePasswordField();
		passwordField.setCaption($("UserCredentialView.password"));
		passwordField.setRequired(addActionMode);
		passwordField.setId("password");
		passwordField.addStyleName("password");
		if (addActionMode) {
			passwordField.setVisible(!presenter.isLDAPAuthentication());
			passwordField.setEnabled(!presenter.isLDAPAuthentication());
			passwordField.setRequired(!presenter.isLDAPAuthentication());
		} else {
			passwordField.setEnabled(presenter.canModifyPassword(userCredentialVO.getUsername())
									 && userCredentialVO.getSyncMode() != UserSyncMode.SYNCED);
			passwordField.setVisible(presenter.canModifyPassword(userCredentialVO.getUsername())
									 && userCredentialVO.getSyncMode() != UserSyncMode.SYNCED);
		}

		confirmPasswordField = new EditablePasswordField();
		confirmPasswordField.setCaption($("UserCredentialView.confirmPassword"));
		confirmPasswordField.setRequired(addActionMode);
		confirmPasswordField.setId("confirmPassword");
		confirmPasswordField.addStyleName("confirmPassword");
		Validator passwordFieldsValidator = new Validator() {
			@Override
			public void validate(Object value)
					throws InvalidValueException {
				if (passwordField.getValue() != null && !passwordField.getValue().equals(value)) {
					confirmPasswordField.focus();
					throw new InvalidValueException($("AddEditUserCredentialView.passwordsFieldsMustBeEquals"));
				}
			}
		};
		confirmPasswordField.addValidator(passwordFieldsValidator);
		if (addActionMode) {
			confirmPasswordField.setVisible(!presenter.isLDAPAuthentication());
			confirmPasswordField.setEnabled(!presenter.isLDAPAuthentication());
			confirmPasswordField.setRequired(!presenter.isLDAPAuthentication());
		} else {
			confirmPasswordField.setEnabled(presenter.canModifyPassword(userCredentialVO.getUsername())
											&& userCredentialVO.getSyncMode() != UserSyncMode.SYNCED);
			confirmPasswordField.setVisible(presenter.canModifyPassword(userCredentialVO.getUsername())
											&& userCredentialVO.getSyncMode() != UserSyncMode.SYNCED);
		}

		collectionsField = new OptionGroup($("UserCredentialView.collections"));
		collectionsField.addStyleName("collections");
		collectionsField.addStyleName("collections-username");
		collectionsField.setId("collections");
		collectionsField.setMultiSelect(true);
		for (String collection : presenter.getAllCollections()) {
			collectionsField.addItem(collection);
			collectionsField.setItemCaption(collection, presenter.getCollectionTitle(collection));
			if (userCredentialVO.getCollections() != null && userCredentialVO.getCollections().contains(collection)) {
				collectionsField.select(collection);
			}
		}

		statusField = new OptionGroup($("UserCredentialView.status"));
		statusField.addStyleName("status");
		statusField.setId("status");
		for (UserCredentialStatus status : UserCredentialStatus.values()) {
			statusField.addItem(status);
			statusField.setItemCaption(status, $("UserCredentialView.status." + status.getCode()));
		}
		statusField.setEnabled(presenter.canAndOrModify(userCredentialVO.getUsername())
							   && userCredentialVO.getSyncMode() != UserSyncMode.SYNCED);

		// Allow to modify user collection and nothing else when ldapsynch.
		boolean isEnabled = presenter.userNotLDAPSynced(userCredentialVO.getUsername());
		if (!isEnabled) {
			usernameField.setRequired(false);
			firstNameField.setRequired(false);
			lastNameField.setRequired(false);
			emailField.setRequired(false);
			jobTitle.setRequired(false);
			phone.setRequired(false);
			fax.setRequired(false);
			address.setRequired(false);
			personalEmailsField.setRequired(false);
			passwordField.setRequired(false);
			confirmPasswordField.setRequired(false);
			statusField.setRequired(false);
		}

		usernameField.setEnabled(isEnabled);
		firstNameField.setEnabled(isEnabled && (addActionMode || userCredentialVO.getSyncMode() == UserSyncMode.LOCALLY_CREATED));
		lastNameField.setEnabled(isEnabled && (addActionMode || userCredentialVO.getSyncMode() == UserSyncMode.LOCALLY_CREATED));
		emailField.setEnabled(isEnabled && (addActionMode || userCredentialVO.getSyncMode() == UserSyncMode.LOCALLY_CREATED));
		jobTitle.setEnabled(isEnabled);
		phone.setEnabled(isEnabled);
		fax.setEnabled(isEnabled);
		address.setEnabled(isEnabled);
		personalEmailsField.setEnabled(isEnabled);
		passwordField.setEnabled(isEnabled && userCredentialVO.getSyncMode() != UserSyncMode.SYNCED);
		confirmPasswordField.setEnabled(isEnabled && userCredentialVO.getSyncMode() != UserSyncMode.SYNCED);
		collectionsField.setEnabled(true);
		statusField.setEnabled(isEnabled && userCredentialVO.getSyncMode() != UserSyncMode.SYNCED);
		confirmPasswordField.setReadOnly(!confirmPasswordField.isEnabled() || !presenter.isPasswordChangeEnabled());
		passwordField.setReadOnly(!passwordField.isEnabled() || !presenter.isPasswordChangeEnabled());

		return new BaseForm<UserCredentialVO>(userCredentialVO, this, usernameField, firstNameField,
				lastNameField, emailField, jobTitle, phone, fax, address, personalEmailsField, passwordField,
				confirmPasswordField, collectionsField, statusField) {
			@Override
			protected void saveButtonClick(UserCredentialVO userCredentialVO)
					throws ValidationException {
				presenter.saveButtonClicked(userCredentialVO);
			}

			@Override
			protected void cancelButtonClick(UserCredentialVO userCredentialVO) {
				presenter.cancelButtonClicked();
			}
		};
	}

	public void resetUsername() {
		usernameField.setValue(null);
	}
}

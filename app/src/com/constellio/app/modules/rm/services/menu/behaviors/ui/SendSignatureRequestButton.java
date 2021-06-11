package com.constellio.app.modules.rm.services.menu.behaviors.ui;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.SignatureExternalAccessServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.constellio.app.ui.framework.components.fields.date.JodaDateField;
import com.constellio.app.ui.framework.components.fields.lookup.LookupRecordField;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.model.entities.records.wrappers.User;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.constellio.app.ui.i18n.i18n.$;

public class SendSignatureRequestButton extends WindowButton {

	private User requester;
	private Document document;
	private BaseView view;

	private RMSchemasRecordsServices rm;
	private SignatureExternalAccessServices signatureServices;

	private VerticalLayout userLayout;
	private OptionGroup userOptionGroup;
	private JodaDateField dateField;
	private BaseButton sendButton;

	private BaseTextField externalUserNameField;
	private BaseTextField externalUserMailField;
	private LookupRecordField internalUserField;

	private enum UserType {
		Internal,
		External
	}

	public SendSignatureRequestButton(AppLayerFactory appLayerFactory, String collection, User requester,
									  Document document,
									  BaseView view) {
		super($("DocumentMenuItemActionBehaviors.sendSignatureRequest"),
				$("DocumentMenuItemActionBehaviors.sendSignatureRequest"),
				WindowButton.WindowConfiguration.modalDialog("570px", "400px"));

		this.requester = requester;
		this.document = document;
		this.view = view;

		rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		signatureServices = new SignatureExternalAccessServices(collection, appLayerFactory);
	}

	@Override
	protected Component buildWindowContent() {
		VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setHeight("100%");
		mainLayout.setMargin(new MarginInfo(true));
		mainLayout.setSpacing(true);

		List<UserType> userOptions = new ArrayList<>();
		userOptions.add(UserType.Internal);
		userOptions.add(UserType.External);
		userOptionGroup = new OptionGroup($("DocumentMenuItemActionBehaviors.userType"), userOptions);
		userOptionGroup.addStyleName("horizontal");
		userOptionGroup.setItemCaption(UserType.Internal, $("DocumentMenuItemActionBehaviors.internalUser"));
		userOptionGroup.setItemCaption(UserType.External, $("DocumentMenuItemActionBehaviors.externalUser"));
		userOptionGroup.select(UserType.Internal);
		if (signatureServices.isDisableExternalSignatures()) {
			userOptionGroup.setEnabled(false);
		}
		userOptionGroup.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				refreshUserLayout((UserType) event.getProperty().getValue());
			}
		});
		mainLayout.addComponent(userOptionGroup);

		userLayout = new VerticalLayout();
		mainLayout.addComponent(userLayout);

		dateField = new JodaDateField();
		dateField.setCaption($("DocumentMenuItemActionBehaviors.expirationDate"));
		dateField.setRequired(true);
		mainLayout.addComponent(dateField);

		HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.setSpacing(true);
		buttonLayout.setWidth("100%");
		buttonLayout.setHeight("100%");
		mainLayout.addComponent(buttonLayout);
		mainLayout.setExpandRatio(buttonLayout, 1);

		sendButton = new BaseButton($("LabelsButton.send")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				send();
			}
		};
		sendButton.addStyleName(ValoTheme.BUTTON_PRIMARY);
		buttonLayout.addComponent(sendButton);
		buttonLayout.setComponentAlignment(sendButton, Alignment.BOTTOM_CENTER);

		refreshUserLayout((UserType) userOptionGroup.getValue());

		return mainLayout;
	}

	private void refreshUserLayout(UserType type) {
		userLayout.removeAllComponents();

		boolean isInternal = type.equals(UserType.Internal);
		if (isInternal) {
			userLayout.addComponent(buildInternalUserLayout());
		} else {
			userLayout.addComponent(buildExternalUserLayout());
		}
	}

	private Component buildInternalUserLayout() {
		internalUserField = new LookupRecordField(User.SCHEMA_TYPE);
		internalUserField.setCaption($("DocumentMenuItemActionBehaviors.user"));
		internalUserField.setRequired(true);
		internalUserField.addStyleName("user-lookup");
		return internalUserField;
	}

	private Component buildExternalUserLayout() {
		VerticalLayout layout = new VerticalLayout();
		layout.setSpacing(true);

		externalUserNameField = new BaseTextField();
		externalUserNameField.setCaption($("DocumentMenuItemActionBehaviors.externalUserFullname"));
		externalUserNameField.setRequired(true);
		externalUserNameField.setWidth("100%");
		layout.addComponent(externalUserNameField);

		externalUserMailField = new BaseTextField();
		externalUserMailField.setCaption($("DocumentMenuItemActionBehaviors.externalUserEmail"));
		externalUserMailField.setRequired(true);
		externalUserMailField.setWidth("100%");
		layout.addComponent(externalUserMailField);

		return layout;
	}

	private void send() {
		boolean isInternal = ((UserType) userOptionGroup.getValue()).equals(UserType.Internal);
		if (isInternal) {
			validateInternalRequest();
		} else {
			validateExternalRequest();
		}
	}

	private void validateInternalRequest() {
		if (internalUserField.getValue() == null) {
			internalUserField.setRequiredError($("requiredField"));
		} else if (dateField.getConvertedValue() == null) {
			dateField.setRequiredError($("requiredField"));
		} else {
			String userId = (String) internalUserField.getValue();
			User internalUser = rm.getUser(userId);
			sendRequest(userId, internalUser.getTitle(), internalUser.getEmail());
		}
	}

	private void validateExternalRequest() {
		if (externalUserNameField.getValue() == null) {
			externalUserNameField.setRequiredError($("requiredField"));
		} else if (externalUserMailField.getValue() == null) {
			externalUserMailField.setRequiredError($("requiredField"));
		} else if (dateField.getConvertedValue() == null) {
			dateField.setRequiredError($("requiredField"));
		} else {
			sendRequest(null, externalUserNameField.getValue(), externalUserMailField.getValue());
		}
	}

	private void sendRequest(String internalUserId, String userName, String userMail) {
		Locale locale = view.getSessionContext().getCurrentLocale();
		try {
			signatureServices.sendSignatureRequest(document.getId(), internalUserId, userName, userMail,
					(LocalDate) dateField.getConvertedValue(), locale.getLanguage(), requester);
			view.showMessage($("EmailServerConfigView.results.success"));
		} catch (Exception e) {
			view.showErrorMessage($("DocumentMenuItemActionBehaviors.errorGeneratingAccess"));
		} finally {
			getWindow().close();
		}
	}
}
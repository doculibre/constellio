package com.constellio.app.ui.framework.components.viewers.pdftron;

import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.components.fields.BasePasswordField;
import com.constellio.app.ui.framework.components.layouts.I18NHorizontalLayout;
import com.constellio.app.ui.handlers.OnEnterKeyHandler;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.security.authentification.AuthenticationService;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import static com.constellio.app.ui.i18n.i18n.$;

public class PdfTronSignatureAuthenticationWindowButton extends WindowButton {

	private AuthenticationService authenticationService;
	private User user;

	private Label errorLabel;
	private Button signInButton;

	public PdfTronSignatureAuthenticationWindowButton(ModelLayerFactory modelLayerFactory, User user) {
		super($("pdfTronViewer.finalize"), $("pdfTronViewer.authentication"),
				new WindowConfiguration(true, true, "600px", "180px"));

		authenticationService = modelLayerFactory.newAuthenticationService();
		this.user = user;
	}

	@Override
	protected Component buildWindowContent() {
		VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setSpacing(true);

		Label infoLabel = new Label($("pdfTronViewer.authenticationInfo"));

		errorLabel = new Label($("LoginView.badLoginMessage"));
		errorLabel.addStyleName("error-label");
		errorLabel.setVisible(false);

		Component fields = buildFields();

		mainLayout.addComponent(infoLabel);
		mainLayout.addComponent(fields);
		mainLayout.setComponentAlignment(fields, Alignment.MIDDLE_CENTER);
		mainLayout.addComponent(errorLabel);

		return mainLayout;
	}

	private Component buildFields() {
		HorizontalLayout fieldsLayout = new I18NHorizontalLayout();
		fieldsLayout.setSpacing(true);
		fieldsLayout.addStyleName("fields");

		TextField usernameField = new TextField($("LoginView.username"));
		usernameField.setIcon(FontAwesome.USER);
		usernameField.setMaxLength(100);
		usernameField.addStyleName(ValoTheme.TEXTFIELD_INLINE_ICON);
		usernameField.setValue(user.getUsername());
		usernameField.setEnabled(false);

		BasePasswordField passwordField = new BasePasswordField($("LoginView.password"));
		passwordField.setIcon(FontAwesome.LOCK);
		passwordField.setMaxLength(100);
		passwordField.addStyleName(ValoTheme.TEXTFIELD_INLINE_ICON);
		passwordField.focus();
		passwordField.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				errorLabel.setVisible(false);
			}
		});

		signInButton = new Button($("pdfTronViewer.finalize"));
		signInButton.addStyleName(ValoTheme.BUTTON_PRIMARY);
		signInButton.setClickShortcut(KeyCode.ENTER);

		fieldsLayout.addComponents(usernameField, passwordField, signInButton);
		fieldsLayout.setComponentAlignment(signInButton, Alignment.BOTTOM_LEFT);

		signInButton.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(final ClickEvent event) {
				attemptSignIn(passwordField.getValue());
			}
		});

		OnEnterKeyHandler onEnterHandler = new OnEnterKeyHandler() {
			@Override
			public void onEnterKeyPressed() {
				attemptSignIn(passwordField.getValue());
			}
		};
		onEnterHandler.installOn(usernameField);
		onEnterHandler.installOn(passwordField);

		return fieldsLayout;
	}

	private void attemptSignIn(String password) {
		if (authenticationService.authenticate(user.getUsername(), password)) {
			signInButton.setEnabled(false);
			getWindow().setClosable(false);
			onAuthenticated();
		} else {
			errorLabel.setVisible(true);
		}
	}

	public void onAuthenticated() {
	}
}

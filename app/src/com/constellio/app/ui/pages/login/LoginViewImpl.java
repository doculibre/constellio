package com.constellio.app.ui.pages.login;

import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.components.fields.BasePasswordField;
import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.constellio.app.ui.framework.components.layouts.I18NHorizontalLayout;
import com.constellio.app.ui.framework.components.viewers.document.DocumentViewer;
import com.constellio.app.ui.handlers.OnEnterKeyHandler;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.pages.base.LogoUtils;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.users.UserServices;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Responsive;
import com.vaadin.server.VaadinService;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

import javax.servlet.http.Cookie;

import static com.constellio.app.ui.i18n.i18n.$;

@SuppressWarnings("serial")
public class LoginViewImpl extends BaseViewImpl implements LoginView {

	private static final String USERNAME_COOKIE = "Constellio.username";

	private String initialUsername;

	private VerticalLayout loginFormLayout;

	private CssLayout labelsLayout;
	private Label welcomeLabel;

	private HorizontalLayout fieldsLayout;
	private TextField usernameField;
	private BasePasswordField passwordField;
	private CheckBox rememberMeField;
	private Button signInButton;

	private LoginPresenter presenter;

	public LoginViewImpl() {
		presenter = new LoginPresenter(this);

		setSizeFull();

		loginFormLayout = buildLoginForm();
		addComponent(loginFormLayout);
		setComponentAlignment(loginFormLayout, Alignment.MIDDLE_CENTER);
	}

	private VerticalLayout buildLoginForm() {
		VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setSizeUndefined();
		mainLayout.setSpacing(true);
		Responsive.makeResponsive(mainLayout);
		mainLayout.addStyleName("login-panel");

		labelsLayout = buildLabels();
		fieldsLayout = buildFields();
		rememberMeField = new CheckBox($("LoginView.rememberMe"), true);

		mainLayout.addComponent(labelsLayout);
		mainLayout.addComponent(fieldsLayout);
		mainLayout.addComponent(rememberMeField);
		return mainLayout;
	}

	private CssLayout buildLabels() {
		CssLayout labelsLayout = new CssLayout();
		labelsLayout.addStyleName("labels");
		HorizontalLayout hLayout = new HorizontalLayout();
		hLayout.setSizeFull();

		welcomeLabel = new Label($("LoginView.welcome"));
		welcomeLabel.setSizeUndefined();
		welcomeLabel.addStyleName(ValoTheme.LABEL_H2);
		welcomeLabel.addStyleName(ValoTheme.LABEL_COLORED);
		//		hLayout.addComponent(welcomeLabel);

		String linkTarget = presenter.getLogoTarget();
		Link logo = new Link(null, new ExternalResource(linkTarget));
		ModelLayerFactory modelLayerFactory = getConstellioFactories().getModelLayerFactory();
		logo.setIcon(LogoUtils.getAuthentificationImageResource(modelLayerFactory));
		logo.addStyleName("login-logo");
		logo.setSizeUndefined();
		hLayout.addComponent(logo);
		labelsLayout.addComponent(hLayout);

		return labelsLayout;
	}

	private HorizontalLayout buildFields() {
		HorizontalLayout fieldsLayout = new I18NHorizontalLayout();
		fieldsLayout.setSpacing(true);
		fieldsLayout.addStyleName("fields");

		usernameField = new BaseTextField($("LoginView.username"));
		usernameField.setIcon(FontAwesome.USER);
		usernameField.setMaxLength(100);
		usernameField.addStyleName(ValoTheme.TEXTFIELD_INLINE_ICON);
		if (initialUsername != null) {
			usernameField.setValue(initialUsername);
		} else {
			usernameField.focus();
		}

		passwordField = new BasePasswordField($("LoginView.password"));
		passwordField.setIcon(FontAwesome.LOCK);
		passwordField.setMaxLength(100);
		passwordField.addStyleName(ValoTheme.TEXTFIELD_INLINE_ICON);
		if (initialUsername != null) {
			passwordField.focus();
		}

		signInButton = new Button($("LoginView.signIn"));
		signInButton.addStyleName(ValoTheme.BUTTON_PRIMARY);
		signInButton.setClickShortcut(KeyCode.ENTER);

		fieldsLayout.addComponents(usernameField, passwordField, signInButton);
		fieldsLayout.setComponentAlignment(signInButton, Alignment.BOTTOM_LEFT);

		signInButton.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(final ClickEvent event) {
				attemptSignIn();
			}
		});

		OnEnterKeyHandler onEnterHandler = new OnEnterKeyHandler() {
			@Override
			public void onEnterKeyPressed() {
				attemptSignIn();
			}
		};
		onEnterHandler.installOn(usernameField);
		onEnterHandler.installOn(passwordField);

		return fieldsLayout;
	}

	private void attemptSignIn() {
		presenter.signInAttempt(usernameField.getValue(), passwordField.getValue(), rememberMeField.getValue());
	}

	@Override
	public void updateUIContent() {
		ConstellioUI.getCurrent().updateContent();
	}

	@Override
	public SessionContext getSessionContext() {
		return ConstellioUI.getCurrentSessionContext();
	}

	@Override
	protected Component buildMainComponent(ViewChangeListener.ViewChangeEvent event) {
		return loginFormLayout;
	}

	@Override
	public void showUserHasNoCollectionMessage() {
		Notification.show($("LoginView.userHasNoCollection"));
	}

	@Override
	public void showSystemCurrentlyReindexing() {
		Notification.show($("LoginView.systemCurrentlyReindexing"));
	}

	@Override
	public void showBadLoginMessage() {
		Notification.show($("LoginView.badLoginMessage"));
	}

	@Override
	public void setUsername(String username) {
		if (usernameField != null) {
			usernameField.setValue(username);
		} else {
			initialUsername = username;
		}
	}

	@Override
	public String getUsernameCookieValue() {
		String usernameCookieValue = null;
		Cookie[] cookies = VaadinService.getCurrentRequest().getCookies();
		for (Cookie cookie : cookies) {
			if (cookie.getName().equals(USERNAME_COOKIE)) {
				usernameCookieValue = cookie.getValue();
				break;
			}
		}
		return usernameCookieValue;
	}

	@Override
	public void setUsernameCookie(String username) {
		Cookie usernameCookie = new Cookie(USERNAME_COOKIE, username);
		if (username != null) {
			// Make cookie expire in 2 minutes
			usernameCookie.setMaxAge(Integer.MAX_VALUE);
		} else {
			// Delete the cookie
			usernameCookie.setMaxAge(0);
		}

		// Set the cookie path.
		usernameCookie.setPath(VaadinService.getCurrentRequest().getContextPath());

		// Save cookie
		VaadinService.getCurrentResponse().addCookie(usernameCookie);
	}

	@Override
	public void popPrivacyPolicyWindow(final ModelLayerFactory modelLayerFactory, final User userInLastCollection,
									   final String lastCollection, Runnable signInStepFinishedCallback) {
		final Window window = new Window();
		window.setWidth("90%");
		window.setHeight("90%");
		window.setModal(true);
		window.setCaption($("LoginView.privacyPolicyWindow"));

		VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setSpacing(true);
		mainLayout.setHeight("100%");

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
				userServices.execute(userServices.addUpdate(userInLastCollection.getUsername())
						.setHasAgreedToPrivacyPolicy(true));

				if (signInStepFinishedCallback != null) {
					signInStepFinishedCallback.run();
				} else {
					presenter.signInValidated(userInLastCollection, lastCollection);
				}

				window.close();
			}
		};
		acceptButton.addStyleName(ValoTheme.BUTTON_PRIMARY);

		DocumentViewer documentViewer = new DocumentViewer(presenter.getPrivacyPolicyFile());
		documentViewer.setHeight("100%");

		buttonLayout.addComponents(acceptButton, cancelButton);

		mainLayout.addComponents(documentViewer, buttonLayout);
		mainLayout.setComponentAlignment(buttonLayout, Alignment.BOTTOM_CENTER);
		mainLayout.setExpandRatio(documentViewer, 1.0f);

		window.setContent(mainLayout);
		window.center();
		ConstellioUI.getCurrent().addWindow(window);
	}

	@Override
	public void popLastAlertWindow(final ModelLayerFactory modelLayerFactory, final User userInLastCollection,
								   final String lastCollection, Runnable signInStepFinishedCallback) {
		final Window window = new Window();
		window.setWidth("90%");
		window.setHeight("90%");
		window.setModal(true);
		window.setCaption($("LoginView.lastAlertWindow"));

		VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setSpacing(true);
		mainLayout.setHeight("100%");

		DocumentViewer documentViewer = new DocumentViewer(presenter.getLastAlertFile());
		documentViewer.setHeight("100%");

		HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.setSpacing(true);
		buttonLayout.setHeight("50px");

		BaseButton continueButton = new BaseButton($("continue")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				UserServices userServices = modelLayerFactory.newUserServices();
				userServices.execute(userServices.addUpdate(userInLastCollection.getUsername())
						.setHasReadLastAlert(true));

				if (signInStepFinishedCallback != null) {
					signInStepFinishedCallback.run();
				} else {
					presenter.signInValidated(userInLastCollection, lastCollection);
				}

				window.close();
			}
		};
		continueButton.addStyleName(ValoTheme.BUTTON_PRIMARY);
		buttonLayout.addComponent(continueButton);

		mainLayout.addComponents(documentViewer, buttonLayout);
		mainLayout.setExpandRatio(documentViewer, 9);
		mainLayout.setExpandRatio(buttonLayout, 1);
		mainLayout.setComponentAlignment(buttonLayout, Alignment.BOTTOM_CENTER);

		window.setContent(mainLayout);
		window.center();
		ConstellioUI.getCurrent().addWindow(window);
	}

	@Override
	public void popShowMessageToUserWindow(ModelLayerFactory modelLayerFactory, User userInLastCollection,
										   String lastCollection, Runnable signInStepFinishedCallback) {
		final Window window = new Window();
		window.setWidth("90%");
		window.setHeight("90%");
		window.setModal(true);
		window.setCaption($("LoginView.messageToUserWindow"));

		VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setSpacing(true);
		mainLayout.setWidth("100%");
		mainLayout.setHeight("100%");

		window.addCloseListener(event -> {
			UserServices userServices = modelLayerFactory.newUserServices();
			userServices.execute(userServices.addUpdate(userInLastCollection.getUsername())
					.setHasSeenLatestMessageAtLogin(true));

			if (signInStepFinishedCallback != null) {
				signInStepFinishedCallback.run();
			} else {
				presenter.signInValidated(userInLastCollection, lastCollection);
			}
		});


		HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.setHeight("50px");

		BaseButton acceptButton = new BaseButton($("Ok")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				window.close();
			}
		};
		acceptButton.addStyleName(ValoTheme.BUTTON_PRIMARY);
		buttonLayout.addComponent(acceptButton);

		DocumentViewer documentViewer = new DocumentViewer(presenter.getMessageToShowAtLoginFile());
		documentViewer.setHeight("100%");

		mainLayout.addComponents(documentViewer, buttonLayout);
		mainLayout.setComponentAlignment(buttonLayout, Alignment.BOTTOM_CENTER);
		mainLayout.setExpandRatio(documentViewer, 1.0f);

		window.setContent(mainLayout);
		window.center();
		ConstellioUI.getCurrent().addWindow(window);
	}
}

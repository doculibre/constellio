package com.constellio.app.ui.application;

import com.constellio.app.modules.rm.ui.builders.UserToVOBuilder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.services.sso.SSOServices;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.entities.UserVO;
import com.constellio.app.ui.framework.components.contextmenu.RecordContextMenuHandler;
import com.constellio.app.ui.framework.components.dialogs.ConfirmDialogProperties;
import com.constellio.app.ui.framework.components.dialogs.ConfirmDialogShowerImpl;
import com.constellio.app.ui.framework.components.menuBar.RecordMenuBarHandler;
import com.constellio.app.ui.framework.components.resource.ConstellioResourceHandler;
import com.constellio.app.ui.framework.components.viewers.panel.ViewableRecordVOViewChangeListener;
import com.constellio.app.ui.framework.externals.ExternalWebSignIn.CreateExternalWebSignInCallbackURLParameters;
import com.constellio.app.ui.framework.externals.ExternalWebSignIn.ExternalWebSignInResponse;
import com.constellio.app.ui.handlers.ConstellioErrorHandler;
import com.constellio.app.ui.i18n.i18n;
import com.constellio.app.ui.pages.base.BaseUnauthenticatedViewImpl;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.pages.base.ClickableNotification;
import com.constellio.app.ui.pages.base.ConstellioHeader;
import com.constellio.app.ui.pages.base.EnterViewListener;
import com.constellio.app.ui.pages.base.HttpSessionContext;
import com.constellio.app.ui.pages.base.InitUIListener;
import com.constellio.app.ui.pages.base.MainLayout;
import com.constellio.app.ui.pages.base.MainLayoutImpl;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.base.SessionContextProvider;
import com.constellio.app.ui.pages.base.UIContext;
import com.constellio.app.ui.pages.base.VaadinSessionContext;
import com.constellio.app.ui.pages.externals.ExternalSignInSuccessViewImpl;
import com.constellio.app.ui.pages.login.LoginViewImpl;
import com.constellio.app.ui.pages.setup.ConstellioSetupViewImpl;
import com.constellio.app.ui.util.ResponsiveUtils;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.data.utils.TimeProvider;
import com.constellio.data.utils.dev.Toggle;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.users.SystemWideUserInfos;
import com.constellio.model.services.users.UserServices;
import com.google.common.base.Splitter;
import com.vaadin.annotations.Theme;
import com.vaadin.event.UIEvents.PollListener;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.Page;
import com.vaadin.server.Page.BrowserWindowResizeEvent;
import com.vaadin.server.Page.BrowserWindowResizeListener;
import com.vaadin.server.RequestHandler;
import com.vaadin.server.Responsive;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinServletService;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Component;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.TooltipConfiguration;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;
import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDateTime;
import org.vaadin.dialogs.ConfirmDialog;
import org.vaadin.dialogs.DefaultConfirmDialogFactory;

import java.lang.reflect.InvocationTargetException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.app.ui.i18n.i18n.isRightToLeft;
import static com.constellio.app.ui.pages.base.SessionContext.BYPASS_SSO;

@SuppressWarnings("serial")
@Theme("constellio")
public class ConstellioUI extends UI implements SessionContextProvider, UIContext, BrowserWindowResizeListener {

	private static final ConstellioResourceHandler CONSTELLIO_RESSOURCE_HANDLER = new ConstellioResourceHandler();

	private static final int POLL_INTERVAL = 1000;

	private SessionContext sessionContext;
	private MainLayoutImpl mainLayout;

	private List<RecordContextMenuHandler> recordContextMenuHandlers = new ArrayList<>();

	private List<RecordMenuBarHandler> recordMenuBarHandlers = new ArrayList<>();

	private Map<String, Object> uiContext = new HashMap<>();

	private SSOServices ssoServices;

	private View currentView;

	private ViewChangeEvent viewChangeEvent;

	private ViewableRecordVOViewChangeListener viewableRecordVOViewChangeListener = new ViewableRecordVOViewChangeListener();

	private boolean nested = false;
	private String serviceKeyParam = null;
	private String tokenParam = null;
	private String collectionParam = null;

	static {
		try {
			ConfirmDialog.setFactory(new ConfirmDialog.Factory() {
				@Override
				public ConfirmDialog create(String windowCaption, String message, String okTitle, String cancelTitle,
											String notOKCaption) {
					DefaultConfirmDialogFactory factory = new DefaultConfirmDialogFactory();
					ConfirmDialog confirmDialog = factory.create(windowCaption, message, okTitle, cancelTitle, notOKCaption);
					confirmDialog.addStyleName("confirm-dialog");
					confirmDialog.setContentMode(ConfirmDialog.ContentMode.HTML);
					confirmDialog.setResizable(true);
					return confirmDialog;
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean isNested() {
		return nested;
	}

	public void setNested(boolean nested) {
		this.nested = nested;
	}

	public ViewChangeEvent getViewChangeEvent() {
		return viewChangeEvent;
	}

	public void setViewChangeEvent(ViewChangeEvent viewChangeEvent) {
		this.viewChangeEvent = viewChangeEvent;
	}

	public void addRequestHandler(RequestHandler handler) {
		getSession().addRequestHandler(handler);
	}

	public void removeRequestHandler(RequestHandler handler) {
		getSession().removeRequestHandler(handler);
	}

	@Override
	protected void init(VaadinRequest request) {
		if (!getSession().getRequestHandlers().contains(CONSTELLIO_RESSOURCE_HANDLER)) {
			getSession().addRequestHandler(CONSTELLIO_RESSOURCE_HANDLER);
		}

		nested = "true".equalsIgnoreCase(request.getParameter("nestedUI"));
		serviceKeyParam = request.getParameter("serviceKey");
		tokenParam = request.getParameter("token");
		collectionParam = request.getParameter("collection");

		ssoServices = SSOServices.getInstance();

		Page.getCurrent().setTitle($("ConstellioUI.pageTitle"));

		ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
		AppLayerFactory appLayerFactory = constellioFactories.getAppLayerFactory();

		appLayerFactory.getExtensions().getSystemWideExtensions().addToConstellioUIInitialisation(this);

		List<InitUIListener> initUIListeners = appLayerFactory.getInitUIListeners();
		for (InitUIListener initUIListener : initUIListeners) {
			initUIListener.beforeInitialize(this);
		}

		addStyleName(ValoTheme.UI_WITH_MENU);
		if (isNested()) {
			addStyleName("nested-ui");
		}

		Responsive.makeResponsive(this);
		updateTooltipConfiguration();
		Page.getCurrent().addBrowserWindowResizeListener(this);

		if (getErrorHandler() == null) {
			setErrorHandler(new ConstellioErrorHandler());
		}

		if (sessionContext == null) {
			sessionContext = new VaadinSessionContext();
		}
		VaadinSession.getCurrent().setLocale(i18n.getLocale());
		sessionContext.setCurrentLocale(i18n.getLocale());

		updateContent();

		for (InitUIListener initUIListener : initUIListeners) {
			initUIListener.afterInitialize(this);
		}
	}

	private UserVO ssoAuthenticate() {
		UserVO currentUserVO;

		ConstellioFactories constellioFactories = getConstellioFactories();
		ModelLayerFactory modelLayerFactory = constellioFactories.getModelLayerFactory();
		UserServices userServices = modelLayerFactory.newUserServices();
		RecordServices recordServices = modelLayerFactory.newRecordServices();

		Principal userPrincipal = sessionContext.getUserPrincipal();
		if (userPrincipal != null) {
			String username = userPrincipal.getName();

			SystemWideUserInfos userCredential = userServices.getNullableUserInfos(username);
			if (userCredential != null && userCredential.isActiveInAnyCollection()) {
				List<String> collections = userCredential.getCollections();

				String lastCollection = null;
				User userInLastCollection = null;
				LocalDateTime lastLogin = null;

				for (String collection : collections) {
					User userInCollection = userServices.getUserInCollection(username, collection);
					if (userInLastCollection == null) {
						if (userInCollection != null) {
							lastCollection = collection;
							userInLastCollection = userInCollection;
							lastLogin = userInCollection.getLastLogin();
						}
					} else {
						if (lastLogin == null && userInCollection.getLastLogin() != null) {
							lastCollection = collection;
							userInLastCollection = userInCollection;
							lastLogin = userInCollection.getLastLogin();
						} else if (lastLogin != null && userInCollection.getLastLogin() != null && userInCollection.getLastLogin()
								.isAfter(lastLogin)) {
							lastCollection = collection;
							userInLastCollection = userInCollection;
							lastLogin = userInCollection.getLastLogin();
						}
					}
				}
				if (userInLastCollection != null) {
					try {
						recordServices.update(userInLastCollection
								.setLastLogin(TimeProvider.getLocalDateTime())
								.setLastIPAddress(sessionContext.getCurrentUserIPAddress()));
					} catch (RecordServicesException e) {
						throw new RuntimeException(e);
					}

					modelLayerFactory.newLoggingServices().login(userInLastCollection);
					currentUserVO = new UserToVOBuilder()
							.build(userInLastCollection.getWrappedRecord(), VIEW_MODE.DISPLAY, sessionContext);
					sessionContext.setCurrentUser(currentUserVO);
					sessionContext.setCurrentCollection(lastCollection);
					sessionContext.setForcedSignOut(false);
				} else {
					currentUserVO = null;
				}
			} else {
				currentUserVO = null;
			}
		} else {
			currentUserVO = null;
		}
		return currentUserVO;
	}

	public void updateContent() {
		if (isRightToLeft()) {
			addStyleName("right-to-left");
		} else {
			removeStyleName("right-to-left");
		}

		if (isSetupRequired()) {
			ConstellioSetupViewImpl setupView = new ConstellioSetupViewImpl(VaadinService.getCurrentRequest().getPathInfo());
			setContent(setupView);
			addStyleName("setupview");
		} else if (isUnauthenticatedView() && Toggle.EXTERNAL_UPLOAD_LINK.isEnabled()) {
			Class<? extends View> viewClass = getViewClassForCurrentPage();

			removeStyleName("setupview");
			try {
				Map<String, String> params = Splitter.on('&').withKeyValueSeparator('=').split(Page.getCurrent().getLocation().getQuery());

				BaseUnauthenticatedViewImpl view = (BaseUnauthenticatedViewImpl) viewClass.newInstance();
				view.forParams(params);
				if (view.hasAccess()) {
					view.enter(null);
					setContent(view);
				} else {
					goToLoginPage();
				}
			} catch (Exception e) {
				e.printStackTrace();
				goToLoginPage();
			}
		} else {
			ConstellioFactories constellioFactories = getConstellioFactories();
			AppLayerFactory appLayerFactory = constellioFactories.getAppLayerFactory();
			UserServices userServices = appLayerFactory.getModelLayerFactory().newUserServices();

			UserVO currentUserVO = sessionContext.getCurrentUser();
			if (StringUtils.isNotBlank(tokenParam) && StringUtils.isNotBlank(serviceKeyParam)
				&& userServices.isAuthenticated(serviceKeyParam, tokenParam)) {
				String username = userServices.getUserCredentialByServiceKey(serviceKeyParam);
				try {
					User userInCollection = userServices.getUserInCollection(username, collectionParam);
					currentUserVO = new UserToVOBuilder().build(userInCollection.getWrappedRecord(), VIEW_MODE.DISPLAY, sessionContext);
					sessionContext.setCurrentUser(currentUserVO);
					sessionContext.setCurrentCollection(collectionParam);

					Locale userLanguage = getSessionLanguage(userInCollection);
					sessionContext.setCurrentLocale(userLanguage);
					i18n.setLocale(userLanguage);

					if (ssoServices.isEnabled()) {
						sessionContext.setAttribute(BYPASS_SSO, true);
					}
				} catch (Exception e) {
					//SKIP
				}
			} else if (currentUserVO == null && ssoServices.isEnabled() && !sessionContext.isForcedSignOut()) {
				currentUserVO = ssoAuthenticate();
			}

			if (currentUserVO != null) {
				// Authenticated user
				// FIXME Using actual HTTP session so that this mechanic can work even when a FakeSessionContext is used 
				HttpSessionContext httpSessionContext = new HttpSessionContext(ConstellioVaadinServlet.getCurrentHttpServletRequest());
				ExternalWebSignInResponse externalWebSignInResponse = httpSessionContext.getExternalWebSignInResponse();
				if (externalWebSignInResponse != null) {
					String externalWebSignCallback = externalWebSignInResponse
							.createCallbackURL(new CreateExternalWebSignInCallbackURLParameters(
									httpSessionContext,
									VaadinServletService.getCurrentServletRequest(),
									currentUserVO.getUsername()));

					if (externalWebSignCallback != null) {
						httpSessionContext.setExternalWebSignInResponse(null);
						Page.getCurrent().open(externalWebSignCallback, null);
						setContent(new ExternalSignInSuccessViewImpl());
					}
				} else {
					mainLayout = new MainLayoutImpl(appLayerFactory);
					//				if (isRightToLeft()) {
					//					mainLayout.addStyleName("right-to-left");
					//				}
					setContent(mainLayout);
				}

				Navigator navigator = getNavigator();
				navigator.addViewChangeListener(new ViewChangeListener() {
					@Override
					public boolean beforeViewChange(ViewChangeEvent event) {
						ConstellioFactories.getInstance().onRequestEnded();
						ConstellioFactories.getInstance().onRequestStarted();
						return true;
					}

					@Override
					public void afterViewChange(ViewChangeEvent event) {
						ConstellioUI.getCurrent().setViewChangeEvent(event);
						View oldView = event.getOldView();
						if (oldView instanceof PollListener) {
							removePollListener((PollListener) oldView);
						}

						View newView = event.getNewView();
						if (newView instanceof BaseViewImpl) {
							if (((BaseViewImpl) newView).isBackgroundViewMonitor()) {
								// Important to allow update of components in current UI from another Thread
								setPollInterval(POLL_INTERVAL);
							}
						} else if (newView instanceof PollListener) {
							// Important to allow update of components in current UI from another Thread
							setPollInterval(POLL_INTERVAL);
						} else {
							boolean pollListenerWindow = false;
							for (Window window : getWindows()) {
								if (window instanceof PollListener) {
									pollListenerWindow = true;
									break;
								}
							}
							if (!pollListenerWindow) {
								setPollInterval(-1);
							}
						}
						ConstellioUI.this.currentView = newView;

						ConstellioFactories constellioFactories = getConstellioFactories();
						AppLayerFactory appLayerFactory = constellioFactories.getAppLayerFactory();
						List<EnterViewListener> enterViewListeners = appLayerFactory.getEnterViewListeners();
						for (EnterViewListener enterViewListener : enterViewListeners) {
							enterViewListener.enterView(newView);
						}
					}
				});

				removeStyleName("setupview");
				removeStyleName("loginview");
				//				removeStyleName("right-to-left");

				navigator.addViewChangeListener(viewableRecordVOViewChangeListener);

				navigator.navigateTo(navigator.getState());
			} else {
				goToLoginPage();
			}
		}
	}

	private Locale getSessionLanguage(User userInLastCollection) {
		String userPreferredLanguage = userInLastCollection.getLoginLanguageCode();
		String systemLanguage = getConstellioFactories().getModelLayerFactory().getConfiguration().getMainDataLanguage();
		if (org.apache.commons.lang3.StringUtils.isBlank(userPreferredLanguage)) {
			return getLocale(systemLanguage);
		} else {
			List<String> collectionLanguages = getConstellioFactories().getModelLayerFactory().getCollectionsListManager()
					.getCollectionLanguages(userInLastCollection.getCollection());
			if (collectionLanguages == null || collectionLanguages.isEmpty() || !collectionLanguages
					.contains(userPreferredLanguage)) {
				return getLocale(systemLanguage);
			} else {
				return getLocale(userPreferredLanguage);
			}
		}
	}

	private Locale getLocale(String languageCode) {
		for (Language language : Language.values()) {
			if (language.getCode().equals(languageCode)) {
				return new Locale(languageCode);
			}
		}
		throw new ImpossibleRuntimeException("Invalid language " + languageCode);
	}

	private void goToLoginPage() {
		Navigator navigator = getNavigator();
		if (navigator != null) {
			navigator.removeViewChangeListener(viewableRecordVOViewChangeListener);
		}

		removeStyleName("setupview");
		LoginViewImpl loginView = new LoginViewImpl();
		setContent(loginView);
		addStyleName("loginview");
	}

	private boolean isUnauthenticatedView() {
		Class<? extends View> view = getViewClassForCurrentPage();
		return view != null && BaseUnauthenticatedViewImpl.class.isAssignableFrom(view);
	}

	private Class<? extends View> getViewClassForCurrentPage() {
		ConstellioFactories constellioFactories = getConstellioFactories();
		AppLayerFactory appLayerFactory = constellioFactories.getAppLayerFactory();
		NavigatorConfigurationService navigatorService = appLayerFactory.getNavigatorConfigurationService();
		return navigatorService.getClassForView(Page.getCurrent().getLocation().getPath().replace("/constellio/", ""));
	}

	@Override
	public void addWindow(Window window) throws IllegalArgumentException, NullPointerException {
		if (window instanceof PollListener) {
			setPollInterval(POLL_INTERVAL);
		}
		super.addWindow(window);
	}

	private boolean isSetupRequired() {
		ConstellioFactories constellioFactories = getConstellioFactories();
		return constellioFactories.getAppLayerFactory().getCollectionsManager().getCollectionCodesExcludingSystem().isEmpty()
				&& constellioFactories.getAppLayerFactory().isInitializationFinished();
	}

	public ConstellioHeader getHeader() {
		return mainLayout != null ? mainLayout.getHeader() : null;
	}

	public SessionContext getSessionContext() {
		return sessionContext;
	}

	public void setSessionContext(SessionContext sessionContext) {
		this.sessionContext = sessionContext;
	}

	@Override
	public ConstellioFactories getConstellioFactories() {
		return ConstellioFactories.getInstance();
	}

	public static SessionContext getCurrentSessionContext() {
		return getCurrent() != null ? getCurrent().getSessionContext() : null;
	}

	public boolean isProductionMode() {
		VaadinService service = VaadinService.getCurrent();
		return service.getDeploymentConfiguration().isProductionMode();
	}

	public Navigation navigate() {
		return new Navigation();
	}

	public CoreViews navigateTo() {
		return navigateTo(CoreViews.class);
	}

	public <T extends CoreViews> T navigateTo(Class<T> navigatorClass) {
		try {
			return navigatorClass.getConstructor(Navigator.class).newInstance(getNavigator());
		} catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
			throw new ImpossibleRuntimeException("The navigator does not provide a valid constructor");
		}
	}

	public void addRecordContextMenuHandler(RecordContextMenuHandler recordContextMenuHandler) {
		this.recordContextMenuHandlers.add(recordContextMenuHandler);
	}

	public void removeRecordContextMenuHandler(RecordContextMenuHandler recordContextMenuHandler) {
		this.recordContextMenuHandlers.remove(recordContextMenuHandler);
	}

	public List<RecordContextMenuHandler> getRecordContextMenuHandlers() {
		return recordContextMenuHandlers;
	}

	public void addRecordMenuBarHandler(RecordMenuBarHandler recordMenuBarHandler) {
		this.recordMenuBarHandlers.add(recordMenuBarHandler);
	}

	public void removeRecordMenuBarHandler(RecordMenuBarHandler recordMenuBarHandler) {
		this.recordMenuBarHandlers.remove(recordMenuBarHandler);
	}

	public List<RecordMenuBarHandler> getRecordMenuBarHandlers() {
		return recordMenuBarHandlers;
	}

	public static ConstellioUI getCurrent() {
		return (ConstellioUI) UI.getCurrent();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T clearAttribute(String key) {
		return (T) uiContext.remove(key);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getAttribute(String key) {
		return (T) uiContext.get(key);
	}

	@Override
	public <T> T setAttribute(String key, T value) {
		uiContext.put(key, value);
		return null;
	}

	public View getCurrentView() {
		return currentView;
	}

	public MainLayout getMainLayout() {
		return mainLayout;
	}

	public Component getStaticFooterContent() {
		return mainLayout.getStaticFooterContent();
	}

	public void setStaticFooterContent(Component component) {
		if (mainLayout != null) {
			mainLayout.setStaticFooterContent(component);
		}
	}

	public Thread runAsync(final Runnable runnable, int pollInterval, final Component component) {
		final boolean restorePollingInterval;
		final int pollIntervalBefore = getPollInterval();
		if (getPollInterval() <= 0) {
			setPollInterval(pollInterval);
			restorePollingInterval = true;
		} else {
			restorePollingInterval = false;
		}

		Thread thread = new Thread() {
			@Override
			public void run() {
				try {
					runnable.run();
				} finally {
					if (restorePollingInterval && component.isAttached()) {
						// If not attached, it means that we probably have left the view, which means that poll interval has already been changed
						access(new Runnable() {
							@Override
							public void run() {
								setPollInterval(pollIntervalBefore);
							}
						});
					}
				}
			}
		};
		thread.start();
		return thread;
	}

	private void updateTooltipConfiguration() {
		TooltipConfiguration tooltipConfiguration = getTooltipConfiguration();
		int currentOpenDelay = tooltipConfiguration.getOpenDelay(); // Default: 750
		int currentQuickOpenDelay = tooltipConfiguration.getQuickOpenDelay(); // Default: 100
		int currentQuickOpenTimeout = tooltipConfiguration.getQuickOpenTimeout(); // Default: 1000
		int currentCloseTimeout = tooltipConfiguration.getCloseTimeout(); // Default: 300

		int newCloseTimeout;
		int newOpenDelay;
		int newQuickOpenDelay;
		int newQuickOpenTimeout;
		if (ResponsiveUtils.isMobile()) {
			newOpenDelay = 50;
			newQuickOpenDelay = 50;
			newQuickOpenTimeout = 50;
			newCloseTimeout = 3000;
		} else {
			newOpenDelay = 50;
			newQuickOpenDelay = 50;
			newQuickOpenTimeout = 1000;
			newCloseTimeout = 300;
		}
		if (currentOpenDelay != newOpenDelay) {
			tooltipConfiguration.setOpenDelay(newOpenDelay);
		}
		if (currentQuickOpenDelay != newQuickOpenDelay) {
			tooltipConfiguration.setQuickOpenDelay(newQuickOpenDelay);
		}
		if (currentQuickOpenTimeout != newQuickOpenTimeout) {
			tooltipConfiguration.setQuickOpenTimeout(newQuickOpenTimeout);
		}
		if (currentCloseTimeout != newCloseTimeout) {
			tooltipConfiguration.setCloseTimeout(newCloseTimeout);
		}
	}

	@Override
	public void browserWindowResized(BrowserWindowResizeEvent event) {
		updateTooltipConfiguration();
	}

	public void showMessage(String message) {
		Notification notification = new Notification(message, Type.WARNING_MESSAGE);
		notification.setHtmlContentAllowed(true);
		notification.show(Page.getCurrent());
	}

	public void showClickableMessage(String message) {
		//		Notification notification = new Notification(message, Type.WARNING_MESSAGE);
		//		notification.setDelayMsec(-1);
		//		notification.setHtmlContentAllowed(true);
		//		notification.show(Page.getCurrent());
		ClickableNotification.show(this, "", message);
	}

	public void showClickableMessage(VerticalLayout verticalLayout) {
		ClickableNotification.show(this, "", verticalLayout);
	}

	public void showConfirmDialog(ConfirmDialogProperties properties) {
		new ConfirmDialogShowerImpl(UI::getCurrent).showConfirmDialog(properties);
	}

	public void showErrorMessage(String errorMessage) {
		Notification notification = new Notification(errorMessage.replace("\n", "<br/>") +
													 "<br/><br/>" + $("clickToClose"), Type.WARNING_MESSAGE);
		notification.setDelayMsec(3000);
		notification.setHtmlContentAllowed(true);
		notification.show(Page.getCurrent());
	}

	public void scrollToTop() {
		if (mainLayout != null) {
			mainLayout.scrollToTop();
		}
	}
}

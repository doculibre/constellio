package com.constellio.app.ui.application;

import static com.constellio.app.ui.i18n.i18n.$;

import java.lang.reflect.InvocationTargetException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.constellio.app.ui.framework.components.BaseWindow;
import com.vaadin.annotations.PreserveOnRefresh;
import org.joda.time.LocalDateTime;

import com.constellio.app.modules.rm.ui.builders.UserToVOBuilder;
import com.constellio.app.modules.rm.ui.contextmenu.RMRecordContextMenuHandler;
import com.constellio.app.modules.rm.ui.menuBar.RMRecordMenuBarHandler;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.services.sso.SSOServices;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.entities.UserVO;
import com.constellio.app.ui.framework.components.contextmenu.RecordContextMenuHandler;
import com.constellio.app.ui.framework.components.menuBar.RecordMenuBarHandler;
import com.constellio.app.ui.framework.components.resource.ConstellioResourceHandler;
import com.constellio.app.ui.handlers.ConstellioErrorHandler;
import com.constellio.app.ui.i18n.i18n;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.pages.base.ConstellioHeader;
import com.constellio.app.ui.pages.base.EnterViewListener;
import com.constellio.app.ui.pages.base.InitUIListener;
import com.constellio.app.ui.pages.base.MainLayoutImpl;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.base.SessionContextProvider;
import com.constellio.app.ui.pages.base.UIContext;
import com.constellio.app.ui.pages.base.VaadinSessionContext;
import com.constellio.app.ui.pages.login.LoginViewImpl;
import com.constellio.app.ui.pages.setup.ConstellioSetupViewImpl;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.entities.security.global.UserCredentialStatus;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.users.UserServices;
import com.vaadin.annotations.Theme;
import com.vaadin.event.UIEvents.PollListener;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.Page;
import com.vaadin.server.Page.BrowserWindowResizeEvent;
import com.vaadin.server.Page.BrowserWindowResizeListener;
import com.vaadin.server.RequestHandler;
import com.vaadin.server.Responsive;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.UI;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.dialogs.ConfirmDialog;
import org.vaadin.dialogs.DefaultConfirmDialogFactory;

@SuppressWarnings("serial")
@Theme("constellio")
public class ConstellioUI extends UI implements SessionContextProvider, UIContext {

	private static final ConstellioResourceHandler CONSTELLIO_RESSOURCE_HANDLER = new ConstellioResourceHandler();

	private SessionContext sessionContext;
	private MainLayoutImpl mainLayout;

	private List<RecordContextMenuHandler> recordContextMenuHandlers = new ArrayList<>();
	
	private List<RecordMenuBarHandler> recordMenuBarHandlers = new ArrayList<>();
	
	private Map<String, Object> uiContext = new HashMap<>();

	private SSOServices ssoServices;

	private View currentView;

	static {
		ConfirmDialog.setFactory(new ConfirmDialog.Factory() {
			@Override
			public ConfirmDialog create(String windowCaption, String message, String okTitle, String cancelTitle,
										String notOKCaption) {
				DefaultConfirmDialogFactory factory = new DefaultConfirmDialogFactory();
				ConfirmDialog confirmDialog = factory.create(windowCaption, message, okTitle, cancelTitle, notOKCaption);
				confirmDialog.setContentMode(ConfirmDialog.ContentMode.HTML);
				confirmDialog.setResizable(true);
				confirmDialog.addAttachListener(new AttachListener() {
					@Override
					public void attach(AttachEvent event) {
						BaseWindow.executeZIndexAdjustJavascript(BaseWindow.OVER_ADVANCED_SEARCH_FORM_Z_INDEX + 1);
					}
				});
				return confirmDialog;
			}
		});
	}

	public void addRequestHandler(RequestHandler handler) {
		getSession().addRequestHandler(handler);
	}

	@Override
	protected void init(VaadinRequest request) {

		if (!getSession().getRequestHandlers().contains(CONSTELLIO_RESSOURCE_HANDLER)) {
			getSession().addRequestHandler(CONSTELLIO_RESSOURCE_HANDLER);
		}

		ssoServices = SSOServices.getInstance();

		Page.getCurrent().setTitle($("ConstellioUI.pageTitle"));

		ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
		AppLayerFactory appLayerFactory = constellioFactories.getAppLayerFactory();

		addRecordContextMenuHandler(new RMRecordContextMenuHandler(constellioFactories));
		addRecordMenuBarHandler(new RMRecordMenuBarHandler(constellioFactories));

		List<InitUIListener> initUIListeners = appLayerFactory.getInitUIListeners();
		for (InitUIListener initUIListener : initUIListeners) {
			initUIListener.beforeInitialize(this);
		}

		Responsive.makeResponsive(this);
		addStyleName(ValoTheme.UI_WITH_MENU);

		// Some views need to be aware of browser resize events so a
		// BrowserResizeEvent gets fired to the event but on every occasion.
		Page.getCurrent().addBrowserWindowResizeListener(
				new BrowserWindowResizeListener() {
					@Override
					public void browserWindowResized(final BrowserWindowResizeEvent event) {
					}
				});

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

			UserCredential userCredential = userServices.getUserCredential(username);
			if (userCredential != null && userCredential.getStatus() == UserCredentialStatus.ACTIVE) {
				List<String> collections = userCredential != null ? userCredential.getCollections() : new ArrayList<String>();

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
		if (isSetupRequired()) {
			ConstellioSetupViewImpl setupView = new ConstellioSetupViewImpl();
			setContent(setupView);
			addStyleName("setupview");
		} else {
			ConstellioFactories constellioFactories = getConstellioFactories();
			AppLayerFactory appLayerFactory = constellioFactories.getAppLayerFactory();

			UserVO currentUserVO = sessionContext.getCurrentUser();
			if (currentUserVO == null && ssoServices.isEnabled() && !sessionContext.isForcedSignOut()) {
				currentUserVO = ssoAuthenticate();
			}
			if (currentUserVO != null) {
				// Authenticated user
				mainLayout = new MainLayoutImpl(appLayerFactory);

				setContent(mainLayout);

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
						View oldView = event.getOldView();
						if (oldView instanceof PollListener) {
							removePollListener((PollListener) oldView);
						}

						View newView = event.getNewView();
						if (newView instanceof BaseViewImpl) {
							if (((BaseViewImpl) newView).isBackgroundViewMonitor()) {
								// Important to allow update of components in current UI from another Thread
								setPollInterval(1000);
							}
						} else if (newView instanceof PollListener) {
							// Important to allow update of components in current UI from another Thread
							setPollInterval(1000);
						} else {
							setPollInterval(-1);
						}
						ConstellioUI.this.currentView = newView;
						
						ConstellioFactories constellioFactories = getConstellioFactories();
						AppLayerFactory appLayerFactory = constellioFactories.getAppLayerFactory();
						List<EnterViewListener> enterViewListeners = appLayerFactory.getEnterViewListeners();
						for (EnterViewListener enterViewListener : enterViewListeners) {
							enterViewListener.enterView(newView);
						}

						//						if (enterViewListeners.isEmpty() && !isProductionMode()) {
						//							try {
						//								ConstellioSerializationUtils.validateSerializable(event.getOldView());
						//							} catch (Exception e) {
						//								LOGGER.warn(e.getMessage(), e);
						//							}
						//							try {
						//								ConstellioSerializationUtils.validateSerializable(event.getNewView());
						//							} catch (Exception e) {
						//								LOGGER.warn(e.getMessage(), e);
						//							}
						//						}
					}
				});

				removeStyleName("setupview");
				removeStyleName("loginview");
				navigator.navigateTo(navigator.getState());
			} else {
				removeStyleName("setupview");
				LoginViewImpl loginView = new LoginViewImpl();
				setContent(loginView);
				addStyleName("loginview");
			}
		}
	}

	private boolean isSetupRequired() {
		ConstellioFactories constellioFactories = getConstellioFactories();
		return constellioFactories.getAppLayerFactory().getCollectionsManager().getCollectionCodesExcludingSystem().isEmpty();
	}

	public ConstellioHeader getHeader() {
		return mainLayout.getHeader();
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
		return getCurrent().getSessionContext();
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
		this.recordContextMenuHandlers.remove(recordMenuBarHandler);
	}
	
	public List<RecordMenuBarHandler> getRecordMenuBarHandlers() {
		return recordMenuBarHandlers;
	}

	public static ConstellioUI getCurrent() {
		return (ConstellioUI) UI.getCurrent();
	}
	
	@Override
	public void clearAttribute(String key) {
		uiContext.remove(key);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getAttribute(String key) {
		return (T) uiContext.get(key);
	}

	@Override
	public <T> void setAttribute(String key, T value) {
		uiContext.put(key, value);
	}

	public View getCurrentView() {
		return currentView;
	}

}

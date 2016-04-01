package com.constellio.app.ui.application;

import static com.constellio.app.ui.i18n.i18n.$;

import java.lang.reflect.InvocationTargetException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDateTime;

import com.constellio.app.modules.rm.ui.builders.UserToVOBuilder;
import com.constellio.app.modules.rm.ui.contextmenu.RMRecordContextMenuHandler;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.services.sso.KerberosServices;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.entities.UserVO;
import com.constellio.app.ui.framework.components.contextmenu.RecordContextMenuHandler;
import com.constellio.app.ui.framework.components.resource.ConstellioResourceHandler;
import com.constellio.app.ui.handlers.ConstellioErrorHandler;
import com.constellio.app.ui.i18n.i18n;
import com.constellio.app.ui.pages.base.ConstellioHeader;
import com.constellio.app.ui.pages.base.EnterViewListener;
import com.constellio.app.ui.pages.base.InitUIListener;
import com.constellio.app.ui.pages.base.MainLayoutImpl;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.base.SessionContextProvider;
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

@SuppressWarnings("serial")
@Theme("constellio")
public class ConstellioUI extends UI implements SessionContextProvider {

	private SessionContext sessionContext;
	private MainLayoutImpl mainLayout;

	private List<RecordContextMenuHandler> recordContextMenuHandlers = new ArrayList<>();

	public final RequestHandler requestHandler = new ConstellioResourceHandler();

	private KerberosServices kerberosServices;

	@Override
	protected void init(VaadinRequest request) {
		getSession().addRequestHandler(requestHandler);

		kerberosServices = KerberosServices.getInstance();

		Page.getCurrent().setTitle($("ConstellioUI.pageTitle"));

		// Important to allow update of components in current UI from another Thread
		UI.getCurrent().setPollInterval(1000);

		ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
		AppLayerFactory appLayerFactory = constellioFactories.getAppLayerFactory();

		addRecordContextMenuHandler(new RMRecordContextMenuHandler(constellioFactories));

		List<InitUIListener> initUIListeners = appLayerFactory.getInitUIListeners();
		for (InitUIListener initUIListener : initUIListeners) {
			initUIListener.beforeInitialize(this);
		}

		Responsive.makeResponsive(this);
		//		addStyleName(ValoTheme.UI_WITH_MENU);
		addStyleName("ui-with-top-menu");

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

	@Override
	public void detach() {
		super.detach();
		getSession().removeRequestHandler(requestHandler);
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
					currentUserVO = new UserToVOBuilder().build(userInLastCollection.getWrappedRecord(), VIEW_MODE.DISPLAY, sessionContext);
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
			if (currentUserVO == null && kerberosServices.isEnabled() && !sessionContext.isForcedSignOut()) {
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
						return true;
					}

					@Override
					public void afterViewChange(ViewChangeEvent event) {
						View oldView = event.getOldView();
						if (oldView instanceof PollListener) {
							removePollListener((PollListener) oldView);
						}

						View newView = event.getNewView();
						ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
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

	public static ConstellioUI getCurrent() {
		return (ConstellioUI) UI.getCurrent();
	}

	public class Navigation {
		public CoreViews to() {
			return to(CoreViews.class);
		}

		public <T extends CoreViews> T to(Class<T> navigatorClass) {
			try {
				return navigatorClass.getConstructor(Navigator.class).newInstance(getNavigator());
			} catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
				throw new ImpossibleRuntimeException("The navigator does not provide a valid constructor");
			}
		}
	}
}

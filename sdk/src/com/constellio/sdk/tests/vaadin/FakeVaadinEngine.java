package com.constellio.sdk.tests.vaadin;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.pages.base.ConstellioHeaderImpl;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.base.VaadinSessionContext;
import com.vaadin.data.util.converter.DefaultConverterFactory;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.DeploymentConfiguration;
import com.vaadin.server.DeploymentConfiguration.LegacyProperyToStringMode;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinResponse;
import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinServletRequest;
import com.vaadin.server.VaadinSession;
import com.vaadin.server.WebBrowser;
import com.vaadin.server.WrappedSession;
import com.vaadin.shared.ui.ui.PageState;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.ConnectorTracker;
import com.vaadin.ui.HasComponents;
import com.vaadin.ui.UI;
import com.vaadin.util.CurrentInstance;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class FakeVaadinEngine implements AutoCloseable {
	private final Map<Class<?>, CurrentInstance> vaadinInstancesBeforeTests;

	private final ConstellioUI ui;
	private final VaadinService vaadinService;
	private final VaadinSession vaadinSession;
	private final VaadinRequest vaadinRequest;
	private final VaadinServletRequest vaadinServletRequest;
	private final VaadinResponse vaadinResponse;

	private final ComponentContainer componentContainer;
	private final Navigator navigator;

	private View currentView;

	public FakeVaadinEngine(SessionContext sessionContext) {

		vaadinInstancesBeforeTests = CurrentInstance.getInstances(false);

		ui = setupMockedUI(sessionContext);
		vaadinService = setupMockedVaadinService();

		ConnectorTracker connectorTracker = createConnectorTracker(ui);
		doReturn(connectorTracker).when(ui).getConnectorTracker();

		vaadinSession = setupMockedVaadinSession(sessionContext);
		doReturn(vaadinSession).when(ui).getSession();

		vaadinRequest = setupMockedVaadinRequest();
		vaadinServletRequest = setupMockedVaadinServletRequest();
		vaadinResponse = setupMockedVaadinResponse();

		Page page = createPage(ui);
		doReturn(page).when(ui).getPage();

		WebBrowser webBrowser = new WebBrowser();
		webBrowser.updateRequestDetails(vaadinRequest);
		doReturn(webBrowser).when(vaadinSession).getBrowser();

		componentContainer = setupMockedComponentContainer();
		navigator = createNavigator(ui, componentContainer);
		doReturn(navigator).when(ui).getNavigator();

		doReturn(new ConstellioHeaderImpl()).when(ui).getHeader();


	}

	@Override
	public void close() {
		CurrentInstance.restoreInstances(vaadinInstancesBeforeTests);
	}

	public void show(View view, String viewName, String parameters) {
		view.enter(new ViewChangeEvent(navigator, currentView, view, viewName, parameters));
		currentView = view;
	}

	public void show(AbstractComponent component) {
		component.setParent(ui);
		component.attach();
	}

	public List<Component> getChildComponents(HasComponents component) {
		ArrayList<Component> childComponents = new ArrayList<>();

		component.forEach(childComponents::add);

		return childComponents;
	}

	public List<Component> getFlattenChildComponents(HasComponents component) {
		List<Component> components = new ArrayList<>();

		component.forEach(child -> {
			components.add(child);

			if (child instanceof HasComponents) {
				components.addAll(getFlattenChildComponents((HasComponents) child));
			}
		});

		return components;
	}

	public <TComponent extends Component> List<TComponent> getFlattenChildComponents(Class<TComponent> componentClass,
																					 HasComponents component) {
		return getFlattenChildComponents(component).stream()
				.filter(componentClass::isInstance)
				.map(child -> (TComponent) child)
				.collect(Collectors.toList());
	}

	public ConstellioUI getUi() {
		return ui;
	}

	public VaadinService getVaadinService() {
		return vaadinService;
	}

	public VaadinSession getVaadinSession() {
		return vaadinSession;
	}

	public VaadinRequest getVaadinRequest() {
		return vaadinRequest;
	}

	public ComponentContainer getComponentContainer() {
		return componentContainer;
	}

	public VaadinServletRequest getVaadinServletRequest() {
		return vaadinServletRequest;
	}

	public VaadinResponse getVaadinResponse() {
		return vaadinResponse;
	}

	private ConstellioUI setupMockedUI(SessionContext sessionContext) {
		ConstellioUI mockedUI = mock(ConstellioUI.class);
		ConstellioFactories constellioFactories = ConstellioFactories.getInstance();

		doReturn(Locale.getDefault()).when(mockedUI).getLocale();
		doReturn(constellioFactories).when(mockedUI).getConstellioFactories();
		doReturn(sessionContext).when(mockedUI).getSessionContext();

		CurrentInstance.setInheritable(UI.class, mockedUI);

		return mockedUI;
	}

	private ConnectorTracker createConnectorTracker(UI ui) {
		return new ConnectorTracker(ui);
	}

	private Navigator createNavigator(UI ui, ComponentContainer componentContainer) {
		return new Navigator(ui, componentContainer);
	}

	private ComponentContainer setupMockedComponentContainer() {
		return mock(ComponentContainer.class);
	}

	private VaadinService setupMockedVaadinService() {
		Class<VaadinService> vaadinServiceClass = VaadinService.class;

		VaadinService mockedVaadinService = mock(vaadinServiceClass);

		DeploymentConfiguration mockedDeploymentConfiguration = mock(DeploymentConfiguration.class);

		doReturn(LegacyProperyToStringMode.ENABLED).when(mockedDeploymentConfiguration).getLegacyPropertyToStringMode();
		doReturn(mockedDeploymentConfiguration).when(mockedVaadinService).getDeploymentConfiguration();

		CurrentInstance.setInheritable(vaadinServiceClass, mockedVaadinService);

		return mockedVaadinService;
	}

	private VaadinRequest setupMockedVaadinRequest() {
		Class<VaadinRequest> vaadinRequestClass = VaadinRequest.class;
		VaadinRequest mockedVaadinRequest = mock(vaadinRequestClass);

		doReturn("localhost:7000/constellio").when(mockedVaadinRequest).getContextPath();
		doReturn("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4183.102 Safari/537.36").when(mockedVaadinRequest).getHeader("User-Agent");

		CurrentInstance.setInheritable(vaadinRequestClass, mockedVaadinRequest);

		return mockedVaadinRequest;
	}

	private VaadinSession setupMockedVaadinSession(SessionContext sessionContext) {
		Class<VaadinSession> vaadinSessionClass = VaadinSession.class;

		VaadinSession mockedVaadinSession = mock(vaadinSessionClass);

		WrappedSession wrappedSession = mock(WrappedSession.class);


		doReturn(sessionContext.getCurrentUser()).when(wrappedSession).getAttribute(VaadinSessionContext.CURRENT_USER_ATTRIBUTE);
		doReturn(sessionContext.getCurrentCollection()).when(wrappedSession).getAttribute(VaadinSessionContext.CURRENT_COLLECTION_ATTRIBUTE);
		doReturn(wrappedSession).when(mockedVaadinSession).getSession();


		doReturn(true).when(mockedVaadinSession).hasLock();
		doReturn(new DefaultConverterFactory()).when(mockedVaadinSession).getConverterFactory();


		doAnswer(invocation -> UUID.randomUUID().toString()).when(mockedVaadinSession).createConnectorId(any());


		CurrentInstance.setInheritable(vaadinSessionClass, mockedVaadinSession);

		return mockedVaadinSession;
	}

	private VaadinServletRequest setupMockedVaadinServletRequest() {
		return mock(VaadinServletRequest.class);
	}

	private VaadinResponse setupMockedVaadinResponse() {
		VaadinResponse mockedVaadinResponse = mock(VaadinResponse.class);
		try {
			doReturn(mock(OutputStream.class)).when(mockedVaadinResponse).getOutputStream();
		} catch (IOException e) {
			//It's never supposed to happen
		}

		return mockedVaadinResponse;
	}

	private Page createPage(UI ui) {
		return new Page(ui, new PageState());
	}
}

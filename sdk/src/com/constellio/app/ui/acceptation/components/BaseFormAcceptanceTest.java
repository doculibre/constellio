package com.constellio.app.ui.acceptation.components;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.model.frameworks.validation.ValidationException;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.InDevelopmentTest;
import com.constellio.sdk.tests.annotations.UiTest;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.io.Serializable;

import static org.mockito.Mockito.when;

@UiTest
@InDevelopmentTest
public class BaseFormAcceptanceTest extends ConstellioTest {

	boolean REQUIRED = true;
	boolean MULTIVALUE = true;
	boolean READONLY = true;

	boolean FACULTATIVE = false;
	boolean SINGLEVALUE = false;
	boolean READWRITE = false;

	String dummyPage = "dummyPage";

	@Mock DummyViewPresenter presenter;
	@Mock DummyViewPresenter otherFormPresenter;

	ConstellioWebDriver driver;
	DummyBean dummyBean;

	@Before
	public void setUp()
			throws Exception {
		withSpiedServices(AppLayerFactory.class);
		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers()
		);

		AppLayerFactory factory = getAppLayerFactory();
		NavigatorConfigurationService navigatorConfigurationService = new NavigatorConfigurationService() {

			@Override
			public void configure(Navigator navigator) {
				super.configure(navigator);
				navigator.addView(dummyPage, new DummyView());
			}
		};
		when(factory.getNavigatorConfigurationService()).thenReturn(navigatorConfigurationService);

		driver = newWebDriver(loggedAsUserInCollection(admin, zeCollection));
	}

	@Test
	public void givenNameThenDisplayedInField()
			throws Exception {
		dummyBean = new DummyBean("zeId");
		driver.navigateTo().url(dummyPage);
		waitUntilICloseTheBrowsers();
	}

	public static class DummyBean implements Serializable {

		private String name;

		public DummyBean(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

	}

	private static class DummyViewPresenter {
		protected void saveButtonClick(DummyBean dummyBean)
				throws ValidationException {

		}

		protected void cancelButtonClick(DummyBean dummyBean) {

		}

	}

	public class DummyView extends BaseViewImpl implements View {

		@PropertyId("name")
		private TextField aField;

		@Override
		protected String getTitle() {
			return "BaseFormAcceptanceTest";
		}

		@Override
		protected Component buildMainComponent(ViewChangeEvent event) {
			System.out.println("session id > " + ConstellioUI.getCurrent().getSession().getSession().getId());

			aField = new TextField("Name");

			VerticalLayout verticalLayout = new VerticalLayout();

			BaseForm<DummyBean> form = new BaseForm<DummyBean>(dummyBean, DummyView.this, aField) {
				@Override
				protected void saveButtonClick(DummyBean dummyBean)
						throws ValidationException {
					simulateLatency();
					presenter.saveButtonClick(dummyBean);
				}

				@Override
				protected void cancelButtonClick(DummyBean dummyBean) {
					simulateLatency();
					presenter.cancelButtonClick(dummyBean);
				}
			};

			form.addStyleName("zeForm");
			verticalLayout.addComponent(form);

			return verticalLayout;
		}
	}

	private void simulateLatency() {
		try {
			Thread.sleep(400);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

}

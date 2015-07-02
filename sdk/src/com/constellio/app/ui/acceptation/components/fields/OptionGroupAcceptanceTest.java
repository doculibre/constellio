/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.ui.acceptation.components.fields;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.framework.components.fields.ListOptionGroup;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.tools.OptionGroupWebElement;
import com.constellio.data.utils.Factory;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.InDevelopmentTest;
import com.constellio.sdk.tests.annotations.UiTest;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@UiTest
public class OptionGroupAcceptanceTest extends ConstellioTest {

	String dummyPage = "dummyPage";

	@Mock DummyViewPresenter presenter;
	@Mock DummyViewPresenter otherFormPresenter;
	RMTestRecords rm = new RMTestRecords(zeCollection);

	ConstellioWebDriver driver;
	List<String> options;

	DummyView dummyView;

	OptionGroupWebElement radioGroupField;
	OptionGroupWebElement checkboxGroupField;

	@Before
	public void setUp()
			throws Exception {

		options = new ArrayList<>();
		options.add("Chuck");
		options.add("Dakota");
		options.add("Bob");

		withSpiedServices(AppLayerFactory.class);
		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers()
		);

		rm.setup(getModelLayerFactory()).withFoldersAndContainersOfEveryStatus();

		dummyView = new DummyView();

		AppLayerFactory factory = getAppLayerFactory();
		NavigatorConfigurationService navigatorConfigurationService = new NavigatorConfigurationService() {

			@Override
			public void configure(Navigator navigator) {
				super.configure(navigator);
				navigator.addView(dummyPage, dummyView);
			}
		};
		when(factory.getNavigatorConfigurationService()).thenReturn(navigatorConfigurationService);

		driver = newWebDriver(loggedAsUserInCollection("admin", zeCollection));

	}

	@Test
	@InDevelopmentTest
	//TODO Vincent Ne passe plus
	public void givenRadioAndCheckboxesFields()
			throws Exception {

		givenOptionGroupFieldsComponents();
		driver.navigateTo().url(dummyPage + "/" + Locale.FRENCH);
		driver.manage().window().maximize();
		radioGroupField = new OptionGroupWebElement(driver.find("zeRadiosField"));
		checkboxGroupField = new OptionGroupWebElement(driver.find("zeCheckboxesField"));

		whenGetValuesThenOK();
		whenToggleCheckboxesValuesThenOK();
		whenToggleRadioButtonValueThenOK();
	}

	public void whenGetValuesThenOK()
			throws Exception {

		assertThat(radioGroupField.isMultiSelect()).isFalse();
		assertThat(radioGroupField.getAllValues()).hasSize(3);
		assertThat(radioGroupField.getCheckedValues()).isEmpty();
		assertThat(radioGroupField.getAllValues())
				.containsExactly("Chuck", "Dakota", "Bob");
		assertThat(checkboxGroupField.isMultiSelect()).isTrue();
		assertThat(checkboxGroupField.getAllValues()).hasSize(3);
		assertThat(checkboxGroupField.getCheckedValues()).isEmpty();
		assertThat(checkboxGroupField.getAllValues())
				.containsExactly("Chuck", "Dakota", "Bob");
	}

	public void whenToggleCheckboxesValuesThenOK()
			throws Exception {

		checkboxGroupField.toggle("Dakota");
		checkboxGroupField.toggle("Bob");

		assertThat(checkboxGroupField.isMultiSelect()).isTrue();
		assertThat(checkboxGroupField.getAllValues()).hasSize(3);
		assertThat(checkboxGroupField.getCheckedValues()).hasSize(2);
		assertThat(checkboxGroupField.getAllValues())
				.containsExactly("Chuck", "Dakota", "Bob");
		assertThat(checkboxGroupField.getCheckedValues())
				.containsExactly("Dakota", "Bob");
	}

	public void whenToggleRadioButtonValueThenOK()
			throws Exception {

		radioGroupField.toggle("Dakota");
		radioGroupField.toggle("Bob");

		assertThat(radioGroupField.isMultiSelect()).isFalse();
		assertThat(radioGroupField.getAllValues()).hasSize(3);
		assertThat(radioGroupField.getCheckedValues()).hasSize(1);
		assertThat(radioGroupField.getAllValues())
				.containsExactly("Chuck", "Dakota", "Bob");
		assertThat(radioGroupField.getCheckedValues())
				.containsExactly("Bob");
	}

	//
	private static class DummyViewPresenter {

	}

	@SuppressWarnings("serial")
	public static class DummyView extends BaseViewImpl implements View {

		static Factory<ListOptionGroup> listRadioGroupFactory;
		static Factory<ListOptionGroup> listCheckboxGroupFactory;

		public DummyView() {
			super();
		}

		@Override
		protected String getTitle() {
			return "OptionGroupAcceptanceTest";
		}

		@Override
		protected void initBeforeCreateComponents(ViewChangeEvent event) {
			String localeCode = event.getParameters();
			if (StringUtils.isNotEmpty(localeCode)) {
				UI.getCurrent().setLocale(new Locale(localeCode));
			}
		}

		@Override
		protected Component buildMainComponent(ViewChangeEvent event) {
			System.out.println("session id > " + ConstellioUI.getCurrent().getSession().getSession().getId());

			VerticalLayout verticalLayout = new VerticalLayout();

			ListOptionGroup listRadioGroup = listRadioGroupFactory.get();
			ListOptionGroup listCheckboxGroup = listCheckboxGroupFactory.get();

			verticalLayout.addComponent(listRadioGroup);
			verticalLayout.addComponent(listCheckboxGroup);

			return verticalLayout;
		}

	}

	private void givenOptionGroupFieldsComponents() {
		DummyView.listRadioGroupFactory = new Factory<ListOptionGroup>() {
			@Override
			public ListOptionGroup get() {
				ListOptionGroup listOptionGroup = new ListOptionGroup("List radioOptionGroup", options);
				listOptionGroup.addStyleName("zeRadiosField");
				listOptionGroup.setMultiSelect(false);
				//				listOptionGroup.setImmediate(true);
				return listOptionGroup;
			}
		};

		DummyView.listCheckboxGroupFactory = new Factory<ListOptionGroup>() {
			@Override
			public ListOptionGroup get() {
				ListOptionGroup listOptionGroup = new ListOptionGroup("List checkBoxOptionGroup", options);
				listOptionGroup.addStyleName("zeCheckboxesField");
				listOptionGroup.setMultiSelect(true);
				//				listOptionGroup.setImmediate(true);
				return listOptionGroup;
			}
		};
	}
}

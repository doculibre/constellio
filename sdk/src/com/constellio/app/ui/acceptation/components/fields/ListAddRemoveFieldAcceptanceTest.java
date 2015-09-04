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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.wrappers.FilingSpace;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.fields.list.ListAddRemoveDateField;
import com.constellio.app.ui.framework.components.fields.list.ListAddRemoveField;
import com.constellio.app.ui.framework.components.fields.list.ListAddRemoveRecordComboBox;
import com.constellio.app.ui.framework.components.fields.list.ListAddRemoveRecordLookupField;
import com.constellio.app.ui.framework.components.fields.list.ListAddRemoveTextField;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.tools.components.listAddRemove.ListAddRemoveDateFieldWebElement;
import com.constellio.app.ui.tools.components.listAddRemove.ListAddRemoveDropDownWebElement;
import com.constellio.app.ui.tools.components.listAddRemove.ListAddRemoveLookupWebElement;
import com.constellio.app.ui.tools.components.listAddRemove.ListAddRemoveTextFieldWebElement;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.UiTest;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;
import com.vaadin.data.Property;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

@UiTest
public class ListAddRemoveFieldAcceptanceTest extends ConstellioTest {

	LocalDate date1 = new LocalDate();
	LocalDate date2 = date1.minusDays(42);
	LocalDate date3 = date2.minusDays(42);

	ConstellioWebDriver driver;
	RecordVO dummyViewRecord;

	MetadataSchemaVO schema;

	String dummyPage = "dummyPage";

	DummyView dummyView;

	@Before
	public void setUp()
			throws Exception {
		withSpiedServices(AppLayerFactory.class);
		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers()
		);

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

		driver = newWebDriver(loggedAsUserInCollection(admin, zeCollection));
	}

	private static Map<Locale, String> asLocaleMap(String englishValue, String frenchValue) {
		Map<Locale, String> map = new HashMap<>();
		map.put(Locale.ENGLISH, englishValue);
		map.put(Locale.FRENCH, frenchValue);
		return map;
	}

	@Test
	public void givenStringListForTextFieldThenControllableByWebElement() {
		DummyProperty<String> dummyStringProperty = new DummyProperty<String>(new ArrayList<String>());

		ListAddRemoveTextField addRemoveTextField = new ListAddRemoveTextField();
		addRemoveTextField.setPropertyDataSource(dummyStringProperty);
		addRemoveTextField.setStyleName("zeDisplay");
		addRemoveTextField.setCaption("ListAddRemoveTextField");
		dummyView.addListAddRemoveField(addRemoveTextField);

		driver.navigateTo().url(dummyPage);

		validateCanAddRemoveStringValuesIn("zeDisplay");

	}

	@Test
	public void givenDateListForDateFieldThenControllableByWebElement() {
		DummyProperty<Date> dummyDateProperty = new DummyProperty<Date>(new ArrayList<Date>());

		ListAddRemoveDateField addRemoveDateField = new ListAddRemoveDateField();
		addRemoveDateField.setPropertyDataSource(dummyDateProperty);
		addRemoveDateField.setStyleName("zeDisplay");
		addRemoveDateField.setCaption("ListAddRemoveDateField");
		dummyView.addListAddRemoveField(addRemoveDateField);

		driver.navigateTo().url(dummyPage);
		validateCanAddRemoveDateValuesIn("zeDisplay");
	}

	//@Test
	public void givenRecordLookupFieldThenControllableByWebElement()
			throws Exception {

		RMTestRecords records = new RMTestRecords(zeCollection).setup(getModelLayerFactory());

		DummyProperty<String> dummyReferenceProperty = new DummyProperty<>(new ArrayList<String>());
		ListAddRemoveRecordLookupField addRemoveLookupField = new ListAddRemoveRecordLookupField(FilingSpace.DEFAULT_SCHEMA);
		addRemoveLookupField.setPropertyDataSource(dummyReferenceProperty);
		addRemoveLookupField.setStyleName("zeDisplay");
		addRemoveLookupField.setCaption("ListAddRemoveLookupField");
		dummyView.addListAddRemoveField(addRemoveLookupField);

		driver.navigateTo().url(dummyPage);
		validateCanAddRemoveRecordsUsingLookupValuesIn("zeDisplay", records);
	}

	//@Test
	public void givenRecordComboboxThenControllableByWebElement()
			throws Exception {

		RMTestRecords records = new RMTestRecords(zeCollection).setup(getModelLayerFactory());

		DummyProperty<String> dummyReferenceProperty = new DummyProperty<>(new ArrayList<String>());
		ListAddRemoveRecordComboBox addRemoveLookupField = new ListAddRemoveRecordComboBox(RetentionRule.DEFAULT_SCHEMA);
		addRemoveLookupField.setPropertyDataSource(dummyReferenceProperty);
		addRemoveLookupField.setStyleName("zeDisplay");
		addRemoveLookupField.setCaption("ListAddRemoveLookupField");
		dummyView.addListAddRemoveField(addRemoveLookupField);

		driver.navigateTo().url(dummyPage);
		validateCanAddRemoveRecordsUsingDropDownValuesIn("zeDisplay", records);
	}

	@Test
	public void givenTwoListFieldsThenBothControllableByWebElement() {
		DummyProperty<String> dummyStringProperty = new DummyProperty<String>(new ArrayList<String>());
		ListAddRemoveTextField addRemoveTextField = new ListAddRemoveTextField();
		addRemoveTextField.setPropertyDataSource(dummyStringProperty);
		addRemoveTextField.setStyleName("stringList");
		addRemoveTextField.setCaption("ListAddRemoveTextField");
		dummyView.addListAddRemoveField(addRemoveTextField);

		DummyProperty<Date> dummyDateProperty = new DummyProperty<Date>(new ArrayList<Date>());
		ListAddRemoveDateField addRemoveDateField = new ListAddRemoveDateField();
		addRemoveDateField.setPropertyDataSource(dummyDateProperty);
		addRemoveDateField.setStyleName("dateList");
		addRemoveDateField.setCaption("ListAddRemoveDateField");
		dummyView.addListAddRemoveField(addRemoveDateField);

		driver.navigateTo().url(dummyPage);

		//		validateCanAddRemoveStringValuesIn("stringList");
		//		validateCanAddRemoveDateValuesIn("dateList");
	}

	private void validateCanAddRemoveStringValuesIn(String style) {
		ListAddRemoveTextFieldWebElement addRemoveField = new ListAddRemoveTextFieldWebElement(driver.find(style));

		addRemoveField.setValue("Édouard");
		addRemoveField.setValue("Chuck Norris");
		addRemoveField.clickAdd();
		assertThat(addRemoveField.getValues()).containsOnlyOnce("Chuck Norris");

		addRemoveField.add("Dakota L'indien");
		assertThat(addRemoveField.getValues()).containsExactly("Chuck Norris", "Dakota L'indien");

		addRemoveField.modifyTo(1, "Édouard Lechat");
		assertThat(addRemoveField.getValues()).containsExactly("Chuck Norris", "Édouard Lechat");

		//		addRemoveField.remove(1);
		//		assertThat(addRemoveField.getValues()).containsOnlyOnce("Chuck Norris");
	}

	private void validateCanAddRemoveDateValuesIn(String style) {
		ListAddRemoveDateFieldWebElement addRemoveField = new ListAddRemoveDateFieldWebElement(driver.find(style));

		addRemoveField.getInputComponent().setValue(date2);
		addRemoveField.getInputComponent().setValue(date1);
		addRemoveField.clickAdd();
		assertThat(addRemoveField.getValues()).containsOnlyOnce(date1.toString());

		addRemoveField.getInputComponent().setValue(date2);
		addRemoveField.clickAdd();
		assertThat(addRemoveField.getValues()).containsExactly(date1.toString(), date2.toString());

		addRemoveField.clickModify(1);
		addRemoveField.getInputComponent().setValue(date3);
		addRemoveField.clickAdd();
		assertThat(addRemoveField.getValues()).containsExactly(date1.toString(), date3.toString());

		//		addRemoveField.remove(1);
		//		assertThat(addRemoveField.getValues()).containsOnlyOnce(date1.toString());
	}

	private void validateCanAddRemoveRecordsUsingDropDownValuesIn(String style, RMTestRecords records) {
		ListAddRemoveDropDownWebElement addRemoveField = new ListAddRemoveDropDownWebElement(driver.find(style));

		addRemoveField.add(records.getRule1().getTitle());
		assertThat(addRemoveField.getValues()).containsOnlyOnce(records.getRule1().getTitle());

		addRemoveField.add(records.getRule2().getTitle());
		assertThat(addRemoveField.getValues()).containsExactly(records.getRule1().getTitle(), records.getRule2().getTitle());

		addRemoveField.modify(0, records.getRule3().getTitle());
		assertThat(addRemoveField.getValues()).containsExactly(records.getRule3().getTitle(), records.getRule2().getTitle());

		addRemoveField.remove(1);
		assertThat(addRemoveField.getValues()).containsOnlyOnce(records.getRule3().getTitle());
	}

	private void validateCanAddRemoveRecordsUsingLookupValuesIn(String style, RMTestRecords records) {
		ListAddRemoveLookupWebElement addRemoveField = new ListAddRemoveLookupWebElement(driver.find(style));

		addRemoveField.addElementByChoosingFirstChoice(records.getRule1().getTitle());
		assertThat(addRemoveField.getValues()).containsOnlyOnce(records.getRule1().getTitle());

		addRemoveField.addElementByChoosingFirstChoice(records.getRule2().getTitle());
		assertThat(addRemoveField.getValues()).containsExactly(records.getRule1().getTitle(), records.getRule2().getTitle());

		addRemoveField.modifyElementByChoosingFirstChoice(0, records.getRule3().getTitle());
		assertThat(addRemoveField.getValues()).containsExactly(records.getRule3().getTitle(), records.getRule2().getTitle());

		addRemoveField.remove(1);
		assertThat(addRemoveField.getValues()).containsOnlyOnce(records.getRule3().getTitle());
	}

	private class DummyProperty<T extends Serializable> implements Property<List<T>> {

		private List<T> value;

		public DummyProperty(List<T> value) {
			this.value = value;
		}

		@Override
		public List<T> getValue() {
			return value;
		}

		@Override
		public void setValue(List<T> newValue)
				throws com.vaadin.data.Property.ReadOnlyException {
			this.value = newValue;
		}

		@SuppressWarnings("unchecked")
		@Override
		public Class<? extends List<T>> getType() {
			return (Class) List.class;
		}

		@Override
		public boolean isReadOnly() {
			return false;
		}

		@Override
		public void setReadOnly(boolean newStatus) {
			if (newStatus) {
				throw new UnsupportedOperationException("Never read-only");
			}
		}

	}

	public class DummyView extends BaseViewImpl implements View {

		private List<ListAddRemoveField<?, ?>> listAddRemoveFields = new ArrayList<ListAddRemoveField<?, ?>>();

		public void addListAddRemoveField(ListAddRemoveField<?, ?> listAddRemoveField) {
			this.listAddRemoveFields.add(listAddRemoveField);
		}

		@Override
		protected String getTitle() {
			return "ListAddRemoveFieldAcceptanceTest";
		}

		@Override
		protected Component buildMainComponent(ViewChangeEvent event) {
			System.out.println("session id > " + ConstellioUI.getCurrent().getSession().getSession().getId());

			VerticalLayout mainLayout = new VerticalLayout();
			mainLayout.setWidth("100%");
			mainLayout.setSpacing(true);

			Label titleLabel = new Label(ListAddRemoveFieldAcceptanceTest.class.getSimpleName());
			titleLabel.addStyleName(ValoTheme.LABEL_H1);

			mainLayout.addComponent(titleLabel);
			for (ListAddRemoveField<?, ?> listAddRemoveField : listAddRemoveFields) {
				listAddRemoveField.setWidth("100%");
				mainLayout.addComponent(listAddRemoveField);
			}

			return mainLayout;
		}
	}

}

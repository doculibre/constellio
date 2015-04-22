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
package com.constellio.app.ui.acceptation.components;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.MetadataValueVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.components.RecordDisplay;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.tools.RecordDisplayWebElement;
import com.constellio.model.entities.schemas.MetadataValueType;
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

@InDevelopmentTest
@UiTest
public class RecordDisplayAcceptanceTest extends ConstellioTest {

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
	RecordVO dummyViewRecord;

	MetadataSchemaVO schema;

	@Before
	public void setUp()
			throws Exception {
		withSpiedServices(AppLayerFactory.class);
		givenCollection(zeCollection).withConstellioRMModule().withAllTestUsers();

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
		schema = new MetadataSchemaVO("zeSchema", zeCollection, asLocaleMap("The schema", "Ze schema"));
	}

	@Test
	public void givenLocaleThenDisplayedCaptionInSameLanguage() {
		MetadataVO metadata1 = new MetadataVO("metadata1", MetadataValueType.STRING, zeCollection, schema, FACULTATIVE,
				SINGLEVALUE,
				READWRITE, asLocaleMap("The m1", "Ze M1"), null, null, null, null, null, null);
		MetadataValueVO metadata1Value = new MetadataValueVO(metadata1, "testmetadata1");

		dummyViewRecord = new RecordVO("zeId", Arrays.asList(metadata1Value), VIEW_MODE.DISPLAY);

		RecordDisplayWebElement recordDisplay;

		driver.navigateTo().url(dummyPage + "/" + Locale.FRENCH);
		recordDisplay = new RecordDisplayWebElement(driver.find("zeDisplay"));
		assertThat(recordDisplay.getCaption("metadata1")).isEqualTo("Ze M1");
		assertThat(recordDisplay.getValue("metadata1")).isEqualTo("testmetadata1");

		driver.navigateTo().url(dummyPage + "/" + Locale.ENGLISH);
		recordDisplay = new RecordDisplayWebElement(driver.find("zeDisplay"));
		assertThat(recordDisplay.getCaption("metadata1")).isEqualTo("The m1");
		assertThat(recordDisplay.getValue("metadata1")).isEqualTo("testmetadata1");
	}

	@Test
	public void givenDateMetadataThenDisplayedWithCorrectCaptionAndPattern() {
		MetadataVO metadata1 = new MetadataVO("metadata1", MetadataValueType.DATE_TIME, zeCollection, schema, FACULTATIVE,
				SINGLEVALUE,
				READWRITE, asLocaleMap("The m1", "Ze M1"), null, null, null, null, null, null);
		MetadataValueVO metadata1Value = new MetadataValueVO(metadata1, new LocalDateTime(2015, 2, 15, 0, 0));

		dummyViewRecord = new RecordVO("zeId", Arrays.asList(metadata1Value), VIEW_MODE.DISPLAY);
		driver.navigateTo().url(dummyPage + "/" + Locale.FRENCH);

		RecordDisplayWebElement recordDisplay = new RecordDisplayWebElement(driver.find("zeDisplay"));

		assertThat(recordDisplay.getCaption("metadata1")).isEqualTo("Ze M1");
		assertThat(recordDisplay.getValue("metadata1")).isEqualTo("2015-02-15 00:00:00");
	}

	@Test
	public void givenNullMetadataThenConsideredInvisible() {
		MetadataVO metadata1 = new MetadataVO("metadata1", MetadataValueType.STRING, zeCollection, schema, FACULTATIVE,
				SINGLEVALUE,
				READWRITE, asLocaleMap("The m1", "Ze M1"), null, null, null, null, null, null);
		MetadataValueVO metadata1Value = new MetadataValueVO(metadata1, null);

		dummyViewRecord = new RecordVO("zeId", Arrays.asList(metadata1Value), VIEW_MODE.DISPLAY);
		driver.navigateTo().url(dummyPage + "/" + Locale.ENGLISH);

		RecordDisplayWebElement recordDisplay = new RecordDisplayWebElement(driver.find("zeDisplay"));

		assertThat(recordDisplay.isVisible("metadata1")).isFalse();
	}

	private static Map<Locale, String> asLocaleMap(String englishValue, String frenchValue) {
		Map<Locale, String> map = new HashMap<>();
		map.put(Locale.ENGLISH, englishValue);
		map.put(Locale.FRENCH, frenchValue);
		return map;
	}

	private static class DummyViewPresenter {

	}

	@SuppressWarnings("serial")
	public class DummyView extends BaseViewImpl implements View {

		public DummyView() {
			super();
		}

		@Override
		protected String getTitle() {
			return "RecordDisplayAcceptanceTest";
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

			RecordDisplay recordDisplay = new RecordDisplay(dummyViewRecord);

			recordDisplay.addStyleName("zeDisplay");
			verticalLayout.addComponent(recordDisplay);

			return verticalLayout;
		}
	}

}

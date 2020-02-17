package com.constellio.app.ui.acceptation.components;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.entities.CollectionInfoVO;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.MetadataValueVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.components.RecordForm;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.tools.ApplicationRuntimeException;
import com.constellio.app.ui.tools.RecordFormWebElement;
import com.constellio.model.entities.CollectionInfo;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.frameworks.validation.ValidationException;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.InDevelopmentTest;
import com.constellio.sdk.tests.annotations.UiTest;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@UiTest
public class RecordFormAcceptanceTest extends ConstellioTest {

	private boolean throwExceptionOnDummyViewInit;

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
		CollectionInfo collectionInfo = getModelLayerFactory().getCollectionsListManager().getCollectionInfo(zeCollection);

		CollectionInfoVO collectionInfoVO = new CollectionInfoVO(collectionInfo.getMainSystemLanguage(), collectionInfo.getCode(), collectionInfo.getCollectionLanguages(),
				collectionInfo.getMainSystemLocale(), collectionInfo.getSecondaryCollectionLanguesCodes(), collectionInfo.getCollectionLanguesCodes(), collectionInfo.getCollectionLocales());


		schema = new MetadataSchemaVO("zeSchema", zeCollection, asLocaleMap("The schema", "Ze schema"), collectionInfoVO, new HashMap<>());

		throwExceptionOnDummyViewInit = false;
	}

	@Test
	public void givenRequiredStringMetadataWhenSubmittedWithEmptyValueThenValidationFailAndSaveNeverCalledOnPresenter()
			throws Exception {
		MetadataVO metadata = new MetadataVO((short) 0, "m1", "m1", MetadataValueType.STRING, zeCollection, schema, REQUIRED, SINGLEVALUE,
				READWRITE, asLocaleMap("The m1", "Ze M1"), null, null, null, null, null, null, null, null, false,
				new HashSet<String>(), false, null, new HashMap<String, Object>(), schema.getCollectionInfoVO(), false, true, null);
		dummyViewRecord = new RecordVO("zeId", Arrays.asList(new MetadataValueVO(metadata)), VIEW_MODE.FORM);
		driver.navigateTo().url(dummyPage);

		RecordFormWebElement form = new RecordFormWebElement(driver.find("zeForm"));
		form.clickSaveButtonAndWaitForPageReload();

		verify(presenter, never()).saveButtonClick(any(RecordVO.class));

		//waitUntilICloseTheBrowsers();

	}

	@Test
	public void givenOtherFormSelectedWhenClickButtonThenClickInSameForm()
			throws Exception {

		MetadataVO metadata = new MetadataVO((short) 0, "m1", "m1", MetadataValueType.STRING, zeCollection, schema, FACULTATIVE, SINGLEVALUE,
				READWRITE, asLocaleMap("The m1", "Ze M1"), null, null, null, null, null, null, null, null, false,
				new HashSet<String>(), false, null, new HashMap<String, Object>(), schema.getCollectionInfoVO(), false, true, null);
		dummyViewRecord = new RecordVO("zeId", Arrays.asList(new MetadataValueVO(metadata)), VIEW_MODE.FORM);
		driver.navigateTo().url(dummyPage);

		RecordFormWebElement otherForm = new RecordFormWebElement(driver.find("anotherForm"));
		otherForm.clickSaveButtonAndWaitForPageReload();

		verify(otherFormPresenter).saveButtonClick(any(RecordVO.class));

	}

	@Test
	public void givenFacultativeStringMetadataWhenSubmittedWithEmptyValueThenSaveCalledOnPresenter()
			throws Exception {

		MetadataVO metadata = new MetadataVO((short) 0, "m1", "m1", MetadataValueType.STRING, zeCollection, schema, FACULTATIVE, SINGLEVALUE,
				READWRITE, asLocaleMap("The m1", "Ze M1"), null, null, null, null, null, null, null, null, false,
				new HashSet<String>(), false, null, new HashMap<String, Object>(), schema.getCollectionInfoVO(), false, true, null);
		dummyViewRecord = new RecordVO("zeId", Arrays.asList(new MetadataValueVO(metadata)), VIEW_MODE.FORM);
		driver.navigateTo().url(dummyPage);

		RecordFormWebElement form = new RecordFormWebElement(driver.find("zeForm"));
		form.clickSaveButtonAndWaitForPageReload();

		verify(presenter).saveButtonClick(any(RecordVO.class));

	}

	//@Test
	public void givenUnexpectedRuntimeExceptionThrownByPresenterThen()
			throws Exception {

		RuntimeException runtimeException = new RuntimeException("Boom!");

		doThrow(runtimeException).when(presenter).saveButtonClick(any(RecordVO.class));

		MetadataVO metadata = new MetadataVO((short) 0, "m1", "m1", MetadataValueType.STRING, zeCollection, schema, FACULTATIVE, SINGLEVALUE,
				READWRITE, asLocaleMap("The m1", "Ze M1"), null, null, null, null, null, null, null, null, false,
				new HashSet<String>(), false, null, new HashMap<String, Object>(), schema.getCollectionInfoVO(), false, true, null);
		dummyViewRecord = new RecordVO("zeId", Arrays.asList(new MetadataValueVO(metadata)), VIEW_MODE.FORM);
		driver.navigateTo().url(dummyPage);

		RecordFormWebElement form = new RecordFormWebElement(driver.find("zeForm"));

		try {
			//Devrait détecter qu'une exception est lancer et à son tour lancer une exception
			form.clickSaveButtonAndWaitForPageReload();
			//waitUntilICloseTheBrowsers();
			fail("ApplicationRuntimeException expected");
		} catch (ApplicationRuntimeException e) {
			String fullStackTrace = ExceptionUtils.getFullStackTrace(e);
			assertThat(fullStackTrace).contains("Boom!");
		}

	}

	@Test
	public void givenViewThrowsAnExceptionUponInitalizationThenApplicationRuntimeExceptionThrown()
			throws Exception {

		throwExceptionOnDummyViewInit = true;

		try {
			driver.navigateTo().url(dummyPage);
			fail("ApplicationRuntimeException expected");
		} catch (ApplicationRuntimeException e) {
			//OK
		}

	}

	@Test
	@InDevelopmentTest
	//TODO Vincent Ne passe plus
	public void whenModifyingValuesInFormThenSetInComponentsAndRetrievable()
			throws Exception {

		ArgumentCaptor<RecordVO> recordVOArgumentCaptor = ArgumentCaptor.forClass(RecordVO.class);

		MetadataVO metadata1 = new MetadataVO((short) 0, "metadata1", "metadata1", MetadataValueType.STRING, zeCollection, schema, FACULTATIVE,
				SINGLEVALUE,
				READWRITE, asLocaleMap("The m1", "Ze M1"), null, null, null, null, null, null, null, null, false,
				new HashSet<String>(), false, null, new HashMap<String, Object>(), schema.getCollectionInfoVO(), false, true, null);
		MetadataVO metadata2 = new MetadataVO((short) 0, "metadata2", "metadata2", MetadataValueType.STRING, zeCollection, schema, FACULTATIVE,
				SINGLEVALUE,
				READWRITE, asLocaleMap("The m2", "Ze M2"), null, null, null, null, null, null, null, null, false,
				new HashSet<String>(), false, null, new HashMap<String, Object>(), schema.getCollectionInfoVO(), false, true, null);
		MetadataValueVO metadata1Value = new MetadataValueVO(metadata1, "metadata1InitialValue");
		MetadataValueVO metadata2Value = new MetadataValueVO(metadata2, "metadata2InitialValue");

		dummyViewRecord = new RecordVO("zeId", Arrays.asList(metadata1Value, metadata2Value), VIEW_MODE.FORM);
		driver.navigateTo().url(dummyPage);

		RecordFormWebElement form = new RecordFormWebElement(driver.find("zeForm"));
		assertThat(form.<String>getValue("metadata1")).isEqualTo("metadata1InitialValue");
		assertThat(form.<String>getValue("metadata2")).isEqualTo("metadata2InitialValue");

		String newMetadata1Value = null;
		String newMetadata2Value = null;
		for (int i = 0; i < 10; i++) {
			newMetadata1Value = "metadata1ModifiedValue" + i;
			newMetadata2Value = "metadata2ModifiedValue" + i;

			form.setValue("metadata1", newMetadata1Value);
			form.setValue("metadata2", newMetadata2Value);
			assertThat(form.<String>getValue("metadata1")).isEqualTo(newMetadata1Value);
			assertThat(form.<String>getValue("metadata2")).isEqualTo(newMetadata2Value);
		}

		form.clickSaveButtonAndWaitForPageReload();

		verify(presenter).saveButtonClick(recordVOArgumentCaptor.capture());
		RecordVO receivedRecordVO = recordVOArgumentCaptor.getValue();
		assertThat(receivedRecordVO.<String>get("metadata1")).isEqualTo(newMetadata1Value);
		assertThat(receivedRecordVO.<String>get("metadata2")).isEqualTo(newMetadata2Value);
	}

	//TODO Maxime
	//@Test
	public void whenModifyingReferenceValuesInFormThenSetInComponentsAndRetrievable()
			throws Exception {

		ArgumentCaptor<RecordVO> recordVOArgumentCaptor = ArgumentCaptor.forClass(RecordVO.class);

		MetadataVO metadata1 = new MetadataVO((short) 0, "metadata1", "metadata1", MetadataValueType.REFERENCE, zeCollection, schema, FACULTATIVE,
				SINGLEVALUE,
				READWRITE, asLocaleMap("The m1", "Ze M1"), null, null, null, null, null, null, null, null, false,
				new HashSet<String>(), false, null, new HashMap<String, Object>(), schema.getCollectionInfoVO(), false, true, null);
		MetadataVO metadata2 = new MetadataVO((short) 0, "metadata2", "metadata2", MetadataValueType.REFERENCE, zeCollection, schema, FACULTATIVE,
				SINGLEVALUE,
				READWRITE, asLocaleMap("The m2", "Ze M2"), null, null, null, null, null, null, null, null, false,
				new HashSet<String>(), false, null, new HashMap<String, Object>(), schema.getCollectionInfoVO(), false, true, null);
		MetadataValueVO metadata1Value = new MetadataValueVO(metadata1, "metadata1InitialValue");
		MetadataValueVO metadata2Value = new MetadataValueVO(metadata2, "metadata2InitialValue");

		dummyViewRecord = new RecordVO("zeId", Arrays.asList(metadata1Value, metadata2Value), VIEW_MODE.FORM);
		driver.navigateTo().url(dummyPage);

		RecordFormWebElement form = new RecordFormWebElement(driver.find("zeForm"));
		assertThat(form.<String>getValue("metadata1")).isEqualTo("metadata1InitialValue");
		assertThat(form.<String>getValue("metadata2")).isEqualTo("metadata2InitialValue");

		String newMetadata1Value = null;
		String newMetadata2Value = null;
		for (int i = 0; i < 10; i++) {
			newMetadata1Value = "metadata1ModifiedValue" + i;
			newMetadata2Value = "metadata2ModifiedValue" + i;

			form.setValue("metadata1", newMetadata1Value);
			form.setValue("metadata2", newMetadata2Value);

			assertThat(form.<String>getValue("metadata1")).isEqualTo(newMetadata1Value);
			assertThat(form.<String>getValue("metadata2")).isEqualTo(newMetadata2Value);
		}

		form.clickSaveButtonAndWaitForPageReload();

		verify(presenter).saveButtonClick(recordVOArgumentCaptor.capture());
		RecordVO receivedRecordVO = recordVOArgumentCaptor.getValue();
		assertThat(receivedRecordVO.<String>get("metadata1")).isEqualTo(newMetadata1Value);
		assertThat(receivedRecordVO.<String>get("metadata2")).isEqualTo(newMetadata2Value);

	}


	/*
	@Test
	public void a()
			throws Exception {
		givenFacultativeStringMetadataWhenSubmittedWithEmptyValueThenSaveCalledOnPresenter();
	}

	@Test
	public void b()
			throws Exception {
		givenFacultativeStringMetadataWhenSubmittedWithEmptyValueThenSaveCalledOnPresenter();
	}

	@Test
	public void c()
			throws Exception {
		givenFacultativeStringMetadataWhenSubmittedWithEmptyValueThenSaveCalledOnPresenter();
	}

	@Test
	public void d()
			throws Exception {
		givenFacultativeStringMetadataWhenSubmittedWithEmptyValueThenSaveCalledOnPresenter();
	}

	@Test
	public void e()
			throws Exception {
		givenFacultativeStringMetadataWhenSubmittedWithEmptyValueThenSaveCalledOnPresenter();
	}

	@Test
	public void f()
			throws Exception {
		givenFacultativeStringMetadataWhenSubmittedWithEmptyValueThenSaveCalledOnPresenter();
	}

	@Test
	public void g()
			throws Exception {
		givenFacultativeStringMetadataWhenSubmittedWithEmptyValueThenSaveCalledOnPresenter();
	}

	@Test
	public void h()
			throws Exception {
		givenFacultativeStringMetadataWhenSubmittedWithEmptyValueThenSaveCalledOnPresenter();
	}

	@Test
	public void i()
			throws Exception {
		givenFacultativeStringMetadataWhenSubmittedWithEmptyValueThenSaveCalledOnPresenter();
	}

	@Test
	public void j()
			throws Exception {
		givenFacultativeStringMetadataWhenSubmittedWithEmptyValueThenSaveCalledOnPresenter();
	}

	@Test
	public void k()
			throws Exception {
		givenFacultativeStringMetadataWhenSubmittedWithEmptyValueThenSaveCalledOnPresenter();
	}

	@Test
	public void l()
			throws Exception {
		givenFacultativeStringMetadataWhenSubmittedWithEmptyValueThenSaveCalledOnPresenter();
	}

	@Test
	public void m()
			throws Exception {
		givenFacultativeStringMetadataWhenSubmittedWithEmptyValueThenSaveCalledOnPresenter();
	}

	@Test
	public void n()
			throws Exception {
		givenFacultativeStringMetadataWhenSubmittedWithEmptyValueThenSaveCalledOnPresenter();
	}

	@Test
	public void o()
			throws Exception {
		givenFacultativeStringMetadataWhenSubmittedWithEmptyValueThenSaveCalledOnPresenter();
	}

	@Test
	public void p()
			throws Exception {
		givenFacultativeStringMetadataWhenSubmittedWithEmptyValueThenSaveCalledOnPresenter();
	}

	@Test
	public void q()
			throws Exception {
		givenFacultativeStringMetadataWhenSubmittedWithEmptyValueThenSaveCalledOnPresenter();
	}

	@Test
	public void r()
			throws Exception {
		givenFacultativeStringMetadataWhenSubmittedWithEmptyValueThenSaveCalledOnPresenter();
	}

	@Test
	public void s()
			throws Exception {
		givenFacultativeStringMetadataWhenSubmittedWithEmptyValueThenSaveCalledOnPresenter();
	}

	@Test
	public void t()
			throws Exception {
		givenFacultativeStringMetadataWhenSubmittedWithEmptyValueThenSaveCalledOnPresenter();
	}

	@Test
	public void u()
			throws Exception {
		givenFacultativeStringMetadataWhenSubmittedWithEmptyValueThenSaveCalledOnPresenter();
	}

	@Test
	public void v()
			throws Exception {
		givenFacultativeStringMetadataWhenSubmittedWithEmptyValueThenSaveCalledOnPresenter();
	}

	@Test
	public void w()
			throws Exception {
		givenFacultativeStringMetadataWhenSubmittedWithEmptyValueThenSaveCalledOnPresenter();
	}

	@Test
	public void x()
			throws Exception {
		givenFacultativeStringMetadataWhenSubmittedWithEmptyValueThenSaveCalledOnPresenter();
	}

	@Test
	public void y()
			throws Exception {
		givenFacultativeStringMetadataWhenSubmittedWithEmptyValueThenSaveCalledOnPresenter();
	}

	@Test
	public void z()
			throws Exception {
		givenFacultativeStringMetadataWhenSubmittedWithEmptyValueThenSaveCalledOnPresenter();
	}
	*/

	private static Map<Locale, String> asLocaleMap(String englishValue, String frenchValue) {
		Map<Locale, String> map = new HashMap<>();
		map.put(Locale.ENGLISH, englishValue);
		map.put(Locale.FRENCH, frenchValue);
		return map;
	}

	private static class DummyViewPresenter {

		protected void saveButtonClick(RecordVO record)
				throws ValidationException {

		}

		protected void cancelButtonClick(RecordVO record) {

		}

	}

	public class DummyView extends BaseViewImpl implements View {

		@Override
		protected String getTitle() {
			return "RecordFormAcceptanceTest";
		}

		@Override
		protected Component buildMainComponent(ViewChangeEvent event) {
			if (throwExceptionOnDummyViewInit) {
				throw new RuntimeException("throwExceptionOnDummyViewInit");
			}
			System.out.println("session id > " + ConstellioUI.getCurrent().getSession().getSession().getId());

			VerticalLayout verticalLayout = new VerticalLayout();

			RecordForm anotherForm = new RecordForm(dummyViewRecord) {
				@Override
				protected void saveButtonClick(RecordVO record)
						throws ValidationException {
					simulateLatency();
					otherFormPresenter.saveButtonClick(record);
				}

				@Override
				protected void cancelButtonClick(RecordVO record) {
					simulateLatency();
					otherFormPresenter.cancelButtonClick(record);
				}
			};

			RecordForm form = new RecordForm(dummyViewRecord) {
				@Override
				protected void saveButtonClick(RecordVO record)
						throws ValidationException {
					simulateLatency();
					presenter.saveButtonClick(record);
				}

				@Override
				protected void cancelButtonClick(RecordVO record) {
					simulateLatency();
					presenter.cancelButtonClick(record);
				}
			};

			anotherForm.addStyleName("anotherForm");
			verticalLayout.addComponent(anotherForm);

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

package com.constellio.app.ui.acceptation.components;

import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.FilingSpace;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.buttons.ConfirmDialogButton;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.DisableButton;
import com.constellio.app.ui.framework.buttons.DisplayButton;
import com.constellio.app.ui.framework.buttons.EditButton;
import com.constellio.app.ui.framework.buttons.EnableButton;
import com.constellio.app.ui.framework.buttons.IconButton;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.framework.containers.ButtonsContainer.ContainerButton;
import com.constellio.app.ui.framework.containers.RecordVOLazyContainer;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.tools.RecordContainerWebElement;
import com.constellio.app.ui.tools.RecordContainerWebElementRuntimeException.RecordContainerWebElementRuntimeException_NoSuchRowWithValueInColumn;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.InDevelopmentTest;
import com.constellio.sdk.tests.annotations.UiTest;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;
import com.vaadin.data.Container;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.vaadin.dialogs.ConfirmDialog;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

@InDevelopmentTest
@UiTest
public class RecordContainerAcceptanceTest extends ConstellioTest {

	private static final String PROPERTY_BUTTONS_1 = "buttons1";
	private static final String PROPERTY_BUTTONS_2 = "buttons2";
	private static final String CUSTOM_ICON_BUTTON_STYLE = "customIconButton";
	private static final String CUSTOM_CONFIRM_BUTTON_STYLE = "customConfirmButton";

	MetadataSchemaTypes schemaTypes;
	MetadataSchema filingSpaceSchema, administrativeUnitSchema;
	String dummyPage = "dummyPage";

	@Mock DummyViewPresenter presenter, anotherPresenter;

	ConstellioWebDriver driver;

	RecordServices recordServices;
	ModelLayerFactory modelLayerFactory;
	SessionContext sessionContext;

	@Before
	public void setUp()
			throws Exception {
		withSpiedServices(AppLayerFactory.class);
		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers()
		);
		inCollection(zeCollection).giveWriteAndDeleteAccessTo(dakota);

		modelLayerFactory = getModelLayerFactory();
		recordServices = modelLayerFactory.newRecordServices();
		schemaTypes = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(zeCollection);
		filingSpaceSchema = schemaTypes.getSchema(FilingSpace.DEFAULT_SCHEMA);
		administrativeUnitSchema = schemaTypes.getSchema(AdministrativeUnit.DEFAULT_SCHEMA);

		given200FilingSpaces();
		given200AdministrativeUnits();

		AppLayerFactory factory = getAppLayerFactory();
		NavigatorConfigurationService navigatorConfigurationService = new NavigatorConfigurationService() {
			@Override
			public void configure(Navigator navigator) {
				super.configure(navigator);
				navigator.addView(dummyPage, new DummyView());
			}
		};
		when(factory.getNavigatorConfigurationService()).thenReturn(navigatorConfigurationService);

		sessionContext = loggedAsUserInCollection(dakota, zeCollection);
		driver = newWebDriver(loggedAsUserInCollection(dakota, zeCollection));
	}

	@Test
	public void givenRecordContainerThenCanClickOnAllButtons()
			throws Exception {
		driver.navigateTo().url(dummyPage);

		RecordContainerWebElement zeTable = new RecordContainerWebElement(driver.find("zeTable"));

		zeTable.getRow(0).clickButton(DisplayButton.BUTTON_STYLE);
		zeTable.getRow(1).clickButtonAndConfirm(DeleteButton.BUTTON_STYLE);
		zeTable.getRow(2).clickButton(EditButton.BUTTON_STYLE);
		zeTable.getRow(3).clickButtonAndConfirm(DisableButton.BUTTON_STYLE);
		zeTable.getRow(4).clickButtonAndConfirm(EnableButton.BUTTON_STYLE);
		zeTable.getRow(5).clickButton(CUSTOM_ICON_BUTTON_STYLE);
		zeTable.getRow(6).clickButtonAndConfirm(CUSTOM_CONFIRM_BUTTON_STYLE);

		zeTable.getRow(6).clickButton(EditButton.BUTTON_STYLE);
		zeTable.getRow(7).clickButton(DisplayButton.BUTTON_STYLE);
		zeTable.getRow(8).clickButtonAndConfirm(DeleteButton.BUTTON_STYLE);
		zeTable.getRow(4).clickButtonAndConfirm(EnableButton.BUTTON_STYLE);
		zeTable.getRow(3).clickButtonAndConfirm(DisableButton.BUTTON_STYLE);
		zeTable.getRow(6).clickButtonAndConfirm(CUSTOM_CONFIRM_BUTTON_STYLE);
		zeTable.getRow(5).clickButton(CUSTOM_ICON_BUTTON_STYLE);

		InOrder inOrder = inOrder(presenter);
		inOrder.verify(presenter).displayButtonClick(0);
		inOrder.verify(presenter).deleteButtonClick(1);
		inOrder.verify(presenter).editButtonClick(2);
		inOrder.verify(presenter).disableButtonClick(3);
		inOrder.verify(presenter).enableButtonClick(4);
		inOrder.verify(presenter).customButtonClick(5);
		inOrder.verify(presenter).customConfirmButtonClick(6);

		inOrder.verify(presenter).editButtonClick(6);
		inOrder.verify(presenter).displayButtonClick(7);
		inOrder.verify(presenter).deleteButtonClick(8);
		inOrder.verify(presenter).enableButtonClick(4);
		inOrder.verify(presenter).disableButtonClick(3);
		inOrder.verify(presenter).customConfirmButtonClick(6);
		inOrder.verify(presenter).customButtonClick(5);
	}

	//TODO Vincent
	//@Test
	public void given2RecordContainersThenCanRetrieveRowsInformationOfBoth()
			throws Exception {
		driver.navigateTo().url(dummyPage);
		//		waitUntilICloseTheBrowsers();

		RecordContainerWebElement zeTable = new RecordContainerWebElement(driver.find("zeTable"));
		assertThat(zeTable.getHeaderTitles()).containsOnly("Code", "Titre", PROPERTY_BUTTONS_1);

		assertThat(zeTable.getRow(0)).isNotNull();
		assertThat(zeTable.getRow(0).getValueInColumn(0)).isEqualTo("F1");
		assertThat(zeTable.getRow(0).getValueInColumn(1)).isEqualTo("The description of Filing space #1");
		assertThat(zeTable.getRow(1).getValueInColumn(0)).isEqualTo("F2");
		assertThat(zeTable.getRow(1).getValueInColumn(1)).isEqualTo("The description of Filing space #2");
		assertThat(zeTable.getRow(8).getValueInColumn(0)).isEqualTo("F9");
		assertThat(zeTable.getRow(8).getValueInColumn(1)).isEqualTo("The description of Filing space #9");

		assertThat(zeTable.getFirstRowWithValueInColumn("F12", 0).getIndex()).isEqualTo(11);
		assertThat(zeTable.getFirstRowWithValueInColumn("The description of Filing space #6", 1).getValueInColumn(0))
				.isEqualTo("F6");

		try {
			zeTable.getFirstRowWithValueInColumn("X10", 0);
			fail("RecordContainerWebElementRuntimeException_NoSuchRowWithValueInColumn expected");
		} catch (RecordContainerWebElementRuntimeException_NoSuchRowWithValueInColumn e) {
			//OK
		}

		assertThat(zeTable.hasRowWithValueInColumn("F4", 0)).isTrue();
		assertThat(zeTable.hasRowWithValueInColumn("F4", 1)).isFalse();

		RecordContainerWebElement anotherTable = new RecordContainerWebElement(driver.find("anotherTable"));
		assertThat(anotherTable.getHeaderTitles()).containsOnly("Code", "Titre", PROPERTY_BUTTONS_2);

		assertThat(anotherTable.getRow(0)).isNotNull();
		assertThat(anotherTable.getRow(0).getValueInColumn(0)).isEqualTo("A1");
		assertThat(anotherTable.getRow(0).getValueInColumn(1)).isEqualTo("The description of Administrative unit #1");
		assertThat(anotherTable.getRow(1).getValueInColumn(0)).isEqualTo("A2");
		assertThat(anotherTable.getRow(1).getValueInColumn(1)).isEqualTo("The description of Administrative unit #2");
		assertThat(anotherTable.getRow(8).getValueInColumn(0)).isEqualTo("A9");
		assertThat(anotherTable.getRow(8).getValueInColumn(1)).isEqualTo("The description of Administrative unit #9");

		assertThat(anotherTable.getFirstRowWithValueInColumn("A12", 0).getIndex()).isEqualTo(11);
		assertThat(anotherTable.getFirstRowWithValueInColumn("The description of Administrative unit #6", 1).getValueInColumn(0))
				.isEqualTo("A6");

		//		waitUntilICloseTheBrowsers();
	}

	@Test
	public void given2RecordContainersThenCanClickOnEditAndRemoveButtons()
			throws Exception {
		driver.navigateTo().url(dummyPage);

		RecordContainerWebElement zeTable = new RecordContainerWebElement(driver.find("zeTable"));

		zeTable.getRow(0).clickButton(DisplayButton.BUTTON_STYLE);
		zeTable.getRow(1).clickButtonAndConfirm(DeleteButton.BUTTON_STYLE);
		//		waitUntilICloseTheBrowsers();
		zeTable.getRow(2).clickButton(EditButton.BUTTON_STYLE);

		zeTable.getRow(6).clickButton(EditButton.BUTTON_STYLE);
		zeTable.getRow(7).clickButton(DisplayButton.BUTTON_STYLE);
		zeTable.getRow(8).clickButtonAndConfirm(DeleteButton.BUTTON_STYLE);

		RecordContainerWebElement anotherTable = new RecordContainerWebElement(driver.find("anotherTable"));
		anotherTable.getRow(4).clickButton(DisplayButton.BUTTON_STYLE);
		anotherTable.getRow(5).clickButton(EditButton.BUTTON_STYLE);
		anotherTable.getRow(6).clickButtonAndConfirm(DeleteButton.BUTTON_STYLE);

		InOrder inOrder = inOrder(presenter, anotherPresenter);
		inOrder.verify(presenter).displayButtonClick(0);
		inOrder.verify(presenter).deleteButtonClick(1);
		inOrder.verify(presenter).editButtonClick(2);
		inOrder.verify(presenter).editButtonClick(6);
		inOrder.verify(presenter).displayButtonClick(7);
		inOrder.verify(presenter).deleteButtonClick(8);
		inOrder.verify(anotherPresenter).displayButtonClick(4);
		inOrder.verify(anotherPresenter).editButtonClick(5);
		inOrder.verify(anotherPresenter).deleteButtonClick(6);
	}

	//---------------------------------------------------------------

	private void given200AdministrativeUnits() {
		Transaction transaction = new Transaction();

		for (int i = 0; i < 200; i++) {
			AdministrativeUnit administrativeUnit = new AdministrativeUnit(
					recordServices.newRecordWithSchema(administrativeUnitSchema), schemaTypes);
			administrativeUnit.setCode("A" + (i + 1));
			administrativeUnit.setTitle("Administrative unit #" + (i + 1));
			administrativeUnit.setTitle("The description of Administrative unit #" + (i + 1));
			transaction.add(administrativeUnit.getWrappedRecord());
		}

		try {
			recordServices.execute(transaction);
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}
	}

	private void given200FilingSpaces() {

		Transaction transaction = new Transaction();

		for (int i = 0; i < 200; i++) {
			FilingSpace filingSpace = new FilingSpace(recordServices.newRecordWithSchema(filingSpaceSchema), schemaTypes);
			filingSpace.setCode("F" + (i + 1));
			filingSpace.setTitle("Filing space #" + (i + 1));
			filingSpace.setTitle("The description of Filing space #" + (i + 1));
			transaction.add(filingSpace.getWrappedRecord());
		}

		try {
			recordServices.execute(transaction);
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}
	}

	private static class DummyViewPresenter implements Serializable {

		protected void displayButtonClick(Object itemId) {
			System.err.println("Display : " + itemId);
		}

		protected void editButtonClick(Object itemId) {
			System.err.println("Edit : " + itemId);
		}

		protected void deleteButtonClick(Object itemId) {
			System.err.println("Delete : " + itemId);
		}

		protected void enableButtonClick(Object itemId) {
			System.err.println("Enable : " + itemId);
		}

		protected void disableButtonClick(Object itemId) {
			System.err.println("Disable : " + itemId);
		}

		protected void customButtonClick(Object itemId) {
			System.err.println("Custom : " + itemId);
		}

		protected void customConfirmButtonClick(Object itemId) {
			System.err.println("Custom confirm : " + itemId);
		}

	}

	@SuppressWarnings("serial")
	public class DummyView extends BaseViewImpl implements View {

		public DummyView() {
			super();
		}

		@Override
		protected String getTitle() {
			return "RecordContainerAcceptanceTest";
		}

		@Override
		protected Component buildMainComponent(ViewChangeEvent event) {
			System.out.println("session id > " + ConstellioUI.getCurrent().getSession().getSession().getId());

			VerticalLayout verticalLayout = new VerticalLayout();

			Table zeTable = new Table("zeTable", newAllRecordsContainer(filingSpaceSchema, presenter, PROPERTY_BUTTONS_1));

			Table anotherTable = new Table("anotherTable",
					newAllRecordsContainer(administrativeUnitSchema, anotherPresenter, PROPERTY_BUTTONS_2));

			zeTable.addStyleName("zeTable");
			verticalLayout.addComponent(zeTable);

			anotherTable.addStyleName("anotherTable");
			verticalLayout.addComponent(anotherTable);

			return verticalLayout;
		}

		private Container newAllRecordsContainer(MetadataSchema schema, final DummyViewPresenter tablePresenter,
												 String buttonsLabel) {
			Container recordsContainer = new RecordVOLazyContainer(getAllRecordsDataProvider(schema));
			final ButtonsContainer<?> buttonsContainer = new ButtonsContainer(recordsContainer, buttonsLabel);

			buttonsContainer.addButton(new ContainerButton() {
				@Override
				protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> buttonsContainer) {
					return new DisplayButton() {
						@Override
						protected void buttonClick(ClickEvent event) {
							tablePresenter.displayButtonClick(itemId);
						}
					};
				}
			});

			buttonsContainer.addButton(new ContainerButton() {
				@Override
				protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> buttonsContainer) {
					return new EditButton() {
						@Override
						protected void buttonClick(ClickEvent event) {
							tablePresenter.editButtonClick(itemId);
						}
					};
				}
			});

			buttonsContainer.addButton(new ContainerButton() {
				@Override
				protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> buttonsContainer) {
					return new DeleteButton() {
						@Override
						protected void confirmButtonClick(ConfirmDialog dialog) {
							tablePresenter.deleteButtonClick(itemId);
						}
					};
				}
			});

			buttonsContainer.addButton(new ContainerButton() {
				@Override
				protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> buttonsContainer) {
					return new DisableButton() {
						@Override
						protected void confirmButtonClick(ConfirmDialog dialog) {
							tablePresenter.disableButtonClick(itemId);
						}
					};
				}
			});

			buttonsContainer.addButton(new ContainerButton() {
				@Override
				protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> buttonsContainer) {
					return new EnableButton() {
						@Override
						protected void confirmButtonClick(ConfirmDialog dialog) {
							tablePresenter.enableButtonClick(itemId);
						}
					};
				}
			});

			buttonsContainer.addButton(new ContainerButton() {
				@Override
				protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> buttonsContainer) {
					Button customIconButton = new IconButton(new ThemeResource("images/commun/permission.gif"), "Test") {
						@Override
						protected void buttonClick(ClickEvent event) {
							tablePresenter.customButtonClick(itemId);
						}
					};
					customIconButton.addStyleName(CUSTOM_ICON_BUTTON_STYLE);
					return customIconButton;
				}
			});

			buttonsContainer.addButton(new ContainerButton() {
				@Override
				protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> buttonsContainer) {
					Button customConfirmButton = new ConfirmDialogButton(new ThemeResource("images/commun/infobulle.gif"),
							"Test") {
						@Override
						protected String getConfirmDialogMessage() {
							return "Confirmer ceci?";
						}

						@Override
						protected void confirmButtonClick(ConfirmDialog dialog) {
							tablePresenter.customConfirmButtonClick(itemId);
						}
					};
					customConfirmButton.addStyleName(CUSTOM_CONFIRM_BUTTON_STYLE);
					return customConfirmButton;
				}
			});
			return buttonsContainer;
		}

		private RecordVODataProvider getAllRecordsDataProvider(MetadataSchema schema) {
			List<String> metadataCodes = new ArrayList<String>();
			metadataCodes.add(schema.getMetadata("id").getCode());
			metadataCodes.add(schema.getMetadata("code").getCode());
			metadataCodes.add(schema.getMetadata("title").getCode());

			final MetadataSchemaVO schemaVO = new MetadataSchemaToVOBuilder()
					.build(schema, VIEW_MODE.TABLE, metadataCodes, sessionContext);
			RecordToVOBuilder voBuilder = new RecordToVOBuilder();
			return new RecordVODataProvider(schemaVO, voBuilder, modelLayerFactory) {
				@Override
				public LogicalSearchQuery getQuery() {
					String collection = schemaVO.getCollection();
					String schemaCode = schemaVO.getCode();
					MetadataSchemasManager metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
					MetadataSchema schema = metadataSchemasManager.getSchemaTypes(collection).getSchema(schemaCode);
					return new LogicalSearchQuery(LogicalSearchQueryOperators.from(schema).returnAll());
				}
			};
		}

	}

}

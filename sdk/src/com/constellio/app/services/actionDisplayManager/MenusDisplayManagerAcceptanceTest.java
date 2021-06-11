package com.constellio.app.services.actionDisplayManager;

import com.constellio.app.services.actionDisplayManager.MenuDisplayItem.Type;
import com.constellio.app.services.actionDisplayManager.MenusDisplayTransaction.Action;
import com.constellio.app.services.collections.CollectionsManager;
import com.constellio.app.services.extensions.plugins.ConstellioPluginManager;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.events.SDKEventBusSendingService;
import com.constellio.model.frameworks.validation.ValidationException;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;
import org.assertj.core.api.ListAssert;
import org.assertj.core.groups.Tuple;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.constellio.sdk.tests.TestUtils.linkEventBus;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.api.Assertions.tuple;

public class MenusDisplayManagerAcceptanceTest extends ConstellioTest {

	CollectionsManager collectionsManager;
	CollectionsManager collectionsManagerOfOtherInstance;
	Users users = new Users();
	private String anotherCollection = "anotherCollection";

	private MenusDisplayManager manager;
	private MenusDisplayManager managerOfOtherInstance;
	private SchemasRecordsServices schemas;
	private UserServices userServices;
	private SDKEventBusSendingService zeInstanceEventBus;

	private ConstellioPluginManager pluginManager;

	private String schemaType1 = "schemaType1";
	private String schemaType2 = "schemaType2";
	private String schemaType3 = "schemaType3";

	private String collection1 = "collection1";
	private String collection2 = "collection2";

	private String actionCode1 = "actionCode1";
	private String actionCode2 = "actionCode2";
	private String actionCode3 = "actionCode3";
	private String actionCode4 = "actionCode4";

	private String actionIcon1 = "actionIcon1";
	private String actionIcon2 = "actionIcon2";
	private String actionIcon3 = "actionIcon3";
	private String actionIcon4 = "actionIcon4";

	private String containerCode1 = "containerCode1";
	private String containerCode2 = "containerCode2";
	private String containerCode3 = "containerCode3";

	private String containerIcon1 = "containerIcon1";
	private String containerIcon2 = "containerIcon2";
	private String containerIcon3 = "containerIcon3";

	private String i18nKey1 = "i18nKey1";
	private String i18nKey2 = "i18nKey2";
	private String i18nKey3 = "i18nKey3";
	private String i18nKey4 = "i18nKey4";

	private Map<Locale, String> containerLabel1 = createStringMap("containerLabel1Fr", "containerLabel1En");
	private Map<Locale, String> containerLabel2 = createStringMap("containerLabel2Fr", "containerLabel2En");
	private Map<Locale, String> containerLabel3 = createStringMap("containerLabel3Fr", "containerLabel3En");


	public static Map createStringMap(String french, String english) {
		Map labels = new HashMap();

		labels.put(Locale.FRENCH, french);
		labels.put(Locale.ENGLISH, english);

		return labels;
	}

	@Before
	public void setUp() {
		prepareSystem(
				withZeCollection().withAllTest(users),
				withCollection(anotherCollection).withAllTestUsers()
		);

		pluginManager = getAppLayerFactory().getPluginManager();
		collectionsManager = getAppLayerFactory().getCollectionsManager();

		AppLayerFactory otherInstanceAppLayerFactory = getAppLayerFactory("otherInstance");
		collectionsManagerOfOtherInstance = otherInstanceAppLayerFactory.getCollectionsManager();
		zeInstanceEventBus = linkEventBus(getDataLayerFactory(),
				otherInstanceAppLayerFactory.getModelLayerFactory().getDataLayerFactory());

		userServices = getModelLayerFactory().newUserServices();
		users = new Users().setUp(userServices, zeCollection);
		manager = getAppLayerFactory().getMenusDisplayManager();
		managerOfOtherInstance = otherInstanceAppLayerFactory.getMenusDisplayManager();
		schemas = new SchemasRecordsServices(zeCollection, getModelLayerFactory());
	}

	@Test
	public void givenMenusDisplayInMultipleCollectionsThenAllIndependent()
			throws Exception {
		givenCollection("collection1");
		givenCollection("collection2");

		assertThatEventsReceivedOnZeInstance();
		assertThatEventsSentFromZeInstance().isNotEmpty();

		MenuDisplayListBySchemaType menuDisplayListBySchemaTypeCollection1 = manager.getMenuDisplayList("collection1");
		MenuDisplayListBySchemaType menuDisplayListBySchemaTypeCollection2 = manager.getMenuDisplayList("collection2");

		MenuDisplayListBySchemaType menuDisplayListBySchemaTypeCollection1OtherInstance = managerOfOtherInstance.getMenuDisplayList("collection1");
		MenuDisplayListBySchemaType menuDisplayListBySchemaTypeCollection2OtherInstance = managerOfOtherInstance.getMenuDisplayList("collection2");

		assertThat(menuDisplayListBySchemaTypeCollection1.getNumberOfSchemaTypeRegistered()).isEqualTo(0);
		assertThat(menuDisplayListBySchemaTypeCollection2.getNumberOfSchemaTypeRegistered()).isEqualTo(0);
		assertThat(menuDisplayListBySchemaTypeCollection1OtherInstance.getNumberOfSchemaTypeRegistered()).isEqualTo(0);
		assertThat(menuDisplayListBySchemaTypeCollection2OtherInstance.getNumberOfSchemaTypeRegistered()).isEqualTo(0);

		List<MenuDisplayItem> menuDisplayItemList1 = getBasicMenuDisplayList();

		List<MenuDisplayItem> menuDisplayItemList2 = new ArrayList<>();
		menuDisplayItemList2.add(new MenuDisplayItem(actionCode3, actionIcon3, i18nKey3));
		menuDisplayItemList2.add(new MenuDisplayContainer(containerCode2, containerLabel2, containerIcon2));


		List<MenuDisplayItem> menuDisplayItemList3 = new ArrayList<>();
		menuDisplayItemList3.add(new MenuDisplayContainer(containerCode3, containerLabel3, containerIcon3));
		menuDisplayItemList3.add(new MenuDisplayItem(actionCode4, actionIcon4, i18nKey4, containerCode3));


		manager.withActionsDisplay(collection1, schemaType1, menuDisplayItemList1);
		managerOfOtherInstance.withActionsDisplay(collection1, schemaType3, menuDisplayItemList2);
		manager.withActionsDisplay(collection2, schemaType2, menuDisplayItemList3);

		assertThatEventsReceivedOnZeInstance().containsExactly(
				tuple("configUpdated", "/" + collection1 + "/actionsDisplayConfig.xml")
		);
		assertThatEventsSentFromZeInstance().containsExactly(
				tuple("configUpdated", "/" + collection1 + "/actionsDisplayConfig.xml"),
				tuple("configUpdated", "/" + collection2 + "/actionsDisplayConfig.xml")
		);

		MenuDisplayListBySchemaType menuDisplayManagerCollection1 = manager.getMenuDisplayList("collection1");
		MenuDisplayListBySchemaType menuDisplayManagerOtherInstanceCollection2 = managerOfOtherInstance.getMenuDisplayList("collection2");


		assertThat(menuDisplayManagerCollection1.getNumberOfSchemaTypeRegistered()).isEqualTo(2);
		assertThat(menuDisplayManagerOtherInstanceCollection2.getNumberOfSchemaTypeRegistered()).isEqualTo(1);
		assertThat(menuDisplayManagerOtherInstanceCollection2.getNumberOfSchemaTypeRegistered()).isEqualTo(1);

		MenuDisplayList menuDisplayListSchemaType2 = menuDisplayManagerOtherInstanceCollection2.getActionDisplayList(schemaType2);
		List<MenuDisplayItem> rootMenuListSchemaType2 = menuDisplayListSchemaType2.getRootMenuList();
		assertThat(rootMenuListSchemaType2).hasSize(1);
		assertThat(menuDisplayListSchemaType2.getRawMenus()).hasSize(2);


		// Collection 2
		MenuDisplayItem menuDisplayItem1SchemaType2 = rootMenuListSchemaType2.get(0);
		assertThat(menuDisplayItem1SchemaType2.getCode()).isEqualTo(containerCode3);
		assertThat(menuDisplayItem1SchemaType2.getIcon()).isEqualTo(containerIcon3);
		assertThat(menuDisplayItem1SchemaType2.getType()).isEqualTo(Type.CONTAINER);
		assertThat(menuDisplayItem1SchemaType2.isOfficiallyActive()).isTrue();
		assertThat(((MenuDisplayContainer) menuDisplayItem1SchemaType2).getLabels().get(Locale.FRENCH)).isEqualTo("containerLabel3Fr");
		assertThat(((MenuDisplayContainer) menuDisplayItem1SchemaType2).getLabels().get(Locale.ENGLISH)).isEqualTo("containerLabel3En");

		List<MenuDisplayItem> subMenuSchemaType2 = menuDisplayListSchemaType2.getSubMenu(menuDisplayItem1SchemaType2.getCode());
		assertThat(subMenuSchemaType2).hasSize(1);
		assertThat(subMenuSchemaType2.get(0).getType()).isEqualTo(Type.MENU);
		assertThat(subMenuSchemaType2.get(0).getCode()).isEqualTo(actionCode4);
		assertThat(subMenuSchemaType2.get(0).getIcon()).isEqualTo(actionIcon4);
		assertThat(subMenuSchemaType2.get(0).getI18nKey()).isEqualTo(i18nKey4);
		assertThat(subMenuSchemaType2.get(0).getParentCode()).isEqualTo(containerCode3);

		// Collection 1
		MenuDisplayList menuDisplayListSchemaType1 = menuDisplayManagerCollection1.getActionDisplayList(schemaType1);
		List<MenuDisplayItem> rootMenuListSchemaType1 = menuDisplayListSchemaType1.getRootMenuList();
		assertThat(rootMenuListSchemaType1).hasSize(2);
		assertThat(menuDisplayListSchemaType1.getRawMenus()).hasSize(3);
		assertThat(rootMenuListSchemaType1.get(0).getCode()).isEqualTo((actionCode1));
		assertThat(rootMenuListSchemaType1.get(0).getIcon()).isEqualTo(actionIcon1);
		assertThat(rootMenuListSchemaType1.get(0).getI18nKey()).isEqualTo(i18nKey1);
		assertThat(rootMenuListSchemaType1.get(0).isContainer()).isFalse();
		assertThat(rootMenuListSchemaType1.get(0).isOfficiallyActive()).isTrue();

		assertThat(rootMenuListSchemaType1.get(1).getCode()).isEqualTo(containerCode1);
		assertThat(rootMenuListSchemaType1.get(1).getIcon()).isEqualTo(containerIcon1);
		assertThat(rootMenuListSchemaType1.get(1).getType()).isEqualTo(Type.CONTAINER);
		assertThat(rootMenuListSchemaType1.get(1).isOfficiallyActive()).isTrue();
		assertThat(((MenuDisplayContainer) rootMenuListSchemaType1.get(1)).getLabels().get(Locale.FRENCH)).isEqualTo("containerLabel1Fr");
		assertThat(((MenuDisplayContainer) rootMenuListSchemaType1.get(1)).getLabels().get(Locale.ENGLISH)).isEqualTo("containerLabel1En");

		assertThat(menuDisplayListSchemaType1.getActiveSubMenu(containerCode1)).hasSize(0);

		MenuDisplayItem inactiveSubItemMenu = menuDisplayListSchemaType1.getSubMenu(containerCode1).get(0);

		assertThat(inactiveSubItemMenu.getCode()).isEqualTo((actionCode2));
		assertThat(inactiveSubItemMenu.getIcon()).isEqualTo(actionIcon2);
		assertThat(inactiveSubItemMenu.getI18nKey()).isEqualTo(i18nKey2);
		assertThat(inactiveSubItemMenu.isContainer()).isFalse();
		assertThat(inactiveSubItemMenu.isOfficiallyActive()).isFalse();
		assertThat(inactiveSubItemMenu.getParentCode()).isEqualTo(containerCode1);


		MenuDisplayList menuDisplayListSchemaType1AnOtherInstance = menuDisplayManagerOtherInstanceCollection2.getActionDisplayList(schemaType2);
		List<MenuDisplayItem> rootMenuListSchemaType1AnOtherInstance = menuDisplayListSchemaType1AnOtherInstance.getRootMenuList();
		assertThat(menuDisplayListSchemaType1AnOtherInstance.getRawMenus()).hasSize(2);
		assertThat(rootMenuListSchemaType1AnOtherInstance).hasSize(1);

		assertThat(rootMenuListSchemaType1AnOtherInstance.get(0).getCode()).isEqualTo(containerCode3);
		assertThat(rootMenuListSchemaType1AnOtherInstance.get(0).getIcon()).isEqualTo(containerIcon3);
		assertThat(rootMenuListSchemaType1AnOtherInstance.get(0).getType()).isEqualTo(Type.CONTAINER);
		assertThat(rootMenuListSchemaType1AnOtherInstance.get(0).isOfficiallyActive()).isTrue();
		assertThat(((MenuDisplayContainer) rootMenuListSchemaType1AnOtherInstance.get(0)).getLabels().get(Locale.FRENCH)).isEqualTo("containerLabel3Fr");
		assertThat(((MenuDisplayContainer) rootMenuListSchemaType1AnOtherInstance.get(0)).getLabels().get(Locale.ENGLISH)).isEqualTo("containerLabel3En");


		MenuDisplayItem menuDisplayItem = menuDisplayListSchemaType1AnOtherInstance.getSubMenu(containerCode3).get(0);
		assertThat(menuDisplayItem.getCode()).isEqualTo((actionCode4));
		assertThat(menuDisplayItem.getIcon()).isEqualTo(actionIcon4);
		assertThat(menuDisplayItem.getI18nKey()).isEqualTo(i18nKey4);
		assertThat(menuDisplayItem.isContainer()).isFalse();
		assertThat(menuDisplayItem.isOfficiallyActive()).isTrue();
		assertThat(menuDisplayItem.getParentCode()).isEqualTo(containerCode3);
	}

	@Test
	public void givenMenuDisplayInMenuDisplayListUseMethodHasChangesWithIdenticalContentReturnTrue()
			throws ValidationException {
		givenCollection("collection1");

		List<MenuDisplayItem> menuDisplayItemList1 = getBasicMenuDisplayList();

		List<MenuDisplayItem> menuDisplayItemListCopy = getMenuDisplayItemsCopy(menuDisplayItemList1);

		manager.withActionsDisplay(collection1, schemaType1, menuDisplayItemList1);

		MenuDisplayList menuDisplayList = manager.getMenuDisplayList(collection1).getActionDisplayList(schemaType1);

		assertThat(menuDisplayList.hasChanges(menuDisplayItemListCopy)).isFalse();
	}

	@NotNull
	private List<MenuDisplayItem> getMenuDisplayItemsCopy(List<MenuDisplayItem> menuDisplayItemList1) {
		List<MenuDisplayItem> menuDisplayItemListCopy = new ArrayList<>();
		for (MenuDisplayItem menuDisplayItem : menuDisplayItemList1) {
			if (!(menuDisplayItem instanceof MenuDisplayContainer)) {
				menuDisplayItemListCopy.add(menuDisplayItem.newMenuDisplayItemWithActive(menuDisplayItem.isActive()));
			} else {
				menuDisplayItemListCopy.add(menuDisplayItem.getMenuDisplayContainer().newMenuDisplayContainerWithActive(menuDisplayItem.isActive()));
			}
		}
		return menuDisplayItemListCopy;
	}

	@Test
	public void givenOneMenuDisplayItemUseMethodHasChangesWithDifferentItemThenReturnFalse()
			throws ValidationException {
		givenCollection("collection1");

		List<MenuDisplayItem> menuDisplayItemList1 = new ArrayList<>();

		menuDisplayItemList1.add(new MenuDisplayItem(actionCode1, actionIcon1, i18nKey1));

		manager.withActionsDisplay(collection1, schemaType1, menuDisplayItemList1);

		MenuDisplayList menuDisplayList = manager.getMenuDisplayList(collection1).getActionDisplayList(schemaType1);

		List<MenuDisplayItem> menuDisplayItemsCopy = new ArrayList<>();
		MenuDisplayItem menuDisplayItemCopy = menuDisplayItemList1.get(0).newMenuDisplayItemWithActive(menuDisplayItemList1.get(0).isActive());
		menuDisplayItemsCopy.add(menuDisplayItemCopy);

		assertThat(menuDisplayList.hasChanges(menuDisplayItemsCopy)).isFalse();

		List<MenuDisplayItem> menuDisplayItems2 = new ArrayList<>();

		menuDisplayItems2.add(menuDisplayItemCopy.newMenuDisplayItemWithIcon(null));

		assertThat(menuDisplayList.hasChanges(menuDisplayItems2)).isTrue();
	}

	@Test
	public void givenOneMenuDisplayContainerUseMethodHasChangesWithDifferentLabelThenReturnFalse()
			throws ValidationException {
		givenCollection("collection1");

		List<MenuDisplayItem> menuDisplayItemList1 = new ArrayList<>();

		menuDisplayItemList1.add(new MenuDisplayContainer(containerCode1, containerLabel1, containerIcon1));

		manager.withActionsDisplay(collection1, schemaType1, menuDisplayItemList1);

		MenuDisplayList menuDisplayList = manager.getMenuDisplayList(collection1).getActionDisplayList(schemaType1);

		List<MenuDisplayItem> menuDisplayItemsCopy = new ArrayList<>();
		MenuDisplayContainer menuDisplayContainerCopy = menuDisplayItemList1.get(0).getMenuDisplayContainer().newMenuDisplayContainerWithActive(menuDisplayItemList1.get(0).isActive());
		menuDisplayItemsCopy.add(menuDisplayContainerCopy);

		assertThat(menuDisplayList.hasChanges(menuDisplayItemsCopy)).isFalse();

		List<MenuDisplayItem> menuDisplayItems2 = new ArrayList<>();

		menuDisplayItems2.add(menuDisplayContainerCopy.newMenuDisplayContainerWithLabels(containerLabel2));

		assertThat(menuDisplayList.hasChanges(menuDisplayItems2)).isTrue();
	}

	@Test
	public void givenThreeMenuDisplayItemUseMethodHasChangesWithDifferentOrderThenReturnFalse()
			throws ValidationException {
		givenCollection("collection1");

		List<MenuDisplayItem> menuDisplayItemList1 = getBasicMenuDisplayList();

		manager.withActionsDisplay(collection1, schemaType1, menuDisplayItemList1);

		List<MenuDisplayItem> menuDisplayItemListCopy = getMenuDisplayItemsCopy(menuDisplayItemList1);


		MenuDisplayList menuDisplayList = manager.getMenuDisplayList(collection1).getActionDisplayList(schemaType1);

		assertThat(menuDisplayList.hasChanges(menuDisplayItemListCopy)).isFalse();

		List<MenuDisplayItem> menuDisplayItemList2 = Arrays.asList(menuDisplayItemList1.get(1), menuDisplayItemList1.get(0), menuDisplayItemList1.get(2));

		assertThat(menuDisplayList.hasChanges(menuDisplayItemList2)).isTrue();
	}

	@NotNull
	private List<MenuDisplayItem> getBasicMenuDisplayList() {
		List<MenuDisplayItem> menuDisplayItemList1 = new ArrayList<>();

		menuDisplayItemList1.add(new MenuDisplayItem(actionCode1, actionIcon1, i18nKey1));
		menuDisplayItemList1.add(new MenuDisplayContainer(containerCode1, containerLabel1, containerIcon1));
		menuDisplayItemList1.add(new MenuDisplayItem(actionCode2, actionIcon2, i18nKey2, false, containerCode1, false));

		return menuDisplayItemList1;
	}

	private void assertBasicMenuDisplayList(String collection, String schemaType) {
		List<MenuDisplayItem> menuDisplayItemList = manager.getMenuDisplayList(collection).getActionDisplayList(schemaType).getRawMenus();

		assertActionCode(menuDisplayItemList.get(0), 1);
		assertContainerCode((MenuDisplayContainer) menuDisplayItemList.get(1), 1);
		assertActionCode(menuDisplayItemList.get(2), 2);

	}

	@Test
	public void givenMenuDisplayWithInexisitngParentThenError() throws ValidationException {
		givenCollection("collection1");

		List<MenuDisplayItem> menuDisplayItemList1 = new ArrayList<>();
		menuDisplayItemList1.add(new MenuDisplayItem(actionCode1, actionIcon1, i18nKey1, containerCode1));

		try {
			manager.withActionsDisplay(collection1, schemaType1, menuDisplayItemList1);
			fail("Action with inexisting parent should throw.");
		} catch (ValidationException error) {
			assertThat(error.getValidationErrors().getValidationErrors().get(0).getCode()).isEqualTo("com.constellio.app.services.actionDisplayManager.MenusDisplayManager_parentDoesExist");
		}
	}

	@Test
	public void givenMenuDisplayWithNoCodeThenError() throws ValidationException {
		givenCollection("collection1");

		List<MenuDisplayItem> menuDisplayItemList1 = new ArrayList<>();
		menuDisplayItemList1.add(new MenuDisplayItem(null, actionIcon1, i18nKey1, containerCode1));

		try {
			manager.withActionsDisplay(collection1, schemaType1, menuDisplayItemList1);
			fail("Action require a code");
		} catch (ValidationException error) {
			assertThat(error.getValidationErrors().getValidationErrors().get(0).getCode()).isEqualTo("com.constellio.app.services.actionDisplayManager.MenusDisplayManager_valueRequired");
		}
	}

	@Test
	public void givenEmptyMenuDisplayListForSchemaTypeAddAtEndAndBeginingThenOrderIsValid() throws ValidationException {
		givenCollection("collection1");

		MenusDisplayTransaction menusDisplayTransaction = new MenusDisplayTransaction();


		MenuDisplayItem actionsDisplay1 = new MenuDisplayItem(actionCode1, actionIcon1, i18nKey1);
		menusDisplayTransaction.addElement(Action.ADD_UPDATE, schemaType1, actionsDisplay1, MenuPositionActionOptions.displayActionAtEnd());

		MenuDisplayItem actionsDisplay2 = new MenuDisplayItem(actionCode2, actionIcon2, i18nKey2);
		menusDisplayTransaction.addElement(Action.ADD_UPDATE, schemaType1, actionsDisplay2, MenuPositionActionOptions.displayActionAtBeginning());

		MenuDisplayItem actionsDisplay3 = new MenuDisplayItem(actionCode3, actionIcon3, i18nKey3, true, null, true);
		menusDisplayTransaction.addElement(Action.ADD_UPDATE, schemaType1, actionsDisplay3, MenuPositionActionOptions.displayActionAtEnd());

		manager.execute(collection1, menusDisplayTransaction);

		MenuDisplayList menuDisplayList = manager.getMenuDisplayList(collection1).getActionDisplayList(schemaType1);

		List<MenuDisplayItem> rootMenuList = menuDisplayList.getRootMenuList();

		MenuDisplayItem displayItem1 = rootMenuList.get(0);
		assertActionCode(displayItem1, 2);

		MenuDisplayItem displayItem2 = rootMenuList.get(1);
		assertActionCode(displayItem2, 1);

		MenuDisplayItem displayItem3 = rootMenuList.get(2);
		assertActionCode(displayItem3, 3);

	}

	public void assertActionCode(MenuDisplayItem menuDisplayItem, int i) {
		assertThat(menuDisplayItem.isContainer()).isFalse();
		assertThat(menuDisplayItem.getCode()).isEqualTo("actionCode" + i);
		assertThat(menuDisplayItem.getIcon()).isEqualTo("actionIcon" + i);
		assertThat(menuDisplayItem.getI18nKey()).isEqualTo("i18nKey" + i);
	}

	public void assertContainerCode(MenuDisplayContainer menuDisplayItem, int i) {
		assertThat(menuDisplayItem.isContainer()).isTrue();
		assertThat(menuDisplayItem.getCode()).isEqualTo("containerCode" + i);
		assertThat(menuDisplayItem.getIcon()).isEqualTo("containerIcon" + i);
		assertThat(menuDisplayItem.getLabels().get(Locale.FRENCH)).isEqualTo("containerLabel" + i + "Fr");
		assertThat(menuDisplayItem.getLabels().get(Locale.ENGLISH)).isEqualTo("containerLabel" + i + "En");
	}

	@Test
	public void givenMenuDisplayListWithElementsAddBeforeAndAfterThenOrderedAsExpected() throws ValidationException {
		givenCollection("collection1");

		List<MenuDisplayItem> menuDisplayItemList1 = getBasicMenuDisplayList();

		manager.withActionsDisplay(collection1, schemaType1, menuDisplayItemList1);

		MenusDisplayTransaction menusDisplayTransaction = new MenusDisplayTransaction();
		MenuDisplayItem actionsDisplay3 = new MenuDisplayItem(actionCode3, actionIcon3, i18nKey3);
		MenuDisplayItem actionsDisplay4 = new MenuDisplayItem(actionCode4, actionIcon4, i18nKey4);

		menusDisplayTransaction.addElement(Action.ADD_UPDATE, schemaType1, actionsDisplay4, MenuPositionActionOptions.displayActionBefore(actionCode1));
		menusDisplayTransaction.addElement(Action.ADD_UPDATE, schemaType1, actionsDisplay3, MenuPositionActionOptions.displayActionAfter(actionCode1));

		manager.execute(collection1, menusDisplayTransaction);

		MenuDisplayListBySchemaType menuDisplayListBySchemaType = manager.getMenuDisplayList(collection1);
		MenuDisplayList actionDisplayListSchemaType1 = menuDisplayListBySchemaType.getActionDisplayList(schemaType1);
		List<MenuDisplayItem> menuDisplayList = actionDisplayListSchemaType1.getRawMenus();

		assertThat(menuDisplayList.get(0).getCode()).isEqualTo(actionCode4);
		assertActionCode(menuDisplayList.get(0), 4);
		assertThat(menuDisplayList.get(1).getCode()).isEqualTo(actionCode1);
		assertActionCode(menuDisplayList.get(1), 1);
		assertThat(menuDisplayList.get(2).getCode()).isEqualTo(actionCode3);
		assertActionCode(menuDisplayList.get(2), 3);
		assertThat(menuDisplayList.get(3).getCode()).isEqualTo(containerCode1);
		assertContainerCode((MenuDisplayContainer) menuDisplayList.get(3), 1);
		assertThat(menuDisplayList.get(4).getCode()).isEqualTo(actionCode2);
		assertThat(menuDisplayList.get(4).getParentCode()).isEqualTo(containerCode1);
		assertThat(menuDisplayList.get(4).isActive()).isEqualTo(false);

		List<MenuDisplayItem> menuDisplayRootMenuList = actionDisplayListSchemaType1.getRootMenuList();

		assertThat(menuDisplayRootMenuList).hasSize(4);

		assertThat(menuDisplayList.get(0).getCode()).isEqualTo(actionCode4);
		assertThat(menuDisplayList.get(1).getCode()).isEqualTo(actionCode1);
		assertThat(menuDisplayList.get(2).getCode()).isEqualTo(actionCode3);
		assertThat(menuDisplayList.get(3).getCode()).isEqualTo(containerCode1);
		MenuDisplayItem menuDisplayItemActionCode2 = actionDisplayListSchemaType1.getSubMenu(containerCode1).get(0);
		assertThat(menuDisplayItemActionCode2.getCode()).isEqualTo(actionCode2);
		assertActionCode(menuDisplayItemActionCode2, 2);

	}


	@Test
	public void givenTransactionWithInsertAfterElementInTransactionItSelfThenItIsAddedAtTheRightPosiition()
			throws ValidationException {
		givenCollection("collection1");

		MenusDisplayTransaction menusDisplayTransaction = new MenusDisplayTransaction();
		MenuDisplayItem actionsDisplay1 = new MenuDisplayItem(actionCode1, actionIcon1, i18nKey1);
		MenuDisplayItem actionsDisplay2 = new MenuDisplayItem(actionCode2, actionIcon1, i18nKey1);

		menusDisplayTransaction.addElement(Action.ADD_UPDATE, schemaType1, actionsDisplay1, MenuPositionActionOptions.displayActionAtEnd());
		menusDisplayTransaction.addElement(Action.ADD_UPDATE, schemaType1, actionsDisplay2, MenuPositionActionOptions.displayActionAfter(actionCode1));

		manager.execute(collection1, menusDisplayTransaction);

		List<MenuDisplayItem> menuDisplayList = manager.getMenuDisplayList(collection1).getActionDisplayList(schemaType1).getRawMenus();
		assertThat(menuDisplayList).hasSize(2);

		assertThat(menuDisplayList.get(0).getCode()).isEqualTo(actionCode1);
		assertThat(menuDisplayList.get(1).getCode()).isEqualTo(actionCode2);
	}

	@Test
	public void givenMenuDisplayListRemoveElement() throws ValidationException {
		givenCollection("collection1");

		List<MenuDisplayItem> menuDisplayItemList1 = getBasicMenuDisplayList();

		manager.withActionsDisplay(collection1, schemaType1, menuDisplayItemList1);

		MenusDisplayTransaction menusDisplayTransaction = new MenusDisplayTransaction();
		menusDisplayTransaction.addElement(Action.REMOVE, schemaType1, new MenuDisplayItem(actionCode2), null);

		manager.execute(collection1, menusDisplayTransaction);

		MenuDisplayList menuDisplayList = manager.getMenuDisplayList(collection1).getActionDisplayList(schemaType1);

		List<MenuDisplayItem> menuDisplayItemList = menuDisplayList.getRawMenus();

		assertThat(menuDisplayItemList).hasSize(2);
		assertThat(menuDisplayItemList.get(0).getCode()).isEqualTo(actionCode1);
		assertThat(menuDisplayItemList.get(1).getCode()).isEqualTo(containerCode1);


		MenusDisplayTransaction menusDisplayTransaction2 = new MenusDisplayTransaction();
		menusDisplayTransaction2.addElement(Action.REMOVE, schemaType1, new MenuDisplayItem(actionCode1), null);
		menusDisplayTransaction2.addElement(Action.REMOVE, schemaType1, new MenuDisplayItem(containerCode1), null);

		manager.execute(collection1, menusDisplayTransaction2);
		assertThat(manager.getMenuDisplayList(collection1).getActionDisplayList(schemaType1)).isNull();
	}

	@Test
	public void testUpdateItemWithAddUpdate() throws ValidationException {
		givenCollection("collection1");

		List<MenuDisplayItem> menuDisplayItemList1 = getBasicMenuDisplayList();

		manager.withActionsDisplay(collection1, schemaType1, menuDisplayItemList1);

		assertBasicMenuDisplayList(collection1, schemaType1);

		MenusDisplayTransaction menusDisplayTransaction = new MenusDisplayTransaction();

		MenuDisplayItem menuDisplayItem = menuDisplayItemList1.get(0);

		final String NEW_ICON = "newICon22";

		menuDisplayItem = menuDisplayItem.newMenuDisplayItemWithIcon(NEW_ICON);

		menusDisplayTransaction.addElement(Action.ADD_UPDATE, schemaType1, menuDisplayItem, MenuPositionActionOptions.displayActionAfter(actionCode2));

		manager.execute(collection1, menusDisplayTransaction);

		MenuDisplayList menuDisplayList = manager.getMenuDisplayList(collection1).getActionDisplayList(schemaType1);
		List<MenuDisplayItem> menuDisplayItemList = menuDisplayList.getRawMenus();

		assertThat(menuDisplayItemList).hasSize(3);
		assertThat(menuDisplayItemList.get(0).getCode()).isEqualTo(containerCode1);
		assertThat(menuDisplayItemList.get(1).getCode()).isEqualTo(actionCode2);
		assertThat(menuDisplayItemList.get(2).getCode()).isEqualTo(actionCode1);
		assertThat(menuDisplayItemList.get(2).getIcon()).isEqualTo(NEW_ICON);
	}

	@Test
	public void givenSchemaWithMenuDisplayGivenEmptyListThenOnSchemaGetListReturnNull() throws ValidationException {
		givenCollection("collection1");

		List<MenuDisplayItem> menuDisplayItemList1 = getBasicMenuDisplayList();

		manager.withActionsDisplay(collection1, schemaType1, menuDisplayItemList1);
		manager.withActionsDisplay(collection1, schemaType2, getBasicMenuDisplayList());

		assertThat(manager.getMenuDisplayList(collection1).getActionDisplayList(schemaType1).getRawMenus()).hasSize(3);
		assertThat(manager.getMenuDisplayList(collection1).getActionDisplayList(schemaType2).getRawMenus()).hasSize(3);

		manager.withActionsDisplay(collection1, schemaType1, new ArrayList<>());

		assertThat(manager.getMenuDisplayList(collection1).getActionDisplayList(schemaType1)).isNull();
		assertThat(manager.getMenuDisplayList(collection1).getActionDisplayList(schemaType2).getRawMenus()).hasSize(3);
	}

	@Test
	public void givenMenuDisplayItemWithNoCodeThenRequire() {
		givenCollection("collection1");

		List<MenuDisplayItem> menuDisplayItemList1 = new ArrayList<>();
		menuDisplayItemList1.add(new MenuDisplayItem(null, actionIcon1, i18nKey1));

		try {
			manager.withActionsDisplay(collection1, schemaType1, menuDisplayItemList1);
			fail("Action with inexisting parent should throw.");
		} catch (ValidationException error) {
			assertThat(error.getValidationErrors().getValidationErrors().get(0).getCode()).isEqualTo("com.constellio.app.services.actionDisplayManager.MenusDisplayManager_valueRequired");
			assertThat(error.getValidationErrors().getValidationErrors().get(0).getParameter("code")).isEqualTo("code");
		}
	}

	@Test
	public void givenMenuDisplayItemWithNoIconThenRequire() {
		givenCollection("collection1");

		List<MenuDisplayItem> menuDisplayItemList1 = new ArrayList<>();
		menuDisplayItemList1.add(new MenuDisplayItem("superCode", null, i18nKey1));

		try {
			manager.withActionsDisplay(collection1, schemaType1, menuDisplayItemList1);
			fail("Action with inexisting parent should throw.");
		} catch (ValidationException error) {
			assertThat(error.getValidationErrors().getValidationErrors().get(0).getCode()).isEqualTo("com.constellio.app.services.actionDisplayManager.MenusDisplayManager_valueRequired");
			assertThat(error.getValidationErrors().getValidationErrors().get(0).getParameter("code")).isEqualTo("icon");
		}
	}

	@Test
	public void givenMenuDisplayItemWithNoI18nKeyThenRequire() {
		givenCollection("collection1");

		List<MenuDisplayItem> menuDisplayItemList1 = new ArrayList<>();
		menuDisplayItemList1.add(new MenuDisplayItem("superCode", "icon", null));

		try {
			manager.withActionsDisplay(collection1, schemaType1, menuDisplayItemList1);
			fail("Action with inexisting parent should throw.");
		} catch (ValidationException error) {
			assertThat(error.getValidationErrors().getValidationErrors().get(0).getCode()).isEqualTo("com.constellio.app.services.actionDisplayManager.MenusDisplayManager_valueRequired");
			assertThat(error.getValidationErrors().getValidationErrors().get(0).getParameter("code")).isEqualTo("i18nKey");
		}
	}

	@Test
	public void givenMenuDisplayItemMethodGetQuickActionListReturnsNActions() {
		MenuDisplayListBySchemaType menuDisplayListBySchemaType = prepareMenuDisplayListForMethodGetQuickActionListTesting();

		List<String> expectedValues = Arrays.asList(actionCode1, actionCode2, actionCode3);

		assertThat(menuDisplayListBySchemaType.getActionDisplayList(schemaType1).getQuickActionList().stream().map(MenuDisplayItem::getCode).toArray())
				.containsExactly(expectedValues.toArray());

		assertThat(menuDisplayListBySchemaType.getActionDisplayList(schemaType1).getQuickActionList(2).stream().map(MenuDisplayItem::getCode).toArray())
				.containsExactly(expectedValues.subList(0, 2).toArray());

		assertThat(menuDisplayListBySchemaType.getActionDisplayList(schemaType2).getQuickActionList().stream().map(MenuDisplayItem::getCode).toArray())
				.containsExactly(expectedValues.toArray());

		assertThat(menuDisplayListBySchemaType.getActionDisplayList(schemaType2).getQuickActionList(2).stream().map(MenuDisplayItem::getCode).toArray())
				.containsExactly(expectedValues.subList(0, 2).toArray());
	}

	@Test
	public void testUnicityOfMenuItemCode() throws ValidationException {
		givenCollection("collection1");

		List<MenuDisplayItem> menuDisplayItemList1 = getBasicMenuDisplayList();
		menuDisplayItemList1.add(menuDisplayItemList1.get(0).newMenuDisplayItemWithIcon("icon"));

		try {
			manager.withActionsDisplay(collection1, schemaType1, menuDisplayItemList1);
			fail("action should throw code is already taken.");
		} catch (ValidationException error) {
			assertThat(error.getValidationErrors().getValidationErrors().get(0).getCode()).isEqualTo("com.constellio.app.services.actionDisplayManager.MenusDisplayManager_unicityCodeRule");
			assertThat(error.getValidationErrors().getValidationErrors().get(0).getParameter("code")).isEqualTo(actionCode1);
		}
	}

	@Test
	public void tryUpdatingContainerWithMenusDisplayItem() throws ValidationException {
		givenCollection("collection1");

		List<MenuDisplayItem> menuDisplayItemList1 = getBasicMenuDisplayList();
		manager.withActionsDisplay(collection1, schemaType1, menuDisplayItemList1);

		MenusDisplayTransaction menusDisplayTransaction = new MenusDisplayTransaction();
		MenuDisplayItem menuDisplayItem = new MenuDisplayItem(containerCode1, actionIcon3, i18nKey3);
		menusDisplayTransaction.addElement(Action.ADD_UPDATE, schemaType1, menuDisplayItem, MenuPositionActionOptions.displayActionAtEnd());

		try {
			manager.execute(collection1, menusDisplayTransaction);
			fail("action should throw code since we are updating a menu container with a menu item");
		} catch (ValidationException error) {
			assertThat(error.getValidationErrors().getValidationErrors().get(0).getCode()).isEqualTo("com.constellio.app.services.actionDisplayManager.MenusDisplayManager_cannotUpdateMenuDisplayContainerWithMenuDisplayItem");
			assertThat(error.getValidationErrors().getValidationErrors().get(0).getParameter("code")).isEqualTo(containerCode1);
		}
	}

	@Test
	public void tryUpdatingMenuDisplayItemWithMenusDisplayContainer() throws ValidationException {
		givenCollection("collection1");

		List<MenuDisplayItem> menuDisplayItemList1 = getBasicMenuDisplayList();
		manager.withActionsDisplay(collection1, schemaType1, menuDisplayItemList1);

		MenusDisplayTransaction menusDisplayTransaction = new MenusDisplayTransaction();
		MenuDisplayContainer menuDisplayContainer = new MenuDisplayContainer(actionCode1, containerLabel2, containerIcon2);
		menusDisplayTransaction.addElement(Action.ADD_UPDATE, schemaType1, menuDisplayContainer, MenuPositionActionOptions.displayActionAtEnd());

		try {
			manager.execute(collection1, menusDisplayTransaction);
			fail("action should throw code since we are updating a menu container with a menu item");
		} catch (ValidationException error) {
			assertThat(error.getValidationErrors().getValidationErrors().get(0).getCode()).isEqualTo("com.constellio.app.services.actionDisplayManager.MenusDisplayManager_cannotUpdateMenuDisplayItemWithMenuDisplayContainer");
			assertThat(error.getValidationErrors().getValidationErrors().get(0).getParameter("code")).isEqualTo(actionCode1);
		}
	}

	@Test
	public void tryremovingMenuItemContainerWithChildrenThenThrow() throws ValidationException {
		givenCollection("collection1");

		List<MenuDisplayItem> menuDisplayItemList1 = getBasicMenuDisplayList();

		manager.withActionsDisplay(collection1, schemaType1, menuDisplayItemList1);

		MenusDisplayTransaction menusDisplayTransaction = new MenusDisplayTransaction();
		menusDisplayTransaction.addElement(Action.REMOVE, schemaType1, new MenuDisplayItem(containerCode1), null);

		try {
			manager.execute(collection1, menusDisplayTransaction);
			fail("Action should fail. Container has a children");
		} catch (ValidationException error) {
			assertThat(error.getValidationErrors().getValidationErrors().get(0).getCode()).isEqualTo("com.constellio.app.services.actionDisplayManager.MenusDisplayManager_hasChildren");
			assertThat(error.getValidationErrors().getValidationErrors().get(0).getParameter("code")).isEqualTo(containerCode1);
		}
	}


	private ListAssert<Tuple> assertThatEventsReceivedOnZeInstance() {
		return assertThat(
				zeInstanceEventBus.newReceivedEventsOnBus("configManager"))
				.extracting("type", "data");
	}

	private ListAssert<Tuple> assertThatEventsSentFromZeInstance() {
		return assertThat(
				zeInstanceEventBus.newSentEventsOnBus("configManager"))
				.extracting("type", "data");
	}

	private MenuDisplayListBySchemaType prepareMenuDisplayListForMethodGetQuickActionListTesting() {
		givenCollection(collection1);

		try {
			manager.withActionsDisplay(collection1, schemaType1, Arrays.asList(
					new MenuDisplayItem(actionCode1, "icon", ""),
					new MenuDisplayItem(actionCode2, "icon", ""),
					new MenuDisplayItem(actionCode3, "icon", ""),
					new MenuDisplayItem(actionCode4, "icon", "")
			));

			manager.withActionsDisplay(collection1, schemaType2, Arrays.asList(
					new MenuDisplayContainer(containerCode1, containerLabel1, "icon"),
					new MenuDisplayItem(actionCode1, "icon", "", true, containerCode1, true),
					new MenuDisplayItem(actionCode2, "icon", "", true, containerCode1, true),
					new MenuDisplayItem(actionCode3, "icon", "", true),
					new MenuDisplayContainer(containerCode2, containerLabel2, "icon"),
					new MenuDisplayItem(actionCode4, "icon", "", true, containerCode2, true)
			));
		} catch (ValidationException error) {
			fail(error.getMessage());
		}

		return manager.getMenuDisplayList(collection1);
	}
}

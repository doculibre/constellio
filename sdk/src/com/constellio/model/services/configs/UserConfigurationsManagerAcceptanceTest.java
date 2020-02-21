package com.constellio.model.services.configs;

import com.constellio.app.services.collections.CollectionsManager;
import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.model.entities.configs.UserConfigurationType;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.structures.TableProperties;
import com.constellio.model.services.records.RecordServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.SlowTest;
import com.constellio.sdk.tests.setups.Users;
import org.junit.Before;
import org.junit.Test;

import static com.constellio.sdk.tests.TestUtils.linkEventBus;
import static org.assertj.core.api.Assertions.assertThat;

@SlowTest
public class UserConfigurationsManagerAcceptanceTest extends ConstellioTest {

	private String anotherCollection = "anotherCollection";

	private ConfigManager configManager;
	private CollectionsManager collectionManager;
	private RecordServices recordServices;
	private Users users;
	private static UserConfigurationsManager userConfigManager, otherInstanceUserConfigManager;
	private User bob;

	@Before
	public void setUp() throws Exception {
		configManager = getDataLayerFactory().getConfigManager();
		collectionManager = getAppLayerFactory().getCollectionsManager();
		recordServices = getModelLayerFactory().newRecordServices();
		users = new Users().setUp(getModelLayerFactory().newUserServices());

		userConfigManager = getModelLayerFactory().getUserConfigurationsManager();
		otherInstanceUserConfigManager = getModelLayerFactory("other-instance").getUserConfigurationsManager();
		linkEventBus(getModelLayerFactory(), getModelLayerFactory("other-instance"));

		givenSpecialCollection(zeCollection).withAllTestUsers();
		givenSpecialCollection(anotherCollection).withAllTestUsers();

		bob = users.bobIn(zeCollection);
	}

	@Test
	public void whenInitializingThenConfigsLoaded() throws Exception {
		String key = "TEST";
		String value = "test";

		userConfigManager.setValue(bob, key, UserConfigurationType.STRING, value);

		UserConfigurationsManager otherManager = new UserConfigurationsManager(configManager);
		otherManager.initialize();

		assertThat(otherManager.<String>getValue(bob, key, UserConfigurationType.STRING)).isEqualTo(value);
	}

	@Test
	public void whenDeletingUserThenConfigDeleted() throws Exception {
		userConfigManager.setValue(bob, "TEST", UserConfigurationType.STRING, "test");

		String filePath = userConfigManager.getFilePath(bob);
		assertThat(configManager.getProperties(filePath)).isNotNull();

		recordServices.logicallyDelete(bob.getWrappedRecord(), User.GOD);
		recordServices.physicallyDelete(bob.getWrappedRecord(), User.GOD);

		assertThat(configManager.getProperties(filePath)).isNull();
	}

	@Test
	public void whenDeletingCollectionThenConfigDeleted() throws Exception {
		userConfigManager.setValue(bob, "TEST", UserConfigurationType.STRING, "test");

		String filePath = userConfigManager.getFilePath(bob);
		assertThat(configManager.getProperties(filePath)).isNotNull();

		collectionManager.deleteCollection(anotherCollection);

		assertThat(configManager.getProperties(filePath)).isNull();
	}

	@Test
	public void givenDifferentUsersThenDifferentValues() throws Exception {
		String key = "TEST";
		User chuck = users.chuckNorrisIn(zeCollection);

		userConfigManager.setValue(bob, key, UserConfigurationType.STRING, "bob");
		userConfigManager.setValue(chuck, key, UserConfigurationType.STRING, "chuck");

		assertThat(userConfigManager.<String>getValue(bob, key, UserConfigurationType.STRING)).isEqualTo("bob");
		assertThat(userConfigManager.<String>getValue(chuck, key, UserConfigurationType.STRING)).isEqualTo("chuck");
	}

	@Test
	public void givenDifferentCollectionsThenDifferentValues() throws Exception {
		String key = "TEST";
		User bobInAnotherCollection = users.bobIn(anotherCollection);

		userConfigManager.setValue(bob, key, UserConfigurationType.STRING, zeCollection);
		userConfigManager.setValue(bobInAnotherCollection, key, UserConfigurationType.STRING, anotherCollection);

		assertThat(userConfigManager.<String>getValue(bob, key, UserConfigurationType.STRING)).isEqualTo(zeCollection);
		assertThat(userConfigManager.<String>getValue(bobInAnotherCollection, key, UserConfigurationType.STRING)).isEqualTo(anotherCollection);
	}

	@Test
	public void givenStringMetadataThenCanRetrieveAndAlterValue() throws Exception {
		String key = "TEST";
		String value = "test";

		assertThat(userConfigManager.<String>getValue(bob, key, UserConfigurationType.STRING)).isNull();
		assertThat(otherInstanceUserConfigManager.<String>getValue(bob, key, UserConfigurationType.STRING)).isNull();

		userConfigManager.setValue(bob, key, UserConfigurationType.STRING, value);

		assertThat(userConfigManager.<String>getValue(bob, key, UserConfigurationType.STRING)).isEqualTo(value);
		assertThat(otherInstanceUserConfigManager.<String>getValue(bob, key, UserConfigurationType.STRING)).isEqualTo(value);

		value = "testUpdated";
		otherInstanceUserConfigManager.setValue(bob, key, UserConfigurationType.STRING, value);

		assertThat(otherInstanceUserConfigManager.<String>getValue(bob, key, UserConfigurationType.STRING)).isEqualTo(value);
		assertThat(userConfigManager.<String>getValue(bob, key, UserConfigurationType.STRING)).isEqualTo(value);
	}

	@Test
	public void givenIntegerMetadataThenCanRetrieveAndAlterValue() throws Exception {
		String key = "TEST";
		Integer value = 111;

		assertThat(userConfigManager.<Integer>getValue(bob, key, UserConfigurationType.INTEGER)).isNull();
		assertThat(otherInstanceUserConfigManager.<Integer>getValue(bob, key, UserConfigurationType.INTEGER)).isNull();

		userConfigManager.setValue(bob, key, UserConfigurationType.INTEGER, value);

		assertThat(userConfigManager.<Integer>getValue(bob, key, UserConfigurationType.INTEGER)).isEqualTo(value);
		assertThat(otherInstanceUserConfigManager.<Integer>getValue(bob, key, UserConfigurationType.INTEGER)).isEqualTo(value);

		value = 222;
		otherInstanceUserConfigManager.setValue(bob, key, UserConfigurationType.INTEGER, value);

		assertThat(otherInstanceUserConfigManager.<Integer>getValue(bob, key, UserConfigurationType.INTEGER)).isEqualTo(value);
		assertThat(userConfigManager.<Integer>getValue(bob, key, UserConfigurationType.INTEGER)).isEqualTo(value);
	}

	@Test
	public void givenBooleanMetadataThenCanRetrieveAndAlterValue() throws Exception {
		String key = "TEST";
		Boolean value = false;

		assertThat(userConfigManager.<Boolean>getValue(bob, key, UserConfigurationType.BOOLEAN)).isNull();
		assertThat(otherInstanceUserConfigManager.<Boolean>getValue(bob, key, UserConfigurationType.BOOLEAN)).isNull();

		userConfigManager.setValue(bob, key, UserConfigurationType.BOOLEAN, value);

		assertThat(userConfigManager.<Boolean>getValue(bob, key, UserConfigurationType.BOOLEAN)).isEqualTo(value);
		assertThat(otherInstanceUserConfigManager.<Boolean>getValue(bob, key, UserConfigurationType.BOOLEAN)).isEqualTo(value);

		value = true;
		otherInstanceUserConfigManager.setValue(bob, key, UserConfigurationType.BOOLEAN, value);

		assertThat(otherInstanceUserConfigManager.<Boolean>getValue(bob, key, UserConfigurationType.BOOLEAN)).isEqualTo(value);
		assertThat(userConfigManager.<Boolean>getValue(bob, key, UserConfigurationType.BOOLEAN)).isEqualTo(value);
	}

	@Test
	public void givenTablePropertiesMetadataThenCanRetrieveAndAlterValue() throws Exception {
		String tableId = "fakeId";
		String columnId = "fakeColumnId";
		TableProperties value = new TableProperties(tableId);

		TableProperties valueFromManager = userConfigManager.getTablePropertiesValue(bob, tableId);
		assertThat(valueFromManager).isNotNull();
		assertThat(valueFromManager.getTableId()).isEqualTo(tableId);
		assertThat(valueFromManager.getVisibleColumnIds()).isNull();
		assertThat(valueFromManager.getSortedAscending()).isNull();
		assertThat(valueFromManager.getSortedColumnId()).isNull();
		assertThat(valueFromManager.getColumnWidths()).isNull();
		assertThat(valueFromManager.getColumnWidth(columnId)).isNull();

		TableProperties valueFromOtherManager = otherInstanceUserConfigManager.getTablePropertiesValue(bob, tableId);
		assertThat(valueFromOtherManager).isNotNull();
		assertThat(valueFromOtherManager.getTableId()).isEqualTo(tableId);
		assertThat(valueFromOtherManager.getVisibleColumnIds()).isNull();
		assertThat(valueFromOtherManager.getSortedAscending()).isNull();
		assertThat(valueFromOtherManager.getSortedColumnId()).isNull();
		assertThat(valueFromOtherManager.getColumnWidths()).isNull();
		assertThat(valueFromOtherManager.getColumnWidth(columnId)).isNull();

		value.setSortedAscending(true);
		userConfigManager.setTablePropertiesValue(bob, tableId, value);

		valueFromManager = userConfigManager.getTablePropertiesValue(bob, tableId);
		assertThat(valueFromManager).isNotNull();
		assertThat(valueFromManager.getTableId()).isEqualTo(tableId);
		assertThat(valueFromManager.getVisibleColumnIds()).isNull();
		assertThat(valueFromManager.getSortedAscending()).isEqualTo(true);
		assertThat(valueFromManager.getSortedColumnId()).isNull();
		assertThat(valueFromManager.getColumnWidths()).isNull();
		assertThat(valueFromManager.getColumnWidth(columnId)).isNull();

		valueFromOtherManager = otherInstanceUserConfigManager.getTablePropertiesValue(bob, tableId);
		assertThat(valueFromOtherManager).isNotNull();
		assertThat(valueFromOtherManager.getTableId()).isEqualTo(tableId);
		assertThat(valueFromOtherManager.getVisibleColumnIds()).isNull();
		assertThat(valueFromOtherManager.getSortedAscending()).isEqualTo(true);
		assertThat(valueFromOtherManager.getSortedColumnId()).isNull();
		assertThat(valueFromOtherManager.getColumnWidths()).isNull();
		assertThat(valueFromOtherManager.getColumnWidth(columnId)).isNull();

		value.setSortedColumnId(columnId);
		otherInstanceUserConfigManager.setTablePropertiesValue(bob, tableId, value);

		valueFromManager = userConfigManager.getTablePropertiesValue(bob, tableId);
		assertThat(valueFromManager).isNotNull();
		assertThat(valueFromManager.getTableId()).isEqualTo(tableId);
		assertThat(valueFromManager.getVisibleColumnIds()).isNull();
		assertThat(valueFromManager.getSortedAscending()).isEqualTo(true);
		assertThat(valueFromManager.getSortedColumnId()).isEqualTo(columnId);
		assertThat(valueFromManager.getColumnWidths()).isNull();
		assertThat(valueFromManager.getColumnWidth(columnId)).isNull();

		valueFromOtherManager = otherInstanceUserConfigManager.getTablePropertiesValue(bob, tableId);
		assertThat(valueFromOtherManager).isNotNull();
		assertThat(valueFromOtherManager.getTableId()).isEqualTo(tableId);
		assertThat(valueFromOtherManager.getVisibleColumnIds()).isNull();
		assertThat(valueFromOtherManager.getSortedAscending()).isEqualTo(true);
		assertThat(valueFromOtherManager.getSortedColumnId()).isEqualTo(columnId);
		assertThat(valueFromOtherManager.getColumnWidths()).isNull();
		assertThat(valueFromOtherManager.getColumnWidth(columnId)).isNull();
	}

	@Test
	public void givenMultipleMetadataThenCanRetrieveAndAlterValue() throws Exception {
		String stringKey = "STRING_KEY";
		String stringValue = "test";
		String integerKey = "INTEGER_KEY";
		Integer integerValue = 111;
		String booleanKey = "BOOLEAN_KEY";
		Boolean booleanValue = true;
		String tableId = "tableId";
		String otherTableId = "otherTableId";
		String columnId = "fakeColumnId";

		userConfigManager.setValue(bob, stringKey, UserConfigurationType.STRING, stringValue);
		userConfigManager.setValue(bob, integerKey, UserConfigurationType.INTEGER, integerValue);
		userConfigManager.setValue(bob, booleanKey, UserConfigurationType.BOOLEAN, booleanValue);
		userConfigManager.setTablePropertiesValue(bob, tableId, new TableProperties(tableId));
		userConfigManager.setTablePropertiesValue(bob, otherTableId, new TableProperties(otherTableId));

		assertThat(userConfigManager.<String>getValue(bob, stringKey, UserConfigurationType.STRING)).isEqualTo(stringValue);
		assertThat(userConfigManager.<Integer>getValue(bob, integerKey, UserConfigurationType.INTEGER)).isEqualTo(integerValue);
		assertThat(userConfigManager.<Boolean>getValue(bob, booleanKey, UserConfigurationType.BOOLEAN)).isEqualTo(booleanValue);

		TableProperties tableValueFromManager = userConfigManager.getTablePropertiesValue(bob, tableId);
		assertThat(tableValueFromManager).isNotNull();
		assertThat(tableValueFromManager.getTableId()).isEqualTo(tableId);

		TableProperties otherTableValueFromManager = userConfigManager.getTablePropertiesValue(bob, otherTableId);
		assertThat(otherTableValueFromManager).isNotNull();
		assertThat(otherTableValueFromManager.getTableId()).isEqualTo(otherTableId);

		assertThat(otherInstanceUserConfigManager.<String>getValue(bob, stringKey, UserConfigurationType.STRING)).isEqualTo(stringValue);
		assertThat(otherInstanceUserConfigManager.<Integer>getValue(bob, integerKey, UserConfigurationType.INTEGER)).isEqualTo(integerValue);
		assertThat(otherInstanceUserConfigManager.<Boolean>getValue(bob, booleanKey, UserConfigurationType.BOOLEAN)).isEqualTo(booleanValue);

		TableProperties tableValueFromOtherManager = otherInstanceUserConfigManager.getTablePropertiesValue(bob, tableId);
		assertThat(tableValueFromOtherManager).isNotNull();
		assertThat(tableValueFromOtherManager.getTableId()).isEqualTo(tableId);

		TableProperties otherTableValueFromOtherManager = otherInstanceUserConfigManager.getTablePropertiesValue(bob, otherTableId);
		assertThat(otherTableValueFromOtherManager).isNotNull();
		assertThat(otherTableValueFromOtherManager.getTableId()).isEqualTo(otherTableId);

		stringValue = "testUpdated";
		integerValue = 222;
		booleanValue = false;
		tableValueFromManager.setSortedColumnId(columnId);
		otherTableValueFromManager.setSortedColumnId(columnId);

		userConfigManager.setValue(bob, stringKey, UserConfigurationType.STRING, stringValue);
		userConfigManager.setValue(bob, integerKey, UserConfigurationType.INTEGER, integerValue);
		userConfigManager.setValue(bob, booleanKey, UserConfigurationType.BOOLEAN, booleanValue);
		userConfigManager.setTablePropertiesValue(bob, tableId, tableValueFromManager);
		userConfigManager.setTablePropertiesValue(bob, otherTableId, otherTableValueFromManager);

		assertThat(userConfigManager.<String>getValue(bob, stringKey, UserConfigurationType.STRING)).isEqualTo(stringValue);
		assertThat(userConfigManager.<Integer>getValue(bob, integerKey, UserConfigurationType.INTEGER)).isEqualTo(integerValue);
		assertThat(userConfigManager.<Boolean>getValue(bob, booleanKey, UserConfigurationType.BOOLEAN)).isEqualTo(booleanValue);

		tableValueFromManager = userConfigManager.getTablePropertiesValue(bob, tableId);
		assertThat(tableValueFromManager).isNotNull();
		assertThat(tableValueFromManager.getTableId()).isEqualTo(tableId);
		assertThat(tableValueFromManager.getSortedColumnId()).isEqualTo(columnId);

		otherTableValueFromManager = userConfigManager.getTablePropertiesValue(bob, otherTableId);
		assertThat(otherTableValueFromManager).isNotNull();
		assertThat(otherTableValueFromManager.getTableId()).isEqualTo(otherTableId);
		assertThat(otherTableValueFromManager.getSortedColumnId()).isEqualTo(columnId);

		assertThat(otherInstanceUserConfigManager.<String>getValue(bob, stringKey, UserConfigurationType.STRING)).isEqualTo(stringValue);
		assertThat(otherInstanceUserConfigManager.<Integer>getValue(bob, integerKey, UserConfigurationType.INTEGER)).isEqualTo(integerValue);
		assertThat(otherInstanceUserConfigManager.<Boolean>getValue(bob, booleanKey, UserConfigurationType.BOOLEAN)).isEqualTo(booleanValue);

		tableValueFromOtherManager = otherInstanceUserConfigManager.getTablePropertiesValue(bob, tableId);
		assertThat(tableValueFromOtherManager).isNotNull();
		assertThat(tableValueFromOtherManager.getTableId()).isEqualTo(tableId);
		assertThat(tableValueFromOtherManager.getSortedColumnId()).isEqualTo(columnId);

		otherTableValueFromOtherManager = otherInstanceUserConfigManager.getTablePropertiesValue(bob, otherTableId);
		assertThat(otherTableValueFromOtherManager).isNotNull();
		assertThat(otherTableValueFromOtherManager.getTableId()).isEqualTo(otherTableId);
		assertThat(otherTableValueFromOtherManager.getSortedColumnId()).isEqualTo(columnId);
	}

	@Test
	public void whenUpdatingTableVisibleColumnsThenValueUpdated() throws Exception {
		// TODO::JOLA (P3)
	}

	@Test
	public void whenUpdatingTableColumnWidthsThenValueUpdated() throws Exception {
		// TODO::JOLA (P3)
	}

	@Test
	public void whenUpdatingTableSortThenValueUpdated() throws Exception {
		// TODO::JOLA (P3)
	}
}

package com.constellio.app.modules.tasks.model;

import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.services.TasksSchemasRecordsServices;
import com.constellio.model.entities.schemas.RecordCacheType;
import com.constellio.model.services.records.RecordServices;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Test;

import static com.constellio.data.dao.dto.records.RecordDTOMode.FULLY_LOADED;
import static com.constellio.data.dao.dto.records.RecordDTOMode.SUMMARY;
import static com.constellio.model.services.records.GetRecordOptions.RETURNING_SUMMARY;
import static com.constellio.sdk.tests.TestUtils.assertThatSolrUsage;
import static org.assertj.core.api.Assertions.assertThat;

public class TaskRecordsCachesHookAcceptanceTest extends ConstellioTest {

	TasksSchemasRecordsServices tasks;

	@Test
	public void givenVolatileWhenCreatingModelTaskThenAlwaysFullyRetrievableUsingCache() throws Exception {

		prepareSystem(withZeCollection().withConstellioRMModule());

		tasks = new TasksSchemasRecordsServices(zeCollection, getAppLayerFactory());

		RecordServices recordServices = getModelLayerFactory().newRecordServices();

		Task t1 = modelTask();
		recordServices.add(t1);

		assertThatSolrUsage(getModelLayerFactory(), () -> {

			assertThat(recordServices.get(t1.getId(), RETURNING_SUMMARY).getRecordDTOMode()).isEqualTo(SUMMARY);
			assertThat(recordServices.get(t1.getId()).getRecordDTOMode()).isEqualTo(FULLY_LOADED);
			assertThat(recordServices.get(t1.getId(), RETURNING_SUMMARY).getVersion()).isEqualTo(t1.getVersion());
			assertThat(recordServices.get(t1.getId()).getVersion()).isEqualTo(t1.getVersion());

			getModelLayerFactory().getRecordsCaches().invalidateVolatile();
			assertThat(recordServices.get(t1.getId(), RETURNING_SUMMARY).getRecordDTOMode()).isEqualTo(SUMMARY);
			assertThat(recordServices.get(t1.getId()).getRecordDTOMode()).isEqualTo(FULLY_LOADED);
			assertThat(recordServices.get(t1.getId(), RETURNING_SUMMARY).getVersion()).isEqualTo(t1.getVersion());
			assertThat(recordServices.get(t1.getId()).getVersion()).isEqualTo(t1.getVersion());

			assertThat(recordServices.get(t1.getId(), RETURNING_SUMMARY).getRecordDTOMode()).isEqualTo(SUMMARY);
			assertThat(recordServices.get(t1.getId()).getRecordDTOMode()).isEqualTo(FULLY_LOADED);
			assertThat(recordServices.get(t1.getId(), RETURNING_SUMMARY).getVersion()).isEqualTo(t1.getVersion());
			assertThat(recordServices.get(t1.getId()).getVersion()).isEqualTo(t1.getVersion());
		}).hasGetByIdsCountEqualTo(0);

		Task t1V2 = tasks.getTask(t1.getId()).setTitle("New title!");
		recordServices.update(t1V2);

		assertThatSolrUsage(getModelLayerFactory(), () -> {
			assertThat(recordServices.get(t1.getId(), RETURNING_SUMMARY).getRecordDTOMode()).isEqualTo(SUMMARY);
			assertThat(recordServices.get(t1.getId()).getRecordDTOMode()).isEqualTo(FULLY_LOADED);
			assertThat(recordServices.get(t1.getId(), RETURNING_SUMMARY).getVersion()).isEqualTo(t1V2.getVersion());
			assertThat(recordServices.get(t1.getId()).getVersion()).isEqualTo(t1V2.getVersion());

			getModelLayerFactory().getRecordsCaches().invalidateVolatile();
			assertThat(recordServices.get(t1.getId(), RETURNING_SUMMARY).getRecordDTOMode()).isEqualTo(SUMMARY);
			assertThat(recordServices.get(t1.getId()).getRecordDTOMode()).isEqualTo(FULLY_LOADED);
			assertThat(recordServices.get(t1.getId(), RETURNING_SUMMARY).getVersion()).isEqualTo(t1V2.getVersion());
			assertThat(recordServices.get(t1.getId()).getVersion()).isEqualTo(t1V2.getVersion());

			assertThat(recordServices.get(t1.getId(), RETURNING_SUMMARY).getRecordDTOMode()).isEqualTo(SUMMARY);
			assertThat(recordServices.get(t1.getId()).getRecordDTOMode()).isEqualTo(FULLY_LOADED);
			assertThat(recordServices.get(t1.getId(), RETURNING_SUMMARY).getVersion()).isEqualTo(t1V2.getVersion());
			assertThat(recordServices.get(t1.getId()).getVersion()).isEqualTo(t1V2.getVersion());
		}).hasGetByIdsCountEqualTo(0);

		restartLayers();
		RecordServices recordServices2 = getModelLayerFactory().newRecordServices();

		assertThatSolrUsage(getModelLayerFactory(), () -> {
			assertThat(recordServices2.get(t1.getId(), RETURNING_SUMMARY).getRecordDTOMode()).isEqualTo(SUMMARY);
			assertThat(recordServices2.get(t1.getId()).getRecordDTOMode()).isEqualTo(FULLY_LOADED);
			assertThat(recordServices2.get(t1.getId(), RETURNING_SUMMARY).getVersion()).isEqualTo(t1V2.getVersion());
			assertThat(recordServices2.get(t1.getId()).getVersion()).isEqualTo(t1V2.getVersion());

			getModelLayerFactory().getRecordsCaches().invalidateVolatile();
			assertThat(recordServices2.get(t1.getId(), RETURNING_SUMMARY).getRecordDTOMode()).isEqualTo(SUMMARY);
			assertThat(recordServices2.get(t1.getId()).getRecordDTOMode()).isEqualTo(FULLY_LOADED);
			assertThat(recordServices2.get(t1.getId(), RETURNING_SUMMARY).getVersion()).isEqualTo(t1V2.getVersion());
			assertThat(recordServices2.get(t1.getId()).getVersion()).isEqualTo(t1V2.getVersion());

			assertThat(recordServices2.get(t1.getId(), RETURNING_SUMMARY).getRecordDTOMode()).isEqualTo(SUMMARY);
			assertThat(recordServices2.get(t1.getId()).getRecordDTOMode()).isEqualTo(FULLY_LOADED);
			assertThat(recordServices2.get(t1.getId(), RETURNING_SUMMARY).getVersion()).isEqualTo(t1V2.getVersion());
			assertThat(recordServices2.get(t1.getId()).getVersion()).isEqualTo(t1V2.getVersion());
		}).hasGetByIdsCountEqualTo(1); //Model tasks are not loaded in cache at startup



	}


	//Disabling volatile allows us to make a better test
	@Test
	public void givenNoVolatileWhenCreatingModelTaskThenAlwaysFullyRetrievableUsingCache() throws Exception {

		prepareSystem(withZeCollection().withConstellioRMModule());

		getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, (types)->{
			types.getSchemaType(Task.SCHEMA_TYPE).setRecordCacheType(RecordCacheType.SUMMARY_CACHED_WITHOUT_VOLATILE);
		});

		tasks = new TasksSchemasRecordsServices(zeCollection, getAppLayerFactory());

		RecordServices recordServices = getModelLayerFactory().newRecordServices();

		Task t1 = modelTask();
		recordServices.add(t1);

		assertThatSolrUsage(getModelLayerFactory(), () -> {

			assertThat(recordServices.get(t1.getId(), RETURNING_SUMMARY).getRecordDTOMode()).isEqualTo(SUMMARY);
			assertThat(recordServices.get(t1.getId()).getRecordDTOMode()).isEqualTo(FULLY_LOADED);
			assertThat(recordServices.get(t1.getId(), RETURNING_SUMMARY).getVersion()).isEqualTo(t1.getVersion());
			assertThat(recordServices.get(t1.getId()).getVersion()).isEqualTo(t1.getVersion());

			getModelLayerFactory().getRecordsCaches().invalidateVolatile();
			assertThat(recordServices.get(t1.getId(), RETURNING_SUMMARY).getRecordDTOMode()).isEqualTo(SUMMARY);
			assertThat(recordServices.get(t1.getId()).getRecordDTOMode()).isEqualTo(FULLY_LOADED);
			assertThat(recordServices.get(t1.getId(), RETURNING_SUMMARY).getVersion()).isEqualTo(t1.getVersion());
			assertThat(recordServices.get(t1.getId()).getVersion()).isEqualTo(t1.getVersion());

			assertThat(recordServices.get(t1.getId(), RETURNING_SUMMARY).getRecordDTOMode()).isEqualTo(SUMMARY);
			assertThat(recordServices.get(t1.getId()).getRecordDTOMode()).isEqualTo(FULLY_LOADED);
			assertThat(recordServices.get(t1.getId(), RETURNING_SUMMARY).getVersion()).isEqualTo(t1.getVersion());
			assertThat(recordServices.get(t1.getId()).getVersion()).isEqualTo(t1.getVersion());
		}).hasGetByIdsCountEqualTo(0);

		Task t1V2 = tasks.getTask(t1.getId()).setTitle("New title!");
		recordServices.update(t1V2);

		assertThatSolrUsage(getModelLayerFactory(), () -> {
			assertThat(recordServices.get(t1.getId(), RETURNING_SUMMARY).getRecordDTOMode()).isEqualTo(SUMMARY);
			assertThat(recordServices.get(t1.getId()).getRecordDTOMode()).isEqualTo(FULLY_LOADED);
			assertThat(recordServices.get(t1.getId(), RETURNING_SUMMARY).getVersion()).isEqualTo(t1V2.getVersion());
			assertThat(recordServices.get(t1.getId()).getVersion()).isEqualTo(t1V2.getVersion());

			getModelLayerFactory().getRecordsCaches().invalidateVolatile();
			assertThat(recordServices.get(t1.getId(), RETURNING_SUMMARY).getRecordDTOMode()).isEqualTo(SUMMARY);
			assertThat(recordServices.get(t1.getId()).getRecordDTOMode()).isEqualTo(FULLY_LOADED);
			assertThat(recordServices.get(t1.getId(), RETURNING_SUMMARY).getVersion()).isEqualTo(t1V2.getVersion());
			assertThat(recordServices.get(t1.getId()).getVersion()).isEqualTo(t1V2.getVersion());

			assertThat(recordServices.get(t1.getId(), RETURNING_SUMMARY).getRecordDTOMode()).isEqualTo(SUMMARY);
			assertThat(recordServices.get(t1.getId()).getRecordDTOMode()).isEqualTo(FULLY_LOADED);
			assertThat(recordServices.get(t1.getId(), RETURNING_SUMMARY).getVersion()).isEqualTo(t1V2.getVersion());
			assertThat(recordServices.get(t1.getId()).getVersion()).isEqualTo(t1V2.getVersion());
		}).hasGetByIdsCountEqualTo(0);

		restartLayers();
		RecordServices recordServices2 = getModelLayerFactory().newRecordServices();

		assertThatSolrUsage(getModelLayerFactory(), () -> {
			assertThat(recordServices2.get(t1.getId(), RETURNING_SUMMARY).getRecordDTOMode()).isEqualTo(SUMMARY);
			assertThat(recordServices2.get(t1.getId()).getRecordDTOMode()).isEqualTo(FULLY_LOADED);
			assertThat(recordServices2.get(t1.getId(), RETURNING_SUMMARY).getVersion()).isEqualTo(t1V2.getVersion());
			assertThat(recordServices2.get(t1.getId()).getVersion()).isEqualTo(t1V2.getVersion());

			getModelLayerFactory().getRecordsCaches().invalidateVolatile();
			assertThat(recordServices2.get(t1.getId(), RETURNING_SUMMARY).getRecordDTOMode()).isEqualTo(SUMMARY);
			assertThat(recordServices2.get(t1.getId()).getRecordDTOMode()).isEqualTo(FULLY_LOADED);
			assertThat(recordServices2.get(t1.getId(), RETURNING_SUMMARY).getVersion()).isEqualTo(t1V2.getVersion());
			assertThat(recordServices2.get(t1.getId()).getVersion()).isEqualTo(t1V2.getVersion());

			assertThat(recordServices2.get(t1.getId(), RETURNING_SUMMARY).getRecordDTOMode()).isEqualTo(SUMMARY);
			assertThat(recordServices2.get(t1.getId()).getRecordDTOMode()).isEqualTo(FULLY_LOADED);
			assertThat(recordServices2.get(t1.getId(), RETURNING_SUMMARY).getVersion()).isEqualTo(t1V2.getVersion());
			assertThat(recordServices2.get(t1.getId()).getVersion()).isEqualTo(t1V2.getVersion());
		}).hasGetByIdsCountEqualTo(1); //Model tasks are not loaded in cache at startup



	}

	@Test
	public void whenCreatingNormalTaskThenAlwaysSummaryRetrievableUsingCache() throws Exception {

		prepareSystem(withZeCollection().withConstellioRMModule());

		tasks = new TasksSchemasRecordsServices(zeCollection, getAppLayerFactory());


		RecordServices recordServices = getModelLayerFactory().newRecordServices();

		Task t1 = normalTask();
		recordServices.add(t1);

		assertThatSolrUsage(getModelLayerFactory(), () -> {

			assertThat(recordServices.get(t1.getId(), RETURNING_SUMMARY).getRecordDTOMode()).isEqualTo(SUMMARY);
			assertThat(recordServices.get(t1.getId()).getRecordDTOMode()).isEqualTo(FULLY_LOADED);
			assertThat(recordServices.get(t1.getId(), RETURNING_SUMMARY).getVersion()).isEqualTo(t1.getVersion());
			assertThat(recordServices.get(t1.getId()).getVersion()).isEqualTo(t1.getVersion());

			getModelLayerFactory().getRecordsCaches().invalidateVolatile();
			assertThat(recordServices.get(t1.getId(), RETURNING_SUMMARY).getRecordDTOMode()).isEqualTo(SUMMARY);
			assertThat(recordServices.get(t1.getId()).getRecordDTOMode()).isEqualTo(FULLY_LOADED); //First getById
			assertThat(recordServices.get(t1.getId(), RETURNING_SUMMARY).getVersion()).isEqualTo(t1.getVersion());
			assertThat(recordServices.get(t1.getId()).getVersion()).isEqualTo(t1.getVersion());

			assertThat(recordServices.get(t1.getId(), RETURNING_SUMMARY).getRecordDTOMode()).isEqualTo(SUMMARY);
			assertThat(recordServices.get(t1.getId()).getRecordDTOMode()).isEqualTo(FULLY_LOADED);
			assertThat(recordServices.get(t1.getId(), RETURNING_SUMMARY).getVersion()).isEqualTo(t1.getVersion());
			assertThat(recordServices.get(t1.getId()).getVersion()).isEqualTo(t1.getVersion());
		}).hasGetByIdsCountEqualTo(1);

		Task t1V2 = tasks.getTask(t1.getId()).setTitle("New title!");
		recordServices.update(t1V2);

		assertThatSolrUsage(getModelLayerFactory(), () -> {
			assertThat(recordServices.get(t1.getId(), RETURNING_SUMMARY).getRecordDTOMode()).isEqualTo(SUMMARY);
			assertThat(recordServices.get(t1.getId()).getRecordDTOMode()).isEqualTo(FULLY_LOADED);
			assertThat(recordServices.get(t1.getId(), RETURNING_SUMMARY).getVersion()).isEqualTo(t1V2.getVersion());
			assertThat(recordServices.get(t1.getId()).getVersion()).isEqualTo(t1V2.getVersion());

			getModelLayerFactory().getRecordsCaches().invalidateVolatile();
			assertThat(recordServices.get(t1.getId(), RETURNING_SUMMARY).getRecordDTOMode()).isEqualTo(SUMMARY);
			assertThat(recordServices.get(t1.getId()).getRecordDTOMode()).isEqualTo(FULLY_LOADED); //First getById
			assertThat(recordServices.get(t1.getId(), RETURNING_SUMMARY).getVersion()).isEqualTo(t1V2.getVersion());
			assertThat(recordServices.get(t1.getId()).getVersion()).isEqualTo(t1V2.getVersion());

			assertThat(recordServices.get(t1.getId(), RETURNING_SUMMARY).getRecordDTOMode()).isEqualTo(SUMMARY);
			assertThat(recordServices.get(t1.getId()).getRecordDTOMode()).isEqualTo(FULLY_LOADED);
			assertThat(recordServices.get(t1.getId(), RETURNING_SUMMARY).getVersion()).isEqualTo(t1V2.getVersion());
			assertThat(recordServices.get(t1.getId()).getVersion()).isEqualTo(t1V2.getVersion());
		}).hasGetByIdsCountEqualTo(1);

		restartLayers();
		RecordServices recordServices2 = getModelLayerFactory().newRecordServices();

		assertThatSolrUsage(getModelLayerFactory(), () -> {
			assertThat(recordServices2.get(t1.getId(), RETURNING_SUMMARY).getRecordDTOMode()).isEqualTo(SUMMARY);
			assertThat(recordServices2.get(t1.getId()).getRecordDTOMode()).isEqualTo(FULLY_LOADED); //First getById
			assertThat(recordServices2.get(t1.getId(), RETURNING_SUMMARY).getVersion()).isEqualTo(t1V2.getVersion());
			assertThat(recordServices2.get(t1.getId()).getVersion()).isEqualTo(t1V2.getVersion());

			getModelLayerFactory().getRecordsCaches().invalidateVolatile();
			assertThat(recordServices2.get(t1.getId(), RETURNING_SUMMARY).getRecordDTOMode()).isEqualTo(SUMMARY);
			assertThat(recordServices2.get(t1.getId()).getRecordDTOMode()).isEqualTo(FULLY_LOADED); //Second getById
			assertThat(recordServices2.get(t1.getId(), RETURNING_SUMMARY).getVersion()).isEqualTo(t1V2.getVersion());
			assertThat(recordServices2.get(t1.getId()).getVersion()).isEqualTo(t1V2.getVersion());

			assertThat(recordServices2.get(t1.getId(), RETURNING_SUMMARY).getRecordDTOMode()).isEqualTo(SUMMARY);
			assertThat(recordServices2.get(t1.getId()).getRecordDTOMode()).isEqualTo(FULLY_LOADED);
			assertThat(recordServices2.get(t1.getId(), RETURNING_SUMMARY).getVersion()).isEqualTo(t1V2.getVersion());
			assertThat(recordServices2.get(t1.getId()).getVersion()).isEqualTo(t1V2.getVersion());
		}).hasGetByIdsCountEqualTo(2);
	}

	private Task modelTask() {

		return tasks.newTask().setTitle("Ze title").setDescription("Ze description").setModel(true);
	}

	private Task normalTask() {

		return tasks.newTask().setDescription("Ze description").setTitle("Ze title");
	}


}

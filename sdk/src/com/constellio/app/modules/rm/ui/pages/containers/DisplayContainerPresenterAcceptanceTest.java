package com.constellio.app.modules.rm.ui.pages.containers;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static junit.framework.Assert.fail;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Locale;

import com.constellio.sdk.tests.MockedNavigation;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.ui.application.CoreViews;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;

public class DisplayContainerPresenterAcceptanceTest extends ConstellioTest {
	@Mock DisplayContainerView view;
	MockedNavigation navigator;

	private RMTestRecords records = new RMTestRecords(zeCollection);
	private DisplayContainerPresenter presenter;
	private SessionContext sessionContext;
	private RecordVO recordVO;
	private RecordServices recordServices;
	private ContainerRecord container;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers().withRMTest(records)
						.withFoldersAndContainersOfEveryStatus().withEvents()
		);

		sessionContext = FakeSessionContext.adminInCollection(zeCollection);
		sessionContext.setCurrentLocale(Locale.FRENCH);

		recordServices = getModelLayerFactory().newRecordServices();

		when(view.getSessionContext()).thenReturn(sessionContext);
		when(view.getCollection()).thenReturn(zeCollection);
		when(view.getConstellioFactories()).thenReturn(getConstellioFactories());
		when(view.navigate()).thenReturn(navigator);

		presenter = new DisplayContainerPresenter(view);//spy();
		container = records.getContainerBac01();
		List<Record> folders = getRecordsInContainer(container.getId());
		assertThat(folders.size()).isEqualTo(2);
		assertThat(folders.get(0).getId()).isIn(new String[] { records.folder_C50, records.folder_C55 });
		assertThat(folders.get(1).getId()).isIn(new String[] { records.folder_C50, records.folder_C55 });
		recordVO = new RecordToVOBuilder().build(container.getWrappedRecord(), RecordVO.VIEW_MODE.FORM, sessionContext);
	}

	@Test
	public void givenContainerWithEnteredFillRatioThenReturnFillRatio()
			throws Exception, RecordInContainerWithoutLinearMeasure, ContainerWithoutCapacityException {
		Double expectedRatio = 20d;
		container.setFillRatioEntered(expectedRatio);
		recordVO = new RecordToVOBuilder().build(container.getWrappedRecord(), RecordVO.VIEW_MODE.FORM, sessionContext);
		Double ratio = presenter.getFillRatio(recordVO);
		assertThat(ratio).isEqualTo(expectedRatio);
	}

	@Test
	public void givenContainerWithoutCapacityThenThrowException()
			throws Exception {
		try {
			presenter.getFillRatio(recordVO);
			fail("should fail");
		} catch (ContainerWithoutCapacityException e) {
		} catch (RecordInContainerWithoutLinearMeasure recordInContainerWithoutLinearMeasure) {
			fail("should return valid exception");
		}
	}

	@Test
	public void givenContainerWithMissingLinearMeasureInAllRecordsThenThrowException()
			throws Exception {
		container.setCapacity(100d);
		recordVO = new RecordToVOBuilder().build(container.getWrappedRecord(), RecordVO.VIEW_MODE.FORM, sessionContext);
		try {
			presenter.getFillRatio(recordVO);
			fail("should fail");
		} catch (ContainerWithoutCapacityException e) {
			fail("should return valid exception");
		} catch (RecordInContainerWithoutLinearMeasure recordInContainerWithoutLinearMeasure) {
		}
	}

	@Test
	public void givenContainerWithOneMissingLinearMeasureThenThrowException()
			throws Exception {
		container.setCapacity(10d);
		recordServices.update(records.getFolder_C50().setLinearSize(4d));
		recordVO = new RecordToVOBuilder().build(container.getWrappedRecord(), RecordVO.VIEW_MODE.FORM, sessionContext);
		try {
			presenter.getFillRatio(recordVO);
			fail("should fail");
		} catch (ContainerWithoutCapacityException e) {
			fail("should return valid exception");
		} catch (RecordInContainerWithoutLinearMeasure recordInContainerWithoutLinearMeasure) {
		}
	}

	@Test
	public void givenContainerWithoutFoldersThenReturnZero()
			throws Exception, RecordInContainerWithoutLinearMeasure, ContainerWithoutCapacityException {
		Double expectedRatio = 0d;
		container.setCapacity(10d);

		recordServices.update(records.getFolder_C50().set(Folder.CONTAINER, null));
		recordServices.update(records.getFolder_C55().set(Folder.CONTAINER, null));
		recordVO = new RecordToVOBuilder().build(container.getWrappedRecord(), RecordVO.VIEW_MODE.FORM, sessionContext);
		Double ratio = presenter.getFillRatio(recordVO);
		assertThat(ratio).isEqualTo(expectedRatio);
	}

	private List<Record> getRecordsInContainer(String id) {
		RMSchemasRecordsServices schemas = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		Metadata containerMetadata = schemas.folderSchemaType().getDefaultSchema().getMetadata(Folder.CONTAINER);
		LogicalSearchCondition condition = from(schemas.folderSchemaType()).where(containerMetadata).isEqualTo(id);
		LogicalSearchQuery query = new LogicalSearchQuery(condition);
		return getModelLayerFactory().newSearchServices().search(query);
	}

	@Test
	public void givenContainerWithAllRecordsHavingLinearMeasureAndThenThrowReturnValidRatio()
			throws Exception, RecordInContainerWithoutLinearMeasure, ContainerWithoutCapacityException {
		Double expectedRatio = 60d;
		container.setCapacity(10d);
		recordServices.update(records.getFolder_C55().setLinearSize(2d));
		recordServices.update(records.getFolder_C50().setLinearSize(4d));
		recordVO = new RecordToVOBuilder().build(container.getWrappedRecord(), RecordVO.VIEW_MODE.FORM, sessionContext);
		Double ratio = presenter.getFillRatio(recordVO);
		assertThat(ratio).isEqualTo(expectedRatio);
	}
}

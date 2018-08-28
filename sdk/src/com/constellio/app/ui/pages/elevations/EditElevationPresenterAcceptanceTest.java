package com.constellio.app.ui.pages.elevations;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.model.services.search.QueryElevation.DocElevation;
import com.constellio.model.services.search.SearchConfigurationsManager;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.MockedNavigation;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class EditElevationPresenterAcceptanceTest extends ConstellioTest {
	protected RMTestRecords records = new RMTestRecords(zeCollection);
	protected SearchConfigurationsManager searchConfigurationsManager;
	protected EditElevationPresenter presenter;

	@Mock
	protected EditElevationView view;

	protected MockedNavigation navigator;

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(
				withZeCollection().withConstellioRMModule().withRMTest(records).withFoldersAndContainersOfEveryStatus()
		);

		navigator = new MockedNavigation();

		when(view.getConstellioFactories()).thenReturn(getConstellioFactories());
		when(view.getSessionContext()).thenReturn(FakeSessionContext.adminInCollection(zeCollection));
		when(view.navigate()).thenReturn(navigator);

		presenter = new EditElevationPresenter(view);
		searchConfigurationsManager = getModelLayerFactory().getSearchConfigurationsManager();
	}

	@Test
	public void givenElevatedQueryThenListed() {
		String textQuery = "textQuery";
		String idRecord = "idRecord";

		searchConfigurationsManager.setElevated(zeCollection, textQuery, idRecord);

		assertThat(presenter.getAllQuery()).isNotEmpty().contains(textQuery);

		List<DocElevation> elevations = presenter.getElevations(textQuery);
		assertThat(elevations).isNotEmpty().hasSize(1);

		assertThat(elevations.get(0).getId()).isEqualTo(idRecord);
	}

	@Test
	public void givenExcludedIdRecordThenListed() {
		String idRecord = "idRecord";

		searchConfigurationsManager.setExcluded(zeCollection, idRecord);

		assertThat(presenter.getExclusions()).isNotEmpty().contains(idRecord);
	}

	@Test
	public void givenExcludedIdRecordWhenExclusionCancelledThenRemoved() {
		String idRecord = "idRecord";

		searchConfigurationsManager.setExcluded(zeCollection, idRecord);

		assertThat(presenter.getExclusions()).isNotEmpty().contains(idRecord);

		presenter.cancelDocExclusionButtonClicked(idRecord);

		assertThat(presenter.getExclusions()).isNullOrEmpty();
	}

	@Test
	public void givenElevatedQueryWhenCancelledThenRemoved() {
		String textQuery = "textQuery";
		String idRecord = "idRecord";

		searchConfigurationsManager.setElevated(zeCollection, textQuery, idRecord);

		List<DocElevation> elevations = presenter.getElevations(textQuery);
		assertThat(elevations).isNotEmpty().hasSize(1);

		assertThat(elevations.get(0).getId()).isEqualTo(idRecord);

		String textQueryToRemove = "textQueryToRemove";
		String idRecordToRemove = "idRecordToRemove";

		searchConfigurationsManager.setElevated(zeCollection, textQueryToRemove, idRecordToRemove);

		elevations = presenter.getElevations(textQueryToRemove);
		assertThat(elevations).isNotEmpty().hasSize(1);

		assertThat(elevations.get(0).getId()).isEqualTo(idRecordToRemove);

		assertThat(presenter.getAllQuery()).isNotEmpty().contains(textQuery, textQueryToRemove);

		presenter.cancelQueryElevationButtonClicked(textQueryToRemove);

		assertThat(presenter.getAllQuery()).isNotEmpty().contains(textQuery).doesNotContain(textQueryToRemove);
	}

	@Test
	public void givenElevatedQueryWhenCancelAllThenRemoved() {
		String textQuery = "textQuery";
		String idRecord = "idRecord";

		searchConfigurationsManager.setElevated(zeCollection, textQuery, idRecord);

		List<DocElevation> elevations = presenter.getElevations(textQuery);
		assertThat(elevations).isNotEmpty().hasSize(1);

		assertThat(elevations.get(0).getId()).isEqualTo(idRecord);

		String textQuery1 = "textQuery1";
		String idRecord1 = "idRecord1";

		searchConfigurationsManager.setElevated(zeCollection, textQuery1, idRecord1);

		elevations = presenter.getElevations(textQuery1);
		assertThat(elevations).isNotEmpty().hasSize(1);

		assertThat(elevations.get(0).getId()).isEqualTo(idRecord1);

		assertThat(presenter.getAllQuery()).isNotEmpty().contains(textQuery, textQuery1);

		presenter.cancelAllElevationButtonClicked();

		assertThat(presenter.getAllQuery()).isNullOrEmpty();
	}

	@Test
	public void givenElevatedQueryWhenCancelDocThenRemoved() {
		String textQuery = "textQuery";
		String idRecord = "idRecord";

		searchConfigurationsManager.setElevated(zeCollection, textQuery, idRecord);

		List<DocElevation> elevations = presenter.getElevations(textQuery);
		assertThat(elevations).isNotEmpty().hasSize(1);

		DocElevation docElevation = elevations.get(0);
		assertThat(docElevation.getId()).isEqualTo(idRecord);

		presenter.cancelDocElevationButtonClicked(docElevation);

		assertThat(presenter.getAllQuery()).isNullOrEmpty();
	}
}

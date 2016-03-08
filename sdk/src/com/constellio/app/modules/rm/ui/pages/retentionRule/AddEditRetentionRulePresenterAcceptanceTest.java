package com.constellio.app.modules.rm.ui.pages.retentionRule;

import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.ui.application.CoreViews;
import com.constellio.app.ui.entities.VariableRetentionPeriodVO;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.InDevelopmentTest;

@InDevelopmentTest
public class AddEditRetentionRulePresenterAcceptanceTest extends ConstellioTest {
	RMTestRecords records = new RMTestRecords(zeCollection);
	AddEditRetentionRulePresenter presenter;
	@Mock
	private AddEditRetentionRuleView view;
	@Mock
	CoreViews navigator;
	@Mock
	private SessionContext sessionContext;

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers().withRMTest(records)
						.withFoldersAndContainersOfEveryStatus().withEvents()
		);

		when(view.getSessionContext()).thenReturn(sessionContext);
		when(view.getCollection()).thenReturn(zeCollection);
		when(sessionContext.getCurrentCollection()).thenReturn(zeCollection);
		when(view.getConstellioFactories()).thenReturn(getConstellioFactories());
		when(view.navigateTo()).thenReturn(navigator);

		presenter = new AddEditRetentionRulePresenter(view);

	}

	@Test
	public void givenFolderIdMetadataWhenIsRecordIdMetadataThenReturnTrue()
			throws Exception {

		List<VariableRetentionPeriodVO> openPeriods = presenter.getOpenPeriodsDDVList();
		for (VariableRetentionPeriodVO openPeriod : openPeriods) {
			System.out.println(openPeriod.getCode() + ", " + openPeriod.getRecordId() + ", " + openPeriod.getTitle());
		}
	}

}

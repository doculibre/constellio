package com.constellio.app.modules.rm.ui.pages.retentionRule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.ui.application.ConstellioNavigator;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.sdk.tests.ConstellioTest;

/**
 * Created by Patrick on 2015-11-25.
 */
public class DisplayRetentionRulePresenterAcceptTest extends ConstellioTest {

	RMTestRecords records = new RMTestRecords(zeCollection);
	DisplayRetentionRulePresenter presenter;
	@Mock private DisplayRetentionRuleView view;
	@Mock ConstellioNavigator navigator;
	@Mock private SessionContext sessionContext;

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

		presenter = new DisplayRetentionRulePresenter(view);

	}

	@Test
	public void givenRecordWhenGetOpenActivePeriodsDDVListThenOk()
			throws Exception {

		presenter.forParams(records.ruleId_1);

		assertThat(presenter.getOpenActivePeriodsDDVList()).extracting("code", "title")
				.containsOnly(
						tuple("888", "Ouvert"),
						tuple("42", "Ze 42")
				);
	}
}
package com.constellio.app.modules.robots.reports;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.robots.model.DryRunRobotAction;
import com.constellio.app.reports.builders.administration.plan.ReportBuilderTestFramework;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ZeSchemaMetadatas;

/**
 * Created by Patrick on 2015-12-16.
 */
public class DryRunReportPresenterManualAcceptTest extends ReportBuilderTestFramework {

	DryRunReportPresenter presenter;
	List<DryRunRobotAction> dryRunRobotActions = new ArrayList<>();
	DryRunRobotAction dryRunRobotAction1, dryRunRobotAction2, dryRunRobotAction3, dryRunRobotAction4;

	TestsSchemasSetup zeCollectionSetup = new TestsSchemasSetup(zeCollection);
	ZeSchemaMetadatas zeCollectionSchema = zeCollectionSetup.new ZeSchemaMetadatas();

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(withZeCollection());

		defineSchemasManager().using(zeCollectionSetup
				.withAStringMetadata()
				.withABooleanMetadata()
				.withAnotherStringMetadata()
				.withAMultivaluedLargeTextMetadata()
				.withADateMetadata());

		configDryRunRobotActions();
	}

	@Test
	public void whenBuildEmptyReportThenOk() {
		presenter = new DryRunReportPresenter(getModelLayerFactory(), dryRunRobotActions);
		build(new DryRunReportBuilder(presenter.buildModel(), new Locale("fr")));
	}

	@Test
	public void whenBuildReportWithResultsThenOk() {
		dryRunRobotActions.add(dryRunRobotAction1);
		dryRunRobotActions.add(dryRunRobotAction2);
		dryRunRobotActions.add(dryRunRobotAction3);
		dryRunRobotActions.add(dryRunRobotAction4);
		presenter = new DryRunReportPresenter(getModelLayerFactory(), dryRunRobotActions);
		build(new DryRunReportBuilder(presenter.buildModel(), new Locale("fr")));
	}

	@Test
	public void whenBuildReportWithAndOrderResultsThenOk() {
		dryRunRobotActions.add(dryRunRobotAction4);
		dryRunRobotActions.add(dryRunRobotAction3);
		dryRunRobotActions.add(dryRunRobotAction2);
		dryRunRobotActions.add(dryRunRobotAction1);
		presenter = new DryRunReportPresenter(getModelLayerFactory(), dryRunRobotActions);
		build(new DryRunReportBuilder(presenter.buildModel(), new Locale("fr")));
	}

	//
	private void configDryRunRobotActions() {
		Map<Metadata, Object> actionParams1 = new HashMap<>();
		actionParams1.put(zeCollectionSchema.stringMetadata(), "value1");
		actionParams1.put(zeCollectionSchema.dateMetadata(), TimeProvider.getLocalDate().minusDays(1));
		Map<Metadata, Object> actionParams2 = new HashMap<>();
		actionParams2.put(zeCollectionSchema.stringMetadata(), "value2");
		Map<Metadata, Object> actionParams3 = new HashMap<>();
		actionParams3.put(zeCollectionSchema.multivaluedLargeTextMetadata(), Arrays.asList("value3", "value4"));
		actionParams3.put(zeCollectionSchema.booleanMetadata(), true);
		Map<Metadata, Object> actionParams4 = new HashMap<>();
		actionParams4.put(zeCollectionSchema.dateMetadata(), TimeProvider.getLocalDate());

		dryRunRobotAction1 = new DryRunRobotAction();
		dryRunRobotAction1.setActionTitle("classifyConnectorTaxonomy");
		dryRunRobotAction1.setActionParameters(actionParams1);
		dryRunRobotAction1.setRecordId("RecordId1");
		dryRunRobotAction1.setRecordUrl("RecordUrl1");
		dryRunRobotAction1.setRecordTitle("RecordTitle1");
		dryRunRobotAction1.setRobotId("RobotId1");
		dryRunRobotAction1.setRobotCode("RobotoCode1");
		dryRunRobotAction1.setRobotTitle("RobotoTitle1");
		dryRunRobotAction1.setRobotHierarchy("RobotHierarchy1");

		dryRunRobotAction2 = new DryRunRobotAction();
		dryRunRobotAction2.setActionTitle("classifySmbFolderInFolder");
		dryRunRobotAction2.setActionParameters(actionParams2);
		dryRunRobotAction2.setRecordId("RecordId2");
		dryRunRobotAction2.setRecordUrl("RecordUrl2");
		dryRunRobotAction2.setRecordTitle("RecordTitle2");
		dryRunRobotAction2.setRobotId("RobotId2");
		dryRunRobotAction2.setRobotCode("RobotoCode2");
		dryRunRobotAction2.setRobotTitle("RobotoTitle2");
		dryRunRobotAction2.setRobotHierarchy("RobotHierarchy2");

		dryRunRobotAction3 = new DryRunRobotAction();
		dryRunRobotAction3.setActionTitle("classifySmbDocumentInFolder");
		dryRunRobotAction3.setActionParameters(actionParams3);
		dryRunRobotAction3.setRecordId("RecordId3");
		dryRunRobotAction3.setRecordUrl("RecordUrl3");
		dryRunRobotAction3.setRecordTitle("RecordTitle3");
		dryRunRobotAction3.setRobotId("RobotId3");
		dryRunRobotAction3.setRobotCode("RobotoCode3");
		dryRunRobotAction3.setRobotTitle("RobotoTitle3");
		dryRunRobotAction3.setRobotHierarchy("RobotHierarchy3");

		dryRunRobotAction4 = new DryRunRobotAction();
		dryRunRobotAction4.setActionTitle("runExtractorsAction");
		dryRunRobotAction4.setActionParameters(actionParams4);
		dryRunRobotAction4.setRecordId("RecordId4");
		dryRunRobotAction4.setRecordUrl("RecordUrl4");
		dryRunRobotAction4.setRecordTitle("RecordTitle4");
		dryRunRobotAction4.setRobotId("RobotId4");
		dryRunRobotAction4.setRobotCode("RobotoCode4");
		dryRunRobotAction4.setRobotTitle("RobotoTitle4");
		dryRunRobotAction4.setRobotHierarchy("RobotHierarchy4");
	}
}
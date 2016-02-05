package com.constellio.app.modules.rm.reports.administration.plan;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.reports.builders.administration.plan.ClassificationPlanReportBuilder;
import com.constellio.app.modules.rm.reports.model.administration.plan.ClassificationPlanReportModel;
import com.constellio.app.modules.rm.reports.model.administration.plan.ClassificationPlanReportModel.ClassificationPlanReportModel_Category;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.reports.builders.administration.plan.ReportBuilderTestFramework;

public class ClassificationPlanReportBuilderManualAcceptTest extends ReportBuilderTestFramework {

	ClassificationPlanReportModel model;
	RMTestRecords records = new RMTestRecords(zeCollection);

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioRMModule().withRMTest(records)
						.withFoldersAndContainersOfEveryStatus()
		);
	}

	@Test
	public void whenBuildEmptyClassificationPlanReportThenOk() {

		model = new ClassificationPlanReportModel();
		//model.setHeaderLogo(getTestResourceInputStreamFactory("logo.png"));

		build(new ClassificationPlanReportBuilder(model,
				getModelLayerFactory().getFoldersLocator()));

	}

	@Test
	public void whenBuildClassificationPlanByAdministrativeUnitReportThenOk() {

		boolean detailed = true;
		model = configCategories(detailed);
		Map<AdministrativeUnit, List<ClassificationPlanReportModel_Category>> administrativeUnitListCategoryMap = new HashMap<>();
		administrativeUnitListCategoryMap.put(records.getUnit10(), model.getRootCategories());
		model.setCategoriesByAdministrativeUnit(administrativeUnitListCategoryMap);
		model.setByAdministrativeUnit(true);

		build(new ClassificationPlanReportBuilder(model,
				getModelLayerFactory().getFoldersLocator()));
	}

	@Test
	public void whenBuildDetailedClassificationPlanReportThenOk() {

		boolean detailed = true;
		model = configCategories(detailed);

		build(new ClassificationPlanReportBuilder(model,
				getModelLayerFactory().getFoldersLocator()));
	}

	@Test
	public void whenBuildClassificationPlanReportThenOk() {

		boolean detailed = false;
		model = configCategories(detailed);

		build(new ClassificationPlanReportBuilder(model,
				getModelLayerFactory().getFoldersLocator()));
	}

	private ClassificationPlanReportModel configCategories(boolean detailed) {
		ClassificationPlanReportModel model = new ClassificationPlanReportModel();
		ClassificationPlanReportModel_Category categoryLevel_0 = new ClassificationPlanReportModel_Category();
		ClassificationPlanReportModel_Category categoryLevel_01 = new ClassificationPlanReportModel_Category();
		ClassificationPlanReportModel_Category categoryLevel_010 = new ClassificationPlanReportModel_Category();
		ClassificationPlanReportModel_Category categoryLevel_011 = new ClassificationPlanReportModel_Category();
		ClassificationPlanReportModel_Category categoryLevel_0100 = new ClassificationPlanReportModel_Category();
		ClassificationPlanReportModel_Category categoryLevel_01000 = new ClassificationPlanReportModel_Category();
		ClassificationPlanReportModel_Category categoryLevel_01001 = new ClassificationPlanReportModel_Category();
		ClassificationPlanReportModel_Category categoryLevel_010010 = new ClassificationPlanReportModel_Category();
		ClassificationPlanReportModel_Category categoryLevel_0101 = new ClassificationPlanReportModel_Category();
		ClassificationPlanReportModel_Category categoryLevel_02 = new ClassificationPlanReportModel_Category();
		ClassificationPlanReportModel_Category categoryLevel_03 = new ClassificationPlanReportModel_Category();
		ClassificationPlanReportModel_Category categoryLevel_04 = new ClassificationPlanReportModel_Category();
		ClassificationPlanReportModel_Category categoryLevel_05 = new ClassificationPlanReportModel_Category();

		ClassificationPlanReportModel_Category categoryLevel_1 = new ClassificationPlanReportModel_Category();
		ClassificationPlanReportModel_Category categoryLevel_11 = new ClassificationPlanReportModel_Category();
		ClassificationPlanReportModel_Category categoryLevel_12 = new ClassificationPlanReportModel_Category();
		ClassificationPlanReportModel_Category categoryLevel_13 = new ClassificationPlanReportModel_Category();
		ClassificationPlanReportModel_Category categoryLevel_14 = new ClassificationPlanReportModel_Category();
		ClassificationPlanReportModel_Category categoryLevel_15 = new ClassificationPlanReportModel_Category();

		ClassificationPlanReportModel_Category categoryLevel_2 = new ClassificationPlanReportModel_Category();
		ClassificationPlanReportModel_Category categoryLevel_21 = new ClassificationPlanReportModel_Category();
		ClassificationPlanReportModel_Category categoryLevel_22 = new ClassificationPlanReportModel_Category();
		ClassificationPlanReportModel_Category categoryLevel_23 = new ClassificationPlanReportModel_Category();
		ClassificationPlanReportModel_Category categoryLevel_24 = new ClassificationPlanReportModel_Category();
		ClassificationPlanReportModel_Category categoryLevel_25 = new ClassificationPlanReportModel_Category();

		ClassificationPlanReportModel_Category categoryLevel_3 = new ClassificationPlanReportModel_Category();
		ClassificationPlanReportModel_Category categoryLevel_31 = new ClassificationPlanReportModel_Category();
		ClassificationPlanReportModel_Category categoryLevel_32 = new ClassificationPlanReportModel_Category();
		ClassificationPlanReportModel_Category categoryLevel_33 = new ClassificationPlanReportModel_Category();
		ClassificationPlanReportModel_Category categoryLevel_34 = new ClassificationPlanReportModel_Category();
		ClassificationPlanReportModel_Category categoryLevel_35 = new ClassificationPlanReportModel_Category();

		ClassificationPlanReportModel_Category categoryLevel_4 = new ClassificationPlanReportModel_Category();
		ClassificationPlanReportModel_Category categoryLevel_41 = new ClassificationPlanReportModel_Category();
		ClassificationPlanReportModel_Category categoryLevel_42 = new ClassificationPlanReportModel_Category();
		ClassificationPlanReportModel_Category categoryLevel_43 = new ClassificationPlanReportModel_Category();
		ClassificationPlanReportModel_Category categoryLevel_44 = new ClassificationPlanReportModel_Category();
		ClassificationPlanReportModel_Category categoryLevel_45 = new ClassificationPlanReportModel_Category();

		ClassificationPlanReportModel_Category categoryLevel_5 = new ClassificationPlanReportModel_Category();
		ClassificationPlanReportModel_Category categoryLevel_51 = new ClassificationPlanReportModel_Category();
		ClassificationPlanReportModel_Category categoryLevel_52 = new ClassificationPlanReportModel_Category();
		ClassificationPlanReportModel_Category categoryLevel_53 = new ClassificationPlanReportModel_Category();
		ClassificationPlanReportModel_Category categoryLevel_54 = new ClassificationPlanReportModel_Category();
		ClassificationPlanReportModel_Category categoryLevel_55 = new ClassificationPlanReportModel_Category();

		List<String> keywords = Arrays.asList("keyword1", "keyword2", "keyword3");
		List<String> retentionRules = Arrays.asList("rule1", "rule2", "rule3");

		categoryLevel_010010.setCode("level_010010").setLabel("Ze level 010010").setDescription(textOfLength(200))
				.setKeywords(keywords).setRetentionRules(retentionRules);

		categoryLevel_01000.setCode("level_01000").setLabel("Ze level 01000").setDescription(textOfLength(200)).setKeywords(
				keywords).setRetentionRules(retentionRules);
		categoryLevel_01001.setCode("level_01001").setLabel("Ze level 01010").setDescription(textOfLength(200))
				.setKeywords(keywords).setRetentionRules(retentionRules).getCategories()
				.add(categoryLevel_010010);

		categoryLevel_0100.setCode("level_0100").setLabel("Ze level 0100").setDescription(textOfLength(200)).setKeywords(keywords)
				.setRetentionRules(retentionRules).getCategories()
				.addAll(Arrays.asList(categoryLevel_01000, categoryLevel_01001));
		categoryLevel_0101.setCode("level_0101").setLabel("Ze level 0101").setDescription(textOfLength(200)).setKeywords(keywords)
				.setRetentionRules(
						retentionRules);

		categoryLevel_010.setCode("level_010").setLabel("Ze level 010").setDescription(textOfLength(200)).setKeywords(keywords)
				.setRetentionRules(retentionRules).getCategories()
				.addAll(Arrays.asList(categoryLevel_0100, categoryLevel_0101));

		categoryLevel_011.setCode("level_011").setLabel("Ze level 011").setDescription(textOfLength(200)).setKeywords(
				keywords).setRetentionRules(retentionRules);
		categoryLevel_01.setCode("level_01").setLabel("Ze level 01").setDescription(textOfLength(200)).setKeywords(
				keywords).setRetentionRules(retentionRules).getCategories()
				.addAll(Arrays.asList(categoryLevel_010, categoryLevel_011));

		categoryLevel_02.setCode("level_02").setLabel("Ze level 02").setDescription(textOfLength(200)).setKeywords(
				keywords).setRetentionRules(retentionRules);
		categoryLevel_03.setCode("level_03").setLabel("Ze level 03").setDescription(textOfLength(200)).setKeywords(
				keywords).setRetentionRules(retentionRules);
		categoryLevel_04.setCode("level_04").setLabel("Ze level 04").setDescription(textOfLength(200)).setKeywords(
				keywords).setRetentionRules(retentionRules);
		categoryLevel_05.setCode("level_05").setLabel("Ze level 05").setDescription(textOfLength(200)).setKeywords(
				keywords).setRetentionRules(retentionRules);

		categoryLevel_0.setCode("level_0").setLabel("Ze level 0").setDescription(textOfLength(200)).setKeywords(
				keywords).setRetentionRules(retentionRules).getCategories()
				.addAll(Arrays.asList(categoryLevel_01, categoryLevel_02, categoryLevel_03, categoryLevel_04, categoryLevel_05));

		categoryLevel_11.setCode("level_11").setLabel("Ze level 11").setDescription(textOfLength(500));
		categoryLevel_12.setCode("level_12").setLabel("Ze level 12").setDescription(textOfLength(200));
		categoryLevel_13.setCode("level_13").setLabel("Ze level 13").setDescription(textOfLength(200));
		categoryLevel_14.setCode("level_14").setLabel("Ze level 14").setDescription(textOfLength(200));
		categoryLevel_15.setCode("level_15").setLabel("Ze level 15").setDescription(textOfLength(200));
		categoryLevel_1.setCode("level1").setLabel("Ze level 1").setDescription(textOfLength(200)).setKeywords(
				keywords).setRetentionRules(retentionRules).getCategories()
				.addAll(Arrays.asList(categoryLevel_11, categoryLevel_12, categoryLevel_13, categoryLevel_14, categoryLevel_15));
		categoryLevel_2.setCode("level2").setLabel("Ze level 2").setDescription(textOfLength(200)).setKeywords(
				keywords).setRetentionRules(retentionRules);
		categoryLevel_3.setCode("level3").setLabel("Ze level 3").setDescription(textOfLength(200)).setKeywords(
				keywords).setRetentionRules(retentionRules);
		categoryLevel_4.setCode("level4").setLabel("Ze level 4").setDescription(textOfLength(200)).setKeywords(
				keywords).setRetentionRules(retentionRules);
		categoryLevel_5.setCode("level5").setLabel("Ze level 5").setDescription(textOfLength(200));

		model.setHeaderLogo(getTestResourceInputStreamFactory("logo.png")).getRootCategories().add(categoryLevel_0);
		model.setHeaderLogo(getTestResourceInputStreamFactory("logo.png")).getRootCategories().add(categoryLevel_1);
		model.setHeaderLogo(getTestResourceInputStreamFactory("logo.png")).getRootCategories().add(categoryLevel_2);
		model.setHeaderLogo(getTestResourceInputStreamFactory("logo.png")).getRootCategories().add(categoryLevel_3);
		model.setHeaderLogo(getTestResourceInputStreamFactory("logo.png")).getRootCategories().add(categoryLevel_4);
		model.setHeaderLogo(getTestResourceInputStreamFactory("logo.png")).getRootCategories().add(categoryLevel_5);

		model.setDetailed(detailed);
		return model;
	}

}

package com.constellio.app.entities.calculators;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.summaryconfig.SummaryConfigParams;
import com.constellio.app.ui.pages.summaryconfig.SummaryConfigPresenter;
import com.constellio.app.ui.pages.summaryconfig.SummaryConfigView;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.MockedNavigation;
import com.constellio.sdk.tests.setups.Users;
import org.jetbrains.annotations.NotNull;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Locale;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class SummaryCalculatorAcceptanceTest extends ConstellioTest {

	RMTestRecords records = new RMTestRecords(zeCollection);
	Users users = new Users();
	RMSchemasRecordsServices rmRecordSchemaManager;
	RecordServices recordServices;

	@Mock
	SummaryConfigView view;
	MockedNavigation navigator;
	@Mock
	SessionContext sessionContext;

	SummaryConfigPresenter summaryConfigPresenter;

	@Before
	public void setUp() {
		prepareSystem(withZeCollection().withConstellioRMModule().withRMTest(records).withAllTest(users));
		rmRecordSchemaManager = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());

		navigator = new MockedNavigation();

		when(view.getConstellioFactories()).thenReturn(getConstellioFactories());
		when(view.getCollection()).thenReturn(zeCollection);
		when(view.getSessionContext()).thenReturn(sessionContext);
		when(sessionContext.getCurrentCollection()).thenReturn(zeCollection);
		when(sessionContext.getCurrentLocale()).thenReturn(Locale.FRENCH);

		summaryConfigPresenter = new SummaryConfigPresenter(view, Folder.DEFAULT_SCHEMA);
		recordServices = getModelLayerFactory().newRecordServices();
	}

	@Test
	public void givenFolderWithMetadataValueAndReferenceSummaryConfigParameterThenSummaryMetadataHaveAValueTest()
			throws Exception {
		SummaryConfigParams summaryConfigParams = new SummaryConfigParams();

		summaryConfigParams.setMetadataVO(findMetadata(Folder.ADMINISTRATIVE_UNIT));
		summaryConfigParams.setPrefix("prefix :");
		summaryConfigParams.setDisplayCondition(SummaryConfigParams.DisplayCondition.ALWAYS);
		summaryConfigParams.setReferenceMetadataDisplay(SummaryConfigParams.ReferenceMetadataDisplay.CODE);

		summaryConfigPresenter.addMetadaForSummary(summaryConfigParams);


		Folder folder = createFolder();
		recordServices.add(folder);

		Folder resultFolder = rmRecordSchemaManager.getFolder(folder.getId());

		String summary = resultFolder.get(Folder.SUMMARY);

		assertThat(summary).isEqualTo("prefix : 11B");
	}

	@Test
	public void givenFolderWithMetadataValueAndReferenceSummaryColumnTitleParameterThenSummaryMetadataHaveAValueTest()
			throws Exception {
		SummaryConfigParams summaryConfigParams = new SummaryConfigParams();

		summaryConfigParams.setMetadataVO(findMetadata(Folder.ADMINISTRATIVE_UNIT));
		summaryConfigParams.setPrefix("prefix :");
		summaryConfigParams.setDisplayCondition(SummaryConfigParams.DisplayCondition.ALWAYS);
		summaryConfigParams.setReferenceMetadataDisplay(SummaryConfigParams.ReferenceMetadataDisplay.TITLE);

		summaryConfigPresenter.addMetadaForSummary(summaryConfigParams);

		Folder folder = createFolder();
		recordServices.add(folder);

		Folder resultFolder = rmRecordSchemaManager.getFolder(folder.getId());

		String summary = resultFolder.get(Folder.SUMMARY);

		assertThat(summary).isEqualTo("prefix : Unité 11-B");
	}

	@NotNull
	private Folder createFolder() throws RecordServicesException {
		Folder folder = rmRecordSchemaManager.newFolder();

		folder.setAdministrativeUnitEntered(records.unitId_11b);
		folder.setCategoryEntered(records.categoryId_X110);
		folder.setTitle("Ze folder");
		folder.setRetentionRuleEntered(records.ruleId_2);
		folder.setCopyStatusEntered(CopyType.PRINCIPAL);
		folder.setOpenDate(new LocalDate(2014, 11, 4));

		return folder;
	}


	@Test
	public void givenOneFolderWithValueAndDynamicDependancyThenSummaryMetadataHaveAValueTest()
			throws Exception {
		SummaryConfigParams summaryConfigParams = new SummaryConfigParams();

		summaryConfigParams.setMetadataVO(findMetadata(Folder.TITLE));
		summaryConfigParams.setPrefix("prefix :");
		summaryConfigParams.setDisplayCondition(SummaryConfigParams.DisplayCondition.ALWAYS);

		summaryConfigPresenter.addMetadaForSummary(summaryConfigParams);

		Folder folder = createFolder();
		recordServices.add(folder);

		Folder resultFolder = rmRecordSchemaManager.getFolder(folder.getId());

		String summary = resultFolder.get(Folder.SUMMARY);

		// Les paramèetres sont setter.
		assertThat(summary).isEqualTo("prefix : Ze folder");
	}

	@Test
	public void givenOneFolderWithValueAndDynamicDependancyMultiValueThenSummaryMetadataHaveAValueTest()
			throws Exception {
		SummaryConfigParams summaryConfigParams = new SummaryConfigParams();

		summaryConfigParams.setMetadataVO(findMetadata(Folder.KEYWORDS));
		summaryConfigParams.setPrefix("prefix :");
		summaryConfigParams.setDisplayCondition(SummaryConfigParams.DisplayCondition.ALWAYS);
		summaryConfigParams.setReferenceMetadataDisplay(SummaryConfigParams.ReferenceMetadataDisplay.CODE);

		summaryConfigPresenter.addMetadaForSummary(summaryConfigParams);

		Folder folder = createFolder();

		folder.setKeywords(asList("keyword1, keyword2, keyword3"));
		folder.setOpenDate(new LocalDate(2014, 11, 4));

		recordServices.add(folder);

		Folder resultFolder = rmRecordSchemaManager.getFolder(folder.getId());

		String summary = resultFolder.get(Folder.SUMMARY);

		assertThat(summary).isEqualTo("prefix : keyword1, keyword2, keyword3");
	}

	@Test
	public void givenOneFolderWithValueDynamicDependancyMultiValueAndTwoDynamicDepencySingleThenSummaryMetadataHaveAValueTest()
			throws Exception {

		SummaryConfigParams summaryConfigParams1 = new SummaryConfigParams();

		summaryConfigParams1.setMetadataVO(findMetadata(Folder.KEYWORDS));
		summaryConfigParams1.setPrefix("prefix1 :");
		summaryConfigParams1.setDisplayCondition(SummaryConfigParams.DisplayCondition.ALWAYS);

		summaryConfigPresenter.addMetadaForSummary(summaryConfigParams1);

		SummaryConfigParams summaryConfigParams2 = new SummaryConfigParams();

		summaryConfigParams2.setMetadataVO(findMetadata(Folder.ADMINISTRATIVE_UNIT));
		summaryConfigParams2.setPrefix("prefix2 :");
		summaryConfigParams2.setDisplayCondition(SummaryConfigParams.DisplayCondition.ALWAYS);
		summaryConfigParams2.setReferenceMetadataDisplay(SummaryConfigParams.ReferenceMetadataDisplay.CODE);

		summaryConfigPresenter.addMetadaForSummary(summaryConfigParams2);

		SummaryConfigParams summaryConfigParams3 = new SummaryConfigParams();

		summaryConfigParams3.setMetadataVO(findMetadata(Folder.DESCRIPTION));
		summaryConfigParams3.setPrefix("prefix3 :");
		summaryConfigParams3.setDisplayCondition(SummaryConfigParams.DisplayCondition.COMPLETED);
		summaryConfigParams3.setReferenceMetadataDisplay(SummaryConfigParams.ReferenceMetadataDisplay.CODE);

		summaryConfigPresenter.addMetadaForSummary(summaryConfigParams3);

		Folder folder = createFolder();

		folder.setKeywords(asList("keyword1, keyword2, keyword3"));
		folder.setOpenDate(new LocalDate(2014, 11, 4));

		recordServices.add(folder);

		Folder resultFolder = rmRecordSchemaManager.getFolder(folder.getId());

		String summary = resultFolder.get(Folder.SUMMARY);

		assertThat(summary).isEqualTo("prefix1 : keyword1, keyword2, keyword3, prefix2 : 11B");
	}

	@Test
	public void givenDateTimeInSummaryColumnThenFormatCorrectly() throws RecordServicesException {
		SummaryConfigParams summaryConfigParams1 = new SummaryConfigParams();

		summaryConfigParams1.setMetadataVO(findMetadata(Folder.BORROW_DATE));
		summaryConfigParams1.setPrefix("prefix1 :");
		summaryConfigParams1.setDisplayCondition(SummaryConfigParams.DisplayCondition.ALWAYS);

		summaryConfigPresenter.addMetadaForSummary(summaryConfigParams1);

		Folder folder = createFolder();

		folder.setBorrowDate(new LocalDateTime(2016, 11, 4, 10, 10));

		recordServices.add(folder);

		Folder resultFolder = rmRecordSchemaManager.getFolder(folder.getId());

		String summary = resultFolder.get(Folder.SUMMARY);

		assertThat(summary).isEqualTo("prefix1 : 2016-11-04 10:10:00");
	}

	private MetadataVO findMetadata(String localCode) {

		for (MetadataVO metadataVO : summaryConfigPresenter.getMetadatas()) {
			if (metadataVO.getLocalCode().equalsIgnoreCase(localCode)) {
				return metadataVO;
			}
		}

		return null;
	}
}


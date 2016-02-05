package com.constellio.app.ui.pages.search.criteria;

import static com.constellio.app.ui.pages.search.criteria.Criterion.BooleanOperator.AND;
import static com.constellio.app.ui.pages.search.criteria.Criterion.BooleanOperator.OR;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.search.criteria.Criterion.SearchOperator;
import com.constellio.app.ui.pages.search.criteria.RelativeCriteria.RelativeSearchOperator;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.search.query.logical.criteria.MeasuringUnitTime;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;

public class ConditionBuilderAcceptTest extends ConstellioTest {

	SearchServices searchServices;
	RMTestRecords records = new RMTestRecords(zeCollection);
	RMSchemasRecordsServices rm;
	ConditionBuilder folderConditionBuilder;
	RecordServices recordServices;
	LocalDateTime now = TimeProvider.getLocalDateTime();

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers().withRMTest(records)
						.withFoldersAndContainersOfEveryStatus()
		);

		searchServices = getModelLayerFactory().newSearchServices();
		recordServices = getModelLayerFactory().newRecordServices();

		rm = new RMSchemasRecordsServices(zeCollection, getModelLayerFactory());
		folderConditionBuilder = new ConditionBuilder(rm.folderSchemaType());
	}

	@Test
	public void testWithParens()
			throws Exception {

		// A && ( B || C)
		CriteriaBuilder builder = newFolderCriteriaBuilderAsAdmin();
		builder.addCriterion(Folder.ADMINISTRATIVE_UNIT).isEqualTo(records.unitId_10a).booleanOperator(AND);
		builder.addCriterion("title").isContainingText("Écureuil").withLeftParens().booleanOperator(OR);
		builder.addCriterion("title").isContainingText("ouille").withRightParens();
		assertThat(recordIdsOfFolderCriteria(builder)).containsOnly(records.folder_A45, records.folder_A49);

		// A && ( B || (C & D) || E)
		builder = newFolderCriteriaBuilderAsAdmin();
		builder.addCriterion(Folder.ADMINISTRATIVE_UNIT).isEqualTo(records.unitId_10a).booleanOperator(AND);
		builder.addCriterion("title").isContainingText("Écureuil").withLeftParens().booleanOperator(OR);
		builder.addCriterion("title").isContainingText("Chauve").withLeftParens().booleanOperator(AND);
		builder.addCriterion("title").isContainingText("souris").withRightParens().booleanOperator(OR);
		builder.addCriterion("title").isContainingText("ouille").withRightParens();
		assertThat(recordIdsOfFolderCriteria(builder)).containsOnly(records.folder_A45, records.folder_A49, records.folder_A17);

	}

	//Relative equals today
	@Test
	public void whenBuilderWithRelativeCriterionEqualsTodayThenNoRecordFound()
			throws Exception {

		RelativeCriteria relativeCriteria = new RelativeCriteria();
		relativeCriteria.setRelativeSearchOperator(RelativeSearchOperator.TODAY);

		CriteriaBuilder builder = newFolderCriteriaBuilderAsAdmin();
		builder.addCriterion(Folder.OPENING_DATE).searchOperator(SearchOperator.EQUALS).relativeSearchCriteria(relativeCriteria);

		assertThat(recordIdsOfFolderCriteria(builder)).isEmpty();
	}

	@Test
	public void givenAFolderWithOpeningDateOfTodayWhenBuilderWithRelativeCriterionEqualsTodayThenOneRecordFound()
			throws Exception {

		Folder folder = records.getFolder_A01().setOpenDate(now.toLocalDate());
		recordServices.update(folder);

		RelativeCriteria relativeCriteria = new RelativeCriteria();
		relativeCriteria.setRelativeSearchOperator(RelativeSearchOperator.TODAY);
		CriteriaBuilder builder = newFolderCriteriaBuilderAsAdmin();
		builder.addCriterion(Folder.OPENING_DATE).searchOperator(SearchOperator.EQUALS).relativeSearchCriteria(relativeCriteria);

		assertThat(recordIdsOfFolderCriteria(builder)).containsOnly(records.folder_A01);
	}

	@Test
	public void givenAFolderWithOpeningDateOfTodayAndTimeTommorowWhenBuilderWithRelativeCriterionEqualsTodayThenNoRecordFound()
			throws Exception {

		givenTimeIs(now);
		Folder folder = records.getFolder_A01().setOpenDate(now.toLocalDate());
		recordServices.update(folder);

		givenTimeIs(now.plusDays(1));
		RelativeCriteria relativeCriteria = new RelativeCriteria();
		relativeCriteria.setRelativeSearchOperator(RelativeSearchOperator.TODAY);
		CriteriaBuilder builder = newFolderCriteriaBuilderAsAdmin();
		builder.addCriterion(Folder.OPENING_DATE).searchOperator(SearchOperator.EQUALS).relativeSearchCriteria(relativeCriteria);

		assertThat(recordIdsOfFolderCriteria(builder)).isEmpty();
	}

	//Relative equals in future
	@Test
	public void givenAFolderWithOpeningDateInTheFutureWhenBuilderWithRelativeCriterionEqualsFutureThenOneRecordFound()
			throws Exception {

		Folder folder = records.getFolder_A01().setOpenDate(now.toLocalDate().plusDays(1));
		recordServices.update(folder);

		RelativeCriteria relativeCriteria = new RelativeCriteria();
		relativeCriteria.setRelativeSearchOperator(RelativeSearchOperator.FUTURE);
		relativeCriteria.setMeasuringUnitTime(MeasuringUnitTime.DAYS);
		CriteriaBuilder builder = newFolderCriteriaBuilderAsAdmin();
		builder.addCriterion(Folder.OPENING_DATE).searchOperator(SearchOperator.EQUALS).relativeSearchCriteria(relativeCriteria)
				.value(1.0);

		assertThat(recordIdsOfFolderCriteria(builder)).containsOnly(records.folder_A01);
	}

	@Test
	public void givenAFolderWithOpeningDateInThePastWhenBuilderWithRelativeCriterionEqualsFutureThenNoRecordFound()
			throws Exception {

		Folder folder = records.getFolder_A01().setOpenDate(now.toLocalDate().minusDays(1));
		recordServices.update(folder);

		RelativeCriteria relativeCriteria = new RelativeCriteria();
		relativeCriteria.setRelativeSearchOperator(RelativeSearchOperator.FUTURE);
		relativeCriteria.setMeasuringUnitTime(MeasuringUnitTime.DAYS);
		CriteriaBuilder builder = newFolderCriteriaBuilderAsAdmin();
		builder.addCriterion(Folder.OPENING_DATE).searchOperator(SearchOperator.EQUALS).relativeSearchCriteria(relativeCriteria)
				.value(1.0);

		assertThat(recordIdsOfFolderCriteria(builder)).isEmpty();
	}

	//Relative equals in past
	@Test
	public void givenAFolderWithOpeningDateInThePastWhenBuilderWithRelativeCriterionEqualsPastThenOneRecordFound()
			throws Exception {

		Folder folder = records.getFolder_A01().setOpenDate(now.toLocalDate().minusDays(1));
		recordServices.update(folder);

		RelativeCriteria relativeCriteria = new RelativeCriteria();
		relativeCriteria.setRelativeSearchOperator(RelativeSearchOperator.PAST);
		relativeCriteria.setMeasuringUnitTime(MeasuringUnitTime.DAYS);
		CriteriaBuilder builder = newFolderCriteriaBuilderAsAdmin();
		builder.addCriterion(Folder.OPENING_DATE).searchOperator(SearchOperator.EQUALS).relativeSearchCriteria(relativeCriteria)
				.value(1.0);

		assertThat(recordIdsOfFolderCriteria(builder)).containsOnly(records.folder_A01);
	}

	@Test
	public void givenAFolderWithOpeningDateInTheFutureWhenBuilderWithRelativeCriterionEqualsPastThenNoRecordFound()
			throws Exception {

		Folder folder = records.getFolder_A01().setOpenDate(now.toLocalDate().plusDays(1));
		recordServices.update(folder);

		RelativeCriteria relativeCriteria = new RelativeCriteria();
		relativeCriteria.setRelativeSearchOperator(RelativeSearchOperator.PAST);
		relativeCriteria.setMeasuringUnitTime(MeasuringUnitTime.DAYS);
		CriteriaBuilder builder = newFolderCriteriaBuilderAsAdmin();
		builder.addCriterion(Folder.OPENING_DATE).searchOperator(SearchOperator.EQUALS).relativeSearchCriteria(relativeCriteria)
				.value(1.0);

		assertThat(recordIdsOfFolderCriteria(builder)).isEmpty();
	}

	//Relative equals
	@Test
	public void givenAFolderWithOpeningDateInThePastWhenBuilderWithRelativeCriterionEqualsTheDateThenOneRecordFound()
			throws Exception {

		LocalDate ldt = now.toLocalDate().minusDays(1);
		Folder folder = records.getFolder_A01().setOpenDate(ldt);
		recordServices.update(folder);

		RelativeCriteria relativeCriteria = new RelativeCriteria();
		relativeCriteria.setRelativeSearchOperator(RelativeSearchOperator.EQUALS);
		CriteriaBuilder builder = newFolderCriteriaBuilderAsAdmin();
		builder.addCriterion(Folder.OPENING_DATE).searchOperator(SearchOperator.EQUALS).relativeSearchCriteria(relativeCriteria)
				.value(ldt);

		assertThat(recordIdsOfFolderCriteria(builder)).containsOnly(records.folder_A01);
	}

	@Test
	public void givenAFolderWithOpeningDateInTheFutureWhenBuilderWithRelativeCriterionEqualsTheDateThenOneRecordFound()
			throws Exception {

		LocalDate ldt = now.toLocalDate().plusDays(1);
		Folder folder = records.getFolder_A01().setOpenDate(ldt);
		recordServices.update(folder);

		RelativeCriteria relativeCriteria = new RelativeCriteria();
		relativeCriteria.setRelativeSearchOperator(RelativeSearchOperator.EQUALS);
		CriteriaBuilder builder = newFolderCriteriaBuilderAsAdmin();
		builder.addCriterion(Folder.OPENING_DATE).searchOperator(SearchOperator.EQUALS).relativeSearchCriteria(relativeCriteria)
				.value(ldt);

		assertThat(recordIdsOfFolderCriteria(builder)).containsOnly(records.folder_A01);
	}

	//Relative between
	@Test
	public void givenAFolderWithOpeningDateWhenBuilderWithRelativeCriterionBetweenTheDateThenOneRecordFound()
			throws Exception {

		Folder folder = records.getFolder_A01().setOpenDate(now.toLocalDate());
		recordServices.update(folder);

		RelativeCriteria relativeCriteria = new RelativeCriteria();
		relativeCriteria.setRelativeSearchOperator(RelativeSearchOperator.EQUALS);
		relativeCriteria.setEndRelativeSearchOperator(RelativeSearchOperator.TODAY);
		CriteriaBuilder builder = newFolderCriteriaBuilderAsAdmin();
		builder.addCriterion(Folder.OPENING_DATE).searchOperator(SearchOperator.BETWEEN).relativeSearchCriteria(relativeCriteria)
				.value(now.toLocalDate().minusDays(1));

		assertThat(recordIdsOfFolderCriteria(builder)).containsOnly(records.folder_A01);
	}

	@Test
	public void givenAFolderWithOpeningDateWhenBuilderWithRelativeCriterionBetweenTheDateThenNoRecordFound()
			throws Exception {

		Folder folder = records.getFolder_A01().setOpenDate(now.toLocalDate().minusDays(1));
		recordServices.update(folder);

		RelativeCriteria relativeCriteria = new RelativeCriteria();
		relativeCriteria.setRelativeSearchOperator(RelativeSearchOperator.TODAY);
		relativeCriteria.setEndRelativeSearchOperator(RelativeSearchOperator.EQUALS);
		CriteriaBuilder builder = newFolderCriteriaBuilderAsAdmin();
		builder.addCriterion(Folder.OPENING_DATE).searchOperator(SearchOperator.BETWEEN).relativeSearchCriteria(relativeCriteria)
				.endValue(now.toLocalDate().plusDays(1));

		assertThat(recordIdsOfFolderCriteria(builder)).isEmpty();
	}

	//Relative lesserThan
	@Test
	public void givenAFolderWithOpeningDateWhenBuilderWithRelativeCriterionLesserThanTheDateThenOneRecordFound()
			throws Exception {

		Folder folder = records.getFolder_A01().setOpenDate(now.toLocalDate().plusDays(1));
		recordServices.update(folder);

		RelativeCriteria relativeCriteria = new RelativeCriteria();
		relativeCriteria.setRelativeSearchOperator(RelativeSearchOperator.TODAY);
		CriteriaBuilder builder = newFolderCriteriaBuilderAsAdmin();
		builder.addCriterion(Folder.OPENING_DATE).searchOperator(SearchOperator.LESSER_THAN)
				.relativeSearchCriteria(relativeCriteria);

		assertThat(recordIdsOfFolderCriteria(builder)).doesNotContain(records.folder_A01);
	}

	//Relative greaterThan
	@Test
	public void givenAFolderWithOpeningDateWhenBuilderWithRelativeCriterionGreaterThanTheDateThenOneRecordFound()
			throws Exception {

		Folder folder = records.getFolder_A01().setOpenDate(now.toLocalDate().plusDays(1));
		recordServices.update(folder);

		RelativeCriteria relativeCriteria = new RelativeCriteria();
		relativeCriteria.setRelativeSearchOperator(RelativeSearchOperator.TODAY);
		CriteriaBuilder builder = newFolderCriteriaBuilderAsAdmin();
		builder.addCriterion(Folder.OPENING_DATE).searchOperator(SearchOperator.GREATER_THAN)
				.relativeSearchCriteria(relativeCriteria);

		assertThat(recordIdsOfFolderCriteria(builder)).containsOnly(records.folder_A01);
	}

	//
	private List<String> recordIdsOfFolderCriteria(CriteriaBuilder criteriaBuilder)
			throws Exception {
		LogicalSearchCondition condition = folderConditionBuilder.build(criteriaBuilder.build());
		LogicalSearchQuery query = new LogicalSearchQuery(condition);
		return searchServices.searchRecordIds(query);
	}

	private CriteriaBuilder newFolderCriteriaBuilderAsAdmin() {
		SessionContext sessionContext = FakeSessionContext.adminInCollection(zeCollection);
		return new CriteriaBuilder(rm.folderSchemaType(), sessionContext);
	}
}

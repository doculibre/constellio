package com.constellio.app.ui.pages.search.criteria;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.search.criteria.Criterion.SearchOperator;
import com.constellio.app.ui.pages.search.criteria.RelativeCriteria.RelativeSearchOperator;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.search.query.logical.criteria.MeasuringUnitTime;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import org.assertj.core.api.ListAssert;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.constellio.app.ui.pages.search.criteria.Criterion.BooleanOperator.AND;
import static com.constellio.app.ui.pages.search.criteria.Criterion.BooleanOperator.OR;
import static org.assertj.core.api.Assertions.assertThat;

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

		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		folderConditionBuilder = new ConditionBuilder(rm.folderSchemaType(), "fr");
	}

	@Test
	public void testWithParens()
			throws Exception {

		// A && ( B || C)
		CriteriaBuilder builder = newFolderCriteriaBuilderAsAdmin();
		builder.addCriterion(Folder.ADMINISTRATIVE_UNIT).isEqualTo(records.unitId_10a).booleanOperator(AND);
		builder.addCriterion("title").isContainingText("Écureuil").withLeftParens().booleanOperator(OR);
		builder.addCriterion("title").isContainingText("grenouille").withRightParens();
		assertThat(recordIdsOfFolderCriteria(builder)).containsOnly(records.folder_A45, records.folder_A49);

		// A && ( B || (C & D) || E)
		builder = newFolderCriteriaBuilderAsAdmin();
		builder.addCriterion(Folder.ADMINISTRATIVE_UNIT).isEqualTo(records.unitId_10a).booleanOperator(AND);
		builder.addCriterion("title").isContainingText("Écureuil").withLeftParens().booleanOperator(OR);
		builder.addCriterion("title").isContainingText("Chauve").withLeftParens().booleanOperator(AND);
		builder.addCriterion("title").isContainingText("souris").withRightParens().booleanOperator(OR);
		builder.addCriterion("title").isContainingText("grenouille").withRightParens();
		assertThat(recordIdsOfFolderCriteria(builder)).containsOnly(records.folder_A45, records.folder_A49, records.folder_A17);

	}

	private ListAssert<String> assertWhenSearchingFolderTitleWithExactMatch(String text) {
		CriteriaBuilder builder = newFolderCriteriaBuilderAsAdmin();
		builder.addCriterion("title").isEqualTo(text);

		try {
			return assertThat(recordIdsOfFolderCriteria(builder));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private ListAssert<String> assertWhenSearchingFolderTitleWithoutExactMatch(String text) {
		CriteriaBuilder builder = newFolderCriteriaBuilderAsAdmin();
		builder.addCriterion("title").isContainingText(text);

		try {
			return assertThat(recordIdsOfFolderCriteria(builder));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	public void whenSearchingAnalyzedFieldAndExactExpressionWithFuzzySearchOperatorThenGoodResults()
			throws RecordServicesException {
		recordServices.update(records.getFolder_A48().setTitle("~Fuzzy S~earch"));

		assertWhenSearchingFolderTitleWithExactMatch("~Fuzzy S~earch").containsOnly("A48");
		assertWhenSearchingFolderTitleWithExactMatch("~fuzzy s~earch").isEmpty();
		assertWhenSearchingFolderTitleWithExactMatch("~fuzzy").isEmpty();

		assertWhenSearchingFolderTitleWithoutExactMatch("~Fuzzy").containsOnly("A48");
		assertWhenSearchingFolderTitleWithoutExactMatch("s~earch").containsOnly("A48");
		assertWhenSearchingFolderTitleWithoutExactMatch("~fuzzy*").containsOnly("A48");
		assertWhenSearchingFolderTitleWithoutExactMatch("~fuzzy").containsOnly("A48");
	}

	@Test
	public void whenSearchingAnalyzedFieldAndExactExpressionThenGoodResults()
			throws Exception {

		recordServices.update(records.getFolder_A48().setTitle("Gorille rose"));

		assertWhenSearchingFolderTitleWithExactMatch("Gorille rose").containsOnly("A48");
		assertWhenSearchingFolderTitleWithExactMatch("gorille rose").isEmpty();
		assertWhenSearchingFolderTitleWithExactMatch("Gorille").isEmpty();

		assertWhenSearchingFolderTitleWithoutExactMatch("Gorille").containsOnly("A48");
		assertWhenSearchingFolderTitleWithoutExactMatch("Gorilles").containsOnly("A48");
		assertWhenSearchingFolderTitleWithoutExactMatch("Goril").containsOnly("A48");
		assertWhenSearchingFolderTitleWithoutExactMatch("rose").containsOnly("A48");
		assertWhenSearchingFolderTitleWithoutExactMatch("rosé").containsOnly("A48");
		//assertWhenSearchingFolderTitleWithoutExactMatch("Gor?lle").containsOnly("A48");
		assertWhenSearchingFolderTitleWithoutExactMatch("Gori*").containsOnly("A48");
		//assertWhenSearchingFolderTitleWithoutExactMatch("ril").isEmpty();
		assertWhenSearchingFolderTitleWithoutExactMatch("gorille").containsOnly("A48");

		assertWhenSearchingFolderTitleWithExactMatch("Chauve-souris").containsOnly("A17");
		assertWhenSearchingFolderTitleWithExactMatch("Chauve souris").isEmpty();
		assertWhenSearchingFolderTitleWithExactMatch("Chauve").isEmpty();
		assertWhenSearchingFolderTitleWithExactMatch("souris").isEmpty();
		assertWhenSearchingFolderTitleWithExactMatch("Souris").containsOnly("A93");
		assertWhenSearchingFolderTitleWithExactMatch("Chauve-Souris").isEmpty();

		assertWhenSearchingFolderTitleWithoutExactMatch("Souris").containsOnly("A17", "A93");
		assertWhenSearchingFolderTitleWithoutExactMatch("souris").containsOnly("A17", "A93");
		assertWhenSearchingFolderTitleWithoutExactMatch("Chauve-souris").containsOnly("A17", "A93");
		assertWhenSearchingFolderTitleWithoutExactMatch("Chauve-Souris").containsOnly("A17", "A93");
		assertWhenSearchingFolderTitleWithoutExactMatch("Chauve").containsOnly("A17");
		assertWhenSearchingFolderTitleWithoutExactMatch("Chauve*").containsOnly("A17");
		assertWhenSearchingFolderTitleWithoutExactMatch("Chauv*").containsOnly("A17");
		assertWhenSearchingFolderTitleWithoutExactMatch("*souris").isEmpty();
		assertWhenSearchingFolderTitleWithoutExactMatch("*our*").contains("A17");

		assertWhenSearchingFolderTitleWithoutExactMatch("Cheval").containsOnly("A18");
		assertWhenSearchingFolderTitleWithoutExactMatch("Chevaux").containsOnly("A18");

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

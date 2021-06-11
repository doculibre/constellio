package com.constellio.app.ui.pages.search.criteria;

import com.constellio.app.ui.pages.search.criteria.Criterion.BooleanOperator;
import com.constellio.app.ui.pages.search.criteria.Criterion.SearchOperator;
import com.constellio.app.ui.pages.search.criteria.RelativeCriteria.RelativeSearchOperator;
import com.constellio.app.ui.pages.search.criteria.SearchCriterionTestSetup.CriterionTestRecord;
import com.constellio.app.ui.pages.search.criteria.SearchCriterionTestSetup.TestEnum;
import com.constellio.model.services.search.query.logical.criteria.MeasuringUnitTime;
import com.constellio.sdk.tests.ConstellioTest;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CriterionFactoryAcceptanceTest extends ConstellioTest {
	public static final String STRING_VALUE = "string value";
	public static final String STRING_VALUE_WITH_SPECIAL_CHARACTERS = "list:abc & def ; but not, ghi.";
	public static final int INT_VALUE = 42;
	public static final LocalDateTime DATE_VALUE = new LocalDateTime(2015, 1, 1, 0, 0, 0);
	public static final LocalDateTime DATE_END_VALUE = new LocalDateTime(2015, 1, 31, 23, 59, 59);
	public static final double DOUBLE_VALUE = 42.42;

	SearchCriterionTestSetup setup = new SearchCriterionTestSetup(zeCollection);
	CriterionTestRecord shortcuts = setup.getShortcuts();

	CriterionFactory factory;

	@Before
	public void setUp() {
		givenCollection(zeCollection);
		defineSchemasManager().using(setup);

		factory = new CriterionFactory();
	}

	@Test
	public void givenACriterionWithStringMetadataWhenSavingAndRestoringThenGetRestoredInstanceWithSameValuesAsSavedInstance()
			throws ConditionException {
		Criterion savedCriterion = stringCriterion();
		String serializedCriterion = factory.toString(savedCriterion);
		Criterion restoredCriterion = factory.build(serializedCriterion);
		String reserializedCriterion = factory.toString(restoredCriterion);
		assertThat(savedCriterion).isEqualToComparingFieldByField(restoredCriterion);
		assertThat(serializedCriterion).isEqualTo(reserializedCriterion);
	}

	@Test
	public void givenACriterionWithStructuredFactoryMetadataWhenSavingAndRestoringThenGetRestoredInstanceWithSameValuesAsSavedInstance()
			throws ConditionException {
		Criterion savedCriterion = structuredFactoryCriterion();
		String serializedCriterion = factory.toString(savedCriterion);
		Criterion restoredCriterion = factory.build(serializedCriterion);
		String reserializedCriterion = factory.toString(restoredCriterion);
		assertThat(savedCriterion).isEqualToComparingFieldByField(restoredCriterion);
		assertThat(serializedCriterion).isEqualTo(reserializedCriterion);
	}

	@Test
	public void givenACriterionWithSpecialCharactersInStringMetadataWhenSavingAndRestoringThenGetRestoredInstanceWithSameValuesAsSavedInstance()
			throws ConditionException {
		Criterion savedCriterion = stringWithSpecialCharactersCriterion();
		String serializedCriterion = factory.toString(savedCriterion);
		Criterion restoredCriterion = factory.build(serializedCriterion);
		String reserializedCriterion = factory.toString(restoredCriterion);
		assertThat(savedCriterion).isEqualToComparingFieldByField(restoredCriterion);
		assertThat(serializedCriterion).isEqualTo(reserializedCriterion);
	}

	@Test
	public void givenACriterionWithDoubleMetadataWhenSavingAndRestoringThenGetRestoredInstanceWithSameValuesAsSavedInstance()
			throws ConditionException {
		Criterion savedCriterion = doubleCriterion();
		String serializedCriterion = factory.toString(savedCriterion);
		Criterion restoredCriterion = factory.build(serializedCriterion);
		String reserializedCriterion = factory.toString(restoredCriterion);
		assertThat(savedCriterion).isEqualToComparingFieldByField(restoredCriterion);
		assertThat(serializedCriterion).isEqualTo(reserializedCriterion);
	}

	@Test
	public void givenACriterionWithBooleanMetadataWhenSavingAndRestoringThenGetRestoredInstanceWithSameValuesAsSavedInstance()
			throws ConditionException {
		Criterion savedCriterion = booleanCriterion();
		String serializedCriterion = factory.toString(savedCriterion);
		Criterion restoredCriterion = factory.build(serializedCriterion);
		String reserializedCriterion = factory.toString(restoredCriterion);
		assertThat(savedCriterion).isEqualToComparingFieldByField(restoredCriterion);
		assertThat(serializedCriterion).isEqualTo(reserializedCriterion);
	}

	@Test
	public void givenACriterionWithDateMetadataWhenSavingAndRestoringThenGetRestoredInstanceWithSameValuesAsSavedInstance()
			throws ConditionException {
		Criterion savedCriterion = betweenDateCriterion();
		String serializedCriterion = factory.toString(savedCriterion);
		Criterion restoredCriterion = factory.build(serializedCriterion);
		String reserializedCriterion = factory.toString(restoredCriterion);
		assertThat(savedCriterion).isEqualToComparingFieldByField(restoredCriterion);
		assertThat(serializedCriterion).isEqualTo(reserializedCriterion);
	}

	@Test
	public void givenACriterionWithEnumMetadataWhenSavingAndRestoringThenGetRestoredInstanceWithSameValuesAsSavedInstance()
			throws ConditionException {
		Criterion savedCriterion = enumCriterion();
		String serializedCriterion = factory.toString(savedCriterion);
		Criterion restoredCriterion = factory.build(serializedCriterion);
		String reserializedCriterion = factory.toString(restoredCriterion);
		assertThat(savedCriterion).isEqualToComparingFieldByField(restoredCriterion);
		assertThat(serializedCriterion).isEqualTo(reserializedCriterion);
	}

	@Test
	public void givenRelativeCriteriaEqualDateWhenSavingAndRestoringThenGetRestoredInstanceWithSameValuesAsSavedInstance()
			throws ConditionException {
		Criterion savedCriterion = relativeCriterionEqualDate();
		String serializedCriterion = factory.toString(savedCriterion);
		Criterion restoredCriterion = factory.build(serializedCriterion);
		String reserializedCriterion = factory.toString(restoredCriterion);
		assertThat(savedCriterion).isEqualToComparingFieldByField(restoredCriterion);
		assertThat(serializedCriterion).isEqualTo(reserializedCriterion);
	}

	@Test
	public void givenRelativeCriteriaEqualDateTimeWhenSavingAndRestoringThenGetRestoredInstanceWithSameValuesAsSavedInstance()
			throws ConditionException {
		Criterion savedCriterion = relativeCriterionEqualDate();
		String serializedCriterion = factory.toString(savedCriterion);
		Criterion restoredCriterion = factory.build(serializedCriterion);
		String reserializedCriterion = factory.toString(restoredCriterion);
		assertThat(savedCriterion).isEqualToComparingFieldByField(restoredCriterion);
		assertThat(serializedCriterion).isEqualTo(reserializedCriterion);
	}

	@Test
	public void givenRelativeCriteriaTodayWhenSavingAndRestoringThenGetRestoredInstanceWithSameValuesAsSavedInstance()
			throws ConditionException {
		Criterion savedCriterion = relativeCriterionTodayDate();
		String serializedCriterion = factory.toString(savedCriterion);
		Criterion restoredCriterion = factory.build(serializedCriterion);
		String reserializedCriterion = factory.toString(restoredCriterion);
		assertThat(savedCriterion).isEqualToComparingFieldByField(restoredCriterion);
		assertThat(serializedCriterion).isEqualTo(reserializedCriterion);
	}

	@Test
	public void givenRelativeCriteriaPastWhenSavingAndRestoringThenGetRestoredInstanceWithSameValuesAsSavedInstance()
			throws ConditionException {
		Criterion savedCriterion = relativeCriterionPastDate();
		String serializedCriterion = factory.toString(savedCriterion);
		Criterion restoredCriterion = factory.build(serializedCriterion);
		String reserializedCriterion = factory.toString(restoredCriterion);
		assertThat(savedCriterion).isEqualToComparingFieldByField(restoredCriterion);
		assertThat(serializedCriterion).isEqualTo(reserializedCriterion);
	}

	@Test
	public void givenRelativeCriteriaFutureWhenSavingAndRestoringThenGetRestoredInstanceWithSameValuesAsSavedInstance()
			throws ConditionException {
		Criterion savedCriterion = relativeCriterionFutureDate();
		String serializedCriterion = factory.toString(savedCriterion);
		Criterion restoredCriterion = factory.build(serializedCriterion);
		String reserializedCriterion = factory.toString(restoredCriterion);
		assertThat(savedCriterion).isEqualToComparingFieldByField(restoredCriterion);
		assertThat(serializedCriterion).isEqualTo(reserializedCriterion);
	}

	@Test
	public void givenRelativeCriteriaBetweenWhenSavingAndRestoringThenGetRestoredInstanceWithSameValuesAsSavedInstance()
			throws ConditionException {
		Criterion savedCriterion = relativeCriterionBetweenDate();
		String serializedCriterion = factory.toString(savedCriterion);
		Criterion restoredCriterion = factory.build(serializedCriterion);
		String reserializedCriterion = factory.toString(restoredCriterion);
		assertThat(savedCriterion).isEqualToComparingFieldByField(restoredCriterion);
		assertThat(serializedCriterion).isEqualTo(reserializedCriterion);
	}
	//

	private List<Criterion> complexCriterion() {
		// (criterionTestRecord_default_aString CONTAINS "string value" AND
		// NOT criterionTestRecord_aDouble > 42.42) OR
		// (criterionTestRecord_default_aBoolean is true AND
		// criterionTestRecord_default_aDate BETWEEN(2015-01-01, 2015-01-31) AND
		// criterionTestRecord_default_anEnum == TestEnum.VALUE1)
		List<Criterion> complex = new ArrayList<>();
		complex.add(criterion1());
		complex.add(criterion2());
		complex.add(criterion3());
		complex.add(betweenDateCriterion());
		complex.add(criterion4());
		return complex;
	}

	private Criterion criterion1() {
		Criterion s = stringCriterion();
		s.setLeftParens(true);
		s.setBooleanOperator(BooleanOperator.AND_NOT);
		return s;
	}

	private Criterion criterion2() {
		Criterion d = doubleCriterion();
		d.setRightParens(true);
		d.setBooleanOperator(BooleanOperator.OR);
		return d;
	}

	private Criterion criterion3() {
		Criterion b = booleanCriterion();
		b.setLeftParens(true);
		return b;
	}

	private Criterion criterion4() {
		Criterion e = enumCriterion();
		e.setRightParens(true);
		return e;
	}

	private Criterion stringCriterion() {
		// criterionTestRecord_default_aString CONTAINS "string value"
		Criterion criterion = new Criterion(shortcuts.code());
		criterion.setMetadata(shortcuts.aString().getCode(), shortcuts.aString().getType(), null);
		criterion.setSearchOperator(SearchOperator.CONTAINS_TEXT);
		criterion.setValue(STRING_VALUE);
		return criterion;
	}

	private Criterion structuredFactoryCriterion() {
		// criterionTestRecord_default_aString CONTAINS "string value"
		Criterion criterion = new Criterion(shortcuts.code());
		criterion.setMetadata(shortcuts.aSeparatedStructure().getCode(), shortcuts.aSeparatedStructure().getType(), null);
		criterion.setSearchOperator(SearchOperator.CONTAINS_TEXT);
		criterion.setValue(STRING_VALUE);
		return criterion;
	}

	private Criterion stringWithSpecialCharactersCriterion() {
		// criterionTestRecord_default_aString CONTAINS "string value"
		Criterion criterion = new Criterion(shortcuts.code());
		criterion.setMetadata(shortcuts.aString().getCode(), shortcuts.aString().getType(), null);
		criterion.setSearchOperator(SearchOperator.CONTAINS_TEXT);
		criterion.setValue(STRING_VALUE_WITH_SPECIAL_CHARACTERS);
		return criterion;
	}

	private Criterion intCriterion() {
		// DO NOT USE (YET) -- WE DO NOT SUPPORT INTEGER FIELDS
		// criterionTestRecord_default_anInt < 42
		Criterion criterion = new Criterion(shortcuts.code());
		criterion.setMetadata(shortcuts.aString().getCode(), shortcuts.anInt().getType(), null);
		criterion.setSearchOperator(SearchOperator.LESSER_THAN);
		criterion.setValue(INT_VALUE);
		return criterion;
	}

	private Criterion doubleCriterion() {
		// criterionTestRecord_aDouble > 42.42
		Criterion criterion = new Criterion(shortcuts.code());
		criterion.setMetadata(shortcuts.aDouble().getCode(), shortcuts.aDouble().getType(), null);
		criterion.setSearchOperator(SearchOperator.GREATER_THAN);
		criterion.setValue(DOUBLE_VALUE);
		return criterion;
	}

	private Criterion booleanCriterion() {
		// criterionTestRecord_default_aBoolean is true
		Criterion criterion = new Criterion(shortcuts.code());
		criterion.setMetadata(shortcuts.aBoolean().getCode(), shortcuts.aBoolean().getType(), null);
		criterion.setSearchOperator(SearchOperator.IS_TRUE);
		return criterion;
	}

	private Criterion betweenDateCriterion() {
		// criterionTestRecord_default_aDate BETWEEN(2015-01-01, 2015-01-31)
		Criterion criterion = new Criterion(shortcuts.code());
		criterion.setMetadata(shortcuts.aDate().getCode(), shortcuts.aDate().getType(), null);
		criterion.setSearchOperator(SearchOperator.BETWEEN);
		criterion.setValue(DATE_VALUE);
		criterion.setEndValue(DATE_END_VALUE);
		return criterion;
	}

	private Criterion enumCriterion() {
		// criterionTestRecord_default_anEnum == TestEnum.VALUE1
		Criterion criterion = new Criterion(shortcuts.code());
		criterion.setMetadata(shortcuts.anEnum().getCode(), shortcuts.anEnum().getType(),
				shortcuts.anEnum().getEnumClass().getName());
		criterion.setSearchOperator(SearchOperator.EQUALS);
		criterion.setValue(TestEnum.VALUE1);
		criterion.setEnumClassName(TestEnum.class.getName());
		return criterion;
	}

	private Criterion relativeCriterionEqualDate() {
		Criterion criterion = new Criterion(shortcuts.code());
		criterion.setMetadata(shortcuts.aDate().getCode(), shortcuts.aDate().getType(), null);
		criterion.setSearchOperator(SearchOperator.EQUALS);
		RelativeCriteria relativeCriteria = new RelativeCriteria();
		relativeCriteria.setRelativeSearchOperator(RelativeSearchOperator.EQUALS);
		criterion.setRelativeCriteria(relativeCriteria);
		criterion.setValue(DATE_VALUE);
		return criterion;
	}

	private Criterion relativeCriterionTodayDate() {
		Criterion criterion = new Criterion(shortcuts.code());
		criterion.setMetadata(shortcuts.aDate().getCode(), shortcuts.aDate().getType(), null);
		criterion.setSearchOperator(SearchOperator.GREATER_THAN);
		RelativeCriteria relativeCriteria = new RelativeCriteria();
		relativeCriteria.setRelativeSearchOperator(RelativeSearchOperator.TODAY);
		criterion.setRelativeCriteria(relativeCriteria);
		return criterion;
	}

	private Criterion relativeCriterionPastDate() {
		Criterion criterion = new Criterion(shortcuts.code());
		criterion.setMetadata(shortcuts.aDate().getCode(), shortcuts.aDate().getType(), null);
		criterion.setSearchOperator(SearchOperator.GREATER_THAN);
		RelativeCriteria relativeCriteria = new RelativeCriteria();
		relativeCriteria.setRelativeSearchOperator(RelativeSearchOperator.PAST);
		relativeCriteria.setMeasuringUnitTime(MeasuringUnitTime.DAYS);
		criterion.setRelativeCriteria(relativeCriteria);
		criterion.setValue(2.0);
		return criterion;
	}

	private Criterion relativeCriterionFutureDate() {
		Criterion criterion = new Criterion(shortcuts.code());
		criterion.setMetadata(shortcuts.aDate().getCode(), shortcuts.aDate().getType(), null);
		criterion.setSearchOperator(SearchOperator.GREATER_THAN);
		RelativeCriteria relativeCriteria = new RelativeCriteria();
		relativeCriteria.setRelativeSearchOperator(RelativeSearchOperator.FUTURE);
		relativeCriteria.setMeasuringUnitTime(MeasuringUnitTime.DAYS);
		criterion.setRelativeCriteria(relativeCriteria);
		criterion.setValue(2.0);
		return criterion;
	}

	private Criterion relativeCriterionBetweenDate() {
		Criterion criterion = new Criterion(shortcuts.code());
		criterion.setMetadata(shortcuts.aDate().getCode(), shortcuts.aDate().getType(), null);
		criterion.setSearchOperator(SearchOperator.BETWEEN);
		RelativeCriteria relativeCriteria = new RelativeCriteria();
		relativeCriteria.setRelativeSearchOperator(RelativeSearchOperator.PAST);
		relativeCriteria.setMeasuringUnitTime(MeasuringUnitTime.DAYS);
		relativeCriteria.setEndRelativeSearchOperator(RelativeSearchOperator.FUTURE);
		relativeCriteria.setEndMeasuringUnitTime(MeasuringUnitTime.DAYS);
		criterion.setRelativeCriteria(relativeCriteria);
		criterion.setValue(2.0);
		criterion.setEndValue(2.0);
		return criterion;
	}
}

/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.constellio.app.ui.pages.search.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

import com.constellio.app.ui.pages.search.criteria.Criterion.BooleanOperator;
import com.constellio.app.ui.pages.search.criteria.Criterion.SearchOperator;
import com.constellio.app.ui.pages.search.criteria.SearchCriterionTestSetup.CriterionTestRecord;
import com.constellio.app.ui.pages.search.criteria.SearchCriterionTestSetup.TestEnum;
import com.constellio.sdk.tests.ConstellioTest;

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
		Criterion savedCriterion = dateCriterion();
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
		complex.add(dateCriterion());
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

	private Criterion dateCriterion() {
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
}

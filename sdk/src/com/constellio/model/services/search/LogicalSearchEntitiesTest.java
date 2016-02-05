package com.constellio.model.services.search;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.all;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.allConditions;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.any;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.anyConditions;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.containing;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.containingText;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.endingWithText;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.in;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.isFalse;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.isFalseOrNull;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.isNotNull;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.isTrue;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.isTrueOrNull;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.notContainingElements;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.notIn;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.startingWithText;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.where;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.whereAll;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.whereAny;
import static com.constellio.sdk.tests.TestUtils.asList;
import static com.constellio.sdk.tests.TestUtils.chuckNorris;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.sdk.tests.ConstellioTest;

public class LogicalSearchEntitiesTest extends ConstellioTest {

	@Mock MetadataSchema schema;
	@Mock Metadata firstTextMetadata;
	@Mock Metadata referenceMetadata;
	@Mock Metadata secondTextMetadata;
	@Mock Metadata booleanMetadata;

	@Before
	public void setUp() {
		when(schema.getCollection()).thenReturn(zeCollection);
		when(firstTextMetadata.getCollection()).thenReturn(zeCollection);
		when(firstTextMetadata.getCode()).thenReturn("firstText");
		when(firstTextMetadata.getType()).thenReturn(MetadataValueType.STRING);
		when(firstTextMetadata.isMultivalue()).thenReturn(true);
		when(secondTextMetadata.getCollection()).thenReturn(zeCollection);
		when(secondTextMetadata.getCode()).thenReturn("secondText");
		when(secondTextMetadata.getType()).thenReturn(MetadataValueType.STRING);
		when(secondTextMetadata.isMultivalue()).thenReturn(true);
		when(referenceMetadata.getCollection()).thenReturn(zeCollection);
		when(referenceMetadata.getCode()).thenReturn("reference");
		when(referenceMetadata.getType()).thenReturn(MetadataValueType.REFERENCE);
		when(booleanMetadata.getCollection()).thenReturn(zeCollection);
		when(booleanMetadata.getCode()).thenReturn("boolean");
		when(booleanMetadata.getType()).thenReturn(MetadataValueType.BOOLEAN);

	}

	@Test
	public void testName()
			throws Exception {
		List<String> arrayList = new ArrayList<>(asList("allo", "bonjour"));
		List<String> asList = asList("allo", "bonjour");

		assertThat(arrayList).isEqualTo(arrayList);
		assertThat(arrayList).isEqualTo(asList);
		assertThat(asList).isEqualTo(arrayList);
		assertThat(asList).isEqualTo(asList);

	}

	@Test
	public void whenAllMetadataThenAnyWhereOnACriterionThenRootConditionIsTheAny()
			throws Exception {
		LogicalSearchCondition condition = from(schema).where(firstTextMetadata).isNotNull().andWhere(secondTextMetadata)
				.isContainingText("text").orWhere(firstTextMetadata).isNotContainingElements(Arrays.asList("othertext"));

		LogicalSearchCondition subCondition1 = where(firstTextMetadata).isNotNull();
		LogicalSearchCondition subCondition2 = where(secondTextMetadata).isContainingText("text");
		LogicalSearchCondition subCondition3 = where(firstTextMetadata).isNotContainingElements(Arrays.asList("othertext"));
		LogicalSearchCondition equalCondition = from(schema).whereAnyCondition(
				asList(allConditions(asList(subCondition1, subCondition2)), subCondition3));
		LogicalSearchCondition differentCondition = from(schema).whereAllConditions(
				asList(anyConditions(asList(subCondition2, subCondition1)), subCondition3));

		assertThat(condition).isEqualTo(equalCondition).isNotEqualTo(differentCondition);
	}

	@Test
	public void whenAnyMetadataThenAndWhereOnACriterionThenRootConditionIsTheAnd()
			throws Exception {
		LogicalSearchCondition condition = from(schema).where(firstTextMetadata).isNotNull().orWhere(secondTextMetadata)
				.isContainingText("text").andWhere(booleanMetadata).isFalse();

		LogicalSearchCondition subCondition1 = where(firstTextMetadata).isNotNull();
		LogicalSearchCondition subCondition2 = where(secondTextMetadata).isContainingText("text");
		LogicalSearchCondition subCondition3 = where(booleanMetadata).isFalse();
		LogicalSearchCondition equalCondition = from(schema).whereAllConditions(
				asList(anyConditions(asList(subCondition1, subCondition2)), subCondition3));
		LogicalSearchCondition differentCondition = from(schema).whereAnyCondition(
				asList(allConditions(asList(subCondition2, subCondition1)), subCondition3));

		assertThat(condition).isEqualTo(equalCondition).isNotEqualTo(differentCondition);
	}

	@Test
	public void whenUsingWhereAllOnOnUngoingConditionThenSameAsUsingWhereAndSubCondition()
			throws Exception {
		LogicalSearchCondition condition = from(schema).whereAll(Arrays.asList(firstTextMetadata, secondTextMetadata))
				.isNotNull();

		LogicalSearchCondition equalCondition = from(schema).where(
				whereAll(asList(firstTextMetadata, secondTextMetadata)).isNotNull());
		LogicalSearchCondition differentCondition = from(schema).where(
				whereAny(asList(secondTextMetadata, firstTextMetadata)).isNotNull());

		assertThat(condition).isEqualTo(equalCondition).isNotEqualTo(differentCondition);
	}

	@Test
	public void whenUsingWhereAllListOnOnUngoingConditionThenSameAsUsingWhereAndSubCondition()
			throws Exception {
		LogicalSearchCondition condition = from(schema).whereAll(asList(firstTextMetadata, secondTextMetadata)).isNotNull();

		LogicalSearchCondition equalCondition = from(schema).where(
				whereAll(asList(firstTextMetadata, secondTextMetadata)).isNotNull());
		LogicalSearchCondition differentCondition = from(schema).where(
				whereAny(asList(secondTextMetadata, firstTextMetadata)).isNotNull());

		assertThat(condition).isEqualTo(equalCondition).isNotEqualTo(differentCondition);
	}

	@Test
	public void whenUsingWhereAnyOnOnUngoingConditionThenSameAsUsingWhereAndSubCondition()
			throws Exception {
		LogicalSearchCondition condition = from(schema).whereAny(asList(firstTextMetadata, secondTextMetadata)).isNotNull();

		LogicalSearchCondition equalCondition = from(schema).where(
				whereAny(asList(firstTextMetadata, secondTextMetadata)).isNotNull());
		LogicalSearchCondition differentCondition = from(schema).where(
				whereAll(asList(secondTextMetadata, firstTextMetadata)).isNotNull());

		assertThat(condition).isEqualTo(equalCondition).isNotEqualTo(differentCondition);
	}

	@Test
	public void whenUsingWhereAnyListOnOnUngoingConditionThenSameAsUsingWhereAndSubCondition()
			throws Exception {
		LogicalSearchCondition condition = from(schema).whereAny(asList(firstTextMetadata, secondTextMetadata)).isNotNull();

		LogicalSearchCondition equalCondition = from(schema).where(
				whereAny(asList(firstTextMetadata, secondTextMetadata)).isNotNull());
		LogicalSearchCondition differentCondition = from(schema).where(
				whereAll(asList(secondTextMetadata, firstTextMetadata)).isNotNull());

		assertThat(condition).isEqualTo(equalCondition).isNotEqualTo(differentCondition);
	}

	@Test
	public void whenUsingAndOnValueClauseThenSameAsUsingIsAll()
			throws Exception {
		LogicalSearchCondition condition = from(schema).where(firstTextMetadata).isNotNull()
				.and(asList(startingWithText("prefix"), endingWithText("suffix")));

		LogicalSearchCondition equalCondition = from(schema).where(firstTextMetadata).isAll(
				asList(isNotNull(), startingWithText("prefix"), endingWithText("suffix")));

		LogicalSearchCondition differentCondition = from(schema).where(firstTextMetadata).isAll(
				asList(isNotNull(), all(asList(endingWithText("suffix"), startingWithText("prefix")))));

		assertThat(condition).isEqualTo(equalCondition).isNotEqualTo(differentCondition);
	}

	@Test
	public void whenUsingAndThenOrOnValueClauseThenSameAsUsingIsAllThenIsAny()
			throws Exception {
		LogicalSearchCondition condition = from(schema).where(firstTextMetadata).isNotNull()
				.and(asList(startingWithText("prefix"), endingWithText("suffix"))).or(asList(containingText(chuckNorris)));

		LogicalSearchCondition equalCondition = from(schema).where(firstTextMetadata).isAny(
				asList(all(asList(isNotNull(), startingWithText("prefix"), endingWithText("suffix"))),
						containingText(chuckNorris)));

		LogicalSearchCondition differentCondition = from(schema).where(firstTextMetadata).isAll(
				asList(isNotNull(), any(asList(endingWithText("suffix"), startingWithText("prefix")))));

		assertThat(condition).isEqualTo(equalCondition).isNotEqualTo(differentCondition);
	}

	@Test
	public void whenUsingOrThenAndOnValueClauseThenSameAsUsingIsAnyThenIsAll()
			throws Exception {
		LogicalSearchCondition condition = from(schema).where(firstTextMetadata).isNotNull()
				.or(asList(startingWithText("prefix"), endingWithText("suffix"))).and(asList(containingText(chuckNorris)));

		LogicalSearchCondition equalCondition = from(schema).where(firstTextMetadata).isAll(
				asList(any(asList(isNotNull(), startingWithText("prefix"), endingWithText("suffix"))),
						containingText(chuckNorris)));

		LogicalSearchCondition differentCondition = from(schema).where(firstTextMetadata).isAny(
				asList(isNotNull(), all(asList(endingWithText("suffix"), startingWithText("prefix")))));

		assertThat(condition).isEqualTo(equalCondition).isNotEqualTo(differentCondition);
	}

	@Test
	public void whenUsingIsNotThenSameAsUsingNot()
			throws Exception {
		LogicalSearchCondition condition = from(schema).where(firstTextMetadata).isNot(containingText(chuckNorris));

		LogicalSearchCondition equalCondition = from(schema).where(firstTextMetadata).isNot(containingText(chuckNorris));

		LogicalSearchCondition differentCondition = from(schema).where(firstTextMetadata).is(containingText(chuckNorris));

		assertThat(condition).isEqualTo(equalCondition).isNotEqualTo(differentCondition);
	}

	@Test
	public void whenIsContainingThenSameAsUsingContaining()
			throws Exception {
		LogicalSearchCondition condition = from(schema).where(firstTextMetadata).isContaining(asList("zeValue"));

		LogicalSearchCondition equalCondition = from(schema).where(firstTextMetadata).is(containing(asList("zeValue")));
		LogicalSearchCondition differentCondition = from(schema).where(firstTextMetadata).is(containing(asList("anotherValue")));

		assertThat(condition).isEqualTo(equalCondition).isNotEqualTo(differentCondition);
	}

	@Test
	public void whenIsNotContainingThenSameAsUsingNotContaining()
			throws Exception {
		LogicalSearchCondition condition = from(schema).where(firstTextMetadata).isNotContainingElements(asList("zeValue"));

		LogicalSearchCondition equalCondition = from(schema).where(firstTextMetadata).is(
				notContainingElements(asList("zeValue")));
		LogicalSearchCondition differentCondition = from(schema).where(firstTextMetadata).is(
				notContainingElements(asList("anotherValue")));

		assertThat(condition).isEqualTo(equalCondition).isNotEqualTo(differentCondition);
	}

	@Test
	public void whenIsInThenSameAsUsingIn()
			throws Exception {
		LogicalSearchCondition condition = from(schema).where(firstTextMetadata).isIn(Arrays.asList("value1", "value2"));

		LogicalSearchCondition equalCondition = from(schema).where(firstTextMetadata).is(in(Arrays.asList("value1", "value2")));
		LogicalSearchCondition differentCondition = from(schema).where(firstTextMetadata).is(
				in(Arrays.asList("value1", "value2", "value3")));

		assertThat(condition).isEqualTo(equalCondition).isNotEqualTo(differentCondition);
	}

	@Test
	public void whenIsNotInThenSameAsUsingNotIn()
			throws Exception {
		LogicalSearchCondition condition = from(schema).where(firstTextMetadata).isNotIn(Arrays.asList("value1", "value2"));

		LogicalSearchCondition equalCondition = from(schema).where(firstTextMetadata).is(
				notIn(Arrays.asList("value1", "value2")));
		LogicalSearchCondition differentCondition = from(schema).where(firstTextMetadata).is(
				notIn(Arrays.asList("value1", "value2", "value3")));

		assertThat(condition).isEqualTo(equalCondition).isNotEqualTo(differentCondition);
	}

	@Test
	public void whenIsNotInWithNestedQueryThenSameAsUsingNotIn()
			throws Exception {
		LogicalSearchCondition nestedSearch = from(schema).where(firstTextMetadata).isNotIn(Arrays.asList("value1", "value2"));
		LogicalSearchCondition otherNestedSearch = from(schema).where(firstTextMetadata).isNotIn(
				Arrays.asList("value1", "value2", "value3"));

		LogicalSearchCondition condition = from(schema).where(firstTextMetadata).isNotIn(Arrays.asList(nestedSearch));
		LogicalSearchCondition equalCondition = from(schema).where(firstTextMetadata).is(notIn(Arrays.asList(nestedSearch)));
		LogicalSearchCondition differentCondition = from(schema).where(firstTextMetadata).is(
				notIn(Arrays.asList(otherNestedSearch)));

		assertThat(condition).isEqualTo(equalCondition).isNotEqualTo(differentCondition);
	}

	@Test
	public void whenIsContainingTextThenSameAsUsingContainingWithText()
			throws Exception {
		LogicalSearchCondition condition = from(schema).where(firstTextMetadata).isContainingText("prefix");
		LogicalSearchCondition equalCondition = from(schema).where(firstTextMetadata).is(containingText("prefix"));
		LogicalSearchCondition differentCondition = from(schema).where(firstTextMetadata).is(containingText("prefix2"));

		assertThat(condition).isEqualTo(equalCondition).isNotEqualTo(differentCondition);
	}

	@Test
	public void whenIsStartingWithTextThenSameAsUsingStartingWithText()
			throws Exception {
		LogicalSearchCondition condition = from(schema).where(firstTextMetadata).isStartingWithText("prefix");
		LogicalSearchCondition equalCondition = from(schema).where(firstTextMetadata).is(startingWithText("prefix"));
		LogicalSearchCondition differentCondition = from(schema).where(firstTextMetadata).is(startingWithText("prefix2"));

		assertThat(condition).isEqualTo(equalCondition).isNotEqualTo(differentCondition);
	}

	@Test
	public void whenIsEndingWithTextThenSameAsUsingEndingWithText()
			throws Exception {
		LogicalSearchCondition condition = from(schema).where(firstTextMetadata).isEndingWithText("prefix");
		LogicalSearchCondition equalCondition = from(schema).where(firstTextMetadata).is(endingWithText("prefix"));
		LogicalSearchCondition differentCondition = from(schema).where(firstTextMetadata).is(endingWithText("prefix2"));

		assertThat(condition).isEqualTo(equalCondition).isNotEqualTo(differentCondition);
	}

	@Test
	public void whenIsFalseThenSameAsUsingIsFalse()
			throws Exception {
		LogicalSearchCondition condition = from(schema).where(booleanMetadata).isFalse();

		LogicalSearchCondition equalCondition = from(schema).where(booleanMetadata).is(isFalse());
		LogicalSearchCondition differentCondition = from(schema).where(booleanMetadata).is(isFalseOrNull());

		assertThat(condition).isEqualTo(equalCondition).isNotEqualTo(differentCondition);
	}

	@Test
	public void whenIsFalseOrNullThenSameAsUsingIsFalseOrNull()
			throws Exception {
		LogicalSearchCondition condition = from(schema).where(booleanMetadata).isFalseOrNull();

		LogicalSearchCondition equalCondition = from(schema).where(booleanMetadata).is(isFalseOrNull());
		LogicalSearchCondition differentCondition = from(schema).where(booleanMetadata).is(isTrueOrNull());

		assertThat(condition).isEqualTo(equalCondition).isNotEqualTo(differentCondition);
	}

	@Test
	public void whenIsTrueThenSameAsUsingIsTrue()
			throws Exception {
		LogicalSearchCondition condition = from(schema).where(booleanMetadata).isTrue();

		LogicalSearchCondition equalCondition = from(schema).where(booleanMetadata).is(isTrue());
		LogicalSearchCondition differentCondition = from(schema).where(booleanMetadata).is(isFalse());

		assertThat(condition).isEqualTo(equalCondition).isNotEqualTo(differentCondition);
	}

	@Test
	public void whenIsTrueOrNullThenSameAsUsingIsFalseOrNull()
			throws Exception {
		LogicalSearchCondition condition = from(schema).where(booleanMetadata).isTrueOrNull();

		LogicalSearchCondition equalCondition = from(schema).where(booleanMetadata).is(isTrueOrNull());
		LogicalSearchCondition differentCondition = from(schema).where(booleanMetadata).is(isTrue());

		assertThat(condition).isEqualTo(equalCondition).isNotEqualTo(differentCondition);
	}

	// @Test(expected = LogicalSearchConditionRuntimeException.UnsupportedConditionForMetadata.class)
	// public void whenUsingInvalidConditionWithinIsThenRuntimeException() throws Exception {
	// from(schema).where(booleanMetadata).is(endingWithText("t"));
	// }
	//
	// @Test(expected = LogicalSearchConditionRuntimeException.UnsupportedConditionForMetadata.class)
	// public void whenUsingInvalidConditionWithinNotThenRuntimeException() throws Exception {
	// from(schema).where(booleanMetadata).is(not(endingWithText("t")));
	// }
	//
	// @Test(expected = LogicalSearchConditionRuntimeException.UnsupportedConditionForMetadata.class)
	// public void whenUsingInvalidConditionWithinIsNotThenRuntimeException() throws Exception {
	// from(schema).where(booleanMetadata).isNot(endingWithText("t"));
	// }
	//
	// @Test(expected = LogicalSearchConditionRuntimeException.UnsupportedConditionForMetadata.class)
	// public void whenUsingContainingTextOnNonTextMetadataThenRuntimeException() throws Exception {
	// from(schema).where(booleanMetadata).isContainingText("t");
	// }
	//
	// @Test(expected = LogicalSearchConditionRuntimeException.UnsupportedConditionForMetadata.class)
	// public void whenUsingStartingWithTextOnNonTextMetadataThenRuntimeException() throws Exception {
	// from(schema).where(booleanMetadata).isStartingWithText("t");
	// }
	//
	// @Test(expected = LogicalSearchConditionRuntimeException.UnsupportedConditionForMetadata.class)
	// public void whenUsingEndingWithTextOnNonTextMetadataThenRuntimeException() throws Exception {
	// from(schema).where(booleanMetadata).isEndingWithText("t");
	// }
	//
	// @Test(expected = LogicalSearchConditionRuntimeException.UnsupportedConditionForMetadata.class)
	// public void whenUsingIsTrueOnNonBooleanMetadataThenRuntimeException() throws Exception {
	// from(schema).where(firstTextMetadata).isTrue();
	// }
	//
	// @Test(expected = LogicalSearchConditionRuntimeException.UnsupportedConditionForMetadata.class)
	// public void whenUsingIsFalseOnNonBooleanMetadataThenRuntimeException() throws Exception {
	// from(schema).where(firstTextMetadata).isFalse();
	// }
	//
	// @Test(expected = LogicalSearchConditionRuntimeException.UnsupportedConditionForMetadata.class)
	// public void whenUsingIsTrueOrNullOnNonBooleanMetadataThenRuntimeException() throws Exception {
	// from(schema).where(firstTextMetadata).isTrueOrNull();
	// }
	//
	// @Test(expected = LogicalSearchConditionRuntimeException.UnsupportedConditionForMetadata.class)
	// public void whenUsingIsFalseOrNullOnNonBooleanMetadataThenRuntimeException() throws Exception {
	// from(schema).where(firstTextMetadata).isFalseOrNull();
	// }

}

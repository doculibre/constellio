package com.constellio.model.services.search.query;

import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SolrFilterBuilderTest extends ConstellioTest {

	SolrFilterBuilder builder;

	@Test
	public void givenEmptyFilterThenReturnedValueDependsOnConfig() {

		builder = SolrFilterBuilder.createAndFilterReturningFalseIfEmpty();
		assertThat(builder.build()).isEqualTo("collection_s:_A38_");
		assertThat(builder.isAlwaysTrue()).isFalse();

		builder = SolrFilterBuilder.createAndFilterReturningTrueIfEmpty();
		assertThat(builder.build()).isEqualTo("*:*");
		assertThat(builder.isAlwaysTrue()).isTrue();

		builder = SolrFilterBuilder.createOrFilterReturningFalseIfEmpty();
		assertThat(builder.build()).isEqualTo("collection_s:_A38_");
		assertThat(builder.isAlwaysTrue()).isFalse();

		builder = SolrFilterBuilder.createOrFilterReturningTrueIfEmpty();
		assertThat(builder.build()).isEqualTo("*:*");
		assertThat(builder.isAlwaysTrue()).isTrue();


	}

	@Test
	public void givenFilterWithOneConditionThenNoParenthesis() {

		builder = SolrFilterBuilder.createAndFilterReturningFalseIfEmpty();
		builder.append("field_s", "value");
		assertThat(builder.build()).isEqualTo("field_s:value");
		assertThat(builder.isAlwaysTrue()).isFalse();

		builder = SolrFilterBuilder.createAndFilterReturningTrueIfEmpty();
		builder.append("field_s", "value");
		assertThat(builder.build()).isEqualTo("field_s:value");
		assertThat(builder.isAlwaysTrue()).isFalse();

		builder = SolrFilterBuilder.createOrFilterReturningFalseIfEmpty();
		builder.append("field_s", "value");
		assertThat(builder.build()).isEqualTo("field_s:value");
		assertThat(builder.isAlwaysTrue()).isFalse();

		builder = SolrFilterBuilder.createOrFilterReturningTrueIfEmpty();
		builder.append("field_s", "value");
		assertThat(builder.build()).isEqualTo("field_s:value");
		assertThat(builder.isAlwaysTrue()).isFalse();

	}

	@Test
	public void givenFilterWithMultipleConditionsThenUseConfiguredRootOperation() {

		builder = SolrFilterBuilder.createAndFilterReturningFalseIfEmpty();
		builder.append("field_s", "value1");
		builder.appendNegative("field_s", "value2");
		assertThat(builder.build()).isEqualTo("field_s:value1 AND -field_s:value2");
		assertThat(builder.isAlwaysTrue()).isFalse();

		builder = SolrFilterBuilder.createAndFilterReturningTrueIfEmpty();
		builder.append("field_s", "value1");
		builder.appendNegative("field_s", "value2");
		assertThat(builder.build()).isEqualTo("field_s:value1 AND -field_s:value2");
		assertThat(builder.isAlwaysTrue()).isFalse();

		builder = SolrFilterBuilder.createOrFilterReturningFalseIfEmpty();
		builder.append("field_s", "value1");
		builder.appendNegative("field_s", "value2");
		assertThat(builder.build()).isEqualTo("field_s:value1 OR -field_s:value2");
		assertThat(builder.isAlwaysTrue()).isFalse();

		builder = SolrFilterBuilder.createOrFilterReturningTrueIfEmpty();
		builder.append("field_s", "value1");
		builder.appendNegative("field_s", "value2");
		assertThat(builder.build()).isEqualTo("field_s:value1 OR -field_s:value2");
		assertThat(builder.isAlwaysTrue()).isFalse();
	}

	@Test
	public void givenFilterWithEmptyGroupReturningFalseIfEmptyThenAlwaysHaveTheImpossibleCondition() {


		builder = SolrFilterBuilder.createAndFilterReturningFalseIfEmpty();
		builder.openORGroupReturningFalseIfEmpty();
		builder.closeGroup();
		assertThat(builder.build()).isEqualTo("(collection_s:_A38_)");
		assertThat(builder.isAlwaysTrue()).isFalse();

		builder = SolrFilterBuilder.createAndFilterReturningTrueIfEmpty();
		builder.openORGroupReturningFalseIfEmpty();
		builder.closeGroup();
		assertThat(builder.build()).isEqualTo("(collection_s:_A38_)");
		assertThat(builder.isAlwaysTrue()).isFalse();

		builder = SolrFilterBuilder.createOrFilterReturningFalseIfEmpty();
		builder.openORGroupReturningFalseIfEmpty();
		builder.closeGroup();
		assertThat(builder.build()).isEqualTo("(collection_s:_A38_)");
		assertThat(builder.isAlwaysTrue()).isFalse();

		builder = SolrFilterBuilder.createOrFilterReturningTrueIfEmpty();
		builder.openORGroupReturningFalseIfEmpty();
		builder.closeGroup();
		assertThat(builder.build()).isEqualTo("(collection_s:_A38_)");
		assertThat(builder.isAlwaysTrue()).isFalse();


		builder = SolrFilterBuilder.createAndFilterReturningFalseIfEmpty();
		builder.openANDGroupReturningFalseIfEmpty();
		builder.closeGroup();
		assertThat(builder.build()).isEqualTo("(collection_s:_A38_)");
		assertThat(builder.isAlwaysTrue()).isFalse();

		builder = SolrFilterBuilder.createAndFilterReturningTrueIfEmpty();
		builder.openANDGroupReturningFalseIfEmpty();
		builder.closeGroup();
		assertThat(builder.build()).isEqualTo("(collection_s:_A38_)");
		assertThat(builder.isAlwaysTrue()).isFalse();

		builder = SolrFilterBuilder.createOrFilterReturningFalseIfEmpty();
		builder.openANDGroupReturningFalseIfEmpty();
		builder.closeGroup();
		assertThat(builder.build()).isEqualTo("(collection_s:_A38_)");
		assertThat(builder.isAlwaysTrue()).isFalse();

		builder = SolrFilterBuilder.createOrFilterReturningTrueIfEmpty();
		builder.openANDGroupReturningFalseIfEmpty();
		builder.closeGroup();
		assertThat(builder.build()).isEqualTo("(collection_s:_A38_)");
		assertThat(builder.isAlwaysTrue()).isFalse();
	}


	@Test
	public void givenFilterWithEmptyGroupReturningTrueIfEmptyThenAlwaysHaveTheImpossibleCondition() {


		builder = SolrFilterBuilder.createAndFilterReturningFalseIfEmpty();
		builder.openORGroupReturningTrueIfEmpty();
		builder.closeGroup();
		assertThat(builder.build()).isEqualTo("(*:*)");
		assertThat(builder.isAlwaysTrue()).isFalse();

		builder = SolrFilterBuilder.createAndFilterReturningTrueIfEmpty();
		builder.openORGroupReturningTrueIfEmpty();
		builder.closeGroup();
		assertThat(builder.build()).isEqualTo("(*:*)");
		assertThat(builder.isAlwaysTrue()).isFalse();

		builder = SolrFilterBuilder.createOrFilterReturningFalseIfEmpty();
		builder.openORGroupReturningTrueIfEmpty();
		builder.closeGroup();
		assertThat(builder.build()).isEqualTo("(*:*)");
		assertThat(builder.isAlwaysTrue()).isFalse();

		builder = SolrFilterBuilder.createOrFilterReturningTrueIfEmpty();
		builder.openORGroupReturningTrueIfEmpty();
		builder.closeGroup();
		assertThat(builder.build()).isEqualTo("(*:*)");
		assertThat(builder.isAlwaysTrue()).isFalse();


		builder = SolrFilterBuilder.createAndFilterReturningFalseIfEmpty();
		builder.openANDGroupReturningTrueIfEmpty();
		builder.closeGroup();
		assertThat(builder.build()).isEqualTo("(*:*)");
		assertThat(builder.isAlwaysTrue()).isFalse();

		builder = SolrFilterBuilder.createAndFilterReturningTrueIfEmpty();
		builder.openANDGroupReturningTrueIfEmpty();
		builder.closeGroup();
		assertThat(builder.build()).isEqualTo("(*:*)");
		assertThat(builder.isAlwaysTrue()).isFalse();

		builder = SolrFilterBuilder.createOrFilterReturningFalseIfEmpty();
		builder.openANDGroupReturningTrueIfEmpty();
		builder.closeGroup();
		assertThat(builder.build()).isEqualTo("(*:*)");
		assertThat(builder.isAlwaysTrue()).isFalse();

		builder = SolrFilterBuilder.createOrFilterReturningTrueIfEmpty();
		builder.openANDGroupReturningTrueIfEmpty();
		builder.closeGroup();
		assertThat(builder.build()).isEqualTo("(*:*)");
		assertThat(builder.isAlwaysTrue()).isFalse();
	}


	@Test
	public void givenFilterWithEmptyGroupRemovedIfEmptyThenAlwaysHaveTheImpossibleCondition() {


		builder = SolrFilterBuilder.createAndFilterReturningFalseIfEmpty();
		builder.openORGroupRemovedIfEmpty();
		builder.closeGroup();
		assertThat(builder.build()).isEqualTo("collection_s:_A38_");
		assertThat(builder.isAlwaysTrue()).isFalse();

		builder = SolrFilterBuilder.createAndFilterReturningTrueIfEmpty();
		builder.openORGroupRemovedIfEmpty();
		builder.closeGroup();
		assertThat(builder.build()).isEqualTo("*:*");
		assertThat(builder.isAlwaysTrue()).isTrue();

		builder = SolrFilterBuilder.createOrFilterReturningFalseIfEmpty();
		builder.openORGroupRemovedIfEmpty();
		builder.closeGroup();
		assertThat(builder.build()).isEqualTo("collection_s:_A38_");
		assertThat(builder.isAlwaysTrue()).isFalse();

		builder = SolrFilterBuilder.createOrFilterReturningTrueIfEmpty();
		builder.openORGroupRemovedIfEmpty();
		builder.closeGroup();
		assertThat(builder.build()).isEqualTo("*:*");
		assertThat(builder.isAlwaysTrue()).isTrue();


		builder = SolrFilterBuilder.createAndFilterReturningFalseIfEmpty();
		builder.openANDGroupRemovedIfEmpty();
		builder.closeGroup();
		assertThat(builder.build()).isEqualTo("collection_s:_A38_");
		assertThat(builder.isAlwaysTrue()).isFalse();

		builder = SolrFilterBuilder.createAndFilterReturningTrueIfEmpty();
		builder.openANDGroupRemovedIfEmpty();
		builder.closeGroup();
		assertThat(builder.build()).isEqualTo("*:*");
		assertThat(builder.isAlwaysTrue()).isTrue();

		builder = SolrFilterBuilder.createOrFilterReturningFalseIfEmpty();
		builder.openANDGroupRemovedIfEmpty();
		builder.closeGroup();
		assertThat(builder.build()).isEqualTo("collection_s:_A38_");
		assertThat(builder.isAlwaysTrue()).isFalse();

		builder = SolrFilterBuilder.createOrFilterReturningTrueIfEmpty();
		builder.openANDGroupRemovedIfEmpty();
		builder.closeGroup();
		assertThat(builder.build()).isEqualTo("*:*");
		assertThat(builder.isAlwaysTrue()).isTrue();
	}


	@Test
	public void givenComplexFilterThenValid() {


		builder = SolrFilterBuilder.createAndFilterReturningTrueIfEmpty();
		builder.openORGroupRemovedIfEmpty();
		builder.append("field_s", "value1");
		builder.openORGroupReturningFalseIfEmpty();
		builder.closeGroup();
		builder.append("field_s", "value2");
		builder.closeGroup();
		builder.append("field_s", "value3");
		builder.openANDGroupRemovedIfEmpty();
		builder.closeGroup();
		builder.append("field_s", "value4");

		builder.openORGroupReturningFalseIfEmpty();
		builder.openANDGroupReturningTrueIfEmpty();
		builder.append("field_s", "value5");
		builder.append("field_s", "value6");
		builder.closeGroup();
		builder.openANDGroupReturningFalseIfEmpty();
		builder.append("field_s", "value7");
		builder.append("field_s", "value8");
		builder.closeGroup();
		builder.closeGroup();

		assertThat(builder.build()).isEqualTo("(field_s:value1 OR (collection_s:_A38_) OR field_s:value2) AND field_s:value3 AND field_s:value4 AND ((field_s:value5 AND field_s:value6) OR (field_s:value7 AND field_s:value8))");
		assertThat(builder.isAlwaysTrue()).isFalse();

	}


}

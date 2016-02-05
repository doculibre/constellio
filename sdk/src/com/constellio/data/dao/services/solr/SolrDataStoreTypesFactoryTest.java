package com.constellio.data.dao.services.solr;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.constellio.sdk.tests.ConstellioTest;

public class SolrDataStoreTypesFactoryTest extends ConstellioTest {

	SolrDataStoreTypesFactory typesFactory = new SolrDataStoreTypesFactory();

	@Test
	public void whenSingleStringThenUseTypeCorrectSolrType()
			throws Exception {
		assertThat(typesFactory.forString(false)).isEqualTo("s");
	}

	@Test
	public void whenMultipleStringThenUseTypeCorrectSolrType()
			throws Exception {
		assertThat(typesFactory.forString(true)).isEqualTo("ss");
	}

	@Test
	public void whenSingleDoubleThenUseTypeCorrectSolrType()
			throws Exception {
		assertThat(typesFactory.forDouble(false)).isEqualTo("d");
	}

	@Test
	public void whenMultipleDoubleThenUseTypeCorrectSolrType()
			throws Exception {
		assertThat(typesFactory.forDouble(true)).isEqualTo("ds");
	}

	@Test
	public void whenSingleDateThenUseTypeCorrectSolrType()
			throws Exception {
		assertThat(typesFactory.forDate(false)).isEqualTo("da");
	}

	@Test
	public void whenMultipleDateThenUseTypeCorrectSolrType()
			throws Exception {
		assertThat(typesFactory.forDate(true)).isEqualTo("das");
	}

	@Test
	public void whenSingleDateTimeThenUseTypeCorrectSolrType()
			throws Exception {
		assertThat(typesFactory.forDateTime(false)).isEqualTo("dt");
	}

	@Test
	public void whenMultipleDateTimeThenUseTypeCorrectSolrType()
			throws Exception {
		assertThat(typesFactory.forDateTime(true)).isEqualTo("dts");
	}

	@Test
	public void whenSingleBooleanThenUseTypeCorrectSolrType()
			throws Exception {
		assertThat(typesFactory.forBoolean(false)).isEqualTo("s");
	}

	@Test
	public void whenMultipleBooleanThenUseTypeCorrectSolrType()
			throws Exception {
		assertThat(typesFactory.forBoolean(true)).isEqualTo("ss");
	}
}

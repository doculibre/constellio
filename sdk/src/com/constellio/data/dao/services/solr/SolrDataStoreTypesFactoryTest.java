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

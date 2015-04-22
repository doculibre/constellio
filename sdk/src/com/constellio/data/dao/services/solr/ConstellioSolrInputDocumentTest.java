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

import static com.constellio.sdk.tests.TestUtils.asMap;
import static java.util.Arrays.asList;

import org.junit.Test;

import com.constellio.data.utils.ImpossibleRuntimeException;

public class ConstellioSolrInputDocumentTest {

	float boost = 1.0f;

	ConstellioSolrInputDocument document = new ConstellioSolrInputDocument();

	@Test
	public void whenSetNonNullKeyAndValueThenOK()
			throws Exception {
		document.setField("key", 42);
	}

	@Test(expected = ImpossibleRuntimeException.class)
	public void whenSetNullValueThenException()
			throws Exception {
		document.setField("key", null);
	}

	@Test(expected = ImpossibleRuntimeException.class)
	public void whenSetListWithNullValueThenException()
			throws Exception {
		document.setField("key", asList((String) null));
	}

	@Test(expected = ImpossibleRuntimeException.class)
	public void whenSetMapWithNullValueThenException()
			throws Exception {
		document.setField("key", asMap("set", (String) null));
	}

	@Test(expected = ImpossibleRuntimeException.class)
	public void whenSetMapWithListWithNullValueThenException()
			throws Exception {
		document.setField("key", asMap("set", asList((String) null)));
	}

	@Test(expected = ImpossibleRuntimeException.class)
	public void whenSetNullNameThenException()
			throws Exception {
		document.setField(null, 42);
	}

	@Test(expected = ImpossibleRuntimeException.class)
	public void whenSetIdWithNonStringValueThenException()
			throws Exception {
		document.setField("id", 42);
	}

	@Test
	public void whenAddNonNullKeyAndValueThenOK()
			throws Exception {
		document.addField("key", 42);
	}

	@Test(expected = ImpossibleRuntimeException.class)
	public void whenAddNullValueThenException()
			throws Exception {
		document.addField("key", null);
	}

	@Test(expected = ImpossibleRuntimeException.class)
	public void whenAddNullNameThenException()
			throws Exception {
		document.addField(null, 42);
	}

	@Test(expected = ImpossibleRuntimeException.class)
	public void whenAddIdWithNonStringValueThenException()
			throws Exception {
		document.addField("id", 42);
	}

	@Test
	public void whenSetNonNullKeyAndValueAndBoostThenOK()
			throws Exception {
		document.setField("key", 42, boost);
	}

	@Test(expected = ImpossibleRuntimeException.class)
	public void whenSetNullValueAndBoostThenException()
			throws Exception {
		document.setField("key", null, boost);
	}

	@Test(expected = ImpossibleRuntimeException.class)
	public void whenSetListWithNullValueAndBoostThenException()
			throws Exception {
		document.setField("key", asList((String) null), boost);
	}

	@Test(expected = ImpossibleRuntimeException.class)
	public void whenSetMapWithNullValueAndBoostThenException()
			throws Exception {
		document.setField("key", asMap("set", (String) null), boost);
	}

	@Test(expected = ImpossibleRuntimeException.class)
	public void whenSetMapWithListWithNullValueAndBoostThenException()
			throws Exception {
		document.setField("key", asMap("set", asList((String) null)), boost);
	}

	@Test(expected = ImpossibleRuntimeException.class)
	public void whenSetNullNameAndBoostThenException()
			throws Exception {
		document.setField(null, 42, boost);
	}

	@Test(expected = ImpossibleRuntimeException.class)
	public void whenSetIdWithNonStringValueAndBoostThenException()
			throws Exception {
		document.setField("id", 42, boost);
	}
}

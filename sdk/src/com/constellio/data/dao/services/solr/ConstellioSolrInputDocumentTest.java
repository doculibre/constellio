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

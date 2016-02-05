package com.constellio.app.modules.es.connectors.http;

import static com.constellio.app.modules.es.connectors.http.ConnectorHttpUtils.toAbsoluteHRef;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.constellio.sdk.tests.ConstellioTest;

public class ConnectorHttpUtilsAcceptanceTest extends ConstellioTest {

	@Test
	public void whenGetAbsoluteHrefThenConvertRelativeUrlToAbsolute()
			throws Exception {

		assertThat(toAbsoluteHRef("http://a.com/b/c", "../../ze.txt")).isEqualTo("http://a.com/ze.txt");
		assertThat(toAbsoluteHRef("http://a.com/b/c/d", "../ze.txt")).isEqualTo("http://a.com/b/c/ze.txt");
		assertThat(toAbsoluteHRef("http://a.com/b/c/d", "ze.txt")).isEqualTo("http://a.com/b/c/d/ze.txt");
		assertThat(toAbsoluteHRef("http://a.com/b/c/d/", "ze.txt")).isEqualTo("http://a.com/b/c/d/ze.txt");
		assertThat(toAbsoluteHRef("http://a.com/b/c/d", "/ze.txt")).isEqualTo("http://a.com/ze.txt");
		assertThat(toAbsoluteHRef("http://a.com/b/c/d/", "/ze.txt")).isEqualTo("http://a.com/ze.txt");
		assertThat(toAbsoluteHRef("http://a.com/b/c/d", "http://a.com/ze.txt")).isEqualTo("http://a.com/ze.txt");
		assertThat(toAbsoluteHRef("http://a.com/b/c/d/", "http://a.com/b/ze.txt")).isEqualTo("http://a.com/b/ze.txt");

	}
}

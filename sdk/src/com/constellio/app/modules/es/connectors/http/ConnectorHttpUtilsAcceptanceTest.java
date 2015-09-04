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

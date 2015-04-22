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
package com.constellio.model.services.contents;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.Test;

import com.constellio.model.entities.records.ParsedContent;
import com.constellio.sdk.tests.TestUtils;

public class ParsedContentConverterTest {

	@Test
	public void whenWritingThenReadingParsedContentThenSame()
			throws Exception {

		String content = "]]></xml> test <![CDATA[";
		String lang = "elvish";
		String mime = "oldText";
		long length = 666;
		Map<String, Object> properties = TestUtils.asMap("k1", "v1=", "k2", "v2");
		//Mettre des listes pour voir

		ParsedContentConverter converter = new ParsedContentConverter();

		ParsedContent parsedContent = new ParsedContent(content, lang, mime, length, properties);
		String parsedContentAsString = converter.convertToString(parsedContent);
		ParsedContent parsedContent2 = converter.convertToParsedContent(parsedContentAsString);
		String parsedContentAsString2 = converter.convertToString(parsedContent2);

		assertThat(parsedContent2.getLanguage()).isEqualTo(lang);
		assertThat(parsedContent2.getMimeType()).isEqualTo(mime);
		assertThat(parsedContent2.getLength()).isEqualTo(length);
		assertThat(parsedContent2.getParsedContent()).isEqualTo(content);
		assertThat(parsedContent2.getProperties()).hasSize(2).containsEntry("k1", "v1=").containsEntry("k2", "v2");
		assertThat(parsedContentAsString2).isEqualTo(parsedContentAsString);

	}

}

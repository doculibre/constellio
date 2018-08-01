package com.constellio.model.services.contents;

import com.constellio.model.entities.records.ParsedContent;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.MapEntry.entry;

public class ParsedContentConverterTest {

	@Test
	public void whenWritingThenReadingParsedContentThenSame()
			throws Exception {

		String content = "]]></xml> test\n <![CDATA[";
		String lang = "elvish";
		String mime = "oldText";
		long length = 666;

		Map<String, List<String>> styles = new HashMap<>();
		styles.put("style1\n", asList("value1=", "value2\nvalue2b"));
		styles.put("style2\n\r", asList("value3:", "value4\""));

		Map<String, Object> properties = new HashMap<>();
		properties.put("k1\n", "v1=");
		properties.put("k2\n\r", "v2");

		ParsedContentConverter converter = new ParsedContentConverter();

		ParsedContent parsedContent = new ParsedContent(content, lang, mime, length, properties, styles);
		String parsedContentAsString = converter.convertToString(parsedContent);
		ParsedContent parsedContent2 = converter.convertToParsedContent(parsedContentAsString);
		String parsedContentAsString2 = converter.convertToString(parsedContent2);

		assertThat(parsedContent2.getLanguage()).isEqualTo(lang);
		assertThat(parsedContent2.getMimeType()).isEqualTo(mime);
		assertThat(parsedContent2.getLength()).isEqualTo(length);
		assertThat(parsedContent2.getParsedContent()).isEqualTo(content);
		assertThat(parsedContent2.getProperties()).containsOnly(
				entry("k1\n", "v1="),
				entry("k2\n\r", "v2"));

		assertThat(parsedContent2.getStyles()).containsOnly(
				entry("style1\n", asList("value1=", "value2\nvalue2b")),
				entry("style2\n\r", asList("value3:", "value4\"")));

		assertThat(parsedContentAsString2).isEqualTo(parsedContentAsString);

	}
}

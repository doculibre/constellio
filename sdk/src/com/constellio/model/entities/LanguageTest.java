package com.constellio.model.entities;

import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class LanguageTest extends ConstellioTest {
	@Test
	public void givenLanguageElementsThenSupported() {
		Language[] values = Language.values();
		for(Language l:values) {
			assertThat(Language.isSupported(l.code));
		}
	}

	@Test
	public void givenLanguageElementsThenGettersOk() {
		Language[] values = Language.values();
		for(Language l:values) {
			assertThat(l.code).isEqualTo(l.getCode());
			assertThat(l.locale).isEqualTo(l.getLocale());
		}
	}

	@Test
	public void givenLanguageElementsThenWithLocaleOk() {
		Language[] values = Language.values();
		for(Language l:values) {
			if (l.locale != null) {
				assertThat(Language.withLocale(l.locale)).isEqualTo(l);
			} else {
				assertThat(Language.withLocale(l.locale)).isEqualTo(Language.French);
			}
		}
	}

	@Test
	public void givenCodeNullWhenWithCodeThenNull() {
		assertThat(Language.withCode(null)).isNull();
	}

	@Test
	public void givenCodeUnknownWhenWithCodeThenLanguageFrench() {
		assertThat(Language.withCode("totallyUnknownCode")).isEqualTo(Language.French);
	}

	@Test
	public void givenLanguageElementsThenWithCodeOk() {
		Language[] values = Language.values();
		for(Language l:values) {
			assertThat(Language.withCode(l.code)).isEqualTo(l);
		}
	}

	@Test
	public void givenLanguageElementsThenWithCodesOk() {
		Language[] values = Language.values();

		List<String> codes = new ArrayList<>();
		for(Language l:values) {
			codes.add(l.code);
		}

		assertThat(Language.withCodes(codes)).contains(values);
	}

	@Test
	public void givenLanguageElementsThenAvalaible() {
		assertThat(Language.getAvailableLanguages()).contains(Language.values());
	}
}

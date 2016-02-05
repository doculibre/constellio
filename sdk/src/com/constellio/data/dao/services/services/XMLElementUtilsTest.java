package com.constellio.data.dao.services.services;

import static org.assertj.core.api.Assertions.assertThat;

import org.jdom2.Element;
import org.junit.Test;

import com.constellio.data.dao.services.XMLElementUtils;
import com.constellio.sdk.tests.ConstellioTest;

public class XMLElementUtilsTest extends ConstellioTest {

	@Test
	public void givenContentAndNameThenNewElementWithContentReturnCorrectElement() {
		Element returnElement = XMLElementUtils.newElementWithContent("zeName", "zeContent");
		assertThat(returnElement.getContent().get(0).getValue()).isEqualTo("zeContent");
		assertThat(returnElement.getName()).isEqualTo("zeName");
	}

	@Test
	public void givenBooleanAndNameThenNewElementWithContentReturnCorrectElement() {
		Element returnElement = XMLElementUtils.newElementWithContent("zeName", false);
		assertThat(returnElement.getContent().get(0).getValue()).isEqualTo(Boolean.toString(false));
		assertThat(returnElement.getName()).isEqualTo("zeName");
	}
}

package com.constellio.model.services.security;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import com.constellio.model.entities.security.XMLAuthorizationDetails;
import com.constellio.sdk.tests.ConstellioTest;

public class XMLAuthorizationDetailsWriterTest extends ConstellioTest {

	private static final String END_DATE = "endDate";
	private static final String ID = "id";
	Document document;
	AuthorizationDetailsWriter writer;
	Element authorizationsElement;

	@Before
	public void setup()
			throws Exception {

		document = new Document();
		writer = new AuthorizationDetailsWriter(document);
		writer.createEmptyAuthorizations();
	}

	@Test
	public void whenCreateEmptyAuthorizationsThenItIsCreated()
			throws Exception {

		authorizationsElement = document.getRootElement();
		assertThat(authorizationsElement.getChildren()).isEmpty();
	}

	@Test
	public void whenAddTaxonomyThenItIsInEnableList()
			throws Exception {

		XMLAuthorizationDetails xmlAuthorizationDetails = newAuthorization();

		writer.add(xmlAuthorizationDetails);

		authorizationsElement = document.getRootElement();
		assertThat(authorizationsElement.getChildren()).hasSize(1);
		assertThat(authorizationsElement.getChildren().get(0).getAttributeValue(ID)).isEqualTo(xmlAuthorizationDetails.getId());
	}

	@Test
	public void whenRemoveAuthorizationsThenItIsRemoved()
			throws Exception {
		XMLAuthorizationDetails xmlAuthorizationDetails = newAuthorization();
		writer.add(xmlAuthorizationDetails);

		writer.remove(xmlAuthorizationDetails.getId());

		authorizationsElement = document.getRootElement();
		assertThat(authorizationsElement.getChildren()).isEmpty();

	}

	@Test
	public void givenIdListWhenClearAuthorizationsThenRemoveAuthorizationsWithIds()
			throws Exception {
		XMLAuthorizationDetails xmlAuthorizationDetails = newAuthorization();
		XMLAuthorizationDetails xmlAuthorizationDetails2 = newAuthorization();
		writer.add(xmlAuthorizationDetails);
		writer.add(xmlAuthorizationDetails2);
		List<String> authorizationsIdsToRemove = new ArrayList<>();
		authorizationsIdsToRemove.add(xmlAuthorizationDetails.getId());
		authorizationsIdsToRemove.add(xmlAuthorizationDetails2.getId());

		writer.clearAuthorizations(authorizationsIdsToRemove);

		authorizationsElement = document.getRootElement();
		assertThat(authorizationsElement.getChildren()).isEmpty();
	}

	@Test
	public void givenAuthorizationWhenModifyEndDateAuthorizationsThenItIsModified()
			throws Exception {
		XMLAuthorizationDetails xmlAuthorizationDetails = newAuthorization();
		writer.add(xmlAuthorizationDetails);

		LocalDate endate = new LocalDate(2020, 1, 1);
		xmlAuthorizationDetails = xmlAuthorizationDetails.withNewEndDate(endate);
		writer.modifyEndDate(xmlAuthorizationDetails.getId(), xmlAuthorizationDetails.getEndDate());

		authorizationsElement = document.getRootElement();
		assertThat(authorizationsElement.getChildren()).hasSize(1);
		assertThat(authorizationsElement.getChild("authorization").getChildText(END_DATE)).isEqualTo(
				xmlAuthorizationDetails.getEndDate().toString());

	}

	private XMLAuthorizationDetails newAuthorization() {
		return XMLAuthorizationDetails.create(aString(), asList("role1"), zeCollection);
	}
}

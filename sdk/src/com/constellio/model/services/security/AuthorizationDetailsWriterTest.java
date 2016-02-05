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

import com.constellio.model.entities.security.AuthorizationDetails;
import com.constellio.sdk.tests.ConstellioTest;

public class AuthorizationDetailsWriterTest extends ConstellioTest {

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

		AuthorizationDetails authorizationDetails = newAuthorization();

		writer.add(authorizationDetails);

		authorizationsElement = document.getRootElement();
		assertThat(authorizationsElement.getChildren()).hasSize(1);
		assertThat(authorizationsElement.getChildren().get(0).getAttributeValue(ID)).isEqualTo(authorizationDetails.getId());
	}

	@Test
	public void whenRemoveAuthorizationsThenItIsRemoved()
			throws Exception {
		AuthorizationDetails authorizationDetails = newAuthorization();
		writer.add(authorizationDetails);

		writer.remove(authorizationDetails.getId());

		authorizationsElement = document.getRootElement();
		assertThat(authorizationsElement.getChildren()).isEmpty();

	}

	@Test
	public void givenIdListWhenClearAuthorizationsThenRemoveAuthorizationsWithIds()
			throws Exception {
		AuthorizationDetails authorizationDetails = newAuthorization();
		AuthorizationDetails authorizationDetails2 = newAuthorization();
		writer.add(authorizationDetails);
		writer.add(authorizationDetails2);
		List<String> authorizationsIdsToRemove = new ArrayList<>();
		authorizationsIdsToRemove.add(authorizationDetails.getId());
		authorizationsIdsToRemove.add(authorizationDetails2.getId());

		writer.clearAuthorizations(authorizationsIdsToRemove);

		authorizationsElement = document.getRootElement();
		assertThat(authorizationsElement.getChildren()).isEmpty();
	}

	@Test
	public void givenAuthorizationWhenModifyEndDateAuthorizationsThenItIsModified()
			throws Exception {
		AuthorizationDetails authorizationDetails = newAuthorization();
		writer.add(authorizationDetails);

		LocalDate endate = new LocalDate(2020, 1, 1);
		authorizationDetails = authorizationDetails.withNewEndDate(endate);
		writer.modifyEndDate(authorizationDetails.getId(), authorizationDetails.getEndDate());

		authorizationsElement = document.getRootElement();
		assertThat(authorizationsElement.getChildren()).hasSize(1);
		assertThat(authorizationsElement.getChild("authorization").getChildText(END_DATE)).isEqualTo(
				authorizationDetails.getEndDate().toString());

	}

	private AuthorizationDetails newAuthorization() {
		return AuthorizationDetails.create(aString(), asList("role1"), zeCollection);
	}
}

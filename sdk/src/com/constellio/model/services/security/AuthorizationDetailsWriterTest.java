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

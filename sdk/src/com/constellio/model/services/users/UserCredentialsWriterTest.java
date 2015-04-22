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
package com.constellio.model.services.users;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.jdom2.Document;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.entities.security.global.UserCredentialStatus;
import com.constellio.sdk.tests.ConstellioTest;

public class UserCredentialsWriterTest extends ConstellioTest {

	private static final String COLLECTIONS = "collections";
	private static final String TOKENS = "tokens";
	private static final String GLOBAL_GROUP = "globalGroup";
	private static final String GLOBAL_GROUPS = "globalGroups";
	private static final String EMAIL = "email";
	private static final String LAST_NAME = "lastName";
	private static final String FIRST_NAME = "firstName";
	private static final String USERNAME = "username";
	public static final String STATUS = "status";
	public static final String DOMAIN = "domain";
	UserCredentialsWriter writer;
	Document document;
	UserCredential chuckUserCredential;
	UserCredential dakotaUserCredential;
	LocalDateTime endDate;

	Map<String, LocalDateTime> tokens;

	@Before
	public void setup()
			throws Exception {
		document = new Document();
		writer = new UserCredentialsWriter(document);
		writer.createEmptyUserCredentials();

		tokens = new HashMap<String, LocalDateTime>();
		endDate = new LocalDateTime(2014, 11, 04, 10, 30);
		tokens.put("token1", endDate);
		chuckUserCredential = new UserCredential("chuck", "Chuck", "Norris", "chuck.norris@gmail.com", "serviceKeyChuck", false,
				Arrays.asList("group1"),
				Arrays.asList(zeCollection), tokens, UserCredentialStatus.ACTIVE, "");
		dakotaUserCredential = new UserCredential("dakota", "Dakota", "Lindien", "dakota.lindien@gmail.com",
				Arrays.asList("group1"), Arrays.asList(zeCollection, "collection1"), UserCredentialStatus.ACTIVE, "");
	}

	@Test
	public void whenCreateEmptyUserCredentialsThenItIsCreated()
			throws Exception {

		assertThat(document.getRootElement().getChildren()).isEmpty();
	}

	@Test
	public void whenAddUserCredentialThenItIsAdded()
			throws Exception {

		writer.addUpdate(chuckUserCredential);

		assertThat(document.getRootElement().getChildren()).hasSize(1);
		assertThat(document.getRootElement().getChildren().get(0).getAttributeValue(USERNAME)).isEqualTo(
				chuckUserCredential.getUsername());
		assertThat(document.getRootElement().getChildren().get(0).getChild(FIRST_NAME).getText()).isEqualTo(
				chuckUserCredential.getFirstName());
		assertThat(document.getRootElement().getChildren().get(0).getChild(LAST_NAME).getText()).isEqualTo(
				chuckUserCredential.getLastName());
		assertThat(document.getRootElement().getChildren().get(0).getChild(EMAIL).getText()).isEqualTo(
				chuckUserCredential.getEmail());
		assertThat(document.getRootElement().getChildren().get(0).getChild(GLOBAL_GROUPS).getChild(GLOBAL_GROUP).getText())
				.isEqualTo(chuckUserCredential.getGlobalGroups().get(0));
		assertThat(document.getRootElement().getChildren().get(0).getChild(COLLECTIONS).getChildren().get(0).getText()).isEqualTo(
				chuckUserCredential.getCollections().get(0));
		assertThat(document.getRootElement().getChildren().get(0).getChild(STATUS).getText()).isEqualTo(
				String.valueOf(chuckUserCredential.getStatus().getCode()));
		assertThat(document.getRootElement().getChildren().get(0).getChild(DOMAIN).getText()).isEqualTo(
				chuckUserCredential.getDomain());
		assertThat(document.getRootElement().getChildren().get(0).getChild(TOKENS).getChildren().get(0).getChildren().get(0)
				.getText()).isEqualTo(
				"token1");
		assertThat(document.getRootElement().getChildren().get(0).getChild(TOKENS).getChildren().get(0).getChildren().get(1)
				.getText()).isEqualTo(
				endDate.toString());
	}

	@Test
	public void givenUserCredentialWhenUpdateUserCredentialThenItIsUpdated()
			throws Exception {

		writer.addUpdate(chuckUserCredential);

		chuckUserCredential = new UserCredential("chuck", "Chuck", "Norris", "chuck.norris@gmail.com", Arrays.asList("group1"),
				Arrays.asList(zeCollection, "collection1"), UserCredentialStatus.ACTIVE, "");

		writer.addUpdate(chuckUserCredential);
		assertThat(document.getRootElement().getChildren()).hasSize(1);
		assertThat(document.getRootElement().getChildren().get(0).getAttributeValue(USERNAME)).isEqualTo(
				chuckUserCredential.getUsername());
		assertThat(document.getRootElement().getChildren().get(0).getChild(COLLECTIONS).getChildren().get(0).getText()).isEqualTo(
				chuckUserCredential.getCollections().get(0));
		assertThat(document.getRootElement().getChildren().get(0).getChild(COLLECTIONS).getChildren().get(1).getText()).isEqualTo(
				chuckUserCredential.getCollections().get(1));
	}

	@Test
	public void givenUserWhenRemoveCollectionThenItIsRemoved()
			throws Exception {

		writer.addUpdate(chuckUserCredential);
		writer.addUpdate(dakotaUserCredential);

		writer.removeCollection(zeCollection);

		assertThat(document.getRootElement().getChildren().get(0).getChild(COLLECTIONS).getChildren()).isEmpty();
		assertThat(document.getRootElement().getChildren().get(1).getChild(COLLECTIONS).getChildren().get(0).getText()).isEqualTo(
				"collection1");
	}

	@Test
	public void givenUserWhenRemoveTokenThenItIsRemoved()
			throws Exception {

		writer.addUpdate(chuckUserCredential);
		writer.addUpdate(dakotaUserCredential);

		assertThat(document.getRootElement().getChildren().get(0).getChild(TOKENS).getChildren()).isNotEmpty();
		writer.removeToken("token1");

		assertThat(document.getRootElement().getChildren().get(0).getChild(TOKENS).getChildren()).isEmpty();
	}

	@Test
	public void givenUserWhenRemoveHimFromCollectionThenHeIsRemoved()
			throws Exception {

		writer.addUpdate(chuckUserCredential);
		writer.addUpdate(dakotaUserCredential);

		writer.removeUserFromCollection(chuckUserCredential, zeCollection);

		assertThat(document.getRootElement().getChildren().get(0).getChild(COLLECTIONS).getChildren()).isEmpty();
		assertThat(document.getRootElement().getChildren().get(1).getChild(COLLECTIONS).getChildren().get(0).getText()).isEqualTo(
				zeCollection);
		assertThat(document.getRootElement().getChildren().get(1).getChild(COLLECTIONS).getChildren().get(1).getText()).isEqualTo(
				"collection1");
	}

	@Test
	public void givenUsersWithGroupWhenRemoveGroupThenItIsRemoved()
			throws Exception {

		chuckUserCredential = new UserCredential("chuck", "Chuck", "Norris", "chuck.norris@gmail.com", Arrays.asList("group1",
				"group2"), Arrays.asList(zeCollection), UserCredentialStatus.ACTIVE, "");
		dakotaUserCredential = new UserCredential("dakota", "Dakota", "Lindien", "dakota.lindien@gmail.com", Arrays.asList(
				"group1", "group2"), Arrays.asList(zeCollection, "collection1"), UserCredentialStatus.ACTIVE, "");
		writer.addUpdate(chuckUserCredential);
		writer.addUpdate(dakotaUserCredential);

		writer.removeGroup("group1");

		assertThat(document.getRootElement().getChildren().get(0).getChild(GLOBAL_GROUPS).getChildren()).hasSize(1);
		assertThat(document.getRootElement().getChildren().get(0).getChild(GLOBAL_GROUPS).getChild(GLOBAL_GROUP).getText())
				.isEqualTo("group2");
		assertThat(document.getRootElement().getChildren().get(1).getChild(GLOBAL_GROUPS).getChildren()).hasSize(1);
		assertThat(document.getRootElement().getChildren().get(1).getChild(GLOBAL_GROUPS).getChild(GLOBAL_GROUP).getText())
				.isEqualTo("group2");
	}
}

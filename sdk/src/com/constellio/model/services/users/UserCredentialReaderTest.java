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

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.jdom2.Document;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.entities.security.global.UserCredentialStatus;
import com.constellio.sdk.tests.ConstellioTest;

public class UserCredentialReaderTest extends ConstellioTest {

	Document document;
	UserCredentialsWriter writer;
	UserCredentialsReader reader;
	UserCredential chuckUserCredential;
	UserCredential edouardUserCredential;

	LocalDateTime endDateTime;

	@Before
	public void setup()
			throws Exception {
		newChuckUserCredential();
		newEdouardUserCredential();

		Document document = new Document();
		writer = new UserCredentialsWriter(document);

		writer.createEmptyUserCredentials();
		writer.addUpdate(chuckUserCredential);
		writer.addUpdate(edouardUserCredential);
		writer.addUpdate(chuckUserCredential);
		reader = new UserCredentialsReader(document);
	}

	@Test
	public void givenTwoUsersCredentialsWhenReadAllThenGetThem()
			throws Exception {
		Map<String, UserCredential> usersCredentials = reader.readAll(asList("zeCollection"));

		assertThat(usersCredentials).hasSize(2);
		assertThat(usersCredentials.containsKey("chuck")).isTrue();
		assertThat(usersCredentials.containsKey("edouard")).isTrue();
	}

	private void newChuckUserCredential() {

		Map<String, LocalDateTime> tokens = new HashMap<String, LocalDateTime>();
		endDateTime = new LocalDateTime(2014, 11, 04, 10, 30);
		tokens.put("token1", endDateTime);
		chuckUserCredential = new UserCredential("chuck", "Chuck", "Norris", "chuck.norris@gmail.com", "serviceKeyChuck", false,
				asList("group1"), asList(zeCollection), tokens, UserCredentialStatus.ACTIVE, "domain");
	}

	private void newEdouardUserCredential() {
		edouardUserCredential = new UserCredential("edouard", "Edouard", "Lechat", "edouard.lechat@gmail.com",
				asList("group1"), asList("collection1"), UserCredentialStatus.ACTIVE, "domain");
	}
}

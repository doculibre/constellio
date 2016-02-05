package com.constellio.model.services.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jdom2.Document;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import com.constellio.model.entities.security.AuthorizationDetails;
import com.constellio.sdk.tests.ConstellioTest;

public class AuthorizationDetailsReaderTest extends ConstellioTest {

	Document document;
	AuthorizationDetailsWriter writer;
	AuthorizationDetailsReader reader;
	AuthorizationDetails authorizationDetails1;
	AuthorizationDetails authorizationDetails2;
	AuthorizationDetails authorizationDetails3;

	@Before
	public void setup()
			throws Exception {

		document = new Document();
		writer = new AuthorizationDetailsWriter(document);
		writer.createEmptyAuthorizations();

		authorizationDetails1 = newAuthorization("1");
		authorizationDetails2 = newAuthorization("2");
		authorizationDetails3 = newAuthorization("3");

		writer.add(authorizationDetails1);
		writer.add(authorizationDetails2);
		writer.add(authorizationDetails3);
		reader = new AuthorizationDetailsReader(document);
	}

	@Test
	public void whenReadAllThenReturnTheAuthorizationsObjects()
			throws Exception {
		Map<String, AuthorizationDetails> authorizationDetailses = reader.readAll();

		assertThat(authorizationDetailses.keySet())
				.containsOnly(authorizationDetails1.getId(), authorizationDetails2.getId(), authorizationDetails3.getId());

		AuthorizationDetails returnedDetail1 = authorizationDetailses.get(authorizationDetails1.getId());
		AuthorizationDetails returnedDetail2 = authorizationDetailses.get(authorizationDetails2.getId());
		AuthorizationDetails returnedDetail3 = authorizationDetailses.get(authorizationDetails3.getId());

		assertThat(returnedDetail1.getId()).isEqualTo(authorizationDetails1.getId());
		assertThat(returnedDetail1.getRoles()).isEqualTo(authorizationDetails1.getRoles());
		assertThat(returnedDetail1.getStartDate()).isEqualTo(authorizationDetails1.getStartDate());
		assertThat(returnedDetail1.getEndDate()).isEqualTo(authorizationDetails1.getEndDate());
		assertThat(returnedDetail2.getId()).isEqualTo(authorizationDetails2.getId());
		assertThat(returnedDetail2.getRoles()).isEqualTo(authorizationDetails2.getRoles());
		assertThat(returnedDetail2.getStartDate()).isEqualTo(authorizationDetails2.getStartDate());
		assertThat(returnedDetail2.getEndDate()).isEqualTo(authorizationDetails2.getEndDate());
		assertThat(returnedDetail3.getId()).isEqualTo(authorizationDetails3.getId());
		assertThat(returnedDetail3.getRoles()).isEqualTo(authorizationDetails3.getRoles());
		assertThat(returnedDetail3.getStartDate()).isEqualTo(authorizationDetails3.getStartDate());
		assertThat(returnedDetail3.getEndDate()).isEqualTo(authorizationDetails3.getEndDate());
	}

	private AuthorizationDetails newAuthorization(String id) {
		List<String> roles = newRoleList(id);
		LocalDate startDate = new LocalDate(2012, 01, 01).plusDays(Integer.parseInt(id));
		LocalDate endDate = new LocalDate(2013, 01, 01).plusDays(Integer.parseInt(id));
		AuthorizationDetails authorizationDetails = AuthorizationDetails.create(aString(), roles, zeCollection);
		return AuthorizationDetails.create(aString(), roles, startDate, endDate, zeCollection);
	}

	private List<String> newRoleList(String id) {
		List<String> roles = new ArrayList<>();
		roles.add("role1_" + id);
		roles.add("role2_" + id);
		roles.add("role3_" + id);
		roles.add("role4_" + id);
		return roles;
	}

}

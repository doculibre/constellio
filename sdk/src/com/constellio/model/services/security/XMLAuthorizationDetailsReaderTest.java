package com.constellio.model.services.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jdom2.Document;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import com.constellio.model.entities.security.XMLAuthorizationDetails;
import com.constellio.sdk.tests.ConstellioTest;

public class XMLAuthorizationDetailsReaderTest extends ConstellioTest {

	Document document;
	AuthorizationDetailsWriter writer;
	AuthorizationDetailsReader reader;
	XMLAuthorizationDetails xmlAuthorizationDetails1;
	XMLAuthorizationDetails xmlAuthorizationDetails2;
	XMLAuthorizationDetails xmlAuthorizationDetails3;

	@Before
	public void setup()
			throws Exception {

		document = new Document();
		writer = new AuthorizationDetailsWriter(document);
		writer.createEmptyAuthorizations();

		xmlAuthorizationDetails1 = newAuthorization("1");
		xmlAuthorizationDetails2 = newAuthorization("2");
		xmlAuthorizationDetails3 = newAuthorization("3");

		writer.add(xmlAuthorizationDetails1);
		writer.add(xmlAuthorizationDetails2);
		writer.add(xmlAuthorizationDetails3);
		reader = new AuthorizationDetailsReader(document);
	}

	@Test
	public void whenReadAllThenReturnTheAuthorizationsObjects()
			throws Exception {
		Map<String, XMLAuthorizationDetails> authorizationDetailses = reader.readAll();

		assertThat(authorizationDetailses.keySet())
				.containsOnly(xmlAuthorizationDetails1.getId(), xmlAuthorizationDetails2.getId(), xmlAuthorizationDetails3.getId());

		XMLAuthorizationDetails returnedDetail1 = authorizationDetailses.get(xmlAuthorizationDetails1.getId());
		XMLAuthorizationDetails returnedDetail2 = authorizationDetailses.get(xmlAuthorizationDetails2.getId());
		XMLAuthorizationDetails returnedDetail3 = authorizationDetailses.get(xmlAuthorizationDetails3.getId());

		assertThat(returnedDetail1.getId()).isEqualTo(xmlAuthorizationDetails1.getId());
		assertThat(returnedDetail1.getRoles()).isEqualTo(xmlAuthorizationDetails1.getRoles());
		assertThat(returnedDetail1.getStartDate()).isEqualTo(xmlAuthorizationDetails1.getStartDate());
		assertThat(returnedDetail1.getEndDate()).isEqualTo(xmlAuthorizationDetails1.getEndDate());
		assertThat(returnedDetail2.getId()).isEqualTo(xmlAuthorizationDetails2.getId());
		assertThat(returnedDetail2.getRoles()).isEqualTo(xmlAuthorizationDetails2.getRoles());
		assertThat(returnedDetail2.getStartDate()).isEqualTo(xmlAuthorizationDetails2.getStartDate());
		assertThat(returnedDetail2.getEndDate()).isEqualTo(xmlAuthorizationDetails2.getEndDate());
		assertThat(returnedDetail3.getId()).isEqualTo(xmlAuthorizationDetails3.getId());
		assertThat(returnedDetail3.getRoles()).isEqualTo(xmlAuthorizationDetails3.getRoles());
		assertThat(returnedDetail3.getStartDate()).isEqualTo(xmlAuthorizationDetails3.getStartDate());
		assertThat(returnedDetail3.getEndDate()).isEqualTo(xmlAuthorizationDetails3.getEndDate());
	}

	private XMLAuthorizationDetails newAuthorization(String id) {
		List<String> roles = newRoleList(id);
		LocalDate startDate = new LocalDate(2012, 01, 01).plusDays(Integer.parseInt(id));
		LocalDate endDate = new LocalDate(2013, 01, 01).plusDays(Integer.parseInt(id));
		XMLAuthorizationDetails xmlAuthorizationDetails = XMLAuthorizationDetails.create(aString(), roles, zeCollection);
		return XMLAuthorizationDetails.create(aString(), roles, startDate, endDate, zeCollection);
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

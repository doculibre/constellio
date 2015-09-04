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
package com.constellio.app.modules.rm.model;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.constants.RMTaxonomies;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Email;
import com.constellio.model.entities.records.wrappers.UserDocument;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ContentVersionDataSummary;
import com.constellio.model.services.records.RecordServices;
import com.constellio.sdk.tests.ConstellioTest;

public class DocumentAcceptanceTest extends ConstellioTest {

	RMSchemasRecordsServices rm;
	RMTestRecords records = new RMTestRecords(zeCollection);
	RecordServices recordServices;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioRMModule().withRMTest(records)
						.withFoldersAndContainersOfEveryStatus()
		);

		assertThat(getModelLayerFactory().getTaxonomiesManager().getPrincipalTaxonomy(zeCollection).getCode())
				.isEqualTo(RMTaxonomies.ADMINISTRATIVE_UNITS);

		rm = new RMSchemasRecordsServices(zeCollection, getModelLayerFactory());
		recordServices = getModelLayerFactory().newRecordServices();

	}

	@Test
	public void whenCreatingADocumentWithoutDescriptionThenOK()
			throws Exception {

		Document document = rm.newDocument().setTitle("My document").setDescription("test").setFolder(records.folder_A03);
		recordServices.add(document);
		document.setDescription(null).setTitle("Z");
		recordServices.update(document);

	}

	@Test
	public void whenCreatingADocumentFromAMSGFileThenExtractMetadatas()
			throws Exception {
		ContentManager contentManager = rm.getModelLayerFactory().getContentManager();
		ContentVersionDataSummary datasummary = contentManager.upload(
				getTestResourceInputStreamFactory("test.msg").create(SDK_STREAM));

		Document document = rm.newDocumentWithId("zeId")
				.setFolder(records.folder_A05)
				.setTitle("a dummy title")
				.setContent(contentManager.createMajor(records.getAdmin(), "test.msg", datasummary));

		recordServices.add(document);

		Email email = rm.getEmail("zeId");
		assertThat(email.getSchemaCode()).isEqualTo(Email.SCHEMA);
		assertThat(email.getTitle()).isEqualTo("broullion2");
		assertThat(email.getEmailFrom()).isEqualTo("Addin");
		assertThat(email.getEmailTo()).isEqualTo(asList("ff@doculibre.com", "ll@doculibre.com"));
		assertThat(email.getEmailBCCTo()).isEqualTo(asList("rccr@doculibre.com", "hcch@doculibre.com"));
		assertThat(email.getEmailCCTo()).isEqualTo(asList("rr@doculibre.com", "hh@doculibre.com"));
		assertThat(email.getEmailObject()).isEqualTo("broullion2");
	}

	@Test
	public void whenCreatingAUserDocumentFromAMSGFileThenExtractMetadatas()
			throws Exception {
		ContentManager contentManager = rm.getModelLayerFactory().getContentManager();
		ContentVersionDataSummary datasummary = contentManager.upload(
				getTestResourceInputStreamFactory("test.msg").create(SDK_STREAM));

		UserDocument userDocument = rm.newUserDocumentWithId("zeId")
				.setContent(contentManager.createMajor(records.getAdmin(), "test.msg", datasummary));

		recordServices.add(userDocument);

		UserDocument email = rm.getUserDocument("zeId");
		assertThat(email.getSchemaCode()).isEqualTo(UserDocument.DEFAULT_SCHEMA);
		assertThat(email.getTitle()).isEqualTo("broullion2");

	}

}
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
package com.constellio.app.api.cmis.rm;

import static java.util.Calendar.MARCH;
import static org.apache.chemistry.opencmis.commons.enums.VersioningState.MAJOR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.math.BigInteger;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.runtime.ObjectIdImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.assertj.core.api.Condition;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.model.services.security.authentification.AuthenticationService;
import com.constellio.sdk.tests.ConstellioTest;

public class RMModuleUseCasesAcceptTest extends ConstellioTest {

	Session cmisSession;
	RMTestRecords records;

	@Before
	public void setUp()
			throws Exception {
		givenCollection(zeCollection).withConstellioRMModule().withAllTestUsers();
		records = new RMTestRecords(zeCollection).setup(getModelLayerFactory()).withFoldersAndContainersOfEveryStatus();

		AuthenticationService authenticationService = getModelLayerFactory().newAuthenticationService();
		authenticationService.changePassword(chuckNorris, "1qaz2wsx");

	}

	//@Test
	//This is a problem in OpenCMIS, the "folder_default_category" is silently filtered
	//And the server will throw an exception that "folder_default_category" is required
	//The solution is to fill the field "folder_default_categoryEntered"
	public void whenCreateFolderUsingAutomaticMetadataThenException() {
		cmisSession = newCmisSessionBuilder().authenticatedBy(chuckNorris, "1qaz2wsx").onCollection(zeCollection).build();

		try {
			Map<String, Object> folderProperties = new HashMap<>();
			folderProperties.put(PropertyIds.OBJECT_TYPE_ID, "folder_default");
			folderProperties.put("folder_default_title", "My folder");
			folderProperties.put("folder_default_category", records.categoryId_X100);
			folderProperties.put("folder_default_retentionRuleEntered", records.ruleId_1);
			folderProperties.put("folder_default_administrativeUnitEntered", records.unitId_10);
			folderProperties.put("folder_default_filingSpaceEntered", records.filingId_A);
			folderProperties.put("folder_default_openingDate", new GregorianCalendar(2013, MARCH, 27));
			cmisSession.createFolder(folderProperties, new ObjectIdImpl(records.unitId_10));
			fail("Exception expected");
		} catch (Exception e) {
			assertThat(e).hasMessage("Metadata 'folder_default_category' is readonly.");
		}

	}

	@Test
	public void createAFolderAndADocumentThenModifyThem()
			throws Exception {
		cmisSession = newCmisSessionBuilder().authenticatedBy(chuckNorris, "1qaz2wsx").onCollection(zeCollection).build();

		//
		//-- Create a folder
		Map<String, Object> folderProperties = new HashMap<>();
		folderProperties.put(PropertyIds.OBJECT_TYPE_ID, "folder_default");
		folderProperties.put("folder_default_title", "My folder");
		folderProperties.put("folder_default_categoryEntered", records.categoryId_X100);
		folderProperties.put("folder_default_retentionRuleEntered", records.ruleId_1);
		folderProperties.put("folder_default_administrativeUnitEntered", records.unitId_10);
		folderProperties.put("folder_default_filingSpaceEntered", records.filingId_A);
		folderProperties.put("folder_default_openingDate", new GregorianCalendar(2013, MARCH, 27));
		ObjectId newFolderId = cmisSession.createFolder(folderProperties, new ObjectIdImpl(records.unitId_10));

		//
		//-- Create a document
		Map<String, Object> documentProperties = new HashMap<>();
		documentProperties.put(PropertyIds.OBJECT_TYPE_ID, "document_default");
		documentProperties.put("document_default_title", "My document");
		documentProperties.put("document_default_folder", newFolderId.getId());
		ObjectId newDocumentId = cmisSession.createFolder(documentProperties, newFolderId);

		//
		//-- Add a content to the new document
		Map<String, Object> contentProperties = new HashMap<>();
		contentProperties.put("metadata", "content");
		contentProperties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");
		ContentStream contentStream = new ContentStreamImpl(
				"My document.pdf",
				BigInteger.valueOf(getTestResourceFile("test.pdf").length()),
				"application/pdf",
				getTestResourceInputStream("test.pdf"));
		ObjectId documentContentId = cmisSession.createDocument(contentProperties, newDocumentId, contentStream, MAJOR);

		//
		//-- Validate new folder
		Folder folder = (Folder) cmisSession.getObject(newFolderId);
		assertThat(folder.getPropertyValue("folder_default_title")).isEqualTo("My folder");
		assertThat(folder.getPropertyValue("folder_default_categoryEntered")).isEqualTo(records.categoryId_X100);
		assertThat(folder.getPropertyValue("folder_default_retentionRuleEntered")).isEqualTo(records.ruleId_1);
		assertThat(folder.getPropertyValue("folder_default_copyStatusEntered")).isNull();
		assertThat(folder.getPropertyValue("folder_default_copyStatus")).isEqualTo("P");
		assertThat(folder.getPropertyValue("folder_default_administrativeUnitEntered")).isEqualTo(records.unitId_10);
		assertThat(folder.getPropertyValue("folder_default_filingSpaceEntered")).isEqualTo(records.filingId_A);
		assertThat(folder.getPropertyValue("folder_default_openingDate")).is(sameDayThan(new LocalDate(2013, 3, 27)));

		//
		//-- Validate new document
		Folder document = (Folder) cmisSession.getObject(newDocumentId);
		assertThat(document.getPropertyValue("document_default_title")).isEqualTo("My document");
		assertThat(document.getPropertyValue("document_default_folder")).isEqualTo(newFolderId.getId());
		assertThat(document.getPropertyValue("document_default_category")).isEqualTo(records.categoryId_X100);
		assertThat(document.getPropertyValue("document_default_retentionRule")).isEqualTo(records.ruleId_1);
		assertThat(document.getPropertyValue("document_default_administrativeUnit")).isEqualTo(records.unitId_10);
		assertThat(document.getPropertyValue("document_default_filingSpace")).isEqualTo(records.filingId_A);

		//
		//-- Validate document's content
		Document content = (Document) cmisSession.getObject(documentContentId);
		assertThat(content.getContentStreamFileName()).isEqualTo("My document.pdf");

		//
		//-- Modify the folder
		folderProperties = new HashMap<>();
		folderProperties.put(PropertyIds.OBJECT_TYPE_ID, "folder_default");
		folderProperties.put("folder_default_categoryEntered", records.categoryId_X110);
		folderProperties.put("folder_default_retentionRuleEntered", records.ruleId_2);
		folderProperties.put("folder_default_copyStatusEntered", "P");
		folder.updateProperties(folderProperties);

		//
		//-- Wait until the document has been reindexed
		waitForBatchProcess();

		//
		//-- Validate new folder
		folder = (Folder) cmisSession.getObject(newFolderId);
		assertThat(folder.getPropertyValue("folder_default_title")).isEqualTo("My folder");
		assertThat(folder.getPropertyValue("folder_default_categoryEntered")).isEqualTo(records.categoryId_X110);
		assertThat(folder.getPropertyValue("folder_default_retentionRuleEntered")).isEqualTo(records.ruleId_2);
		assertThat(folder.getPropertyValue("folder_default_copyStatusEntered")).isEqualTo("P");
		assertThat(folder.getPropertyValue("folder_default_copyStatus")).isEqualTo("P");
		assertThat(folder.getPropertyValue("folder_default_administrativeUnitEntered")).isEqualTo(records.unitId_10);
		assertThat(folder.getPropertyValue("folder_default_filingSpaceEntered")).isEqualTo(records.filingId_A);
		assertThat(folder.getPropertyValue("folder_default_openingDate")).is(sameDayThan(new LocalDate(2013, 3, 27)));

		//
		//-- Validate new document
		document = (Folder) cmisSession.getObject(newDocumentId);
		assertThat(document.getPropertyValue("document_default_title")).isEqualTo("My document");
		assertThat(document.getPropertyValue("document_default_folder")).isEqualTo(newFolderId.getId());
		assertThat(document.getPropertyValue("document_default_category")).isEqualTo(records.categoryId_X110);
		assertThat(document.getPropertyValue("document_default_retentionRule")).isEqualTo(records.ruleId_2);
		assertThat(document.getPropertyValue("document_default_administrativeUnit")).isEqualTo(records.unitId_10);
		assertThat(document.getPropertyValue("document_default_filingSpace")).isEqualTo(records.filingId_A);

	}

	private Condition<? super Object> sameDayThan(final LocalDate localDate) {
		return new Condition<Object>() {
			@Override
			public boolean matches(Object value) {
				Calendar calendar = (Calendar) value;
				assertThat(calendar.get(Calendar.YEAR)).isEqualTo(localDate.getYear());
				assertThat(calendar.get(Calendar.MONTH) + 1).isEqualTo(localDate.getMonthOfYear());
				assertThat(calendar.get(Calendar.DAY_OF_MONTH)).isEqualTo(localDate.getDayOfMonth());
				return true;
			}
		};
	}

}

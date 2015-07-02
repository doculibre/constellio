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
package com.constellio.app.api.cmis.accept;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.runtime.ObjectIdImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

import com.constellio.app.api.cmis.accept.CmisAcceptanceTestSetup.Records;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.model.services.taxonomies.TaxonomiesSearchServices;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.DriverTest;
import com.constellio.sdk.tests.setups.Users;

@DriverTest
public class CreateFolderRequestAcceptTest extends ConstellioTest {

	UserServices userServices;
	TaxonomiesManager taxonomiesManager;
	MetadataSchemasManager schemasManager;
	RecordServices recordServices;
	Users users = new Users();
	CmisAcceptanceTestSetup zeCollectionSchemas = new CmisAcceptanceTestSetup(zeCollection);
	Records zeCollectionRecords;
	TaxonomiesSearchServices taxonomiesSearchServices;

	Session cmisSession;

	@Before
	public void setUp()
			throws Exception {

		userServices = getModelLayerFactory().newUserServices();
		taxonomiesManager = getModelLayerFactory().getTaxonomiesManager();
		schemasManager = getModelLayerFactory().getMetadataSchemasManager();
		recordServices = getModelLayerFactory().newRecordServices();

		taxonomiesSearchServices = getModelLayerFactory().newTaxonomiesSearchService();

		users.setUp(userServices);

		defineSchemasManager().using(zeCollectionSchemas);
		taxonomiesManager.addTaxonomy(zeCollectionSchemas.getTaxonomy1(), schemasManager);
		taxonomiesManager.setPrincipalTaxonomy(zeCollectionSchemas.getTaxonomy1(), schemasManager);
		zeCollectionRecords = zeCollectionSchemas.givenRecords(recordServices);

		userServices.addUserToCollection(users.bob(), zeCollection);
		userServices.addUserToCollection(users.chuckNorris(), zeCollection);

		cmisSession = givenAdminSessionOnZeCollection();
		recordServices.update(users.chuckNorrisIn(zeCollection).setCollectionWriteAccess(true).getWrappedRecord());
	}

	@Test
	public void whenCreatingAFolderThenFolderHasRightValues()
			throws Exception {
		givenTimeIs(new LocalDate(2014, 11, 04));
		String id = createNewFolderWithTestProperties("taxo1_category1", "folder_default");

		Record createdRecord = recordServices.getDocumentById(id);
		assertThat(createdRecord).isNotNull();
		assertThat(createdRecord.get(Schemas.TITLE)).isEqualTo("testFolder");
		assertThat(((LocalDateTime) createdRecord.get(Schemas.CREATED_ON)).toDate()).isEqualTo(
				new LocalDate(2014, 11, 04).toDate());
		assertThat(createdRecord.get(Schemas.CREATED_BY)).isEqualTo(users.chuckNorrisIn(zeCollection).getId());
		assertThat(createdRecord.get(zeCollectionSchemas.folderSchema.taxonomy1())).isEqualTo("taxo1_category1");
	}

	@Test
	public void whenCreatingACategoryThenCategoryHasRightValues()
			throws Exception {
		givenTimeIs(new LocalDate(2014, 11, 04));
		String id = createNewCategoryWithTestProperties("taxo1_fond1", "category_default");

		Record createdRecord = recordServices.getDocumentById(id);
		assertThat(createdRecord).isNotNull();
		assertThat(createdRecord.get(Schemas.TITLE)).isEqualTo("testCategory");
		assertThat(((LocalDateTime) createdRecord.get(Schemas.CREATED_ON)).toDate()).isEqualTo(
				new LocalDate(2014, 11, 04).toDate());
		assertThat(createdRecord.get(Schemas.CREATED_BY)).isEqualTo(users.chuckNorrisIn(zeCollection).getId());
		assertThat(createdRecord.get(zeCollectionSchemas.category.parentOfDocumentFond())).isEqualTo("taxo1_fond1");
	}

	@Test
	public void whenCreatingACollectionThenException()
			throws Exception {
		String rootId = cmisSession.getRootFolder().getId();
		try {
			createNewCollection(rootId, "collection_default");
		} catch (CmisRuntimeException ex) {
			assertThat(ex.getMessage()).isEqualTo("Cannot create collection via CMIS.");
		}
	}

	@Test
	public void whenCreatingATaxonomyThenException()
			throws Exception {
		try {
			createNewTaxonomy(zeCollection, "taxonomy");
		} catch (CmisRuntimeException ex) {
			assertThat(ex.getMessage()).isEqualTo("Cannot create taxonomy via CMIS.");
		}
	}

	private String createNewFolderWithTestProperties(String parent, String objectType) {
		ObjectId parentFolderId = new ObjectIdImpl(parent);
		Map<String, Object> newFolderProperties = new HashMap<>();
		newFolderProperties.put(PropertyIds.OBJECT_TYPE_ID, objectType);
		newFolderProperties.put("folder_default_title", "testFolder");
		return cmisSession.createFolder(newFolderProperties, parentFolderId).getId();
	}

	private String createNewCategoryWithTestProperties(String parent, String objectType) {
		ObjectId parentFolderId = new ObjectIdImpl(parent);
		Map<String, Object> newFolderProperties = new HashMap<>();
		newFolderProperties.put(PropertyIds.OBJECT_TYPE_ID, objectType);
		newFolderProperties.put("category_default_title", "testCategory");
		return cmisSession.createFolder(newFolderProperties, parentFolderId).getId();
	}

	private String createNewCollection(String parent, String objectType) {
		ObjectId parentFolderId = new ObjectIdImpl(parent);
		Map<String, Object> newFolderProperties = new HashMap<>();
		newFolderProperties.put(PropertyIds.OBJECT_TYPE_ID, objectType);
		newFolderProperties.put("collection_default_title", "testCollection");
		return cmisSession.createFolder(newFolderProperties, parentFolderId).getId();
	}

	private String createNewTaxonomy(String parent, String objectType) {
		ObjectId parentFolderId = new ObjectIdImpl(parent);
		Map<String, Object> newFolderProperties = new HashMap<>();
		newFolderProperties.put(PropertyIds.OBJECT_TYPE_ID, objectType);
		return cmisSession.createFolder(newFolderProperties, parentFolderId).getId();
	}

	private Session givenAdminSessionOnZeCollection()
			throws RecordServicesException {
		getModelLayerFactory().newAuthenticationService().changePassword(chuckNorris, "1qaz2wsx");
		return newCmisSessionBuilder().authenticatedBy(chuckNorris, "1qaz2wsx").onCollection(zeCollection).build();
	}
}

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
package com.constellio.app.api.cmis.requests.navigation;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.junit.Before;
import org.junit.Test;

import com.constellio.app.api.cmis.accept.CmisAcceptanceTestSetup;
import com.constellio.app.api.cmis.accept.CmisAcceptanceTestSetup.Records;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.model.services.taxonomies.TaxonomiesSearchServices;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestRecord;
import com.constellio.sdk.tests.annotations.DriverTest;
import com.constellio.sdk.tests.setups.Users;

@DriverTest
public class GetObjectRequestAcceptTest extends ConstellioTest {

	UserServices userServices;
	TaxonomiesManager taxonomiesManager;
	MetadataSchemasManager metadataSchemasManager;
	RecordServices recordServices;
	Users users = new Users();
	CmisAcceptanceTestSetup zeCollectionSchemas = new CmisAcceptanceTestSetup(zeCollection);
	Records zeCollectionRecords;
	TaxonomiesSearchServices taxonomiesSearchServices;

	// Session cmisSession;

	@Before
	public void setUp()
			throws Exception {

		userServices = getModelLayerFactory().newUserServices();
		taxonomiesManager = getModelLayerFactory().getTaxonomiesManager();
		metadataSchemasManager = getModelLayerFactory().getMetadataSchemasManager();
		recordServices = getModelLayerFactory().newRecordServices();

		taxonomiesSearchServices = getModelLayerFactory().newTaxonomiesSearchService();

		users.setUp(userServices);

		defineSchemasManager().using(zeCollectionSchemas);
		taxonomiesManager.addTaxonomy(zeCollectionSchemas.getTaxonomy1(), metadataSchemasManager);
		taxonomiesManager.setPrincipalTaxonomy(zeCollectionSchemas.getTaxonomy1(), metadataSchemasManager);
		zeCollectionRecords = zeCollectionSchemas.givenRecords(recordServices);

		userServices.addUserToCollection(users.bob(), zeCollection);
		userServices.addUserToCollection(users.chuckNorris(), zeCollection);

	}

	@Test
	public void givenObjectCreatedThenObjectCanBeObtainedByRequestAndFieldsAreCorrect()
			throws Exception {
		recordServices.update(users.chuckNorrisIn(zeCollection).setCollectionReadAccess(true).getWrappedRecord());
		Session cmisSession = givenAdminSessionOnZeCollection();
		recordServices.update(zeCollectionRecords.folder1.set(Schemas.CREATED_BY, users.chuckNorrisIn(zeCollection).getId()));
		CmisObject object = cmisSession.getObject(zeCollectionRecords.folder1.getId());

		thenFolderOneObjectHasCorrectFields(object);
	}

	@Test
	public void givenObjectCreatedWithUserThenObjectHasCorrectCreatedByValue()
			throws Exception {
		recordServices.update(users.chuckNorrisIn(zeCollection).setCollectionReadAccess(true).setCollectionWriteAccess(true)
				.getWrappedRecord());
		Session cmisSession = givenAdminSessionOnZeCollection();
		Record record = new TestRecord(zeCollectionSchemas.getSchema("folder_default"), "aTestFolder");
		recordServices.execute(new Transaction(record).setUser(users.chuckNorrisIn(zeCollection)));
		CmisObject object = cmisSession.getObject(record.getId());

		assertThat(object.getCreatedBy()).isEqualTo(users.chuckNorrisIn(zeCollection).getId());
	}

	@Test
	public void givenTaxoCreatedThenTaxoCanBeObtainedByRequestAndFieldsAreCorrect()
			throws Exception {
		recordServices.update(users.chuckNorrisIn(zeCollection).setCollectionReadAccess(true).getWrappedRecord());
		Session cmisSession = givenAdminSessionOnZeCollection();
		CmisObject object = cmisSession.getObject("taxo_" + zeCollectionSchemas.getTaxonomy1().getCode());

		thenTaxoOneObjectHasCorrectFields(object);
	}

	@Test
	public void givenConceptCreatedThenConceptCanBeObtainedByRequestAndFieldsAreCorrect()
			throws Exception {
		recordServices.update(users.chuckNorrisIn(zeCollection).setCollectionReadAccess(true).getWrappedRecord());
		Session cmisSession = givenAdminSessionOnZeCollection();
		CmisObject object = cmisSession.getObject(zeCollectionRecords.taxo1_category2.getId());

		thenCategoryTwoObjectHasCorrectFields(object);
	}

	private void thenFolderOneObjectHasCorrectFields(CmisObject object) {
		assertThat(object).isNotNull();
		assertThat(object.getId()).isEqualTo("folder1");
		assertThat(object.getProperty("folder_default_id").getValue()).isEqualTo("folder1");
		assertThat(object.getProperty("folder_default_title").getValue()).isEqualTo("folder1");
		assertThat(object.getProperty("folder_default_schema").getValue()).isEqualTo("folder_default");
		assertThat(object.getProperty("folder_default_parentpath").getFirstValue()).isEqualTo(
				"/taxo1/taxo1_fond1/taxo1_fond1_1/taxo1_category1");
		assertThat(object.getProperty("folder_default_path").getFirstValue()).isEqualTo(
				"/taxo1/taxo1_fond1/taxo1_fond1_1/taxo1_category1/folder1");
		assertThat(object.getProperty(PropertyIds.PARENT_ID).getValue()).isEqualTo("taxo1_category1");
	}

	private void thenCategoryTwoObjectHasCorrectFields(CmisObject object) {
		assertThat(object).isNotNull();
		assertThat(object.getId()).isEqualTo("taxo1_category2");
		//assertThat(object.getProperty("category_default_id").getValue()).isEqualTo("taxo1_category2");
		assertThat(object.getProperty("category_default_title").getValue()).isEqualTo("taxo1_category2");
		assertThat(object.getProperty("category_default_schema").getValue()).isEqualTo("category_default");
		assertThat(object.getProperty("category_default_parentpath").getFirstValue()).isEqualTo("/taxo1/taxo1_fond1");
		assertThat(object.getProperty("category_default_path").getFirstValue()).isEqualTo("/taxo1/taxo1_fond1/taxo1_category2");
		assertThat(object.getProperty(PropertyIds.PARENT_ID).getValue()).isEqualTo("taxo1_fond1");
	}

	private void thenTaxoOneObjectHasCorrectFields(CmisObject object) {
		assertThat(object).isNotNull();
		assertThat(object.getId()).isEqualTo("taxo_taxo1");
		assertThat(object.getName()).isEqualTo("taxo1");
		assertThat(object.getProperty(PropertyIds.PARENT_ID).getValue()).isEqualTo(zeCollection);
		assertThat(object.getProperty(PropertyIds.PATH).getValue()).isEqualTo("/taxo_taxo1");
	}

	private Session givenAdminSessionOnZeCollection()
			throws RecordServicesException {
		getModelLayerFactory().newAuthenticationService().changePassword(chuckNorris, "1qaz2wsx");
		return newCmisSessionBuilder().authenticatedBy(chuckNorris, "1qaz2wsx").onCollection(zeCollection).build();
	}

}

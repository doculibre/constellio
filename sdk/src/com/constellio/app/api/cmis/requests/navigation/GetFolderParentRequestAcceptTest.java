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

import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.junit.Before;
import org.junit.Test;

import com.constellio.app.api.cmis.accept.CmisAcceptanceTestSetup;
import com.constellio.app.api.cmis.accept.CmisAcceptanceTestSetup.Records;
import com.constellio.model.entities.records.Record;
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
public class GetFolderParentRequestAcceptTest extends ConstellioTest {

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
	public void whenGetParentOfSubFolderThenItIsReturned()
			throws Exception {
		Record record = zeCollectionRecords.folder2_1;

		ObjectData objectDataParent = cmisSession.getBinding().getNavigationService()
				.getFolderParent(cmisSession.getRepositoryInfo().getId(), record.getId(), null, null);

		assertThat(objectDataParent.getId()).isEqualTo(zeCollectionRecords.folder2.getId());
	}

	@Test
	public void whenGetParentOfSubCategoryThenItIsReturned()
			throws Exception {
		Record record = zeCollectionRecords.taxo1_category2_1;

		ObjectData objectDataParent = cmisSession.getBinding().getNavigationService()
				.getFolderParent(cmisSession.getRepositoryInfo().getId(), record.getId(), null, null);

		assertThat(objectDataParent.getId()).isEqualTo(zeCollectionRecords.taxo1_category2.getId());
	}

	@Test
	public void whenGetParentOfCategoryThenItIsReturned()
			throws Exception {
		Record record = zeCollectionRecords.taxo1_category2;

		ObjectData objectDataParent = cmisSession.getBinding().getNavigationService()
				.getFolderParent(cmisSession.getRepositoryInfo().getId(), record.getId(), null, null);

		assertThat(objectDataParent.getId()).isEqualTo(zeCollectionRecords.taxo1_fond1.getId());
	}

	@Test
	public void whenGetParentOfFolderThenItIsReturned()
			throws Exception {
		Record record = zeCollectionRecords.folder4;

		ObjectData objectDataParent = cmisSession.getBinding().getNavigationService()
				.getFolderParent(cmisSession.getRepositoryInfo().getId(), record.getId(), null, null);

		assertThat(objectDataParent.getId()).isEqualTo(zeCollectionRecords.taxo1_category2.getId());
	}

	private Session givenAdminSessionOnZeCollection()
			throws RecordServicesException {
		getModelLayerFactory().newAuthenticationService().changePassword(chuckNorris, "1qaz2wsx");
		return newCmisSessionBuilder().authenticatedBy(chuckNorris, "1qaz2wsx").onCollection(zeCollection).build();
	}
}

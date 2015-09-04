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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.modules.rm.wrappers.StorageSpace;
import com.constellio.app.modules.rm.wrappers.UniformSubdivision;
import com.constellio.app.modules.rm.wrappers.type.DocumentType;
import com.constellio.app.modules.rm.wrappers.type.FolderType;
import com.constellio.app.modules.rm.wrappers.type.MediumType;
import com.constellio.model.services.records.cache.RecordsCache;
import com.constellio.sdk.tests.ConstellioTest;

public class RMCachingAcceptTest extends ConstellioTest {

	RMTestRecords records = new RMTestRecords(zeCollection);
	RMSchemasRecordsServices rm;
	RecordsCache cache;

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(withZeCollection().withConstellioRMModule().withAllTestUsers()
				.withRMTest(records).withFoldersAndContainersOfEveryStatus().withDocumentsHavingContent());
		cache = getModelLayerFactory().getRecordsCaches().getCache(zeCollection);
		rm = new RMSchemasRecordsServices(zeCollection, getModelLayerFactory());
		cache.invalidateAll();
	}

	@Test
	public void givenRMModuleThenAdministrativeUnitSchemaTypeIsCached()
			throws Exception {

		assertThat(cache.getCacheConfigOf(AdministrativeUnit.SCHEMA_TYPE).isPermanent()).isTrue();
		assertThat(cache.isCached(records.unitId_10)).isFalse();
		rm.getAdministrativeUnit(records.unitId_10);
		assertThat(cache.isCached(records.unitId_10)).isTrue();
	}

	@Test
	public void givenRMModuleThenCategorySchemaTypeIsCached()
			throws Exception {
		assertThat(cache.getCacheConfigOf(Category.SCHEMA_TYPE).isPermanent()).isTrue();
		assertThat(cache.isCached(records.categoryId_ZE42)).isFalse();
		rm.getCategory(records.categoryId_ZE42);
		assertThat(cache.isCached(records.categoryId_ZE42)).isTrue();
	}

	@Test
	public void givenRMModuleThenStorageSpaceSchemaTypeIsCached()
			throws Exception {
		assertThat(cache.getCacheConfigOf(StorageSpace.SCHEMA_TYPE).isPermanent()).isTrue();
		assertThat(cache.isCached(records.storageSpaceId_S01_01)).isFalse();
		rm.getStorageSpace(records.storageSpaceId_S01_01);
		assertThat(cache.isCached(records.storageSpaceId_S01_01)).isTrue();
	}

	@Test
	public void givenRMModuleThenRetentionRuleSchemaTypeIsCached()
			throws Exception {
		assertThat(cache.getCacheConfigOf(RetentionRule.SCHEMA_TYPE).isPermanent()).isTrue();
		assertThat(cache.isCached(records.ruleId_2)).isFalse();
		rm.getRetentionRule(records.ruleId_2);
		assertThat(cache.isCached(records.ruleId_2)).isTrue();
	}

	@Test
	public void givenRMModuleThenUniformSubdivisionSchemaTypeIsCached()
			throws Exception {
		assertThat(cache.getCacheConfigOf(UniformSubdivision.SCHEMA_TYPE).isPermanent()).isTrue();
		assertThat(cache.isCached(records.subdivId_2)).isFalse();
		rm.getUniformSubdivision(records.subdivId_2);
		assertThat(cache.isCached(records.subdivId_2)).isTrue();
	}

	@Test
	public void givenRMModuleThenValueListTypesAreCached()
			throws Exception {
		assertThat(cache.getCacheConfigOf(FolderType.SCHEMA_TYPE).isPermanent()).isTrue();
		assertThat(cache.getCacheConfigOf(DocumentType.SCHEMA_TYPE).isPermanent()).isTrue();
		assertThat(cache.getCacheConfigOf(MediumType.SCHEMA_TYPE).isPermanent()).isTrue();

		assertThat(cache.isCached(records.MD)).isFalse();
		rm.getMediumType(records.MD);
		assertThat(cache.isCached(records.MD)).isTrue();
	}

	@Test
	public void givenRMModuleThenFolderSchemaTypeIsCached()
			throws Exception {
		assertThat(cache.getCacheConfigOf(Folder.SCHEMA_TYPE).isVolatile()).isTrue();
		assertThat(cache.isCached(records.folder_A05)).isFalse();
		rm.getFolder(records.folder_A05);
		assertThat(cache.isCached(records.folder_A05)).isTrue();
	}

	@Test
	public void givenRMModuleThenDocumentSchemaTypeIsCached()
			throws Exception {

		assertThat(cache.getCacheConfigOf(Document.SCHEMA_TYPE).isVolatile()).isTrue();
		assertThat(cache.isCached(records.document_A49)).isFalse();
		rm.getDocument(records.document_A49);
		assertThat(cache.isCached(records.document_A49)).isTrue();
	}
}

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

import static com.constellio.app.modules.rm.model.CopyRetentionRule.newPrincipal;
import static com.constellio.app.modules.rm.model.enums.FolderMediaType.ANALOG;
import static com.constellio.app.modules.rm.model.enums.FolderMediaType.ELECTRONIC;
import static com.constellio.app.modules.rm.model.enums.FolderMediaType.HYBRID;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.constants.RMTaxonomies;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.model.enums.DecomListStatus;
import com.constellio.app.modules.rm.model.enums.DecommissioningListType;
import com.constellio.app.modules.rm.model.enums.DecommissioningType;
import com.constellio.app.modules.rm.model.enums.OriginStatus;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.StorageSpace;
import com.constellio.app.modules.rm.wrappers.structures.DecomListContainerDetail;
import com.constellio.app.modules.rm.wrappers.structures.DecomListFolderDetail;
import com.constellio.app.modules.rm.wrappers.type.ContainerRecordType;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.sdk.tests.ConstellioTest;

public class DecommissioningListAcceptanceTest extends ConstellioTest {

	LocalDate november4 = new LocalDate(2009, 11, 4);
	LocalDate december12 = new LocalDate(2009, 12, 12);
	LocalDate january12_2010 = new LocalDate(2010, 1, 12);

	RMSchemasRecordsServices schemas;
	RMTestRecords rm;
	RecordServices recordServices;

	ContainerRecord containerRecord;
	Folder folder;
	DecommissioningList decommissioningList;

	@Before
	public void setUp()
			throws Exception {
		givenCollection(zeCollection).withConstellioRMModule();
		assertThat(getModelLayerFactory().getTaxonomiesManager().getPrincipalTaxonomy(zeCollection).getCode())
				.isEqualTo(RMTaxonomies.ADMINISTRATIVE_UNITS);

		schemas = new RMSchemasRecordsServices(zeCollection, getModelLayerFactory());
		rm = new RMTestRecords(zeCollection).setup(getModelLayerFactory()).withFoldersAndContainersOfEveryStatus();
		recordServices = getModelLayerFactory().newRecordServices();

		decommissioningList = schemas.newDecommissioningList();
		containerRecord = getContainerRecord();

	}

	@Test
	public void givenInitializedThenSchemaHasNoSecurity()
			throws Exception {

		assertThat(getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection)
				.getSchemaType(DecommissioningList.SCHEMA_TYPE).hasSecurity()).isFalse();
	}

	@Test
	public void whenSaveDecommissioningListThenMetadataValuesSaved()
			throws Exception {

		Folder aFolder = newFolder();
		Folder anotherFolder = newFolder();
		ContainerRecord aContainer = newContainerRecord("A");
		ContainerRecord anotherContainer = newContainerRecord("B");

		decommissioningList = schemas.newDecommissioningList();
		decommissioningList.setTitle("Ze list");
		decommissioningList.setAdministrativeUnit(rm.unitId_10);
		decommissioningList.setApprovalDate(november4);
		decommissioningList.setDescription("zeDescription");
		decommissioningList.setApprovalRequest(rm.getUsers().dakotaLIndienIn(zeCollection).getId());
		decommissioningList.setApprovalUser(rm.getUsers().dakotaLIndienIn(zeCollection));
		decommissioningList.setFilingSpace(rm.filingId_A);
		decommissioningList.setDecommissioningListType(DecommissioningListType.FOLDERS_TO_CLOSE);
		decommissioningList.setOriginArchivisticStatus(OriginStatus.SEMI_ACTIVE);

		decommissioningList.setFolderDetailsFor(aFolder.getId(), anotherFolder.getId());
		decommissioningList.setContainerDetailsFor(aContainer.getId(), anotherContainer.getId());

		decommissioningList.setProcessingDate(december12);
		decommissioningList.setProcessingUser(rm.getUsers().dakotaLIndienIn(zeCollection).getId());
		decommissioningList.setValidationDate(january12_2010);
		decommissioningList.setValidationUser(rm.getUsers().dakotaLIndienIn(zeCollection).getId());

		decommissioningList = saveAndLoad(decommissioningList);

		assertThat(decommissioningList.getAdministrativeUnit()).isEqualTo(rm.unitId_10);
		assertThat(decommissioningList.getApprovalDate()).isEqualTo(november4);
		assertThat(decommissioningList.getDescription()).isEqualTo("zeDescription");
		assertThat(decommissioningList.getTitle()).isEqualTo("Ze list");
		assertThat(decommissioningList.getApprovalRequest()).isEqualTo(rm.getUsers().dakotaLIndienIn(zeCollection).getId());
		assertThat(decommissioningList.getApprovalUser()).isEqualTo(rm.getUsers().dakotaLIndienIn(zeCollection).getId());
		assertThat(decommissioningList.getFilingSpace()).isEqualTo(rm.filingId_A);
		assertThat(decommissioningList.getFolders()).isEqualTo(asList(aFolder.getId(), anotherFolder.getId()));
		assertThat(decommissioningList.getFolderDetails())
				.isEqualTo(asList(new DecomListFolderDetail(aFolder.getId()), new DecomListFolderDetail(anotherFolder.getId())));
		assertThat(decommissioningList.getContainers()).isEqualTo(asList(aContainer.getId(), anotherContainer.getId()));
		assertThat(decommissioningList.getContainerDetails()).isEqualTo(asList(
				new DecomListContainerDetail(aContainer.getId()),
				new DecomListContainerDetail(anotherContainer.getId())));
		assertThat(decommissioningList.getProcessingDate()).isEqualTo(december12);
		assertThat(decommissioningList.getProcessingUser()).isEqualTo(rm.getUsers().dakotaLIndienIn(zeCollection).getId());
		assertThat(decommissioningList.getValidationDate()).isEqualTo(january12_2010);
		assertThat(decommissioningList.getValidationUser()).isEqualTo(rm.getUsers().dakotaLIndienIn(zeCollection).getId());
		assertThat(decommissioningList.getDecommissioningListType()).isEqualTo(DecommissioningListType.FOLDERS_TO_CLOSE);
		assertThat(decommissioningList.getOriginArchivisticStatus()).isEqualTo(OriginStatus.SEMI_ACTIVE);

	}

	@Test
	public void givenFoldersWithUniformRuleAndNonUniformCopyAndCategoryThenNotUniform()
			throws Exception {

		decommissioningList = saveAndLoad(newFilingSpaceAList().setFolderDetailsFor(rm.folders("A04-A06")));
		assertThat(decommissioningList.hasAnalogicalMedium()).isEqualTo(true);
		assertThat(decommissioningList.hasElectronicMedium()).isEqualTo(true);
		assertThat(decommissioningList.getFoldersMediaTypes()).containsOnly(HYBRID, HYBRID, HYBRID);
		assertThat(decommissioningList.getStatus()).isEqualTo(DecomListStatus.GENERATED);
		assertThat(decommissioningList.getUniformCategory()).isEqualTo(rm.categoryId_X110);
		assertThat(decommissioningList.getUniformCopyRule().toString()).isEqualTo(newPrincipal(rm.PA_MD, "888-5-C").toString());
		assertThat(decommissioningList.getUniformCopyType()).isEqualTo(CopyType.PRINCIPAL);
		assertThat(decommissioningList.getUniformRule()).isEqualTo(rm.ruleId_1);
		assertThat(decommissioningList.isUniform()).isEqualTo(true);

		decommissioningList = saveAndLoad(newFilingSpaceAList().setFolderDetailsFor(rm.folders("A04-A06, A16-A18")));
		assertThat(decommissioningList.hasAnalogicalMedium()).isEqualTo(true);
		assertThat(decommissioningList.hasElectronicMedium()).isEqualTo(true);
		assertThat(decommissioningList.getFoldersMediaTypes()).containsOnly(HYBRID, HYBRID, HYBRID, HYBRID, HYBRID, HYBRID);
		assertThat(decommissioningList.getStatus()).isEqualTo(DecomListStatus.GENERATED);
		assertThat(decommissioningList.getUniformCategory()).isNull();
		assertThat(decommissioningList.getUniformCopyRule().toString()).isEqualTo(newPrincipal(rm.PA_MD, "888-5-C").toString());
		assertThat(decommissioningList.getUniformCopyType()).isEqualTo(CopyType.PRINCIPAL);
		assertThat(decommissioningList.getUniformRule()).isEqualTo(rm.ruleId_1);
		assertThat(decommissioningList.isUniform()).isEqualTo(false);

		decommissioningList = saveAndLoad(newFilingSpaceAList().setFolderDetailsFor(rm.folders("A22-A24")));
		assertThat(decommissioningList.getFoldersMediaTypes()).containsOnly(ANALOG, ANALOG, ANALOG);
		assertThat(decommissioningList.hasAnalogicalMedium()).isEqualTo(true);
		assertThat(decommissioningList.hasElectronicMedium()).isEqualTo(false);

		decommissioningList = saveAndLoad(newFilingSpaceAList().setFolderDetailsFor(rm.folders("A25-A27")));
		assertThat(decommissioningList.getFoldersMediaTypes()).containsOnly(ELECTRONIC, ELECTRONIC, ELECTRONIC);
		assertThat(decommissioningList.hasAnalogicalMedium()).isEqualTo(false);
		assertThat(decommissioningList.hasElectronicMedium()).isEqualTo(true);

		decommissioningList = saveAndLoad(newFilingSpaceAList().setFolderDetailsFor(rm.folders("A22-A27")));
		assertThat(decommissioningList.hasAnalogicalMedium()).isEqualTo(true);
		assertThat(decommissioningList.hasElectronicMedium()).isEqualTo(true);
		assertThat(decommissioningList.getFoldersMediaTypes())
				.containsOnly(ANALOG, ANALOG, ANALOG, ELECTRONIC, ELECTRONIC, ELECTRONIC);
		assertThat(decommissioningList.getStatus()).isEqualTo(DecomListStatus.GENERATED);
		assertThat(decommissioningList.getUniformCategory()).isEqualTo(rm.categoryId_X120);
		assertThat(decommissioningList.getUniformCopyRule()).isNull();
		assertThat(decommissioningList.getUniformCopyType()).isEqualTo(CopyType.PRINCIPAL);
		assertThat(decommissioningList.getUniformRule()).isEqualTo(rm.ruleId_4);
		assertThat(decommissioningList.isUniform()).isEqualTo(false);
	}

	private DecommissioningList newFilingSpaceAList() {
		DecommissioningList decommissioningList = schemas.newDecommissioningList();
		decommissioningList.setTitle("Ze list");
		decommissioningList.setAdministrativeUnit(rm.unitId_10);
		decommissioningList.setFilingSpace(rm.filingId_A);
		return decommissioningList;
	}

	//

	private DecommissioningList saveAndLoad(DecommissioningList decommissioningList)
			throws RecordServicesException {
		recordServices.add(decommissioningList, rm.getGandalf_managerInABC());
		return schemas.getDecommissioningList(decommissioningList.getId());
	}

	private Folder getFolder() {
		if (folder != null && schemas.getFolder(folder.getId()) != null) {
			return schemas.getFolder(folder.getId());
		} else {
			return folder = newFolder();
		}
	}

	private Folder newFolder() {
		Folder folder = schemas.newFolder();
		folder.setAdministrativeUnitEntered(rm.unitId_11);
		folder.setFilingSpaceEntered(rm.filingId_A);
		folder.setCategoryEntered(rm.categoryId_X110);
		folder.setRetentionRuleEntered(rm.ruleId_2);
		folder.setCopyStatusEntered(CopyType.PRINCIPAL);
		folder.setTitle("Ze folder");
		folder.setOpenDate(november4);
		folder.setCloseDateEntered(december12);
		try {
			recordServices.add(folder.getWrappedRecord(), User.GOD);
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}
		return schemas.getFolder(folder.getId());
	}

	private ContainerRecord getContainerRecord() {
		if (containerRecord != null && containerRecord.getWrappedRecord() != null) {
			return containerRecord;
		} else {
			return containerRecord = newContainerRecord(aString());
		}
	}

	private ContainerRecord newContainerRecord(String token) {
		ContainerRecordType type = newContainerRecordType(token);
		ContainerRecord containerRecord = schemas.newContainerRecord();
		containerRecord.setType(type);
		containerRecord.setTitle("zeContainerRecord Title " + token);
		containerRecord.setAdministrativeUnit(rm.getUnit10());
		containerRecord.setDecommissioningType(DecommissioningType.TRANSFERT_TO_SEMI_ACTIVE);
		containerRecord.setFilingSpace(rm.filingId_A);
		containerRecord.setFull(true);
		containerRecord.setStorageSpace(newStorageSpace());
		containerRecord.setTemporaryIdentifier("zeContainerRecord " + token);
		try {
			recordServices.add(containerRecord.getWrappedRecord(), User.GOD);
			return containerRecord;
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}
	}

	StorageSpace newStorageSpace() {
		StorageSpace storageSpace = schemas.newStorageSpace();
		storageSpace.setCode("storageSpace" + anInteger());
		try {
			recordServices.add(storageSpace.getWrappedRecord(), User.GOD);
			return storageSpace;
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}
	}

	ContainerRecordType newContainerRecordType(String token) {
		ContainerRecordType type = schemas.newContainerRecordType();
		type.setCode("zeContainerType." + token);
		type.setDescription("zeContainerType Description");
		type.setTitle("zeContainerType Title " + token);

		try {
			recordServices.add(type.getWrappedRecord(), User.GOD);
			return type;
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}
	}

}

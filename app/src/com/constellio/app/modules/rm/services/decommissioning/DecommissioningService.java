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
package com.constellio.app.modules.rm.services.decommissioning;

import static com.constellio.app.modules.rm.constants.RMTaxonomies.ADMINISTRATIVE_UNITS;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.joda.time.LocalDate;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.enums.DisposalType;
import com.constellio.app.modules.rm.model.enums.OriginStatus;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.modules.rm.wrappers.FilingSpace;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.modules.rm.wrappers.UniformSubdivision;
import com.constellio.app.modules.rm.wrappers.structures.FolderDetailWithType;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.ReturnedMetadatasFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.model.services.taxonomies.TaxonomiesSearchServices;
import com.google.common.base.Strings;

public class DecommissioningService {
	private final ModelLayerFactory modelLayerFactory;
	private final RecordServices recordServices;
	private final RMSchemasRecordsServices rm;
	private final TaxonomiesSearchServices taxonomiesSearchServices;
	private final TaxonomiesManager taxonomiesManager;
	private final SearchServices searchServices;
	private final String collection;
	private final RMConfigs configs;

	public DecommissioningService(String collection, ModelLayerFactory modelLayerFactory) {
		this.collection = collection;
		this.modelLayerFactory = modelLayerFactory;
		this.rm = new RMSchemasRecordsServices(collection, modelLayerFactory);
		this.taxonomiesSearchServices = modelLayerFactory.newTaxonomiesSearchService();
		this.taxonomiesManager = modelLayerFactory.getTaxonomiesManager();
		this.recordServices = modelLayerFactory.newRecordServices();
		this.searchServices = modelLayerFactory.newSearchServices();
		this.configs = new RMConfigs(modelLayerFactory.getSystemConfigurationsManager());
	}

	public DecommissioningList createDecommissioningList(DecommissioningListParams params, User user) {
		DecommissioningList decommissioningList = rm.newDecommissioningList()
				.setTitle(params.getTitle())
				.setDescription(params.getDescription())
				.setFilingSpace(params.getFilingSpace())
				.setAdministrativeUnit(params.getAdministrativeUnit())
				.setDecommissioningListType(params.getSearchType().toDecomListType())
				.setOriginArchivisticStatus(
						params.getSearchType().isFromSemiActive() ? OriginStatus.SEMI_ACTIVE : OriginStatus.ACTIVE);

		if (params.getSearchType().isFromSemiActive()) {
			List<ContainerRecord> containers = getContainersOfFolders(params.getSelectedFolderIds());
			decommissioningList.setFolderDetailsFrom(getFoldersInContainers(containers));
			decommissioningList.setContainerDetailsFrom(containers);
		} else {
			decommissioningList.setFolderDetailsFor(params.getSelectedFolderIds());
		}

		try {
			recordServices.add(decommissioningList, user);
		} catch (RecordServicesException e) {
			// TODO: Proper exception
			throw new RuntimeException(e);
		}

		return decommissioningList;
	}

	public ModelLayerFactory getModelLayerFactory() {
		return modelLayerFactory;
	}

	public RMSchemasRecordsServices getRMSchemasRecordServices() {
		return rm;
	}

	public RMConfigs getRMConfigs() {
		return configs;
	}

	public boolean isEditable(DecommissioningList decommissioningList, User user) {
		return decommissioningList.isUnprocessed() && user.has(RMPermissionsTo.EDIT_DECOMMISSIONING_LIST).on(decommissioningList);
	}

	public boolean isDeletable(DecommissioningList decommissioningList, User user) {
		return decommissioningList.isUnprocessed() && user.has(RMPermissionsTo.EDIT_DECOMMISSIONING_LIST).on(decommissioningList);
	}

	public boolean isProcessable(DecommissioningList decommissioningList, User user) {
		return user.has(RMPermissionsTo.PROCESS_DECOMMISSIONING_LIST).on(decommissioningList) &&
				decommissioningList.isUnprocessed() &&
				areAllFoldersProcessable(decommissioningList);
	}

	public boolean isSortable(DecommissioningList decommissioningList) {
		return decommissioningList.isToInactive() && hasFoldersToSort(decommissioningList);
	}

	public boolean canEditContainers(DecommissioningList decommissioningList, User user) {
		return isEditable(decommissioningList, user) && needsPacking(decommissioningList);
	}

	public boolean isFolderProcessable(DecommissioningList decommissioningList, FolderDetailWithType folder) {
		return decommissioningList.isProcessed() || folder.getDecommissioningType().isClosureOrDestroyal() ||
				(isFolderProcessable(folder) && !isFolderRepackable(decommissioningList, folder));
	}

	public void decommission(DecommissioningList decommissioningList, User user) {
		Decommissioner.forList(decommissioningList, this)
				.process(decommissioningList, user, TimeProvider.getLocalDate());
	}

	private List<ContainerRecord> getContainersOfFolders(List<String> folderIds) {
		Set<String> containerIds = new HashSet<>();
		for (Record record : recordServices.getRecordsById(collection, folderIds)) {
			Folder folder = rm.wrapFolder(record);
			containerIds.add(folder.getContainer());
		}
		return rm.wrapContainerRecords(recordServices.getRecordsById(collection, new ArrayList<>(containerIds)));
	}

	private List<Folder> getFoldersInContainers(List<ContainerRecord> containers) {
		LogicalSearchQuery query = new LogicalSearchQuery(
				from(rm.folderSchemaType()).where(rm.folderContainer()).isIn(containers));
		return rm.wrapFolders(searchServices.search(query));
	}

	private boolean needsPacking(DecommissioningList decommissioningList) {
		return !decommissioningList.getDecommissioningListType().isClosingOrDestroyal() || isSortable(decommissioningList);
	}

	private boolean areAllFoldersProcessable(DecommissioningList decommissioningList) {
		for (FolderDetailWithType folder : decommissioningList.getFolderDetailsWithType()) {
			if (!folder.getDecommissioningType().isClosureOrDestroyal() && !isFolderProcessable(folder)) {
				return false;
			}
		}
		return true;
	}

	private boolean isFolderRepackable(DecommissioningList decommissioningList, FolderDetailWithType folder) {
		return decommissioningList.isFromSemiActive() && folder.getType().potentiallyHasAnalogMedium();
	}

	private boolean isFolderProcessable(FolderDetailWithType folder) {
		return !(folder.getType().potentiallyHasAnalogMedium() &&
				Strings.isNullOrEmpty(folder.getDetail().getContainerRecordId()));
	}

	private boolean hasFoldersToSort(DecommissioningList decommissioningList) {
		LogicalSearchCondition condition = from(rm.folderSchemaType())
				.where(Schemas.IDENTIFIER).isIn(decommissioningList.getFolders())
				.andWhere(rm.folderInactiveDisposalType()).isEqualTo(DisposalType.SORT);
		return searchServices.getResultsCount(condition) > 0;
	}

	public List<AdministrativeUnit> getAllAdminUnitHierarchyOf(String administrativeUnitId) {
		Record record = rm.getAdministrativeUnit(administrativeUnitId).getWrappedRecord();
		return rm.wrapAdministrativeUnits(taxonomiesSearchServices.getAllConceptHierarchyOf(adminUnitsTaxonomy(), record));
	}

	public List<String> getAllAdminUnitIdsHierarchyOf(String administrativeUnitId) {
		Record record = rm.getAdministrativeUnit(administrativeUnitId).getWrappedRecord();
		return taxonomiesSearchServices.getAllConceptIdsHierarchyOf(adminUnitsTaxonomy(), record);
	}

	public List<String> getAdministrativeUnitsForUser(User user) {
		return taxonomiesSearchServices.getAllPrincipalConceptIdsAvailableTo(adminUnitsTaxonomy(), user);
	}

	public List<String> getAdministrativeUnitsWithFilingSpaceForUser(FilingSpace filingSpace, User user) {
		LogicalSearchCondition condition = taxonomiesSearchServices
				.getAllConceptHierarchyOfCondition(adminUnitsTaxonomy(), null)
				.andWhere(rm.administrativeUnitFilingSpaces()).isEqualTo(filingSpace);

		return searchServices.searchRecordIds(new LogicalSearchQuery()
				.setCondition(condition)
				.filteredWithUser(user));
	}

	public List<String> getUserFilingSpaces(User user) {
		return searchServices.searchRecordIds(new LogicalSearchQuery(from(rm.filingSpaceSchemaType())
				.whereAny(rm.filingSpaceAdministrators(), rm.filingSpaceUsers()).isEqualTo(user)));
	}

	public List<String> getRetentionRulesForCategory(String categoryId, String uniformSubdivisionId) {
		List<String> rules = new ArrayList<>();
		if (uniformSubdivisionId != null) {
			UniformSubdivision uniformSubdivision = new UniformSubdivision(recordServices.getDocumentById(uniformSubdivisionId),
					modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection));
			if (!uniformSubdivision.getRetentionRules().isEmpty()) {
				rules.addAll(searchServices.searchRecordIds(new LogicalSearchQuery(from(rm.retentionRuleSchemaType())
						.where(Schemas.IDENTIFIER).isIn(uniformSubdivision.getRetentionRules()))));
			}
		}

		if (rules.isEmpty() && categoryId != null) {
			Category category = new Category(recordServices.getDocumentById(categoryId),
					modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection));
			if (!category.getRententionRules().isEmpty()) {
				rules.addAll(searchServices.searchRecordIds(new LogicalSearchQuery(from(rm.retentionRuleSchemaType())
						.where(Schemas.IDENTIFIER).isIn(category.getRententionRules()))));
			}
		}

		return rules;
	}

	public boolean isCopyStatusInputPossible(Folder folder) {
		recordServices.recalculate(folder);
		if (configs.isCopyRuleTypeAlwaysModifiable()) {
			return true;

		} else if (folder.getRetentionRule() != null) {
			RetentionRule rule = rm.getRetentionRule(folder.getRetentionRule());
			return rule.isResponsibleAdministrativeUnits();

		} else {
			return false;
		}
	}

	public boolean isTransferDateInputPossibleForUser(Folder folder, User user) {
		recordServices.recalculate(folder);
		CopyRetentionRule retentionRule = folder.getMainCopyRule();

		boolean allowedByRetentionRule = retentionRule != null && retentionRule.canTransferToSemiActive();
		return allowedByRetentionRule && user.has(RMPermissionsTo.MODIFY_FOLDER_DECOMMISSIONING_DATES).on(folder);
	}

	public boolean isDepositDateInputPossibleForUser(Folder folder, User user) {
		recordServices.recalculate(folder);
		CopyRetentionRule retentionRule = folder.getMainCopyRule();

		boolean allowedByRetentionRule = retentionRule != null && retentionRule.canDeposit();
		return allowedByRetentionRule && user.has(RMPermissionsTo.MODIFY_FOLDER_DECOMMISSIONING_DATES).on(folder);
	}

	public boolean isDestructionDateInputPossibleForUser(Folder folder, User user) {
		recordServices.recalculate(folder);
		CopyRetentionRule retentionRule = folder.getMainCopyRule();

		boolean allowedByRetentionRule = retentionRule != null && retentionRule.canDestroy();
		return allowedByRetentionRule && user.has(RMPermissionsTo.MODIFY_FOLDER_DECOMMISSIONING_DATES).on(folder);
	}

	public boolean isContainerInputPossibleForUser(Folder folder, User user) {
		recordServices.recalculate(folder);

		boolean folderIsNotActive = folder.getArchivisticStatus().isSemiActiveOrInactive();
		return folderIsNotActive && user.has(RMPermissionsTo.MODIFY_FOLDER_DECOMMISSIONING_DATES).on(folder);
	}

	public String getUniformRuleOf(ContainerRecord container) {
		boolean firstTime = true;
		MetadataSchema folderSchema = rm.schema(Folder.DEFAULT_SCHEMA);
		List<Record> records = getFoldersInContainer(container, folderSchema, folderSchema.getMetadata(Folder.RETENTION_RULE));
		String retentionRule = null;
		for (Record record : records) {
			Folder folder = new Folder(record, modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection));
			if (firstTime) {
				retentionRule = folder.getRetentionRule();
				firstTime = false;
			} else {
				if (retentionRule != null && !retentionRule.equals(folder.getRetentionRule())) {
					return null;
				}
			}
		}
		return retentionRule;
	}

	public LocalDate getDispositionDate(ContainerRecord container) {
		LocalDate minimumDate = null;
		MetadataSchema folderDefaultSchema = rm.schema(Folder.DEFAULT_SCHEMA);
		List<Record> records = getFoldersInContainer(container, folderDefaultSchema,
				folderDefaultSchema.getMetadata(Folder.EXPECTED_DEPOSIT_DATE),
				folderDefaultSchema.getMetadata(Folder.EXPECTED_DESTRUCTION_DATE));
		for (Record record : records) {
			minimumDate = getMinimumLocalDate(minimumDate, record);
		}
		return minimumDate;
	}

	public List<String> getMediumTypesOf(ContainerRecord container) {
		Set<String> mediumTypesSet = new HashSet<>();
		List<String> mediumTypes = new ArrayList<>();
		MetadataSchema folderSchema = rm.schema(Folder.DEFAULT_SCHEMA);
		List<Record> records = getFoldersInContainer(container, folderSchema, folderSchema.getMetadata(Folder.MEDIUM_TYPES));
		for (Record record : records) {
			Folder folder = new Folder(record, modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection));
			mediumTypesSet.addAll(folder.getMediumTypes());
		}
		mediumTypes.addAll(mediumTypesSet);
		return mediumTypes;
	}

	public boolean hasFolderToDestroy(ContainerRecord container) {
		return true;
	}

	public boolean hasFolderToSort(ContainerRecord container) {
		return true;
	}

	public boolean hasFolderToDeposit(ContainerRecord container) {
		MetadataSchema folderDefaultSchema = rm.schema(Folder.DEFAULT_SCHEMA);
		List<Record> records = getFoldersInContainer(container, folderDefaultSchema,
				folderDefaultSchema.getMetadata(Folder.MAIN_COPY_RULE),
				folderDefaultSchema.getMetadata(Folder.CONTAINER));
		for (Record record : records) {
			Folder folder = new Folder(record, modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection));
			if (DisposalType.DEPOSIT == folder.getMainCopyRule().getInactiveDisposalType()) {
				return true;
			}
		}
		return false;
	}

	public Folder newSubFolderIn(Folder parentfolder) {
		//TODO
		Folder subFolder = rm.newFolder();
		subFolder.setParentFolder(parentfolder);
		subFolder.setRetentionRuleEntered(parentfolder.getRetentionRule());
		subFolder.setMediumTypes(parentfolder.getMediumTypes());
		subFolder.setCopyStatusEntered(parentfolder.getCopyStatusEntered());
		subFolder.setOpenDate(new LocalDate(2014, 11, 4));
		return subFolder;
	}

	private Taxonomy adminUnitsTaxonomy() {
		return taxonomiesManager.getEnabledTaxonomyWithCode(collection, ADMINISTRATIVE_UNITS);
	}

	public Folder duplicateStructureAndSave(Folder folder) {

		Transaction transaction = new Transaction();
		Folder duplicatedFolder = duplicateStructureAndAddToTransaction(folder, transaction);
		try {
			recordServices.execute(transaction);
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}
		return duplicatedFolder;
	}

	private Folder duplicateStructureAndAddToTransaction(Folder folder, Transaction transaction) {
		Folder duplicatedFolder = duplicate(folder);
		transaction.add(duplicatedFolder);

		List<Folder> children = rm.wrapFolders(searchServices.search(new LogicalSearchQuery()
				.setCondition(from(rm.folderSchemaType()).where(rm.folderParentFolder()).isEqualTo(folder))));
		for (Folder child : children) {
			Folder duplicatedChild = duplicateStructureAndAddToTransaction(child, transaction);
			duplicatedChild.setTitle(child.getTitle());
			duplicatedChild.setParentFolder(duplicatedFolder);
		}
		return duplicatedFolder;
	}

	public Folder duplicateAndSave(Folder folder) {
		try {
			Folder duplicatedFolder = duplicate(folder);
			recordServices.add(duplicatedFolder);
			return duplicatedFolder;
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}
	}

	public Folder duplicate(Folder folder) {
		Folder newFolder = rm.newFolderWithType(folder.getType());
		MetadataSchema schema = newFolder.getSchema();

		for (Metadata metadata : schema.getMetadatas().onlyEnabled().onlyNonSystemReserved().onlyManuals()) {
			newFolder.getWrappedRecord().set(metadata, folder.getWrappedRecord().get(metadata));
		}
		newFolder.setTitle(folder.getTitle() + " (Copie)");

		return newFolder;
	}

	//
	private List<Record> getFoldersInContainer(ContainerRecord container, MetadataSchema folderDefaultSchema,
			Metadata... metadatas) {
		Metadata containerMetadata = folderDefaultSchema.getMetadata(Folder.CONTAINER);
		LogicalSearchCondition condition = from(folderDefaultSchema).where(containerMetadata).isEqualTo(container.getId());
		LogicalSearchQuery query = new LogicalSearchQuery(condition);
		query.setReturnedMetadatas(ReturnedMetadatasFilter
				.onlyFields(metadatas));
		return searchServices.search(query);
	}

	private LocalDate getMinimumLocalDate(LocalDate minimumDate, Record record) {
		Folder folder = new Folder(record, modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection));
		if (folder.getExpectedDepositDate() != null && folder.getExpectedDestructionDate() != null) {
			if (folder.getExpectedDepositDate().isBefore(folder.getExpectedDestructionDate())) {
				if (minimumDate != null) {
					if (folder.getExpectedDepositDate().isBefore(minimumDate)) {
						minimumDate = folder.getExpectedDepositDate();
					}
				} else {
					minimumDate = folder.getExpectedDepositDate();
				}
			} else {
				if (minimumDate != null) {
					if (folder.getExpectedDestructionDate().isBefore(minimumDate)) {
						minimumDate = folder.getExpectedDestructionDate();
					}
				} else {
					minimumDate = folder.getExpectedDestructionDate();
				}
			}
		} else if (folder.getExpectedDepositDate() != null) {
			if (minimumDate != null) {
				if (folder.getExpectedDepositDate().isBefore(minimumDate)) {
					minimumDate = folder.getExpectedDepositDate();
				}
			} else {
				minimumDate = folder.getExpectedDepositDate();
			}

		} else if (folder.getExpectedDestructionDate() != null) {
			if (minimumDate != null) {
				if (folder.getExpectedDestructionDate().isBefore(minimumDate)) {
					minimumDate = folder.getExpectedDestructionDate();
				}
			} else {
				minimumDate = folder.getExpectedDestructionDate();
			}
		}
		return minimumDate;
	}
}

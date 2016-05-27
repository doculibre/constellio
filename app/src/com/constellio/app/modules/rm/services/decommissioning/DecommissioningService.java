package com.constellio.app.modules.rm.services.decommissioning;

import static com.constellio.app.modules.rm.constants.RMTaxonomies.ADMINISTRATIVE_UNITS;
import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.RMEmailTemplateConstants;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.constants.RMTaxonomies;
import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.enums.DecomListStatus;
import com.constellio.app.modules.rm.model.enums.DisposalType;
import com.constellio.app.modules.rm.model.enums.OriginStatus;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.modules.rm.wrappers.UniformSubdivision;
import com.constellio.app.modules.rm.wrappers.structures.FolderDetailWithType;
import com.constellio.data.utils.LangUtils;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.EmailToSend;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.structures.EmailAddress;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.StatusFilter;
import com.constellio.model.services.search.query.ReturnedMetadatasFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.model.services.taxonomies.TaxonomiesSearchOptions;
import com.constellio.model.services.taxonomies.TaxonomiesSearchServices;
import com.constellio.model.services.taxonomies.TaxonomySearchRecord;

public class DecommissioningService {
	private final ModelLayerFactory modelLayerFactory;
	private final RecordServices recordServices;
	private final RMSchemasRecordsServices rm;
	private final TaxonomiesSearchServices taxonomiesSearchServices;
	private final TaxonomiesManager taxonomiesManager;
	private final SearchServices searchServices;
	private final String collection;
	private final RMConfigs configs;
	private final DecommissioningEmailService emailService;

	public DecommissioningService(String collection, ModelLayerFactory modelLayerFactory) {
		this.collection = collection;
		this.modelLayerFactory = modelLayerFactory;
		this.rm = new RMSchemasRecordsServices(collection, modelLayerFactory);
		this.taxonomiesSearchServices = modelLayerFactory.newTaxonomiesSearchService();
		this.taxonomiesManager = modelLayerFactory.getTaxonomiesManager();
		this.recordServices = modelLayerFactory.newRecordServices();
		this.searchServices = modelLayerFactory.newSearchServices();
		this.configs = new RMConfigs(modelLayerFactory.getSystemConfigurationsManager());
		this.emailService = new DecommissioningEmailService(collection, modelLayerFactory);
	}

	public DecommissioningList createDecommissioningList(DecommissioningListParams params, User user) {
		DecommissioningList decommissioningList = rm.newDecommissioningList()
				.setTitle(params.getTitle())
				.setDescription(params.getDescription())
				.setAdministrativeUnit(params.getAdministrativeUnit())
				.setDecommissioningListType(params.getSearchType().toDecomListType())
				.setOriginArchivisticStatus(
						params.getSearchType().isFromSemiActive() ? OriginStatus.SEMI_ACTIVE : OriginStatus.ACTIVE);

		List<String> recordIds = params.getSelectedRecordIds();
		if (decommissioningList.getDecommissioningListType().isDocumentList()) {
			decommissioningList.setDocuments(recordIds);
		} else if (params.getSearchType().isFromSemiActive()) {
			List<ContainerRecord> containers = getContainersOfFolders(recordIds);
			List<Folder> folders = getFolders(recordIds);
			if (!configs.areMixedContainersAllowed()) {
				folders.addAll(getFoldersInContainers(containers));
				folders = LangUtils.withoutDuplicates(folders);
			}
			decommissioningList.setFolderDetailsFrom(folders);
			decommissioningList.setContainerDetailsFrom(containers);
		} else {
			decommissioningList.setFolderDetailsFor(recordIds);
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
		return decommissioningList.isUnprocessed() && securityService().canDelete(decommissioningList, user);
	}

	public boolean isProcessable(DecommissioningList decommissioningList, User user) {
		return decommissioningList.getDecommissioningListType().isFolderList() ?
				isFolderListProcessable(decommissioningList, user) :
				isDocumentListProcessable(decommissioningList, user);
	}

	private boolean isFolderListProcessable(DecommissioningList decommissioningList, User user) {
		return decommissioningList.isUnprocessed() &&
				areAllFoldersProcessable(decommissioningList) &&
				isApprovedOrDoesNotNeedApproval(decommissioningList) &&
				securityService().canProcess(decommissioningList, user);
	}

	private boolean isDocumentListProcessable(DecommissioningList decommissioningList, User user) {
		return decommissioningList.isUnprocessed() && securityService().canProcess(decommissioningList, user);
	}

	private boolean isApprovedOrDoesNotNeedApproval(DecommissioningList decommissioningList) {
		switch (decommissioningList.getStatus()) {
		case APPROVED:
			return true;
		case IN_APPROVAL:
		case IN_VALIDATION:
			return false;
		}
		if (decommissioningList.getDecommissioningListType().isClosing()) {
			return !configs.isApprovalRequiredForClosing();
		}
		if (decommissioningList.getDecommissioningListType().isTransfert()) {
			return !configs.isApprovalRequiredForTransfer();
		}
		if (decommissioningList.getDecommissioningListType().isDeposit()) {
			return decommissioningList.isFromActive() ?
					!configs.isApprovalRequiredForDepositOfActive() :
					!configs.isApprovalRequiredForDepositOfSemiActive();
		}
		if (decommissioningList.getDecommissioningListType().isDestroyal()) {
			return decommissioningList.isFromActive() ?
					!configs.isApprovalRequiredForDestructionOfActive() :
					!configs.isApprovalRequiredForDestructionOfSemiActive();
		}
		return false;
	}

	public boolean isApprovalRequestPossible(DecommissioningList decommissioningList, User user) {
		return decommissioningList.getStatus() != DecomListStatus.IN_VALIDATION &&
				decommissioningList.getStatus() != DecomListStatus.APPROVED &&
				decommissioningList.getStatus() != DecomListStatus.PROCESSED &&
				decommissioningList.getStatus() != DecomListStatus.IN_APPROVAL &&
				securityService().canAskApproval(decommissioningList, user);
	}

	public boolean isApprovalPossible(DecommissioningList decommissioningList, User user) {
		return decommissioningList.getStatus() != DecomListStatus.IN_VALIDATION &&
				decommissioningList.getStatus() == DecomListStatus.IN_APPROVAL &&
				!decommissioningList.getApprovalRequest().equals(user.getId()) &&
				securityService().canApprove(decommissioningList, user);
	}

	public boolean isValidationPossible(DecommissioningList decommissioningList, User user) {
		return decommissioningList.getStatus() == DecomListStatus.IN_VALIDATION &&
				securityService().canValidate(decommissioningList, user);
	}

	public boolean isValidationRequestPossible(DecommissioningList decommissioningList, User user) {
		return decommissioningList.getStatus() != DecomListStatus.APPROVED &&
				decommissioningList.getStatus() != DecomListStatus.PROCESSED &&
				securityService().canAskValidation(decommissioningList, user);
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

	public boolean isApproved(DecommissioningList decommissioningList) {
		return decommissioningList.isApproved();
	}

	public boolean isValidationRequestedFor(DecommissioningList decommissioningList, User user) {
		return securityService().canValidate(decommissioningList, user);
	}

	public void approveList(DecommissioningList decommissioningList, User user) {
		decommissioner(decommissioningList).approve(decommissioningList, user, TimeProvider.getLocalDate());
	}

	public void approvalRequest(DecommissioningList decommissioningList, User approvalUser)
			throws DecommissioningEmailServiceException, RecordServicesException {
		List<String> parameters = new ArrayList<>();
		parameters.add("decomList" + EmailToSend.PARAMETER_SEPARATOR + decommissioningList.getTitle());
		sendEmailForList(decommissioningList, approvalUser, RMEmailTemplateConstants.APPROVAL_REQUEST_TEMPLATE_ID, parameters);
		try {
			decommissioningList.setApprovalRequest(approvalUser);
			decommissioningList.setApprovalRequestDate(new LocalDate());

			Transaction transaction = new Transaction().setUser(approvalUser);
			transaction.add(decommissioningList);
			recordServices.execute(transaction);
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}
	}

	public void sendEmailForList(DecommissioningList list, User user, String templateID, List<String> parameters) {
		EmailToSend emailToSend = rm.newEmailToSend();
		try {
			List<EmailAddress> toAddresses = getEmailReceivers(emailService.getManagerEmailForList(list));
			emailToSend.setSubject($("DecommissionningServices.approvalRequest"))
					.setSendOn(TimeProvider.getLocalDateTime())
					.setParameters(parameters)
					.setTemplate(templateID)
					.setTo(toAddresses)
					.setTryingCount(0d);

			Transaction transaction = new Transaction().setUser(user);
			transaction.add(emailToSend);
			recordServices.execute(transaction);
		} catch (DecommissioningEmailServiceException e) {
			//TODO Display error cant find manager email
		} catch (RecordServicesException e) {
			//TODO Display error about email
			throw new RuntimeException(e);
		}
	}

	public void sendValidationRequest(DecommissioningList list, User sender, List<String> users, String comments) {
		List<String> parameters = new ArrayList<>();
		parameters.add("decomList" + EmailToSend.PARAMETER_SEPARATOR + list.getTitle());
		parameters.add("comments" + EmailToSend.PARAMETER_SEPARATOR + comments);

		sendEmailForList(list, null, RMEmailTemplateConstants.VALIDATION_REQUEST_TEMPLATE_ID, parameters);
		for (String user : users) {
			list.addValidationRequest(user, TimeProvider.getLocalDate());
		}
		try {
			recordServices.update(list, sender);
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}
	}

	private List<EmailAddress> getEmailReceivers(List<User> managersList) {
		List<EmailAddress> returnAddresses = new ArrayList<>();
		if (managersList == null) {
			return returnAddresses;
		}
		for (User currentManager : managersList) {
			returnAddresses.add(new EmailAddress(currentManager.getTitle(), currentManager.getEmail()));
		}
		return returnAddresses;
	}

	public void decommission(DecommissioningList decommissioningList, User user) {
		decommissioner(decommissioningList).process(decommissioningList, user, TimeProvider.getLocalDate());
	}

	public void recycleContainer(ContainerRecord container, User user) {
		Transaction transaction = new Transaction().setUser(user);
		for (Folder folder : getFoldersInContainers(container)) {
			transaction.add(folder.setContainer((String) null));
		}
		transaction.add(prepareToRecycle(container));
		try {
			recordServices.execute(transaction);
		} catch (RecordServicesException e) {
			// TODO: Proper exception
			throw new RuntimeException(e);
		}
	}

	public ContainerRecord prepareToRecycle(ContainerRecord container) {
		return container.setRealTransferDate(null).setRealDepositDate(null).setFull(false).setFillRatioEntered(0.0);
	}

	Decommissioner decommissioner(DecommissioningList decommissioningList) {
		return Decommissioner.forList(decommissioningList, this);
	}

	public List<Folder> getFoldersForAdministrativeUnit(String administrativeUnitId) {
		LogicalSearchQuery query = new LogicalSearchQuery(
				from(rm.folderSchemaType()).where(rm.folderAdministrativeUnit()).is(administrativeUnitId))
				.filteredByStatus(StatusFilter.ACTIVES)
				.sortAsc(Schemas.TITLE);
		return rm.wrapFolders(searchServices.search(query));
	}

	public List<Folder> getFoldersForClassificationPlan(String classificationPlanId) {
		LogicalSearchQuery query = new LogicalSearchQuery(
				from(rm.folderSchemaType()).where(rm.folderCategory()).is(classificationPlanId))
				.filteredByStatus(StatusFilter.ACTIVES)
				.sortAsc(Schemas.TITLE);
		return rm.wrapFolders(searchServices.search(query));
	}

	public long getFolderCountForRetentionRule(String retentionRuleId) {
		LogicalSearchQuery query = new LogicalSearchQuery(
				from(rm.folderSchemaType()).where(rm.folderRetentionRule()).is(retentionRuleId))
				.filteredByStatus(StatusFilter.ACTIVES);
		return searchServices.getResultsCount(query);
	}

	public List<RetentionRule> getRetentionRulesForAdministrativeUnit(String administrativeUnitId) {
		Set<RetentionRule> retentionRules = new HashSet<>();
		for (Folder folder : getFoldersForAdministrativeUnit(administrativeUnitId)) {
			String retentionRuleId = folder.getRetentionRule();
			if (retentionRuleId != null) {
				Record retentionRuleRecord = rm.getRetentionRule(retentionRuleId).getWrappedRecord();
				boolean deleted = retentionRuleRecord.get(Schemas.LOGICALLY_DELETED_STATUS) == null ?
						false :
						(Boolean) retentionRuleRecord.get(Schemas.LOGICALLY_DELETED_STATUS);
				if (!deleted) {
					RetentionRule retentionRule = rm.wrapRetentionRule(retentionRuleRecord);
					retentionRules.add(retentionRule);
				}
			}
		}
		return new ArrayList<>(retentionRules);
	}

	private List<ContainerRecord> getContainersOfFolders(List<String> folderIds) {
		Set<String> containerIds = new HashSet<>();
		for (Record record : recordServices.getRecordsById(collection, folderIds)) {
			Folder folder = rm.wrapFolder(record);
			containerIds.add(folder.getContainer());
		}
		return rm.wrapContainerRecords(recordServices.getRecordsById(collection, new ArrayList<>(containerIds)));
	}

	private List<Folder> getFoldersInContainers(ContainerRecord... containers) {
		return getFoldersInContainers(Arrays.asList(containers));
	}

	private List<Folder> getFoldersInContainers(List<ContainerRecord> containers) {
		LogicalSearchQuery query = new LogicalSearchQuery(
				from(rm.folderSchemaType()).where(rm.folderContainer()).isIn(containers));
		return rm.wrapFolders(searchServices.search(query));
	}

	private List<Folder> getFolders(List<String> folderIds) {
		LogicalSearchQuery query = new LogicalSearchQuery(from(rm.folderSchemaType()).where(Schemas.IDENTIFIER).isIn(folderIds));
		return rm.wrapFolders(searchServices.search(query));
	}

	private boolean needsPacking(DecommissioningList decommissioningList) {
		return !decommissioningList.getDecommissioningListType().isClosingOrDestroyal() || isSortable(decommissioningList);
	}

	private boolean areAllFoldersProcessable(DecommissioningList decommissioningList) {
		for (FolderDetailWithType folder : decommissioningList.getFolderDetailsWithType()) {
			if (folder.isIncluded() && !folder.getDecommissioningType().isClosureOrDestroyal() && !isFolderProcessable(folder)) {
				return false;
			}
		}
		return true;
	}

	private boolean isFolderRepackable(DecommissioningList decommissioningList, FolderDetailWithType folder) {
		return decommissioningList.isFromSemiActive() && folder.getType().potentiallyHasAnalogMedium();
	}

	private boolean isFolderProcessable(FolderDetailWithType folder) {
		return !(folder.getType().potentiallyHasAnalogMedium() && StringUtils.isBlank(folder.getDetail().getContainerRecordId()));
	}

	private boolean hasFoldersToSort(DecommissioningList decommissioningList) {
		LogicalSearchCondition condition = from(rm.folderSchemaType())
				.where(Schemas.IDENTIFIER).isIn(decommissioningList.getFolders())
				.andWhere(rm.folderInactiveDisposalType()).isEqualTo(DisposalType.SORT);
		return searchServices.hasResults(condition);
	}

	public List<String> getAllAdminUnitIdsHierarchyOf(String administrativeUnitId) {
		Record record = rm.getAdministrativeUnit(administrativeUnitId).getWrappedRecord();
		return taxonomiesSearchServices.getAllConceptIdsHierarchyOf(adminUnitsTaxonomy(), record);
	}

	public List<String> getChildrenAdministrativeUnit(String administrativeUnitId, User user) {
		Record record = rm.getAdministrativeUnit(administrativeUnitId).getWrappedRecord();

		return toIdList(taxonomiesSearchServices.getVisibleChildConcept(user, RMTaxonomies.ADMINISTRATIVE_UNITS,
				record, new TaxonomiesSearchOptions()));
	}

	private List<String> toIdList(List<TaxonomySearchRecord> visibleChildConcept) {
		List<String> ids = new ArrayList<>();
		for (TaxonomySearchRecord record : visibleChildConcept) {
			ids.add(record.getId());
		}
		return ids;
	}

	public List<String> getAdministrativeUnitsForUser(User user) {
		return modelLayerFactory.newAuthorizationsServices()
				.getConceptsForWhichUserHasPermission(RMPermissionsTo.PROCESS_DECOMMISSIONING_LIST, user);
	}

	public List<String> getRetentionRulesForCategory(String categoryId, String uniformSubdivisionId) {
		return getRetentionRulesForCategory(categoryId, uniformSubdivisionId, StatusFilter.ALL);
	}

	public List<String> getRetentionRulesForCategory(String categoryId, String uniformSubdivisionId, StatusFilter statusFilter) {
		List<String> rules = new ArrayList<>();
		if (uniformSubdivisionId != null) {
			UniformSubdivision uniformSubdivision = new UniformSubdivision(recordServices.getDocumentById(uniformSubdivisionId),
					modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection));
			if (!uniformSubdivision.getRetentionRules().isEmpty()) {
				rules.addAll(searchServices.searchRecordIds(new LogicalSearchQuery(from(rm.retentionRuleSchemaType())
						.where(Schemas.IDENTIFIER).isIn(uniformSubdivision.getRetentionRules())
						.andWhere(Schemas.LOGICALLY_DELETED_STATUS).isFalseOrNull()).filteredByStatus(statusFilter)));
			}
		}

		if (rules.isEmpty() && categoryId != null) {
			Category category = new Category(recordServices.getDocumentById(categoryId),
					modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection));
			if (!category.getRententionRules().isEmpty()) {
				rules.addAll(searchServices.searchRecordIds(new LogicalSearchQuery(from(rm.retentionRuleSchemaType())
						.where(Schemas.IDENTIFIER).isIn(category.getRententionRules())
						.andWhere(Schemas.LOGICALLY_DELETED_STATUS).isFalseOrNull()).filteredByStatus(statusFilter)));
			}
		}

		return rules;
	}

	public boolean isCopyStatusInputPossible(Folder folder) {
		return isCopyStatusInputPossible(folder, null);
	}

	public boolean isCopyStatusInputPossible(Folder folder, User folderCreator) {
		boolean hasPrimaries = true;
		boolean hasAdminUnits = true;
		boolean isResponsibleAdministrativeUnits = false;

		recordServices.recalculate(folder);

		RetentionRule rule = null;
		if (folder.getRetentionRule() != null) {
			rule = rm.getRetentionRule(folder.getRetentionRule());
			hasAdminUnits = !rule.getAdministrativeUnits().isEmpty();
			hasPrimaries = !rule.getPrincipalCopies().isEmpty();
			isResponsibleAdministrativeUnits = rule.isResponsibleAdministrativeUnits();
		}
		if (!hasPrimaries) {
			return false;
		}
		if (configs.isCopyRuleTypeAlwaysModifiable()) {
			return true;
		} else {
			if (hasAdminUnits && isResponsibleAdministrativeUnits) {
				if (folder.getWrappedRecord().isSaved()) {
					//folder modification
					return true;
				} else {
					//folder creation
					if (configs.isOpenHolder()) {
						List<String> creatorAdminUnits = getUserAdminUnits(folderCreator);
						Set<String> ruleUnitsAndSubUnits = getRuleHierarchyUnits(rule);
						return CollectionUtils.intersection(creatorAdminUnits, ruleUnitsAndSubUnits).isEmpty();
					}
				}
			}
		}
		return isResponsibleAdministrativeUnits;
	}

	private Set<String> getRuleHierarchyUnits(RetentionRule rule) {
		Set<String> returnSet = new HashSet<>();
		Taxonomy principalTaxonomy = modelLayerFactory.getTaxonomiesManager().getPrincipalTaxonomy(
				rule.getCollection());
		for (String unit : rule.getAdministrativeUnits()) {
			List<String> currentUnits = taxonomiesSearchServices
					.getAllConceptIdsHierarchyOf(principalTaxonomy, rm.getAdministrativeUnit(unit).getWrappedRecord());
			returnSet.addAll(currentUnits);
		}
		return returnSet;
	}

	private List<String> getUserAdminUnits(User user) {
		List<String> returnList = new ArrayList<>();
		LogicalSearchCondition condition = LogicalSearchQueryOperators.from(this.rm.administrativeUnitSchema()).returnAll();
		List<Record> results = this.searchServices.search(new LogicalSearchQuery(condition).filteredWithUserWrite(user)
				.setReturnedMetadatas(ReturnedMetadatasFilter.idVersionSchema()));
		for (Record record : results) {
			returnList.add(record.getId());
		}
		return returnList;
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
		return user.has(RMPermissionsTo.MODIFY_FOLDER_DECOMMISSIONING_DATES).on(folder)
				&& (folderIsNotActive || configs.areActiveInContainersAllowed());
	}

	public String getUniformRuleOf(ContainerRecord container) {
		boolean firstTime = true;
		MetadataSchema folderSchema = rm.schema(Folder.DEFAULT_SCHEMA);
		List<Record> records = getFoldersInContainer(container, folderSchema.getMetadata(Folder.RETENTION_RULE));
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
		List<Record> records = getFoldersInContainer(container, rm.folderExpectedDepositDate(),
				rm.folderExpectedDestructionDate());
		for (Record record : records) {
			minimumDate = getMinimumLocalDate(minimumDate, record);
		}
		return minimumDate;
	}

	public List<String> getMediumTypesOf(ContainerRecord container) {
		Set<String> mediumTypesSet = new HashSet<>();
		List<String> mediumTypes = new ArrayList<>();
		List<Record> records = getFoldersInContainer(container, rm.folderMediumTypes());
		for (Record record : records) {
			Folder folder = rm.wrapFolder(record);
			mediumTypesSet.addAll(folder.getMediumTypes());
		}
		mediumTypes.addAll(mediumTypesSet);
		return mediumTypes;
	}

	public boolean hasFolderToDeposit(ContainerRecord container) {
		List<Record> records = getFoldersInContainer(container, rm.folderMainCopyRule(), rm.folderContainer());
		for (Record record : records) {
			Folder folder = rm.wrapFolder(record);
			if (DisposalType.DEPOSIT == folder.getMainCopyRule().getInactiveDisposalType()) {
				return true;
			}
		}
		return false;
	}

	public Folder newSubFolderIn(Folder parentfolder) {
		Folder subFolder = rm.newFolder();
		subFolder.setParentFolder(parentfolder);
		subFolder.setRetentionRuleEntered(parentfolder.getRetentionRule());
		subFolder.setMediumTypes(parentfolder.getMediumTypes());
		subFolder.setCopyStatusEntered(parentfolder.getCopyStatusEntered());
		subFolder.setOpenDate(TimeProvider.getLocalDate());
		return subFolder;
	}

	private Taxonomy adminUnitsTaxonomy() {
		return taxonomiesManager.getEnabledTaxonomyWithCode(collection, ADMINISTRATIVE_UNITS);
	}

	public Folder duplicateStructureAndSave(Folder folder, User currentUser) {

		Transaction transaction = new Transaction();
		Folder duplicatedFolder = duplicateStructureAndAddToTransaction(folder, currentUser, transaction);
		try {
			recordServices.execute(transaction);
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}
		return duplicatedFolder;
	}

	private Folder duplicateStructureAndAddToTransaction(Folder folder, User currentUser, Transaction transaction) {
		Folder duplicatedFolder = duplicate(folder, currentUser);
		transaction.add(duplicatedFolder);

		List<Folder> children = rm.wrapFolders(searchServices.search(new LogicalSearchQuery()
				.setCondition(from(rm.folderSchemaType()).where(rm.folderParentFolder()).isEqualTo(folder))));
		for (Folder child : children) {
			Folder duplicatedChild = duplicateStructureAndAddToTransaction(child, currentUser, transaction);
			duplicatedChild.setTitle(child.getTitle());
			duplicatedChild.setParentFolder(duplicatedFolder);
		}
		return duplicatedFolder;
	}

	public Folder duplicateAndSave(Folder folder, User currentUser) {
		try {
			Folder duplicatedFolder = duplicate(folder, currentUser);
			recordServices.add(duplicatedFolder);
			return duplicatedFolder;
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}
	}

	public Folder duplicate(Folder folder, User currentUser) {
		Folder newFolder = rm.newFolderWithType(folder.getType());
		MetadataSchema schema = newFolder.getSchema();

		for (Metadata metadata : schema.getMetadatas().onlyEnabled().onlyNonSystemReserved().onlyManuals().onlyDuplicatable()) {
			newFolder.getWrappedRecord().set(metadata, folder.getWrappedRecord().get(metadata));
		}
		newFolder.setTitle(folder.getTitle() + " (Copie)");
		newFolder.setFormCreatedBy(currentUser);
		newFolder.setFormCreatedOn(TimeProvider.getLocalDateTime());

		return newFolder;
	}

	private List<Record> getFoldersInContainer(ContainerRecord container, Metadata... metadatas) {
		LogicalSearchQuery query = new LogicalSearchQuery(
				from(rm.folderSchemaType()).where(rm.folderContainer()).isEqualTo(container))
				.setReturnedMetadatas(ReturnedMetadatasFilter.onlyMetadatas(metadatas));
		return searchServices.search(query);
	}

	private LocalDate getMinimumLocalDate(LocalDate minimumDate, Record record) {
		Folder folder = rm.wrapFolder(record);
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

	private DecommissioningSecurityService securityService() {
		return new DecommissioningSecurityService(collection, modelLayerFactory);
	}
}


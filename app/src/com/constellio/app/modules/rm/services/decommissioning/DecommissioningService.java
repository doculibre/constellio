package com.constellio.app.modules.rm.services.decommissioning;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.RMEmailTemplateConstants;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.constants.RMTaxonomies;
import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.RetentionPeriod;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.model.enums.DecomListStatus;
import com.constellio.app.modules.rm.model.enums.DecommissioningType;
import com.constellio.app.modules.rm.model.enums.DisposalType;
import com.constellio.app.modules.rm.model.enums.FolderStatus;
import com.constellio.app.modules.rm.model.enums.OriginStatus;
import com.constellio.app.modules.rm.navigation.RMNavigationConfiguration;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.borrowingServices.BorrowingType;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningServiceException.DecommissioningServiceException_TooMuchOptimisticLockingWhileAttemptingToDecommission;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RMTask;
import com.constellio.app.modules.rm.wrappers.RMUserFolder;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.modules.rm.wrappers.UniformSubdivision;
import com.constellio.app.modules.rm.wrappers.structures.Comment;
import com.constellio.app.modules.rm.wrappers.structures.FolderDetailWithType;
import com.constellio.app.modules.rm.wrappers.type.YearType;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.utils.LangUtils;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.RecordUpdateOptions;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.EmailToSend;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.records.wrappers.UserDocument;
import com.constellio.model.entities.records.wrappers.UserFolder;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.structures.EmailAddress;
import com.constellio.model.extensions.ModelLayerCollectionExtensions;
import com.constellio.model.extensions.events.schemas.PutSchemaRecordsInTrashEvent;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ContentVersionDataSummary;
import com.constellio.model.services.emails.EmailRecipientServices;
import com.constellio.model.services.extensions.ModelLayerExtensions;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.logging.LoggingServices;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.StatusFilter;
import com.constellio.model.services.search.query.ReturnedMetadatasFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.taxonomies.ConceptNodesTaxonomySearchServices;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.model.services.taxonomies.TaxonomiesSearchOptions;
import com.constellio.model.services.taxonomies.TaxonomiesSearchServices;
import com.constellio.model.services.taxonomies.TaxonomySearchRecord;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.constellio.app.modules.rm.constants.RMTaxonomies.ADMINISTRATIVE_UNITS;
import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class DecommissioningService {
	private static Logger LOGGER = LoggerFactory.getLogger(DecommissioningService.class);
	private final AppLayerFactory appLayerFactory;
	private final ModelLayerFactory modelLayerFactory;
	private final RecordServices recordServices;
	private final RMSchemasRecordsServices rm;
	private final TaxonomiesSearchServices taxonomiesSearchServices;
	private final ConceptNodesTaxonomySearchServices conceptNodesTaxonomySearchServices;
	private final TaxonomiesManager taxonomiesManager;
	private final SearchServices searchServices;
	private final String collection;
	private final RMConfigs configs;
	private final DecommissioningEmailService emailService;
	private final ConstellioEIMConfigs eimConfigs;
	private final MetadataSchemasManager metadataSchemasManager;
	private final LoggingServices loggingServices;

	public DecommissioningService(String collection, AppLayerFactory appLayerFactory) {
		this.collection = collection;
		this.appLayerFactory = appLayerFactory;
		this.modelLayerFactory = appLayerFactory.getModelLayerFactory();
		this.rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		this.taxonomiesSearchServices = modelLayerFactory.newTaxonomiesSearchService();
		this.conceptNodesTaxonomySearchServices = new ConceptNodesTaxonomySearchServices(modelLayerFactory);
		this.taxonomiesManager = modelLayerFactory.getTaxonomiesManager();
		this.recordServices = modelLayerFactory.newRecordServices();
		this.searchServices = modelLayerFactory.newSearchServices();
		this.configs = new RMConfigs(modelLayerFactory.getSystemConfigurationsManager());
		this.emailService = new DecommissioningEmailService(collection, modelLayerFactory);
		this.eimConfigs = new ConstellioEIMConfigs(modelLayerFactory.getSystemConfigurationsManager());
		this.metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
		this.loggingServices = modelLayerFactory.newLoggingServices();
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
			decommissioningList.setFolderDetailsFor(folders, params.getFolderDetailStatus());
			decommissioningList.setContainerDetailsFrom(containers);
		} else {
			decommissioningList.setFolderDetailsFor(rm.getFolders(recordIds), params.getFolderDetailStatus());
			decommissioningList.setContainerDetailsFrom(getContainersOfFolders(recordIds));
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
		return decommissioningList.isUnprocessed() && securityService().canModify(decommissioningList, user);
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

	public boolean isFolderRemovableFromContainer(DecommissioningList decommissioningList,
												  FolderDetailWithType folder) {
		return !decommissioningList.isProcessed() && !folder.getDecommissioningType().isClosureOrDestroyal()
			   && isRemovableFromContainer(decommissioningList, folder);
	}

	public boolean isApproved(DecommissioningList decommissioningList) {
		return decommissioningList.isApproved();
	}

	public boolean isValidationRequestedFor(DecommissioningList decommissioningList, User user) {
		return securityService().canValidate(decommissioningList, user);
	}

	public void approveList(DecommissioningList decommissioningList, User user) throws DecommissioningServiceException {
		approveList(decommissioningList, user, 0);
	}

	public void approveList(DecommissioningList decommissioningList, User user, int attempt)
			throws DecommissioningServiceException {

		try {
			decommissioner(decommissioningList).approve(decommissioningList, user, TimeProvider.getLocalDate());

		} catch (RecordServicesException.OptimisticLocking e) {
			if (attempt < 3) {
				LOGGER.warn("Operation failed, retrying...", e);
				approveList(rm.getDecommissioningList(decommissioningList.getId()), user, attempt + 1);
			} else {
				throw new DecommissioningServiceException_TooMuchOptimisticLockingWhileAttemptingToDecommission();
			}
		}
	}

	public void denyApprovalOnList(DecommissioningList decommissioningList, User denier, String comment)
			throws DecommissioningServiceException {
		denyApprovalOnList(decommissioningList, denier, comment, 0);
	}

	public void denyApprovalOnList(DecommissioningList decommissioningList, User denier, String comment, int attempt)
			throws DecommissioningServiceException {
		try {

			decommissioner(decommissioningList).denyApproval(decommissioningList, denier, comment);
		} catch (RecordServicesException.OptimisticLocking e) {
			if (attempt < 3) {
				LOGGER.warn("Operation failed, retrying...", e);
				denyApprovalOnList(rm.getDecommissioningList(decommissioningList.getId()), denier, comment, attempt + 1);
			} else {
				throw new DecommissioningServiceException_TooMuchOptimisticLockingWhileAttemptingToDecommission();
			}
		}
	}

	public void approvalRequest(List<User> managerList, DecommissioningList decommissioningList, User approvalUser)
			throws RecordServicesException {
		List<String> parameters = new ArrayList<>();
		boolean isAddingRecordIdInEmails = eimConfigs.isAddingRecordIdInEmails();
		if (isAddingRecordIdInEmails) {
			parameters.add("decomList" + EmailToSend.PARAMETER_SEPARATOR + StringEscapeUtils.escapeHtml4(decommissioningList.getTitle()) + " (" + decommissioningList.getId() + ")");
		} else {
			parameters.add("decomList" + EmailToSend.PARAMETER_SEPARATOR + StringEscapeUtils.escapeHtml4(decommissioningList.getTitle()));
		}

		String constellioUrl = eimConfigs.getConstellioUrl();
		String displayURL = RMNavigationConfiguration.DECOMMISSIONING_LIST_DISPLAY;
		parameters.add("constellioURL" + EmailToSend.PARAMETER_SEPARATOR + constellioUrl);
		parameters.add("recordURL" + EmailToSend.PARAMETER_SEPARATOR + constellioUrl + "#!" + displayURL + "/" + decommissioningList.getId());

		sendEmailForList(managerList, approvalUser, RMEmailTemplateConstants.APPROVAL_REQUEST_TEMPLATE_ID, parameters);
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

	public void sendEmailForList(List<User> userList, User user, String templateID, List<String> parameters) {
		EmailToSend emailToSend = rm.newEmailToSend();
		try {
			List<EmailAddress> toAddresses = getEmailReceivers(userList);
			String subject = "";
			if (RMEmailTemplateConstants.APPROVAL_REQUEST_TEMPLATE_ID.equals(templateID)) {
				subject = $("DecommissionningServices.approvalRequestEmailTitle");
			} else {
				subject = $("DecommissionningServices.validationRequest");
			}

			emailToSend.setSubject(subject)
					.setSendOn(TimeProvider.getLocalDateTime())
					.setParameters(parameters)
					.setTemplate(templateID)
					.setTo(toAddresses)
					.setTryingCount(0d);

			Transaction transaction = new Transaction().setUser(user);
			transaction.add(emailToSend);
			recordServices.execute(transaction);
		} catch (RecordServicesException e) {
			//TODO Display error about email
			throw new RuntimeException(e);
		}
	}

	public void sendEmailForList(DecommissioningList list, User user, List<String> usersToSendEmail, String templateID,
								 List<String> parameters) {
		EmailToSend emailToSend = rm.newEmailToSend();
		try {
			List<EmailAddress> toAddresses = new ArrayList<>();
			if (usersToSendEmail == null || usersToSendEmail.isEmpty()) {
				toAddresses = getEmailReceivers(emailService.getManagerEmailForList(list));
			} else {
				toAddresses = getEmailReceivers(rm.getUsers(usersToSendEmail));
			}
			String subject = "";
			if (RMEmailTemplateConstants.APPROVAL_REQUEST_TEMPLATE_ID.equals(templateID)) {
				subject = $("DecommissionningServices.approvalRequestEmailTitle");
			} else {
				subject = $("DecommissionningServices.validationRequest");
			}

			emailToSend.setSubject(subject)
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

	public void sendValidationRequest(DecommissioningList list, User sender, List<String> users, String comments,
									  boolean saveComment) {
		List<String> parameters = new ArrayList<>();
		List<Comment> commentaires = new ArrayList<>();
		boolean isAddingRecordIdInEmails = eimConfigs.isAddingRecordIdInEmails();
		if (isAddingRecordIdInEmails) {
			parameters.add("decomList" + EmailToSend.PARAMETER_SEPARATOR + StringEscapeUtils.escapeHtml4(list.getTitle()) + " (" + list.getId() + ")");
		} else {
			parameters.add("decomList" + EmailToSend.PARAMETER_SEPARATOR + StringEscapeUtils.escapeHtml4(list.getTitle()));
		}

		parameters.add("comments" + EmailToSend.PARAMETER_SEPARATOR + StringEscapeUtils.escapeHtml4(comments));

		String constellioUrl = eimConfigs.getConstellioUrl();
		String displayURL = RMNavigationConfiguration.DECOMMISSIONING_LIST_DISPLAY;
		parameters.add("constellioURL" + EmailToSend.PARAMETER_SEPARATOR + constellioUrl);
		parameters.add("recordURL" + EmailToSend.PARAMETER_SEPARATOR + constellioUrl + "#!" + displayURL + "/" + list.getId());

		sendEmailForList(list, null, users, RMEmailTemplateConstants.VALIDATION_REQUEST_TEMPLATE_ID, parameters);
		for (String user : users) {
			list.addValidationRequest(user, TimeProvider.getLocalDate());
		}
		for (Comment comment : list.getComments()) {
			commentaires.add(comment);
		}
		if (saveComment) {
			Comment comment = new Comment();
			comment.setMessage(comments);
			comment.setUser(sender);
			comment.setDateTime(LocalDateTime.now());
			commentaires.add(comment);
			list.setComments(commentaires);
		}
		try {
			recordServices.update(list, sender);
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}
	}

	private List<EmailAddress> getEmailReceivers(List<User> managersList) {
		return EmailRecipientServices.toFilteredEmailAddressList(managersList);
	}

	public void decommission(DecommissioningList decommissioningList, User user)
			throws DecommissioningServiceException {

		decommission(decommissioningList, user, 0);
	}

	public void decommission(DecommissioningList decommissioningList, User user, int attempt)
			throws DecommissioningServiceException {

		try {
			decommissioner(decommissioningList).process(decommissioningList, user, TimeProvider.getLocalDate());

		} catch (RecordServicesException.OptimisticLocking e) {
			modelLayerFactory.getRecordsCaches().getCache(decommissioningList.getCollection())
					.reloadSchemaType(Folder.SCHEMA_TYPE);

			modelLayerFactory.getRecordsCaches().getCache(decommissioningList.getCollection())
					.reloadSchemaType(ContainerRecord.SCHEMA_TYPE);

			if (attempt < 3) {
				LOGGER.warn("Decommission failed, retrying...", e);
				decommission(rm.getDecommissioningList(decommissioningList.getId()), user, attempt + 1);
			} else {
				throw new DecommissioningServiceException_TooMuchOptimisticLockingWhileAttemptingToDecommission();
			}
		}

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
		return Decommissioner.forList(decommissioningList, this, appLayerFactory);
	}

	public List<Folder> getFoldersForAdministrativeUnit(String administrativeUnitId) {
		LogicalSearchQuery query = new LogicalSearchQuery(
				from(rm.folder.schemaType()).where(rm.folder.administrativeUnit()).is(administrativeUnitId))
				.filteredByStatus(StatusFilter.ACTIVES)
				.sortAsc(Schemas.TITLE);
		return rm.wrapFolders(searchServices.search(query));
	}

	public List<Folder> getFoldersForClassificationPlan(String classificationPlanId) {
		LogicalSearchQuery query = new LogicalSearchQuery(
				from(rm.folder.schemaType()).where(rm.folder.category()).is(classificationPlanId))
				.filteredByStatus(StatusFilter.ACTIVES)
				.sortAsc(Schemas.TITLE);
		return rm.wrapFolders(searchServices.search(query));
	}

	public long getFolderCountForRetentionRule(String retentionRuleId) {
		LogicalSearchQuery query = new LogicalSearchQuery(
				from(rm.folder.schemaType()).where(rm.folder.retentionRule()).is(retentionRuleId))
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
				from(rm.folder.schemaType()).where(rm.folder.container()).isIn(containers));
		return rm.wrapFolders(searchServices.search(query));
	}

	private List<Folder> getFolders(List<String> folderIds) {
		LogicalSearchQuery query = new LogicalSearchQuery(from(rm.folder.schemaType()).where(Schemas.IDENTIFIER).isIn(folderIds));
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
		return folder.getType().potentiallyHasAnalogMedium();
	}

	private boolean isRemovableFromContainer(DecommissioningList decommissioningList, FolderDetailWithType folder) {
		return folder.getType().potentiallyHasAnalogMedium();
	}

	private boolean isFolderProcessable(FolderDetailWithType folder) {
		return !(folder.getType().potentiallyHasAnalogMedium() && StringUtils.isBlank(folder.getDetail().getContainerRecordId()));
	}

	private boolean hasFoldersToSort(DecommissioningList decommissioningList) {
		LogicalSearchCondition condition = from(rm.folder.schemaType())
				.where(Schemas.IDENTIFIER).isIn(decommissioningList.getFolders())
				.andWhere(rm.folder.inactiveDisposalType()).isEqualTo(DisposalType.SORT);
		return searchServices.hasResults(condition);
	}

	public List<String> getAllAdminUnitIdsHierarchyOf(String administrativeUnitId) {
		Record record = rm.getAdministrativeUnit(administrativeUnitId).getWrappedRecord();
		return conceptNodesTaxonomySearchServices.getAllConceptIdsHierarchyOf(adminUnitsTaxonomy(), record);
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

	public List<String> getRetentionRulesForCategory(String categoryId, String uniformSubdivisionId) {
		return getRetentionRulesForCategory(categoryId, uniformSubdivisionId, StatusFilter.ALL);
	}

	public List<String> getRetentionRulesForCategory(String categoryId, String uniformSubdivisionId,
													 StatusFilter statusFilter) {
		List<String> rules = new ArrayList<>();
		if (uniformSubdivisionId != null) {
			UniformSubdivision uniformSubdivision = new UniformSubdivision(recordServices.getDocumentById(uniformSubdivisionId),
					modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection));
			if (!uniformSubdivision.getRetentionRules().isEmpty()) {
				for (String retentionRuleId : uniformSubdivision.getRetentionRules()) {
					RetentionRule retentionRule = rm.getRetentionRule(retentionRuleId);
					if (LangUtils.isFalseOrNull(retentionRule.get(Schemas.LOGICALLY_DELETED_STATUS))) {
						rules.add(retentionRuleId);
					}
				}
			}
		}

		if (rules.isEmpty() && categoryId != null) {
			Category category = new Category(recordServices.getDocumentById(categoryId),
					modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection));
			if (!category.getRententionRules().isEmpty()) {

				for (String retentionRuleId : category.getRententionRules()) {
					RetentionRule retentionRule = rm.getRetentionRule(retentionRuleId);
					if (LangUtils.isFalseOrNull(retentionRule.get(Schemas.LOGICALLY_DELETED_STATUS))) {
						rules.add(retentionRuleId);
					}
				}

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
			List<String> currentUnits = conceptNodesTaxonomySearchServices
					.getAllConceptIdsHierarchyOf(principalTaxonomy, rm.getAdministrativeUnit(unit).getWrappedRecord());
			returnSet.addAll(currentUnits);
		}
		return returnSet;
	}

	private List<String> getUserAdminUnits(User user) {
		List<String> returnList = new ArrayList<>();
		LogicalSearchCondition condition = LogicalSearchQueryOperators.from(this.rm.administrativeUnit.schema()).returnAll();
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
		LocalDate actualTransferDate = folder.getActualTransferDate();

		boolean allowedByRetentionRule = getRMConfigs().isAllowTransferDateFieldWhenCopyRuleHasNoSemiActiveState() || (retentionRule != null && retentionRule.canTransferToSemiActive());
		return (actualTransferDate != null || allowedByRetentionRule) && user.has(RMPermissionsTo.MODIFY_FOLDER_DECOMMISSIONING_DATES).on(folder);
	}

	public boolean isDepositDateInputPossibleForUser(Folder folder, User user) {
		recordServices.recalculate(folder);
		CopyRetentionRule retentionRule = folder.getMainCopyRule();
		LocalDate actualDepositDate = folder.getActualDepositDate();

		boolean allowedByRetentionRule = retentionRule != null && retentionRule.canDeposit();
		return (actualDepositDate != null || allowedByRetentionRule) && user.has(RMPermissionsTo.MODIFY_FOLDER_DECOMMISSIONING_DATES).on(folder);
	}

	public boolean isDestructionDateInputPossibleForUser(Folder folder, User user) {
		recordServices.recalculate(folder);
		CopyRetentionRule retentionRule = folder.getMainCopyRule();
		LocalDate actualDestructionDate = folder.getActualDestructionDate();

		boolean allowedByRetentionRule = retentionRule != null && retentionRule.canDestroy();
		return (actualDestructionDate != null || allowedByRetentionRule) && user.has(RMPermissionsTo.MODIFY_FOLDER_DECOMMISSIONING_DATES).on(folder);
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
		LocalDate comparedDate = null;
		List<Record> records = getFoldersInContainer(container, rm.folder.expectedDepositDate(),
				rm.folder.expectedDestructionDate());

		if (getRMConfigs().isPopulateBordereauxWithLesserDispositionDate()) {
			for (Record record : records) {
				comparedDate = LangUtils.min(comparedDate, getMinimumLocalDateForRecord(record, false));
			}
		} else {
			for (Record record : records) {
				comparedDate = LangUtils.max(comparedDate, getMaximalLocalDateForRecord(record, false));
			}
		}

		return comparedDate;
	}

	public String getSemiActiveInterval(ContainerRecord container) {
		Map<String, String> maximalIntervals = new HashMap<>();
		maximalIntervals.put("fixed", null);
		maximalIntervals.put("888", null);
		maximalIntervals.put("999", null);
		List<Record> records = getFoldersInContainer(container, rm.folder.mainCopyRule());
		for (Record record : records) {
			getMaximalSemiActiveInterval(maximalIntervals, record);
		}

		String interval = "";
		String separator = "";
		String fixed = maximalIntervals.get("fixed");
		String variable888 = maximalIntervals.get("888");
		String variable999 = maximalIntervals.get("999");
		if (fixed != null) {
			interval += fixed + " an(s)";
			separator = " / ";
		}
		if (variable888 != null) {
			interval += separator + variable888;
			separator = " / ";
		}
		if (variable999 != null) {
			interval += separator + variable999;
		}
		return interval;
	}

	public List<String> getMediumTypesOf(ContainerRecord container) {
		Set<String> mediumTypesSet = new HashSet<>();
		List<String> mediumTypes = new ArrayList<>();
		List<Record> records = getFoldersInContainer(container, rm.folder.mediumTypes());
		for (Record record : records) {
			Folder folder = rm.wrapFolder(record);
			mediumTypesSet.addAll(folder.getMediumTypes());
		}
		mediumTypes.addAll(mediumTypesSet);
		return mediumTypes;
	}

	public boolean hasFolderToDeposit(ContainerRecord container) {
		List<Record> records = getFoldersInContainer(container);
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

	public Folder duplicateStructureAndSave(Folder folder, User currentUser)
			throws RecordServicesException {
		return duplicateStructure(folder, currentUser, true);
	}

	public Folder duplicateStructure(Folder folder, User currentUser, boolean forceTitleDuplication)
			throws RecordServicesException {

		Transaction transaction = new Transaction();
		Folder duplicatedFolder = duplicateStructureAndAddToTransaction(folder, currentUser, transaction, forceTitleDuplication);
		recordServices.execute(transaction);
		return duplicatedFolder;
	}

	public void validateDuplicateStructure(Folder folder, User currentUser, boolean forceTitleDuplication)
			throws RecordServicesException {
		Transaction transaction = new Transaction();
		Folder duplicatedFolder = duplicateStructureAndAddToTransaction(folder, currentUser, transaction, forceTitleDuplication);
		recordServices.prepareRecords(transaction);
	}

	public Folder duplicateStructureAndDocuments(Folder folder, User currentUser, boolean forceTitleDuplication) {

		Transaction transaction = new Transaction();
		Folder duplicatedFolder = duplicateStructureAndDocumentsAndAddToTransaction(folder, currentUser, transaction,
				forceTitleDuplication);
		try {
			recordServices.execute(transaction);
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}
		return duplicatedFolder;
	}

	private Folder duplicateStructureAndAddToTransaction(Folder folder, User currentUser, Transaction transaction,
														 boolean forceTitleDuplication) {
		Folder duplicatedFolder = duplicate(folder, currentUser, forceTitleDuplication);
		transaction.add(duplicatedFolder);

		List<Folder> children = rm.wrapFolders(searchServices.search(new LogicalSearchQuery()
				.setCondition(from(rm.folder.schemaType()).where(rm.folder.parentFolder()).isEqualTo(folder))
				.filteredByStatus(StatusFilter.ACTIVES)));
		for (Folder child : children) {
			Folder duplicatedChild = duplicateStructureAndAddToTransaction(child, currentUser, transaction,
					forceTitleDuplication);
			duplicatedChild.setTitle(child.getTitle());
			duplicatedChild.setParentFolder(duplicatedFolder);
		}
		return duplicatedFolder;
	}

	private Folder duplicateStructureAndDocumentsAndAddToTransaction(Folder folder, User currentUser,
																	 Transaction transaction,
																	 boolean forceTitleDuplication) {
		Folder duplicatedFolder = duplicate(folder, currentUser, forceTitleDuplication);
		transaction.add(duplicatedFolder);

		List<Folder> children = rm.wrapFolders(searchServices.search(new LogicalSearchQuery()
				.setCondition(from(rm.folder.schemaType()).where(rm.folder.parentFolder()).isEqualTo(folder))
				.filteredByStatus(StatusFilter.ACTIVES)));
		for (Folder child : children) {
			Folder duplicatedChild = duplicateStructureAndDocumentsAndAddToTransaction(child, currentUser, transaction,
					forceTitleDuplication);
			duplicatedChild.setTitle(child.getTitle());
			duplicatedChild.setParentFolder(duplicatedFolder);
		}

		List<Document> childrenDocuments = rm.wrapDocuments(searchServices.search(new LogicalSearchQuery()
				.setCondition(from(rm.document.schemaType()).where(rm.document.folder()).isEqualTo(folder)
						.andWhere(Schemas.LOGICALLY_DELETED_STATUS).isFalseOrNull())));
		for (Document child : childrenDocuments) {
			Document newDocument = createDuplicateOfDocument(child, currentUser);
			newDocument.setFolder(duplicatedFolder);
			transaction.add(newDocument);
		}
		return duplicatedFolder;
	}

	public Document createDuplicateOfDocument(Document duplicatedDocument, User currentUser) {
		Document newDocument = rm.newDocumentWithType(duplicatedDocument.getType());

		for (Metadata metadata : duplicatedDocument.getSchema().getMetadatas().onlyNonSystemReserved()
				.onlyManualsOrAutomaticWithEvaluator().onlyDuplicable()) {
			newDocument.set(metadata, duplicatedDocument.get(metadata));
		}
		LocalDateTime now = LocalDateTime.now();

		newDocument.setFormCreatedBy(currentUser);
		newDocument.setFormCreatedOn(now);
		newDocument.setCreatedBy(currentUser.getId()).setModifiedBy(currentUser.getId());
		newDocument.setCreatedOn(now).setModifiedOn(now);
		return newDocument;
	}

	public Folder duplicate(Folder folder, User currentUser, boolean forceTitleDuplication) {
		Folder newFolder = rm.newFolderWithType(folder.getType());
		MetadataSchema schema = newFolder.getSchema();

		for (Metadata metadata : schema.getMetadatas().onlyEnabled().onlyNonSystemReserved()
				.onlyManualsOrAutomaticWithEvaluator().onlyDuplicable()) {
			newFolder.getWrappedRecord().set(metadata, folder.getWrappedRecord().get(metadata));
		}

		if (folder.getSchema().getMetadata(Schemas.TITLE.getCode()).isDuplicable() || forceTitleDuplication) {
			newFolder.setTitle(folder.getTitle() + " (Copie)");
		}

		LocalDateTime localDateTime = TimeProvider.getLocalDateTime();
		newFolder.setFormCreatedBy(currentUser);
		newFolder.setFormCreatedOn(localDateTime);
		newFolder.setCreatedBy(currentUser.getId()).setModifiedBy(currentUser.getId());
		newFolder.setCreatedOn(localDateTime).setModifiedOn(localDateTime);

		return newFolder;
	}

	public List<RMUserFolder> getSubUserFolders(RMUserFolder userFolder) {
		List<RMUserFolder> subUserFolders = new ArrayList<>();
		MetadataSchema userFolderSchema = rm.userFolderSchema();
		Metadata parentUserFolderMetadata = userFolderSchema.getMetadata(UserFolder.PARENT_USER_FOLDER);

		LogicalSearchQuery subFoldersQuery = new LogicalSearchQuery();
		subFoldersQuery.setCondition(LogicalSearchQueryOperators.from(userFolderSchema).where(parentUserFolderMetadata)
				.isEqualTo(userFolder.getWrappedRecord()));
		for (Record subFolderRecord : searchServices.search(subFoldersQuery)) {
			RMUserFolder subUserFolder = rm.wrapUserFolder(subFolderRecord);
			subUserFolders.add(subUserFolder);
		}
		return subUserFolders;
	}

	public List<UserDocument> getUserDocuments(RMUserFolder userFolder) {
		List<UserDocument> userDocuments = new ArrayList<>();

		MetadataSchema userDocumentSchema = rm.userDocumentSchema();
		Metadata userFolderMetadata = userDocumentSchema.getMetadata(UserDocument.USER_FOLDER);

		LogicalSearchQuery userDocumentsQuery = new LogicalSearchQuery();
		userDocumentsQuery.setCondition(LogicalSearchQueryOperators.from(userDocumentSchema).where(userFolderMetadata)
				.isEqualTo(userFolder.getWrappedRecord()));
		for (Record userDocumentRecord : searchServices.search(userDocumentsQuery)) {
			UserDocument userDocument = rm.wrapUserDocument(userDocumentRecord);
			userDocuments.add(userDocument);
		}

		return userDocuments;
	}

	public void duplicateSubStructureAndSave(Folder folder, RMUserFolder userFolder, User currentUser)
			throws RecordServicesException, IOException {
		Transaction transaction = new Transaction();
		List<RMUserFolder> subUserFolders = getSubUserFolders(userFolder);
		for (RMUserFolder subUserFolder : subUserFolders) {
			duplicateStructureAndSave(subUserFolder, folder, currentUser, transaction);
		}
		List<UserDocument> userDocuments = getUserDocuments(userFolder);
		for (UserDocument userDocument : userDocuments) {
			Document document = rm.newDocument();
			populateDocumentFromUserDocument(document, userDocument, currentUser);
			document.setFolder(folder);
			transaction.add(document);
		}
		recordServices.execute(transaction);
	}

	private void duplicateStructureAndSave(RMUserFolder userFolder, Folder parentFolder, User currentUser,
										   Transaction transaction)
			throws IOException {
		Folder folder = rm.newFolder();
		populateFolderFromUserFolder(folder, userFolder, currentUser);
		folder.setParentFolder(parentFolder);
		transaction.add(folder);

		List<RMUserFolder> subUserFolders = getSubUserFolders(userFolder);
		for (RMUserFolder subUserFolder : subUserFolders) {
			// Recursive call
			duplicateStructureAndSave(subUserFolder, folder, currentUser, transaction);
		}
		List<UserDocument> userDocuments = getUserDocuments(userFolder);
		for (UserDocument userDocument : userDocuments) {
			Document document = rm.newDocument();
			populateDocumentFromUserDocument(document, userDocument, currentUser);
			document.setFolder(folder);
			transaction.add(document);
		}
	}

	public void populateFolderFromUserFolder(Folder folder, RMUserFolder userFolder, User currentUser) {
		folder.setTitle(userFolder.getTitle());
		LocalDate openDate;
		if (userFolder.getFormCreatedOn() != null) {
			openDate = new LocalDate(userFolder.getFormCreatedOn());
		} else {
			openDate = TimeProvider.getLocalDate();
		}
		folder.setOpenDate(openDate);
		folder.setFormCreatedBy(currentUser);
		folder.setFormCreatedOn(userFolder.getFormCreatedOn());
		folder.setFormModifiedBy(currentUser);
		folder.setFormModifiedOn(userFolder.getFormModifiedOn());
		if (userFolder.getParentFolder() != null) {
			folder.setParentFolder(userFolder.getParentFolder());
		} else {
			folder.setAdministrativeUnitEntered(userFolder.getAdministrativeUnit());
			folder.setCategoryEntered(userFolder.getCategory());
			folder.setRetentionRuleEntered(userFolder.getRetentionRule());
			folder.setCopyStatusEntered(CopyType.PRINCIPAL);
		}
	}

	public void populateDocumentFromUserDocument(Document document, UserDocument userDocument, User currentUser)
			throws IOException {
		ContentManager contentManager = modelLayerFactory.getContentManager();

		String filename = userDocument.getTitle();
		String contentInputStreamId = userDocument.getContent().getCurrentVersion().getHash();
		try (InputStream inputStream = contentManager
				.getContentInputStream(contentInputStreamId, "DecommissioningServices.populateDocumentFromUserDocument.in")) {
			ContentManager.ContentVersionDataSummaryResponse uploadResponse = contentManager
					.upload(inputStream, "DecommissioningServices.populateDocumentFromUserDocument.upload");
			ContentVersionDataSummary contentVersion = uploadResponse.getContentVersionDataSummary();
			Content content = contentManager.createMajor(currentUser, filename, contentVersion);
			document.setContent(content);
		}
		document.setTitle(filename);
		document.setFolder(userDocument.getFolder());
		document.setContent(userDocument.getContent());
		document.setFormCreatedBy(currentUser);
		document.setFormCreatedOn(userDocument.getFormCreatedOn());
		document.setFormModifiedBy(currentUser);
		document.setFormModifiedOn(userDocument.getFormModifiedOn());
	}

	public void deleteUserFolder(RMUserFolder userFolder, User currentUser) {
		List<RMUserFolder> subUserFolders = getSubUserFolders(userFolder);
		for (RMUserFolder subUserFolder : subUserFolders) {
			// Recursive call
			deleteUserFolder(subUserFolder, currentUser);
		}

		List<UserDocument> userDocuments = getUserDocuments(userFolder);
		for (UserDocument userDocument : userDocuments) {
			delete(userDocument.getWrappedRecord(), null, true, currentUser);
		}
		delete(userFolder.getWrappedRecord(), null, true, currentUser);
	}

	public void deleteUserDocument(UserDocument userDocument, User currentUser) {
		delete(userDocument.getWrappedRecord(), null, true, currentUser);
	}

	private void delete(Record record, String reason, boolean physically, User user) {
		boolean putFirstInTrash = putFirstInTrash(record);
		if (recordServices.validateLogicallyThenPhysicallyDeletable(record, user).isEmpty() || putFirstInTrash) {
			recordServices.logicallyDelete(record, user);
			modelLayerFactory.newLoggingServices().logDeleteRecordWithJustification(record, user, reason);
			if (physically && !putFirstInTrash) {
				recordServices.physicallyDelete(record, user);
			}
		}
	}

	private boolean putFirstInTrash(Record record) {
		ModelLayerExtensions ext = modelLayerFactory.getExtensions();
		if (ext == null) {
			return false;
		}
		ModelLayerCollectionExtensions extensions = ext.forCollection(record.getCollection());
		PutSchemaRecordsInTrashEvent event = new PutSchemaRecordsInTrashEvent(record.getSchemaCode(), null);
		return extensions.isPutInTrashBeforePhysicalDelete(event);
	}

	public YearType getYearType(ContainerRecord container) {
		LocalDate comparedDate = null;
		List<Record> records = getFoldersInContainer(container);
		YearType yearType = null;

		if (getRMConfigs().isPopulateBordereauxWithLesserDispositionDate()) {
			for (Record record : records) {
				LocalDate minDate = LangUtils.min(comparedDate, getMinimumLocalDateForRecord(record, false));
				if (!minDate.equals(comparedDate)) {
					yearType = getYearType(rm.wrapFolder(record), minDate);
				}
				comparedDate = minDate;
			}
		} else {
			for (Record record : records) {
				LocalDate maxDate = LangUtils.max(comparedDate, getMaximalLocalDateForRecord(record, false));
				if (!Objects.equals(maxDate, comparedDate)) {
					yearType = getYearType(rm.wrapFolder(record), maxDate);
				}
				comparedDate = maxDate;
			}
		}

		return yearType;
	}

	private YearType getYearType(Folder folder, LocalDate comparedDate) {
		if (comparedDate == null) {
			return null;
		} else if (comparedDate.equals(folder.getExpectedTransferDate()) || comparedDate.equals(folder.getActualTransferDate())) {
			return folder.getMainCopyRule().getSemiActiveYearTypeId() == null ? null : rm.getYearType(folder.getMainCopyRule().getSemiActiveYearTypeId());
		} else {
			return folder.getMainCopyRule().getInactiveYearTypeId() == null ? null : rm.getYearType(folder.getMainCopyRule().getInactiveYearTypeId());
		}
	}

	private List<Record> getFoldersInContainer(ContainerRecord container, Metadata... metadatas) {
		LogicalSearchQuery query = new LogicalSearchQuery(
				from(rm.folderSchemaType()).where(rm.folder.container()).isEqualTo(container))
				.setReturnedMetadatas(ReturnedMetadatasFilter.onlyMetadatas(metadatas));
		return searchServices.search(query);
	}

	private List<Record> getFoldersInContainer(ContainerRecord container) {
		LogicalSearchQuery query = new LogicalSearchQuery(
				from(rm.folderSchemaType()).where(rm.folder.container()).isEqualTo(container));
		return searchServices.search(query);
	}

	private LocalDate getMinimumLocalDateForRecord(Record record, boolean takeActualDates) {
		Folder folder = rm.wrapFolder(record);
		if (!takeActualDates) {
			List<LocalDate> dates = getListOfExpectedDates(folder);
			return getMinimal(dates);
		}
		List<LocalDate> dates = getListOfAllDates(folder, false);

		return getMinimal(dates);
	}

	private LocalDate getMaximalLocalDateForRecord(Record record, boolean takeActualDates) {
		Folder folder = rm.wrapFolder(record);
		if (!takeActualDates) {
			List<LocalDate> dates = getListOfExpectedDates(folder);
			return getMaximal(dates);
		}
		List<LocalDate> dates = getListOfAllDates(folder, true);
		return getMaximal(dates);
	}

	private List<LocalDate> getListOfExpectedDates(Folder folder) {
		List<LocalDate> dates = new ArrayList<LocalDate>();
		dates.add(folder.getExpectedDestructionDate());
		dates.add(folder.getExpectedDepositDate());
		return dates;
	}

	private List<LocalDate> getListOfAllDates(Folder folder, boolean includeTransfert) {
		List<LocalDate> dates = getListOfExpectedDates(folder);
		dates.add(folder.getActualDestructionDate());
		dates.add(folder.getActualDepositDate());

		if (includeTransfert) {
			dates.add(folder.getExpectedTransferDate());
			dates.add(folder.getActualTransferDate());
		}

		return dates;
	}

	private LocalDate getMaximal(List<LocalDate> dates) {
		LocalDate max = null;
		for (LocalDate date : dates) {
			max = LangUtils.max(max, date);
		}
		return max;
	}

	private LocalDate getMinimal(List<LocalDate> dates) {
		LocalDate min = null;
		for (LocalDate date : dates) {
			min = LangUtils.min(min, date);
		}
		return min;
	}

	private void getMaximalSemiActiveInterval(Map<String, String> maximalIntervals, Record record) {
		Folder folder = rm.wrapFolder(record);
		if (folder != null) {
			CopyRetentionRule firstCopyRetentionRule = folder.getMainCopyRule();
			if (firstCopyRetentionRule != null) {

				RetentionPeriod retentionPeriod = firstCopyRetentionRule.getSemiActiveRetentionPeriod();
				if (retentionPeriod != null) {
					String interval = org.apache.commons.lang.StringUtils.defaultString(retentionPeriod.toString());
					String intervalType = "fixed";
					if (retentionPeriod.is888()) {
						intervalType = "888";
					} else if (retentionPeriod.is999()) {
						intervalType = "999";
					}
					String maximalInterval = maximalIntervals.get(intervalType);
					if (maximalInterval == null || interval.compareToIgnoreCase(maximalInterval) > 1) {
						maximalIntervals.put(intervalType, interval);
					}
				}
			}
		}
	}

	private DecommissioningSecurityService securityService() {
		return new DecommissioningSecurityService(collection, appLayerFactory);
	}

	public String getDecommissionningLabel(ContainerRecord record) {
		DecommissioningType decommissioningType = getDecommissioningType(record);
		return decommissioningType != null ? decommissioningType.getLabel() : null;
	}

	public DecommissioningType getDecommissioningType(ContainerRecord container) {
		DecommissioningType decommissioningType = container.getDecommissioningType();
		if (decommissioningType == null) {
			List<Folder> folderRecords = getFoldersInContainers(container);
			for (Folder folder : folderRecords) {
				FolderStatus folderStatus = folder.getArchivisticStatus();
				if (FolderStatus.SEMI_ACTIVE == folderStatus) {
					decommissioningType = DecommissioningType.TRANSFERT_TO_SEMI_ACTIVE;
					break;
				} else if (FolderStatus.INACTIVE_DEPOSITED == folderStatus) {
					decommissioningType = DecommissioningType.DEPOSIT;
					break;
				} else if (FolderStatus.INACTIVE_DESTROYED == folderStatus) {
					decommissioningType = DecommissioningType.DESTRUCTION;
					break;
				}
			}
		}
		return decommissioningType;
	}

	public void reactivateRecordsFromTask(String taskId, LocalDate reactivationDate, User respondant, User applicant,
										  boolean isAccepted)
			throws RecordServicesException {

		Record taskRecord = recordServices.getDocumentById(taskId);
		RMTask task = rm.wrapRMTask(taskRecord);
		String schemaType = "";
		if (task.getLinkedFolders() != null) {
			schemaType = Folder.SCHEMA_TYPE;
			Transaction t = new Transaction();
			t.setOptions(RecordUpdateOptions.validationExceptionSafeOptions());
			for (String folderId : task.getLinkedFolders()) {
				Folder folder = rm.getFolder(folderId);
				if (isAccepted) {
					t.add(folder.addReactivation(applicant, LocalDate.now()).setReactivationDecommissioningDate(reactivationDate)
							.addPreviousDepositDate(folder.getActualDepositDate())
							.addPreviousTransferDate(folder.getActualTransferDate())
							.setActualDepositDate(null).setActualTransferDate(null));
				}
				loggingServices
						.completeReactivationRequestTask(recordServices.getDocumentById(folder.getId()), task.getId(), isAccepted,
								applicant, respondant, task.getReason(), reactivationDate.toString());
				alertUsers(RMEmailTemplateConstants.ALERT_REACTIVATED, schemaType, taskRecord, folder.getWrappedRecord(), null,
						null, reactivationDate, respondant, applicant, null, isAccepted);
			}
			recordServices.execute(t);
		}
		if (task.getLinkedContainers() != null) {
			schemaType = ContainerRecord.SCHEMA_TYPE;
			for (String containerId : task.getLinkedContainers()) {
				Transaction t = new Transaction();
				t.setOptions(RecordUpdateOptions.validationExceptionSafeOptions());
				ContainerRecord containerRecord = rm.getContainerRecord(containerId);
				List<Folder> folders = rm.searchFolders(
						LogicalSearchQueryOperators.from(rm.folder.schemaType()).where(rm.folder.container())
								.isEqualTo(containerRecord.getId()));
				if (folders != null) {
					for (Folder folder : folders) {
						if (isAccepted && isFolderReactivable(folder, applicant)) {
							t.add(folder.addReactivation(applicant, LocalDate.now())
									.setReactivationDecommissioningDate(reactivationDate)
									.addPreviousDepositDate(folder.getActualDepositDate())
									.addPreviousTransferDate(folder.getActualTransferDate())
									.setActualDepositDate(null).setActualTransferDate(null));
						}
					}
				}
				recordServices.execute(t);
				loggingServices
						.completeReactivationRequestTask(recordServices.getDocumentById(containerId), task.getId(), isAccepted,
								applicant, respondant, task.getReason(), reactivationDate.toString());
				alertUsers(RMEmailTemplateConstants.ALERT_REACTIVATED, schemaType, taskRecord, containerRecord.getWrappedRecord(),
						null, null, reactivationDate, respondant, applicant, null, isAccepted);
			}

		}
	}

	public boolean isFolderReactivable(Folder folder, User currentUser) {
		return folder != null && folder.getArchivisticStatus().isSemiActiveOrInactive()
			   && currentUser.has(RMPermissionsTo.REACTIVATION_REQUEST_ON_FOLDER).on(folder);
	}

	private void alertUsers(String template, String schemaType, Record task, Record record, LocalDate borrowingDate,
							LocalDate returnDate, LocalDate reactivationDate, User currentUser,
							User borrowerEntered, BorrowingType borrowingType, boolean isAccepted) {

		try {
			String displayURL = schemaType.equals(Folder.SCHEMA_TYPE) ?
								RMNavigationConfiguration.DISPLAY_FOLDER :
								RMNavigationConfiguration.DISPLAY_CONTAINER;
			String subject = "";
			List<String> parameters = new ArrayList<>();
			Transaction transaction = new Transaction();
			EmailToSend emailToSend = newEmailToSend();
			EmailAddress toAddress = new EmailAddress();
			subject = task.getTitle();

			if (template.equals(RMEmailTemplateConstants.ALERT_BORROWED)) {
				toAddress = new EmailAddress(borrowerEntered.getTitle(), borrowerEntered.getEmail());
				parameters.add("borrowingType" + EmailToSend.PARAMETER_SEPARATOR + borrowingType);
				parameters.add("borrowerEntered" + EmailToSend.PARAMETER_SEPARATOR + StringEscapeUtils.escapeHtml4(borrowerEntered.getFirstName() + " " + borrowerEntered.getLastName() +
																												   " (" + borrowerEntered.getUsername() + ")"));
				parameters.add("borrowingDate" + EmailToSend.PARAMETER_SEPARATOR + formatDateToParameter(borrowingDate));
				parameters.add("returnDate" + EmailToSend.PARAMETER_SEPARATOR + formatDateToParameter(returnDate));
			} else if (template.equals(RMEmailTemplateConstants.ALERT_REACTIVATED)) {
				toAddress = new EmailAddress(borrowerEntered.getTitle(), borrowerEntered.getEmail());
				parameters.add("reactivationDate" + EmailToSend.PARAMETER_SEPARATOR + formatDateToParameter(reactivationDate));
			} else if (template.equals(RMEmailTemplateConstants.ALERT_RETURNED)) {
				toAddress = new EmailAddress(borrowerEntered.getTitle(), borrowerEntered.getEmail());
				parameters.add("returnDate" + EmailToSend.PARAMETER_SEPARATOR + formatDateToParameter(returnDate));
			} else if (template.equals(RMEmailTemplateConstants.ALERT_BORROWING_EXTENTED)) {
				toAddress = new EmailAddress(borrowerEntered.getTitle(), borrowerEntered.getEmail());
				parameters.add("borrowerEntered" + EmailToSend.PARAMETER_SEPARATOR + borrowerEntered.getFirstName() + " " + borrowerEntered.getLastName() +
							   " (" + borrowerEntered.getUsername() + ")");
				parameters.add("extensionDate" + EmailToSend.PARAMETER_SEPARATOR + formatDateToParameter(LocalDate.now()));
				parameters.add("returnDate" + EmailToSend.PARAMETER_SEPARATOR + formatDateToParameter(returnDate));
			}

			LocalDateTime sendDate = TimeProvider.getLocalDateTime();
			emailToSend.setTo(toAddress);
			emailToSend.setSendOn(sendDate);
			emailToSend.setSubject(subject);
			String fullTemplate = isAccepted ?
								  template + RMEmailTemplateConstants.ACCEPTED :
								  template + RMEmailTemplateConstants.DENIED;
			emailToSend.setTemplate(fullTemplate);
			parameters.add("subject" + EmailToSend.PARAMETER_SEPARATOR + StringEscapeUtils.escapeHtml4(subject));
			String recordTitle = record.getTitle();
			boolean isAddingRecordIdInEmails = eimConfigs.isAddingRecordIdInEmails();
			if (isAddingRecordIdInEmails) {
				parameters.add("title" + EmailToSend.PARAMETER_SEPARATOR + StringEscapeUtils.escapeHtml4(recordTitle) + " (" + record.getId() + ")");
			} else {
				parameters.add("title" + EmailToSend.PARAMETER_SEPARATOR + StringEscapeUtils.escapeHtml4(recordTitle));
			}
			parameters.add("currentUser" + EmailToSend.PARAMETER_SEPARATOR + StringEscapeUtils.escapeHtml4(currentUser.getFirstName() + " " + currentUser.getLastName() +
																										   " (" + currentUser.getUsername() + ")"));
			String constellioUrl = eimConfigs.getConstellioUrl();
			parameters.add("constellioURL" + EmailToSend.PARAMETER_SEPARATOR + constellioUrl);
			parameters.add("recordURL" + EmailToSend.PARAMETER_SEPARATOR + constellioUrl + "#!" + displayURL + "/" + record
					.getId());
			Map<Language, String> labels = metadataSchemasManager.getSchemaTypes(collection).getSchemaType(schemaType).getLabels();
			for (Map.Entry<Language, String> label : labels.entrySet()) {
				parameters.add("recordType" + "_" + label.getKey().getCode() + EmailToSend.PARAMETER_SEPARATOR + StringEscapeUtils.escapeHtml4(label.getValue().toLowerCase()));
			}
			parameters.add("isAccepted" + EmailToSend.PARAMETER_SEPARATOR + $(String.valueOf(isAccepted)));
			emailToSend.setParameters(parameters);
			transaction.add(emailToSend);

			recordServices.execute(transaction);

		} catch (RecordServicesException e) {
			LOGGER.error("Cannot alert user", e);
		}

	}

	private String formatDateToParameter(LocalDate date) {
		if (date == null) {
			return "";
		}
		return date.toString("yyyy-MM-dd");
	}

	private EmailToSend newEmailToSend() {
		MetadataSchemaTypes types = metadataSchemasManager.getSchemaTypes(collection);
		MetadataSchema schema = types.getSchemaType(EmailToSend.SCHEMA_TYPE).getDefaultSchema();
		Record emailToSendRecord = recordServices.newRecordWithSchema(schema);
		return new EmailToSend(emailToSendRecord, types);
	}

	public String getContainerRecordExtremeDates(ContainerRecord container) {
		LocalDate minimum = null;
		LocalDate maximum = null;
		List<Record> records = getFoldersInContainer(container, rm.folder.expectedDepositDate(),
				rm.folder.expectedDestructionDate());

		for (Record record : records) {
			minimum = LangUtils.min(minimum, getMinimumLocalDateForRecord(record, false));
		}
		for (Record record : records) {
			maximum = LangUtils.max(maximum, getMaximalLocalDateForRecord(record, false));
		}

		return minimum == null ? "" : minimum.getYear() + "-" + maximum.getYear();

		//		List<Folder> folders = rm.searchFolders(
		//				new LogicalSearchQuery(from(rm.folder.schemaType()).where(rm.folder.container()).isEqualTo(con)));

		//		int beginingYear = 10000;
		//		int endingYear = 10000;
		//
		//		for (Folder folder : folders) {
		//
		//			if (folder.getOpenDate().getYear() < beginingYear) {
		//				beginingYear = folder.getOpenDate().getYear();
		//			}
		//			if (folder.getCloseDate().getYear() > endingYear) {
		//				endingYear = folder.getCloseDate().getYear();
		//			}
		//
		//		}
		//
		//		if (beginingYear == 10000) {
		//			return "";
		//		} else {
		//			return beginingYear + "-" + endingYear;
		//		}
	}

	public LocalDate getDispositionDate(Record record) {
		if (getRMConfigs().isPopulateBordereauxWithLesserDispositionDate()) {
			return getMinimumLocalDateForRecord(record, true);
		} else {
			return getMaximalLocalDateForRecord(record, true);
		}
	}
}


package com.constellio.app.modules.rm.reports.decommissioning;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.enums.DecommissioningType;
import com.constellio.app.modules.rm.model.enums.DisposalType;
import com.constellio.app.modules.rm.reports.model.decommissioning.DocumentReportModel;
import com.constellio.app.modules.rm.reports.model.decommissioning.DocumentReportModel.DocumentTransfertModel_Calendar;
import com.constellio.app.modules.rm.reports.model.decommissioning.DocumentReportModel.DocumentTransfertModel_Document;
import com.constellio.app.modules.rm.reports.model.decommissioning.DocumentReportModel.DocumentTransfertModel_Identification;
import com.constellio.app.modules.rm.reports.model.decommissioning.ReportBooleanField;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningService;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.modules.rm.wrappers.type.MediumType;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class ContainerRecordReportPresenter {

	private String EMPTY_FOR_NOW = "";

	private String collection;
	private RMSchemasRecordsServices rm;
	private SearchServices searchServices;
	private List<Folder> folders = new ArrayList<>();
	private int beginingYear = Integer.MAX_VALUE;
	private int endingYear = Integer.MIN_VALUE;
	private DecommissioningService decommissioningService;
	private RecordServices recordServices;
	private MetadataSchemasManager schemasManager;
	private IOServices ioServices;
	private DecommissioningType reportType;
	private MetadataSchemaTypes types;
	private RMConfigs rmConfigs;

	public ContainerRecordReportPresenter(String collection, AppLayerFactory appLayerFactory) {
		this.collection = collection;
		searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();
		rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		decommissioningService = new DecommissioningService(collection, appLayerFactory);
		recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
		schemasManager = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager();
		ioServices = appLayerFactory.getModelLayerFactory().getIOServicesFactory().newIOServices();
		types = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(collection);
		rmConfigs = new RMConfigs(appLayerFactory.getModelLayerFactory().getSystemConfigurationsManager());

	}

	public DocumentReportModel build(ContainerRecord container) {

		DocumentReportModel model = new DocumentReportModel();
		model.setPrintDate(TimeProvider.getLocalDate().toString());

		// Documents
		model.setDocumentList(buildDocuments(container));

		// Calendar
		model.setCalendarModel(buildCalendarModel(container));

		// Identification
		model.setIdentificationModel(buildIdentificationModel(container));

		return model;
	}

	public ContainerRecord getContainerRecord(String containerId) {
		Record record = recordServices.getDocumentById(containerId);
		MetadataSchemaTypes types = schemasManager.getSchemaTypes(collection);
		ContainerRecord containerRecord = new ContainerRecord(record, types);
		setReportType(containerRecord.getDecommissioningType());
		return containerRecord;
	}

	public DecommissioningType getReportType() {
		return reportType;
	}

	public IOServices getIoServices() {
		return ioServices;
	}

	private List<DocumentTransfertModel_Document> buildDocuments(ContainerRecord container) {
		List<DocumentTransfertModel_Document> documents = new ArrayList<>();

		if (container != null) {
			String containerId = container.getId();

			if (containerId != null && !containerId.isEmpty()) {
				folders = getFolders(containerId);

				for (Folder folder : folders) {
					DocumentTransfertModel_Document document = buildDocumentFrom(folder);

					updateBeginingYearIfDateFromFolderEarlier(folder);

					updateEndingYearIfDateFromFolderLater(folder);

					documents.add(document);
				}
			}
		}
		return documents;
	}

	private List<Folder> getFolders(String containerId) {
		MetadataSchemaType folderSchemaType = rm.folder.schemaType();

		Metadata containerMetadata = folderSchemaType.getDefaultSchema().getMetadata(Folder.CONTAINER);
		Metadata administrativeMetadata = folderSchemaType.getDefaultSchema().getMetadata(Folder.ADMINISTRATIVE_UNIT);
		Metadata categoryMetadata = folderSchemaType.getDefaultSchema().getMetadata(Folder.CATEGORY);
		Metadata titleMetadata = Schemas.TITLE;

		LogicalSearchQuery foldersQuery = new LogicalSearchQuery(LogicalSearchQueryOperators.from(folderSchemaType)
				.where(containerMetadata).isEqualTo(containerId).andWhere(Schemas.LOGICALLY_DELETED_STATUS).isFalseOrNull())
				.sortAsc(rm.folder.categoryCode()).sortAsc(administrativeMetadata).sortAsc(categoryMetadata).sortAsc(titleMetadata);

		List<Folder> folders = rm.wrapFolders(searchServices.search(foldersQuery));

		if (folders == null) {
			folders = new ArrayList<>();
		}

		return folders;

	}

	private DocumentTransfertModel_Document buildDocumentFrom(Folder folder) {
		DocumentTransfertModel_Document document = new DocumentTransfertModel_Document();

		String categoryCode = "";
		String adminUnitCode = "";
		String delayNumber = "";
		String startingYear = "";
		String endingYear = "";
		String referenceId = "";
		String title = "";

		if (folder != null) {
			categoryCode = StringUtils.defaultString(folder.getCategoryCode());
			adminUnitCode = StringUtils.defaultString(folder.getAdministrativeUnitCode());

			String ruleId = folder.getRetentionRule();

			if (ruleId != null && !ruleId.isEmpty()) {
				RetentionRule retentionRule = rm.getRetentionRule(ruleId);

				if (retentionRule != null) {
					delayNumber = StringUtils.defaultString(retentionRule.getCode());
				}
			}

			LocalDate startingDate = folder.getOpenDate();
			if (startingDate != null) {
				startingYear = StringUtils.defaultString(startingDate.toString("yyyy"));
			}

			LocalDate endingDate = folder.getCloseDate();
			if (endingDate != null) {
				endingYear = StringUtils.defaultString(endingDate.toString("yyyy"));
			}

			referenceId = StringUtils.defaultString(getIdWithoutLeadingZero(folder));
			title = StringUtils.defaultString(folder.getTitle());

		}

		document.setCode(categoryCode);
		document.setUnit(adminUnitCode);
		document.setDelayNumber(delayNumber);
		document.setStartingYear(startingYear);
		document.setEndingYear(endingYear);
		document.setReferenceId(referenceId);
		// TODO document.setRestrictionYear();
		document.setTitle(title);

		return document;
	}

	private void updateBeginingYearIfDateFromFolderEarlier(Folder folder) {
		LocalDate openDate = folder.getOpenDate();
		if (openDate != null && openDate.getYear() < beginingYear) {
			beginingYear = openDate.getYear();
		}
	}

	private void updateEndingYearIfDateFromFolderLater(Folder folder) {
		LocalDate closeDate = folder.getCloseDate();
		if (closeDate != null && closeDate.getYear() > endingYear) {
			endingYear = closeDate.getYear();
		}
	}

	private String getIdWithoutLeadingZero(Folder folder) {
		String idWithoutLeadingZero = "";
		if (folder != null) {
			idWithoutLeadingZero = folder.getId();
			idWithoutLeadingZero = StringUtils.removeStart(idWithoutLeadingZero, "0");
		}
		return idWithoutLeadingZero;
	}

	private DocumentTransfertModel_Calendar buildCalendarModel(ContainerRecord container) {
		DocumentTransfertModel_Calendar calendarModel = new DocumentTransfertModel_Calendar();

		String calendarNumber = "";
		List<ReportBooleanField> conservationDispositions = new ArrayList<>();
		String dispositionYear = "";
		String extremeDates = "";
		String quantity = "";
		String ruleNumber = "";
		String semiActiveRange = "";
		List<ReportBooleanField> supports = new ArrayList<>();

		if (container != null) {

			String uniformRuleId = decommissioningService.getUniformRuleOf(container);

			if (uniformRuleId != null && !uniformRuleId.isEmpty()) {
				RetentionRule retentionRule = rm.getRetentionRule(uniformRuleId);

				if (retentionRule != null) {
					ruleNumber = retentionRule.getCode();
				}
			}

			if (folders != null) {
				quantity = Integer.toString(folders.size());
			}

			LocalDate dispositionDate = decommissioningService.getDispositionDate(container);
			if (dispositionDate != null) {
				dispositionYear = Integer.toString(dispositionDate.getYear());
			}

			supports = getSupports(container);

			extremeDates = buildExtremeDatesWithUnverifiedAssumptionThatGetDocumentsWasCalledBefore();

			if (folders != null && !folders.isEmpty()) {
				Folder folder = folders.get(0);
				if (folder != null) {
					CopyRetentionRule firstCopyRetentionRule = folder.getMainCopyRule();
					if (firstCopyRetentionRule != null) {
						conservationDispositions = getConservationDispositionFields(firstCopyRetentionRule);
					}
				}
			}
			semiActiveRange = decommissioningService.getSemiActiveInterval(container);

			calendarNumber = rm.getCollection(collection).getConservationCalendarNumber();
		}

		calendarModel.setCalendarNumber(calendarNumber);
		calendarModel.setConservationDisposition(conservationDispositions);
		calendarModel.setDispositionYear(dispositionYear);
		calendarModel.setExtremeDate(extremeDates);
		calendarModel.setQuantity(quantity);
		calendarModel.setRuleNumber(ruleNumber);
		calendarModel.setSemiActiveRange(semiActiveRange);
		calendarModel.setSupports(supports);

		return calendarModel;
	}

	private List<ReportBooleanField> getSupports(ContainerRecord container) {
		List<ReportBooleanField> fields = new ArrayList<>();

		if (container != null) {
			List<String> mediumTypes = decommissioningService.getMediumTypesOf(container);

			if (mediumTypes != null) {
				for (String mediumTypeStr : mediumTypes) {
					Record record = recordServices.getDocumentById(mediumTypeStr);
					MediumType mediumType = new MediumType(record, types);
					ReportBooleanField field = new ReportBooleanField(mediumType.getCode(),
							mediumTypes.contains(mediumType.getId()));
					fields.add(field);
				}
			}
		}

		return fields;
	}

	private String buildExtremeDatesWithUnverifiedAssumptionThatGetDocumentsWasCalledBefore() {
		StringBuilder stringBuilder = new StringBuilder();
		if (beginingYear != Integer.MAX_VALUE) {
			stringBuilder.append(beginingYear);
		} else {
			stringBuilder.append("N/A");
		}

		stringBuilder.append("-");

		if (endingYear != Integer.MIN_VALUE) {
			stringBuilder.append(endingYear);
		} else {
			stringBuilder.append("N/A");
		}
		return stringBuilder.toString();
	}

	private List<ReportBooleanField> getConservationDispositionFields(CopyRetentionRule firstCopyRetentionRule) {
		List<ReportBooleanField> conservationDispositionFields = new ArrayList<>();

		if (firstCopyRetentionRule != null) {
			ReportBooleanField destructionField = new ReportBooleanField(DisposalType.DESTRUCTION.getCode(),
					firstCopyRetentionRule.getInactiveDisposalType() == DisposalType.DESTRUCTION);
			conservationDispositionFields.add(destructionField);

			ReportBooleanField sortField = new ReportBooleanField(DisposalType.SORT.getCode(),
					firstCopyRetentionRule.getInactiveDisposalType() == DisposalType.SORT);
			conservationDispositionFields.add(sortField);

			ReportBooleanField constructionField = new ReportBooleanField(DisposalType.DEPOSIT.getCode(),
					firstCopyRetentionRule.getInactiveDisposalType() == DisposalType.DEPOSIT);
			conservationDispositionFields.add(constructionField);
		}
		return conservationDispositionFields;
	}

	private DocumentTransfertModel_Identification buildIdentificationModel(ContainerRecord container) {
		DocumentTransfertModel_Identification identificationModel = new DocumentTransfertModel_Identification();


		String administrativeAddress = "";
		String boxNumber = "";
		String containerNumber = "";
		String organisationName = "";
		String publicOrganisationNumber = "";
		String sentDateTransfer = "";
		String sentDateDeposit = "";

		if (container != null) {

			List<String> administrativeUnits = container.getAdministrativeUnits();
			if (administrativeUnits != null && administrativeUnits.size() == 1) {
				AdministrativeUnit administrativeUnit = getAdministrativeUnitAddress(administrativeUnits.get(0));

				administrativeAddress = administrativeUnit.getCode() + " - " + administrativeUnit.getTitle()
										+ "\n" + StringUtils.defaultString(administrativeUnit.getAdress());
			} else if (administrativeUnits != null && !administrativeUnits.isEmpty()) {
				administrativeAddress = $("DocumentTransfertReport.multipleAdministrativeUnits");
			}

			containerNumber = container.getIdentifier();
			boxNumber = container.getTemporaryIdentifier();

			buildUserPart(container, identificationModel);

			Collection collection = rm.getCollection(this.collection);
			organisationName = rmConfigs.isPopulateBordereauxWithCollection() ? collection.getTitle() : "";
			publicOrganisationNumber = collection.getOrganizationNumber();

			LocalDate firstTransferReportDate = container.getFirstTransferReportDate();
			if (firstTransferReportDate != null) {
				sentDateTransfer = StringUtils.defaultString(firstTransferReportDate.toString());
			} else {
				sentDateTransfer = StringUtils.defaultString(LocalDate.now().toString());
			}

			LocalDate firstDepositReportDate = container.getFirstDepositReportDate();
			if (firstDepositReportDate != null) {
				sentDateDeposit = StringUtils.defaultString(firstDepositReportDate.toString());
			} else {
				sentDateDeposit = StringUtils.defaultString(LocalDate.now().toString());
			}
		}

		identificationModel.setAdministrationAddress(administrativeAddress);
		identificationModel.setBoxNumber(boxNumber);
		identificationModel.setContainerNumber(containerNumber);
		identificationModel.setOrganisationName(organisationName);
		identificationModel.setPublicOrganisationNumber(publicOrganisationNumber);
		identificationModel.setSentDateTransfer(sentDateTransfer);
		identificationModel.setSentDateDeposit(sentDateDeposit);

		return identificationModel;
	}

	private void buildUserPart(ContainerRecord container, DocumentTransfertModel_Identification identificationModel) {
		String title = "";
		String jobTitle = "";
		String phone = "";
		String email = "";

		String userdId = container.getDocumentResponsible();
		if (userdId != null && !userdId.isEmpty()) {

			User user = getUser(userdId);

			if (user != null) {
				title = StringUtils.defaultString(user.getTitle());
				jobTitle = StringUtils.defaultString(user.getJobTitle());
				phone = StringUtils.defaultString(user.getPhone());
				email = StringUtils.defaultString(user.getEmail());
			}
		}

		identificationModel.setResponsible(title);
		identificationModel.setFunction(jobTitle);
		identificationModel.setPhoneNumber(phone);
		identificationModel.setEmail(email);
	}

	private AdministrativeUnit getAdministrativeUnitAddress(String administrativeUnitId) {
		AdministrativeUnit administrativeUnit = null;
		if (administrativeUnitId != null && !administrativeUnitId.isEmpty()) {
			administrativeUnit = rm.getAdministrativeUnit(administrativeUnitId);
		}

		return administrativeUnit;
	}

	private User getUser(String userId) {
		User user = rm.getUser(userId);
		return user;
	}

	public void setReportType(DecommissioningType reportType) {
		this.reportType = reportType;
	}
}
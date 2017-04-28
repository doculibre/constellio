package com.constellio.app.modules.rm.extensions;

import static com.constellio.model.services.search.query.logical.LogicalSearchQuery.query;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.where;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.api.extensions.SystemCheckExtension;
import com.constellio.app.api.extensions.params.CollectionSystemCheckParams;
import com.constellio.app.api.extensions.params.TryRepairAutomaticValueParams;
import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.PrintableLabel;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.dao.services.contents.ContentDao;
import com.constellio.data.dao.services.contents.ContentDaoException.ContentDaoException_NoSuchContent;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.model.conf.FoldersLocator;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ContentVersionDataSummary;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.RecordServicesRuntimeException.NoSuchRecordWithId;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;

public class RMSystemCheckExtension extends SystemCheckExtension {

	private static final String DEPOSIT_DATE_BEFORE_TRANSFER_DATE = "depositDateBeforeTransferDate";
	private static final String DESTRUCTION_DATE_BEFORE_TRANSFER_DATE = "destructionDateBeforeTransferDate";
	private static Logger LOGGER = LoggerFactory.getLogger(RMSystemCheckExtension.class);

	String collection;

	AppLayerFactory appLayerFactory;
	ContentDao contentDao;
	ContentManager contentManager;
	SearchServices searchServices;
	RecordServices recordServices;
	IOServices ioServices;

	public final String METRIC_LOGICALLY_DELETED_ADM_UNITS = "rm.admUnits.logicallyDeleted";
	public final String METRIC_LOGICALLY_DELETED_CATEGORIES = "rm.categories.logicallyDeleted";

	public final String DELETED_ADM_UNITS = "rm.admUnit.deleted";
	public final String RESTORED_ADM_UNITS = "rm.admUnit.restored";
	public final String DELETED_CATEGORIES = "rm.category.deleted";
	public final String RESTORED_CATEGORIES = "rm.category.restored";

	RMSchemasRecordsServices rm;

	RMConfigs configs;

	public RMSystemCheckExtension(String collection, AppLayerFactory appLayerFactory) {
		this.collection = collection;
		this.appLayerFactory = appLayerFactory;
		this.recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
		this.searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();
		this.contentDao = appLayerFactory.getModelLayerFactory().getDataLayerFactory().getContentsDao();
		this.rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		this.ioServices = appLayerFactory.getModelLayerFactory().getIOServicesFactory().newIOServices();
		this.contentManager = appLayerFactory.getModelLayerFactory().getContentManager();
		this.configs = new RMConfigs(appLayerFactory.getModelLayerFactory().getSystemConfigurationsManager());
	}

	@Override
	public boolean tryRepairAutomaticValue(TryRepairAutomaticValueParams params) {
		if (params.isMetadata(DecommissioningList.SCHEMA_TYPE, DecommissioningList.FOLDERS)) {
			DecommissioningList list = rm.wrapDecommissioningList(params.getRecord());
			for (String folderToRemove : params.getValuesToRemove()) {
				list.removeFolderDetail(folderToRemove);
			}

			return true;
		}
		return false;
	}

	@Override
	public void checkCollection(CollectionSystemCheckParams params) {
		boolean markedForReindexing = false;
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		for (AdministrativeUnit unit : rm.searchAdministrativeUnits(where(Schemas.LOGICALLY_DELETED_STATUS).isTrue())) {
			String label = unit.getCode() + " - " + unit.getTitle();
			params.getResultsBuilder().incrementMetric(METRIC_LOGICALLY_DELETED_ADM_UNITS);
			params.getResultsBuilder().markLogicallyDeletedRecordAsError(unit);
			if (params.isRepair()) {
				params.getResultsBuilder().markAsRepaired(unit.getId());
				try {
					recordServices.refresh(unit);
					if (!unit.getWrappedRecord().isDisconnected()) {
						recordServices.add(unit.set(Schemas.LOGICALLY_DELETED_STATUS.getLocalCode(), false));

						if (recordServices.isLogicallyThenPhysicallyDeletable(unit.getWrappedRecord(), User.GOD)) {
							recordServices.logicallyDelete(unit.getWrappedRecord(), User.GOD);
							recordServices.physicallyDelete(unit.getWrappedRecord(), User.GOD);
							params.getResultsBuilder().addListItem(DELETED_ADM_UNITS, label);

						} else {
							params.getResultsBuilder().addListItem(RESTORED_ADM_UNITS, label);
						}
					} else {
						params.getResultsBuilder().addListItem(DELETED_ADM_UNITS, label);
					}
				} catch (RecordServicesException e) {
					throw new RuntimeException(e);
				} catch (NoSuchRecordWithId e) {
					//OK
				}
				markedForReindexing = true;
			}
		}

		List<Category> categories = rm.searchCategorys(where(Schemas.LOGICALLY_DELETED_STATUS).isTrue());

		for (Category category : categories) {
			String label = category.getCode() + " - " + category.getTitle();
			params.getResultsBuilder().incrementMetric(METRIC_LOGICALLY_DELETED_CATEGORIES);
			params.getResultsBuilder().markLogicallyDeletedRecordAsError(category);
			if (params.isRepair()) {
				params.getResultsBuilder().markAsRepaired(category.getId());
				try {
					recordServices.refresh(category);
					if (!category.getWrappedRecord().isDisconnected()) {
						recordServices.add(category.set(Schemas.LOGICALLY_DELETED_STATUS.getLocalCode(), false));
						if (recordServices.isLogicallyThenPhysicallyDeletable(category.getWrappedRecord(), User.GOD)) {
							recordServices.logicallyDelete(category.getWrappedRecord(), User.GOD);
							recordServices.physicallyDelete(category.getWrappedRecord(), User.GOD);
							params.getResultsBuilder().addListItem(DELETED_CATEGORIES, label);
						} else {
							params.getResultsBuilder().addListItem(RESTORED_CATEGORIES, label);
						}
					} else {
						params.getResultsBuilder().addListItem(DELETED_CATEGORIES, label);
					}

				} catch (RecordServicesException e) {
					throw new RuntimeException(e);
				} catch (NoSuchRecordWithId e) {
					//OK
				}
				markedForReindexing = true;
			}
		}

		if (configs.allowModificationOfArchivisticStatusAndExpectedDates().isAlwaysEnabledOrDuringImportOnly()) {
			Iterator<Record> foldersIterator = searchServices.recordsIterator(query(from(rm.folder.schemaType())
					.where(rm.folder.manualExpectedDepositDate()).isNotNull()
					.orWhere(rm.folder.manualExpectedDesctructionDate()).isNotNull()));

			while (foldersIterator.hasNext()) {
				Folder folder = rm.wrapFolder(foldersIterator.next());

				LocalDate manualExpectedDeposit = folder.getManualExpectedDepositDate();
				LocalDate manualExpectedDestruction = folder.getManualExpectedDestructionDate();
				LocalDate actualTransfer = folder.getActualTransferDate();
				LocalDate manualExpectedTransfer = folder.getExpectedTransferDate();

				boolean fixDeposit = false;
				boolean fixDestruction = false;

				Map<String, Object> errorParams = new HashMap<>();
				errorParams.put("idTitle", folder.getId() + "-" + folder.getTitle());

				if (manualExpectedDeposit != null && actualTransfer != null && manualExpectedDeposit.isBefore(actualTransfer)) {
					params.getResultsBuilder().addNewValidationError(RMSystemCheckExtension.class,
							DEPOSIT_DATE_BEFORE_TRANSFER_DATE, errorParams);
					fixDeposit = params.isRepair();
				}

				if (manualExpectedDeposit != null && manualExpectedTransfer != null
						&& manualExpectedDeposit.isBefore(manualExpectedTransfer)) {
					params.getResultsBuilder().addNewValidationError(RMSystemCheckExtension.class,
							DEPOSIT_DATE_BEFORE_TRANSFER_DATE, errorParams);
					fixDeposit = params.isRepair();
				}
				if (manualExpectedDestruction != null && actualTransfer != null
						&& manualExpectedDestruction.isBefore(actualTransfer)) {
					params.getResultsBuilder().addNewValidationError(RMSystemCheckExtension.class,
							DESTRUCTION_DATE_BEFORE_TRANSFER_DATE, errorParams);
					fixDestruction = params.isRepair();
				}

				if (manualExpectedDestruction != null && manualExpectedTransfer != null
						&& manualExpectedDestruction.isBefore(manualExpectedTransfer)) {
					params.getResultsBuilder().addNewValidationError(RMSystemCheckExtension.class,
							DESTRUCTION_DATE_BEFORE_TRANSFER_DATE, errorParams);
					fixDestruction = params.isRepair();
				}

				if (fixDeposit || fixDestruction) {

					if (fixDeposit) {
						folder.setManualExpectedDepositDate(null);
					}
					if (fixDestruction) {
						folder.setManualExpectedDestructionDate(null);
					}
					try {
						recordServices.update(folder);
					} catch (RecordServicesException e) {
						throw new RuntimeException(e);
					}
				}
			}
		}

		if (markedForReindexing) {
			appLayerFactory.getSystemGlobalConfigsManager().setReindexingRequired(true);
		}

		if (params.isRepair()) {
			for (PrintableLabel printableLabel : rm.searchPrintableLabels(LogicalSearchQueryOperators.ALL)) {
				Content content = printableLabel.getJasperfile();
				String hash = content.getCurrentVersion().getHash();

				try {
					contentDao.getContentInputStreamFactory(hash);
				} catch (ContentDaoException_NoSuchContent e) {

					switch (printableLabel.getTitle()) {
					case "Code de plan justifié de dossier à droite (Avery 5161)":
						installLabel(printableLabel, "Etiquettes Dossiers/5161/Avery_5161_D_Dossier.jasper");
						break;

					case "Code de plan justifié de dossier à droite (Avery 5163)":
						installLabel(printableLabel, "Etiquettes Dossiers/5163/Avery_5163_D_Dossier.jasper");
						break;

					case "Code de plan justifié de dossier à gauche (Avery 5163)":
						installLabel(printableLabel, "Etiquettes Dossiers/5163/Avery_5163_G_Dossier.jasper");
						break;

					case "Code de plan justifié de dossier à droite (Avery 5159)":
						installLabel(printableLabel, "Etiquettes Dossiers/5159/Avery_5159_D_Dossier.jasper");
						break;

					case "Code de plan justifié de dossier à droite (Avery 5162)":
						installLabel(printableLabel, "Etiquettes Dossiers/5162/Avery_5162_D_Dossier.jasper");
						break;

					case "Code de plan justifié de dossier à gauche (Avery 5162)":
						installLabel(printableLabel, "Etiquettes Dossiers/5162/Avery_5162_G_Dossier.jasper");
						break;

					case "Code de plan justifié de dossier à gauche (Avery 5161)":
						installLabel(printableLabel, "Etiquettes Dossiers/5161/Avery_5161_G_Dossier.jasper");
						break;

					case "Code de plan justifié de dossier à gauche (Avery 5159)":
						installLabel(printableLabel, "Etiquettes Dossiers/5159/Avery_5159_G_Dossier.jasper");
						break;

					case "Code de plan justifié de conteneur (Avery 5161)":
						installLabel(printableLabel, "Etiquettes Contenants/5161/Avery_5161_Container.jasper");
						break;

					case "Code de plan justifié de conteneur (Avery 5159)":
						installLabel(printableLabel, "Etiquettes Contenants/5159/Avery_5159_Container.jasper");
						break;

					case "Code de plan justifié de conteneur (Avery 5163)":
						installLabel(printableLabel, "Etiquettes Contenants/5163/Avery_5163_Container.jasper");
						break;

					case "Code de plan justifié de conteneur (Avery 5162)":
						installLabel(printableLabel, "Etiquettes Contenants/5162/Avery_5162_Container.jasper");
						break;

					}
				}
			}
		}
	}

	private void installLabel(PrintableLabel printableLabel, String subPath) {
		Content content = printableLabel.getJasperfile();
		File resources_i18n = new FoldersLocator().getI18nFolder();
		subPath = ("migrations/rm/combo/defaultJasperFiles/" + subPath).replace("/", File.separator);
		File jasperFile = new File(resources_i18n, subPath);

		InputStream inputStream = null;
		try {
			inputStream = ioServices.newFileInputStream(jasperFile, "installLabel");
			ContentVersionDataSummary summary = contentManager.upload(inputStream, false, false, "jasperFile.jasper");
			Content newContent = contentManager.createFileSystem(content.getCurrentVersion().getFilename(), summary);
			printableLabel.setJasperFile(newContent);
			try {
				recordServices.update(printableLabel);
			} catch (RecordServicesException e) {
				throw new RuntimeException(e);
			}
		} catch (FileNotFoundException e) {
			ioServices.closeQuietly(inputStream);
		}

	}
}

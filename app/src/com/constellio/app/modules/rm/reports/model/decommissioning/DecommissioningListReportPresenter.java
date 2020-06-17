package com.constellio.app.modules.rm.reports.model.decommissioning;

import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.modules.rm.extensions.api.DecommissioningListFolderTableExtension;
import com.constellio.app.modules.rm.extensions.api.RMModuleExtensions;
import com.constellio.app.modules.rm.reports.model.decommissioning.DecommissioningListReportModel.DecommissioningListReportModel_Folder;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.modules.rm.wrappers.structures.FolderDetailWithType;
import com.constellio.app.modules.rm.wrappers.type.MediumType;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.utils.MultipleFieldsComparator;
import com.constellio.data.conf.FoldersLocator;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class DecommissioningListReportPresenter {

	private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(DecommissioningListReportPresenter.class);

	private String collection;
	private AppLayerFactory appLayerFactory;
	private SearchServices searchServices;
	private String decommissioningListId;
	private RMSchemasRecordsServices rm;

	public DecommissioningListReportPresenter(String collection, AppLayerFactory appLayerFactory,
											  String decommissioningListId) {

		this.collection = collection;
		this.appLayerFactory = appLayerFactory;
		this.decommissioningListId = decommissioningListId;
		this.rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		this.searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();
	}

	public DecommissioningListReportModel build() {

		DecommissioningListReportModel model = new DecommissioningListReportModel();
		final DecommissioningListFolderTableExtension folderDetailTableExtension = getFolderDetailTableExtension();

		MetadataSchemaType folderSchemaType = rm.folder.schemaType();
		DecommissioningList decommissioningList = rm.getDecommissioningList(decommissioningListId);

		List<String> processedFolders = new ArrayList<>();
		if (decommissioningList.getFolders() != null) {
			processedFolders.addAll(decommissioningList.getFolders());
			for (FolderDetailWithType folder : decommissioningList.getFolderDetailsWithType()) {
				if (folder.isExcluded()) {
					processedFolders.remove(folder.getFolderId());
				}
			}
		}

		LogicalSearchQuery foldersQuery = new LogicalSearchQuery()
				.setCondition(LogicalSearchQueryOperators.from(folderSchemaType)
						.where(Schemas.IDENTIFIER)
						.isIn(processedFolders));

		if (folderDetailTableExtension != null) {
			model.setWithMediumType(true);
			model.setWithMainCopyRule(true);
			//            foldersQuery.sortAsc(folderSchemaType.getDefaultSchema().get(Folder.CATEGORY_CODE)).sortAsc(ancienNumeroMetadata).sortAsc(Schemas.IDENTIFIER);
		} else {
			foldersQuery.sortAsc(folderSchemaType.getDefaultSchema().get(Folder.CATEGORY_CODE)).sortAsc(Schemas.LEGACY_ID).sortAsc(Schemas.IDENTIFIER);
		}

		List<Folder> folders = rm.wrapFolders(searchServices.search(foldersQuery));
		if (folderDetailTableExtension != null) {
			Collections.sort(folders, new MultipleFieldsComparator(getCategoryComparator(), getPreviousIdComparator(), getIdComparator()));
		}

		List<DecommissioningListReportModel_Folder> foldersModel = new ArrayList<>();
		for (Folder folder : folders) {
			Category category = rm.getCategory(folder.getCategory());
			RetentionRule retentionRule = rm.getRetentionRule(folder.getRetentionRule());
			String categoryCodeTitle = category.getCode() + " - " + category.getTitle();
			String retentionRuleCodeTitle = retentionRule.getCode() + " - " + retentionRule.getTitle();
			String containerRecordId = decommissioningList.getFolderDetail(folder.getId()).getContainerRecordId();
			String containerRecordTitle = "";
			if (containerRecordId != null) {
				ContainerRecord containerRecord = rm.getContainerRecord(containerRecordId);
				containerRecordTitle = containerRecord.getTitle();
			}
			String legacyId = folder.getLegacyId();
			String id = folder.getId();
			id = id != null ? id.replaceFirst("^0+(?!$)", "") : id;
			DecommissioningListReportModel_Folder folderModel = new DecommissioningListReportModel_Folder(legacyId, id,
					folder.getTitle(), retentionRuleCodeTitle, categoryCodeTitle, containerRecordTitle);
			if (folderDetailTableExtension != null) {
				folderModel.setLegacyId(folderDetailTableExtension.getPreviousId(folder));
				List<String> mediumTypesIds = folder.getMediumTypes();
				if (!mediumTypesIds.isEmpty()) {
					List<MediumType> mediumTypes = rm.getMediumTypes(mediumTypesIds);
					StringBuilder stringBuilder = new StringBuilder();
					String prefix = "";
					for (MediumType mediumType : mediumTypes) {
						stringBuilder.append(prefix);
						prefix = ",";
						stringBuilder.append(mediumType.getTitle());
					}
					folderModel.setMediumTypes(stringBuilder.toString());
				}
				folderModel.setMainCopyRule(folder.getMainCopyRule().toString());
			}
			foldersModel.add(folderModel);
		}

		String decommissioningListType = $(
				"DecommissioningListType." + decommissioningList.getDecommissioningListType().getCode());
		AdministrativeUnit administrativeUnit = rm.getAdministrativeUnit(
				decommissioningList.getAdministrativeUnit());
		String administrativeUnitCode = administrativeUnit.getCode();
		String administrativeUnitTitle = administrativeUnit.getTitle();
		model.setDecommissioningListTitle(decommissioningList.getTitle());
		model.setDecommissioningListType(decommissioningListType);
		model.setDecommissioningListAdministrativeUnitCodeAndTitle(administrativeUnitCode + " -  " + administrativeUnitTitle);
		model.setFolders(foldersModel);
		return model;
	}

	public FoldersLocator getFoldersLocator() {
		return appLayerFactory.getModelLayerFactory().getFoldersLocator();
	}

	public DecommissioningListFolderTableExtension getFolderDetailTableExtension() {
		RMModuleExtensions rmModuleExtensions = appLayerFactory.getExtensions().forCollection(collection).forModule(ConstellioRMModule.ID);
		return rmModuleExtensions.getDecommissioningListFolderTableExtension();
	}

	public Comparator getCategoryComparator() {
		return new Comparator<Folder>() {
			@Override
			public int compare(Folder o1, Folder o2) {
				return o1.getCategoryCode().compareTo(o2.getCategoryCode());
			}
		};
	}

	public Comparator getPreviousIdComparator() {
		final DecommissioningListFolderTableExtension folderDetailTableExtension = getFolderDetailTableExtension();
		return new Comparator<Folder>() {
			@Override
			public int compare(Folder o1, Folder o2) {
				// Get the values to compare
				final String value1 = folderDetailTableExtension.getPreviousIdOrNull(o1);
				final String value2 = folderDetailTableExtension.getPreviousIdOrNull(o2);
				if (value1 == null) {
					if (value2 == null) {
						return 0;
					} else {
						return 1;
					}
				} else if (value2 == null) {
					return -1;
				}

				try {
					Integer int1 = Integer.parseInt(value1);
					Integer int2 = Integer.parseInt(value2);
					return int1.compareTo(int2);
				} catch (NumberFormatException e) {

				}
				return value1.compareTo(value2);
			}
		};
	}

	public Comparator getIdComparator() {
		return new Comparator<Folder>() {
			@Override
			public int compare(Folder o1, Folder o2) {
				// Get the values to compare
				final String value1 = o1.getId();
				final String value2 = o2.getId();
				if (value1 == null) {
					if (value2 == null) {
						return 0;
					} else {
						return 1;
					}
				} else if (value2 == null) {
					return -1;
				}

				try {
					Integer int1 = Integer.parseInt(value1);
					Integer int2 = Integer.parseInt(value2);
					return int1.compareTo(int2);
				} catch (NumberFormatException e) {

				}
				return value1.compareTo(value2);
			}
		};
	}
}
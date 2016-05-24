package com.constellio.app.modules.rm.extensions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.data.frameworks.extensions.ExtensionBooleanResult;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.extensions.behaviors.RecordExtension;
import com.constellio.model.extensions.events.records.RecordInCreationBeforeSaveEvent;
import com.constellio.model.extensions.events.records.RecordInCreationBeforeValidationAndAutomaticValuesCalculationEvent;
import com.constellio.model.extensions.events.records.RecordInModificationBeforeSaveEvent;
import com.constellio.model.extensions.events.records.RecordInModificationBeforeValidationAndAutomaticValuesCalculationEvent;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.ReturnedMetadatasFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.model.services.taxonomies.TaxonomiesSearchServices;

public class RMFolderExtension extends RecordExtension {
	private final RMSchemasRecordsServices rmSchema;
	final String collection;

	final ModelLayerFactory modelLayerFactory;
	final RecordServices recordServices;
	final SearchServices searchServices;
	final TaxonomiesSearchServices taxonomiesSearchServices;
	final TaxonomiesManager taxonomyManager;
	final RMSchemasRecordsServices rm;

	public RMFolderExtension(String collection, ModelLayerFactory modelLayerFactory) {
		this.collection = collection;
		this.modelLayerFactory = modelLayerFactory;
		rmSchema = new RMSchemasRecordsServices(collection, modelLayerFactory);
		recordServices = modelLayerFactory.newRecordServices();
		searchServices = modelLayerFactory.newSearchServices();
		taxonomiesSearchServices = modelLayerFactory.newTaxonomiesSearchService();
		taxonomyManager = modelLayerFactory.getTaxonomiesManager();
		this.rm = new RMSchemasRecordsServices(collection, modelLayerFactory);
	}

	@Override
	public void recordInCreationBeforeValidationAndAutomaticValuesCalculation(
			RecordInCreationBeforeValidationAndAutomaticValuesCalculationEvent event) {
		if (event.isSchemaType(Folder.SCHEMA_TYPE)) {
			folderInCreation(rmSchema.wrapFolder(event.getRecord()), event.getTransactionUser());
		}
	}

	@Override
	public void recordInCreationBeforeSave(RecordInCreationBeforeSaveEvent event) {
		if (event.isSchemaType(Folder.SCHEMA_TYPE)) {
			setFolderPermissionStatus(event.getRecord());
		}
	}

	@Override
	public void recordInModificationBeforeSave(RecordInModificationBeforeSaveEvent event) {
		if (event.isSchemaType(Folder.SCHEMA_TYPE) && event.hasModifiedMetadata(Folder.ARCHIVISTIC_STATUS)) {
			setFolderPermissionStatus(event.getRecord());
		}
	}

	@Override
	public void recordInModificationBeforeValidationAndAutomaticValuesCalculation(
			RecordInModificationBeforeValidationAndAutomaticValuesCalculationEvent event) {
		if (event.isSchemaType(Folder.SCHEMA_TYPE) && event.getRecord().get(rmSchema.folderParentFolder()) != null) {
			Folder folder = rmSchema.wrapFolder(event.getRecord());
			folder.setAdministrativeUnitEntered((String) null);
			folder.setCategoryEntered((String) null);
			folder.setRetentionRuleEntered((String) null);
			folder.setCopyStatusEntered(null);
		}
	}

	private void setFolderPermissionStatus(Record record) {
		Folder folder = rmSchema.wrapFolder(record);
		folder.setPermissionStatus(folder.getArchivisticStatus());
	}

	private void folderInCreation(Folder folder, User user) {
		Boolean openHolderActivated = modelLayerFactory.getSystemConfigurationsManager().getValue(RMConfigs.OPEN_HOLDER);
		if (openHolderActivated) {
			if (folder.getCreatedBy() != null) {
				User createdBy = rmSchema.getUser(folder.getCreatedBy());
				updateStatusCopyIfRequired(folder, createdBy);
			} else if (user != null) {
				updateStatusCopyIfRequired(folder, user);
			}
		}
	}

	private void updateStatusCopyIfRequired(Folder folder, User user) {
		String ruleId = folder.getRetentionRuleEntered();
		if (StringUtils.isNotBlank(ruleId)) {
			RetentionRule rule = rmSchema.getRetentionRule(ruleId);
			if (rule != null) {
				boolean hasPrincipalCopies = !rule.getPrincipalCopies().isEmpty();
				if (hasPrincipalCopies) {
					List<String> adminUnits = rule.getAdministrativeUnits();
					if (!adminUnits.isEmpty() && rule.isResponsibleAdministrativeUnits()) {
						setFolderStatusAsPrincipalIfUserInRuleAdministrativeUnitsOrSubUnits(folder, user, rule);
					}
				}
			}
		}
	}

	private void setFolderStatusAsPrincipalIfUserInRuleAdministrativeUnitsOrSubUnits(Folder folder, User user,
			RetentionRule rule) {
		List<String> creatorAdminUnits = getUserAdminUnits(user);
		boolean creatorInRuleAdminUnits = !CollectionUtils.intersection(creatorAdminUnits, getRuleHierarchyUnits(rule))
				.isEmpty();
		if (creatorInRuleAdminUnits) {
			folder.setCopyStatusEntered(CopyType.PRINCIPAL);
		}
	}

	private Set<String> getRuleHierarchyUnits(RetentionRule rule) {
		Set<String> returnSet = new HashSet<>();
		Taxonomy principalTaxonomy = modelLayerFactory.getTaxonomiesManager().getPrincipalTaxonomy(
				rule.getCollection());
		for (String unit : rule.getAdministrativeUnits()) {
			List<String> currentUnits = taxonomiesSearchServices
					.getAllConceptIdsHierarchyOf(principalTaxonomy, rmSchema.getAdministrativeUnit(unit).getWrappedRecord());
			returnSet.addAll(currentUnits);
		}
		return returnSet;
	}

	private List<String> getUserAdminUnits(User user) {
		List<String> returnList = new ArrayList<>();
		LogicalSearchCondition condition = LogicalSearchQueryOperators.from(this.rmSchema.administrativeUnitSchema()).returnAll();
		List<Record> results = this.searchServices.search(new LogicalSearchQuery(condition).filteredWithUserWrite(user)
				.setReturnedMetadatas(ReturnedMetadatasFilter.idVersionSchema()));
		for (Record record : results) {
			returnList.add(record.getId());
		}
		return returnList;
	}

	@Override
	public ExtensionBooleanResult isRecordModifiableBy(IsRecordModifiableByParams params) {
		User user = params.getUser();
		if (params.isSchemaType(Folder.SCHEMA_TYPE)) {
			Folder folder = rm.wrapFolder(params.getRecord());

			if (user.hasWriteAccess().on(folder)) {
				if (folder.getPermissionStatus().isInactive()) {
					if (folder.getBorrowed() != null && folder.getBorrowed()) {
						return ExtensionBooleanResult
								.trueIf((user.has(RMPermissionsTo.MODIFY_INACTIVE_BORROWED_FOLDER).on(folder) && user
										.has(RMPermissionsTo.MODIFY_INACTIVE_FOLDERS).on(folder)));
					}
					return ExtensionBooleanResult.trueIf(user.has(RMPermissionsTo.MODIFY_INACTIVE_FOLDERS).on(folder));
				}
				if (folder.getPermissionStatus().isSemiActive()) {
					if (folder.getBorrowed() != null && folder.getBorrowed()) {
						return ExtensionBooleanResult
								.trueIf(user.has(RMPermissionsTo.MODIFY_SEMIACTIVE_BORROWED_FOLDER).on(folder) && user
										.has(RMPermissionsTo.MODIFY_SEMIACTIVE_FOLDERS).on(folder));
					}
					return ExtensionBooleanResult.trueIf(user.has(RMPermissionsTo.MODIFY_SEMIACTIVE_FOLDERS).on(folder));
				}
				return ExtensionBooleanResult.TRUE;
			}

			return ExtensionBooleanResult.FALSE;
		}
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}
}

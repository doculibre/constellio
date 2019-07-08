package com.constellio.app.extensions;

import com.constellio.app.api.extensions.BatchProcessingExtension;
import com.constellio.app.api.extensions.BatchProcessingExtension.AddCustomLabelsParams;
import com.constellio.app.api.extensions.BatchProcessingExtension.IsMetadataDisplayedWhenModifiedParams;
import com.constellio.app.api.extensions.BatchProcessingExtension.IsMetadataModifiableParams;
import com.constellio.app.api.extensions.DocumentViewButtonExtension;
import com.constellio.app.api.extensions.DownloadContentVersionLinkExtension;
import com.constellio.app.api.extensions.FieldBindingExtention;
import com.constellio.app.api.extensions.GenericRecordPageExtension;
import com.constellio.app.api.extensions.LabelTemplateExtension;
import com.constellio.app.api.extensions.ListSchemaExtention;
import com.constellio.app.api.extensions.MetadataDisplayCustomValueExtention;
import com.constellio.app.api.extensions.MetadataFieldExtension;
import com.constellio.app.api.extensions.PageExtension;
import com.constellio.app.api.extensions.PagesComponentsExtension;
import com.constellio.app.api.extensions.RecordDisplayFactoryExtension;
import com.constellio.app.api.extensions.RecordExportExtension;
import com.constellio.app.api.extensions.RecordFieldFactoryExtension;
import com.constellio.app.api.extensions.RecordSecurityExtension;
import com.constellio.app.api.extensions.RecordTextInputDataProviderExtension;
import com.constellio.app.api.extensions.SIPExtension;
import com.constellio.app.api.extensions.SchemaTypesPageExtension;
import com.constellio.app.api.extensions.SearchCriterionExtension;
import com.constellio.app.api.extensions.SearchPageExtension;
import com.constellio.app.api.extensions.SelectionPanelExtension;
import com.constellio.app.api.extensions.SystemCheckExtension;
import com.constellio.app.api.extensions.TaxonomyPageExtension;
import com.constellio.app.api.extensions.XmlGeneratorExtension;
import com.constellio.app.api.extensions.params.AddComponentToSearchResultParams;
import com.constellio.app.api.extensions.params.AddFieldsInLabelXMLParams;
import com.constellio.app.api.extensions.params.AvailableActionsParam;
import com.constellio.app.api.extensions.params.CanConsultTaxonomyParams;
import com.constellio.app.api.extensions.params.CanManageTaxonomyParams;
import com.constellio.app.api.extensions.params.CollectionSystemCheckParams;
import com.constellio.app.api.extensions.params.ConvertStructureToMapParams;
import com.constellio.app.api.extensions.params.DecorateMainComponentAfterInitExtensionParams;
import com.constellio.app.api.extensions.params.DocumentViewButtonExtensionParam;
import com.constellio.app.api.extensions.params.ExportCollectionInfosSIPIsTaxonomySupportedParams;
import com.constellio.app.api.extensions.params.ExtraMetadataToGenerateOnReferenceParams;
import com.constellio.app.api.extensions.params.FieldBindingExtentionParam;
import com.constellio.app.api.extensions.params.FilterCapsuleParam;
import com.constellio.app.api.extensions.params.GetAvailableExtraMetadataAttributesParam;
import com.constellio.app.api.extensions.params.GetSearchResultSimpleTableWindowComponentParam;
import com.constellio.app.api.extensions.params.IsBuiltInMetadataAttributeModifiableParam;
import com.constellio.app.api.extensions.params.ListSchemaExtraCommandParams;
import com.constellio.app.api.extensions.params.ListSchemaExtraCommandReturnParams;
import com.constellio.app.api.extensions.params.MetadataDisplayCustomValueExtentionParams;
import com.constellio.app.api.extensions.params.MetadataFieldExtensionParams;
import com.constellio.app.api.extensions.params.OnWriteRecordParams;
import com.constellio.app.api.extensions.params.PagesComponentsExtensionParams;
import com.constellio.app.api.extensions.params.RecordFieldFactoryExtensionParams;
import com.constellio.app.api.extensions.params.RecordFieldFactoryPostBuildExtensionParams;
import com.constellio.app.api.extensions.params.RecordFieldsExtensionParams;
import com.constellio.app.api.extensions.params.RecordSecurityParam;
import com.constellio.app.api.extensions.params.RecordTextInputDataProviderSortMetadatasParam;
import com.constellio.app.api.extensions.params.SearchPageConditionParam;
import com.constellio.app.api.extensions.params.TryRepairAutomaticValueParams;
import com.constellio.app.api.extensions.params.UpdateComponentExtensionParams;
import com.constellio.app.api.extensions.params.ValidateRecordsCheckParams;
import com.constellio.app.api.extensions.taxonomies.FolderDeletionEvent;
import com.constellio.app.api.extensions.taxonomies.GetCustomResultDisplayParam;
import com.constellio.app.api.extensions.taxonomies.GetTaxonomyExtraFieldsParam;
import com.constellio.app.api.extensions.taxonomies.GetTaxonomyManagementClassifiedTypesParams;
import com.constellio.app.api.extensions.taxonomies.TaxonomyExtraField;
import com.constellio.app.api.extensions.taxonomies.TaxonomyManagementClassifiedType;
import com.constellio.app.api.extensions.taxonomies.UserSearchEvent;
import com.constellio.app.api.extensions.taxonomies.ValidateTaxonomyDeletableParams;
import com.constellio.app.extensions.api.SchemaRecordExtention;
import com.constellio.app.extensions.api.SchemaRecordExtention.SchemaRecordExtensionActionPossibleParams;
import com.constellio.app.extensions.api.cmis.CmisExtension;
import com.constellio.app.extensions.api.cmis.params.BuildAllowableActionsParams;
import com.constellio.app.extensions.api.cmis.params.BuildCmisObjectFromConstellioRecordParams;
import com.constellio.app.extensions.api.cmis.params.BuildConstellioRecordFromCmisObjectParams;
import com.constellio.app.extensions.api.cmis.params.CheckInParams;
import com.constellio.app.extensions.api.cmis.params.CheckOutParams;
import com.constellio.app.extensions.api.cmis.params.DeleteTreeParams;
import com.constellio.app.extensions.api.cmis.params.GetObjectParams;
import com.constellio.app.extensions.api.cmis.params.IsSchemaTypeSupportedParams;
import com.constellio.app.extensions.menu.MenuItemActionsExtension;
import com.constellio.app.extensions.records.RecordAppExtension;
import com.constellio.app.extensions.records.RecordNavigationExtension;
import com.constellio.app.extensions.records.params.BuildRecordVOParams;
import com.constellio.app.extensions.records.params.GetDynamicFieldMetadatasParams;
import com.constellio.app.extensions.records.params.GetIconPathParams;
import com.constellio.app.extensions.records.params.IsMetadataSpecialCaseToNotBeShownParams;
import com.constellio.app.extensions.records.params.IsMetadataVisibleInRecordFormParams;
import com.constellio.app.extensions.sequence.AvailableSequence;
import com.constellio.app.extensions.sequence.AvailableSequenceForRecordParams;
import com.constellio.app.extensions.sequence.CollectionSequenceExtension;
import com.constellio.app.extensions.treenode.TreeNodeExtension;
import com.constellio.app.modules.rm.extensions.params.RMSchemaTypesPageExtensionExclusionByPropertyParams;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.MetadataFieldFactory;
import com.constellio.app.ui.framework.components.RecordFieldFactory;
import com.constellio.app.ui.framework.components.SearchResultDisplay;
import com.constellio.app.ui.framework.components.display.ReferenceDisplay;
import com.constellio.app.ui.framework.components.fields.AdditionnalRecordField;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.app.ui.pages.search.criteria.Criterion;
import com.constellio.data.frameworks.extensions.ExtensionBooleanResult;
import com.constellio.data.frameworks.extensions.ExtensionUtils;
import com.constellio.data.frameworks.extensions.ExtensionUtils.BooleanCaller;
import com.constellio.data.frameworks.extensions.VaultBehaviorsList;
import com.constellio.data.utils.KeyListMap;
import com.constellio.data.utils.Provider;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Capsule;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.AllowedReferences;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataFilter;
import com.constellio.model.entities.schemas.MetadataFilterFactory;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.vaadin.server.Resource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static com.constellio.app.api.extensions.GenericRecordPageExtension.OTHERS_TAB;

public class AppLayerCollectionExtensions {

	//------------ Extension points -----------

	private Map<String, ModuleExtensions> moduleExtensionsMap = new HashMap<>();

	public VaultBehaviorsList<PageExtension> pageAccessExtensions = new VaultBehaviorsList<>();

	public VaultBehaviorsList<TaxonomyPageExtension> taxonomyAccessExtensions = new VaultBehaviorsList<>();

	public VaultBehaviorsList<GenericRecordPageExtension> schemaTypeAccessExtensions = new VaultBehaviorsList<>();

	public VaultBehaviorsList<SchemaTypesPageExtension> schemaTypesPageExtensions = new VaultBehaviorsList<>();

	public VaultBehaviorsList<SearchPageExtension> searchPageExtensions = new VaultBehaviorsList<>();

	public VaultBehaviorsList<RecordAppExtension> recordAppExtensions = new VaultBehaviorsList<>();

	public VaultBehaviorsList<TreeNodeExtension> treeNodeAppExtension = new VaultBehaviorsList<>();

	public VaultBehaviorsList<RecordNavigationExtension> recordNavigationExtensions = new VaultBehaviorsList<>();

	public VaultBehaviorsList<BatchProcessingExtension> batchProcessingExtensions = new VaultBehaviorsList<>();

	public VaultBehaviorsList<CmisExtension> cmisExtensions = new VaultBehaviorsList<>();

	public List<DownloadContentVersionLinkExtension> downloadContentVersionLinkExtensions = new ArrayList<>();

	public VaultBehaviorsList<PagesComponentsExtension> pagesComponentsExtensions = new VaultBehaviorsList<>();

	public VaultBehaviorsList<SearchCriterionExtension> searchCriterionExtensions = new VaultBehaviorsList<>();

	public VaultBehaviorsList<SelectionPanelExtension> selectionPanelExtensions = new VaultBehaviorsList<>();

	public VaultBehaviorsList<RecordFieldFactoryExtension> recordFieldFactoryExtensions = new VaultBehaviorsList<>();

	public VaultBehaviorsList<RecordDisplayFactoryExtension> recordDisplayFactoryExtensions = new VaultBehaviorsList<>();

	public VaultBehaviorsList<CollectionSequenceExtension> collectionSequenceExtensions = new VaultBehaviorsList<>();

	public VaultBehaviorsList<SystemCheckExtension> systemCheckExtensions = new VaultBehaviorsList<>();

	public VaultBehaviorsList<RecordExportExtension> recordExportExtensions = new VaultBehaviorsList<>();

	public VaultBehaviorsList<LabelTemplateExtension> labelTemplateExtensions = new VaultBehaviorsList<>();

	public VaultBehaviorsList<DocumentViewButtonExtension> documentViewButtonExtension = new VaultBehaviorsList<>();

	public VaultBehaviorsList<ListSchemaExtention> listSchemaCommandExtensions = new VaultBehaviorsList<>();

	public VaultBehaviorsList<MetadataFieldExtension> metadataFieldExtensions = new VaultBehaviorsList<>();

	public VaultBehaviorsList<MetadataDisplayCustomValueExtention> metadataDisplayCustomValueExtentions = new VaultBehaviorsList<>();

	public VaultBehaviorsList<FieldBindingExtention> fieldBindingExtentions = new VaultBehaviorsList<>();

	public VaultBehaviorsList<SIPExtension> sipExtensions = new VaultBehaviorsList<>();

	public VaultBehaviorsList<MetadataFieldExtension> workflowExecutionFieldExtensions = new VaultBehaviorsList<>();

	public VaultBehaviorsList<RecordTextInputDataProviderExtension> recordTextInputDataProviderExtensions = new VaultBehaviorsList<>();

	public VaultBehaviorsList<RecordSecurityExtension> recordSecurityExtensions = new VaultBehaviorsList<>();

	public VaultBehaviorsList<MenuItemActionsExtension> menuItemActionsExtensions = new VaultBehaviorsList<>();

	public VaultBehaviorsList<XmlGeneratorExtension> xmlGeneratorExtensions = new VaultBehaviorsList<>();

	public VaultBehaviorsList<SchemaRecordExtention> schemaRecordExtentions = new VaultBehaviorsList<>();

	//Key : schema type code
	//Values : record's code
	public KeyListMap<String, String> lockedRecords = new KeyListMap<>();

	public <T extends ModuleExtensions> T forModule(String moduleId) {
		return (T) moduleExtensionsMap.get(moduleId);
	}

	public void registerModuleExtensionsPoint(String moduleId, ModuleExtensions extensions) {
		moduleExtensionsMap.put(moduleId, extensions);
	}

	//----------------- Callers ---------------

	public List<AvailableSequence> getAvailableSequencesForRecord(Record record) {

		AvailableSequenceForRecordParams params = new AvailableSequenceForRecordParams(record);
		List<AvailableSequence> availableSequences = new ArrayList<>();

		for (CollectionSequenceExtension extension : collectionSequenceExtensions) {
			List<AvailableSequence> extensionSequences = extension.getAvailableSequencesForRecord(params);
			if (extensionSequences != null) {
				availableSequences.addAll(extensionSequences);
			}
		}

		return availableSequences;
	}

	public void onWriteRecord(OnWriteRecordParams params) {
		for (RecordExportExtension recordExportExtension : recordExportExtensions) {
			recordExportExtension.onWriteRecord(params);
		}
	}

	public void getExtraMetadataToGenerateOnAReference(
			ExtraMetadataToGenerateOnReferenceParams extraMetadataToGenerateOnReferenceParams) {
		for (XmlGeneratorExtension xmlGeneratorExtension1 : xmlGeneratorExtensions) {
			xmlGeneratorExtension1.getExtraMetadataToGenerateOnReference(extraMetadataToGenerateOnReferenceParams);
		}
	}

	public Map<String, Object> convertStructureToMap(ConvertStructureToMapParams params) {
		for (RecordExportExtension recordExportExtension : recordExportExtensions) {
			Map<String, Object> converted = recordExportExtension.convertStructureToMap(params);
			if (converted != null) {
				return converted;
			}
		}
		return null;
	}

	public void buildRecordVO(BuildRecordVOParams params) {
		for (RecordAppExtension recordAppExtension : recordAppExtensions) {
			recordAppExtension.buildRecordVO(params);
		}
	}

	public String getIconForRecord(GetIconPathParams params) {
		for (RecordAppExtension recordAppExtension : recordAppExtensions) {
			String icon = recordAppExtension.getIconPathForRecord(params);
			if (icon != null) {
				return icon;
			}
		}
		return null;
	}

	public String getIconForRecordVO(GetIconPathParams params) {
		for (RecordAppExtension recordAppExtension : recordAppExtensions) {
			String icon = recordAppExtension.getIconPathForRecordVO(params);
			if (icon != null) {
				return icon;
			}
		}
		return null;
	}

	public String getExtensionForRecordVO(GetIconPathParams params) {
		for (RecordAppExtension recordAppExtension : recordAppExtensions) {
			String icon = recordAppExtension.getExtensionForRecordVO(params);
			if (icon != null) {
				return icon;
			}
		}
		return null;
	}

	public void buildCMISObjectFromConstellioRecord(BuildCmisObjectFromConstellioRecordParams params) {
		for (CmisExtension extension : cmisExtensions) {
			extension.buildCMISObjectFromConstellioRecord(params);
		}
	}

	public void buildConstellioRecordFromCmisObject(BuildConstellioRecordFromCmisObjectParams params) {
		for (CmisExtension extension : cmisExtensions) {
			extension.buildConstellioRecordFromCmisObject(params);
		}
	}

	public void buildAllowableActions(BuildAllowableActionsParams params) {
		for (CmisExtension extension : cmisExtensions) {
			extension.buildAllowableActions(params);
		}
	}

	public void onGetObject(GetObjectParams params) {
		for (CmisExtension extension : cmisExtensions) {
			extension.onGetObject(params);
		}
	}

	public boolean isSchemaTypeSupported(final IsSchemaTypeSupportedParams params, boolean defaultValue) {
		return ExtensionUtils.getBooleanValue(cmisExtensions, defaultValue, new BooleanCaller<CmisExtension>() {
			@Override
			public ExtensionBooleanResult call(CmisExtension extension) {
				return extension.isSchemaTypeSupported(params);
			}
		});
	}
	//
	//	public void onCreateCMISFolder(CreateFolderParams params) {
	//		for (CmisExtension extension : cmisExtensions) {
	//			extension.onCreateCMISFolder(params);
	//		}
	//	}
	//
	//	public void onCreateCMISDocument(CreateDocumentParams params) {
	//		for (CmisExtension extension : cmisExtensions) {
	//			extension.onCreateCMISDocument(params);
	//		}
	//	}
	//
	//	public void onUpdateCMISFolder(UpdateFolderParams params) {
	//		for (CmisExtension extension : cmisExtensions) {
	//			extension.onUpdateCMISFolder(params);
	//		}
	//	}
	//
	//	public void onUpdateCMISDocument(UpdateDocumentParams params) {
	//		for (CmisExtension extension : cmisExtensions) {
	//			extension.onUpdateCMISDocument(params);
	//		}
	//	}

	public void onCheckIn(CheckInParams params) {
		for (CmisExtension extension : cmisExtensions) {
			extension.onCheckIn(params);
		}
	}

	public void onCheckOut(CheckOutParams params) {
		for (CmisExtension extension : cmisExtensions) {
			extension.onCheckOut(params);
		}
	}

	public void onDeleteTree(DeleteTreeParams params) {
		for (CmisExtension extension : cmisExtensions) {
			extension.onDeleteTree(params);
		}
	}

	//	public void onDeleteContent(DeleteContentParams params) {
	//		for (CmisExtension extension : cmisExtensions) {
	//			extension.onDeleteContent(params);
	//		}
	//	}

	public boolean isRecordAvalibleToAllUsers(RecordSecurityParam recordSecurityParam) {
		for (RecordSecurityExtension recordSecurityExtension : recordSecurityExtensions) {
			if (recordSecurityExtension.isRecordAvalibleToAllUsers(recordSecurityParam)) {
				return true;
			}
		}

		return false;
	}

	public SearchResultDisplay getCustomResultDisplayFor(GetCustomResultDisplayParam params) {
		for (SearchPageExtension extension : searchPageExtensions) {
			SearchResultDisplay result = extension.getCustomResultDisplayFor(params);
			if (result != null) {
				return result;
			}
		}
		return null;
	}

	public Component getSimpleTableWindowComponent(GetSearchResultSimpleTableWindowComponentParam params) {
		for (SearchPageExtension extension : searchPageExtensions) {
			Component result = extension.getSimpleTableWindowComponent(params);
			if (result != null) {
				return result;
			}
		}
		return null;
	}

	public List<TaxonomyManagementClassifiedType> getClassifiedTypes(
			GetTaxonomyManagementClassifiedTypesParams params) {
		List<TaxonomyManagementClassifiedType> types = new ArrayList<>();
		for (TaxonomyPageExtension extension : taxonomyAccessExtensions) {
			types.addAll(extension.getClassifiedTypesFor(params));
		}
		return types;
	}

	public List<TaxonomyExtraField> getTaxonomyExtraFields(GetTaxonomyExtraFieldsParam params) {
		List<TaxonomyExtraField> fields = new ArrayList<>();
		for (TaxonomyPageExtension extension : taxonomyAccessExtensions) {
			fields.addAll(extension.getTaxonomyExtraFieldsFor(params));
		}
		return fields;
	}

	public String getSortMetadataCode(Taxonomy taxonomy) {
		String sortMetadataCode = null;
		for (TaxonomyPageExtension extension : taxonomyAccessExtensions) {
			sortMetadataCode = extension.getSortMetadataCode(taxonomy);
		}
		return sortMetadataCode;
	}

	public Metadata[] getRecordTextInputDataProviderSortMetadatas(RecordTextInputDataProviderSortMetadatasParam param) {
		Metadata[] sortMetadatas = null;
		for (RecordTextInputDataProviderExtension extension : recordTextInputDataProviderExtensions) {
			sortMetadatas = extension.getSearchSortMetadatas(param);
		}
		return sortMetadatas;
	}

	public boolean hasPageAccess(boolean defaultValue, final Class<? extends BasePresenter> presenterClass,
								 final String params,
								 final User user) {
		return pageAccessExtensions.getBooleanValue(defaultValue, new BooleanCaller<PageExtension>() {
			@Override
			public ExtensionBooleanResult call(PageExtension behavior) {
				return behavior.hasPageAccess(presenterClass, params, user);
			}
		});
	}

	public boolean hasRestrictedRecordAccess(boolean defaultValue, final Class<? extends BasePresenter> presenterClass,
											 final String params, final User user, final Record restrictedRecord) {
		return pageAccessExtensions.getBooleanValue(defaultValue, new BooleanCaller<PageExtension>() {
			@Override
			public ExtensionBooleanResult call(PageExtension behavior) {
				return behavior.hasRestrictedRecordAccess(presenterClass, params, user, restrictedRecord);
			}
		});
	}

	public boolean canManageSchema(boolean defaultValue, final User user, final MetadataSchemaType schemaType) {
		return schemaTypeAccessExtensions.getBooleanValue(defaultValue, new BooleanCaller<GenericRecordPageExtension>() {
			@Override
			public ExtensionBooleanResult call(GenericRecordPageExtension behavior) {
				return behavior.canManageSchema(user, schemaType);
			}
		});
	}

	public boolean canViewSchemaRecord(boolean defaultValue, final User user, final MetadataSchemaType schemaType,
									   final Record restrictedRecord) {
		return schemaTypeAccessExtensions.getBooleanValue(defaultValue, new BooleanCaller<GenericRecordPageExtension>() {
			@Override
			public ExtensionBooleanResult call(GenericRecordPageExtension behavior) {
				return behavior.canViewSchemaRecord(user, schemaType, restrictedRecord);
			}
		});
	}

	public boolean canModifySchemaRecord(boolean defaultValue, final User user, final MetadataSchemaType schemaType,
										 final Record restrictedRecord) {
		return schemaTypeAccessExtensions.getBooleanValue(defaultValue, new BooleanCaller<GenericRecordPageExtension>() {
			@Override
			public ExtensionBooleanResult call(GenericRecordPageExtension behavior) {
				return behavior.canModifySchemaRecord(user, schemaType, restrictedRecord);
			}
		});
	}

	public boolean canLogicallyDeleteSchemaRecord(boolean defaultValue, final User user,
												  final MetadataSchemaType schemaType,
												  final Record restrictedRecord) {
		return schemaTypeAccessExtensions.getBooleanValue(defaultValue, new BooleanCaller<GenericRecordPageExtension>() {
			@Override
			public ExtensionBooleanResult call(GenericRecordPageExtension behavior) {
				return behavior.canLogicallyDeleteSchemaRecord(user, schemaType, restrictedRecord);
			}
		});
	}

	public boolean isSchemaTypeConfigurable(boolean defaultValue, final MetadataSchemaType schemaType) {
		return schemaTypeAccessExtensions.getBooleanValue(defaultValue, new BooleanCaller<GenericRecordPageExtension>() {
			@Override
			public ExtensionBooleanResult call(GenericRecordPageExtension behavior) {
				return behavior.isSchemaTypeConfigurable(schemaType);
			}
		});
	}

	public String getSchemaTypeDisplayGroup(final MetadataSchemaType schemaType) {
		String schemaTypeDisplayGroup = null;
		for (GenericRecordPageExtension extension : schemaTypeAccessExtensions) {
			schemaTypeDisplayGroup = extension.getSchemaTypeDisplayGroup(schemaType);
			if (schemaTypeDisplayGroup != null) {
				break;
			}
		}
		return schemaTypeDisplayGroup != null ? schemaTypeDisplayGroup : OTHERS_TAB;
	}

	public boolean canManageTaxonomy(final CanManageTaxonomyParams canManageTaxonomyParams) {
		return taxonomyAccessExtensions.getBooleanValue(canManageTaxonomyParams.isDefaultValue(), new BooleanCaller<TaxonomyPageExtension>() {
			@Override
			public ExtensionBooleanResult call(TaxonomyPageExtension behavior) {
				return behavior.canManageTaxonomy(canManageTaxonomyParams);
			}
		});
	}

	public boolean canConsultTaxonomy(final CanConsultTaxonomyParams canConsultTaxonomyParams) {
		return taxonomyAccessExtensions.getBooleanValue(canConsultTaxonomyParams.getDefaultValue(), new BooleanCaller<TaxonomyPageExtension>() {
			@Override
			public ExtensionBooleanResult call(TaxonomyPageExtension behavior) {
				return behavior.canConsultTaxonomy(canConsultTaxonomyParams);
			}
		});
	}

	public boolean displayTaxonomy(boolean defaultValue, final User user, final Taxonomy taxonomy) {
		return taxonomyAccessExtensions.getBooleanValue(defaultValue, new BooleanCaller<TaxonomyPageExtension>() {
			@Override
			public ExtensionBooleanResult call(TaxonomyPageExtension behavior) {
				return behavior.displayTaxonomy(user, taxonomy);
			}
		});
	}

	public ValidationErrors validateTaxonomyDeletable(Taxonomy taxonomy) {
		ValidationErrors validationErrors = new ValidationErrors();
		for (TaxonomyPageExtension taxonomyPageExtension : taxonomyAccessExtensions.getExtensions()) {
			taxonomyPageExtension.validateTaxonomyDeletable(new ValidateTaxonomyDeletableParams(taxonomy, validationErrors));
		}
		return validationErrors;
	}

	public void decorateView(PagesComponentsExtensionParams params) {
		for (PagesComponentsExtension extension : pagesComponentsExtensions) {
			extension.decorateView(params);
		}
	}

	public void decorateMainComponentBeforeViewInstanciated(DecorateMainComponentAfterInitExtensionParams params) {
		for (PagesComponentsExtension extension : pagesComponentsExtensions) {
			extension.decorateMainComponentBeforeViewInstanciated(params);
		}
	}

	public void decorateMainComponentAfterViewAssembledOnViewEntered(
			DecorateMainComponentAfterInitExtensionParams params) {
		for (PagesComponentsExtension extension : pagesComponentsExtensions) {
			extension.decorateMainComponentAfterViewAssembledOnViewEntered(params);
		}
	}

	public void decorateMainComponentBeforeViewAssembledOnViewEntered(
			DecorateMainComponentAfterInitExtensionParams params) {
		for (PagesComponentsExtension extension : pagesComponentsExtensions) {
			extension.decorateMainComponentBeforeViewAssembledOnViewEntered(params);
		}
	}

	public boolean isMetadataDisplayedWhenModifiedInBatchProcessing(final Metadata metadata) {
		return batchProcessingExtensions.getBooleanValue(true, new BooleanCaller<BatchProcessingExtension>() {
			@Override
			public ExtensionBooleanResult call(BatchProcessingExtension behavior) {
				return behavior.isMetadataDisplayedWhenModified(new IsMetadataDisplayedWhenModifiedParams(metadata));
			}
		});
	}

	public boolean isMetadataModifiableInBatchProcessing(final Metadata metadata, final User user,
														 final String recordId) {
		return batchProcessingExtensions.getBooleanValue(true, new BooleanCaller<BatchProcessingExtension>() {
			@Override
			public ExtensionBooleanResult call(BatchProcessingExtension behavior) {
				return behavior.isMetadataModifiable(new IsMetadataModifiableParams(metadata, user, recordId));
			}
		});
	}

	public Map<String, String> getCustomLabels(final MetadataSchema schema, final Locale locale,
											   final Provider<String, String> resourceProvider) {
		Map<String, String> customLabels = new HashMap<>();
		for (BatchProcessingExtension extension : batchProcessingExtensions) {
			extension.addCustomLabel(new AddCustomLabelsParams(schema, locale, resourceProvider, customLabels));
		}
		return customLabels;
	}

	public RecordFieldFactory newRecordFieldFactory(RecordFieldFactoryExtensionParams params) {
		RecordFieldFactory recordFieldFactory = null;
		for (RecordFieldFactoryExtension extension : recordFieldFactoryExtensions) {
			recordFieldFactory = extension.newRecordFieldFactory(params);
			if (recordFieldFactory != null) {
				break;
			}
		}
		if(recordFieldFactory == null) {
			return new RecordFieldFactoryExtension().newRecordFieldFactory(params);
		}
		return recordFieldFactory;
	}

	public void notifyNewUserSearch(UserSearchEvent event) {
		for (SearchPageExtension extension : searchPageExtensions) {
			extension.notifyNewUserSearch(event);
		}
	}

	public Capsule filter(FilterCapsuleParam param) {
		Capsule result = null;
		for (SearchPageExtension extension : searchPageExtensions) {
			result = extension.filter(param);
		}
		return result;
	}

	public void notifyFolderDeletion(FolderDeletionEvent event) {
		for (RecordAppExtension extension : recordAppExtensions) {
			extension.notifyFolderDeleted(event);
		}
	}

	public void checkCollection(CollectionSystemCheckParams params) {
		for (SystemCheckExtension extension : systemCheckExtensions) {
			extension.checkCollection(params);
		}
	}

	public boolean validateRecord(ValidateRecordsCheckParams params) {
		boolean repaired = false;
		for (SystemCheckExtension extension : systemCheckExtensions) {
			repaired |= extension.validateRecord(params);
		}
		return repaired;
	}

	public boolean tryRepairAutomaticValue(TryRepairAutomaticValueParams params) {
		boolean repaired = false;
		for (SystemCheckExtension extension : systemCheckExtensions) {
			repaired |= extension.tryRepairAutomaticValue(params);
		}
		return repaired;
	}

	public void addAvailableActions(AvailableActionsParam param) {
		for (SelectionPanelExtension extension : selectionPanelExtensions) {
			extension.addAvailableActions(param);
		}
	}

	public void updateComponent(UpdateComponentExtensionParams params) {
		for (PagesComponentsExtension extension : pagesComponentsExtensions) {
			extension.updateComponent(params);
		}
	}

	public List<String> getAvailableExtraMetadataAttributes(GetAvailableExtraMetadataAttributesParam param) {
		List<String> values = new ArrayList<>();
		for (SchemaTypesPageExtension extensions : schemaTypesPageExtensions) {
			List<String> extensionValues = extensions.getAvailableExtraMetadataAttributes(param);
			if (extensionValues != null) {
				for (String extensionValue : extensionValues) {
					values.add(extensionValue);
				}
			}
		}
		return values;
	}

	public boolean isMetadataEnabledInRecordForm(final RecordVO recordVO, final MetadataVO metadataVO) {
		return ExtensionUtils.getBooleanValue(recordAppExtensions, true, new BooleanCaller<RecordAppExtension>() {
			@Override
			public ExtensionBooleanResult call(RecordAppExtension extension) {
				return extension.isMetadataVisibleInRecordForm(new IsMetadataVisibleInRecordFormParams() {
					@Override
					public MetadataVO getMetadataVO() {
						return metadataVO;
					}

					@Override
					public RecordVO getRecordVO() {
						return recordVO;
					}
				});
			}
		});
	}

	public boolean isMetadataSpecialCaseToNotBeShown(
			final IsMetadataSpecialCaseToNotBeShownParams isMetadataSpecialCaseToNotBeShownParams) {
		return ExtensionUtils.getBooleanValue(recordAppExtensions, false, new BooleanCaller<RecordAppExtension>() {
			@Override
			public ExtensionBooleanResult call(RecordAppExtension extension) {
				return extension.isMetadataSpecialCaseToNotBeShown(isMetadataSpecialCaseToNotBeShownParams);
			}
		});
	}


	public List<String> getDynamicFieldMetadatas(GetDynamicFieldMetadatasParams params) {
		List<String> dynamicFieldMetadatas = new ArrayList<>();
		for (RecordAppExtension recordAppExtension : recordAppExtensions) {
			List<String> extensionDynamicFieldMetadatas = recordAppExtension.getDynamicFieldMetadatas(params);
			if (extensionDynamicFieldMetadatas != null) {
				dynamicFieldMetadatas.addAll(extensionDynamicFieldMetadatas);
			}
		}
		return dynamicFieldMetadatas;
	}

	public void addFieldsInLabelXML(AddFieldsInLabelXMLParams params) {
		for (LabelTemplateExtension extension : labelTemplateExtensions) {
			extension.addFieldsInLabelXML(params);
		}
	}

	public File changeDownloadableTemplate() {
		File file = null;
		for (LabelTemplateExtension extension : labelTemplateExtensions) {
			File temp = extension.changeDownloadableTemplate();
			if (temp != null) {
				file = temp;
			}
		}
		return file;
	}

	public boolean isMetadataRequiredStatusModifiable(final IsBuiltInMetadataAttributeModifiableParam params) {
		return ExtensionUtils.getBooleanValue(schemaTypesPageExtensions, false, new BooleanCaller<SchemaTypesPageExtension>() {
			@Override
			public ExtensionBooleanResult call(SchemaTypesPageExtension extension) {
				return extension.isBuiltInMetadataAttributeModifiable(params);
			}
		});
	}

	public Component getComponentForCriterion(Criterion criterion) {
		for (SearchCriterionExtension extension : searchCriterionExtensions) {
			Component component = extension.getComponentForCriterion(criterion);
			if (component != null) {
				return component;
			}
		}
		return null;
	}

	public Component getDisplayForReference(AllowedReferences allowedReferences, String id) {
		for (RecordDisplayFactoryExtension extension : recordDisplayFactoryExtensions) {
			Component component = extension.getDisplayForReference(allowedReferences, id);
			if (component != null) {
				return component;
			}
		}
		return getDefaultDisplayForReference(id);
	}

	public Field<?> getMetadataField(MetadataFieldExtensionParams metadataFieldExtensionParams) {
		for (MetadataFieldExtension extension : metadataFieldExtensions) {
			Field<?> component = extension.getMetadataField(metadataFieldExtensionParams);
			if (component != null) {
				return component;
			}
		}
		return new MetadataFieldFactory().build(metadataFieldExtensionParams.getMetadataVO());
	}

	public Object getMetadataDisplayCustomValueExtention(
			MetadataDisplayCustomValueExtentionParams metadataDisplayCustomValueExtentionParams) {
		Object valueFound = null;
		for (MetadataDisplayCustomValueExtention metadataDisplayCustomValueExtention : metadataDisplayCustomValueExtentions) {
			valueFound = metadataDisplayCustomValueExtention.getCustomDisplayValue(metadataDisplayCustomValueExtentionParams);
			if (valueFound != null) {
				break;
			}
		}

		return valueFound;
	}

	public Field<?> getFieldForMetadata() {
		for (MetadataFieldExtension extension : workflowExecutionFieldExtensions) {
			Field<?> component = extension.getMetadataField(null);
				return component;
		}
		return null;
	}

	public List<Button> getDocumentViewButtonExtension(Record record, User user) {
		List<Button> buttons = new ArrayList<>();
		for (DocumentViewButtonExtension extension : documentViewButtonExtension) {
			buttons.addAll(extension.addButton(new DocumentViewButtonExtensionParam(record, user)));
		}
		return buttons;
	}

	public List<ListSchemaExtraCommandReturnParams> getListSchemaExtraCommandExtensions(
			ListSchemaExtraCommandParams listSchemaExtraCommandParams) {
		List<ListSchemaExtraCommandReturnParams> listSchemaParams = new ArrayList<>();
		for (ListSchemaExtention listSchemaCommandExtention : listSchemaCommandExtensions) {
			listSchemaParams.addAll(listSchemaCommandExtention.getExtraCommands(listSchemaExtraCommandParams));
		}

		return listSchemaParams;
	}

	public Component getDefaultDisplayForReference(String id) {
		return new ReferenceDisplay(id);
	}

	public List<String> getUnwantedTaxonomiesForExportation() {
		Set<String> unwantedTaxonomies = new HashSet<>();
		for (RecordExportExtension extension : recordExportExtensions) {
			List<String> unwantedTaxonomiesFromExtension = extension.getUnwantedTaxonomiesForExportation();
			if (unwantedTaxonomiesFromExtension != null) {
				unwantedTaxonomies.addAll(unwantedTaxonomiesFromExtension);
			}
		}
        return new ArrayList<>(unwantedTaxonomies);
    }public LogicalSearchCondition adjustSearchPageCondition(SearchPageConditionParam param) {
		LogicalSearchCondition condition = param.getCondition();
		for (SearchPageExtension extension : searchPageExtensions) {
			condition = extension.adjustSearchPageCondition(
					new SearchPageConditionParam(param.getMainComponent(), condition, param.getUser()));
		}
		return condition;
	}

	public Resource getIconFromContent(GetIconPathParams params) {
		for (RecordAppExtension extension : recordAppExtensions) {
			Resource calculatedResource = extension.getIconFromContent(params);
			if (calculatedResource != null) {
				return calculatedResource;
			}
		}
		return null;
	}

	public List<MetadataFilter> getMetadataAccessExclusionFilters() {
		List<MetadataFilter> metadataFilter = new ArrayList<>();
		metadataFilter.add(MetadataFilterFactory.excludeMetadataWithLocalCode(Schemas.TITLE_CODE));
		for (SchemaTypesPageExtension schemaTypesPageExtension : schemaTypesPageExtensions) {
			metadataFilter.addAll(schemaTypesPageExtension.getMetadataAccessExclusionFilters());
		}

		return metadataFilter;
	}

	public boolean isMetadataAccessExclusionByPropertyFilter(
			RMSchemaTypesPageExtensionExclusionByPropertyParams rmSchemaTypesPageExtensionExclusionByPropertyParams) {
		for (SchemaTypesPageExtension schemaTypesPageExtension : schemaTypesPageExtensions) {
			if (schemaTypesPageExtension.getMetadataAccessExclusionPropertyFilter(rmSchemaTypesPageExtensionExclusionByPropertyParams)) {
				return true;
			}
		}

		return false;
	}

	public List<AdditionnalRecordField> getAdditionnalFields(RecordFieldsExtensionParams params) {
		List<AdditionnalRecordField> additionnalFields = new ArrayList<>();
		for (PagesComponentsExtension extension : pagesComponentsExtensions) {
			additionnalFields.addAll(extension.getAdditionnalFields(params));
		}
		return additionnalFields;
    }

	public void fieldBindingExtentions(FieldBindingExtentionParam fieldBindingExtentionParam) {
		for (FieldBindingExtention fieldBindingExtention : fieldBindingExtentions) {
			fieldBindingExtention.baseFormFieldBinding(fieldBindingExtentionParam);
		}
	}

	public boolean isExportedTaxonomyInSIPCollectionInfos(final Taxonomy taxonomy) {
		return sipExtensions.getBooleanValue(true, new BooleanCaller<SIPExtension>() {
			@Override
			public ExtensionBooleanResult call(SIPExtension extension) {
				return extension.isExportedTaxonomyInSIPCollectionInfos(new ExportCollectionInfosSIPIsTaxonomySupportedParams(taxonomy));
			}
		});
	}
	public void postRecordFieldFactoryBuild(RecordFieldFactoryPostBuildExtensionParams params) {
		for (RecordFieldFactoryExtension extension : recordFieldFactoryExtensions) {
			extension.postBuild(params);
		}
	}

	public void orderListOfElements(Record[] recordElements) {
		for (LabelTemplateExtension extension : labelTemplateExtensions) {
			extension.orderListOfElements(recordElements);
		}
	}

	public <T extends Component> List<T> addComponentToSearchResult(
			AddComponentToSearchResultParams param) {
		List<T> result = new ArrayList<>();
		for (SearchPageExtension extension : searchPageExtensions) {
			List<T> paramResult = (List<T>) extension.addComponentToSearchResult(param);
			if (paramResult != null) {
				for (T component : paramResult) {
					result.add(component);
				}
			}
		}
		return result;
	}

	public boolean isEditActionPossibleOnSchemaRecord(final Record record, final User user) {
		return schemaRecordExtentions.getBooleanValue(true,
				(behavior) -> behavior.isEditActionPossible(
						new SchemaRecordExtensionActionPossibleParams(record, user)));
	}

	public boolean isDeleteActionPossibleOnSchemaRecord(final Record record, final User user) {
		return schemaRecordExtentions.getBooleanValue(true,
				(behavior) -> behavior.isDeleteActionPossible(
						new SchemaRecordExtensionActionPossibleParams(record, user)));
	}
}

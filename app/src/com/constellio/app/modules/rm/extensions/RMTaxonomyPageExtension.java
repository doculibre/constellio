package com.constellio.app.modules.rm.extensions;

import com.constellio.app.api.extensions.TaxonomyPageExtension;
import com.constellio.app.api.extensions.params.CanConsultTaxonomyParams;
import com.constellio.app.api.extensions.params.CanManageTaxonomyParams;
import com.constellio.app.api.extensions.taxonomies.GetTaxonomyExtraFieldsParam;
import com.constellio.app.api.extensions.taxonomies.GetTaxonomyManagementClassifiedTypesParams;
import com.constellio.app.api.extensions.taxonomies.TaxonomyExtraField;
import com.constellio.app.api.extensions.taxonomies.TaxonomyManagementClassifiedType;
import com.constellio.app.api.extensions.taxonomies.ValidateTaxonomyDeletableParams;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.constants.RMTaxonomies;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningService;
import com.constellio.app.modules.rm.ui.builders.FolderToVOBuilder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.entities.MetadataSchemaTypeVO;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.MetadataSchemaTypeToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.components.MetadataDisplayFactory;
import com.constellio.app.ui.framework.components.display.ReferenceDisplay;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.base.SessionContextProvider;
import com.constellio.data.frameworks.extensions.ExtensionBooleanResult;
import com.constellio.data.utils.Factory;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordUtils;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.vaadin.ui.Component;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class RMTaxonomyPageExtension extends TaxonomyPageExtension {

	private static final String CONTAINERS_TAXONOMY_CODE = "containers";
	private String collection;

	public RMTaxonomyPageExtension(String collection) {
		this.collection = collection;
	}

	@Override
	public ExtensionBooleanResult canManageTaxonomy(CanManageTaxonomyParams canManageTaxonomyParams) {
		User user = canManageTaxonomyParams.getUser();

		if (canManageTaxonomyParams.getTaxonomy().getCode().equals(RMTaxonomies.ADMINISTRATIVE_UNITS)) {
			return ExtensionBooleanResult.forceTrueIf(user.has(CorePermissions.MANAGE_SECURITY).globally());

		} else if (canManageTaxonomyParams.getTaxonomy().getCode().equals(RMTaxonomies.CLASSIFICATION_PLAN)) {
			return ExtensionBooleanResult.forceTrueIf(
					user.has(RMPermissionsTo.MANAGE_CLASSIFICATION_PLAN).globally());
		} else if (canManageTaxonomyParams.getTaxonomy().getCode().equals(RMTaxonomies.STORAGES)) {
			return ExtensionBooleanResult.forceTrueIf(
					user.has(RMPermissionsTo.MANAGE_STORAGE_SPACES).globally());
		} else {
			return ExtensionBooleanResult.NOT_APPLICABLE;
		}
	}

	public ExtensionBooleanResult canConsultTaxonomy(CanConsultTaxonomyParams canConsultTaxonomyParams) {
		if(canConsultTaxonomyParams.getTaxonomy().getCode().equals(RMTaxonomies.CLASSIFICATION_PLAN)) {
			return ExtensionBooleanResult.forceTrueIf(canConsultTaxonomyParams.getUser().has(RMPermissionsTo.CONSULT_CLASSIFICATION_PLAN).globally());
		} else {
			return ExtensionBooleanResult.NOT_APPLICABLE;
		}
	}

	@Override
	public List<TaxonomyManagementClassifiedType> getClassifiedTypesFor(
			GetTaxonomyManagementClassifiedTypesParams params) {
		List<TaxonomyManagementClassifiedType> types = new ArrayList<>();
		SessionContextProvider sessionContextProvider = params.getSessionContextProvider();
		String conceptId = params.getRecord().getId();
		if (params.isTaxonomy(RMTaxonomies.ADMINISTRATIVE_UNITS)) {
			types.add(getClassifiedFolderInAdministrativeUnits(conceptId, sessionContextProvider));
			types.add(getClassifiedRetentionRuleWithAdministrativeUnit(conceptId, sessionContextProvider));

		} else if (params.isTaxonomy(RMTaxonomies.CLASSIFICATION_PLAN)) {
			types.add(getClassifiedFolderInCategory(conceptId, sessionContextProvider));

		}

		return types;
	}

	@Override
	public List<TaxonomyExtraField> getTaxonomyExtraFieldsFor(GetTaxonomyExtraFieldsParam params) {
		List<TaxonomyExtraField> fields = new ArrayList<>();

		//		if (params.isTaxonomy(RMTaxonomies.ADMINISTRATIVE_UNITS)) {
		//			final List<String> retentionRules = getRetentionRules(params.getRecord().getId(), params.getSessionContextProvider());
		//			fields.add(new TaxonomyExtraField() {
		//				@Override
		//				public String getCode() {
		//					return "retentionRules";
		//				}
		//
		//				@Override
		//				public String getTitle() {
		//					return $("TaxonomyManagementView.retentionRules");
		//				}
		//
		//				@Override
		//				public Component buildComponent() {
		//					return buildDisplayList(retentionRules);
		//				}
		//			});
		//		}

		return fields;
	}

	@Override
	public ExtensionBooleanResult displayTaxonomy(User user, Taxonomy taxonomy) {
		if (taxonomy.getCode().equals(RMTaxonomies.ADMINISTRATIVE_UNITS) || taxonomy.getCode()
				.equals(RMTaxonomies.CLASSIFICATION_PLAN)) {
			return ExtensionBooleanResult.FALSE;
		} else {
			return ExtensionBooleanResult.NOT_APPLICABLE;
		}
	}

	@Override
	public void validateTaxonomyDeletable(ValidateTaxonomyDeletableParams validateTaxonomyDeletableParams) {
		Taxonomy taxonomy = validateTaxonomyDeletableParams.getTaxonomy();
		if (taxonomy.getCode().equals(CONTAINERS_TAXONOMY_CODE)) {
			validateTaxonomyDeletableParams.getValidationErrors().add(RMTaxonomyPageExtension.class, "cannotDeleteContainersTaxonomy");
		}
	}

	public List<String> getRetentionRules(String conceptId, SessionContextProvider sessionContextProvider) {
		AppLayerFactory appLayerFactory = sessionContextProvider.getConstellioFactories().getAppLayerFactory();
		DecommissioningService decommissioningService = new DecommissioningService(collection, appLayerFactory);
		return new RecordUtils().toWrappedRecordIdsList(decommissioningService.getRetentionRulesForAdministrativeUnit(conceptId));
	}

	private Component buildDisplayList(List<String> list) {
		Component retentionRulesDisplayComponent;
		MetadataDisplayFactory metadataDisplayFactory = new MetadataDisplayFactory();
		List<Component> elementDisplayComponents = new ArrayList<Component>();
		for (String elementDisplayValue : list) {
			Component elementDisplayComponent = new ReferenceDisplay(elementDisplayValue);
			elementDisplayComponent.setSizeFull();
			elementDisplayComponents.add(elementDisplayComponent);
		}
		if (!elementDisplayComponents.isEmpty()) {
			retentionRulesDisplayComponent = metadataDisplayFactory.newCollectionValueDisplayComponent(elementDisplayComponents);
		} else {
			retentionRulesDisplayComponent = null;
		}
		return retentionRulesDisplayComponent;
	}

	private TaxonomyManagementClassifiedType getClassifiedFolderInAdministrativeUnits(final String conceptId,
																					  final SessionContextProvider sessionContextProvider) {
		return new TaxonomyManagementClassifiedType() {
			@Override
			public MetadataSchemaTypeVO getSchemaType() {
				return getFolderTypeVO(sessionContextProvider);
			}

			@Override
			public RecordVODataProvider getDataProvider() {
				Factory<LogicalSearchQuery> searchQueryFactory = newFolderInAdministrativeUnitSearchQuery(
						conceptId, sessionContextProvider);
				return newFolderDataProvider(searchQueryFactory, sessionContextProvider);
			}

			@Override
			public String getCountLabel() {
				return $("TaxonomyManagementView.numberOfFolders");
			}

			@Override
			public String getTabLabel() {
				return $("TaxonomyManagementView.tabs.folders");
			}
		};

	}

	private TaxonomyManagementClassifiedType getClassifiedRetentionRuleWithAdministrativeUnit(final String conceptId,
																							  final SessionContextProvider sessionContextProvider) {
		return new TaxonomyManagementClassifiedType() {
			@Override
			public MetadataSchemaTypeVO getSchemaType() {
				return getRetentionRuleTypeVO(sessionContextProvider);
			}

			@Override
			public RecordVODataProvider getDataProvider() {
				Factory<LogicalSearchQuery> searchQueryFactory = newRulesWithAdministrativeUnitSearchQuery(
						conceptId, sessionContextProvider);
				return newRuleDataProvider(searchQueryFactory, sessionContextProvider);
			}

			@Override
			public String getCountLabel() {
				return $("TaxonomyManagementView.numberOfRetentionRules");
			}

			@Override
			public String getTabLabel() {
				return $("TaxonomyManagementView.tabs.retentionRules");
			}
		};

	}

	private TaxonomyManagementClassifiedType getClassifiedFolderInCategory(final String conceptId,
																		   final SessionContextProvider sessionContextProvider) {
		return new TaxonomyManagementClassifiedType() {
			@Override
			public MetadataSchemaTypeVO getSchemaType() {
				return getFolderTypeVO(sessionContextProvider);
			}

			@Override
			public RecordVODataProvider getDataProvider() {
				Factory<LogicalSearchQuery> searchQueryFactory = newFolderInCategorySearchQuery(
						conceptId, sessionContextProvider);
				return newFolderDataProvider(searchQueryFactory, sessionContextProvider);
			}

			@Override
			public String getCountLabel() {
				return $("TaxonomyManagementView.numberOfFolders");
			}

			@Override
			public String getTabLabel() {
				return $("TaxonomyManagementView.tabs.folders");
			}
		};
	}

	private RecordVODataProvider newRuleDataProvider(final Factory<LogicalSearchQuery> logicalSearchQueryFactory,
													 SessionContextProvider sessionContextProvider) {

		MetadataSchemaToVOBuilder schemaVOBuilder = new MetadataSchemaToVOBuilder();
		SessionContext sessionContext = sessionContextProvider.getSessionContext();

		AppLayerFactory appLayerFactory = sessionContextProvider.getConstellioFactories().getAppLayerFactory();
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, sessionContextProvider);
		RecordToVOBuilder voBuilder = new RecordToVOBuilder();
		MetadataSchemaVO rulesSchemaVO = schemaVOBuilder.build(rm.retentionRule.schema(), VIEW_MODE.TABLE, sessionContext);
		return new RecordVODataProvider(rulesSchemaVO, voBuilder, sessionContextProvider) {
			@Override
			public LogicalSearchQuery getQuery() {
				return logicalSearchQueryFactory.get();
			}
		};
	}

	private RecordVODataProvider newFolderDataProvider(final Factory<LogicalSearchQuery> logicalSearchQueryFactory,
													   SessionContextProvider sessionContextProvider) {

		MetadataSchemaToVOBuilder schemaVOBuilder = new MetadataSchemaToVOBuilder();
		SessionContext sessionContext = sessionContextProvider.getSessionContext();
		FolderToVOBuilder voBuilder = new FolderToVOBuilder();
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, sessionContextProvider);
		MetadataSchemaVO foldersSchemaVO = schemaVOBuilder.build(rm.folder.schema(), VIEW_MODE.TABLE, sessionContext);
		return new RecordVODataProvider(foldersSchemaVO, voBuilder, sessionContextProvider) {
			@Override
			public LogicalSearchQuery getQuery() {
				return logicalSearchQueryFactory.get();
			}
		};
	}

	private MetadataSchemaTypeVO getRetentionRuleTypeVO(SessionContextProvider sessionContextProvider) {
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, sessionContextProvider);
		MetadataSchemaTypeToVOBuilder schemaTypeVOBuilder = new MetadataSchemaTypeToVOBuilder();
		return schemaTypeVOBuilder.build(rm.retentionRule.schemaType());
	}

	private MetadataSchemaTypeVO getFolderTypeVO(SessionContextProvider sessionContextProvider) {
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, sessionContextProvider);
		MetadataSchemaTypeToVOBuilder schemaTypeVOBuilder = new MetadataSchemaTypeToVOBuilder();
		return schemaTypeVOBuilder.build(rm.folder.schemaType());
	}

	private Factory<LogicalSearchQuery> newFolderInAdministrativeUnitSearchQuery(
			final String conceptId, final SessionContextProvider sessionContextProvider) {
		return new Factory<LogicalSearchQuery>() {

			@Override
			public LogicalSearchQuery get() {
				RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, sessionContextProvider);
				return new LogicalSearchQuery()
						.setCondition(from(rm.folder.schemaType()).where(rm.folder.administrativeUnit()).isEqualTo(conceptId))
						.sortAsc(Schemas.TITLE);
			}
		};
	}

	private Factory<LogicalSearchQuery> newRulesWithAdministrativeUnitSearchQuery(
			final String conceptId, final SessionContextProvider sessionContextProvider) {
		return new Factory<LogicalSearchQuery>() {

			@Override
			public LogicalSearchQuery get() {
				RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, sessionContextProvider);
				return new LogicalSearchQuery()
						.setCondition(from(rm.retentionRule.schemaType())
								.where(rm.retentionRule.administrativeUnits()).isEqualTo(conceptId))
						.sortAsc(Schemas.TITLE);
			}
		};
	}

	private Factory<LogicalSearchQuery> newFolderInCategorySearchQuery(
			final String conceptId, final SessionContextProvider sessionContextProvider) {
		return new Factory<LogicalSearchQuery>() {

			@Override
			public LogicalSearchQuery get() {
				RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, sessionContextProvider);
				return new LogicalSearchQuery()
						.setCondition(from(rm.folder.schemaType()).where(rm.folder.category()).isEqualTo(conceptId))
						.sortAsc(Schemas.TITLE);
			}
		};
	}
}

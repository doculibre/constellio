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
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.MetadataSchemaTypeVO;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.MetadataSchemaTypeToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.base.SessionContextProvider;
import com.constellio.data.frameworks.extensions.ExtensionBooleanResult;
import com.constellio.data.utils.Factory;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordUtils;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.users.UserServices;
import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
					user.has(RMPermissionsTo.MANAGE_CLASSIFICATION_PLAN).onSomething());
		} else if (canManageTaxonomyParams.getTaxonomy().getCode().equals(RMTaxonomies.STORAGES)) {
			return ExtensionBooleanResult.forceTrueIf(
					user.has(RMPermissionsTo.MANAGE_STORAGE_SPACES).globally());
		} else {
			return ExtensionBooleanResult.NOT_APPLICABLE;
		}
	}

	public ExtensionBooleanResult canConsultTaxonomy(CanConsultTaxonomyParams canConsultTaxonomyParams) {
		if(canConsultTaxonomyParams.getTaxonomy().getCode().equals(RMTaxonomies.CLASSIFICATION_PLAN)) {
			return ExtensionBooleanResult.forceTrueIf(canConsultTaxonomyParams.getUser().has(RMPermissionsTo.CONSULT_CLASSIFICATION_PLAN).onSomething());
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
		if (params.isTaxonomy(RMTaxonomies.CLASSIFICATION_PLAN)) {
			fields.add(new TaxonomyExtraField() {
				@Override
				public String getCode() {
					return "administrativeUnits";
				}

				@Override
				public String getLabel() {
					return $("TaxonomyManagementView.responsibleAdministrativeUnits");
				}

				@Override
				public Component buildComponent() {
					return buildResponsibleAdministrativeUnitsComponent(params);
				}

			});
		}

		return fields;
	}

	private Component buildResponsibleAdministrativeUnitsComponent(GetTaxonomyExtraFieldsParam params) {
		Set<String> rulesResponsibleAdministrativeUnits = getRulesResponsibleAdministrativeUnits(params);
		List<Component> administrativeUnitsReferenceDisplayComponents = getAdministrativeUnitsReferenceDisplaySortedComponents(rulesResponsibleAdministrativeUnits, params.getSessionContextProvider());
		VerticalLayout verticalLayout = new VerticalLayout();
		boolean hasComponents = false;
		for (Component component : administrativeUnitsReferenceDisplayComponents) {
			verticalLayout.addComponent(component);
			hasComponents = true;
		}
		return hasComponents ? verticalLayout : null;
	}

	private Set<String> getRulesResponsibleAdministrativeUnits(GetTaxonomyExtraFieldsParam params) {
		final Record record = params.getRecord();
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, params.getSessionContextProvider());
		Category category = rm.wrapCategory(record);
		List<String> rententionRulesIds = category.getRententionRules();
		List<RetentionRule> retentionRules = rm.getRetentionRules(rententionRulesIds);
		Set<String> responsibleAdministrativeUnits = new HashSet<>();
		for (RetentionRule retentionRule : retentionRules) {
			responsibleAdministrativeUnits.addAll(retentionRule.getAdministrativeUnits());
		}
		return responsibleAdministrativeUnits;
	}

	private List<Component> getAdministrativeUnitsReferenceDisplaySortedComponents(
			Set<String> responsibleAdministrativeUnits, SessionContextProvider sessionContextProvider) {
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, sessionContextProvider);
		AppLayerFactory appLayerFactory = ConstellioFactories.getInstance().getAppLayerFactory();
		Metadata metadata = rm.retentionRule.administrativeUnits();
		String currentCollection = metadata.getCollection();
		List<Component> componentsList = new ArrayList<>();
		for (String administrativeUnit : responsibleAdministrativeUnits) {
			componentsList.add(appLayerFactory.getExtensions().forCollection(currentCollection).
					getDisplayForReference(metadata.getAllowedReferences(), administrativeUnit));
		}
		Collections.sort(componentsList, Comparator.comparing(Component::getCaption));
		return componentsList;
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
		return RecordUtils.toWrappedRecordIdsList(decommissioningService.getRetentionRulesForAdministrativeUnit(conceptId));
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
													   final SessionContextProvider sessionContextProvider) {

		MetadataSchemaToVOBuilder schemaVOBuilder = new MetadataSchemaToVOBuilder();
		SessionContext sessionContext = sessionContextProvider.getSessionContext();
		FolderToVOBuilder voBuilder = new FolderToVOBuilder();
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, sessionContextProvider);
		MetadataSchemaVO foldersSchemaVO = schemaVOBuilder.build(rm.folder.schema(), VIEW_MODE.TABLE, sessionContext);
		User currentUser = rm.getUser(sessionContext.getCurrentUser().getId());
		return new RecordVODataProvider(foldersSchemaVO, voBuilder, sessionContextProvider) {
			@Override
			public LogicalSearchQuery getQuery() {
				return logicalSearchQueryFactory.get().filteredWithUserRead(currentUser);
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
				User currentUser = getCurrentUser(sessionContextProvider);
				return new LogicalSearchQuery()
						.setCondition(from(rm.folder.schemaType()).where(rm.folder.administrativeUnit()).isEqualTo(conceptId))
						.sortAsc(Schemas.TITLE).filteredWithUserRead(currentUser);
			}
		};
	}

	private User getCurrentUser(SessionContextProvider sessionContextProvider) {
		UserServices userServices = sessionContextProvider.getConstellioFactories().getModelLayerFactory().newUserServices();
		SessionContext sessionContext = sessionContextProvider.getSessionContext();
		String username = sessionContext.getCurrentUser().getUsername();
		String collection = sessionContext.getCurrentCollection();
		return userServices.getUserInCollection(username, collection);
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
				User currentUser = getCurrentUser(sessionContextProvider);
				return new LogicalSearchQuery()
						.setCondition(from(rm.folder.schemaType()).where(rm.folder.category()).isEqualTo(conceptId))
						.sortAsc(Schemas.TITLE).filteredWithUserRead(currentUser);
			}
		};
	}
}

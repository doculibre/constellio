package com.constellio.app.modules.robots.ui.pages;

import com.constellio.app.entities.schemasDisplay.MetadataDisplayConfig;
import com.constellio.app.extensions.AppLayerCollectionExtensions;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbDocument;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbFolder;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.robots.model.RegisteredAction;
import com.constellio.app.modules.robots.model.services.RobotsService;
import com.constellio.app.modules.robots.model.wrappers.ActionParameters;
import com.constellio.app.modules.robots.model.wrappers.Robot;
import com.constellio.app.modules.robots.services.RobotsManager;
import com.constellio.app.modules.robots.ui.components.actionParameters.DynamicParametersField.DynamicParametersPresenter;
import com.constellio.app.modules.robots.ui.navigation.RobotViews;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.entities.UserVO;
import com.constellio.app.ui.framework.builders.MetadataToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.components.OverridingMetadataFieldFactory.Choice;
import com.constellio.app.ui.framework.components.OverridingMetadataFieldFactory.FieldOverridePresenter;
import com.constellio.app.ui.framework.components.OverridingMetadataFieldFactory.OverrideMode;
import com.constellio.app.ui.framework.data.SearchResultVODataProvider;
import com.constellio.app.ui.pages.base.SchemaPresenterUtils;
import com.constellio.app.ui.pages.search.AdvancedSearchCriteriaComponent.SearchCriteriaPresenter;
import com.constellio.app.ui.pages.search.SearchCriteriaPresenterUtils;
import com.constellio.app.ui.pages.search.SearchPresenterService;
import com.constellio.app.ui.pages.search.criteria.Criterion;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.data.dao.dto.records.FacetValue;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.enums.SearchPageLength;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemasRuntimeException;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.RecordServicesException.ValidationException;
import com.constellio.model.services.schemas.MetadataList;
import com.constellio.model.services.search.StatusFilter;
import com.constellio.model.services.search.query.ReturnedMetadatasFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.vaadin.ui.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class AddEditRobotPresenter extends BaseRobotPresenter<AddEditRobotView>
		implements FieldOverridePresenter, SearchCriteriaPresenter, DynamicParametersPresenter {
	private static final String RUN_EXTRACTORS_ACTION = "runExtractorsAction";
	private static final String PATH_PREFIX = "pathPrefix";
	private static final String IN_TAXONOMY = "inTaxonomy";
	private static final String PLAN = "plan";
	private static final String DEFAULT_COPY_STATUS = "defaultCopyStatus";
	public static final String ADD = "add";
	public static final String EDIT = "edit";

	private static final String ACTION = Robot.DEFAULT_SCHEMA + "_" + Robot.ACTION;
	private static final String SCHEMA_FILTER = Robot.DEFAULT_SCHEMA + "_" + Robot.SCHEMA_FILTER;

	private RecordToVOBuilder recordToVOBuilder = new RecordToVOBuilder();
	private SearchCriteriaPresenterUtils searchCriteriaPresenterUtils;
	private RecordVO robot;
	private RecordVO actionParameters;
	private String schemaFilter;
	private String pageMode;
	private String actionCode;

	transient SchemasDisplayManager schemasDisplayManager;
	transient SearchPresenterService searchPresenterService;

	public AddEditRobotPresenter(AddEditRobotView view) {
		super(view, Robot.DEFAULT_SCHEMA);
		init();

	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		init();
	}

	private void init() {

		User user = view.getConstellioFactories().getAppLayerFactory()
				.getModelLayerFactory().newUserServices()
				.getUserInCollection(view.getSessionContext().getCurrentUser().getUsername(), collection);
		searchPresenterService = new SearchPresenterService(collection, user, modelLayerFactory, null);
		schemasDisplayManager = appLayerFactory.getMetadataSchemasDisplayManager();
		searchCriteriaPresenterUtils = new SearchCriteriaPresenterUtils(view.getSessionContext());
	}

	public AddEditRobotPresenter forParams(String parameters) {
		Map<String, String> params = ParamUtils.getParamsMap(parameters);
		pageMode = params.get("pageMode");
		if (isAddMode()) {
			robot = newRobot(params.get("parentId"));
		} else if (isEditMode()) {
			robot = loadRobot(params.get("robotId"));
			actionCode = robot.get(Robot.ACTION);
			actionParameters = loadActionParametersRecord();
		}
		schemaFilter = robot.get(Robot.SCHEMA_FILTER);
		return this;
	}

	public RecordVO getRobot() {
		return robot;
	}

	public void saveButtonClicked(RecordVO recordVO) {
		Transaction transaction = new Transaction().setUser(getCurrentUser());
		if (actionParameters != null) {
			transaction.add(toParametersRecord(actionParameters));
		}
		transaction.add(toRecord(recordVO));
		try {
			recordServices().execute(transaction);
		} catch (RecordServicesException e) {
			view.showErrorMessage(e.getMessage());
			return;
		}
		view.navigate().to(RobotViews.class).robotConfiguration(recordVO.getId());
	}

	public void backButtonClicked(RecordVO recordVO) {
		if (isAddMode()) {
			String parentId = recordVO.get(Robot.PARENT);
			if (parentId == null) {
				view.navigate().to(RobotViews.class).listRootRobots();
			} else {
				view.navigate().to(RobotViews.class).robotConfiguration(parentId);
			}
		} else {
			view.navigate().to(RobotViews.class).robotConfiguration(recordVO.getId());
		}
	}

	public boolean canEditSchemaFilter() {
		return isAddMode() && robot.get(Robot.PARENT) == null;
	}

	public void schemaFilterSelected(String schemaType) {
		this.schemaFilter = schemaType;
		view.setCriteriaSchema(schemaType);
		view.setAvailableActions(getActionChoices());
		view.resetActionParameters(null);
		view.setActionParametersFieldEnabled(requiresActionParameters());
	}

	public String getSchemaFilter() {
		return schemaFilter;
	}

	@Override
	public OverrideMode getOverride(String metadataCode) {
		switch (metadataCode) {
			case ACTION:
			case SCHEMA_FILTER:
				return OverrideMode.DROPDOWN;
			default:
				return OverrideMode.NONE;
		}
	}

	@Override
	public List<Choice> getChoices(String metadataCode) {
		switch (metadataCode) {
			case ACTION:
				return getActionChoices();
			case SCHEMA_FILTER:
				return getSchemaFilterChoices();
			default:
				throw new ImpossibleRuntimeException("BUG. No choices for metadata: " + metadataCode);
		}
	}

	public boolean canAutoExecute() {
		return robot.get(Robot.PARENT) == null;
	}

	@Override
	public void addCriterionRequested() {
		view.addEmptyCriterion();
	}

	@Override
	public List<MetadataVO> getMetadataAllowedInCriteria() {
		MetadataSchemaType schemaType = types().getSchemaType(schemaFilter);
		List<FacetValue> schema_s = modelLayerFactory.newSearchServices().query(new LogicalSearchQuery()
				.setNumberOfRows(0)
				.setCondition(from(schemaType).returnAll()).addFieldFacet("schema_s").filteredWithUser(getCurrentUser()))
				.getFieldFacetValues("schema_s");
		Set<String> metadataCodes = new HashSet<>();
		if (schema_s != null) {
			for (FacetValue facetValue : schema_s) {
				if (facetValue.getQuantity() > 0) {
					String schema = facetValue.getValue();
					for (Metadata metadata : types().getSchema(schema).getMetadatas()) {
						if (metadata.getInheritance() != null && metadata.isEnabled()) {
							metadataCodes.add(metadata.getInheritance().getCode());
						} else if (metadata.getInheritance() == null && metadata.isEnabled()) {
							metadataCodes.add(metadata.getCode());
						}
					}
				}
			}
		}

		MetadataToVOBuilder builder = new MetadataToVOBuilder();

		List<MetadataVO> result = new ArrayList<>();
		//		result.add(builder.build(schemaType.getMetadataWithAtomicCode(CommonMetadataBuilder.PATH), view.getSessionContext()));
		MetadataList allMetadatas = schemaType.getAllMetadatas();
		for (Metadata metadata : allMetadatas) {
			if (!schemaType.hasSecurity() || (metadataCodes.contains(metadata.getCode()))) {
				boolean isTextOrString = metadata.getType() == MetadataValueType.STRING || metadata.getType() == MetadataValueType.TEXT;
				MetadataDisplayConfig config = schemasDisplayManager().getMetadata(view.getCollection(), metadata.getCode());
				if (config.isVisibleInAdvancedSearch() && isMetadataVisibleForUser(metadata, getCurrentUser()) &&
					(!isTextOrString || isTextOrString && metadata.isSearchable() ||
					 Schemas.PATH.getLocalCode().equals(metadata.getLocalCode()) ||
					 ConnectorSmbFolder.PARENT_CONNECTOR_URL.equals(metadata.getLocalCode()) ||
					 ConnectorSmbFolder.CONNECTOR_URL.equals(metadata.getLocalCode()) ||
					 ConnectorSmbDocument.PARENT_CONNECTOR_URL.equals(metadata.getLocalCode()))) {
					result.add(builder.build(metadata, view.getSessionContext()));
				}
			}
		}
		return result;
	}

	@Override
	public Map<String, String> getMetadataSchemasList(String schemaTypeCode) {
		return searchCriteriaPresenterUtils.getMetadataSchemasList(schemaTypeCode);
	}

	private boolean isMetadataVisibleForUser(Metadata metadata, User currentUser) {
		if (MetadataValueType.REFERENCE.equals(metadata.getType())) {
			String referencedSchemaType = metadata.getAllowedReferences().getTypeWithAllowedSchemas();
			Taxonomy taxonomy = appLayerFactory.getModelLayerFactory().getTaxonomiesManager().getTaxonomyFor(collection, referencedSchemaType);
			if (taxonomy != null) {
				List<String> taxonomyGroupIds = taxonomy.getGroupIds();
				List<String> taxonomyUserIds = taxonomy.getUserIds();
				List<String> userGroups = currentUser.getUserGroups();
				for (String group : taxonomyGroupIds) {
					for (String userGroup : userGroups) {
						if (userGroup.equals(group)) {
							return true;
						}
					}
				}
				return (taxonomyGroupIds.isEmpty() && taxonomyUserIds.isEmpty()) || taxonomyUserIds.contains(currentUser.getId());
			} else {
				return true;
			}
		}
		return true;
	}

	@Override
	public MetadataVO getMetadataVO(String metadataCode) {
		return presenterService().getMetadataVO(metadataCode, view.getSessionContext());
	}

	@Override
	public Component getExtensionComponentForCriterion(Criterion criterion) {
		AppLayerCollectionExtensions extensions = appLayerFactory.getExtensions().forCollection(view.getCollection());
		return extensions.getComponentForCriterion(criterion);
	}

	@Override
	public void showErrorMessage(String message) {
		view.showErrorMessage(message);
	}

	public boolean isAddMode() {
		return ADD.equals(pageMode);
	}

	public boolean isEditMode() {
		return EDIT.equals(pageMode);
	}

	private RecordVO loadRobot(String robotId) {
		return recordToVOBuilder.build(getRecord(robotId), VIEW_MODE.FORM, view.getSessionContext());
	}

	private RecordVO newRobot(String parentId) {
		Robot robot = new RobotsService(view.getCollection(), appLayerFactory).newRobot(parentId);
		return recordToVOBuilder.build(robot.getWrappedRecord(), VIEW_MODE.FORM, view.getSessionContext());
	}

	private List<Choice> getSchemaFilterChoices() {
		List<Choice> choices = new ArrayList<>();
		Language language = Language.withCode(view.getSessionContext().getCurrentLocale().getLanguage());
		for (String code : manager().getSupportedSchemaTypes()) {
			choices.add(new Choice(code, schemaType(code).getLabel(language)));
		}
		return choices;
	}

	private List<Choice> getActionChoices() {
		List<Choice> choices = new ArrayList<>();
		if (schemaFilter != null) {
			for (RegisteredAction action : manager().getRegisteredActionsFor(schemaFilter)) {
				if (!RUN_EXTRACTORS_ACTION.equals(action.getCode())) {
					choices.add(new Choice(action.getCode(), $("robot.action." + action.getCode())));
				}
			}
		}
		return choices;
	}

	@Override
	public RecordVO getDynamicParametersRecord() {
		return actionParameters;
	}

	@Override
	public RecordVO newDynamicParametersRecord() {
		RegisteredAction action = manager().getActionFor(actionCode);
		String schemaCode = action.getParametersSchemaLocalCode();
		ActionParameters actionParameters = robotSchemas().newActionParameters(schemaCode);

		initMetadataValue(actionParameters, DEFAULT_COPY_STATUS, CopyType.PRINCIPAL, false);
		initMetadataValue(actionParameters, IN_TAXONOMY, PLAN, true);

		try {
			Metadata m = actionParameters.getSchema().get(PATH_PREFIX);
			initMetadataValue(actionParameters, PATH_PREFIX, m.getDefaultValue(), true);
		} catch (MetadataSchemasRuntimeException.NoSuchMetadata e) {
			// Just ignore it : The metadata defaultCopyStatus doesn't appart to this scheme
		}

		return recordToVOBuilder.build(actionParameters.getWrappedRecord(), VIEW_MODE.FORM, view.getSessionContext());
	}

	private <T> void initMetadataValue(ActionParameters actionParameters, String metadataCode, T value, boolean force) {
		try {
			Metadata metadata = actionParameters.getSchema().get(metadataCode);
			if (metadata != null && (force || actionParameters.get(metadata) == null)) {
				actionParameters.set(metadata, value);
			}
		} catch (MetadataSchemasRuntimeException.NoSuchMetadata e) {
			// Just ignore it : The metadata defaultCopyStatus doesn't appart to this scheme
		}
	}

	private RecordVO loadActionParametersRecord() {
		String id = robot.get(Robot.ACTION_PARAMETERS);
		if (id == null) {
			return null;
		} else {
			return recordToVOBuilder.build(getRecord(id), VIEW_MODE.FORM, view.getSessionContext());
		}
	}

	private Record toParametersRecord(RecordVO recordVO) {
		String schema = ActionParameters.SCHEMA_TYPE + "_" + getParametersSchemaLocalCode();
		return new SchemaPresenterUtils(schema, view.getConstellioFactories(), view.getSessionContext()).toRecord(recordVO);
	}

	@SuppressWarnings("unused")
	@Override
	public boolean saveParametersRecord(RecordVO record) {
		try {
			recordServices().validateRecord(toParametersRecord(record));
		} catch (ValidationException e) {
			view.showErrorMessage($(e.getErrors()));
			return false;
		}
		actionParameters = record;
		view.resetActionParameters(record);
		return true;
	}

	@Override
	public void cancelParametersEdit(RecordVO record) {
		// No need to actually do anything
	}

	public void actionSelected(String actionCode) {
		this.actionCode = actionCode;
		if (actionParameters != null && !actionParameters.getSchema().getCode().endsWith("_" + getParametersSchemaLocalCode())) {
			view.resetActionParameters(null);
		}
		view.setActionParametersFieldEnabled(requiresActionParameters());
	}

	public boolean requiresActionParameters() {
		if (actionCode == null) {
			return false;
		}
		return getParametersSchemaLocalCode() != null;
	}

	private String getParametersSchemaLocalCode() {
		return manager().getActionFor(actionCode).getParametersSchemaLocalCode();
	}

	public SearchResultVODataProvider getSearchResults(final List<Criterion> searchCriteria) {
		SearchPageLength defaultPageLength = getCurrentUser().getDefaultPageLength();
		int providerPageLength = defaultPageLength != null ? defaultPageLength.getValue() : 10;
		return new SearchResultVODataProvider(new RecordToVOBuilder(), appLayerFactory, view.getSessionContext(), providerPageLength) {
			@Override
			public LogicalSearchQuery getQuery() {
				return getSearchQuery(searchCriteria);
			}
		};
	}

	protected LogicalSearchQuery getSearchQuery(List<Criterion> searchCriteria) {
		LogicalSearchQuery query = new LogicalSearchQuery(getSearchCondition(searchCriteria)).filteredWithUser(getCurrentUser())
				.filteredByStatus(StatusFilter.ACTIVES).setPreferAnalyzedFields(true);

		query.setReturnedMetadatas(
				ReturnedMetadatasFilter.onlyFields(schemasDisplayManager.getReturnedFieldsForSearch(collection)));

		return query;
	}

	private LogicalSearchCondition getSearchCondition(List<Criterion> searchCriteria) {
		RobotsManager robotsManager = manager();
		Robot tmpRobot = robotSchemas().wrapRobot(toRecord(robot));
		tmpRobot.setSearchCriteria(searchCriteria);
		tmpRobot.setSchemaFilter(schemaFilter);
		return robotsManager.getResolveCondition(tmpRobot);
	}

	public UserVO getCurrentUserVO() {
		return view.getSessionContext().getCurrentUser();
	}
}

package com.constellio.app.modules.robots.ui.pages;

import static com.constellio.app.ui.i18n.i18n.$;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.constellio.app.entities.schemasDisplay.MetadataDisplayConfig;
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
import com.constellio.app.ui.pages.search.SearchPresenterService;
import com.constellio.app.ui.pages.search.criteria.Criterion;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.RecordServicesException.ValidationException;
import com.constellio.model.services.schemas.builders.CommonMetadataBuilder;
import com.constellio.model.services.search.StatusFilter;
import com.constellio.model.services.search.query.ReturnedMetadatasFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

public class AddEditRobotPresenter extends BaseRobotPresenter<AddEditRobotView>
		implements FieldOverridePresenter, SearchCriteriaPresenter, DynamicParametersPresenter {
	public static final String ADD = "add";
	public static final String EDIT = "edit";

	private static final String ACTION = Robot.DEFAULT_SCHEMA + "_" + Robot.ACTION;
	private static final String SCHEMA_FILTER = Robot.DEFAULT_SCHEMA + "_" + Robot.SCHEMA_FILTER;

	private RecordToVOBuilder recordToVOBuilder = new RecordToVOBuilder();
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
		searchPresenterService = new SearchPresenterService(collection, modelLayerFactory);
		schemasDisplayManager = appLayerFactory.getMetadataSchemasDisplayManager();
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
		MetadataToVOBuilder builder = new MetadataToVOBuilder();
		MetadataSchemaType schemaType = schemaType(schemaFilter);

		List<MetadataVO> result = new ArrayList<>();
		result.add(builder.build(schemaType.getMetadataWithAtomicCode(CommonMetadataBuilder.PATH), view.getSessionContext()));
		for (Metadata metadata : schemaType.getAllMetadatas()) {
			MetadataDisplayConfig config = schemasDisplayManager().getMetadata(view.getCollection(), metadata.getCode());
			if (config.isVisibleInAdvancedSearch()) {
				result.add(builder.build(metadata, view.getSessionContext()));
			}
		}
		return result;
	}

	@Override
	public MetadataVO getMetadataVO(String metadataCode) {
		return presenterService().getMetadataVO(metadataCode, view.getSessionContext());
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
		for (String code : manager().getSupportedSchemaTypes()) {
			choices.add(new Choice(code, schemaType(code).getLabel()));
		}
		return choices;
	}

	private List<Choice> getActionChoices() {
		List<Choice> choices = new ArrayList<>();
		if (schemaFilter != null) {
			for (RegisteredAction action : manager().getRegisteredActionsFor(schemaFilter)) {
				choices.add(new Choice(action.getCode(), $("robot.action." + action.getCode())));
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
		return recordToVOBuilder.build(actionParameters.getWrappedRecord(), VIEW_MODE.FORM, view.getSessionContext());
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
		return new SchemaPresenterUtils(schema, view.getConstellioFactories(), view.getSessionContext())
				.toRecord(recordVO);
	}

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
		return new SearchResultVODataProvider(new RecordToVOBuilder(), modelLayerFactory,
				view.getSessionContext()) {
			@Override
			protected LogicalSearchQuery getQuery() {
				return getSearchQuery(searchCriteria);
			}
		};
	}

	protected LogicalSearchQuery getSearchQuery(List<Criterion> searchCriteria) {
		LogicalSearchQuery query = new LogicalSearchQuery(getSearchCondition(searchCriteria))
				.filteredWithUser(getCurrentUser())
				.filteredByStatus(StatusFilter.ACTIVES)
				.setPreferAnalyzedFields(true);

		query.setReturnedMetadatas(ReturnedMetadatasFilter.onlyFields(
				schemasDisplayManager.getReturnedFieldsForSearch(collection)));

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

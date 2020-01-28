package com.constellio.app.modules.robots.ui.pages;

import com.constellio.app.modules.robots.model.wrappers.Robot;
import com.constellio.app.modules.robots.ui.navigation.RobotViews;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.search.StatusFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class ListRootRobotsPresenter extends BaseRobotPresenter<ListRootRobotsView> {
	private RecordToVOBuilder recordToVOBuilder = new RecordToVOBuilder();
	private final MetadataSchemaVO schemaVO;

	public ListRootRobotsPresenter(ListRootRobotsView view) {
		super(view, Robot.DEFAULT_SCHEMA);
		schemaVO = new MetadataSchemaToVOBuilder().build(defaultSchema(), VIEW_MODE.TABLE, view.getSessionContext());
	}

	public RecordVODataProvider getRootRobotsDataProvider() {
		return new RecordVODataProvider(schemaVO, recordToVOBuilder, modelLayerFactory, view.getSessionContext()) {
			@Override
			public LogicalSearchQuery getQuery() {
				return new LogicalSearchQuery(from(defaultSchema()).where(getMetadata(Robot.PARENT)).isNull())
						.filteredByStatus(StatusFilter.ACTIVES).sortAsc(Schemas.TITLE);
			}
		};
	}

	public boolean isLegacyIdIndexDisabledWarningVisible() {
		return !modelLayerFactory.getSystemConfigs().isLegacyIdentifierIndexedInMemory();
	}

	public void displayButtonClicked(RecordVO recordVO) {
		view.navigate().to(RobotViews.class).robotConfiguration(recordVO.getId());
	}

	public void deleteButtonClicked(RecordVO recordVO) {
		robotsService().deleteRobotHierarchy(recordVO.getId());
		view.navigate().to(RobotViews.class).listRootRobots();
	}

	public void addButtonClicked() {
		view.navigate().to(RobotViews.class).addRobot(null);
	}

	public void backButtonClicked() {
		view.navigate().to().adminModule();
	}

}

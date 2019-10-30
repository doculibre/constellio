package com.constellio.app.modules.robots.ui.pages;

import com.constellio.app.modules.robots.ui.components.breadcrumb.RobotBreadcrumbTrail;
import com.constellio.app.modules.robots.ui.data.RobotTreeNodesDataProvider;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.AddButton;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.EditButton;
import com.constellio.app.ui.framework.buttons.LinkButton;
import com.constellio.app.ui.framework.components.MetadataDisplayFactory;
import com.constellio.app.ui.framework.components.RecordDisplay;
import com.constellio.app.ui.framework.components.breadcrumb.BaseBreadcrumbTrail;
import com.constellio.app.ui.framework.components.tree.RecordLazyTree;
import com.constellio.app.ui.framework.data.RecordLazyTreeDataProvider;
import com.constellio.app.ui.framework.stream.DownloadStreamResource;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.Page;
import com.vaadin.server.Resource;
import com.vaadin.shared.MouseEventDetails.MouseButton;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.TabSheet;
import org.vaadin.dialogs.ConfirmDialog;

import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class RobotConfigurationViewImpl extends BaseViewImpl implements RobotConfigurationView {
	private final RobotConfigurationPresenter presenter;
	private RecordVO robot;
	private Resource resource;

	public RobotConfigurationViewImpl() {
		this.presenter = new RobotConfigurationPresenter(this);
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeEvent event) {
		robot = presenter.forParams(event.getParameters()).getRootRobot();
	}

	@Override
	protected String getTitle() {
		return null;
	}

	@Override
	protected BaseBreadcrumbTrail buildBreadcrumbTrail() {
		return new RobotBreadcrumbTrail(presenter.getRootRobotId(), this);
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		TabSheet sheet = new TabSheet();
		sheet.setWidth("100%");

		RecordDisplay display = new RecordDisplay(robot, new LocalMetadataDisplayFactory());
		sheet.addTab(display, $("RobotConfigurationView.metadata"));

		// LegacyRobotLazyTreeDataProvider provider = new LegacyRobotLazyTreeDataProvider(
		// getConstellioFactories().getAppLayerFactory(), getCollection(), presenter.getRootRobotId());
		RecordLazyTreeDataProvider provider = new RecordLazyTreeDataProvider(new RobotTreeNodesDataProvider(
				getConstellioFactories().getAppLayerFactory(), getCollection(), presenter.getRootRobotId()));
		RecordLazyTree tree = new RecordLazyTree(provider);
		tree.addItemClickListener(new ItemClickListener() {
			@Override
			public void itemClick(ItemClickEvent event) {
				if (event.getButton() == MouseButton.LEFT) {
					String robotId = (String) event.getItemId();
					presenter.robotNavigationRequested(robotId);
				}
			}
		});
		sheet.addTab(tree, $("RobotConfigurationView.tree"));

		return sheet;
	}

	private class LocalMetadataDisplayFactory extends MetadataDisplayFactory {
		private static final String ROBOT_DEFAULT_ACTION = "robot_default_action";
		private static final String ROBOT_DEFAULT_SCHEMA_FILTER = "robot_default_schemaFilter";

		@Override
		public Component buildSingleValue(RecordVO recordVO, MetadataVO metadata, Object displayValue) {
			if (ROBOT_DEFAULT_SCHEMA_FILTER.equals(metadata.getCode()) || ROBOT_DEFAULT_ACTION.equals(metadata.getCode())) {
				displayValue = $(displayValue.toString());
			}

			return super.buildSingleValue(recordVO, metadata, displayValue);
		}
	}

	@Override
	protected List<Button> buildActionMenuButtons(ViewChangeEvent event) {
		List<Button> buttons = super.buildActionMenuButtons(event);
		buttons.add(buildExecuteButton());
		buttons.add(buildAddButton());
		buttons.add(buildEditButton());
		buttons.add(buildDeleteButton());
		buttons.add(buildLogsButton());
		buttons.add(buildDownloadButton());
		buttons.add(buildDeleteRecordsButton());
		return buttons;
	}

	private Button buildExecuteButton() {
		LinkButton button = new LinkButton($("RobotConfigurationView.executeRobot")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.executeButtonClicked(robot);
			}
		};
		button.setVisible(presenter.canExecute(robot));
		return button;
	}

	private Button buildAddButton() {
		return new AddButton($("RobotConfigurationView.addSubRobot")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.addButtonClicked(robot);
			}
		};
	}

	private Button buildEditButton() {
		return new EditButton($("RobotConfigurationView.editRobot")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.editButtonClicked(robot);
			}
		};
	}

	private DeleteButton buildDeleteButton() {
		return new DeleteButton($("RobotConfigurationView.deleteRobot")) {
			@Override
			protected void confirmButtonClick(ConfirmDialog dialog) {
				presenter.deleteButtonClicked(robot);
			}
		};
	}

	private Button buildLogsButton() {
		return new LinkButton($("RobotConfigurationView.viewLogs")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.viewLogsButtonClicked();
			}
		};
	}

	private Button buildDownloadButton() {
		return new LinkButton($("RobotConfigurationView.download")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				if (resource == null) {
					resource = new DownloadStreamResource(presenter.getResource(), presenter.getReportTitle());
				}

				Page.getCurrent().open(resource, null, false);
			}
		};
	}

	private DeleteButton buildDeleteRecordsButton() {
		return new DeleteButton($("RobotConfigurationView.deleteRecords")) {
			@Override
			protected void confirmButtonClick(ConfirmDialog dialog) {
				presenter.deleteRecordsButtonClicked(robot);
			}
		};
	}

	@Override
	protected ClickListener getBackButtonClickListener() {
		return new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.backButtonClicked();
			}
		};
	}
}
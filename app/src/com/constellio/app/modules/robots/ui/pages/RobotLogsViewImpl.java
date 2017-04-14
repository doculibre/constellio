package com.constellio.app.modules.robots.ui.pages;

import static com.constellio.app.ui.i18n.i18n.$;

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.containers.RecordVOLazyContainer;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;

public class RobotLogsViewImpl extends BaseViewImpl implements RobotLogsView {
	private final RobotLogsPresenter presenter;
	private RecordVO robot;

	public RobotLogsViewImpl() {
		presenter = new RobotLogsPresenter(this);
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

	@Override
	protected void initBeforeCreateComponents(ViewChangeEvent event) {
		robot = presenter.forParams(event.getParameters()).getRobot();
	}

	@Override
	protected String getTitle() {
		return $("RobotLogsView.viewTitle", robot.getTitle());
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		RecordVODataProvider logs = presenter.getLogs();
		RecordVOTable table = new RecordVOTable();
		table.setContainerDataSource(new RecordVOLazyContainer(logs));
		table.setWidth("100%");
		return table;
	}
}

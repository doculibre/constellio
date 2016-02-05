package com.constellio.app.modules.tasks.ui.pages;

import static com.constellio.app.ui.i18n.i18n.$;

import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;

public class TasksLogsViewImpl extends BaseViewImpl implements TasksLogsView {
	private TasksLogsPresenter presenter;

	public TasksLogsViewImpl() {
		presenter = new TasksLogsPresenter(this);
	}

	@Override
	protected String getTitle() {
		return $("TasksLogsView.viewTitle");
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		return new VerticalLayout();
	}
}

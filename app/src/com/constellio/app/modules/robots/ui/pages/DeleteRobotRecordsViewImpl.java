package com.constellio.app.modules.robots.ui.pages;

import com.constellio.app.ui.pages.progress.BaseProgressView;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;

public class DeleteRobotRecordsViewImpl extends BaseProgressView implements DeleteRobotRecordsView {
	
	private String title;
	
	private DeleteRobotRecordsPresenter presenter;

	public DeleteRobotRecordsViewImpl() {
		this.presenter = new DeleteRobotRecordsPresenter(this);
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeEvent event) {
		presenter.forParams(event.getParameters());
	}

	@Override
	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	protected String getTitle() {
		return title;
	}

	@Override
	protected void onDone() {
		presenter.done();
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

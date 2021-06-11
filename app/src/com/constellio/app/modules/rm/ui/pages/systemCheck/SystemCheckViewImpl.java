package com.constellio.app.modules.rm.ui.pages.systemCheck;

import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.DownloadLink;
import com.constellio.app.ui.framework.components.fields.BaseTextArea;
import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FileResource;
import com.vaadin.server.Page;
import com.vaadin.server.Resource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

import java.io.File;

import static com.constellio.app.ui.i18n.i18n.$;

public class SystemCheckViewImpl extends BaseViewImpl implements SystemCheckView {

	private SystemCheckPresenter presenter;

	private VerticalLayout mainLayout;

	private HorizontalLayout buttonsLayout, referenceLayout;

	private Label systemCheckInfoLabel;

	private Button startSystemCheckButton;

	private Button startSystemCheckAndRepairButton;

	private Button findReferences;

	private Button findIncompatibleIdsButton;

	private TextField idField;

	private BaseTextArea reportContentField;

	public SystemCheckViewImpl() {
		this.presenter = new SystemCheckPresenter(this);
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		mainLayout = new VerticalLayout();
		mainLayout.setWidth("100%");
		mainLayout.setSpacing(true);

		buttonsLayout = new HorizontalLayout();
		buttonsLayout.setSpacing(true);

		systemCheckInfoLabel = new Label();

		startSystemCheckButton = new BaseButton($("SystemCheckView.startSystemCheck")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.startSystemCheckButtonClicked();
			}
		};

		startSystemCheckAndRepairButton = new BaseButton($("SystemCheckView.startSystemCheckAndRepair")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.startSystemCheckAndRepairButtonClicked();
			}
		};

		reportContentField = new BaseTextArea($("SystemCheckView.reportContent"));
		reportContentField.setWidth("100%");
		reportContentField.setHeight("100%");
		reportContentField.setEnabled(true);

		referenceLayout = new HorizontalLayout();
		referenceLayout.setSpacing(true);
		idField = new BaseTextField();

		findReferences = new Button($("SystemCheckView.findReferencesToId"));
		findReferences.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				File report = presenter.getReferencesFor(idField.getValue());
				Resource resource = DownloadLink.wrapForDownload(new FileResource(report));
				Page.getCurrent().open(resource, "download", false);
			}
		});

		findIncompatibleIdsButton = new Button($("SystemCheckView.findIncompatibleIds"));
		findIncompatibleIdsButton.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				File report = presenter.getIncompatibleIds();
				Resource resource = DownloadLink.wrapForDownload(new FileResource(report));
				Page.getCurrent().open(resource, "download", false);
			}
		});

		referenceLayout.addComponents(idField, findReferences, findIncompatibleIdsButton);


		mainLayout.addComponents(systemCheckInfoLabel, buttonsLayout, reportContentField, referenceLayout);
		buttonsLayout.addComponents(startSystemCheckButton, startSystemCheckAndRepairButton);

		return mainLayout;
	}

	@Override
	public void setSystemCheckRunning(boolean running) {
		if (running) {
			systemCheckInfoLabel.setValue($("SystemCheckView.systemCheckRunning"));
		} else {
			systemCheckInfoLabel.setValue($("SystemCheckView.systemCheckNotRunning"));
		}
		startSystemCheckButton.setEnabled(!running);
		startSystemCheckAndRepairButton.setEnabled(!running);
	}

	@Override
	public void setReportContent(String reportContent) {
		reportContentField.setValue(reportContent);
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
	protected String getTitle() {
		return $("SystemCheckView.viewTitle");
	}

	@Override
	public boolean isBackgroundViewMonitor() {
		return true;
	}

	@Override
	protected void onBackgroundViewMonitor() {
		presenter.viewRefreshed();
	}

	@Override
	protected void afterViewAssembled(ViewChangeEvent event) {
		presenter.viewAssembled();
	}

}

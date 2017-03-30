package com.constellio.app.modules.rm.ui.pages.systemCheck;

import static com.constellio.app.ui.i18n.i18n.$;

import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.DownloadLink;
import com.constellio.app.ui.framework.components.ReportViewer;
import com.constellio.app.ui.framework.components.fields.BaseTextArea;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.*;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class SystemCheckViewImpl extends BaseViewImpl implements SystemCheckView {

	private SystemCheckPresenter presenter;

	private VerticalLayout mainLayout;

	private HorizontalLayout buttonsLayout, referenceLayout;

	private Label systemCheckInfoLabel;

	private Button startSystemCheckButton;

	private Button startSystemCheckAndRepairButton;

	private Button optainsReferences;

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
		idField = new TextField();

		StreamResource report = null;
		optainsReferences = new Button($("SystemCheckView.optainsReferences"));
		optainsReferences.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				File report = presenter.getReferencesFor(idField.getValue());
				Resource resource = DownloadLink.wrapForDownload(new FileResource(report));
				Page.getCurrent().open(resource, "download", false);
			}
		});
		referenceLayout.addComponents(idField, optainsReferences);

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
	protected boolean isBackgroundViewMonitor() {
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

package com.constellio.app.ui.pages.imports;

import static com.constellio.app.ui.i18n.i18n.$;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.DownloadLink;
import com.constellio.app.ui.framework.components.fields.BaseTextArea;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.Page;
import com.vaadin.server.Resource;
import com.vaadin.server.StreamResource;
import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;

public class ExportViewImpl extends BaseViewImpl implements ExportView {

	private TextArea idsField;

	private Button exportWithoutContentsButton;

	private Button exportWithContentsButton;

	private Button exportLogs;

	private final ExportPresenter presenter;

	public ExportViewImpl() {
		presenter = new ExportPresenter(this);
	}

	@Override
	protected String getTitle() {
		return $("ExportView.viewTitle");
	}

	@Override
	protected ClickListener getBackButtonClickListener() {
		return new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.backButtonPressed();
			}
		};
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		idsField = new BaseTextArea($("ExportView.exportedIds"));
		idsField.setWidth("100%");

		exportWithoutContentsButton = new BaseButton($("ExportView.exportNoContents")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.exportWithoutContentsButtonClicked();
			}
		};

		exportWithContentsButton = new BaseButton($("ExportView.exportAllContents")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.exportWithContentsButtonClicked();
			}
		};
		exportLogs = new BaseButton($("ExportView.exportLogs")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.exportLogs();
			}
		};
		exportWithContentsButton.setVisible(false);

		VerticalLayout layout = new VerticalLayout(idsField, exportWithoutContentsButton, exportWithContentsButton, exportLogs);
		layout.setSizeFull();
		layout.setSpacing(true);

		return layout;
	}

	@Override
	public String getExportedIds() {
		return idsField.getValue();
	}

	@SuppressWarnings("deprecation")
	@Override
	public void startDownload(final String filename, final InputStream inputStream, final String mimeType) {
		StreamSource streamSource = new StreamSource() {
			@Override
			public InputStream getStream() {
				return inputStream;
			}
		};
		StreamResource resource = new StreamResource(streamSource, filename);
		resource.setMIMEType(mimeType);
		Page.getCurrent().open(resource, "_blank", false);
	}
}

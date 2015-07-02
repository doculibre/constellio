/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.ui.pages.imports;

import static com.constellio.app.ui.i18n.i18n.$;

import java.io.InputStream;

import com.constellio.app.ui.framework.components.ReportViewer.DownloadStreamResource;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Link;
import com.vaadin.ui.VerticalLayout;

public class ExportViewImpl extends BaseViewImpl implements ExportView {
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
		Link exportWithoutContents = new Link($("ExportView.exportNoContents"),
				new DownloadStreamResource(buildStreamSource(false), "systemstate.zip", "application/zip"));
		Link exportWithContents = new Link($("ExportView.exportAllContents"),
				new DownloadStreamResource(buildStreamSource(true), "systemstate.zip", "application/zip"));

		VerticalLayout layout = new VerticalLayout(exportWithoutContents, exportWithContents);
		layout.setSizeFull();
		layout.setSpacing(true);

		return layout;
	}

	private StreamSource buildStreamSource(final boolean includeContents) {
		return new StreamSource() {
			@Override
			public InputStream getStream() {
				return presenter.buildExportFile(includeContents);
			}
		};
	}
}

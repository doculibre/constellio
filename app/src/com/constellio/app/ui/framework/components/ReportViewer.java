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
package com.constellio.app.ui.framework.components;

import static com.constellio.app.ui.i18n.i18n.$;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.constellio.app.reports.builders.administration.plan.ReportBuilderFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.vaadin.server.DownloadStream;
import com.vaadin.server.StreamResource;
import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Link;
import com.vaadin.ui.VerticalLayout;

public class ReportViewer extends VerticalLayout {
	public ReportViewer(ReportBuilderFactory factory) {
		StreamSource source = buildSource(factory);

		Embedded viewer = new Embedded();
		viewer.setSource(new StreamResource(source, factory.getFilename()));
		viewer.setType(Embedded.TYPE_BROWSER);
		viewer.setWidth("100%");
		viewer.setHeight("1024px");

		Link download = new Link($("ReportViewer.download", factory.getFilename()),
				new DownloadStreamResource(source, factory.getFilename()));

		addComponents(download, viewer);
		setWidth("100%");
	}

	private StreamSource buildSource(final ReportBuilderFactory factory) {
		return new StreamSource() {
			@Override
			public InputStream getStream() {
				ModelLayerFactory modelLayerFactory = ConstellioFactories.getInstance().getModelLayerFactory();
				ByteArrayOutputStream output = new ByteArrayOutputStream();
				try {
					factory.getReportBuilder(modelLayerFactory).build(output);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				return new ByteArrayInputStream(output.toByteArray());
			}
		};
	}

	private static class DownloadStreamResource extends StreamResource {
		public static String PDF_MIMETYPE = "application/pdf";

		public DownloadStreamResource(StreamSource source, String filename) {
			this(source, filename, PDF_MIMETYPE);
		}

		public DownloadStreamResource(StreamSource source, String filename, String MIMEType) {
			super(source, filename);
			setMIMEType(MIMEType);
		}

		@Override
		public DownloadStream getStream() {
			DownloadStream stream = super.getStream();
			stream.setParameter("Content-Disposition", "attachment; filename=" + getFilename());
			return stream;
		}
	}
}

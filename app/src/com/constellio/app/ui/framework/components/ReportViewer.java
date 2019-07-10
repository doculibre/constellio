package com.constellio.app.ui.framework.components;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.framework.reports.NewReportWriterFactory;
import com.constellio.app.ui.framework.reports.ReportWriter;
import com.constellio.app.ui.framework.stream.DownloadStreamResource;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.vaadin.server.StreamResource;
import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Link;
import com.vaadin.ui.VerticalLayout;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static com.constellio.app.ui.i18n.i18n.$;

public class ReportViewer extends VerticalLayout {

	public ReportViewer(ReportWriter reportWriter, String filename) {
		addStyleName("no-scroll");
		StreamSource source = buildSource(reportWriter);

		Embedded viewer = new Embedded();
		StreamResource streamResource = new StreamResource(source, filename);
		streamResource.setCacheTime(0);
		viewer.setSource(streamResource);
		viewer.setType(Embedded.TYPE_BROWSER);
		viewer.setWidth("100%");
		viewer.setHeight("95%");

		Link download = new Link($("ReportViewer.download", filename),
				new DownloadStreamResource(source, filename, DownloadStreamResource.getMimeTypeFromFileName(filename)));

		addComponents(download, viewer);
		setExpandRatio(viewer, 1);
		setWidth("100%");
		setHeight("100%");
	}

	@Deprecated
	private StreamSource buildSource(final NewReportWriterFactory factory) {
		return new StreamSource() {
			@Override
			public InputStream getStream() {
				ModelLayerFactory modelLayerFactory = ConstellioFactories.getInstance().getModelLayerFactory();
				ByteArrayOutputStream output = new ByteArrayOutputStream();
				try {
					factory.getReportBuilder(modelLayerFactory).write(output);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				return new ByteArrayInputStream(output.toByteArray());
			}
		};
	}

	static StreamSource buildSource(final ReportWriter reportWriter) {
		return new StreamSource() {
			@Override
			public InputStream getStream() {
				ByteArrayOutputStream output = new ByteArrayOutputStream();
				try {
					reportWriter.write(output);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				return new ByteArrayInputStream(output.toByteArray());
			}
		};
	}
}

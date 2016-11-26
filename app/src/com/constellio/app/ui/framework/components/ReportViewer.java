package com.constellio.app.ui.framework.components;

import static com.constellio.app.ui.i18n.i18n.$;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.lang.StringUtils;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.framework.reports.NewReportBuilderFactory;
import com.constellio.app.ui.framework.reports.ReportBuilder;
import com.constellio.app.ui.framework.reports.ReportBuilderFactory;
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
				new DownloadStreamResource(source, factory.getFilename(), getMimeTypeFromFileName(factory.getFilename())));

		addComponents(download, viewer);
		setWidth("100%");
	}

	public ReportViewer(ReportBuilder reportBuilder, String filename) {
		StreamSource source = buildSource(reportBuilder);

		Embedded viewer = new Embedded();
		viewer.setSource(new StreamResource(source, filename));
		viewer.setType(Embedded.TYPE_BROWSER);
		viewer.setWidth("100%");
		viewer.setHeight("1024px");

		Link download = new Link($("ReportViewer.download", filename),
				new DownloadStreamResource(source, filename, getMimeTypeFromFileName(filename)));

		addComponents(download, viewer);
		setWidth("100%");
	}

	static String getMimeTypeFromFileName(String filename) {
		if (StringUtils.isBlank(filename)) {
			return DownloadStreamResource.PDF_MIMETYPE;
		}
		String extension = StringUtils.substringAfterLast(filename, ".");
		if (StringUtils.isBlank(extension)) {
			return DownloadStreamResource.PDF_MIMETYPE;
		}
		extension = extension.toLowerCase();
		if (extension.equals("xls") || extension.equals("xlsx")) {
			return DownloadStreamResource.EXCEL_MIMETYPE;
		}
		if (extension.equals("zip")) {
			return DownloadStreamResource.ZIP_MIMETYPE;
		}
		return DownloadStreamResource.PDF_MIMETYPE;
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

	private StreamSource buildSource(final ReportBuilder reportBuilder) {
		return new StreamSource() {
			@Override
			public InputStream getStream() {
				ByteArrayOutputStream output = new ByteArrayOutputStream();
				try {
					reportBuilder.build(output);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				return new ByteArrayInputStream(output.toByteArray());
			}
		};
	}

	public static class DownloadStreamResource extends StreamResource {
		public static String PDF_MIMETYPE = "application/pdf";
		public static String ZIP_MIMETYPE = "application/zip";
		public static String EXCEL_MIMETYPE = "application/vnd.ms-excel";

		public DownloadStreamResource(StreamSource source, String filename) {
			this(source, filename, getMimeTypeFromFileName(filename));
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

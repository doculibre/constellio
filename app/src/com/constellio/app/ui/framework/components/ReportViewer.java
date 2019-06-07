package com.constellio.app.ui.framework.components;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.framework.reports.NewReportWriterFactory;
import com.constellio.app.ui.framework.reports.ReportWriter;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.utils.MimeTypes;
import com.vaadin.server.DownloadStream;
import com.vaadin.server.StreamResource;
import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Link;
import com.vaadin.ui.VerticalLayout;
import org.apache.commons.lang.StringUtils;

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
				new DownloadStreamResource(source, filename, getMimeTypeFromFileName(filename)));

		addComponents(download, viewer);
		setExpandRatio(viewer, 1);
		setWidth("100%");
		setHeight("100%");
	}

	static String getMimeTypeFromFileName(String filename) {
		if (StringUtils.isBlank(filename)) {
			return DownloadStreamResource.PDF_MIMETYPE;
		} else {
			String extension = StringUtils.substringAfterLast(filename, ".").toLowerCase();
			if (StringUtils.isBlank(extension)) {
				return DownloadStreamResource.PDF_MIMETYPE;
			}
			return MimeTypes.lookupMimeType(extension);
		}
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

	public static class DownloadStreamResource extends StreamResource {
		public static String PDF_MIMETYPE = "application/pdf";
		public static String ZIP_MIMETYPE = "application/zip";
		public static String EXCEL_MIMETYPE = "application/vnd.ms-excel";
		public static String SPREADSHEET_MIMETYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

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

package com.constellio.app.ui.framework.components;

import com.constellio.data.io.services.facades.IOServices;
import com.vaadin.server.DownloadStream;
import com.vaadin.server.Page;
import com.vaadin.server.StreamResource;
import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.ui.BrowserFrame;
import com.vaadin.ui.Link;
import com.vaadin.ui.VerticalLayout;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import java.io.*;

import static com.constellio.app.ui.i18n.i18n.$;

public class LabelViewer extends VerticalLayout {

	public LabelViewer(File PDF, String filename, IOServices ioServices) {
		addStyleName("no-scroll");
		InputStream inputStream = null;
		try {
			inputStream = new FileInputStream(PDF);
			byte[] PDFbytes = IOUtils.toByteArray(inputStream);
			StreamSource source = buildSource(PDFbytes);
			BrowserFrame viewer = new BrowserFrame();
			viewer.setSource(new StreamResource(source, filename));

			viewer.setWidth("100%");
			//            viewer.setHeight("900px");
			int adjustedHeight = Page.getCurrent().getBrowserWindowHeight() - 200;
			viewer.setHeight(adjustedHeight + "px");

			Link download = new Link($("ReportViewer.download", filename),
					new DownloadStreamResource(source, filename, getMimeTypeFromFileName(filename)));

			addComponents(download, viewer);
			setWidth("100%");
			setHeight("100%");
			setExpandRatio(viewer, 1);

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			ioServices.closeQuietly(inputStream);
			ioServices.deleteQuietly(PDF);
		}
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

	private StreamSource buildSource(final byte[] PDF) {
		return new StreamSource() {
			@Override
			public InputStream getStream() {
				try {
					return new ByteArrayInputStream(PDF);
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
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

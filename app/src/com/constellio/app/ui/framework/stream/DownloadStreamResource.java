package com.constellio.app.ui.framework.stream;

import com.constellio.model.utils.MimeTypes;
import com.vaadin.server.DownloadStream;
import com.vaadin.server.StreamResource;
import org.apache.commons.lang.StringUtils;

public class DownloadStreamResource extends StreamResource {
	public static String PDF_MIMETYPE = "application/pdf";
	public static String ZIP_MIMETYPE = "application/zip";
	public static String EXCEL_MIMETYPE = "application/vnd.ms-excel";
	public static String SPREADSHEET_MIMETYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

	public DownloadStreamResource(StreamSource source, String filename) {
		this(source, filename, getMimeTypeFromFileName(filename));
	}

	public DownloadStreamResource(StreamSource source, String filename, String MIMEType) {
		super(source, filename);
		setCacheTime(0);
		setMIMEType(MIMEType);
	}

	@Override
	public DownloadStream getStream() {
		DownloadStream stream = super.getStream();
		stream.setParameter("Content-Disposition", "attachment; filename=" + getFilename());
		return stream;
	}

	public static String getMimeTypeFromFileName(String filename) {
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
}

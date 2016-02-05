package com.constellio.app.ui.framework.components;

import static com.constellio.app.ui.framework.components.ReportViewer.DownloadStreamResource.EXCEL_MIMETYPE;
import static com.constellio.app.ui.framework.components.ReportViewer.DownloadStreamResource.PDF_MIMETYPE;
import static com.constellio.app.ui.framework.components.ReportViewer.DownloadStreamResource.ZIP_MIMETYPE;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class ReportViewerTest {
	@Test
	public void givenExcelFileNameWhenGetMimeThenExcel()
			throws Exception {
		assertThat(ReportViewer.getMimeTypeFromFileName("lo l.xls")).isEqualTo(EXCEL_MIMETYPE);
		assertThat(ReportViewer.getMimeTypeFromFileName("lol.xlsX")).isEqualTo(EXCEL_MIMETYPE);
	}

	@Test
	public void givenPdfFileNameWhenGetMimeThenPdf()
			throws Exception {
		assertThat(ReportViewer.getMimeTypeFromFileName("lol.pdf")).isEqualTo(PDF_MIMETYPE);
	}

	@Test
	public void givenZipFileNameWhenGetMimeThenZip()
			throws Exception {
		assertThat(ReportViewer.getMimeTypeFromFileName("lol.Zip")).isEqualTo(ZIP_MIMETYPE);
	}

	@Test
	public void givenFileNameWithoutExtensionWhenGetMimeThenPdf()
			throws Exception {
		assertThat(ReportViewer.getMimeTypeFromFileName("lol")).isEqualTo(PDF_MIMETYPE);
		assertThat(ReportViewer.getMimeTypeFromFileName("")).isEqualTo(PDF_MIMETYPE);
		assertThat(ReportViewer.getMimeTypeFromFileName(null)).isEqualTo(PDF_MIMETYPE);
	}
}

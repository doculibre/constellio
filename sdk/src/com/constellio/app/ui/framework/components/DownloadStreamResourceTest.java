package com.constellio.app.ui.framework.components;

import com.constellio.app.ui.framework.stream.DownloadStreamResource;
import org.junit.Test;

import static com.constellio.app.ui.framework.stream.DownloadStreamResource.EXCEL_MIMETYPE;
import static com.constellio.app.ui.framework.stream.DownloadStreamResource.PDF_MIMETYPE;
import static com.constellio.app.ui.framework.stream.DownloadStreamResource.SPREADSHEET_MIMETYPE;
import static com.constellio.app.ui.framework.stream.DownloadStreamResource.ZIP_MIMETYPE;
import static org.assertj.core.api.Assertions.assertThat;

public class DownloadStreamResourceTest {
	@Test
	public void givenExcelFileNameWhenGetMimeThenExcel()
			throws Exception {
		assertThat(DownloadStreamResource.getMimeTypeFromFileName("lo l.xls")).isEqualTo(EXCEL_MIMETYPE);
		assertThat(DownloadStreamResource.getMimeTypeFromFileName("lo l.xlsx")).isEqualTo(SPREADSHEET_MIMETYPE);
		assertThat(DownloadStreamResource.getMimeTypeFromFileName("lol.xlsX")).isEqualTo(SPREADSHEET_MIMETYPE);
	}

	@Test
	public void givenPdfFileNameWhenGetMimeThenPdf()
			throws Exception {
		assertThat(DownloadStreamResource.getMimeTypeFromFileName("lol.pdf")).isEqualTo(PDF_MIMETYPE);
	}

	@Test
	public void givenZipFileNameWhenGetMimeThenZip()
			throws Exception {
		assertThat(DownloadStreamResource.getMimeTypeFromFileName("lol.Zip")).isEqualTo(ZIP_MIMETYPE);
	}

	@Test
	public void givenFileNameWithoutExtensionWhenGetMimeThenPdf()
			throws Exception {
		assertThat(DownloadStreamResource.getMimeTypeFromFileName("lol")).isEqualTo(PDF_MIMETYPE);
		assertThat(DownloadStreamResource.getMimeTypeFromFileName("")).isEqualTo(PDF_MIMETYPE);
		assertThat(DownloadStreamResource.getMimeTypeFromFileName(null)).isEqualTo(PDF_MIMETYPE);
	}
}

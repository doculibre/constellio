package com.constellio.data.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.collections.BufferUnderflowException;
import org.apache.commons.io.IOUtils;
import org.apache.poi.hpsf.MarkUnsupportedException;
import org.apache.poi.hpsf.NoPropertySetStreamException;
import org.apache.poi.hpsf.UnexpectedPropertySetTypeException;
import org.apache.poi.hpsf.WritingNotSupportedException;
import org.apache.tika.mime.MimeTypeException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.constellio.data.io.streamFactories.StreamFactory;
import com.constellio.data.utils.OfficeDocumentsServicesException.CannotReadDocumentsProperties;
import com.constellio.data.utils.OfficeDocumentsServicesException.NotCompatibleExtension;
import com.constellio.data.utils.OfficeDocumentsServicesException.PropertyDoesntExist;
import com.constellio.data.utils.OfficeDocumentsServicesException.RTFFileIsNotCompatible;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.SlowTest;

@SlowTest
public class OfficeDocumentsServicesRealTest extends ConstellioTest {
	OfficeDocumentsServices officeDocumentsServices;

	File tempFile;

	@Before
	public void setUp() {
		officeDocumentsServices = new OfficeDocumentsServices();
		tempFile = new File(newTempFolder(), "tmpFile");
	}

	@Test
	public void whenGetPropertyOfNonEmptyWordFileThenReturnTheRightProperty()
			throws Exception {
		StreamFactory<InputStream> inputStream = getTestResourceInputStreamFactory("NonEmptyWordFile.doc");
		assertThat(officeDocumentsServices.getProperty(inputStream, "id_igid", "doc")).isEqualTo("1476047023");
	}

	@Test(expected = OfficeDocumentsServicesException.PropertyDoesntExist.class)
	public void whenGetPropertyOfEmptyWordFileThenException()
			throws CannotReadDocumentsProperties, IOException, PropertyDoesntExist, NotCompatibleExtension,
			RTFFileIsNotCompatible {
		StreamFactory<InputStream> inputStream = getTestResourceInputStreamFactory("EmptyWordFile.doc");
		officeDocumentsServices.getProperty(inputStream, "id_igid", "doc");
	}

	@Test
	public void whenGetPropertyOfNonEmptyExcelFileThenReturnTheRightProperty()
			throws Exception {
		StreamFactory<InputStream> inputStream = getTestResourceInputStreamFactory("NonEmptyExcelFile.xls");
		assertThat(officeDocumentsServices.getProperty(inputStream, "id_igid", "xls")).isEqualTo("1476047023");
	}

	@Test(expected = OfficeDocumentsServicesException.PropertyDoesntExist.class)
	public void whenGetPropertyOfEmptyExcelFileThenException()
			throws CannotReadDocumentsProperties, IOException, PropertyDoesntExist, NotCompatibleExtension,
			RTFFileIsNotCompatible {
		StreamFactory<InputStream> inputStream = getTestResourceInputStreamFactory("EmptyExcelFile.xls");
		officeDocumentsServices.getProperty(inputStream, "id_igid", "xls");
	}

	@Test
	public void whenGetPropertyOfNonEmptyDiapoFileThenReturnTheRightProperty()
			throws Exception {
		StreamFactory<InputStream> inputStream = getTestResourceInputStreamFactory("NonEmptyPowerPointFile.ppt");
		assertThat(officeDocumentsServices.getProperty(inputStream, "id_igid", "ppt")).isEqualTo("1476047023");
	}

	@Test(expected = OfficeDocumentsServicesException.PropertyDoesntExist.class)
	public void whenGetPropertyOfEmptyPowerPointFileThenException()
			throws CannotReadDocumentsProperties, IOException, PropertyDoesntExist, NotCompatibleExtension,
			RTFFileIsNotCompatible {
		StreamFactory<InputStream> inputStream = getTestResourceInputStreamFactory("EmptyPowerPointFile.ppt");

		officeDocumentsServices.getProperty(inputStream, "id_igid", "ppt");
	}

	@Test
	public void whenSetPropertyOfXLSFileThenNewProperty()
			throws NoPropertySetStreamException, MarkUnsupportedException, UnexpectedPropertySetTypeException, IOException,
			CannotReadDocumentsProperties, PropertyDoesntExist, NotCompatibleExtension, WritingNotSupportedException,
			MimeTypeException, RTFFileIsNotCompatible {
		StreamFactory<InputStream> inputStream = getTestResourceInputStreamFactory("EmptyExcelFile.xls");
		StreamFactory<OutputStream> outputStream = getTestResourceOutputStreamFactory(tempFile);

		officeDocumentsServices.setProperty(inputStream, outputStream, "malik", "1234", "xls");
		assertThat(officeDocumentsServices.getProperty(getTestResourceInputStreamFactory(tempFile), "malik", "xls")).isEqualTo(
				"1234");
	}

	@Test
	public void whenSetPropertyOfPptFileThenNewProperty()
			throws NoPropertySetStreamException, MarkUnsupportedException, UnexpectedPropertySetTypeException, IOException,
			CannotReadDocumentsProperties, PropertyDoesntExist, NotCompatibleExtension, WritingNotSupportedException,
			MimeTypeException, RTFFileIsNotCompatible {
		StreamFactory<InputStream> inputStream = getTestResourceInputStreamFactory("EmptyPowerPointFile.ppt");
		StreamFactory<OutputStream> outputStream = getTestResourceOutputStreamFactory(tempFile);

		officeDocumentsServices.setProperty(inputStream, outputStream, "malik", "1234", "ppt");
		assertThat(officeDocumentsServices.getProperty(getTestResourceInputStreamFactory(tempFile), "malik", "ppt")).isEqualTo(
				"1234");
	}

	@Test
	public void whenSetPropertyOfWordFileThenNewProperty()
			throws NoPropertySetStreamException, MarkUnsupportedException, UnexpectedPropertySetTypeException, IOException,
			CannotReadDocumentsProperties, PropertyDoesntExist, NotCompatibleExtension, WritingNotSupportedException,
			MimeTypeException, RTFFileIsNotCompatible {
		StreamFactory<InputStream> inputStream = getTestResourceInputStreamFactory("EmptyWordFile.doc");
		StreamFactory<OutputStream> outputStream = getTestResourceOutputStreamFactory(tempFile);

		officeDocumentsServices.setProperty(inputStream, outputStream, "malik", "1234", "doc");
		assertThat(officeDocumentsServices.getProperty(getTestResourceInputStreamFactory(tempFile), "malik", "doc")).isEqualTo(
				"1234");
	}

	@Test
	public void whenGetPropertyOfNonEmptyExcelFile2007ThenReturnTheRightProperty()
			throws Exception {
		StreamFactory<InputStream> inputStream = getTestResourceInputStreamFactory("NonEmptyExcelFile2007.xlsx");
		assertThat(officeDocumentsServices.getProperty(inputStream, "id_igid", "xlsx")).isEqualTo("1476047023");
	}

	@Test
	public void whenGetPropertyOfNonEmptyPowerPointFile2007ThenReturnTheRightProperty()
			throws Exception {
		StreamFactory<InputStream> inputStream = getTestResourceInputStreamFactory("NonEmptyPowerPointFile2007.pptx");
		assertThat(officeDocumentsServices.getProperty(inputStream, "id_igid", "pptx")).isEqualTo("1476047023");
	}

	@Test
	public void whenGetPropertyOfNonEmptyWordFile2007ThenReturnTheRightProperty()
			throws Exception {
		StreamFactory<InputStream> inputStream = getTestResourceInputStreamFactory("NonEmptyWordFile2007.docx");
		assertThat(officeDocumentsServices.getProperty(inputStream, "id_igid", "docx")).isEqualTo("1476047023");
	}

	@Test(expected = OfficeDocumentsServicesException.PropertyDoesntExist.class)
	public void whenGetPropertyOfEmptyExcelFile2007ThenException()
			throws Exception {
		StreamFactory<InputStream> inputStream = getTestResourceInputStreamFactory("EmptyExcelFile2007.xlsx");
		officeDocumentsServices.getProperty(inputStream, "id_igid", "xlsx");
	}

	@Test(expected = OfficeDocumentsServicesException.PropertyDoesntExist.class)
	public void whenGetPropertyOfEmptyPowerPointFile2007ThenException()
			throws Exception {
		StreamFactory<InputStream> inputStream = getTestResourceInputStreamFactory("EmptyPowerPointFile2007.pptx");
		officeDocumentsServices.getProperty(inputStream, "id_igid", "pptx");
	}

	@Test(expected = OfficeDocumentsServicesException.PropertyDoesntExist.class)
	public void whenGetPropertyOfEmptyWordFile2007ThenException()
			throws Exception {
		StreamFactory<InputStream> inputStream = getTestResourceInputStreamFactory("EmptyWordFile2007.docx");
		officeDocumentsServices.getProperty(inputStream, "id_igid", "docx");
	}

	@Test
	public void whenSetPropertyOfWord2007FileThenRightPropertyExists()
			throws WritingNotSupportedException, NotCompatibleExtension, CannotReadDocumentsProperties, PropertyDoesntExist,
			IOException, MimeTypeException, RTFFileIsNotCompatible {

		StreamFactory<InputStream> inputStream = getTestResourceInputStreamFactory("NonEmptyWordFile2007.docx");
		StreamFactory<OutputStream> outputStream = getTestResourceOutputStreamFactory(tempFile);
		officeDocumentsServices.setProperty(inputStream, outputStream, "malik", "12345", "docx");

		assertThat(officeDocumentsServices.getProperty(getTestResourceInputStreamFactory(tempFile), "malik", "docx")).isEqualTo(
				"12345");
	}

	@Test
	public void whenSetPropertyOfPowerPointFile2007ThenRightPropertyExists()
			throws WritingNotSupportedException, NotCompatibleExtension, CannotReadDocumentsProperties, PropertyDoesntExist,
			IOException, MimeTypeException, RTFFileIsNotCompatible {

		StreamFactory<InputStream> inputStream = getTestResourceInputStreamFactory("NonEmptyPowerPointFile2007.pptx");
		StreamFactory<OutputStream> outputStream = getTestResourceOutputStreamFactory(tempFile);
		officeDocumentsServices.setProperty(inputStream, outputStream, "malik", "12345", "pptx");

		assertThat(officeDocumentsServices.getProperty(getTestResourceInputStreamFactory(tempFile), "malik", "pptx")).isEqualTo(
				"12345");
	}

	@Test
	public void whenSetPropertyOfExcelFile2007ThenRightPropertyExists()
			throws WritingNotSupportedException, NotCompatibleExtension, CannotReadDocumentsProperties, PropertyDoesntExist,
			IOException, MimeTypeException, RTFFileIsNotCompatible {

		StreamFactory<InputStream> inputStream = getTestResourceInputStreamFactory("NonEmptyExcelFile2007.xlsx");
		StreamFactory<OutputStream> outputStream = getTestResourceOutputStreamFactory(tempFile);
		officeDocumentsServices.setProperty(inputStream, outputStream, "malik", "12345", "xlsx");

		assertThat(officeDocumentsServices.getProperty(getTestResourceInputStreamFactory(tempFile), "malik", "xlsx")).isEqualTo(
				"12345");
	}

	@Test
	public void whenGetPropertyOfDocxDocNamedDocThenReturnRightProperty()
			throws CannotReadDocumentsProperties, PropertyDoesntExist, NotCompatibleExtension, IOException,
			RTFFileIsNotCompatible {
		StreamFactory<InputStream> inputStream = getTestResourceInputStreamFactory("DocumentDocxNamedDoc.doc");
		assertThat(officeDocumentsServices.getProperty(inputStream, "id_igid", "doc")).isEqualTo("1476047023");
	}

	@Test
	public void whenSetPropertyOfDocxDocNamedDocThenRightPropertyExists()
			throws WritingNotSupportedException, NotCompatibleExtension, CannotReadDocumentsProperties, PropertyDoesntExist,
			IOException, MimeTypeException, RTFFileIsNotCompatible {

		StreamFactory<InputStream> inputStream = getTestResourceInputStreamFactory("DocumentDocxNamedDoc.doc");
		StreamFactory<OutputStream> outputStream = getTestResourceOutputStreamFactory(tempFile);
		officeDocumentsServices.setProperty(inputStream, outputStream, "malik", "12345", "doc");

		assertThat(officeDocumentsServices.getProperty(getTestResourceInputStreamFactory(tempFile), "malik", "doc")).isEqualTo(
				"12345");
	}

	@Test
	public void whenGetPropertyOfDocDocNamedDocxThenReturnRightProperty()
			throws CannotReadDocumentsProperties, PropertyDoesntExist, NotCompatibleExtension, IOException,
			RTFFileIsNotCompatible {
		StreamFactory<InputStream> inputStream = getTestResourceInputStreamFactory("DocumentDocNamedDocx.docx");
		assertThat(officeDocumentsServices.getProperty(inputStream, "id_igid", "docx")).isEqualTo("1476047023");
	}

	@Test
	public void whenSetPropertyOfDocDocNamedDocxThenRightPropertyExists()
			throws WritingNotSupportedException, NotCompatibleExtension, CannotReadDocumentsProperties, PropertyDoesntExist,
			IOException, MimeTypeException, RTFFileIsNotCompatible {

		StreamFactory<InputStream> inputStream = getTestResourceInputStreamFactory("DocumentDocxNamedDoc.doc");
		StreamFactory<OutputStream> outputStream = getTestResourceOutputStreamFactory(tempFile);
		officeDocumentsServices.setProperty(inputStream, outputStream, "malik", "12345", "docx");

		assertThat(officeDocumentsServices.getProperty(getTestResourceInputStreamFactory(tempFile), "malik", "doc")).isEqualTo(
				"12345");
	}

	@Test
	public void whenGetPropertyOfXlsDocNamedXlsxThenReturnRightProperty()
			throws CannotReadDocumentsProperties, PropertyDoesntExist, NotCompatibleExtension, IOException,
			RTFFileIsNotCompatible {
		StreamFactory<InputStream> inputStream = getTestResourceInputStreamFactory("DocumentXlsNamedXlsx.xlsx");
		assertThat(officeDocumentsServices.getProperty(inputStream, "id_igid", "xlsx")).isEqualTo("1476047023");
	}

	@Test
	public void whenSetPropertyOfXlsDocNamedXlsxThenRightPropertyExists()
			throws WritingNotSupportedException, NotCompatibleExtension, CannotReadDocumentsProperties, PropertyDoesntExist,
			IOException, MimeTypeException, RTFFileIsNotCompatible {

		StreamFactory<InputStream> inputStream = getTestResourceInputStreamFactory("DocumentXlsNamedXlsx.xlsx");
		StreamFactory<OutputStream> outputStream = getTestResourceOutputStreamFactory(tempFile);
		officeDocumentsServices.setProperty(inputStream, outputStream, "malik", "12345", "xlsx");

		assertThat(officeDocumentsServices.getProperty(getTestResourceInputStreamFactory(tempFile), "malik", "xlsx")).isEqualTo(
				"12345");
	}

	@Test
	public void whenGetPropertyOfXlsxDocNamedXlsThenReturnRightProperty()
			throws CannotReadDocumentsProperties, PropertyDoesntExist, NotCompatibleExtension, IOException,
			RTFFileIsNotCompatible {
		StreamFactory<InputStream> inputStream = getTestResourceInputStreamFactory("DocumentXlsxNamedXls.xls");
		assertThat(officeDocumentsServices.getProperty(inputStream, "id_igid", "xls")).isEqualTo("1476047023");
	}

	@Test
	public void whenSetPropertyOfXlsxDocNamedXlsThenRightPropertyExists()
			throws WritingNotSupportedException, NotCompatibleExtension, CannotReadDocumentsProperties, PropertyDoesntExist,
			IOException, MimeTypeException, RTFFileIsNotCompatible {

		StreamFactory<InputStream> inputStream = getTestResourceInputStreamFactory("DocumentXlsxNamedXls.xls");
		StreamFactory<OutputStream> outputStream = getTestResourceOutputStreamFactory(tempFile);
		officeDocumentsServices.setProperty(inputStream, outputStream, "malik", "12345", "xls");

		assertThat(officeDocumentsServices.getProperty(getTestResourceInputStreamFactory(tempFile), "malik", "xls")).isEqualTo(
				"12345");
	}

	@Test
	public void whenGetPropertyOfPptxDocNamedPptThenReturnRightProperty()
			throws CannotReadDocumentsProperties, PropertyDoesntExist, NotCompatibleExtension, IOException,
			RTFFileIsNotCompatible {
		StreamFactory<InputStream> inputStream = getTestResourceInputStreamFactory("DocumentPptxNamedPpt.ppt");
		assertThat(officeDocumentsServices.getProperty(inputStream, "id_igid", "ppt")).isEqualTo("1476047023");
	}

	@Test
	public void whenSetPropertyOfPptxDocNamedPptThenRightPropertyExists()
			throws WritingNotSupportedException, NotCompatibleExtension, CannotReadDocumentsProperties, PropertyDoesntExist,
			IOException, MimeTypeException, RTFFileIsNotCompatible {

		StreamFactory<InputStream> inputStream = getTestResourceInputStreamFactory("DocumentPptxNamedPpt.ppt");
		StreamFactory<OutputStream> outputStream = getTestResourceOutputStreamFactory(tempFile);
		officeDocumentsServices.setProperty(inputStream, outputStream, "malik", "12345", "ppt");

		assertThat(officeDocumentsServices.getProperty(getTestResourceInputStreamFactory(tempFile), "malik", "ppt")).isEqualTo(
				"12345");
	}

	@Test
	public void whenGetPropertyOfPptDocNamedPptxThenReturnRightProperty()
			throws CannotReadDocumentsProperties, PropertyDoesntExist, NotCompatibleExtension, IOException,
			RTFFileIsNotCompatible {
		StreamFactory<InputStream> inputStream = getTestResourceInputStreamFactory("DocumentPptNamedPptx.pptx");
		assertThat(officeDocumentsServices.getProperty(inputStream, "id_igid", "pptx")).isEqualTo("1476047023");
	}

	@Test
	public void whenSetPropertyOfPptDocNamedPptxThenRightPropertyExists()
			throws WritingNotSupportedException, NotCompatibleExtension, CannotReadDocumentsProperties, PropertyDoesntExist,
			MimeTypeException, IOException, RTFFileIsNotCompatible {

		StreamFactory<InputStream> inputStream = getTestResourceInputStreamFactory("DocumentPptNamedPptx.pptx");
		StreamFactory<OutputStream> outputStream = getTestResourceOutputStreamFactory(tempFile);
		officeDocumentsServices.setProperty(inputStream, outputStream, "malik", "12345", "pptx");

		assertThat(officeDocumentsServices.getProperty(getTestResourceInputStreamFactory(tempFile), "malik", "pptx")).isEqualTo(
				"12345");
	}

	@Test(expected = OfficeDocumentsServicesException.RTFFileIsNotCompatible.class)
	public void whenGetPropertyOfRTFDocNamedDocThenException()
			throws CannotReadDocumentsProperties, PropertyDoesntExist, NotCompatibleExtension, IOException,
			RTFFileIsNotCompatible {
		StreamFactory<InputStream> inputStream = getTestResourceInputStreamFactory("DocumentRTFNamedDoc.doc");
		officeDocumentsServices.getProperty(inputStream, "tartanpion", "tartanpion");
	}

	@Test(expected = OfficeDocumentsServicesException.RTFFileIsNotCompatible.class)
	public void whenSetPropertyOfRTFDocNamedDocThenException()
			throws CannotReadDocumentsProperties, PropertyDoesntExist, NotCompatibleExtension, IOException,
			WritingNotSupportedException, MimeTypeException, RTFFileIsNotCompatible {
		StreamFactory<InputStream> inputStream = getTestResourceInputStreamFactory("DocumentRTFNamedDoc.doc");
		StreamFactory<OutputStream> outputStream = getTestResourceOutputStreamFactory(tempFile);

		officeDocumentsServices.setProperty(inputStream, outputStream, "malik", "12345", "doc");
	}

	@Test
	public void whenGetPropertyOfFirstByteOfFileThenException()
			throws IOException, CannotReadDocumentsProperties, PropertyDoesntExist, NotCompatibleExtension,
			RTFFileIsNotCompatible {
		StreamFactory<InputStream> inputStream = getTestResourceInputStreamFactory("DocumentWithALotOfBytes.doc");
		byte[] arrayByte = new byte[1000];
		IOUtils.read(inputStream.create(SDK_STREAM), arrayByte);

		inputStream = getTestResourceInputStreamFactory(arrayByte);


		try {
			officeDocumentsServices.getProperty(inputStream, "property", "ppt");
			Assert.fail("Exception expected");
		} catch(java.nio.BufferUnderflowException | IOException e) {
			//OK
		}
	}
}

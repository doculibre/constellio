package com.constellio.model.services.pdftron;

import com.constellio.model.services.pdftron.PdfTronXMLException.PdfTronXMLException_CannotEditOtherUsersAnnoations;
import com.constellio.sdk.tests.ConstellioTest;
import org.apache.commons.io.IOUtils;
import org.jdom2.Element;
import org.jdom2.output.XMLOutputter;
import org.junit.Test;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import static com.constellio.model.services.pdftron.PdfTronXMLService.areElementEqual;
import static org.assertj.core.api.Assertions.assertThat;

public class PdfTronXMLServiceAcceptanceTest extends ConstellioTest {

	@Test
	public void areEqualTest() {
		String str1null = null;
		String str2null = null;

		assertThat(PdfTronXMLService.areEqual(str1null, str2null)).isTrue();

		String str1NotNull = "notnull";

		assertThat(PdfTronXMLService.areEqual(str1NotNull, str2null)).isFalse();

		String str2NotNull = "notnull";

		assertThat(PdfTronXMLService.areEqual(str1null, str2NotNull)).isFalse();

		assertThat(PdfTronXMLService.areEqual(str1NotNull, str2NotNull)).isTrue();

		String str1NotEqual = "notequal1";

		assertThat(PdfTronXMLService.areEqual(str1NotEqual, str2NotNull)).isFalse();

		String str2NotEqual = "noteequal2";

		assertThat(PdfTronXMLService.areEqual(str1NotNull, str2NotEqual)).isFalse();
	}

	@Test
	public void areElementEqualsNullTest() {
		Element element1 = new Element("element1");
		element1.setAttribute("attribute1", "value1");

		Element element1Copy = new Element("element1");
		element1Copy.setAttribute("attribute1", "value1");

		assertThat(areElementEqual(null, null)).isTrue();

		assertThat(areElementEqual(element1, null)).isFalse();

		assertThat(areElementEqual(null, element1Copy)).isFalse();

		assertThat(areElementEqual(element1, element1Copy)).isTrue();
	}


	@Test
	public void areElementEqualsAttributesTest() {
		Element element1 = new Element("element1");
		element1.setAttribute("attribute1", "value1");
		element1.setAttribute("attribute2", "value2");

		Element element2 = new Element("element1");

		assertThat(areElementEqual(element1, element2)).isFalse();

		element2.setAttribute("attribute2", "value2");
		element2.setAttribute("attribute1", "value1");

		assertThat(areElementEqual(element1, element2)).isTrue();

		element2.setAttribute("attribute2", "value3");

		assertThat(areElementEqual(element1, element2)).isFalse();

		element2.removeAttribute("attribute2");
		element2.setAttribute("attribute3", "value4");

		assertThat(areElementEqual(element1, element2)).isFalse();

		element2.setAttribute("attribute2", "value2");

		assertThat(areElementEqual(element1, element2)).isFalse();
	}


	@Test
	public void areElementEqualsTextTest() {
		Element element1 = new Element("element1");
		element1.setText("text1");
		Element element2 = new Element("element1");
		element2.setText("text1");

		assertThat(areElementEqual(element1, element2)).isTrue();

		element2.setText("text2");

		assertThat(areElementEqual(element1, element2)).isFalse();

		element2.setText(null);

		assertThat(areElementEqual(element1, element2)).isFalse();
		assertThat(areElementEqual(element2, element1)).isFalse();

		element1.setText(null);

		assertThat(areElementEqual(element1, element2)).isTrue();
		assertThat(areElementEqual(element2, element1)).isTrue();
	}


	@Test
	public void areElementEqualsChildrenTest() {
		Element parent1 = new Element("parent1");
		Element child1OfP1 = new Element("child1").setText("text");
		Element child2OfP1 = new Element("child2").setText("text");
		Element child3OfP1 = new Element("child1").setText("text");

		parent1.addContent(child1OfP1);
		parent1.addContent(child2OfP1);
		parent1.addContent(child3OfP1);

		Element parent2 = new Element("parent1");
		Element child3OfP2 = new Element("child1").setText("text");
		Element child1OfP2 = new Element("child1").setText("text");
		Element child2OfP2 = new Element("child2").setText("text");


		parent2.addContent(child1OfP2);
		parent2.addContent(child2OfP2);
		parent2.addContent(child3OfP2);

		assertThat(areElementEqual(parent1, parent2)).isTrue();

		child2OfP2.setName("child1");

		System.out.println(new XMLOutputter().outputString(parent2));

		assertThat(areElementEqual(parent1, parent2)).isFalse();

		child2OfP1.setName("child1");

		assertThat(areElementEqual(parent1, parent2)).isTrue();

		// Restaure to original state
		child2OfP2.setName("child2");
		child2OfP1.setName("child2");
		assertThat(areElementEqual(parent1, parent2)).isTrue();

		child3OfP2.setText("notEqual");
		assertThat(areElementEqual(parent1, parent2)).isFalse();

		child3OfP2.setText("text");
		assertThat(areElementEqual(parent1, parent2)).isTrue();

		Element childOfChildP3P2 = child3OfP2.addContent(new Element("ChildOfChildElement"));

		assertThat(areElementEqual(parent1, parent2)).isFalse();

		Element childOfChildP3P1 = child3OfP1.addContent(new Element("ChildOfChildElement"));

		assertThat(areElementEqual(parent1, parent2)).isTrue();

		childOfChildP3P1.setAttribute("extraAttributeInChildOfChildP3P1", "randomvalue");

		assertThat(areElementEqual(parent1, parent2)).isFalse();
	}

	@Test
	public void whenProcessValidChangeThenOk() throws Exception {
		processNewXmlTest("originalannoations.xml", "annotation-modification1.xml", false, "00000000072");
	}

	@Test
	public void whenProcessModificationOfAminChangingOtherUserAnnotationsThenOk() throws Exception {
		processNewXmlTest("originalannoations.xml", "annotation-modification1.xml", true, "00000000068");
	}

	@Test(expected = PdfTronXMLException_CannotEditOtherUsersAnnoations.class)
	public void whenProcessModificationOfUserChangingOtherUserAnnotationsThenThrow() throws Exception {
		processNewXmlTest("originalannoations.xml", "annotation-modification1.xml", false, "00000000068");
	}

	@Test
	public void whenProcessNoModificationThenOk() throws Exception {
		processNewXmlTest("originalannoations.xml", "originalannoations.xml", false, "00000000068");
	}

	public void processNewXmlTest(String originalAnnotationsXml, String modifiedAnnotationsXml,
								  boolean canUserChangeOtherAnnotation, String userId)
			throws Exception {
		File originalAnnotationsFile = getTestResourceFileWithoutCheckingIfUnitTest(this.getClass(), originalAnnotationsXml);
		File modifiedAnnotationsFile = getTestResourceFileWithoutCheckingIfUnitTest(this.getClass(), modifiedAnnotationsXml);

		InputStream originalAnnotationInputStream = null;
		InputStream modifiedAnnotationValidInputStream = null;

		try {
			originalAnnotationInputStream = new FileInputStream(originalAnnotationsFile);
			modifiedAnnotationValidInputStream = new FileInputStream(modifiedAnnotationsFile);

			String originalAnnoations = IOUtils.toString(originalAnnotationInputStream, "UTF-8");
			String modifiedAnnotations = IOUtils.toString(modifiedAnnotationValidInputStream, "UTF-8");

			PdfTronXMLService pdfTronXMLService = new PdfTronXMLService();

			String newXml = pdfTronXMLService.processNewXML(originalAnnoations, modifiedAnnotations, canUserChangeOtherAnnotation, userId);
			assertThat(newXml.equals(modifiedAnnotations));
		} finally {
			closeQuietly(originalAnnotationInputStream);
			closeQuietly(modifiedAnnotationValidInputStream);
		}
	}

	private void closeQuietly(Closeable closeable) throws IOException {
		if (closeable != null) {
			closeable.close();
		}
	}
}

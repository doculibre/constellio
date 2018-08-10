package com.constellio.model.services.thesaurus.util;

import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SkosUtilAcceptanceTest extends ConstellioTest {
	public static final String NOT_NORMALIZED_STRING = "  Hello  1234 " +
													   "\n    fdsfdsfds fdsfds fdsdsf " +
													   "\nfdskg gfdf lhgflhg  ";
	public static final String NORMALIZED_STRING_RESULT = " HELLO 1234 FDSFDSFDS FDSFDS FDSDSF FDSKG GFDF LHGFLHG ";
	public static final String URL_WITH_ID = "http://www.thesaurus.gouv.qc.ca/tag/terme.do?id=147";

	@Test
	public void givenNotNormalizedStringThenNormalize() {
		assertThat(SkosUtil.normaliseTextForMatching(NOT_NORMALIZED_STRING)).isEqualTo(NORMALIZED_STRING_RESULT);
	}

	@Test
	public void givenURLWithIdFindId() {
		assertThat(SkosUtil.getSkosConceptId(URL_WITH_ID)).isEqualTo(147 + "");
	}
}

package com.constellio.app.modules.rm.model.calculators;

import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.ContentVersion;
import com.constellio.model.entities.records.wrappers.UserDocument;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class UserDocumentContentSizeCalculatorAcceptanceTest extends ConstellioTest {

	@Mock private CalculatorParameters parameters;
	@Mock private Content content;
	@Mock private ContentVersion contentVersion;

	private UserDocumentContentSizeCalculator calculator;
	private LocalDependency<Content> contentParam = LocalDependency.toAContent(UserDocument.CONTENT);

	@Before
	public void setUp()
			throws Exception {
		calculator = new UserDocumentContentSizeCalculator();

		when(content.getCurrentVersion()).thenReturn(contentVersion);
		when(contentVersion.getLength()).thenReturn(1000000L);
	}

	@Test
	public void givenContentIsNullThenReturnZero() {
		when(parameters.get(contentParam)).thenReturn(null);

		assertThat(calculator.calculate(parameters)).isEqualTo(0L);
	}

	@Test
	public void givenContentIsNotNullThenReturnCorrectSize() {
		when(parameters.get(contentParam)).thenReturn(content);

		assertThat(calculator.calculate(parameters)).isEqualTo(1000000L);
	}

}

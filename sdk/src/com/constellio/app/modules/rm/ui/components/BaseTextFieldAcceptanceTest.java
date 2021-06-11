package com.constellio.app.modules.rm.ui.components;

import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.constellio.sdk.tests.ConstellioTest;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class BaseTextFieldAcceptanceTest extends ConstellioTest {

	@Test
	public void givenPotentialXssScriptBaseTextFieldEscapeBrackets() {
		final BaseTextField textField = new BaseTextField();

		final Map<String, Boolean> inputToExpectedHasToBeEscapedMap = new HashMap<String, Boolean>() {{
			put("<img src=x onerror=alert(\"XSS\")>", true);
			put("ABCD <> 1234", false);
			put("ABCD <1234>", false);
			put("ABCD < 1234>", false);
			put("ABCD <1234 >", false);
			put("ABCD <12 34>", false);
			put("ABCD <12=34>", false);
		}};

		inputToExpectedHasToBeEscapedMap.forEach((input, expectedhasToBeEscaped) -> {


			boolean hasToBeEscaped = BaseTextField.hasToBeEscapedBecauseItHasThePotentialToBeAnXSSScript(input);

			assertThat(hasToBeEscaped)
					.overridingErrorMessage("Error: For input \"" + input + "\", hasToBeEscapedBecauseItHasThePotentialToBeAnXSSScript was expected to return " + expectedhasToBeEscaped + " but returned " + hasToBeEscaped + " instead.")
					.isEqualTo(expectedhasToBeEscaped);

			String expectedOutput;
			if (hasToBeEscaped) {
				expectedOutput = StringUtils.replace(input, "<", "&lt;");
				expectedOutput = StringUtils.replace(expectedOutput, ">", "&gt;");
			} else {
				expectedOutput = input;
			}

			textField.setValue(input);
			assertThat(textField.getValue()).isEqualTo(expectedOutput);
		});
	}
}

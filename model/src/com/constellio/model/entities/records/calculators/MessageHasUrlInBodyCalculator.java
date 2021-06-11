package com.constellio.model.entities.records.calculators;

import com.constellio.model.entities.calculators.AbstractMetadataValueCalculator;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.records.wrappers.Message;
import com.constellio.model.entities.records.wrappers.MessageBodyType;

import java.util.List;
import java.util.regex.Pattern;

import static com.constellio.model.entities.records.wrappers.MessageBodyType.HTML;
import static com.constellio.model.entities.records.wrappers.MessageBodyType.PLAIN_TEXT;
import static java.util.Arrays.asList;

public class MessageHasUrlInBodyCalculator extends AbstractMetadataValueCalculator<Boolean> {
	private static Pattern PLAIN_TEXT_URL_PATTERN = Pattern.compile("https?:\\/{2}[^\\s]+");
	private static Pattern HTML_URL_PATTERN = Pattern.compile("<a\\s+(?:[^>]*?\\s+)?href=\"([^\"]*)");

	private LocalDependency<String> messageBodyDependency = LocalDependency.toAText(Message.MESSAGE_BODY);
	private LocalDependency<MessageBodyType> messageBodyTypeDependency = LocalDependency.toAnEnum(Message.MESSAGE_BODY_TYPE);

	@Override
	public Boolean calculate(CalculatorParameters parameters) {

		String messageBody = parameters.get(messageBodyDependency);

		String str = parameters.get(messageBodyTypeDependency).toString();

		MessageBodyType messageBodyType = MessageBodyType.valueOf(str);
		if (HTML.equals(messageBodyType)) {
			return HTML_URL_PATTERN.matcher(messageBody).find();
		} else if (PLAIN_TEXT.equals(messageBodyType)) {
			return PLAIN_TEXT_URL_PATTERN.matcher(messageBody).find();
		}
		return false;
	}

	@Override
	public List<? extends Dependency> getDependencies() {
		return asList(messageBodyDependency, messageBodyTypeDependency);
	}
}

package com.constellio.app.ui.util;

import com.constellio.app.ui.pages.management.schemas.type.CannotDeleteWindow;
import com.constellio.model.entities.records.Record;
import com.constellio.model.frameworks.validation.ValidationError;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.frameworks.validation.ValidationException;
import com.constellio.model.frameworks.validation.ValidationRuntimeException;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.RecordServicesRuntimeException;
import com.constellio.model.services.records.RecordServicesRuntimeException.RecordServicesRuntimeException_CannotLogicallyDeleteRecord;
import com.constellio.model.services.records.RecordServicesRuntimeException.RecordServicesRuntimeException_CannotPhysicallyDeleteRecord;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class MessageUtils {

	private static String DETAIL_SEPARATOR = "------------------------------------------------------------------------";

	public static String toDetailedReportMessage(Exception e) {

		ValidationErrors errors = getValidationErrors(e);

		if (errors != null) {
			return toDetailedReportMessage(errors);
		} else {
			return toMessage(e) + "\n\n\n" + ExceptionUtils.getStackTrace(e);
		}
	}

	public static String toMessage(Exception e) {

		ValidationErrors errors = getValidationErrors(e);

		if (errors != null) {
			return toMessage(errors);

		} else if (e instanceof RecordServicesRuntimeException_CannotLogicallyDeleteRecord) {
			return $(e.getMessage());

		} else if (e instanceof RecordServicesRuntimeException_CannotPhysicallyDeleteRecord) {
			return $(e.getMessage());

		} else if (e instanceof RecordServicesRuntimeException.RecordServicesRuntimeException_CannotRestoreRecord) {
			return $(e.getMessage());

		} else {
			e.printStackTrace();
			return toGeneralErrorMessageWithText(e.getMessage());
		}

	}

	public static ValidationErrors getValidationErrors(Throwable e) {
		if (e instanceof ValidationException) {
			return ((ValidationException) e).getValidationErrors();

		} else if (e instanceof RecordServicesException.ValidationException) {
			return ((RecordServicesException.ValidationException) e).getErrors();

		} else if (e instanceof ValidationRuntimeException) {
			return ((ValidationRuntimeException) e).getValidationErrors();

		} else if (e.getCause() != null) {
			return getValidationErrors(e.getCause());
		} else {
			return null;
		}
	}

	private static String toGeneralErrorMessageWithText(String message) {
		StringBuilder stringBuilder = new StringBuilder();

		stringBuilder.append($("runtimeException"));
		stringBuilder.append("\n\n");
		stringBuilder.append(message);

		return stringBuilder.toString();

	}

	public static CannotDeleteWindow getCannotDeleteWindow(ValidationErrors validationErrors) {
		ValidationError validationError = validationErrors.getValidationErrors().get(0);
		if (!validationError.getParameters().isEmpty()) {
			if (validationError.getParameters().get("records") instanceof List) {
				CannotDeleteWindow cannotDeleteWindow = new CannotDeleteWindow($(validationError.getCode()));
				cannotDeleteWindow.buildWindowConponentsWithTable((List<Record>) validationError.getParameters().get("records"), true);
				return cannotDeleteWindow;
			} else {
				CannotDeleteWindow cannotDeleteWindow = new CannotDeleteWindow($(validationError.getCode(), validationError.getParameters()));
				cannotDeleteWindow.buildWindowConponentsWithoutTable();
				return cannotDeleteWindow;
			}
		} else {
			CannotDeleteWindow cannotDeleteWindow = new CannotDeleteWindow($(validationError.getCode()));
			cannotDeleteWindow.buildWindowConponentsWithoutTable();
			return cannotDeleteWindow;
		}
	}

	public static String toMessage(ValidationErrors validationErrors) {

		StringBuilder stringBuilder = new StringBuilder();

		for (ValidationError validationError : validationErrors.getValidationErrors()) {
			if (stringBuilder.length() != 0) {
				stringBuilder.append("\n");
			}
			stringBuilder.append($(validationError));

		}

		return stringBuilder.toString();

	}

	public static String toDetailedReportMessage(ValidationErrors validationErrors) {

		StringBuilder summaryBuilder = new StringBuilder();
		summaryBuilder.append($("errorReportSummaryTitle"));
		summaryBuilder.append("\n");

		StringBuilder detailedBuilder = new StringBuilder();
		detailedBuilder.append($("errorReportDetailedTitle"));
		detailedBuilder.append("\n");

		int index = 1;
		for (ValidationError validationError : validationErrors.getValidationErrors()) {
			if (index != 1) {
				summaryBuilder.append("\n");
				detailedBuilder.append("\n");
			}

			String errorSummary = index + "- " + $(validationError);
			summaryBuilder.append(errorSummary);

			detailedBuilder.append(DETAIL_SEPARATOR);
			detailedBuilder.append("\n");
			detailedBuilder.append(errorSummary);
			detailedBuilder.append("\n\n");

			if (StringUtils.isBlank(validationError.getAdditionalStack())) {
				detailedBuilder.append($("noMoreDetail"));
			} else {
				detailedBuilder.append(validationError.getAdditionalStack());
			}

			index++;
		}

		summaryBuilder.append("\n\n\n");
		detailedBuilder.append("\n");
		detailedBuilder.append(DETAIL_SEPARATOR);

		return summaryBuilder.toString() + detailedBuilder.toString();
	}
}

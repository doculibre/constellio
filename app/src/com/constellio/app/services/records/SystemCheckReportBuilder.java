package com.constellio.app.services.records;

import static com.constellio.app.ui.i18n.i18n.$;

import com.constellio.model.frameworks.validation.ValidationError;
import com.constellio.model.frameworks.validation.ValidationErrors;

public class SystemCheckReportBuilder {

	SystemCheckManager manager;

	public SystemCheckReportBuilder(SystemCheckManager manager) {
		this.manager = manager;
	}

	public String build() {
		StringBuilder reportBuilder = new StringBuilder();
		SystemCheckResults results = manager.getLastSystemCheckResults();
		ValidationErrors errors = new ValidationErrors(results.errors);

		reportBuilder.append("System check started on : " + results.dateTime + "\n");
		reportBuilder.append("System check status : " + (manager.isSystemCheckResultsRunning() ? "Running" : "Finished") + "\n");
		reportBuilder.append("\n");
		reportBuilder.append("Broken references : " + results.brokenReferences + "\n");
		reportBuilder.append("Checked references : " + results.checkedReferences + "\n");
		reportBuilder.append("Repaired records : " + results.recordsRepaired + "\n");
		reportBuilder.append("\n");
		if (!errors.getValidationErrors().isEmpty()) {
			reportBuilder.append("" + errors.getValidationErrors().size() + " errors : \n");
			for (ValidationError error : errors.getValidationErrors()) {
				reportBuilder.append($(error) + "\n");
			}
			reportBuilder.append("\n");
		}

		if (!errors.getValidationWarnings().isEmpty()) {
			reportBuilder.append("" + errors.getValidationWarnings().size() + " warnings : \n");
			for (ValidationError error : errors.getValidationWarnings()) {
				reportBuilder.append($(error) + "\n");
			}
			reportBuilder.append("\n");
		}

		return reportBuilder.toString();
	}

}

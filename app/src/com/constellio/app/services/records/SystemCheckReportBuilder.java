package com.constellio.app.services.records;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.constellio.app.ui.i18n.i18n;
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

		reportBuilder.append(label("startedOn") + " : " + results.dateTime + "\n");
		reportBuilder.append(label("status") + " : " +
				(manager.isSystemCheckResultsRunning() ? label("running") : label("finished")) + "\n");
		reportBuilder.append(label("repairedRecordsCount") + " : " + results.getRepairedRecords().size() + "\n");
		if (!results.getMetrics().isEmpty()) {
			List<String> keys = new ArrayList<>(results.getMetrics().keySet());
			Collections.sort(keys);
			for (String metricKey : keys) {
				String i18nLabel = label("metric." + metricKey);
				reportBuilder.append(i18nLabel + " : " + results.getMetric(metricKey) + "\n");
			}

			reportBuilder.append("\n");
		}

		if (!results.getResultsInfos().isEmpty()) {
			List<String> keys = new ArrayList<>(results.getResultsInfos().keySet());
			Collections.sort(keys);

			for (String infoKey : keys) {
				String i18nLabel = label("info." + infoKey);
				Object value = (Object) results.getResultsInfos().get(infoKey);
				if (value instanceof List) {
					List list = (List) value;
					Collections.sort(list);
					reportBuilder.append(i18nLabel + " : \n");
					for (Object item : list) {
						reportBuilder.append("\t- " + item + "\n");
					}
					reportBuilder.append("\n");
				} else {
					reportBuilder.append(i18nLabel + " : " + value + "\n");
				}

			}
			reportBuilder.append("\n");
		}

		if (!errors.getValidationErrors().isEmpty()) {
			reportBuilder.append("" + errors.getValidationErrors().size() + " " + label("errors") + " : \n");
			List<String> messages = new ArrayList<>(i18n.asListOfMessages(errors.getValidationErrors()));
			Collections.sort(messages);
			for (String message : messages) {
				reportBuilder.append("\t- " + $(message) + "\n");
			}
			reportBuilder.append("\n");
		}

		if (!errors.getValidationWarnings().isEmpty()) {
			reportBuilder.append("" + errors.getValidationWarnings().size() + " " + label("warnings") + " : \n");
			List<String> messages = new ArrayList<>(i18n.asListOfMessages(errors.getValidationErrors()));
			Collections.sort(messages);
			for (String message : messages) {
				reportBuilder.append("\t- " + $(message) + "\n");
			}
			reportBuilder.append("\n");
		}

		return reportBuilder.toString();
	}

	private String label(String key) {
		return $(SystemCheckReportBuilder.class.getName() + "_" + key);
	}

}

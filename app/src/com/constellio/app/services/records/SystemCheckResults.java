package com.constellio.app.services.records;

import java.util.HashSet;
import java.util.Set;

import org.joda.time.LocalDateTime;

import com.constellio.model.frameworks.validation.ValidationErrors;

public class SystemCheckResults {

	LocalDateTime dateTime = new LocalDateTime();

	int logicallyDeletedUsers = 0;

	int checkedReferences = 0;
	int brokenReferences = 0;

	Set<String> repairedRecords = new HashSet<>();

	ValidationErrors errors = new ValidationErrors();

	public ValidationErrors getErrors() {
		return errors;
	}

	public void markAsRepaired(String id) {
		repairedRecords.add(id);
	}
}


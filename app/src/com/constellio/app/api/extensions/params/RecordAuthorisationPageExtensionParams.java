package com.constellio.app.api.extensions.params;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class RecordAuthorisationPageExtensionParams {
	@Getter
	Record record;

	@Getter
	User user;
}

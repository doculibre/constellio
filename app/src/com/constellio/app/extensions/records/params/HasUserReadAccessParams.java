package com.constellio.app.extensions.records.params;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class HasUserReadAccessParams {
	private User user;
	private Record record;
}

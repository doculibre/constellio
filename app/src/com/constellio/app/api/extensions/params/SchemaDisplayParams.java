package com.constellio.app.api.extensions.params;

import com.constellio.app.ui.entities.RecordVO;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class SchemaDisplayParams {
	String schemaType;
	RecordVO recordVO;
}

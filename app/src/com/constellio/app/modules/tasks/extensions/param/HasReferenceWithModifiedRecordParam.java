package com.constellio.app.modules.tasks.extensions.param;

import com.constellio.app.ui.entities.RecordVO;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class HasReferenceWithModifiedRecordParam {
	RecordVO recordVOWithModifications;
}

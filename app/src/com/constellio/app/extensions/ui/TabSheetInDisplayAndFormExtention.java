package com.constellio.app.extensions.ui;

import lombok.Getter;

import java.util.Collections;
import java.util.List;

public class TabSheetInDisplayAndFormExtention {
	@Getter
	public static class TabSheetInDisplayAndFormExtentionParams {
		String schemaCode;

		public TabSheetInDisplayAndFormExtentionParams(String schemaCode) {
			this.schemaCode = schemaCode;
		}
	}

	public List<String> getTabSheetCaptionToHide(
			TabSheetInDisplayAndFormExtentionParams baseFormExtentionTabActionToHideParams) {
		return Collections.emptyList();
	}
}

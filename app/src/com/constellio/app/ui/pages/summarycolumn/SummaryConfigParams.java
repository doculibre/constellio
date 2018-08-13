package com.constellio.app.ui.pages.summarycolumn;

import com.constellio.app.ui.entities.MetadataVO;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import static com.constellio.app.ui.i18n.i18n.$;


public class SummaryConfigParams {

	public enum DisplayCondition {
		COMPLETED,
		ALWAYS;

		@Override
		public String toString() {
			if (this == COMPLETED) {
				return $("SummaryColomnParams.DisplayCondition.ifcompleted");
			} else if (this == ALWAYS) {
				return $("SummaryColomnParams.DisplayCondition.always");
			}

			return this.toString();
		}
	}

	public enum ReferenceMetadataDisplay {
		CODE,
		TITLE;

		@Override
		public String toString() {
			if (this == CODE) {
				return $("SummaryColomnParams.ReferenceDisplay.code");
			} else if (this == TITLE) {
				return $("SummaryColomnParams.ReferenceDisplay.title");
			}

			return this.toString();
		}

		public String getLocalCode() {
			if (this == CODE) {
				return "code";
			} else if (this == TITLE) {
				return "title";
			}

			throw new NotImplementedException();
		}

		public static ReferenceMetadataDisplay fromInteger(int intToParse) {
			switch (intToParse) {
				case 0:
					return CODE;
				case 1:
					return TITLE;
			}
			return null;
		}
	}

	private MetadataVO metadataVO;
	private String prefix;
	private DisplayCondition displayCondition;
	private ReferenceMetadataDisplay referenceMetadataDisplay;

	public SummaryConfigParams() {

	}

	public ReferenceMetadataDisplay getReferenceMetadataDisplay() {
		return referenceMetadataDisplay;
	}

	public void setReferenceMetadataDisplay(ReferenceMetadataDisplay referenceMetadataDisplay) {
		this.referenceMetadataDisplay = referenceMetadataDisplay;
	}

	public DisplayCondition getDisplayCondition() {
		return displayCondition;
	}

	public void setDisplayCondition(DisplayCondition displayCondition) {
		this.displayCondition = displayCondition;
	}

	public MetadataVO getMetadataVO() {
		return metadataVO;
	}

	public void setMetadataVO(MetadataVO metadata) {
		this.metadataVO = metadata;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
}

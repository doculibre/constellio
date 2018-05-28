package com.constellio.app.ui.pages.summarycolumn;

import com.constellio.app.ui.entities.MetadataVO;

import static com.constellio.app.ui.i18n.i18n.$;


public class SummaryColumnParams {

    public enum DisplayCondition {
        COMPLETED,
        ALWAYS;

        @Override
        public String toString() {
            if(this == COMPLETED) {
                return $("SummaryColomnParams.DisplayCondition.ifcompleted");
            } else if (this == ALWAYS) {
                return $("SummaryColomnParams.DisplayCondition.always");
            }

            return this.toString();
        }
    }

    private MetadataVO metadataVO;
    private String prefix;
    private DisplayCondition displayCondition;

    public SummaryColumnParams(){

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

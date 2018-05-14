package com.constellio.app.modules.rm.ui.pages.folder;

import com.constellio.app.ui.entities.MetadataVO;

public class SummaryColomnParams {

    enum DisplayCondition {
        COMPLETED,
        ALWAYS
    }

    private MetadataVO metadata;
    private String textPrefix;
    private DisplayCondition displayCondition;

    public DisplayCondition getDisplayCondition() {
        return displayCondition;
    }

    public void setDisplayCondition(DisplayCondition displayCondition) {
        this.displayCondition = displayCondition;
    }

    public MetadataVO getMetadata() {
        return metadata;
    }

    public void setMetadata(MetadataVO metadata) {
        this.metadata = metadata;
    }

    public String getTextPrefix() {
        return textPrefix;
    }

    public void setTextPrefix(String textPrefix) {
        this.textPrefix = textPrefix;
    }


}

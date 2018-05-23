package com.constellio.app.modules.rm.ui.pages.folder;

import com.constellio.app.ui.entities.MetadataVO;

public class SummaryColumnVO {
    String prefix;
    MetadataVO metadataVO;
    boolean isAlwaysShown;

    public String getPrefix() {
        return prefix;
    }

    public SummaryColumnVO setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    public MetadataVO getMetadata() {
        return metadataVO;
    }

    public SummaryColumnVO setMetadata(MetadataVO metadataVO) {
        this.metadataVO = metadataVO;
        return this;
    }

    public boolean isAlwaysShown() {
        return isAlwaysShown;
    }

    public SummaryColumnVO setAlwaysShown(boolean alwaysShown) {
        isAlwaysShown = alwaysShown;
        return this;
    }
}

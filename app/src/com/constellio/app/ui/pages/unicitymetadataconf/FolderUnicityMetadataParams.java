package com.constellio.app.ui.pages.unicitymetadataconf;

import com.constellio.app.ui.entities.MetadataVO;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import static com.constellio.app.ui.i18n.i18n.$;


public class FolderUnicityMetadataParams {


    private MetadataVO metadataVO;

    public FolderUnicityMetadataParams(){

    }


    public MetadataVO getMetadataVO() {
        return metadataVO;
    }

    public FolderUnicityMetadataParams setMetadataVO(MetadataVO metadata) {
        this.metadataVO = metadata;
        return this;
    }
}

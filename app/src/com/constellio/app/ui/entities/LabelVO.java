package com.constellio.app.ui.entities;

import com.constellio.model.entities.records.Content;

import static com.constellio.app.modules.rm.wrappers.RMReport.*;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Marco on 2017-01-19.
 */
public class LabelVO extends RecordVO implements Serializable {

    public LabelVO(String id, List<MetadataValueVO> metadataValues, VIEW_MODE viewMode) {
        super(id, metadataValues, viewMode);
    }

    public String getTitle() {
        return get(TITLE);
    }

    public String getJasperFile() {

        return get(JASPERFILE);
    }

    public String getColonne() {
        return get(COLONNE);
    }

    public String getLigne() {
        return get(LIGNE);
    }

    public String getType() {
        return get(TYPE_LABEL);
    }

//    public String getWidth() {
//        return get(WIDTH);
//    }
//
//    public String getHeight() {
//        return get(HEIGHT);
//    }

    public void setTitle(String title) {
        set(TITLE, title);
    }

    public void setColonne(String colonne) {
        set(COLONNE, colonne);
    }

    public void setLigne(String ligne) {
        set(LIGNE, ligne);
    }

    public void setType(String type) {
        set(TYPE_LABEL, type);
    }
//
//    public void setWidth(String width) {
//        set(WIDTH, width);
//    }
//
//    public void setHeight(String height) {
//        set(HEIGHT, height);
//    }

    public void setJasperFile(Content jasperFile) {
        set(JASPERFILE, jasperFile);
    }

}

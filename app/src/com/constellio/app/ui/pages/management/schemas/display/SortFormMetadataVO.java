package com.constellio.app.ui.pages.management.schemas.display;

import com.constellio.app.ui.entities.FormMetadataVO;
import com.constellio.app.ui.pages.base.SessionContext;

import java.util.Comparator;

/**
 * Created by constellios on 2017-04-24.
 */
public class SortFormMetadataVO implements Comparator<Object> {
    SessionContext sessionContext;

    public SortFormMetadataVO(SessionContext sessionContext){
        this.sessionContext = sessionContext;
    }

    public int compare(Object o1, Object o2) {
        FormMetadataVO formMetadataVO1 = (FormMetadataVO) o1;
        FormMetadataVO formMetadataVO2 = (FormMetadataVO) o2;
        return formMetadataVO1.getLabel(sessionContext.getCurrentLocale().getLanguage()).toLowerCase()
                .compareTo((formMetadataVO2.getLabel(sessionContext.getCurrentLocale().getLanguage()).toLowerCase().toLowerCase()));
    }
}

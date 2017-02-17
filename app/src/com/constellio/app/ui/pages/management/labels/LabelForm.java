package com.constellio.app.ui.pages.management.labels;

import com.constellio.app.modules.rm.ui.components.folder.fields.CustomFolderField;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.pages.base.SessionContext;

import java.io.Serializable;

/**
 * Implement:
 * RMreport.TYPE
 * RMreport.TITLE
 * RMreport.JASPERFILE
 * RMreport.LIGNE
 * RMreport.COLONNE
 *
 * @author Nicolas D'Amours & Charles Blanchette
 */
public interface LabelForm extends Serializable {

    void reload();

    void commit();

    ConstellioFactories getConstellioFactories();

    SessionContext getSessionContext();

    CustomLabelField<?> getCustomField(String metadataCode);
}

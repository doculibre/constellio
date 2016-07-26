package com.constellio.app.services.importExport.settings;

import com.constellio.app.services.importExport.settings.model.*;
import com.constellio.app.services.importExport.settings.utils.SettingsXMLFileConstants;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.sdk.tests.ConstellioTest;

import java.util.*;

import static java.util.Arrays.asList;

public class SettingsImportServicesTestUtils extends ConstellioTest implements SettingsXMLFileConstants {

    protected static Map<String, String> getTabsMap() {
        Map<String, String> tabParams = new HashMap<>();
        tabParams.put("default", "Métadonnées");
        tabParams.put("zeTab", "Mon onglet");
        return tabParams;
    }

    protected List<ImportedTab> toListOfTabs(Map<String, String> tabParams) {
        List<ImportedTab> tabs = new ArrayList<>();
        for (Map.Entry<String, String> entry : tabParams.entrySet()) {
            tabs.add(new ImportedTab().setCode(entry.getKey()).setValue(entry.getValue()));
        }
        return tabs;
    }

    protected List<String> toListOfString(String... values) {
        return Arrays.asList(values);
    }

    protected Map<String, String> toTitlesMap(String title_fr, String title_en) {
        Map<String, String> titles = new HashMap<>();
        titles.put("title_fr", title_fr);
        titles.put("title_en", title_en);

        return titles;
    }


}

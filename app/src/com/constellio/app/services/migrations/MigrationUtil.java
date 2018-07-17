package com.constellio.app.services.migrations;

import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.model.entities.Language;
import com.constellio.model.services.factories.ModelLayerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MigrationUtil {
    public static Map<Language, String> getLabelsByLanguage(String collection, ModelLayerFactory modelLayerFactory, MigrationResourcesProvider migrationResourcesProvider, String labelKey) {
        List<String> languageList = modelLayerFactory.getCollectionsListManager().getCollectionLanguages(collection);

        Map<Language,String> mapLangageTitre = new HashMap<>();

        for(String language : languageList) {
            Locale locale = new Locale(language);
            mapLangageTitre.put(Language.withLocale(locale), migrationResourcesProvider.getString(labelKey, locale));
        }
        return mapLangageTitre;
    }
}

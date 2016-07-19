package com.constellio.app.services.importExport.settings.utils;

import com.constellio.app.services.importExport.settings.SettingsImportServicesTestUtils;
import com.constellio.app.services.importExport.settings.model.ImportedCollectionSettings;
import com.constellio.app.services.importExport.settings.model.ImportedConfig;
import com.constellio.app.services.importExport.settings.model.ImportedSettings;
import com.constellio.app.services.importExport.settings.model.ImportedValueList;
import com.constellio.data.dao.managers.config.ConfigManagerRuntimeException;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.extractProperty;

public class SettingsXMLFileReaderTest extends SettingsImportServicesTestUtils {

    private Document document;
    private SettingsXMLFileReader reader;

    @Before
    public void setup() {
        document = getDocument();
        reader = new SettingsXMLFileReader(document);
    }

    @Test
    public void givenAValidSettingsDocumentWhenReadingThenDataAreAllLoaded() {
        ImportedSettings importedSettings = reader.read();
        assertThat(importedSettings).isNotNull();

        // configs
        List<ImportedConfig> configs = importedSettings.getConfigs();
        assertThat(configs).isNotEmpty().hasSize(6);
        assertThat(configs.get(0).getKey()).isEqualTo("documentRetentionRules");
        assertThat(configs.get(0).getValue()).isEqualTo("true");
        assertThat(configs.get(1).getKey()).isEqualTo("enforceCategoryAndRuleRelationshipInFolder");
        assertThat(configs.get(1).getValue()).isEqualTo("false");
        assertThat(configs.get(2).getKey()).isEqualTo("calculatedCloseDate");
        assertThat(configs.get(2).getValue()).isEqualTo("false");
        assertThat(configs.get(3).getKey()).isEqualTo("calculatedCloseDateNumberOfYearWhenFixedRule");
        assertThat(configs.get(3).getValue()).isEqualTo("2015");
        assertThat(configs.get(4).getKey()).isEqualTo("closeDateRequiredDaysBeforeYearEnd");
        assertThat(configs.get(4).getValue()).isEqualTo("15");
        assertThat(configs.get(5).getKey()).isEqualTo("yearEndDate");
        assertThat(configs.get(5).getValue()).isEqualTo("02/28");

        List<ImportedCollectionSettings> collectionSettings = importedSettings.getCollectionsConfigs();
        assertThat(collectionSettings).hasSize(1);

        ImportedCollectionSettings zeCollectionSettings = collectionSettings.get(0);
        assertThat(zeCollectionSettings).isNotNull();
        assertThat(zeCollectionSettings.getCode()).isEqualTo(zeCollection);

        List<ImportedValueList> valueLists = zeCollectionSettings.getValueLists();
        assertThat(valueLists).isNotEmpty().hasSize(4);

        ImportedValueList refValueList = getValueListA();
        ImportedValueList valueListItemA = valueLists.get(0);
        assertThat(valueListItemA).isEqualTo(refValueList);


    }

    public Document getDocument() {
        String inputFilePath = "/home/constellio/workspaces/settings-import-tests/settings-output.xml";
        File inputFile = new File(inputFilePath);
        SAXBuilder builder = new SAXBuilder();
        try {
            return builder.build(inputFile);
        } catch (JDOMException e) {
            throw new ConfigManagerRuntimeException("JDOM2 Exception", e);
        } catch (IOException e) {
            throw new ConfigManagerRuntimeException.CannotCompleteOperation("build Document JDOM2 from file", e);
        }
    }
}

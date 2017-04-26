package com.constellio.app.modules.rm.model.calculators.document;

import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.schemas.MetadataValueType;

import java.util.Arrays;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

/**
 * Created by Constellio on 2017-03-03.
 */
public class DocumentMimeTypeCalculator implements MetadataValueCalculator<String> {
    LocalDependency<Content> contentParam = LocalDependency.toAContent(Document.CONTENT);

    @Override
    public String calculate(CalculatorParameters parameters) {
        Content content = parameters.get(contentParam);
        String mimeType = content != null && content.getCurrentVersion() != null ? content.getCurrentVersion().getMimetype() : null;
        mimeType = mimeType != null ? regroupMicrosoftMimeTypes(mimeType) : null;
        return mimeType;
    }

    @Override
    public String getDefaultValue() {
        return null;
    }

    @Override
    public MetadataValueType getReturnType() {
        return MetadataValueType.STRING;
    }

    @Override
    public boolean isMultiValue() {
        return false;
    }

    @Override
    public List<? extends Dependency> getDependencies() {
        return Arrays.asList(contentParam);
    }

    private String regroupMicrosoftMimeTypes(String mimeType) {
        String newMimeType = mimeType;
        if (mimeType != null) {
            switch (mimeType) {

                case "application/vnd.ms-word.document.macroenabled.12":
                case "application/vnd.ms-word.template.macroenabled.12":
                case "application/vnd.openxmlformats-officedocument.wordprocessingml.document":
                case "application/vnd.openxmlformats-officedocument.wordprocessingml.template":
                    newMimeType = $("Mimetype.microsoftWord");
                    break;

                case "application/vnd.ms-excel.sheet.binary.macroenabled.12":
                case "application/vnd.ms-excel":
                case "application/vnd.ms-excel.template.macroenabled.12":
                case "application/vnd.ms-excel.sheet.macroenabled.12":
                case "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet":
                case "application/vnd.openxmlformats-officedocument.spreadsheetml.template":
                    newMimeType = $("Mimetype.microsoftExcel");
                    break;

                case "application/vnd.openxmlformats-officedocument.presentationml.presentation":
                case "application/vnd.openxmlformats-officedocument.presentationml.slide":
                case "application/vnd.openxmlformats-officedocument.presentationml.slideshow":
                case "application/vnd.openxmlformats-officedocument.presentationml.template":
                case "application/vnd.ms-powerpoint":
                case "application/vnd.ms-powerpoint.presentation.macroenabled.12":
                case "application/vnd.ms-powerpoint.slideshow.macroenabled.12":
                case "application/vnd.ms-powerpoint.template.macroenabled.12":
                    newMimeType = $("Mimetype.microsoftPowerPoint");
                    break;
            }
        }
        return newMimeType;
    }
}

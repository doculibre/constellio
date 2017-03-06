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

/**
 * Created by Constellio on 2017-03-03.
 */
public class DocumentMimeTypeCalculator implements MetadataValueCalculator<String> {
    LocalDependency<Content> contentParam = LocalDependency.toAContent(Document.CONTENT);

    @Override
    public String calculate(CalculatorParameters parameters) {
		Content content = parameters.get(contentParam);
        return content != null && content.getCurrentVersion() != null ? content.getCurrentVersion().getMimetype() : null;
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
}

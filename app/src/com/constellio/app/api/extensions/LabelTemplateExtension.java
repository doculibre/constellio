package com.constellio.app.api.extensions;

import java.util.List;

import com.constellio.app.modules.rm.model.labelTemplate.LabelTemplate;

public abstract class LabelTemplateExtension {

	public abstract void addLabelTemplates(String schemaType, List<LabelTemplate> labelTemplates);

}

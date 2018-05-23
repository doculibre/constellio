package com.constellio.app.ui.pages.search;

import com.constellio.app.ui.pages.base.BasePresenterUtils;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.schemas.MetadataSchema;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchCriteriaPresenterUtils extends BasePresenterUtils{

    public SearchCriteriaPresenterUtils(SessionContext sessionContext) {
        super(null, sessionContext);
    }

    public final Map<String,String> getMetadataSchemasList(String schemaTypeCode){
        Map<String,String> metadataSchemasMap = new HashMap<>();
        String collection = sessionContext.getCurrentCollection();
        String language = sessionContext.getCurrentLocale().getLanguage();
        List<MetadataSchema> metadataSchemas = getConstellioFactories().getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(collection)
                .getSchemaType(schemaTypeCode).getSchemas();
        for (MetadataSchema metadataSchema : metadataSchemas){
            metadataSchemasMap.put(metadataSchema.getCode(), metadataSchema.getLabel(Language.withCode(language)));
        }
        return metadataSchemasMap;
    }
}

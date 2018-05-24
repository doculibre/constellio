package com.constellio.app.ui.pages.summarycolumn;

import com.constellio.app.modules.rm.ui.pages.folder.SummaryColumnVO;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.framework.builders.MetadataToVOBuilder;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.services.exception.TypeRuntimeException;
import com.constellio.model.services.schemas.MetadataList;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.schemas.xml.TypeConvertionUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SummaryColumnPresenter extends SingleSchemaBasePresenter<SummaryColumnView>  {

    private static final String SCHEMA_CODE = "schemaCode";
    public static final String PREFIX = "prefix";
    public static final String METADATA_CODE = "metadataCode";
    public static final String IS_ALWAYS_SHOWN = "isAlwaysShown";
    private String schemaCode;
    private Map<String, String> parameters;
    public static final String SUMMARY_COLOMN = "summaryColumn";

    public SummaryColumnPresenter(SummaryColumnView view) {
        super(view);
    }

    public SummaryColumnPresenter(SummaryColumnView view, String schemaCode) {
        super(view, schemaCode);
    }


    public void forParams(String params) {
        Map<String, String> paramsMap = ParamUtils.getParamsMap(params);
        this.schemaCode = paramsMap.get(SCHEMA_CODE);
    }

    public List<MetadataVO> getMetadatas() {
        MetadataSchemasManager schemasManager = modelLayerFactory.getMetadataSchemasManager();
        MetadataList list = schemasManager.getSchemaTypes(collection).getSchema(getSchemaCode()).getMetadatas();

        List<MetadataVO> metadataVOs = new ArrayList<>();
        MetadataToVOBuilder builder = new MetadataToVOBuilder();
        for (Metadata metadata : list) {
            if (metadata.isEnabled() && !metadata.isSystemReserved()) {
                metadataVOs
                        .add(builder.build(metadata, view.getSessionContext()));
            }
        }

        return metadataVOs;
    }



    public List<SummaryColumnVO> dataInMetadataSummaryColumn() {
        Object objectList = getSummaryMetadata().getCustomParameter().get(SUMMARY_COLOMN);
        List<SummaryColumnVO> lSummaryColumnVOList = new ArrayList<>();

        if (objectList instanceof List) {
            for (Object listObject : (List) objectList) {
                HashMap<String, Object> mapObject = (HashMap<String, Object>) listObject;

                SummaryColumnVO columnResumeParams = new SummaryColumnVO();
                String metadataCode = (String) mapObject.get("metadataCode");
                MetadataToVOBuilder metadataToVOBuilder = new MetadataToVOBuilder();
                MetadataVO metadataVO = metadataToVOBuilder.build(getMetadata(metadataCode), view.getSessionContext());

                columnResumeParams.setMetadata(metadataVO);
                columnResumeParams.setPrefix((String) mapObject.get("prefix"));
                columnResumeParams.setAlwaysShown(Boolean.TRUE.toString().equals(mapObject.get("isAlwaysShown")));

                lSummaryColumnVOList.add(columnResumeParams);
            }
        }

        return lSummaryColumnVOList;
    }

    public void addMetadaForSummary(SummaryColumnParams metadataVO) {
        Map modifiableMap = copyUnModifiableMapToModifiableMap((Map) getSummaryMetadata().getCustomParameter());

        List list = (List) modifiableMap.get(SUMMARY_COLOMN);

        if(list == null) {
            list = new ArrayList();
        }

        Map summaryInmap = new HashMap();
        summaryInmap.put(PREFIX, metadataVO.getPrefix());
        summaryInmap.put(IS_ALWAYS_SHOWN, metadataVO.getDisplayCondition() == SummaryColumnParams.DisplayCondition.ALWAYS);
        summaryInmap.put(METADATA_CODE, metadataVO.getMetadata().getCode());
        list.add(summaryInmap);

        modifiableMap.put(SUMMARY_COLOMN, list);
        saveMetadata(metadataVO.getMetadata().getCode(), modifiableMap);
    }

    public void saveMetadata(final String metadataCode, final Map<String, Object> customParameter) {
        MetadataSchemasManager schemasManager = modelLayerFactory.getMetadataSchemasManager();
        schemasManager.modify(collection, new MetadataSchemaTypesAlteration() {
            @Override
            public void alter(MetadataSchemaTypesBuilder types) {
                types.getSchema(getSchemaCode()).get(metadataCode).setCustomParameter(customParameter);
            }
        });
    }

    private Map<String, Object> copyUnModifiableMapToModifiableMap(Map<String, Object> map) {

        Map<String, Object> newModifiableMap = new HashMap<>();

        for(String key : map.keySet()) {
             Object object = map.get(key);

             newModifiableMap.put(key, getModifiableValue(object));
        }

        return newModifiableMap;
    }

    private Object getModifiableValue(Object object) {
        if(object instanceof List) {
           List<Object> newList = new ArrayList<>();
           for(Object itemInList : ((List) object)) {
               newList.add(getModifiableValue(itemInList));
           }
           return newList;
        } else if (object instanceof Map) {
            Map<String,Object> oldMap = (Map<String,Object>) object;
            Map<String,Object> newMap = new HashMap<>();
            for(String key : oldMap.keySet()) {
                newMap.put(key, getModifiableValue(oldMap.get(key)));
            }
            return newMap;
        } else if (TypeConvertionUtil.canObjectValueBeRepresentedInAString(object)) {
            return object;
        } else {
            throw new TypeRuntimeException.UnsupportedTypeRunTimeException(object.getClass().getName());
        }
    }

    public void moveUpSummaryMetadata(String metadataCode) {
        List<Map> list = (List<Map>) getSummaryMetadata().getCustomParameter().get(SUMMARY_COLOMN);

        if(list == null || list.size() < 2 || list.get(0).get(METADATA_CODE).equals(metadataCode)) {
            return;
        }

        for(int i = 0; i + 1 < list.size(); i++) {
            if(list.get(i + 1).get(METADATA_CODE).equals(metadataCode)) {
                Map map = list.remove(i + 1);

                list.add(i, map);

                break;
            }
        }
    }

    public void moveDownSummaryMetadata(String metadataCode) {
        List<Map> list = (List<Map>) getSummaryMetadata().getCustomParameter().get(SUMMARY_COLOMN);

        if(list == null || list.size() < 2 || list.get(list.size() - 1).get(METADATA_CODE).equals(metadataCode)) {
            return;
        }

        for(int i = list.size() - 1; i  >= 0; i--) {
            if(list.get(i - 1).get(METADATA_CODE).equals(metadataCode)) {
                Map map = list.remove(i - 1);
                list.add(i, map);
                break;
            }
        }
    }


    public Metadata getSummaryMetadata() {
        MetadataSchemasManager schemasManager = modelLayerFactory.getMetadataSchemasManager();
        Metadata metadata = schemasManager.getSchemaTypes(collection).getSchema(getSchemaCode()).getMetadata(Folder.SUMMARY);
        return metadata;
    }


    public void setParameters(Map<String, String> params) {
        this.parameters = params;
    }

    @Override
    protected boolean hasPageAccess(String params, User user) {
        return true;
    }
}

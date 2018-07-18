package com.constellio.app.ui.pages.summarycolumn;

import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.SummaryColumnVO;
import com.constellio.app.ui.framework.builders.MetadataToVOBuilder;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.data.utils.LangUtils;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.exception.TypeRuntimeException;
import com.constellio.model.services.schemas.MetadataList;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.schemas.xml.TypeConvertionUtil;

import java.util.*;

public class SummaryColumnPresenter extends SingleSchemaBasePresenter<SummaryColumnView>  {

    private static final String SCHEMA_CODE = "schemaCode";
    public static final String PREFIX = "prefix";
    public static final String METADATA_CODE = "metadataCode";
    public static final String REFERENCE_METADATA_DISPLAY = "referenceMetadataDisplay";

    public static final String IS_ALWAYS_SHOWN = "isAlwaysShown";
    private String schemaCode;
    private Map<String, String> parameters;
    public static final String SUMMARY_COLOMN = "summaryColumn";
    List<Map<String,Object>> originalCustomParametersSummaryColumn;

    public SummaryColumnPresenter(SummaryColumnView view) {
        super(view);
    }

    public SummaryColumnPresenter(SummaryColumnView view, String schemaCode) {
        super(view, schemaCode);
    }


    public void forParams(String params) {
        Map<String, String> paramsMap = ParamUtils.getParamsMap(params);
        this.schemaCode = paramsMap.get(SCHEMA_CODE);

        if(getSummaryMetadata().getCustomParameter() != null) {
            originalCustomParametersSummaryColumn = (List<Map<String, Object>>) getSummaryMetadata().getCustomParameter().get(SUMMARY_COLOMN);
        } else {
            originalCustomParametersSummaryColumn = null;
        }
    }

    public List<MetadataVO> getMetadatas() {
        MetadataSchemasManager schemasManager = modelLayerFactory.getMetadataSchemasManager();
        MetadataList list = schemasManager.getSchemaTypes(collection).getSchema(getSchemaCode()).getMetadatas();

        List<MetadataVO> metadataVOs = new ArrayList<>();
        MetadataToVOBuilder builder = new MetadataToVOBuilder();
        for (Metadata metadata : list) {
            if (metadata.isEnabled() && !metadata.isSystemReserved() && metadata.getType() != MetadataValueType.CONTENT) {
                metadataVOs
                        .add(builder.build(metadata, view.getSessionContext()));
            }
        }

        return metadataVOs;
    }



    public List<SummaryColumnVO> summaryColumnVOList() {
        MetadataSchemasManager schemasManager = modelLayerFactory.getMetadataSchemasManager();
        Object objectList = getSummaryMetadata().getCustomParameter().get(SUMMARY_COLOMN);
        List<SummaryColumnVO> lSummaryColumnVOList = new ArrayList<>();

        if (objectList instanceof List) {
            for (Object listObject : (List) objectList) {
                Map<String, Object> mapObject = (Map<String, Object>) listObject;

                SummaryColumnVO summaryCoumnVo = new SummaryColumnVO();
                String metadataCode = (String) mapObject.get("metadataCode");
                Metadata metadata = schemasManager.getSchemaTypes(collection).getSchema(TypeConvertionUtil.getSchemaCode(metadataCode)).getMetadatas().getMetadataWithLocalCode(TypeConvertionUtil.getMetadataLocalCode(metadataCode));
                MetadataToVOBuilder metadataToVOBuilder = new MetadataToVOBuilder();
                MetadataVO metadataVO = metadataToVOBuilder.build(metadata, view.getSessionContext());

                summaryCoumnVo.setMetadataVO(metadataVO);
                summaryCoumnVo.setPrefix((String) mapObject.get("prefix"));
                summaryCoumnVo.setAlwaysShown((Boolean) mapObject.get(IS_ALWAYS_SHOWN));
                summaryCoumnVo.setReferenceMetadataDisplay((Integer) mapObject.get(REFERENCE_METADATA_DISPLAY));

                lSummaryColumnVOList.add(summaryCoumnVo);
            }
        }

        return lSummaryColumnVOList;
    }


    public void modifyMetadataForSummaryColumn(SummaryColumnParams summaryColumnParams) {
        Map<String,Object> modifiableMap = copyUnModifiableMapToModifiableMap((Map) getSummaryMetadata().getCustomParameter());

        List<Map<String,Object>> list = (List) modifiableMap.get(SUMMARY_COLOMN);

        Map<String,Object> summaryInmap = getMapInList(list, summaryColumnParams.getMetadataVO().getCode());

        summaryInmap.put(PREFIX, summaryColumnParams.getPrefix());
        summaryInmap.put(IS_ALWAYS_SHOWN, summaryColumnParams.getDisplayCondition() == SummaryColumnParams.DisplayCondition.ALWAYS);
        if(summaryColumnParams.getReferenceMetadataDisplay() != null) {
            summaryInmap.put(REFERENCE_METADATA_DISPLAY, summaryColumnParams.getReferenceMetadataDisplay().ordinal());
        }

        saveMetadata(modifiableMap);
    }

    public void deleteMetadataForSummaryColumn(SummaryColumnVO summaryColumnParams) {
        Map<String,Object> modifiableMap = copyUnModifiableMapToModifiableMap( getSummaryMetadata().getCustomParameter());

        List<Map<String,Object>> list = (List) modifiableMap.get(SUMMARY_COLOMN);

        Map<String,Object> summaryInmap = getMapInList(list, summaryColumnParams.getMetadataVO().getCode());

        list.remove(summaryInmap);

        saveMetadata(modifiableMap);
    }

    public Map<String, Object> getMapInList(List<Map<String, Object>> list, String metadataCode){
        if(list == null) {
            return null;
        }
        for(Map<String,Object> map : list) {
            if(((String)map.get(METADATA_CODE)).equalsIgnoreCase(metadataCode)) {
                return map;
            }
        }

        return null;
    }

    public int findMetadataIndex(List<SummaryColumnVO> metadataList, String metadaCode) {
        for(int i = 0; i < metadataList.size(); i++) {
            SummaryColumnVO summaryColumnVO = metadataList.get(i);
            if(summaryColumnVO.getMetadataVO().getCode().equalsIgnoreCase(metadaCode)) {
                return i;
            }
        }
        return -1;
    }

    public int findMetadataInMapList(List<Map<String,Object>> mapList, String metadataCode) {
        int index = -1;

        if(mapList == null) {
            return index;
        }


        for(int i = 0; i < mapList.size(); i++) {
            if(metadataCode.equalsIgnoreCase((String) mapList.get(i).get(METADATA_CODE))) {
                index = i;
                break;
            }
        }

        return index;
    }

    public void moveMetadataDown(String metadataCode) {
        Map<String,Object> modifiableMap = copyUnModifiableMapToModifiableMap((Map) getSummaryMetadata().getCustomParameter());
        List<Map<String,Object>> list = (List) modifiableMap.get(SUMMARY_COLOMN);

        int index = findMetadataInMapList(list, metadataCode);

        if(index >= list.size() - 1) {
            return;
        }

        Collections.swap(list, index, index+1);

        modifiableMap.put(SUMMARY_COLOMN, list);
        saveMetadata(modifiableMap);
    }

    public void moveMetadataUp(String metadataCode) {
        Map<String,Object> modifiableMap = copyUnModifiableMapToModifiableMap((Map) getSummaryMetadata().getCustomParameter());
        List<Map<String,Object>> list = (List) modifiableMap.get(SUMMARY_COLOMN);

        int index = findMetadataInMapList(list, metadataCode);

        if(index <= 0) {
            return;
        }

        Collections.swap(list, index, index-1);

        modifiableMap.put(SUMMARY_COLOMN, list);
        saveMetadata(modifiableMap);
    }

    public void addMetadaForSummary(SummaryColumnParams summaryColumnParams) {
        Map<String,Object> modifiableMap = copyUnModifiableMapToModifiableMap((Map) getSummaryMetadata().getCustomParameter());

        List<Map<String,Object>> list = (List) modifiableMap.get(SUMMARY_COLOMN);

        if(list == null) {
            list = new ArrayList();
        }

        Map summaryInmap = new HashMap();
        summaryInmap.put(PREFIX, summaryColumnParams.getPrefix());
        summaryInmap.put(IS_ALWAYS_SHOWN, summaryColumnParams.getDisplayCondition() == SummaryColumnParams.DisplayCondition.ALWAYS);
        summaryInmap.put(METADATA_CODE, summaryColumnParams.getMetadataVO().getCode());
        if(summaryColumnParams.getReferenceMetadataDisplay() != null) {
            summaryInmap.put(REFERENCE_METADATA_DISPLAY, summaryColumnParams.getReferenceMetadataDisplay().ordinal());
        }
        list.add(summaryInmap);

        modifiableMap.put(SUMMARY_COLOMN, list);
        saveMetadata(modifiableMap);
    }

    public void saveMetadata(final Map<String, Object> customParameter) {
        MetadataSchemasManager schemasManager = modelLayerFactory.getMetadataSchemasManager();
        if(!LangUtils.areNullableEqual(originalCustomParametersSummaryColumn, customParameter.get(SUMMARY_COLOMN))
                && !appLayerFactory.getSystemGlobalConfigsManager().isReindexingRequired()) {
            appLayerFactory.getSystemGlobalConfigsManager().setReindexingRequired(true);
        }
        schemasManager.modify(collection, new MetadataSchemaTypesAlteration() {
            @Override
            public void alter(MetadataSchemaTypesBuilder types) {
                types.getSchema(getSchemaCode()).get("summary").setCustomParameter(customParameter);
            }
        });
    }

    public boolean isThereAModification(SummaryColumnParams summaryColumnParams) {
        Map<String,Object> map = getMapInList(originalCustomParametersSummaryColumn, summaryColumnParams.getMetadataVO().getCode());

        if(map == null) {
            return true;
        }

        SummaryColumnParams.DisplayCondition displayCondition = SummaryColumnParams.DisplayCondition.COMPLETED;
        String alwaysShown = (String) map.get(IS_ALWAYS_SHOWN);
        if(Boolean.TRUE.equals(alwaysShown)) {
            displayCondition = SummaryColumnParams.DisplayCondition.ALWAYS;
        }

        SummaryColumnParams.ReferenceMetadataDisplay referenceMetadataDisplay = SummaryColumnParams.ReferenceMetadataDisplay.fromInteger((Integer) map.get(REFERENCE_METADATA_DISPLAY));

        return !(map.get(METADATA_CODE).equals(summaryColumnParams.getMetadataVO().getCode())
                && map.get(PREFIX).equals(summaryColumnParams.getPrefix())
                && displayCondition == summaryColumnParams.getDisplayCondition()
                && referenceMetadataDisplay == summaryColumnParams.getReferenceMetadataDisplay());


    }

    private boolean isListMapEqual(List<Map<String, Object>> listMap1, List<Map<String, Object>> listMap2) {
        if(listMap1 == null && listMap2 != null || listMap1 != null && listMap2 == null || listMap1.size() != listMap2.size()) {
            return false;
        }

        for(int i = 0; i < listMap1.size(); i++) {
            for (String stringObjectMap : listMap1.get(i).keySet()) {
                if(!listMap1.get(i).get(stringObjectMap)
                        .equals(listMap2.get(i).get(stringObjectMap))) {
                    return false;
                }
            }
        }
        return true;
    }

    protected Map<String, Object> copyUnModifiableMapToModifiableMap(Map<String, Object> map) {

        Map<String, Object> newModifiableMap = new HashMap<>();

        for(String key : map.keySet()) {
             Object object = map.get(key);

             newModifiableMap.put(key, getModifiableValue(object));
        }

        return newModifiableMap;
    }

    protected Object getModifiableValue(Object object) {
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


    public Metadata getSummaryMetadata() {
        MetadataSchemasManager schemasManager = modelLayerFactory.getMetadataSchemasManager();
        Metadata metadata = schemasManager.getSchemaTypes(collection).getSchema(getSchemaCode()).getMetadata("summary");
        return metadata;
    }


    public void setParameters(Map<String, String> params) {
        this.parameters = params;
    }

    @Override
    protected boolean hasPageAccess(String params, User user) {
        return true;
    }

    public boolean isReindextionFlag() {
        return appLayerFactory.getSystemGlobalConfigsManager().isReindexingRequired();
    }

}

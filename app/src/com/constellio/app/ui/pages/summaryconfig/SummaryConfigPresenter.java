package com.constellio.app.ui.pages.summaryconfig;

import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.SummaryConfigElementVO;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SummaryConfigPresenter extends SingleSchemaBasePresenter<SummaryConfigView> {

	private static final String SCHEMA_CODE = "schemaCode";
	public static final String PREFIX = "prefix";
	public static final String METADATA_CODE = "metadataCode";
	public static final String REFERENCE_METADATA_DISPLAY = "referenceMetadataDisplay";

	public static final String IS_ALWAYS_SHOWN = "isAlwaysShown";
	private String schemaCode;
	private Map<String, String> parameters;
	public static final String SUMMARY_CONFIG = "summaryconfig";
	List<Map<String, Object>> originalCustomParametersSummaryConfig;

	public SummaryConfigPresenter(SummaryConfigView view) {
		super(view);
	}

	public SummaryConfigPresenter(SummaryConfigView view, String schemaCode) {
		super(view, schemaCode);
	}


	public void forParams(String params) {
		Map<String, String> paramsMap = ParamUtils.getParamsMap(params);
		this.schemaCode = paramsMap.get(SCHEMA_CODE);

		if (getSummaryMetadata().getCustomParameter() != null) {
			originalCustomParametersSummaryConfig = (List<Map<String, Object>>) getSummaryMetadata().getCustomParameter().get(SUMMARY_CONFIG);
		} else {
			originalCustomParametersSummaryConfig = null;
		}
	}

	public List<MetadataVO> getMetadatas() {
		MetadataSchemasManager schemasManager = modelLayerFactory.getMetadataSchemasManager();
		MetadataList list = schemasManager.getSchemaTypes(collection).getSchema(getSchemaCode()).getMetadatas();

		List<MetadataVO> metadataVOs = new ArrayList<>();
		MetadataToVOBuilder builder = new MetadataToVOBuilder();
		for (Metadata metadata : list) {
			if (metadata.isEnabled() && metadata.getType() != MetadataValueType.CONTENT) {
				metadataVOs
						.add(builder.build(metadata, view.getSessionContext()));
			}
		}

		return metadataVOs;
	}


	public List<SummaryConfigElementVO> summaryConfigVOList() {
		MetadataSchemasManager schemasManager = modelLayerFactory.getMetadataSchemasManager();
		Object objectList = getSummaryMetadata().getCustomParameter().get(SUMMARY_CONFIG);
		List<SummaryConfigElementVO> lSummaryConfigElementVOList = new ArrayList<>();

		if (objectList instanceof List) {
			for (Object listObject : (List) objectList) {
				Map<String, Object> mapObject = (Map<String, Object>) listObject;

				SummaryConfigElementVO summaryCoumnVo = new SummaryConfigElementVO();
				String metadataCode = (String) mapObject.get("metadataCode");
				Metadata metadata = schemasManager.getSchemaTypes(collection).getSchema(TypeConvertionUtil.getSchemaCode(metadataCode)).getMetadatas().getMetadataWithLocalCode(TypeConvertionUtil.getMetadataLocalCode(metadataCode));
				MetadataToVOBuilder metadataToVOBuilder = new MetadataToVOBuilder();
				MetadataVO metadataVO = metadataToVOBuilder.build(metadata, view.getSessionContext());

				summaryCoumnVo.setMetadataVO(metadataVO);
				summaryCoumnVo.setPrefix((String) mapObject.get("prefix"));
				summaryCoumnVo.setAlwaysShown((Boolean) mapObject.get(IS_ALWAYS_SHOWN));
				summaryCoumnVo.setReferenceMetadataDisplay((Integer) mapObject.get(REFERENCE_METADATA_DISPLAY));

				lSummaryConfigElementVOList.add(summaryCoumnVo);
			}
		}

		return lSummaryConfigElementVOList;
	}


	public void modifyMetadataForSummaryConfig(SummaryConfigParams summaryConfigParams) {
		Map<String, Object> modifiableMap = copyUnModifiableMapToModifiableMap((Map) getSummaryMetadata().getCustomParameter());

		List<Map<String, Object>> list = (List) modifiableMap.get(SUMMARY_CONFIG);

		Map<String, Object> summaryInmap = getMapInList(list, summaryConfigParams.getMetadataVO().getCode());

		summaryInmap.put(PREFIX, summaryConfigParams.getPrefix());
		summaryInmap.put(IS_ALWAYS_SHOWN, summaryConfigParams.getDisplayCondition() == SummaryConfigParams.DisplayCondition.ALWAYS);
		if (summaryConfigParams.getReferenceMetadataDisplay() != null) {
			summaryInmap.put(REFERENCE_METADATA_DISPLAY, summaryConfigParams.getReferenceMetadataDisplay().ordinal());
		}

		saveMetadata(modifiableMap);
	}

	public void deleteMetadataForSummaryConfig(SummaryConfigElementVO summaryConfigParams) {
		Map<String, Object> modifiableMap = copyUnModifiableMapToModifiableMap(getSummaryMetadata().getCustomParameter());

		List<Map<String, Object>> list = (List) modifiableMap.get(SUMMARY_CONFIG);

		Map<String, Object> summaryInmap = getMapInList(list, summaryConfigParams.getMetadataVO().getCode());

		list.remove(summaryInmap);

		saveMetadata(modifiableMap);
	}

	public Map<String, Object> getMapInList(List<Map<String, Object>> list, String metadataCode) {
		if (list == null) {
			return null;
		}
		for (Map<String, Object> map : list) {
			if (((String) map.get(METADATA_CODE)).equalsIgnoreCase(metadataCode)) {
				return map;
			}
		}

		return null;
	}

	public int findMetadataIndex(List<SummaryConfigElementVO> metadataList, String metadaCode) {
		for (int i = 0; i < metadataList.size(); i++) {
			SummaryConfigElementVO summaryConfigElementVO = metadataList.get(i);
			if (summaryConfigElementVO.getMetadataVO().getCode().equalsIgnoreCase(metadaCode)) {
				return i;
			}
		}
		return -1;
	}

	public int findMetadataInMapList(List<Map<String, Object>> mapList, String metadataCode) {
		int index = -1;

		if (mapList == null) {
			return index;
		}


		for (int i = 0; i < mapList.size(); i++) {
			if (metadataCode.equalsIgnoreCase((String) mapList.get(i).get(METADATA_CODE))) {
				index = i;
				break;
			}
		}

		return index;
	}

	public void moveMetadataDown(String metadataCode) {
		Map<String, Object> modifiableMap = copyUnModifiableMapToModifiableMap((Map) getSummaryMetadata().getCustomParameter());
		List<Map<String, Object>> list = (List) modifiableMap.get(SUMMARY_CONFIG);

		int index = findMetadataInMapList(list, metadataCode);

		if (index >= list.size() - 1) {
			return;
		}

		Collections.swap(list, index, index + 1);

		modifiableMap.put(SUMMARY_CONFIG, list);
		saveMetadata(modifiableMap);
	}

	public void moveMetadataUp(String metadataCode) {
		Map<String, Object> modifiableMap = copyUnModifiableMapToModifiableMap((Map) getSummaryMetadata().getCustomParameter());
		List<Map<String, Object>> list = (List) modifiableMap.get(SUMMARY_CONFIG);

		int index = findMetadataInMapList(list, metadataCode);

		if (index <= 0) {
			return;
		}

		Collections.swap(list, index, index - 1);

		modifiableMap.put(SUMMARY_CONFIG, list);
		saveMetadata(modifiableMap);
	}

	public void addMetadaForSummary(SummaryConfigParams summaryConfigParams) {
		Map<String, Object> modifiableMap = copyUnModifiableMapToModifiableMap((Map) getSummaryMetadata().getCustomParameter());

		List<Map<String, Object>> list = (List) modifiableMap.get(SUMMARY_CONFIG);

		if (list == null) {
			list = new ArrayList();
		}

		Map summaryInmap = new HashMap();
		summaryInmap.put(PREFIX, summaryConfigParams.getPrefix());
		summaryInmap.put(IS_ALWAYS_SHOWN, summaryConfigParams.getDisplayCondition() == SummaryConfigParams.DisplayCondition.ALWAYS);
		summaryInmap.put(METADATA_CODE, summaryConfigParams.getMetadataVO().getCode());
		if (summaryConfigParams.getReferenceMetadataDisplay() != null) {
			summaryInmap.put(REFERENCE_METADATA_DISPLAY, summaryConfigParams.getReferenceMetadataDisplay().ordinal());
		}
		list.add(summaryInmap);

		modifiableMap.put(SUMMARY_CONFIG, list);
		saveMetadata(modifiableMap);
	}

	public void saveMetadata(final Map<String, Object> customParameter) {
		MetadataSchemasManager schemasManager = modelLayerFactory.getMetadataSchemasManager();
		if (!LangUtils.areNullableEqual(originalCustomParametersSummaryConfig, customParameter.get(SUMMARY_CONFIG))
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

	public boolean isThereAModification(SummaryConfigParams summaryConfigParams) {
		Map<String, Object> map = getMapInList(originalCustomParametersSummaryConfig, summaryConfigParams.getMetadataVO().getCode());

		if (map == null) {
			return true;
		}

		SummaryConfigParams.DisplayCondition displayCondition = SummaryConfigParams.DisplayCondition.COMPLETED;
		String alwaysShown = (String) map.get(IS_ALWAYS_SHOWN);
		if (Boolean.TRUE.equals(alwaysShown)) {
			displayCondition = SummaryConfigParams.DisplayCondition.ALWAYS;
		}

		SummaryConfigParams.ReferenceMetadataDisplay referenceMetadataDisplay = SummaryConfigParams.ReferenceMetadataDisplay.fromInteger((Integer) map.get(REFERENCE_METADATA_DISPLAY));

		return !(map.get(METADATA_CODE).equals(summaryConfigParams.getMetadataVO().getCode())
				 && map.get(PREFIX).equals(summaryConfigParams.getPrefix())
				 && displayCondition == summaryConfigParams.getDisplayCondition()
				 && referenceMetadataDisplay == summaryConfigParams.getReferenceMetadataDisplay());


	}

	private boolean isListMapEqual(List<Map<String, Object>> listMap1, List<Map<String, Object>> listMap2) {
		if (listMap1 == null && listMap2 != null || listMap1 != null && listMap2 == null || listMap1.size() != listMap2.size()) {
			return false;
		}

		for (int i = 0; i < listMap1.size(); i++) {
			for (String stringObjectMap : listMap1.get(i).keySet()) {
				if (!listMap1.get(i).get(stringObjectMap)
						.equals(listMap2.get(i).get(stringObjectMap))) {
					return false;
				}
			}
		}
		return true;
	}

	protected Map<String, Object> copyUnModifiableMapToModifiableMap(Map<String, Object> map) {

		Map<String, Object> newModifiableMap = new HashMap<>();

		for (String key : map.keySet()) {
			Object object = map.get(key);

			newModifiableMap.put(key, getModifiableValue(object));
		}

		return newModifiableMap;
	}

	protected Object getModifiableValue(Object object) {
		if (object instanceof List) {
			List<Object> newList = new ArrayList<>();
			for (Object itemInList : ((List) object)) {
				newList.add(getModifiableValue(itemInList));
			}
			return newList;
		} else if (object instanceof Map) {
			Map<String, Object> oldMap = (Map<String, Object>) object;
			Map<String, Object> newMap = new HashMap<>();
			for (String key : oldMap.keySet()) {
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

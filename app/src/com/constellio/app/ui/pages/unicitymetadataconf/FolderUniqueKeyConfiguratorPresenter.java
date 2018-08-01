package com.constellio.app.ui.pages.unicitymetadataconf;

import com.constellio.app.modules.rm.model.calculators.folder.FolderUniqueKeyCalculator;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.ui.entities.FolderUnicityVO;
import com.constellio.app.ui.entities.MetadataVO;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FolderUniqueKeyConfiguratorPresenter extends SingleSchemaBasePresenter<FolderUniqueKeyConfiguratorView> {

	private static final String SCHEMA_CODE = "schemaCode";
	public static final String PREFIX = "prefix";
	public static final String METADATA_CODE = "metadataCode";
	public static final String UNIQUE_KEY_CONFIG = FolderUniqueKeyCalculator.UNIQUE_KEY_CONFIG;


	List<Map<String, Object>> originalCustomParametersSummaryColumn;

	public FolderUniqueKeyConfiguratorPresenter(FolderUniqueKeyConfiguratorView view) {
		super(view);
	}

	public FolderUniqueKeyConfiguratorPresenter(FolderUniqueKeyConfiguratorView view, String schemaCode) {
		super(view, schemaCode);
	}


	public void forParams(String params) {
		Map<String, String> paramsMap = ParamUtils.getParamsMap(params);
		this.setSchemaCode(paramsMap.get(SCHEMA_CODE));

		if (getFolderUnicityMetadata().getCustomParameter() != null) {
			originalCustomParametersSummaryColumn = (List<Map<String, Object>>) getFolderUnicityMetadata().getCustomParameter().get(UNIQUE_KEY_CONFIG);
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
			if (metadata.isEnabled() && !metadata.isSystemReserved() && metadata.getType() != MetadataValueType.STRUCTURE) {
				metadataVOs
						.add(builder.build(metadata, view.getSessionContext()));
			}
		}

		return metadataVOs;
	}

	public List<FolderUnicityVO> folderUnicityVOList() {
		MetadataSchemasManager schemasManager = modelLayerFactory.getMetadataSchemasManager();
		Object objectList = getFolderUnicityMetadata().getCustomParameter().get(UNIQUE_KEY_CONFIG);
		List<FolderUnicityVO> lSummaryColumnVOList = new ArrayList<>();

		if (objectList instanceof List) {
			for (Object listObject : (List) objectList) {
				Map<String, Object> mapObject = (Map<String, Object>) listObject;

				FolderUnicityVO folderUnicityVO = new FolderUnicityVO();
				String metadataCode = (String) mapObject.get("metadataCode");
				Metadata metadata = schemasManager.getSchemaTypes(collection).getSchema(TypeConvertionUtil.getSchemaCode(metadataCode))
						.getMetadatas().getMetadataWithLocalCode(TypeConvertionUtil.getMetadataLocalCode(metadataCode));
				MetadataToVOBuilder metadataToVOBuilder = new MetadataToVOBuilder();
				MetadataVO metadataVO = metadataToVOBuilder.build(metadata, view.getSessionContext());

				folderUnicityVO.setMetadataVO(metadataVO);

				lSummaryColumnVOList.add(folderUnicityVO);
			}
		}

		return lSummaryColumnVOList;
	}

	public void deleteMetadataForSummaryColumn(FolderUnicityVO summaryColumnParams) {
		Map<String, Object> modifiableMap = copyUnModifiableMapToModifiableMap(getFolderUnicityMetadata().getCustomParameter());

		List<Map<String, Object>> list = (List) modifiableMap.get(UNIQUE_KEY_CONFIG);

		Map<String, Object> summaryInmap = getMapInList(list, summaryColumnParams.getMetadataVO().getCode());

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

	public void addMetadaForUnicity(FolderUniqueKeyParams folderUniqueKeyParams) {
		Map<String, Object> modifiableMap = copyUnModifiableMapToModifiableMap((Map) getFolderUnicityMetadata().getCustomParameter());

		List<Map<String, Object>> list = (List) modifiableMap.get(UNIQUE_KEY_CONFIG);

		if (list == null) {
			list = new ArrayList();
		}

		Map folderUnicityMap = new HashMap();
		folderUnicityMap.put(METADATA_CODE, folderUniqueKeyParams.getMetadataVO().getCode());
		list.add(folderUnicityMap);

		modifiableMap.put(UNIQUE_KEY_CONFIG, list);
		saveMetadata(modifiableMap);
	}

	public void saveMetadata(final Map<String, Object> customParameter) {
		MetadataSchemasManager schemasManager = modelLayerFactory.getMetadataSchemasManager();
		if (!LangUtils.areNullableEqual(originalCustomParametersSummaryColumn, customParameter.get(UNIQUE_KEY_CONFIG))
			&& !appLayerFactory.getSystemGlobalConfigsManager().isReindexingRequired()) {
			appLayerFactory.getSystemGlobalConfigsManager().setReindexingRequired(true);
		}
		schemasManager.modify(collection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchema(getSchemaCode()).get(Folder.UNIQUE_KEY).setCustomParameter(customParameter);
			}
		});
	}

	protected static Map<String, Object> copyUnModifiableMapToModifiableMap(Map<String, Object> map) {

		Map<String, Object> newModifiableMap = new HashMap<>();

		for (String key : map.keySet()) {
			Object object = map.get(key);

			newModifiableMap.put(key, getModifiableValue(object));
		}

		return newModifiableMap;
	}

	protected static Object getModifiableValue(Object object) {
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


	public Metadata getFolderUnicityMetadata() {
		MetadataSchemasManager schemasManager = modelLayerFactory.getMetadataSchemasManager();
		Metadata metadata = schemasManager.getSchemaTypes(collection).getSchema(getSchemaCode()).getMetadata(Folder.UNIQUE_KEY);
		return metadata;
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return true;
	}

	public boolean isReindextionFlag() {
		return appLayerFactory.getSystemGlobalConfigsManager().isReindexingRequired();
	}

}

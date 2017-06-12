package com.constellio.app.ui.framework.data;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.data.utils.AccentApostropheCleaner;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.MetadataSchemasManager;

@SuppressWarnings("serial")
public class SchemaVODataProvider implements Serializable {
	transient MetadataSchemasManager schemasManager;
	transient List<MetadataSchemaVO> schemas;

	MetadataSchemaToVOBuilder voBuilder;
	String typeCode;
	String collection;
	SessionContext sessionContext;

	public SchemaVODataProvider(MetadataSchemaToVOBuilder voBuilder, ModelLayerFactory modelLayerFactory, String collection,
			String code, SessionContext sessionContext) {
		this.voBuilder = voBuilder;
		this.collection = collection;
		this.typeCode = code;
		this.sessionContext = sessionContext;
		init(modelLayerFactory);
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
		init(constellioFactories.getModelLayerFactory());
	}

	void init(ModelLayerFactory modelLayerFactory) {
		schemasManager = modelLayerFactory.getMetadataSchemasManager();
		schemas = initSchemaVO();
	}

	public MetadataSchemaVO getSchemaVO(Integer index) {
		return schemas.get(index);
	}

	public int size() {
		return schemas.size();
	}

	public List<Integer> list() {
		List<Integer> listInt = new ArrayList<>();
		for (int i = 0; i < schemas.size(); i++) {
			listInt.add(i);
		}

		return listInt;
	}

	public List<MetadataSchemaVO> listSchemaVO(int startIndex, int count) {
		int toIndex = startIndex + count;
		if (startIndex > schemas.size()) {
			return new ArrayList<>();
		} else if (toIndex > schemas.size()) {
			toIndex = schemas.size();
		}
		return schemas.subList(startIndex, toIndex);
	}

	public void sort(Object[] propertyId, boolean[] ascending) {
		final boolean asc = ascending[0];

		Collections.sort(schemas, new Comparator<MetadataSchemaVO>() {
			@Override
			public int compare(MetadataSchemaVO o1, MetadataSchemaVO o2) {
				int result = o1.getLabel().compareTo(o2.getLabel());
				return asc ? result : -result;
			}
		});
	}

	private List<MetadataSchemaVO> initSchemaVO() {
		List<MetadataSchemaVO> result = new ArrayList<>();

		MetadataSchemaTypes types = schemasManager.getSchemaTypes(collection);
		if (types == null) {
			return result;
		}

		MetadataSchemaType type = types.getSchemaType(typeCode);

		for (MetadataSchema schema : type.getCustomSchemas()) {
			result.add(voBuilder.build(schema, VIEW_MODE.TABLE, sessionContext));
		}

		Collections.sort(result, new Comparator<MetadataSchemaVO>() {
			@Override
			public int compare(MetadataSchemaVO o1, MetadataSchemaVO o2) {
				String s1 = AccentApostropheCleaner.removeAccents(o1.getLabel().toLowerCase());
				String s2 = AccentApostropheCleaner.removeAccents(o2.getLabel().toLowerCase());

				return s1.compareTo(s2);
			}
		});

		result.add(0, voBuilder.build(type.getDefaultSchema(), VIEW_MODE.TABLE, sessionContext));

		return result;
	}
}

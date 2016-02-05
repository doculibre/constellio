package com.constellio.app.ui.framework.data;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.constellio.app.extensions.AppLayerCollectionExtensions;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.MetadataSchemaTypeVO;
import com.constellio.app.ui.framework.builders.MetadataSchemaTypeToVOBuilder;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.taxonomies.TaxonomiesManager;

@SuppressWarnings("serial")
public class SchemaTypeVODataProvider implements Serializable {
	transient MetadataSchemasManager schemasManager;
	transient TaxonomiesManager taxonomiesManager;
	transient List<MetadataSchemaTypeVO> schemaTypes;
	transient AppLayerCollectionExtensions extensions;

	MetadataSchemaTypeToVOBuilder voBuilder;
	String collection;

	public SchemaTypeVODataProvider(MetadataSchemaTypeToVOBuilder voBuilder, AppLayerFactory appLayerFactory,
			String collection) {
		this.voBuilder = voBuilder;
		this.collection = collection;
		init(appLayerFactory);
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
		init(constellioFactories.getAppLayerFactory());
	}

	void init(AppLayerFactory appLayerFactory) {
		ModelLayerFactory modelLayerFactory = appLayerFactory.getModelLayerFactory();
		schemasManager = modelLayerFactory.getMetadataSchemasManager();
		taxonomiesManager = modelLayerFactory.getTaxonomiesManager();
		extensions = appLayerFactory.getExtensions().forCollection(collection);
		schemaTypes = initSchemaTypes();
	}

	public MetadataSchemaTypeVO getSchemaTypeVO(Integer index) {
		return schemaTypes.get(index);
	}

	public int size() {
		return schemaTypes.size();
	}

	public List<Integer> list() {
		List<Integer> listInt = new ArrayList<>();
		for (int i = 0; i < schemaTypes.size(); i++) {
			listInt.add(i);
		}
		return listInt;
	}

	public List<MetadataSchemaTypeVO> listSchemaTypeVO() {
		return schemaTypes;
	}

	public void sort(Object[] propertyId, boolean[] ascending) {
		final boolean asc = ascending[0];

		Collections.sort(schemaTypes, new Comparator<MetadataSchemaTypeVO>() {
			@Override
			public int compare(MetadataSchemaTypeVO o1, MetadataSchemaTypeVO o2) {
				int result = o1.getLabel().compareTo(o2.getLabel());
				return asc ? result : -result;
			}
		});
	}

	private List<MetadataSchemaTypeVO> initSchemaTypes() {
		List<MetadataSchemaTypeVO> result = new ArrayList<>();

		MetadataSchemaTypes types = schemasManager.getSchemaTypes(collection);
		if (types == null) {
			return result;
		}

		for (final MetadataSchemaType type : types.getSchemaTypes()) {
			boolean visible = false;
			Taxonomy taxonomy = taxonomiesManager.getTaxonomyFor(type.getCollection(), type.getCode());
			if (taxonomy != null) {
				visible = true;

			} else if (type.hasSecurity() || type.getCode().startsWith("ddv")) {
				visible = true;

			} else if (type.getCode().equals(RetentionRule.SCHEMA_TYPE)) {
				visible = true;
			}

			visible = extensions.isSchemaTypeConfigurable(visible, type);

			if (visible) {
				result.add(voBuilder.build(type));
			}
		}

		return result;
	}
}

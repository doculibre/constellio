package com.constellio.app.ui.framework.data;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.framework.builders.MetadataToVOBuilder;
import com.constellio.app.ui.pages.management.schemas.schema.MetadataValueForProperty;
import com.constellio.data.utils.comparators.AbstractTextComparator;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.MetadataSchemasManager;

import java.io.IOException;
import java.io.Serializable;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@SuppressWarnings("serial")
public class MetadataVODataProvider implements Serializable {
	transient MetadataSchemasManager schemasManager;
	protected MetadataToVOBuilder voBuilder;
	List<MetadataVO> metadataVOs;
	String schemaCode;
	String collection;
	private MetadataValueForProperty metadataValueForProperty;

	public MetadataVODataProvider(MetadataToVOBuilder voBuilder, ModelLayerFactory modelLayerFactory, String collection,
								  String code) {
		this(voBuilder, modelLayerFactory, collection, code, null);
	}

	public MetadataVODataProvider(MetadataToVOBuilder voBuilder, ModelLayerFactory modelLayerFactory, String collection,
								  String code, MetadataValueForProperty metadataValueForProperty) {
		this.voBuilder = voBuilder;
		this.collection = collection;
		this.schemaCode = code;
		init(modelLayerFactory);
		metadataVOs = buildList();
		this.metadataValueForProperty = metadataValueForProperty;
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
		init(constellioFactories.getModelLayerFactory());
	}

	void init(ModelLayerFactory modelLayerFactory) {
		schemasManager = modelLayerFactory.getMetadataSchemasManager();
	}

	public MetadataVO getMetadataVO(String code) {
		for (MetadataVO type : metadataVOs) {
			if (type.getCode().equals(code)) {
				return type;
			}
		}
		return null;
	}

	public MetadataVO getMetadataVO(Integer index) {
		return metadataVOs.get(index);
	}

	public int size() {
		return metadataVOs != null ? metadataVOs.size() : 0;
	}

	public List<Integer> list() {
		List<Integer> listInt = new ArrayList<>();
		for (int i = 0; i < metadataVOs.size(); i++) {
			listInt.add(i);
		}
		return listInt;
	}

	public List<MetadataVO> listMetadataVO() {
		return metadataVOs;
	}

	public List<MetadataVO> listMetadataVO(int startIndex, int count) {
		if (startIndex > metadataVOs.size()) {
			return new ArrayList<>();
		}
		int toIndex = startIndex + count;
		if (toIndex > metadataVOs.size()) {
			toIndex = metadataVOs.size();
		}
		return metadataVOs.subList(startIndex, toIndex);
	}

	public void sort(Object[] propertyId, final boolean[] ascending) {

		Collator collatorForCompr = null;

		if (metadataValueForProperty != null) {
			collatorForCompr = Collator.getInstance(metadataValueForProperty.getCurrentLocale());
		}

		Collator finalCollatorForCompr = collatorForCompr;
		Collections.sort(metadataVOs, new Comparator<MetadataVO>() {
			@Override
			public int compare(MetadataVO o1, MetadataVO o2) {
				String metadata1Value = o1.getLabel();
				String metadata2Value = o2.getLabel();

				if (metadataValueForProperty != null) {
					metadata1Value = (String) metadataValueForProperty.getValue(propertyId[0], o1);
					metadata2Value = (String) metadataValueForProperty.getValue(propertyId[0], o2);
				}

				return (ascending[0] ? 1 : -1) * finalCollatorForCompr.compare(metadata1Value, metadata2Value);
			}
		});
	}

	protected List<MetadataVO> buildList() {
		List<MetadataVO> result = new ArrayList<>();
		MetadataSchemaTypes types = schemasManager.getSchemaTypes(collection);
		if (types != null) {
			MetadataSchema schema = types.getSchema(schemaCode);
			for (Metadata meta : schema.getMetadatas()) {
				if (isAccepted(meta)) {
					result.add(voBuilder.build(meta));
				}
			}
		}
		Collections.sort(result, new AbstractTextComparator<MetadataVO>() {
			@Override
			protected String getText(MetadataVO object) {
				return object.getLabel();
			}
		});
		return result;
	}

	protected boolean isAccepted(Metadata meta) {
		return !meta.isSystemReserved();
	}
}

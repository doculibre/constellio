package com.constellio.app.ui.framework.containers;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.items.RecordVOItem;
import com.constellio.model.entities.records.Record;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordHierarchyServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.taxonomies.TaxonomiesSearchOptions;
import com.vaadin.data.Container;
import com.vaadin.data.Property;
import com.vaadin.data.util.AbstractProperty;

import java.util.Arrays;
import java.util.Collection;

public class TaxonomyConceptsWithChildrenCountContainer extends ContainerAdapter {

	public static final String CHILDREN_COUNT = "taxonomyChildrenCount";

	transient ModelLayerFactory modelLayerFactory;
	String collection;
	private String username;
	private String taxonomy;
	private String schemaType;

	public TaxonomyConceptsWithChildrenCountContainer(Container adapted, String collection, String username,
													  String taxonomy,
													  String schemaType) {
		super(adapted);
		this.collection = collection;
		this.username = username;
		this.taxonomy = taxonomy;
		this.schemaType = schemaType;
		init();
	}

	public TaxonomyConceptsWithChildrenCountContainer(Container adapted, String collection, String username,
													  String taxonomy,
													  String schemaType,
													  ModelLayerFactory modelLayerFactory) {
		this(adapted, username, taxonomy, schemaType, collection);
		this.modelLayerFactory = modelLayerFactory;
		init();
	}

	public void init() {
		if (modelLayerFactory == null) {
			modelLayerFactory = ConstellioFactories.getInstance().getModelLayerFactory();
		}
	}

	@Override
	protected Collection<?> getOwnContainerPropertyIds() {
		return Arrays.asList(CHILDREN_COUNT);
	}

	@Override
	protected Property getOwnContainerProperty(Object itemId, Object propertyId) {
		RecordVO recordVO = ((RecordVOItem) adapted.getItem(itemId)).getRecord();
		if (CHILDREN_COUNT.equals(propertyId)) {
			return newChildrenCountProperty(recordVO);
		}
		return null;
	}

	@Override
	protected Class<?> getOwnType(Object propertyId) {
		if (CHILDREN_COUNT.equals(propertyId)) {
			return Integer.class;
		}
		return null;
	}

	private Property newChildrenCountProperty(final RecordVO recordVO) {
		return new AbstractProperty<Integer>() {
			@Override
			public Integer getValue() {
				Record record = modelLayerFactory.newRecordServices().getDocumentById(recordVO.getId());
				LogicalSearchQuery query = new RecordHierarchyServices(modelLayerFactory)
						.getChildNodesQuery(taxonomy, record, new TaxonomiesSearchOptions());
				return (int) modelLayerFactory.newSearchServices().getResultsCount(query);
			}

			@Override
			public void setValue(Integer newValue)
					throws ReadOnlyException {
				throw new ReadOnlyException();
			}

			@Override
			public Class<Integer> getType() {
				return Integer.class;
			}
		};
	}

	public RecordVO getRecordVO(int itemId) {
		return ((RecordVOLazyContainer) adapted).getRecordVO(itemId);
	}
}

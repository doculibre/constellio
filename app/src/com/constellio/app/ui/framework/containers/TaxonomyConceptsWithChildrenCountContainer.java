/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.ui.framework.containers;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.items.RecordVOItem;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.search.query.ReturnedMetadatasFilter;
import com.constellio.model.services.taxonomies.TaxonomiesSearchOptions;
import com.constellio.model.services.taxonomies.TaxonomySearchRecord;
import com.vaadin.data.Container;
import com.vaadin.data.Property;
import com.vaadin.data.util.AbstractProperty;

public class TaxonomyConceptsWithChildrenCountContainer extends ContainerAdapter {

	public static final String CHILDREN_COUNT = "taxonomyChildrenCount";

	transient ModelLayerFactory modelLayerFactory;
	String collection;
	private String username;
	private String taxonomy;
	private String schemaType;

	public TaxonomyConceptsWithChildrenCountContainer(Container adapted, String collection, String username, String taxonomy,
			String schemaType) {
		super(adapted);
		this.collection = collection;
		this.username = username;
		this.taxonomy = taxonomy;
		this.schemaType = schemaType;
		init();
	}

	public TaxonomyConceptsWithChildrenCountContainer(Container adapted, String collection, String username, String taxonomy,
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
				TaxonomiesSearchOptions options = new TaxonomiesSearchOptions();
				options.setReturnedMetadatasFilter(ReturnedMetadatasFilter.idVersionSchema());
				User user = modelLayerFactory.newUserServices().getUserInCollection(username, collection);
				List<TaxonomySearchRecord> childConcepts = modelLayerFactory.newTaxonomiesSearchService().getLinkableChildConcept(
						user, record, taxonomy, schemaType, options);
				return childConcepts.size();
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

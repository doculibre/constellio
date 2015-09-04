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

import com.constellio.app.modules.rm.model.enums.DecommissioningType;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningSearchConditionFactory;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningSearchConditionFactory.ContainerSearchParameters;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.items.RecordVOItem;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.vaadin.data.Container;
import com.vaadin.data.Property;
import com.vaadin.data.util.AbstractProperty;

public class FilingSpaceWithContainersCountContainer extends ContainerAdapter {

	public static final String CONTAINERS_COUNT = "containersCount";
	public static final String DEPOSIT_PREFIX = "deposit";
	public static final String TRANSFER_PREFIX = "transfer";
	public static final String WITH_STORAGE_SPACE_SUFFIX = "WithStorageSpace";

	transient ModelLayerFactory modelLayerFactory;
	String collection;
	private String currentUserId;
	private String tabName;
	private String adminUnitId;

	public FilingSpaceWithContainersCountContainer(Container adapted, String collection, String currentUserId, String tabName,
			String adminUnitId) {
		super(adapted);
		this.collection = collection;
		this.currentUserId = currentUserId;
		this.tabName = tabName;
		this.adminUnitId = adminUnitId;
		init();
	}

	public FilingSpaceWithContainersCountContainer(Container adapted, String collection, String currentUserId, String tabName,
			String adminUnitId, ModelLayerFactory modelLayerFactory) {
		this(adapted, collection, currentUserId, tabName, adminUnitId);
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
		return Arrays.asList(CONTAINERS_COUNT);
	}

	@Override
	protected Property getOwnContainerProperty(Object itemId, Object propertyId) {
		RecordVO recordVO = ((RecordVOItem) adapted.getItem(itemId)).getRecord();
		if (CONTAINERS_COUNT.equals(propertyId)) {
			return newContainersCountProperty(recordVO);
		}
		return null;
	}

	@Override
	protected Class<?> getOwnType(Object propertyId) {
		if (CONTAINERS_COUNT.equals(propertyId)) {
			return Long.class;
		}
		return null;
	}

	private Property newContainersCountProperty(final RecordVO recordVO) {
		return new AbstractProperty<Long>() {
			@Override
			public Long getValue() {
				DecommissioningSearchConditionFactory searchConditionFactory = new DecommissioningSearchConditionFactory(
						collection, modelLayerFactory);
				ContainerSearchParameters parameters = new ContainerSearchParameters();
				parameters.setAdminUnitId(adminUnitId);
				if (tabName.startsWith(DEPOSIT_PREFIX)) {
					parameters.setType(DecommissioningType.DEPOSIT);
				} else if (tabName.startsWith(TRANSFER_PREFIX)) {
					parameters.setType(DecommissioningType.TRANSFERT_TO_SEMI_ACTIVE);
				}
				parameters.setUserId(currentUserId);
				parameters.setWithStorage(tabName.endsWith(WITH_STORAGE_SPACE_SUFFIX));
				return searchConditionFactory.getVisibleContainersCount(parameters);
			}

			@Override
			public void setValue(Long newValue)
					throws ReadOnlyException {
				throw new ReadOnlyException();
			}

			@Override
			public Class<Long> getType() {
				return Long.class;
			}
		};
	}

	public RecordVO getRecordVO(int itemId) {
		return ((RecordVOLazyContainer) adapted).getRecordVO(itemId);
	}
}

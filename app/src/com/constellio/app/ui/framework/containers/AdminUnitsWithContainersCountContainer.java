package com.constellio.app.ui.framework.containers;

import com.constellio.app.modules.rm.model.enums.DecommissioningType;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningSearchConditionFactory;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningSearchConditionFactory.ContainerSearchParameters;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.items.RecordVOItem;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.vaadin.data.Container;
import com.vaadin.data.Property;
import com.vaadin.data.util.AbstractProperty;

import java.util.Arrays;
import java.util.Collection;

public class AdminUnitsWithContainersCountContainer extends ContainerAdapter {

	public static final String SUB_ADMINISTRATIVE_UNITS_COUNT = "subAdministrativeUnitsCount";
	public static final String CONTAINERS_COUNT = "containersCount";
	public static final String DEPOSIT_PREFIX = "deposit";
	public static final String TRANSFER_PREFIX = "transfer";
	public static final String WITH_STORAGE_SPACE_SUFFIX = "WithStorageSpace";

	transient AppLayerFactory appLayerFactory;
	transient ModelLayerFactory modelLayerFactory;
	String collection;
	private String currentUserId;
	private String tabName;

	public AdminUnitsWithContainersCountContainer(Container adapted, String collection, String currentUserId,
												  String tabName) {
		super(adapted);
		this.collection = collection;
		this.currentUserId = currentUserId;
		this.tabName = tabName;
		init();
	}

	public AdminUnitsWithContainersCountContainer(Container adapted, String collection, String currentUserId,
												  String tabName,
												  AppLayerFactory appLayerFactory) {
		this(adapted, collection, currentUserId, tabName);
		this.appLayerFactory = appLayerFactory;
		this.modelLayerFactory = appLayerFactory.getModelLayerFactory();
		init();
	}

	public void init() {
		if (appLayerFactory == null) {
			appLayerFactory = ConstellioFactories.getInstance().getAppLayerFactory();
		}
		modelLayerFactory = appLayerFactory.getModelLayerFactory();
	}

	@Override
	protected Collection<?> getOwnContainerPropertyIds() {
		return Arrays.asList(SUB_ADMINISTRATIVE_UNITS_COUNT, CONTAINERS_COUNT);
	}

	@Override
	protected Property getOwnContainerProperty(Object itemId, Object propertyId) {
		RecordVO recordVO = ((RecordVOItem) adapted.getItem(itemId)).getRecord();
		if (SUB_ADMINISTRATIVE_UNITS_COUNT.equals(propertyId)) {
			return newSubAdministrativeUnitCountProperty(recordVO);
		} else if (CONTAINERS_COUNT.equals(propertyId)) {
			return newContainersCountProperty(recordVO);
		}
		return null;
	}

	@Override
	protected Class<?> getOwnType(Object propertyId) {
		if (SUB_ADMINISTRATIVE_UNITS_COUNT.equals(propertyId) || CONTAINERS_COUNT.equals(propertyId)) {
			return Integer.class;
		}
		return null;
	}

	private Property newSubAdministrativeUnitCountProperty(final RecordVO recordVO) {
		return new AbstractProperty<Long>() {
			@Override
			public Long getValue() {
				DecommissioningSearchConditionFactory searchConditionFactory = new DecommissioningSearchConditionFactory(
						collection, appLayerFactory);
				return searchConditionFactory.getVisibleSubAdministrativeUnitCount(recordVO.getId());
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

	private Property newContainersCountProperty(final RecordVO recordVO) {
		return new AbstractProperty<Long>() {
			@Override
			public Long getValue() {
				DecommissioningSearchConditionFactory searchConditionFactory = new DecommissioningSearchConditionFactory(
						collection, appLayerFactory);
				ContainerSearchParameters parameters = new ContainerSearchParameters();
				parameters.setAdminUnitId(recordVO.getId());
				//parameters.setFilingSpaceId("Might end up in another adapter for other page. We'll see.");
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

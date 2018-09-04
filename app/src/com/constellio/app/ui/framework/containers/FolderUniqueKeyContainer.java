package com.constellio.app.ui.framework.containers;

import com.constellio.app.ui.entities.FolderUnicityVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.IconButton;
import com.constellio.app.ui.framework.data.FolderUniqueKeyDataProvider;
import com.constellio.app.ui.pages.unicitymetadataconf.FolderUniqueKeyConfiguratorView;
import com.vaadin.data.Property;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.server.ThemeResource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class FolderUniqueKeyContainer extends DataContainer<FolderUniqueKeyDataProvider> {
	public static final String METADATA_VO = "metadataVo";
	public static final String DELETE = "delete";

	FolderUniqueKeyConfiguratorView view;

	public FolderUniqueKeyContainer(FolderUniqueKeyDataProvider dataProvider,
									FolderUniqueKeyConfiguratorView folderUniqueKeyConfiguratorView) {
		super(dataProvider);
		this.view = folderUniqueKeyConfiguratorView;
	}

	@Override
	protected void populateFromData(FolderUniqueKeyDataProvider dataProvider) {
		for (FolderUnicityVO summaryColumnVO : dataProvider.getFolderUnicityVOs()) {
			addItem(summaryColumnVO);
		}
	}

	@Override
	protected Collection<?> getOwnContainerPropertyIds() {
		List<String> containerPropertyIds = new ArrayList<>();
		containerPropertyIds.add(METADATA_VO);
		containerPropertyIds.add(DELETE);


		return containerPropertyIds;
	}

	@Override
	protected Class<?> getOwnType(Object propertyId) {
		Class<?> type;
		if (METADATA_VO.equals(propertyId)) {
			type = MetadataVO.class;
		} else if (DELETE.equals(propertyId)) {
			type = BaseButton.class;
		} else {
			throw new IllegalArgumentException("Invalid propertyId : " + propertyId);
		}

		return type;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Override
	protected Property<?> getOwnContainerProperty(Object itemId, Object propertyId) {
		final FolderUnicityVO folderUnivityItemId = (FolderUnicityVO) itemId;
		final FolderUniqueKeyDataProvider folderUniqueKeyDataProvider = getDataProvider();
		Object value;

		if (METADATA_VO.equals(propertyId)) {
			value = folderUnivityItemId.getMetadataVO();
		} else if (DELETE.equals(propertyId)) {
			value = new IconButton(new ThemeResource("images/icons/actions/delete.png"), $("delete"), true) {
				@Override
				protected void buttonClick(ClickEvent event) {
					view.deleteRow(folderUnivityItemId);
				}
			};
		} else {
			throw new IllegalArgumentException("Invalid propertyId : " + propertyId);
		}
		Class<?> type = getType(propertyId);
		return new ObjectProperty(value, type);
	}
}

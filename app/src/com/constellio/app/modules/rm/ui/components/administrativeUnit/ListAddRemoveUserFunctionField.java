package com.constellio.app.modules.rm.ui.components.administrativeUnit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.fields.list.ListAddRemoveField;
import com.vaadin.data.Property;
import com.vaadin.data.util.AbstractProperty;
import com.vaadin.data.util.TransactionalPropertyWrapper;

public class ListAddRemoveUserFunctionField extends ListAddRemoveField<UserFunctionItem, UserFunctionField> {
	
	private RecordVO administrativeUnitVO;
	
	private List<UserFunctionItem> itemList;
	
	public ListAddRemoveUserFunctionField(RecordVO administrativeUnitVO) {
		this.administrativeUnitVO = administrativeUnitVO;
		init();
	}
	
	private void init() {
		itemList = new ArrayList<>();
		List<String> functionIds = administrativeUnitVO.get(AdministrativeUnit.FUNCTIONS);
		List<String> functionUserIds = administrativeUnitVO.get(AdministrativeUnit.FUNCTIONS_USERS);
		for (int i = 0; i < functionIds.size(); i++) {
			String functionId = functionIds.get(i);
			String userId = functionUserIds.get(i);
			itemList.add(new UserFunctionItem(functionId, userId));
		}
		setValue(itemList);
	}

	@Override
	protected UserFunctionField newAddEditField() {
		return new UserFunctionField();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void setPropertyDataSource(Property newDataSource) {
		// Ignored, dealt with manually
		super.setPropertyDataSource(new TransactionalPropertyWrapper<>(new AbstractProperty<Object>() {
			@Override
			public Object getValue() {
				return itemList;
			}

			@Override
			@SuppressWarnings("unchecked")
			public void setValue(Object newValue) throws ReadOnlyException {
				itemList = (List<UserFunctionItem>) newValue;
				if (itemList != null) {
					List<String> newFunctionIds = new ArrayList<>();
					List<String> newUserIds = new ArrayList<>();
					for (UserFunctionItem newItem : itemList) {
						newFunctionIds.add(newItem.getFunctionId());
						newUserIds.add(newItem.getUserId());
					}
					administrativeUnitVO.set(AdministrativeUnit.FUNCTIONS, newFunctionIds);
					administrativeUnitVO.set(AdministrativeUnit.FUNCTIONS_USERS, newUserIds);
				} else {
					administrativeUnitVO.set(AdministrativeUnit.FUNCTIONS, Collections.EMPTY_LIST);
					administrativeUnitVO.set(AdministrativeUnit.FUNCTIONS_USERS, Collections.EMPTY_LIST);
				}
			}

			@Override
			public Class<? extends Object> getType() {
				return List.class;
			}
		}));
	}

}

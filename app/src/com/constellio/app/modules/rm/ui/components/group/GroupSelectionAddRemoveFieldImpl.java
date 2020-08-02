package com.constellio.app.modules.rm.ui.components.group;

import com.constellio.app.modules.rm.wrappers.RMUser;
import com.constellio.app.ui.framework.components.fields.AdditionnalRecordField;
import com.constellio.app.ui.framework.components.fields.list.ListAddRemoveField;
import com.constellio.data.dao.services.bigVault.SearchResponseIterator;
import com.constellio.model.entities.records.Record;
import com.vaadin.data.util.converter.Converter.ConversionException;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.ComboBox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.constellio.app.ui.i18n.i18n.$;

public class GroupSelectionAddRemoveFieldImpl extends ListAddRemoveField<String, AbstractField<String>> implements AdditionnalRecordField<List<String>> {

	private List<Record> groups;
	private Map<String, String> groupTitles;

	public GroupSelectionAddRemoveFieldImpl(SearchResponseIterator<Record> groups) {
		super();
		setCaption($("CollectionSecurityManagement.groupsSelected"));
		addStyleName("favoritesDisplayOrder");
		setId("favoritesDisplayOrder");
		setRequired(false);
		this.groups = groups.stream().collect(Collectors.toList());
		this.groupTitles = new HashMap<>();
		this.groups.stream().forEach(record -> groupTitles.put(record.getId(), record.getTitle()));
	}

	@Override
	public String getMetadataLocalCode() {
		return RMUser.GROUPS;
	}

	@Override
	public Object getCommittableValue() {
		return getValue();
	}

	@Override
	public Class getType() {
		return List.class;
	}

	@Override
	public List<String> getValue() {
		return super.getValue();
	}

	@Override
	protected AbstractField newAddEditField() {
		ComboBox comboBox = new ComboBox();
		for (Record group : groups) {
			comboBox.addItem(group.getId());
			comboBox.setItemCaption(group.getId(), group.getTitle());
		}
		return comboBox;
	}

	@Override
	protected String getItemCaption(Object itemId) {
		if (groupTitles.containsKey(itemId)) {
			return groupTitles.get(itemId);
		}
		return super.getItemCaption(itemId);
	}

	@Override
	public void setValue(List<String> groups) throws ReadOnlyException, ConversionException {
		if (groups != null) {
			List<String> filteredList = groups.stream().filter(groupId -> groupTitles.containsKey(groupId)).collect(Collectors.toList());
			super.setValue(filteredList);
		} else {
			super.setValue(groups);
		}
	}
}

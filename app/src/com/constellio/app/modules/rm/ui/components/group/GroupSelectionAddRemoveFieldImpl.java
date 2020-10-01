package com.constellio.app.modules.rm.ui.components.group;

import com.constellio.app.modules.rm.wrappers.RMUser;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.components.display.ReferenceDisplay;
import com.constellio.app.ui.framework.components.fields.list.ListAddRemoveField;
import com.constellio.data.dao.services.bigVault.SearchResponseIterator;
import com.constellio.model.entities.records.Record;
import com.vaadin.data.Property;
import com.vaadin.data.util.converter.Converter.ConversionException;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.ComboBox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.constellio.app.ui.i18n.i18n.$;

public class GroupSelectionAddRemoveFieldImpl extends ListAddRemoveField<String, AbstractField<String>> {

	private List<Record> groups;
	private Map<String, ReferenceDisplay> groupTitles;

	public GroupSelectionAddRemoveFieldImpl(SearchResponseIterator<Record> groups) {
		super();
		RecordToVOBuilder builder = new RecordToVOBuilder();
		setCaption($("CollectionSecurityManagement.groupsSelected"));
		addStyleName("favoritesDisplayOrder");
		setId("favoritesDisplayOrder");
		setRequired(false);
		this.groups = groups.stream().collect(Collectors.toList());
		this.groupTitles = new HashMap<>();

		this.groups.stream().forEach(record -> {
			ReferenceDisplay referenceDisplay = new ReferenceDisplay(record.getId(), false);
			referenceDisplay.setCaption(referenceDisplay.getCaption());
			groupTitles.put(record.getId(), referenceDisplay);
		});
	}

	public String getMetadataLocalCode() {
		return RMUser.GROUPS;
	}

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
		comboBox.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(Property.ValueChangeEvent event) {
				tryAdd();
			}
		});
		return comboBox;
	}

	@Override
	protected String getItemCaption(Object itemId) {
		if (groupTitles.containsKey(itemId)) {
			return groupTitles.get(itemId).getCaption();
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

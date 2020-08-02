package com.constellio.app.modules.rm.ui.components.user;

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

public class UserSelectionAddRemoveFieldImpl extends ListAddRemoveField<String, AbstractField<String>> implements AdditionnalRecordField<List<String>> {

	private List<Record> users;
	private Map<String, String> userTitles;

	public UserSelectionAddRemoveFieldImpl(SearchResponseIterator<Record> users) {
		super();
		setCaption($("CollectionSecurityManagement.usersSelected"));
		addStyleName("favoritesDisplayOrder");
		setId("favoritesDisplayOrder");
		setRequired(false);
		this.users = users.stream().collect(Collectors.toList());
		this.userTitles = new HashMap<>();
		this.users.stream().forEach(record -> userTitles.put(record.getId(), record.getTitle()));
	}

	@Override
	public String getMetadataLocalCode() {
		return "user";
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
		for (Record user : users) {
			comboBox.addItem(user.getId());
			comboBox.setItemCaption(user.getId(), user.getTitle());
		}
		return comboBox;
	}

	@Override
	protected String getItemCaption(Object itemId) {
		if (userTitles.containsKey(itemId)) {
			return userTitles.get(itemId);
		}
		return super.getItemCaption(itemId);
	}

	@Override
	public void setValue(List<String> users) throws ReadOnlyException, ConversionException {
		if (users != null) {
			List<String> filteredList = users.stream().filter(groupId -> userTitles.containsKey(groupId)).collect(Collectors.toList());
			super.setValue(filteredList);
		} else {
			super.setValue(users);
		}
	}
}

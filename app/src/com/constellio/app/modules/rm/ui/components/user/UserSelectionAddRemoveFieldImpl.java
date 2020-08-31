package com.constellio.app.modules.rm.ui.components.user;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.framework.components.fields.list.ListAddRemoveField;
import com.constellio.data.dao.services.bigVault.SearchResponseIterator;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.configs.SystemConfigurationsManager;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.vaadin.data.util.converter.Converter.ConversionException;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.ComboBox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.constellio.app.ui.i18n.i18n.$;

public class UserSelectionAddRemoveFieldImpl extends ListAddRemoveField<String, AbstractField<String>> {

	private List<Record> users;
	private Map<String, String> userTitles;

	private SystemConfigurationsManager configurationsManager;
	private SchemasRecordsServices schemasRecordsServices;

	public UserSelectionAddRemoveFieldImpl(AppLayerFactory appLayerFactory, SearchResponseIterator<Record> users) {
		super();
		setCaption($("CollectionSecurityManagement.usersSelected"));
		addStyleName("favoritesDisplayOrder");
		setId("favoritesDisplayOrder");
		setRequired(false);

		configurationsManager = appLayerFactory.getModelLayerFactory().getSystemConfigurationsManager();
		schemasRecordsServices = new SchemasRecordsServices(Collection.SYSTEM_COLLECTION, appLayerFactory.getModelLayerFactory());

		this.users = users.stream().collect(Collectors.toList());
		this.userTitles = new HashMap<>();
		this.users.stream().forEach(record -> {
			userTitles.put(record.getId(), calculateTitle(schemasRecordsServices.wrapUserCredential(record)));
		});
	}

	private String calculateTitle(UserCredential credential) {
		String titlePattern = configurationsManager.getValue(ConstellioEIMConfigs.USER_TITLE_PATTERN);
		String firstName = credential.getFirstName();
		String lastName = credential.getLastName();
		String username = credential.getUsername();
		String email = credential.getEmail();

		firstName = firstName == null ? "" : firstName;
		lastName = lastName == null ? "" : lastName;
		username = username == null ? "" : username;
		email = email == null ? "" : email;

		titlePattern = titlePattern.replace("${firstName}", firstName).replace("${lastName}", lastName)
				.replace("${username}", username).replace("${email}", email);

		String pattern = ".*[A-Za-z0-9]+.*";
		boolean isValid = titlePattern.matches(pattern);
		if (!isValid) {
			titlePattern = username;
		}
		return titlePattern;
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
			comboBox.setItemCaption(user.getId(), userTitles.get(user.getId()));
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

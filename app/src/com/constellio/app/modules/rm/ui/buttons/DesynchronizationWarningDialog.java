package com.constellio.app.modules.rm.ui.buttons;

import com.constellio.model.entities.records.wrappers.User;
import com.vaadin.ui.UI;
import org.vaadin.dialogs.ConfirmDialog;

import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import static com.constellio.app.ui.i18n.i18n.$;

public class DesynchronizationWarningDialog extends ConfirmDialog {
	private List<User> synchronizedUsers;

	public DesynchronizationWarningDialog(List<User> synchronizedUsers) {
		super();
		this.synchronizedUsers = synchronizedUsers;
	}

	public void showConfirm(final UI ui, Listener listener) {

		this.show(ui,
				$("CollectionSecurityManagement.desynchronizationWarningTitle"),
				$("CollectionSecurityManagement.desynchronizationWarningMessage", getUsernamesString()),
				$("Ok"), $("cancel"),
				listener);
	}

	private String getUsernamesString() {
		List<String> synchroUsernamesList = synchronizedUsers.stream()
				.map(u -> u.getUsername()).collect(Collectors.toList());
		StringJoiner stringJoiner = new StringJoiner(", ");
		for (String username : synchroUsernamesList) {
			stringJoiner.add(username);
		}
		return stringJoiner.toString();

	}


}

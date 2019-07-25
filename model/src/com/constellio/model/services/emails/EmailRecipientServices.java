package com.constellio.model.services.emails;

import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.structures.EmailAddress;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class EmailRecipientServices {

	public static List<EmailAddress> toFilteredEmailAddressList(List<User> users) {
		List<EmailAddress> returnAddresses = new ArrayList<>();
		if (users == null) {
			return returnAddresses;
		}
		filterUsersWhoDoNotWantEmails(users);
		filterInactiveUsers(users);
		for (User user : users) {
			returnAddresses.add(new EmailAddress(user.getTitle(), user.getEmail()));
		}
		return returnAddresses;
	}

	private static void filterInactiveUsers(List<User> users) {
		if (users == null) {
			return;
		}
		Iterator<User> userIterator = users.iterator();
		while (userIterator.hasNext()) {
			User user = userIterator.next();
			if (user.isLogicallyDeletedStatus()) {
				userIterator.remove();
			}
		}
	}

	public static void filterUsersWhoDoNotWantEmails(List<User> users) {
		if (users == null) {
			return;
		}
		Iterator<User> userIterator = users.iterator();
		while (userIterator.hasNext()) {
			User user = userIterator.next();
			if (user.isNotReceivingEmails()) {
				userIterator.remove();
			}
		}
	}
}

package com.constellio.app.modules.rm.wrappers;

import com.constellio.app.modules.rm.wrappers.structures.Comment;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class AdministrativeUnit extends RecordWrapper {

	public static final String SCHEMA_TYPE = "administrativeUnit";

	public static final String DEFAULT_SCHEMA = SCHEMA_TYPE + "_default";

	public static final String CODE = "code";
	public static final String DESCRIPTION = "description";
	public static final String PARENT = "parent";
	public static final String FILING_SPACES = "filingSpaces";
	public static final String ADRESS = "adress";
	public static final String FILING_SPACES_USERS = "filingSpacesUsers";
	public static final String FILING_SPACES_ADMINISTRATORS = "filingSpacesAdmins";
	public static final String COMMENTS = "comments";
	public static final String DECOMMISSIONING_MONTH = "decommissioningMonth";
	public static final String ANCESTORS = "unitAncestors";
	public static final String FUNCTIONS = "functions";
	public static final String FUNCTIONS_USERS = "functionUsers";
	public static final String ABBREVIATION = "abbreviation";

	@Deprecated
	public AdministrativeUnit(Record record,
							  MetadataSchemaTypes types) {
		super(record, types, SCHEMA_TYPE);
	}

	@Deprecated
	protected AdministrativeUnit(Record record,
								 MetadataSchemaTypes types, String schemaCode) {
		super(record, types, schemaCode);
	}

	public AdministrativeUnit(Record record,
							  MetadataSchemaTypes types, Locale locale) {
		super(record, types, SCHEMA_TYPE, locale);
	}

	protected AdministrativeUnit(Record record,
								 MetadataSchemaTypes types, String schemaCode, Locale locale) {
		super(record, types, schemaCode, locale);
	}

	public AdministrativeUnit setTitle(String title) {
		super.setTitle(title);
		return this;
	}

	public AdministrativeUnit setTitle(Locale locale, String title) {
		super.setTitle(locale, title);
		return this;
	}

	public String getCode() {
		return get(CODE);
	}

	public AdministrativeUnit setCode(String code) {
		set(CODE, code);
		return this;
	}

	public List<String> getAncestors() {
		return getList(ANCESTORS);
	}


	public String getAdress() {
		return get(ADRESS);
	}

	public AdministrativeUnit setAdress(String adress) {
		set(ADRESS, adress);
		return this;
	}


	private int getFunctionIndex(UserFunction function, User user) {
		String functionId = function.getId();
		String userId = user.getId();
		return getFunctionIndex(functionId, userId);
	}

	private int getFunctionIndex(String administrativeFunctionId, String userId) {
		List<String> functions = getFunctions();
		List<String> functionUsers = getFunctionUsers();

		for (int i = 0; i < functions.size(); i++) {
			if (userId.equals(functionUsers.get(i)) && administrativeFunctionId.equals(functions.get(i))) {
				return i;
			}
		}

		return -1;
	}


	public List<String> getFunctions() {
		return Collections.unmodifiableList(this.<String>getList(FUNCTIONS));
	}

	public List<String> getFunctionUsers() {
		return Collections.unmodifiableList(this.<String>getList(FUNCTIONS_USERS));
	}

	public AdministrativeUnit addFunction(UserFunction function, User user) {

		if (getFunctionIndex(function, user) == -1) {
			List<String> functions = new ArrayList<>(getFunctions());
			List<String> functionUsers = new ArrayList<>(getFunctionUsers());

			functions.add(function.getId());
			functionUsers.add(user.getId());

			set(FUNCTIONS, functions);
			set(FUNCTIONS_USERS, functionUsers);
		}

		return this;
	}

	public AdministrativeUnit removeFunction(UserFunction function, User user) {
		int index;
		while ((index = getFunctionIndex(function, user)) != -1) {
			List<String> functions = new ArrayList<>(getFunctions());
			List<String> functionUsers = new ArrayList<>(getFunctionUsers());

			functions.remove(index);
			functionUsers.remove(index);

			set(FUNCTIONS, functions);
			set(FUNCTIONS_USERS, functionUsers);
		}
		return this;
	}

	public AdministrativeUnit setFunctions(List<?> functions, List<?> users) {
		if (functions.size() != users.size()) {
			throw new IllegalArgumentException("Functions and users list must have same size");
		}
		set(FUNCTIONS, functions);
		set(FUNCTIONS_USERS, users);
		return this;
	}

	public List<String> getUsersWithFunction(UserFunction function) {

		List<String> functions = getFunctions();
		List<String> functionUsers = getFunctionUsers();

		List<String> foundUsers = new ArrayList<>();

		if (functions != null && functionUsers != null && functions.size() == functionUsers.size()) {
			for (int i = 0; i < functions.size(); i++) {
				if (function.getId().equals(functions.get(i))) {
					foundUsers.add(functionUsers.get(i));
				}
			}
		}

		return Collections.unmodifiableList(foundUsers);
	}


	public List<String> getFunctionsOfUser(User user) {

		List<String> functions = getFunctions();
		List<String> functionUsers = getFunctionUsers();

		List<String> foundFunctions = new ArrayList<>();

		for (int i = 0; i < functions.size(); i++) {
			if (user.getId().equals(functionUsers.get(i))) {
				foundFunctions.add(functions.get(i));
			}
		}

		return Collections.unmodifiableList(foundFunctions);
	}

	public String getDescription() {
		return get(DESCRIPTION, locale);
	}


	public AdministrativeUnit setDescription(String description) {
		set(DESCRIPTION, description);
		return this;
	}

	public AdministrativeUnit setDescription(Locale locale, String description) {
		set(DESCRIPTION, locale, description);
		return this;
	}

	public String getParent() {
		return get(PARENT);
	}

	public AdministrativeUnit setParent(AdministrativeUnit parent) {
		set(PARENT, parent);
		return this;
	}

	public AdministrativeUnit setParent(Record parent) {
		set(PARENT, parent);
		return this;
	}

	public AdministrativeUnit setParent(String parent) {
		set(PARENT, parent);
		return this;
	}

	public List<String> getFilingSpaces() {
		return getList(FILING_SPACES);
	}

	public AdministrativeUnit setFilingSpaces(List<?> filingSpaces) {
		set(FILING_SPACES, filingSpaces);
		return this;
	}

	public List<Comment> getComments() {
		return getList(COMMENTS);
	}

	public AdministrativeUnit setComments(List<Comment> comments) {
		set(COMMENTS, comments);
		return this;
	}

	public int getDecommissioningMonth() {
		return get(DECOMMISSIONING_MONTH);
	}

	public AdministrativeUnit setDecommissioningMonth(int decommissioningMonth) {
		set(DECOMMISSIONING_MONTH, decommissioningMonth);
		return this;
	}

	public List<String> getFilingSpacesUsers() {
		return getList(FILING_SPACES_USERS);
	}

	public List<String> getFilingSpacesAdministrators() {
		return getList(FILING_SPACES_ADMINISTRATORS);
	}

	public String getAbbreviation() {
		return get(ABBREVIATION);
	}
}

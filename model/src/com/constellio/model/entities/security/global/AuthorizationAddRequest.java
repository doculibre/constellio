package com.constellio.model.entities.security.global;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Authorization;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.records.RecordUtils;
import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

public class AuthorizationAddRequest {

	private String id;
	private String collection;
	private List<String> principals;
	private String target;
	private List<String> roles;
	private Authorization existingAuthorization;
	private LocalDate start, end;
	private User executedBy;
	private String sharedBy;
	private boolean negative;
	private boolean overridingInheritedAuths;
	private boolean nested;

	private AuthorizationAddRequest(String collection) {
		this(collection, null);
	}

	private AuthorizationAddRequest(String collection, String id) {
		this.principals = principals;
		this.collection = collection;
		this.id = id;
	}

	public AuthorizationAddRequest forPrincipalsIds(List<String> principals) {
		this.principals = principals;
		return this;
	}

	public AuthorizationAddRequest forUsers(String... users) {
		this.principals = asList(users);
		return this;
	}

	public AuthorizationAddRequest forGroups(Group... groups) {
		this.principals = new RecordUtils().toWrappedRecordIdsList(asList(groups));
		return this;
	}

	public AuthorizationAddRequest forUsers(User... users) {
		this.principals = new RecordUtils().toWrappedRecordIdsList(asList(users));
		return this;
	}

	public AuthorizationAddRequest forPrincipalsIds(String... principals) {
		this.principals = asList(principals);
		return this;
	}

	public AuthorizationAddRequest sharedBy(String userId) {
		this.sharedBy = userId;
		return this;
	}

	public AuthorizationAddRequest sharedBy(User user) {
		this.sharedBy = user.getId();
		return this;
	}

	public AuthorizationAddRequest on(String targetWrappersId) {
		this.target = targetWrappersId;
		return this;
	}

	public AuthorizationAddRequest startingOn(LocalDate date) {
		this.start = date;
		return this;
	}

	public AuthorizationAddRequest endingOn(LocalDate date) {
		this.end = date;
		return this;
	}

	public AuthorizationAddRequest during(LocalDate start, LocalDate end) {
		this.start = start;
		this.end = end;
		return this;
	}

	public AuthorizationAddRequest on(Record targetRecord) {
		this.target = targetRecord.getId();
		return this;
	}

	public AuthorizationAddRequest on(RecordWrapper targetRecord) {
		this.target = targetRecord.getId();
		return this;
	}

	private AuthorizationAddRequest withRoles(List<String> roles) {
		this.roles = roles;
		return this;
	}

	private AuthorizationAddRequest setNegative(boolean negative) {
		this.negative = negative;
		return this;
	}

	public AuthorizationAddRequest givingReadDeleteAccess() {
		return withRoles(asList(Role.READ, Role.DELETE));
	}

	public AuthorizationAddRequest givingDeleteAccess() {
		return withRoles(asList(Role.DELETE));
	}

	public AuthorizationAddRequest givingWriteAccess() {
		return withRoles(asList(Role.WRITE));
	}

	public AuthorizationAddRequest givingReadAccess() {
		return withRoles(asList(Role.READ));
	}

	public AuthorizationAddRequest givingReadWriteAccess() {
		return withRoles(asList(Role.READ, Role.WRITE));
	}

	public AuthorizationAddRequest givingReadWriteDeleteAccess() {
		return withRoles(asList(Role.READ, Role.WRITE, Role.DELETE));
	}

	public AuthorizationAddRequest giving(String... roles) {
		return withRoles(asList(roles));
	}

	public AuthorizationAddRequest giving(List<String> roles) {
		return withRoles(roles);
	}

	public AuthorizationAddRequest giving(Role... roles) {
		List<String> rolesCodes = new ArrayList<>();
		for (Role role : roles) {
			rolesCodes.add(role.getCode());
		}
		return withRoles(rolesCodes);
	}

	public AuthorizationAddRequest givingNegativeReadDeleteAccess() {
		return withRoles(asList(Role.READ, Role.DELETE)).setNegative(true);
	}

	public AuthorizationAddRequest givingNegativeDeleteAccess() {
		return withRoles(asList(Role.DELETE)).setNegative(true);
	}

	public AuthorizationAddRequest givingNegativeWriteAccess() {
		return withRoles(asList(Role.WRITE)).setNegative(true);
	}

	public AuthorizationAddRequest givingNegativeReadAccess() {
		return withRoles(asList(Role.READ)).setNegative(true);
	}

	public AuthorizationAddRequest givingNegativeReadWriteAccess() {
		return withRoles(asList(Role.READ, Role.WRITE)).setNegative(true);
	}

	public AuthorizationAddRequest givingUserWhoShared(User user) {
		return withRoles(asList(Role.READ, Role.WRITE)).setSharedBy(user.getId());
	}

	public AuthorizationAddRequest givingNegativeReadWriteDeleteAccess() {
		return withRoles(asList(Role.READ, Role.WRITE, Role.DELETE)).setNegative(true);
	}

	public AuthorizationAddRequest givingNegative(String... roles) {
		return withRoles(asList(roles)).setNegative(true);
	}

	public AuthorizationAddRequest givingNegative(List<String> roles) {
		return withRoles(roles).setNegative(true);
	}

	public AuthorizationAddRequest givingNegative(Role... roles) {
		List<String> rolesCodes = new ArrayList<>();
		for (Role role : roles) {
			rolesCodes.add(role.getCode());
		}
		return withRoles(rolesCodes).setNegative(true);
	}

	public boolean isNested() {
		return nested;
	}

	public AuthorizationAddRequest setNested(boolean nested) {
		this.nested = nested;
		return this;
	}

	public static AuthorizationAddRequest authorizationForGroups(Group... groups) {
		return new AuthorizationAddRequest(groups[0].getCollection()).forGroups(groups);
	}

	public static AuthorizationAddRequest authorizationForGroups(List<Group> groups) {
		return new AuthorizationAddRequest(groups.get(0).getCollection()).forGroups(groups.toArray(new Group[0]));
	}

	public static AuthorizationAddRequest authorizationForUsers(User... users) {
		return new AuthorizationAddRequest(users[0].getCollection()).forUsers(users);
	}

	public static AuthorizationAddRequest nonCascadingNestedAuthorizationForUsers(List<User> users) {
		return new AuthorizationAddRequest(users.get(0).getCollection()).forUsers(users.toArray(new User[0])).setNested(true);
	}

	public static AuthorizationAddRequest nonCascadingNestedAuthorizationForGroups(Group... groups) {
		return new AuthorizationAddRequest(groups[0].getCollection()).forGroups(groups).setNested(true);
	}

	public static AuthorizationAddRequest nonCascadingNestedAuthorizationForGroups(List<Group> groups) {
		return new AuthorizationAddRequest(groups.get(0).getCollection()).forGroups(groups.toArray(new Group[0])).setNested(true);
	}

	public static AuthorizationAddRequest nonCascadingNestedAuthorizationForUsers(User... users) {
		return new AuthorizationAddRequest(users[0].getCollection()).forUsers(users).setNested(true);
	}

	public static AuthorizationAddRequest authorizationForUsers(List<User> users) {
		return new AuthorizationAddRequest(users.get(0).getCollection()).forUsers(users.toArray(new User[0]));
	}

	public static AuthorizationAddRequest authorizationInCollection(String collection) {
		return new AuthorizationAddRequest(collection);
	}

	public static AuthorizationAddRequest authorizationInCollectionSharedBy(String collection, User user) {
		return new AuthorizationAddRequest(collection).setSharedBy(user.getId());
	}

	public static AuthorizationAddRequest authorizationInCollectionWithId(String collection, String id) {
		return new AuthorizationAddRequest(collection, id);
	}

	public String getId() {
		return id;
	}

	public String getCollection() {
		return collection;
	}

	public List<String> getPrincipals() {
		return principals;
	}

	public String getTarget() {
		return target;
	}

	public List<String> getRoles() {
		return roles;
	}

	public Authorization getExistingAuthorization() {
		return existingAuthorization;
	}

	public LocalDate getStart() {
		return start;
	}

	public LocalDate getEnd() {
		return end;
	}

	public User getExecutedBy() {
		return executedBy;
	}

	public String getSharedBy() {
		return sharedBy;
	}

	public AuthorizationAddRequest setSharedBy(String sharedBy) {
		this.sharedBy = sharedBy;
		return this;
	}

	public AuthorizationAddRequest setExecutedBy(User executedBy) {
		this.executedBy = executedBy;
		return this;
	}

	public AuthorizationAddRequest andOverridingInheritedAuths() {
		this.overridingInheritedAuths = true;
		return this;
	}

	public AuthorizationAddRequest andOverridingInheritedAuths(boolean value) {
		this.overridingInheritedAuths = value;
		return this;
	}

	public AuthorizationAddRequest andNegative(boolean value) {
		this.negative = value;
		return this;
	}

	public boolean isOverridingInheritedAuths() {
		return overridingInheritedAuths;
	}

	public boolean isNegative() {
		return negative;
	}
}

package com.constellio.model.entities.enums;

public enum GroupAuthorizationsInheritance {

	/**
	 * Security is inherited from parent to child (default mode) : A user in a group receive accesses given on the sub-groups, but not those given on the parents
	 */
	FROM_PARENT_TO_CHILD,


	/**
	 * Security is inherited from child to parent : a user in a group receive accesses given on the parents, but not those given on the sub-groups
	 */
	FROM_CHILD_TO_PARENT

}

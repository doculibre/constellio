/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.ui.entities;

import java.io.Serializable;
import java.util.Set;

import com.constellio.model.entities.security.global.GlobalGroupStatus;

@SuppressWarnings("serial")
public class GlobalGroupVO implements Serializable {

	String parent;

	String code;

	String name;

	Set<String> collections;

	GlobalGroupStatus status;

	public GlobalGroupVO() {
		this.status = GlobalGroupStatus.ACTIVE;
	}

	public GlobalGroupVO(String parent) {
		this.parent = parent;
		this.status = GlobalGroupStatus.ACTIVE;
	}

	public GlobalGroupVO(String code, String name, Set<String> collections, String parent, GlobalGroupStatus status) {
		this.code = code;
		this.name = name;
		this.collections = collections;
		this.parent = parent;
		this.status = status;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Set<String> getCollections() {
		return collections;
	}

	public void setCollections(Set<String> collections) {
		this.collections = collections;
	}

	public String getParent() {
		return parent;
	}

	public void setParent(String parent) {
		this.parent = parent;
	}

	public GlobalGroupStatus getStatus() {
		return status;
	}

	public void setStatus(GlobalGroupStatus status) {
		this.status = status;
	}
}



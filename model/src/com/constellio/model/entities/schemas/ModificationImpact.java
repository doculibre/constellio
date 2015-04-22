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
package com.constellio.model.entities.schemas;

import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

public class ModificationImpact {

	final List<Metadata> metadataToReindex;
	final LogicalSearchCondition logicalSearchCondition;

	public ModificationImpact(List<Metadata> metadataToReindex,
			LogicalSearchCondition logicalSearchCondition) {
		this.metadataToReindex = metadataToReindex;
		this.logicalSearchCondition = logicalSearchCondition;

		if (logicalSearchCondition == null) {
			throw new RuntimeException("logicalSearchCondition required");
		}
	}

	public List<Metadata> getMetadataToReindex() {
		return metadataToReindex;
	}

	public LogicalSearchCondition getLogicalSearchCondition() {
		return logicalSearchCondition;
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	@Override
	public String toString() {
		return "ModificationImpact{" +
				"metadataToReindex=" + metadataToReindex +
				", logicalSearchCondition=" + logicalSearchCondition +
				'}';
	}
}

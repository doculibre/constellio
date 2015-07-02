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
package com.constellio.app.modules.rm.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.model.enums.DisposalType;
import com.constellio.model.entities.schemas.ModifiableStructure;
import com.constellio.model.utils.EnumWithSmallCodeUtils;

public class CopyRetentionRule implements ModifiableStructure {

	String code;

	CopyType copyType;

	List<String> mediumTypeIds = new ArrayList<>();

	String contentTypesComment;

	RetentionPeriod activeRetentionPeriod = RetentionPeriod.ZERO;

	String activeRetentionComment;

	RetentionPeriod semiActiveRetentionPeriod = RetentionPeriod.ZERO;

	String semiActiveRetentionComment;

	DisposalType inactiveDisposalType;

	String inactiveDisposalComment;

	boolean dirty;

	public String getCode() {
		return code;
	}

	public CopyRetentionRule setCode(String code) {
		dirty = true;
		this.code = code;
		return this;
	}

	public CopyType getCopyType() {
		return copyType;
	}

	public CopyRetentionRule setCopyType(CopyType copyType) {
		dirty = true;
		this.copyType = copyType;
		return this;
	}

	public List<String> getMediumTypeIds() {
		return mediumTypeIds;
	}

	public CopyRetentionRule setMediumTypeIds(List<String> mediumTypeIds) {
		dirty = true;
		this.mediumTypeIds = mediumTypeIds;
		return this;
	}

	public String getContentTypesComment() {
		return contentTypesComment;
	}

	public CopyRetentionRule setContentTypesComment(String contentTypesComment) {
		dirty = true;
		this.contentTypesComment = contentTypesComment;
		return this;
	}

	public RetentionPeriod getActiveRetentionPeriod() {
		return activeRetentionPeriod;
	}

	public CopyRetentionRule setActiveRetentionPeriod(RetentionPeriod activeRetentionPeriod) {
		dirty = true;
		if (activeRetentionPeriod == null || activeRetentionPeriod.getValue() == 0) {
			this.activeRetentionPeriod = RetentionPeriod.ZERO;
		} else {
			this.activeRetentionPeriod = activeRetentionPeriod;
		}
		return this;
	}

	public String getActiveRetentionComment() {
		return activeRetentionComment;
	}

	public CopyRetentionRule setActiveRetentionComment(String activeRetentionComment) {
		dirty = true;
		this.activeRetentionComment = activeRetentionComment;
		return this;
	}

	public RetentionPeriod getSemiActiveRetentionPeriod() {
		return semiActiveRetentionPeriod;
	}

	public CopyRetentionRule setSemiActiveRetentionPeriod(RetentionPeriod semiActiveRetentionPeriod) {
		dirty = true;
		if (semiActiveRetentionPeriod == null || semiActiveRetentionPeriod.getValue() == 0) {
			this.semiActiveRetentionPeriod = RetentionPeriod.ZERO;
		} else {
			this.semiActiveRetentionPeriod = semiActiveRetentionPeriod;
		}
		return this;
	}

	public String getSemiActiveRetentionComment() {
		return semiActiveRetentionComment;
	}

	public CopyRetentionRule setSemiActiveRetentionComment(String semiActiveRetentionComment) {
		dirty = true;
		this.semiActiveRetentionComment = semiActiveRetentionComment;
		return this;
	}

	public DisposalType getInactiveDisposalType() {
		return inactiveDisposalType;
	}

	public CopyRetentionRule setInactiveDisposalType(DisposalType inactiveDisposalType) {
		dirty = true;
		this.inactiveDisposalType = inactiveDisposalType;
		return this;
	}

	public String getInactiveDisposalComment() {
		return inactiveDisposalComment;
	}

	public CopyRetentionRule setInactiveDisposalComment(String inactiveDisposalComment) {
		dirty = true;
		this.inactiveDisposalComment = inactiveDisposalComment;
		return this;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		//		sb.append(copyType == null ? "?" : copyType.getCode());
		//		sb.append(mediumTypeIds.toString());
		//		sb.append(" ");
		sb.append(activeRetentionPeriod == null ? "?" : activeRetentionPeriod.getValue());
		sb.append("-");
		sb.append(semiActiveRetentionPeriod == null ? "?" : semiActiveRetentionPeriod.getValue());
		sb.append("-");
		sb.append(inactiveDisposalType == null ? "?" : inactiveDisposalType.getCode());

		return sb.toString();
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this, "dirty");
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj, "dirty");
	}

	@Override
	public boolean isDirty() {
		return dirty;
	}

	public static CopyRetentionRule newPrincipal(List<String> contentTypesCodes, String value) {
		return newRetentionRule(CopyType.PRINCIPAL, contentTypesCodes, value);
	}

	public static CopyRetentionRule newSecondary(List<String> contentTypesCodes, String value) {
		return newRetentionRule(CopyType.SECONDARY, contentTypesCodes, value);
	}

	public static CopyRetentionRule newPrincipal(List<String> contentTypesCodes) {
		CopyRetentionRule copyRetentionRule = new CopyRetentionRule();
		copyRetentionRule.setMediumTypeIds(contentTypesCodes);
		copyRetentionRule.setCopyType(CopyType.PRINCIPAL);
		return copyRetentionRule;
	}

	public static CopyRetentionRule newSecondary(List<String> contentTypesCodes) {
		CopyRetentionRule copyRetentionRule = new CopyRetentionRule();
		copyRetentionRule.setMediumTypeIds(contentTypesCodes);
		copyRetentionRule.setCopyType(CopyType.SECONDARY);
		return copyRetentionRule;
	}

	public static CopyRetentionRule newRetentionRule(CopyType copyType, List<String> contentTypesCodes, String value) {
		String[] parts = (" " + value + " ").split("-");
		CopyRetentionRule copyRetentionRule = new CopyRetentionRule();
		copyRetentionRule.setMediumTypeIds(contentTypesCodes);
		copyRetentionRule.setCopyType(copyType);

		String part0 = parts[0].trim();
		String part1 = parts[1].trim();
		String part2 = parts[2].trim();

		if (!part0.isEmpty() && !part0.equals("0")) {
			copyRetentionRule.setActiveRetentionPeriod(new RetentionPeriod(Integer.valueOf(part0)));
		}
		if (!part1.isEmpty() && !part1.equals("0")) {
			copyRetentionRule.setSemiActiveRetentionPeriod(new RetentionPeriod(Integer.valueOf(part1)));
		}
		if (!part2.isEmpty()) {
			copyRetentionRule.setInactiveDisposalType((DisposalType) EnumWithSmallCodeUtils.toEnum(DisposalType.class, part2));
		}
		return copyRetentionRule;
	}

	public boolean canTransferToSemiActive() {
		return semiActiveRetentionPeriod != RetentionPeriod.ZERO;
	}

	public boolean canDeposit() {
		return inactiveDisposalType != null && inactiveDisposalType.isDepositOrSort();
	}

	public boolean canDestroy() {
		return inactiveDisposalType != null && inactiveDisposalType.isDestructionOrSort();
	}

	public boolean canSort() {
		return inactiveDisposalType != null && inactiveDisposalType == DisposalType.SORT;
	}

}

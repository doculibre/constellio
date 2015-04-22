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
package com.constellio.model.entities.records;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.constellio.model.entities.schemas.Metadata;

public class TransactionRecordsReindexation {

	private boolean reindexAllMetadatas;

	private List<Metadata> reindexMetadatas = new ArrayList<>();

	public TransactionRecordsReindexation() {
		this.reindexAllMetadatas = false;
	}

	public TransactionRecordsReindexation(List<Metadata> reindexMetadatas) {
		this.reindexMetadatas = reindexMetadatas;
	}

	public TransactionRecordsReindexation(TransactionRecordsReindexation copy) {
		this.reindexAllMetadatas = copy.reindexAllMetadatas;
		this.reindexMetadatas = copy.reindexMetadatas;
	}

	public boolean isReindexed(Metadata metadata) {
		return reindexAllMetadatas || reindexMetadatas.contains(metadata);
	}

	public void addReindexedMetadata(Metadata metadata) {
		if (!isReindexed(metadata)) {
			this.reindexMetadatas.add(metadata);
		}
	}

	public void addReindexedMetadatas(List<Metadata> metadatasToReindex) {
		for (Metadata metadataToReindex : metadatasToReindex) {
			addReindexedMetadata(metadataToReindex);
		}

	}

	public static TransactionRecordsReindexation ALL() {
		TransactionRecordsReindexation reindexation = new TransactionRecordsReindexation();
		reindexation.reindexAllMetadatas = true;
		return reindexation;
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}
}

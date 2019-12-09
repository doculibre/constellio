package com.constellio.data.dao.services.replicationFactor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"id"})
public class ReplicationFactorTransaction implements Comparable<ReplicationFactorTransaction> {
	private String id;
	private String recordId;
	private ReplicationFactorTransactionType type;
	private long version;
	private long timestamp;
	private Map<String, ReplicationFactorSolrInputField> fields;

	@Override
	public int compareTo(@NotNull ReplicationFactorTransaction transaction) {
		return Long.compare(this.version, transaction.version);
	}
}

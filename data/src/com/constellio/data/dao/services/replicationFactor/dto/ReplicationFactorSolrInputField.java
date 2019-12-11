package com.constellio.data.dao.services.replicationFactor.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ReplicationFactorSolrInputField {
	private String name;
	private Object value;
}

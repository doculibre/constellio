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
package com.constellio.app.client.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import com.constellio.app.client.AdminServicesConstants;
import com.constellio.app.client.entities.MetadataResource;

public class SchemaServicesClient {

	final WebTarget target;
	final String token;
	final String serviceKey;
	final String collection;

	SchemaServicesClient(WebTarget target, String token, String serviceKey, String collection) {
		this.target = target;
		this.token = token;
		this.serviceKey = serviceKey;
		this.collection = collection;
	}

	public List<String> getSchemaTypes() {
		Map<String, String> queryParams = new HashMap<>();
		return requestJson("getSchemaTypes", queryParams).get(List.class);
	}

	public List<String> getSchemas(String schemaType) {
		Map<String, String> queryParams = new HashMap<>();
		queryParams.put("schemaType", schemaType);
		return requestJson("getSchemas", queryParams).get(List.class);
	}

	public List<String> getSchemaMetadataCodes(String schemaCode) {
		Map<String, String> queryParams = new HashMap<>();
		queryParams.put("schemaCode", schemaCode);
		return requestJson("getSchemaMetadataCodes", queryParams).get(List.class);
	}

	public List<String> getSchemaValidators(String schemaCode) {
		Map<String, String> queryParams = new HashMap<>();
		queryParams.put("schemaCode", schemaCode);
		return requestJson("getSchemaValidators", queryParams).get(List.class);
	}

	public MetadataResource getMetadata(String code) {
		Map<String, String> queryParams = new HashMap<>();
		queryParams.put("code", code);
		return requestJson("getDataStoreFields", queryParams).get(MetadataResource.class);
	}

	public List<String> getTaxonomies() {
		Map<String, String> queryParams = new HashMap<>();
		return requestJson("getTaxonomies", queryParams).get(List.class);
	}

	public String getPrincipalTaxonomy() {
		Map<String, String> queryParams = new HashMap<>();
		return request("getPrincipalTaxonomy", queryParams).get(String.class);
	}

	public List<String> getTaxonomySchemaTypes(String taxonomyCode) {
		Map<String, String> queryParams = new HashMap<>();
		queryParams.put("taxonomyCode", taxonomyCode);
		return requestJson("getTaxonomySchemaTypes", queryParams).get(List.class);
	}

	public void createSchemaType(String schemaTypeCode) {
		Map<String, String> queryParams = new HashMap<>();
		queryParams.put("schemaTypeCode", schemaTypeCode);
		request("createSchemaType", queryParams).post(Entity.text(""), String.class);
	}

	public void createCustomSchema(String schemaCode) {
		Map<String, String> queryParams = new HashMap<>();
		queryParams.put("schemaCode", schemaCode);
		request("createCustomSchema", queryParams).post(Entity.text(""), String.class);
	}

	public void addSchemaValidator(String schemaCode, String schemaValidator) {
		Map<String, String> queryParams = new HashMap<>();
		queryParams.put("schemaCode", schemaCode);
		queryParams.put("schemaValidator", schemaValidator);
		request("addSchemaValidator", queryParams).post(Entity.text(""), String.class);
	}

	public void removeSchemaValidator(String schemaCode, String schemaValidator) {
		Map<String, String> queryParams = new HashMap<>();
		queryParams.put("schemaCode", schemaCode);
		queryParams.put("schemaValidator", schemaValidator);
		request("removeSchemaValidator", queryParams).post(Entity.text(""), String.class);
	}

	public void addUpdateMetadata(MetadataResource metadataResource) {
		Map<String, String> queryParams = new HashMap<>();
		request("addUpdateMetadata", queryParams).post(Entity.json(metadataResource), String.class);
	}

	public void disableMetadata(String metadataCode) {
		Map<String, String> queryParams = new HashMap<>();
		queryParams.put("metadataCode", metadataCode);
		request("disableMetadata", queryParams).post(Entity.text(""), String.class);
	}

	public void createTaxonomy(String code, List<String> schemaTypes) {
		Map<String, String> queryParams = new HashMap<>();
		queryParams.put("code", code);
		request("createTaxonomy", queryParams).post(Entity.json(schemaTypes), String.class);
	}

	public void setAsPrincipalTaxonomy(String code) {
		Map<String, String> queryParams = new HashMap<>();
		queryParams.put("code", code);
		request("setAsPrincipalTaxonomy", queryParams).post(Entity.text(""), String.class);
	}

	private Builder requestJson(String service, Map<String, String> queryParams) {
		return path(service, queryParams).request(MediaType.APPLICATION_JSON_TYPE)
				.header(AdminServicesConstants.AUTH_TOKEN, token).header(AdminServicesConstants.SERVICE_KEY, serviceKey);
	}

	private Builder request(String service, Map<String, String> queryParams) {
		return path(service, queryParams).request(MediaType.TEXT_PLAIN).header(AdminServicesConstants.AUTH_TOKEN, token)
				.header(AdminServicesConstants.SERVICE_KEY, serviceKey);
	}

	private WebTarget path(String service, Map<String, String> queryParams) {

		WebTarget target = this.target.queryParam("collection", collection);
		for (Map.Entry<String, String> queryParam : queryParams.entrySet()) {
			target = target.queryParam(queryParam.getKey(), queryParam.getValue());
		}
		return target.path(service);
	}

}

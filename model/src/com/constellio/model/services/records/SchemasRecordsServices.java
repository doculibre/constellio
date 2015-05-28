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
package com.constellio.model.services.records;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

import java.util.ArrayList;
import java.util.List;

import com.constellio.data.dao.services.idGenerator.UUIDV1Generator;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.security.roles.Roles;

public class SchemasRecordsServices {

	String collection;

	ModelLayerFactory modelLayerFactory;

	public SchemasRecordsServices(String collection, ModelLayerFactory modelLayerFactory) {
		this.collection = collection;
		this.modelLayerFactory = modelLayerFactory;
	}

	//

	//Generic
	
	protected ModelLayerFactory getModelLayerFactory() {
		return modelLayerFactory;
	}

	public MetadataSchemaTypes getTypes() {
		return modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection);
	}

	public MetadataSchema defaultSchema(String code) {
		return getTypes().getSchema(code + "_default");
	}

	public MetadataSchema schema(String code) {
		return getTypes().getSchema(code);
	}

	public Record get(String id) {
		return modelLayerFactory.newRecordServices().getDocumentById(id);
	}

	public List<Record> get(List<String> id) {
		return modelLayerFactory.newRecordServices().getRecordsById(collection, id);
	}

	public Record create(MetadataSchema schema) {
		return modelLayerFactory.newRecordServices().newRecordWithSchema(schema);
	}

	public Record create(MetadataSchema schema, String id) {
		return modelLayerFactory.newRecordServices().newRecordWithSchema(schema, id);
	}

	public Record getByCode(MetadataSchemaType schemaType, String code) {
		SearchServices searchServices = modelLayerFactory.newSearchServices();

		Metadata codeMetadata = schemaType.getDefaultSchema().getMetadata("code");
		LogicalSearchCondition condition = from(schemaType).where(codeMetadata).isEqualTo(code);

		return searchServices.searchSingleResult(condition);
	}

	//

	//Events

	public MetadataSchema eventSchema() {
		return getTypes().getSchema(Event.DEFAULT_SCHEMA);
	}

	public MetadataSchemaType eventSchemaType() {
		return getTypes().getSchemaType(Event.SCHEMA_TYPE);
	}

	public Metadata eventType() {
		return eventSchema().getMetadata(Event.TYPE);
	}

	public Metadata eventUsername() {
		return eventSchema().getMetadata(Event.USERNAME);
	}

	public Metadata eventCreation() {
		return eventSchema().getMetadata(Schemas.CREATED_ON.getLocalCode());
	}

	public Event wrapEvent(Record record) {
		return new Event(record, getTypes());
	}

	public List<Event> wrapEvents(List<Record> records) {
		List<Event> events = new ArrayList<>();
		for (Record record : records) {
			events.add(new Event(record, getTypes()));
		}
		return events;
	}

	public Event getEvent(String id) {
		return new Event(get(id), getTypes());
	}

	public Event newEvent() {
		String id = UUIDV1Generator.newRandomId();
		return new Event(new RecordImpl(Event.DEFAULT_SCHEMA, collection, id), getTypes());
	}

	//

	//Groups

	public MetadataSchema groupSchema() {
		return getTypes().getSchema(Group.DEFAULT_SCHEMA);
	}

	public MetadataSchemaType groupSchemaType() {
		return getTypes().getSchemaType(Group.SCHEMA_TYPE);
	}

	public Group wrapGroup(Record record) {
		return new Group(record, getTypes());
	}

	public List<Group> wrapGroups(List<Record> records) {
		List<Group> users = new ArrayList<>();
		for (Record record : records) {
			users.add(new Group(record, getTypes()));
		}
		return users;
	}

	public Group getGroup(String id) {
		return new Group(get(id), getTypes());
	}

	public Group newGroup() {
		return new Group(create(groupSchema()), getTypes());
	}

	public Group newGroupWithId(String id) {
		return new Group(create(groupSchema(), id), getTypes());
	}

	//

	//Users

	public MetadataSchema userSchema() {
		return getTypes().getSchema(User.DEFAULT_SCHEMA);
	}

	public MetadataSchemaType userSchemaType() {
		return getTypes().getSchemaType(User.SCHEMA_TYPE);
	}

	public User wrapUser(Record record) {
		return new User(record, getTypes(), getRoles());
	}

	public List<User> wrapUsers(List<Record> records) {
		List<User> users = new ArrayList<>();
		for (Record record : records) {
			users.add(new User(record, getTypes(), getRoles()));
		}
		return users;
	}

	public User getUser(String id) {
		return new User(get(id), getTypes(), getRoles());
	}

	public User newUser() {
		return new User(create(userSchema()), getTypes(), getRoles());
	}

	public User newUserWithId(String id) {
		return new User(create(userSchema(), id), getTypes(), getRoles());
	}

	private Roles getRoles() {
		return modelLayerFactory.getRolesManager().getCollectionRoles(collection);
	}
}

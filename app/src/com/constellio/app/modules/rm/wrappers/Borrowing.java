package com.constellio.app.modules.rm.wrappers;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import org.joda.time.LocalDateTime;

/**
 * Created by Charles Blanchette on 2017-03-10.
 */
public class Borrowing extends Event {
    public static final String SCHEMA_BORROWING = "borrowing";
    public static final String REQUEST_DATE = "requestDate";
    public static final String BORROWING_DATE = "borrowingDate";
    public static final String RETURN_DATE = "returnDate";
    public static final String RETURN_USERNAME = "returnUsername";
    public static final String RETURN_USER_ID = "returnUser";
    public static final String SCHEMA_NAME = Event.SCHEMA_TYPE + "_" + SCHEMA_BORROWING;

    public Borrowing(Record record, MetadataSchemaTypes types) {
        super(record, types);
    }

    public Borrowing setRequestDate(LocalDateTime requestDate) {
        set(REQUEST_DATE, requestDate);
        return this;
    }

    public String getRequestDate() {
        return get(REQUEST_DATE);
    }

    public Borrowing setBorrowingDate(LocalDateTime borrowingDate) {
        set(BORROWING_DATE, borrowingDate);
        return this;
    }

    public String getBorrowingDate() {
        return get(BORROWING_DATE);
    }

    public Borrowing setReturnDate(LocalDateTime returnDate) {
        set(RETURN_DATE, returnDate);
        return this;
    }

    public String getReturnDate() {
        return get(RETURN_DATE);
    }

    public Borrowing setReturnUsername(String returnUsername) {
        set(RETURN_USERNAME, returnUsername);
        return this;
    }

    public String getReturnUsername() {
        return get(RETURN_USERNAME);
    }

    public Borrowing setReturnUserId(String returnUserId){
        set(RETURN_USER_ID, returnUserId);
        return this;
    }

    public String getReturnUserId(){
        return get(RETURN_USER_ID);
    }
}

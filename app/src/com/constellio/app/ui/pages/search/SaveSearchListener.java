package com.constellio.app.ui.pages.search;

import com.constellio.model.entities.records.wrappers.SavedSearch;

public abstract class SaveSearchListener {
    protected abstract void save(Event event);

        public static class Event {
            private SavedSearch savedSearch;

            public Event(SavedSearch savedSearch) {
                this.savedSearch = savedSearch;
            }

            public SavedSearch getSavedSearch() {
                return savedSearch;
            }
        }
}

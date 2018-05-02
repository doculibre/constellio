package com.constellio.model.extensions.events.records;

public class BaseConsumableEventImpl implements ConsumableEvent {
    private boolean isConsumed = false;

    @Override
    final public boolean isConsumed() {
        return isConsumed;
    }

    @Override
    final public void consume() {
        isConsumed = true;
    }
}

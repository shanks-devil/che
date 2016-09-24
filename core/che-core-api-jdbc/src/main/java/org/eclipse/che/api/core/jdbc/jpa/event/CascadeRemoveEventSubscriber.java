package org.eclipse.che.api.core.jdbc.jpa.event;

import org.eclipse.che.api.core.notification.EventSubscriber;

/**
 * TODO
 *
 * @author Anton Korneta.
 */
public abstract class CascadeRemoveEventSubscriber<T extends CascadeRemovalEvent> implements EventSubscriber<T> {

    @Override
    public void onEvent(T event) {
        if (!event.isFailure()) {
            try {
                onCascadeEvent(event);
            } catch (Exception exception) {
                event.setCause(exception);
            }
        }
    }

    /**
     * TODO
     */
    public abstract void onCascadeEvent(T event) throws Exception;
}

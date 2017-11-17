package developer;

import java.io.Closeable;

/**
* Represents a property that can be observed or updated.
*/
public interface IDynamicProperty<T>
{
    /** 
     * Gets the value of the property.
     * Whenever the value is updated, all observers will be notified of the new value.
     * 
     * @return the value
     */
    T getValue();
    
    /** 
     * Sets the value of the property.
     * Whenever the value is updated, all observers will be notified of the new value.
     * 
     * @param the value
     */
    void setValue(T value);
    
    /**
     * Subscribes a callback to this dynamic property.
     * Anytime this dynamic property value is modified, that passed in callback should be called with the new value.
     * Returns an object that can be disposed to end the subscription and stop notifications sent to callback.
     * <p>Any number of subscriptions can be made to dynamic property.  All subscriptions should be notified when the value chagnes.</p>
     * @param callback Method to be called whenever the value is modified.
     * @return An object which can be disposed to cancel the subscription
     */
    Closeable subscribe(IObserver<T> callback);

}

package developer;

import java.io.Closeable;
import java.io.IOException;
import java.util.*;

/**
* Represents a property that can be observed or updated.
*/
public class ConcreteDynamicProperty<T> implements IDynamicProperty<T>
{
	private T m_Value;    
	private List<IObserver<T>> m_Subscriptions; 
	private List<Closeable> m_CloseSubscriptionsCalls; 

	public ConcreteDynamicProperty(T initialValue)
	{
		m_Value = initialValue;
		m_Subscriptions = Collections.synchronizedList(new ArrayList<IObserver<T>>());
		m_CloseSubscriptionsCalls = Collections.synchronizedList(new ArrayList<Closeable>());
	}

	public ConcreteDynamicProperty()
    {
		m_Subscriptions = Collections.synchronizedList(new ArrayList<IObserver<T>>());
		m_CloseSubscriptionsCalls = Collections.synchronizedList(new ArrayList<Closeable>());
	}

	/** 
	* Gets the value of the property.
 	* Whenever the value is updated, all observers will be notified of the new value.
 	* 
 	* @return the value
 	*/
	public T getValue()
	{
		//inform all interested parties that someone has accessed my value
		DependencyListener.getInstance().registerProperty(this, m_Value.getClass());
		return m_Value;
	}
    
	/** 
 	* Sets the value of the property.
 	* Whenever the value is updated, all observers will be notified of the new value.
	* 
	* @param the value
	*/
   	public void setValue(T value)
	{
		//set the value
   		m_Value = value;

		//inform all interested parties that a change has occurred
        observeAll();
	}
	
   	/** 
 	* Sets the value of the property.
 	* Whenever the value is updated, all observers will be notified of the new value.
	* 
	* setValue may be overriden while setValueTo preserves our ability to directly augment m_T
	* 
	* @param the value
	*/
   	final public void setValueTo(T value)
	{
		//set the value
   		m_Value = value;

		//inform all interested parties that a change has occurred
        observeAll();
	}
   	
    /**
    * Subscribes a callback to this dynamic property.
    * Anytime this dynamic property value is modified, that passed in callback should be called with the new value.
    * Returns an object that can be disposed to end the subscription and stop notifications sent to callback.
    * <p>Any number of subscriptions can be made to dynamic property.  All subscriptions should be notified when the value chagnes.</p>
    * @param callback Method to be called whenever the value is modified.
    * @return An object which can be disposed to cancel the subscription
    */
    public Closeable subscribe(IObserver<T> callback)
	{
		synchronized(m_Subscriptions){
			if(!m_Subscriptions.contains(callback))
				m_Subscriptions.add(callback);
		}	

		return new Closeable(){
			@Override
			public void close() throws IOException
			{
				synchronized(m_Subscriptions){
					if(m_Subscriptions.contains(callback))
						m_Subscriptions.remove(callback);
				}
			}
		};
	}

	/**
	 * inform all interested parties of a change in state
	*/
	protected void observeAll()
	{
    	synchronized(m_Subscriptions){
    		for(IObserver<T> obs : m_Subscriptions) {
    			obs.observe(m_Value);
    		}
        }
	}
	
	/**
	 * Add a callback to close any subscriber's subscription to me
	 * @param closeMe
	 */
	public void addCloseableCall(Closeable closeMe)
	{
		synchronized(m_CloseSubscriptionsCalls){
			if(!m_CloseSubscriptionsCalls.contains(closeMe))
				m_CloseSubscriptionsCalls.add(closeMe);
		}
	}
	
	/**
	 * Close all subscriptions to me
	 * Since closure is final, remove the list of Closeables when complete
	 */
	protected void closeAllSubscriptions()
	{
    	synchronized(m_CloseSubscriptionsCalls){
    		for(Closeable closeMe : m_CloseSubscriptionsCalls) {
    			try{
    				closeMe.close();
    			}
    			catch(IOException e)
    			{
    				System.err.println(e.getMessage());
    			}
    		}
    		m_CloseSubscriptionsCalls.clear(); //remove closeCalls once they've been called
    	}
	}
}


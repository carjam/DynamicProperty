package developer;

import java.util.concurrent.Callable;
import java.io.Closeable;

/**
 * Static factory methods to create <see cref="IDynamicProperty{T}"/> instances.
 *
 */
public class DynamicProperty
{
	/**
	 * Creates an {@link IDynamicProperty} instance with the passed in
	 * initialValue
	 *
	 * @param initialValue
	 *            The initial value of the property
	 * @return
	 */
	public static <T> IDynamicProperty<T> create(T initialValue) {
		return new ConcreteDynamicProperty<T> (initialValue);
	}

	/**
	 * @param read
	 *            Called to calculate the value of the property. This method
	 *            will be called exactly 1 time during construction to determine
	 *            the initial value of this calculated dynamic property. If,
	 *            during execution, this method accesses any other
	 *            {@link IDynamicProperty} instances, then this calculated
	 *            dynamic property will subscribe to those other instances. When
	 *            any of those instances are changed, this read function will be
	 *            called again to determine the new value of this calculated
	 *            dynamic property. Under no other circumstances will this
	 *            method be called. <i>Specifically this method should not
	 *            automatically be called</i>
	 * @param write
	 *            Called whenever the {@link IDynamicProperty} property setter
	 *            of this is invoked. This write action can do anything it wants
	 *            with the written value.
	 * @return
	 */
	public static <T> IDynamicProperty<T> create(Callable<T> read, IObserver<T> write) 	
	{
		ConcreteDynamicProperty<T> prop = new ConcreteDynamicProperty<T>() {
			@Override
            public void setValue(T value)
            {
				//execute observe once to do whatever was requested at the base level
                write.observe(value);
                
				//propagate the change if appropriate via subscriptions
                observeAll();
            }
		};

		// initialize our new DP, prop using read.call via it's own observe(T val) call
		// this will set prop's value and create its own subscriptions
		// it will also mean future calls to remove and re-establish subscriptions to accommodate any chained dependencies
		IObserver<Integer> obs = createObserver(read, prop);
		obs.observe(0);
		return prop;
	}
	
	
	/*
	* Due to type erasure we can't use reflection to glean type at runtime.
	* We've chosen to store DPs in a Object collection in order to decouple the Factory and product (DP).
	* Consequently, we have to explicitly cast them when we pull them out of the collection.
	* Unfortunately, this means that we have to implement a conditional statement clause for each type we intend to use.
	*
	* I've chosen to place this method here, with DP instantiation so that like concepts are at least 
	* conceptually grouped to ease future extension.
	*/	
	public static <T,U> IObserver<U> createObserver(Callable<T> read, ConcreteDynamicProperty<T> prop)
	{
		return new IObserver<U>() {
			@Override
            public void observe(U value)
            {
				DependencyListener.getInstance().startListening();
				T callVal;
                try{
                	callVal = read.call();
                } catch (Exception e){
                	callVal = null;
                }
                DependencyListener.getInstance().stopListening();
				
                //read.call may have changed our subscriptions so end them
                prop.closeAllSubscriptions();
                
                //and register the new ones
                for(int ii=0;ii < DependencyListener.getInstance().getSize();ii++)
        		{
        			DescribedProperty resourceObj = DependencyListener.getInstance().getProperty(ii);

        			if(resourceObj.getDescribedClass() == String.class)
        			{
        				IDynamicProperty<String> resource = (IDynamicProperty<String>) resourceObj.getProperty();
        				IObserver<String> obs = createObserver(read, prop);
        				Closeable closeSubscription = resource.subscribe(obs);
        				prop.addCloseableCall(closeSubscription);
        			}
        			else if(resourceObj.getDescribedClass() == Integer.class)
        			{
        				IDynamicProperty<Integer> resource = (IDynamicProperty<Integer>) resourceObj.getProperty();
        				IObserver<Integer> obs = createObserver(read, prop);
        				Closeable closeSubscription = resource.subscribe(obs);
        				prop.addCloseableCall(closeSubscription);
        			}
        			//else if ... Float.class
        			//handle further types here...
        		}
                
                //set new instance's value to read.call
                prop.setValueTo(callVal);
            }
		};
	}
}

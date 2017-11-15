package developer;

import java.util.*;

/**
 * A singleton available to both the Factory that produces IDependencyProperty instances
 * and the products themselves
 * 
 * A listener is necessary to determine an IDP's dependencies at runtime without being privy to their internal behavior
 * We allow dependent IDPs to register themselves here for interested parties to ascertain a IDP's dependencies
 * 
 * Due to type erasure we can't use reflection to glean type at runtime
 * we've chosen to store DPs in a Object collection in order to decouple the Factory and product (DP)
 * 
 * @author jamescarson
 *
 */
		
public class DependencyListener
{
	private List<DescribedProperty> m_Registry;
	private boolean m_bListening;
	
	/**
	 * "Initialization-on-demand" singleton implementation
	 * intended use as efficient & thread-safe
	 */
	private DependencyListener()
	{
		m_bListening = false;
		m_Registry = Collections.synchronizedList(new LinkedList<DescribedProperty>());
	};

	private static class SingletonHolder {
		private static final DependencyListener INSTANCE = new DependencyListener();
	}
 
	public static DependencyListener getInstance() {
        return SingletonHolder.INSTANCE;
    }
	
	/**
	 * IDPs may register the fact that they were called via this method
	 * 
	 * @param property - an instance of an IDependencyProperty
	 * 					implemented as an Object to decouple the generic ConcreteDynamicProprty class from interested parties
	 * 
	 * @param cls - the generic type the Object implements
	 */
	public void registerProperty(Object property, Class<? extends Object> cls)
    {
		if(m_bListening)
		{
			DescribedProperty describedProperty = new DescribedProperty(property, cls);
			
			synchronized(m_Registry)
	        {
				if(!m_Registry.contains(property))
					m_Registry.add(describedProperty);
	        }     
		}
	}
	
	public DescribedProperty getProperty(int i)
	{
		return m_Registry.get(i);
	}
	
	public int getSize()
	{
		return m_Registry.size();
	}

	/**
	 * A party interested in ascertaining an IDP's dependencies should call this first
	 * It cleans out potentially unrelated IDPs
	 * and informs the listener to pay attention to registration events
	 */
	public void startListening()
	
	{
		m_Registry.clear();
		m_bListening = true;
	}
	
	/**
	 * This tells the listener to ignore subsequent registration events
	 * 
	 * Though not critical, for performance and efficient memory use this should be called after startListening
	 * at the conclusion of the event registration window
	 */
	public void stopListening()
	{
		m_bListening = false;
	}
}

package developer;

/**
 * A pair to hold IDependencyProperty and it's Type
 * 
 * Due to type erasure we can't use reflection to glean type at runtime
 * we've chosen to store DPs in a Object collection in order to decouple the Factory and product (DP)
 * 
 * @author jamescarson
 *
 */
public class DescribedProperty
{
	private Class<? extends Object> m_PropertyClass;
	private Object m_Property;
		
	DescribedProperty(Object property, Class<? extends Object> propertyClass)
	{
		m_PropertyClass = propertyClass;
		m_Property = property;
	}
	
	public Class<? extends Object> getDescribedClass()
	{
		return m_PropertyClass;
	}

	public Object getProperty()
	{
		return m_Property;
	}	
}
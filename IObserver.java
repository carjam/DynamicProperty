package developer;

/**
 * Observes values
**/
public interface IObserver<T>
{
	void observe(T value);
}

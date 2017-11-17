package developer;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import org.junit.Assert;
import org.junit.Test;


/// Tests the minimum required features for this coding quiz
/// Tests that your dynamic properties function correctly in a single-threaded application.
public class TestSet1Basic_CalculatedProperties {

		@Test
	    public void readFunctionIsEvaluatedExactlyOnceDuringConstruction() {
	    	// store evalCount as an array so we can change its value from our "lambda"
			final Integer[] evalCount = new Integer[] { 0 };

	        @SuppressWarnings("unused")
			IDynamicProperty<Integer> p = DynamicProperty.create(
        		new Callable<Integer>() { @Override public Integer call() throws Exception { return evalCount[0] = evalCount[0] + 1; } },
        		new IObserver<Integer>() { @Override public void observe(Integer value) { Assert.fail("write method should not be called"); } });
	        Assert.assertTrue(evalCount[0] == 1);
	    }

		@Test
	    public void initialValueIsResultOfReadFunction() {
	    	final Integer[] evalCount = new Integer[] { 0 };
	        IDynamicProperty<Integer> p = DynamicProperty.create(
        		new Callable<Integer>() { @Override public Integer call() throws Exception { return evalCount[0] = evalCount[0] + 1; } },
        		new IObserver<Integer>() { @Override public void observe(Integer value) { Assert.fail("write method should not be called"); } });
	        Assert.assertTrue(p.getValue() == 1);
	    }

		@Test
	    public void readingValueShouldNotTriggerAnotherEvaulation() {
	    	final Integer[] evalCount = new Integer[] { 0 };
	        IDynamicProperty<Integer> p = DynamicProperty.create(
        		new Callable<Integer>() { @Override public Integer call() throws Exception { return evalCount[0] = evalCount[0] + 1; } },
        		new IObserver<Integer>() { @Override public void observe(Integer value) { Assert.fail("write method should not be called"); } });
	        Assert.assertTrue(p.getValue() == 1);
	        Assert.assertTrue(evalCount[0] == 1);
	    }

		@Test
	    public void whenWriteMethodDoesNothingReadIsNotTriggered() {
	    	final Integer[] evalCount = new Integer[] { 0 };
	        IDynamicProperty<Integer> p = DynamicProperty.create(
        		new Callable<Integer>() { @Override public Integer call() throws Exception { return evalCount[0] = evalCount[0] + 1; } },
        		new IObserver<Integer>() { @Override public void observe(Integer value) { /* noop */ } });
	        p.setValue(10);
	        Assert.assertTrue(p.getValue() == 1);
	        Assert.assertTrue(evalCount[0] == 1);
	    }

		@Test
	    public void whenWriteMethodDoesNotModifyDynamicPropertyReadIsNotTriggered() {
	    	final Integer[] v = new Integer[] { 0 };
	        IDynamicProperty<Integer> p = DynamicProperty.create(
        		new Callable<Integer>() { @Override public Integer call() throws Exception { return v[0] = v[0] + 1; } },
        		new IObserver<Integer>() { @Override public void observe(Integer value) { v[0] = value; } });
	        p.setValue(10);
	        Assert.assertTrue(v[0] == 10); // write method updated our value variable
	        Assert.assertTrue(p.getValue() == 1); // but read method is not re-evaluated so it is still 1.
	    }

		@Test
	    public void whenWriteMethodUpdatesDependencyReadIsTriggered() {
			final IDynamicProperty<Integer> v = DynamicProperty.create(42);
	        IDynamicProperty<Integer> p = DynamicProperty.create(
        		new Callable<Integer>() { @Override public Integer call() throws Exception { return v.getValue(); } },
        		new IObserver<Integer>() { @Override public void observe(Integer value) { v.setValue(value); } });

	        Assert.assertTrue(p.getValue() == 42);

	        p.setValue(100);
	        Assert.assertTrue(v.getValue() == 100);
	        Assert.assertTrue(p.getValue() == 100);
	    }

		@Test
	    public void whenWriteMethodUpdatesNonDependentValueReadIsNotTriggered() {
			final IDynamicProperty<Integer> v = DynamicProperty.create(42);
			final IDynamicProperty<Integer> v2 = DynamicProperty.create(99);
	        IDynamicProperty<Integer> p = DynamicProperty.create(
        		new Callable<Integer>() { @Override public Integer call() throws Exception { return v.getValue(); } },
        		new IObserver<Integer>() { @Override public void observe(Integer value) { v2.setValue(value); } });

	        Assert.assertTrue(p.getValue() == 42);
	        p.setValue(100);
	        Assert.assertTrue(v2.getValue() == 100);
	        Assert.assertTrue(v.getValue() == 42);
	        Assert.assertTrue(p.getValue() == 42);
	    }

		@Test
	    public void whenDependencyIsModifiedCalculatedPropertyIsReevaluated() {
			final IDynamicProperty<Integer> v = DynamicProperty.create(42);
	        IDynamicProperty<Integer> p = DynamicProperty.create(
        		new Callable<Integer>() { @Override public Integer call() throws Exception { return 10 * v.getValue(); } },
        		new IObserver<Integer>() { @Override public void observe(Integer value) { v.setValue(value / 10); } });

	        Assert.assertTrue(p.getValue() == 420);
	        v.setValue(55);
	        Assert.assertTrue(p.getValue() == 550);
	    }

		@Test
	    public void whenMultipleDependenciesExistPropertyIsReevaluatedWhenAnyChange() {
			final IDynamicProperty<String> a = DynamicProperty.create("forty-two");
			final IDynamicProperty<Integer> b = DynamicProperty.create(42);
			final IDynamicProperty<Integer> c = DynamicProperty.create(99);
			IDynamicProperty<Integer> p = DynamicProperty.create(
        		new Callable<Integer>() { @Override public Integer call() throws Exception { return a.getValue().length() + b.getValue() + c.getValue(); } },
        		new IObserver<Integer>() { @Override public void observe(Integer value) { /* noop */ } });

	        Assert.assertTrue(p.getValue() == (42 + 99 + "forty-two".length()));

	        b.setValue(100);
	        Assert.assertTrue(p.getValue() == (100 + 99 + "forty-two".length()));

	        a.setValue("");
	        Assert.assertTrue(p.getValue() == (100 + 99));
	    }

		@Test
	    public void canBeDependentOnOtherCalculatedProperties() {
			final IDynamicProperty<Integer> a = DynamicProperty.create(42);
			final IDynamicProperty<Integer> b = DynamicProperty.create(
        		new Callable<Integer>() { @Override public Integer call() throws Exception { return a.getValue() * 10; } },
        		new IObserver<Integer>() { @Override public void observe(Integer value) { a.setValue(value / 10); } });
			IDynamicProperty<String> c = DynamicProperty.create(
        		new Callable<String>() { @Override public String call() throws Exception { return b.getValue().toString(); } },
        		new IObserver<String>() { @Override public void observe(String value) { b.setValue(Integer.valueOf(value)); } });

	        Assert.assertTrue(c.getValue().equals("420"));

	        a.setValue(31);
	        Assert.assertTrue(c.getValue().equals("310"));

	        c.setValue("990");
	        Assert.assertTrue(b.getValue() == 990);
	        Assert.assertTrue(a.getValue() == 99);
	    }

		@Test
		/**
		 * I had to change the following in the capturesNewDependenciesOnSubsequentReads test method to avoid class cast exception (java.lang.ClassCastException: [Ljava.lang.Object; cannot be cast to [Ldeveloper.IDynamicProperty;).
		 *	final IDynamicProperty<Integer>[] v = (IDynamicProperty<Integer>[])new Object[3];
		 *	I changed it to:
		 *	final IDynamicProperty<Integer>[] v = new IDynamicProperty[3];
		 */
	    public void capturesNewDependenciesOnSubsequentReads() {
	    	@SuppressWarnings("unchecked")
	    	final IDynamicProperty<Integer>[] v = (IDynamicProperty<Integer>[])new IDynamicProperty[3];
	    	v[0] = DynamicProperty.create(42);
	    	v[1] = DynamicProperty.create(99);
	    	v[2] = DynamicProperty.create(2012);
	        final IDynamicProperty<Integer> which = DynamicProperty.create(0);
			IDynamicProperty<Integer> p = DynamicProperty.create(
        		new Callable<Integer>() { @Override public Integer call() throws Exception { return v[which.getValue()].getValue(); } },
        		new IObserver<Integer>() { @Override public void observe(Integer value) { v[which.getValue()].setValue(value); } });

	        Assert.assertTrue(p.getValue() == 42);

	        p.setValue(24);
	        Assert.assertTrue(p.getValue() == 24);
	        Assert.assertTrue(v[0].getValue() == 24);

	        which.setValue(2);
	        Assert.assertTrue(p.getValue() == 2012);
	        Assert.assertTrue(v[0].getValue() == 24);

	        v[2].setValue(3012);
	        Assert.assertTrue(p.getValue() == 3012);
	        Assert.assertTrue(v[2].getValue() == 3012);

	        v[1].setValue(1999);
	        Assert.assertTrue(p.getValue() == 3012);
	        Assert.assertTrue(v[2].getValue() == 3012);

	        which.setValue(1);
	        Assert.assertTrue(p.getValue() == 1999);
	    }
}

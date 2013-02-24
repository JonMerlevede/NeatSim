package neatsim.core;

public class Counter {
	private int counter = 0;
	public int getCount() {
		return counter;
	}
	public void increaseCount() {
		counter++;
	}
	@Override
	public String toString() {
		return "" + counter;
	}
}
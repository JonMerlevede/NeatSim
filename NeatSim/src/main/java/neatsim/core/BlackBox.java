package neatsim.core;


public interface BlackBox {
	void setInput(int no, double val);
	double getOutput(int no);
	void activate();
	void reset();
	boolean isValid();
}

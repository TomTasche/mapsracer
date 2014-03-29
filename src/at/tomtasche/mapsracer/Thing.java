package at.tomtasche.mapsracer;

/**
 * this is by far not the best object for representing a point in a graph, but
 * it's approximately what i had to deal with when creating this. using this
 * class there can be multiple instances of the class representing just a single
 * T (one instance represents the T and the connection to T-A, another instance
 * might represent the same T but the connection to T-B, etc).
 * 
 * if you can, you should use a class which is able to represent a T and *all*
 * its connections to other Ts. this would save you from the buildGraph()-method
 * too.
 * 
 */
public interface Thing {
	public String getName();

	public Thing getConnectedTo();
}

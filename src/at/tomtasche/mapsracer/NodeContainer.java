package at.tomtasche.mapsracer;

public class NodeContainer implements Thing {

	private String name;
	private NodeContainer connectedTo;

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Thing getConnectedTo() {
		return connectedTo;
	}
}

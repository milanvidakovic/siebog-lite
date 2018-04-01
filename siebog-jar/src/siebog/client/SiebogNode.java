package siebog.client;

public class SiebogNode {
	public static final SiebogNode LOCAL = null;
	public String host;
	public int port;
	public SiebogNode(String host, int port) {
		this.host = host;
		this.port = port;
	}
}

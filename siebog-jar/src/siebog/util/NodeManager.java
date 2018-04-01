package siebog.util;

/**
 * Used for JBoss node address lookup.
 * @author Minja
 *
 */
public class NodeManager {
	/**
	 * Returns the JBoss node name.
	 * @return
	 */
	public static String getNodeName() {
		return System.getProperty("jboss.node.name");
	}
}

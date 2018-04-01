package siebog.util;

import org.infinispan.Cache;
import org.infinispan.manager.CacheContainer;

import siebog.agentmanager.AID;
import siebog.agentmanager.Agent;

/**
 * Infinispan cache which holds agents and their AIDs as keys. Pair (AID, agent_instance) is placed inside this cache.
 * 
 * @author Dejan 
 * @author Minja
 *
 */
public class GlobalCache {
	private static final String CACHE_CONTAINER = "java:jboss/infinispan/container/siebog-cache";
	private static GlobalCache instance;
	private CacheContainer cacheContainer;
	private static final String RUNNING_AGENTS = "running-agents";

	public static GlobalCache get() {
		if (instance == null) {
			synchronized (GlobalCache.class) {
				if (instance == null)
					instance = new GlobalCache();
			}
		}
		return instance;
	}

	private GlobalCache() {
		cacheContainer = ObjectFactory.lookup(CACHE_CONTAINER, CacheContainer.class, null);
	}

	public Cache<AID, Agent> getRunningAgents() {
		return cacheContainer.getCache(RUNNING_AGENTS);
	}

	public Cache<?, ?> getCache(String name) {
		return cacheContainer.getCache(name);
	}
}

/*	
	private static Map<AID, Agent> instance;

	public static Map<AID, Agent> get() {
		if (instance == null) {
			synchronized (Map.class) {
				if (instance == null)
					instance = new HashMap<AID, Agent>();
			}
		}
		return instance;
	}

	private GlobalCache() {
	}

	public Map<AID, Agent> getRunningAgents() {
		return get();
	}
*/

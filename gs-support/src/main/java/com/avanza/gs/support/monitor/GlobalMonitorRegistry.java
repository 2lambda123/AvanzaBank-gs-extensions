package com.avanza.gs.support.monitor;
/**
 * Holds a jvm global MonitorRegistry, mainly intended to be used
 * by infrastructure classes where it is inconvenient to use dependency injection.
 * 
 * @author Elias Lindholm (elilin)
 *
 */
public class GlobalMonitorRegistry {
	
	private static final MonitorRegistry instance = new MonitorRegistryImpl("global-registry-monitors");

	public static MonitorRegistry get() {
		return instance ;
	}

}

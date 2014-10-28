package com.avanza.gs.support.monitor;

import org.openspaces.pu.service.ServiceMonitorsProvider;


/**
 * ServiceMontitorProvider that scans registered beans for @Monitor annotated methods.
 * 
 * All methods annotated with @Monitor will be exported. 
 * 
 * 
 * 
 * @author Elias Lindholm (elilin)
 *
 */
public interface MonitorRegistry extends ServiceMonitorsProvider {
	
	/**
	 * Scans the bean and registers all service monitors (@Monitor) found in this MonitorRegistry. <p>
	 * 
	 * @param bean
	 * @param beanName
	 */
	void registerMonitors(Object bean, String beanName);
	
}

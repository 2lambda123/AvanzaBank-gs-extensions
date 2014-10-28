package com.avanza.gs.support.monitor;

import java.util.Objects;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * BeanPostProcessor that enables @Monitor scanning and registration of all beans in the
 * ApplicationContext. 
 * 
 * @author Elias Lindholm (elilin)
 *
 */
public class MonitorRegistrationBeanPostProcessor implements BeanPostProcessor {

	private final MonitorRegistry monitorRegistry;	
	
	public MonitorRegistrationBeanPostProcessor(MonitorRegistry annotationMonitorRegistry) {
		this.monitorRegistry = Objects.requireNonNull(annotationMonitorRegistry);
	}
	
	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName)
			throws BeansException {
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		this.monitorRegistry.registerMonitors(bean, beanName);
		return bean;
	}

}

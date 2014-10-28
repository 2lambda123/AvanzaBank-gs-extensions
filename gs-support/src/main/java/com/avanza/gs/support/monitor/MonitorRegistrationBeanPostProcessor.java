/**
 * Copyright (C) 2014 Avanza (-)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

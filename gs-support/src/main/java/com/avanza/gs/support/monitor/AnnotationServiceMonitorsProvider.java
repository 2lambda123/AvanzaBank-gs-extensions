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

import org.openspaces.core.space.mode.BeforeSpaceModeChangeEvent;
import org.openspaces.core.space.mode.SpaceBeforeBackupListener;
import org.openspaces.core.space.mode.SpaceBeforePrimaryListener;
import org.openspaces.pu.service.ServiceMonitors;
import org.openspaces.pu.service.ServiceMonitorsProvider;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import com.gigaspaces.cluster.activeelection.SpaceMode;
/**
 * 
 * NB: make sure that no dynamic proxies are applied to your beans containing {@link Monitor} or {@link Monitors}
 * annotations. For example, avoid having aspects on them.
 * 
 * @author Elias Lindholm (elilin)
 * 
 */
public class AnnotationServiceMonitorsProvider implements BeanPostProcessor, ServiceMonitorsProvider, SpaceBeforePrimaryListener, SpaceBeforeBackupListener {
	
	private final MonitorRegistry monitorRegistry;
	private volatile SpaceMode currentMode;
	
	public AnnotationServiceMonitorsProvider() {
		this(new MonitorRegistryImpl());
	}
	
	public AnnotationServiceMonitorsProvider(String id) {
		this(new MonitorRegistryImpl(id));
	}
	
	public AnnotationServiceMonitorsProvider(MonitorRegistry monitorRegistry) {
		this.monitorRegistry = monitorRegistry;
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
	
	@Override
	public ServiceMonitors[] getServicesMonitors() {
		if (isBackup()) {
			return new ServiceMonitors[0];
		}
		return this.monitorRegistry.getServicesMonitors();
	}

	private boolean isBackup() {
		return this.currentMode == SpaceMode.BACKUP;
	}

	// test hook
	void setSpaceMode(SpaceMode newMode) {
		this.currentMode = newMode;
	}

	@Override
	public void onBeforeBackup(BeforeSpaceModeChangeEvent event) {
		setSpaceMode(SpaceMode.BACKUP);
	}

	@Override
	public void onBeforePrimary(BeforeSpaceModeChangeEvent event) {
		setSpaceMode(SpaceMode.PRIMARY);
	}
}

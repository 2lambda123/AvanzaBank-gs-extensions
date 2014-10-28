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

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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import org.openspaces.pu.service.PlainServiceMonitors;
import org.openspaces.pu.service.ServiceMonitors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Elias Lindholm (elilin)
 * 
 */
public class MonitorRegistryImpl implements MonitorRegistry {

	public static final String DEFAULT_DEFAULT_SERVICE_MONITORS_ID = "custom-monitors"; // Sic, its the default default value!

	private final List<ActiveMonitor> monitorMap = new CopyOnWriteArrayList<>();
	private final List<ActiveMonitors> monitorsMap = new CopyOnWriteArrayList<>();
	private final Logger logger = LoggerFactory.getLogger(AnnotationServiceMonitorsProvider.class);
	private final String defaultId;

	public MonitorRegistryImpl() {
		this(DEFAULT_DEFAULT_SERVICE_MONITORS_ID);
	}

	public MonitorRegistryImpl(String defaultId) {
		this.defaultId = Objects.requireNonNull(defaultId);
	}

	@Override
	public void registerMonitors(Object bean, String beanName) {
		logger.debug("Scanning {} for Monitor's", bean.getClass().getName());
		for (Method method : bean.getClass().getMethods()) {
			if (validMonitorMethod(bean, method)) {
				addMonitor(method, beanName, bean);
			}
			if (validMonitorsMethod(bean, method)) {
				addMonitors(method, beanName, bean);
			}
		}
	}

	private boolean validMonitorsMethod(Object bean, Method method) {
		if (!method.isAnnotationPresent(Monitors.class)) {
			return false;
		}
		if (method.getParameterTypes().length > 0) {
			logger.error("Invalid method signature for Monitors found on bean " + bean.getClass().getName()
					+ ". Method signature must not take any paramaters, was: " + method.toString());
			return false;
		}
		if (!method.getReturnType().equals(Map.class)) {
			logger.error("Invalid method signature for Monitors found on bean " + bean.getClass().getName()
					+ ". Method must return a value of type Map, was: " + method.toString());
			return false;
		}
		return true;
	}

	private boolean validMonitorMethod(Object bean, Method method) {
		if (!method.isAnnotationPresent(Monitor.class)) {
			return false;
		}
		if (method.getParameterTypes().length > 0) {
			logger.error("Invalid method signature for Monitor found on bean " + bean.getClass().getName()
					+ ". Method signature must not take any paramaters, was: " + method.toString());
			return false;
		}
		if (method.getReturnType().equals(Void.TYPE)) {
			logger.error("Invalid method signature for Monitor found on bean " + bean.getClass().getName()
					+ ". Method must return a value, was: " + method.toString());
			return false;
		}
		return true;
	}

	private void addMonitors(Method method, String beanName, Object bean) {
		Monitors monitorsDefinition = method.getAnnotation(Monitors.class);
		String namePrefix = getMonitorNamePrefix(method, beanName, monitorsDefinition);
		String monitorsId = getMonitorsId(monitorsDefinition.id());
		logger.debug("Adding monitors prefixed with '{}' found on method '{}'", namePrefix, method.toString());
		monitorsMap.add(new ActiveMonitors(method, bean, namePrefix, monitorsId));
	}

	private String getMonitorNamePrefix(Method method, String beanName, Monitors monitorsDefinition) {
		String monitorNamePrefix = monitorsDefinition.value();
		if (monitorNamePrefix == null || monitorNamePrefix.trim().isEmpty()) {
			monitorNamePrefix = method.getName();
			logger.debug("Found monitors without name prefix on method '{}'. Montitor names are prefixed with: {}",
					method.toString(), monitorNamePrefix);
		}
		if (monitorsDefinition.prependBeanName()) {
			monitorNamePrefix = beanName + "." + monitorNamePrefix;
		}
		return monitorNamePrefix;
	}

	private void addMonitor(Method method, String beanName, Object bean) {
		Monitor monitorDefinition = method.getAnnotation(Monitor.class);
		String monitorName = getMonitorName(method, beanName, monitorDefinition);
		String monitorsId = getMonitorsId(monitorDefinition.id());
		logger.debug("Adding monitor named '{}' found on method '{}'", monitorName, method.toString());
		monitorMap.add(new ActiveMonitor(method, bean, monitorName, monitorsId));
	}

	private String getMonitorName(Method method, String beanName, Monitor monitorDefinition) {
		String monitorName = monitorDefinition.value();
		if (monitorName == null || monitorName.trim().isEmpty()) {
			monitorName = method.getName();
			logger.debug("Found unamed monitor on method '{}'. Montitor is named to: {}", method.toString(),
					monitorName);
		}
		if (monitorDefinition.prependBeanName()) {
			monitorName = beanName + "." + monitorName;
		}
		return monitorName;
	}

	private String getMonitorsId(String monitorsId) {
		if (monitorsId == null || monitorsId.trim().isEmpty()) {
			monitorsId = this.defaultId; // TODO, use bean classname as default monitorsId
		}
		return monitorsId;
	}

	@Override
	public ServiceMonitors[] getServicesMonitors() {
		Map<String, PlainServiceMonitors> allMonitors = new HashMap<>();
		for (ActiveMonitor activeMonitor : this.monitorMap) {
			try {
				String name = activeMonitor.getName();
				Object value = activeMonitor.getValue();
				String monitorsId = activeMonitor.getMonitorsId();
				PlainServiceMonitors serviceMonitors = getOrCreateServiceMonitor(allMonitors, monitorsId);
				serviceMonitors.getMonitors().put(name, value);
			} catch (Exception e) {
				logger.error("Could not read monitor method: " + activeMonitor.method.getName() + " found on "
						+ activeMonitor.bean.getClass().getName(), e);
			}
		}
		for (ActiveMonitors activeMonitor : this.monitorsMap) {
			try {
				String monitorsId = activeMonitor.getMonitorsId();
				PlainServiceMonitors serviceMonitors = getOrCreateServiceMonitor(allMonitors, monitorsId);
				Map<Object, Object> monitors = activeMonitor.getValue();
				if (monitors == null) {
					logger.info("Malformed @Monitors method. It returned null. Method=" + activeMonitor.method);
					continue;
				}
				Map<String, Object> namePrefixedMonitors = prependNamePrefix(monitors, activeMonitor.getNamePrefix());
				serviceMonitors.getMonitors().putAll(namePrefixedMonitors);
			} catch (Exception e) {
				logger.error("Could not read monitor method: " + activeMonitor.method.getName() + " found on "
						+ activeMonitor.bean.getClass().getName(), e);
			}
		}
		return allMonitors.values().toArray(new ServiceMonitors[0]);
	}

	private Map<String, Object> prependNamePrefix(Map<Object, Object> monitors, String namePrefix) {
		Map<String, Object> result = new HashMap<>();
		for (Entry<Object, Object> monitor : monitors.entrySet()) {
			String monitorName = namePrefix + "." + monitor.getKey();
			result.put(monitorName, monitor.getValue());
		}
		return result;
	}

	private PlainServiceMonitors getOrCreateServiceMonitor(Map<String, PlainServiceMonitors> allMonitors,
			String monitorsId) {
		PlainServiceMonitors serviceMonitors = allMonitors.get(monitorsId);
		if (serviceMonitors == null) {
			serviceMonitors = new PlainServiceMonitors(monitorsId);
			allMonitors.put(monitorsId, serviceMonitors);
		}
		return serviceMonitors;
	}

	private static class ActiveMonitors {
		private final Method method;
		private final Object bean;
		private final String namePrefix;
		private final String monitorsId;

		public ActiveMonitors(Method method, Object bean, String namePrefix, String monitorsId) {
			this.method = method;
			this.bean = bean;
			this.namePrefix = namePrefix;
			this.monitorsId = monitorsId;
		}

		@SuppressWarnings("unchecked")
		public Map<Object, Object> getValue() throws IllegalAccessException, InvocationTargetException {
			return (Map<Object, Object>) method.invoke(bean);
		}

		public String getNamePrefix() {
			return namePrefix;
		}

		public String getMonitorsId() {
			return monitorsId;
		}
	}

	private static class ActiveMonitor {

		private final Method method;
		private final Object bean;
		private final String name;
		private final String monitorsId;

		public ActiveMonitor(Method method, Object bean, String monitorName, String monitorsId) {
			this.method = Objects.requireNonNull(method);
			this.bean = Objects.requireNonNull(bean);
			this.name = Objects.requireNonNull(monitorName);
			this.monitorsId = Objects.requireNonNull(monitorsId);
		}

		private Object getValue() throws IllegalAccessException, InvocationTargetException {
			return method.invoke(bean);
		}

		public String getName() {
			return name;
		}

		public String getMonitorsId() {
			return monitorsId;
		}
	}
}

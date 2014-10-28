package com.avanza.gs.support.monitor;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Test;

public class MonitorRegistrationBeanPostProcessorTest {

	private static final String BEAN_NAME = "bean-name";
	private Object bean = new Object();
	
	@Test
	public void registersMonitorsOnPostProcessAfterInitialization() throws Exception {
		MonitorRegistry mockMonitorRegistry = mock(MonitorRegistry.class);
		MonitorRegistrationBeanPostProcessor annotationServiceMonitorsProvider = new MonitorRegistrationBeanPostProcessor(mockMonitorRegistry);
		
		annotationServiceMonitorsProvider.postProcessAfterInitialization(bean, BEAN_NAME);
		
		verify(mockMonitorRegistry).registerMonitors(bean, BEAN_NAME);
	}

}

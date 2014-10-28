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

/*
 * #%L
 * gs
 * -
 * Copyright (C) 2014 Avanza Bank
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE
 * -
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import static junit.framework.Assert.assertSame;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.stub;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.mockito.Mockito;
import org.openspaces.pu.service.ServiceMonitors;

import com.gigaspaces.cluster.activeelection.SpaceMode;
/**
 * 
 * @author Elias Lindholm (elilin)
 *
 */
public class AnnotationServiceMonitorsProviderTest {

	private static final String BEAN_NAME = "bean-name";
	private final Object bean = new Object();
	
	@Test
	public void registersAllBeans() throws Exception {
		MonitorRegistry mockMonitorRegistry = Mockito.mock(MonitorRegistry.class);
		AnnotationServiceMonitorsProvider annotationServiceMonitorsProvider = new AnnotationServiceMonitorsProvider(mockMonitorRegistry);
		
		annotationServiceMonitorsProvider.postProcessAfterInitialization(bean, BEAN_NAME);
		
		verify(mockMonitorRegistry).registerMonitors(bean, BEAN_NAME);
	}
	
	@Test
	public void delegatesGetServicesMonitorsToProvidedMonitorRegistry_WhenSpaceModeIsPrimary() throws Exception {
		MonitorRegistry stubMonitorRegistry = Mockito.mock(MonitorRegistry.class);
		AnnotationServiceMonitorsProvider annotationServiceMonitorsProvider = new AnnotationServiceMonitorsProvider(stubMonitorRegistry);
		annotationServiceMonitorsProvider.setSpaceMode(SpaceMode.PRIMARY);
		ServiceMonitors[] serviceMonitors = new ServiceMonitors[0];
		stub(stubMonitorRegistry.getServicesMonitors()).toReturn(serviceMonitors);
		
		ServiceMonitors[] result = annotationServiceMonitorsProvider.getServicesMonitors();
		
		assertSame(serviceMonitors, result);
	}
	
	@Test
	public void delegatesGetServicesMonitorsToProvidedMonitorRegistry_WhenSpaceModeIsNone() throws Exception {
		MonitorRegistry stubMonitorRegistry = Mockito.mock(MonitorRegistry.class);
		AnnotationServiceMonitorsProvider annotationServiceMonitorsProvider = new AnnotationServiceMonitorsProvider(stubMonitorRegistry);
		annotationServiceMonitorsProvider.setSpaceMode(SpaceMode.NONE);
		ServiceMonitors[] serviceMonitors = new ServiceMonitors[0];
		stub(stubMonitorRegistry.getServicesMonitors()).toReturn(serviceMonitors);
		
		ServiceMonitors[] result = annotationServiceMonitorsProvider.getServicesMonitors();
		
		assertSame(serviceMonitors, result);
	}
	
	@Test
	public void returnsEmptyServiceMonitorsArray_WhenSpaceModeIsBackup() throws Exception {
		MonitorRegistry stubMonitorRegistry = Mockito.mock(MonitorRegistry.class);
		ServiceMonitors monitorsStub = Mockito.mock(ServiceMonitors.class);
		ServiceMonitors[] nonEmptyMonitorArray = new ServiceMonitors[]{monitorsStub};
		Mockito.stub(stubMonitorRegistry.getServicesMonitors()).toReturn(nonEmptyMonitorArray);
		
		AnnotationServiceMonitorsProvider annotationServiceMonitorsProvider = new AnnotationServiceMonitorsProvider(stubMonitorRegistry);
		annotationServiceMonitorsProvider.setSpaceMode(SpaceMode.BACKUP);
		assertEquals(0, annotationServiceMonitorsProvider.getServicesMonitors().length);
	}
}

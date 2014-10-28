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

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

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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
/**
 * Annotation for exporting a method as part of a ServiceMonitor. The object containing the annotated method
 * must be a spring bean in order to be detected.
 * 
 * An {@link AnnotationServiceMonitorsProvider}
 * must be registered in the applicationContext, otherwise this annotation will have no effect.
 * 
 * 
 * Usage:
 * 
 * <pre>
 * // Must be a spring bean in order for @Monitor methods to be detected.
 * class AuthenticationService {
 * 
 *    private final AtomicLong authenticationCount = new AtomicLong(); 
 *    private final AtomicLong successfulAuthenticationCount = new AtomicLong(); 
 *
 *    public void authenticate(Username un, Password pw) {
 *       authenticationCount.incrementAndGet(); 			
 *       ...
 *       successfulAuthenticationCount.incrementAndGet(); 			
 *    }
 * 
 *    // Exports an monitor with name 'authentication-count' under default serviceId ('custom-monitors')
 *    @Monitor("authentication-count")
 *    public long getTotalAuthenticationCount() {
 *       return authenticationCount.get(); 
 *    }
 *    
 *    // Exports an monitor with name 'successful-authentication-count' under serviceId ('authentication-monitors')
 *    @Monitor(value = "successful-authentication-count", id = "authentication-monitors")
 *    public long getTotalAuthenticationCount() {
 *       return successfulAuthenticationCount.get(); 
 *    }
 *    
 * }
 * </pre>
 * 
 * 
 * @author Elias Lindholm (elilin)
 * 
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Monitor {
	/**
	 * The name of the monitored property. If empty (default), then the monitored property will
	 * have the same name as the exporting method.
	 * 
	 * @return
	 */
	String value() default "";
	
	/**
	 * The id of the ServiceMonitors. If empty (default) then the default ServiceMonitors id will
	 * be used.
	 * 
	 * @return
	 */
	String id() default "";

	/**
	 * Whether the spring bean name should be prepended to the monitor name. Useful
	 * when having multiple instances of the same class, or an hierarchy of objects
	 * where multiple instances want to expose the same monitor. <p>
	 * 
	 * @return
	 */
	boolean prependBeanName() default false;
}

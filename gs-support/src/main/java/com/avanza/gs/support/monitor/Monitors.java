package com.avanza.gs.support.monitor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;

/**
 * Annotation for exporting a method as part of a ServiceMonitor. The return value of the method must be a {@link Map}.
 * The entries in the map will be exported as separate monitors but with a common name prefix. The object containing the
 * annotated method must be a spring bean in order to be detected.
 * 
 * An {@link AnnotationServiceMonitorsProvider} must be registered in the applicationContext, otherwise this annotation
 * will have no effect.
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Monitors {

	/**
	 * The name prefix of the monitors. If empty (default), then the monitor names will be prefixed with the name of the
	 * exporting method.
	 * 
	 * @return
	 */
	String value() default "";

	/**
	 * The id of the ServiceMonitors. If empty (default), then the default ServiceMonitors id will be used.
	 * 
	 * @return
	 */
	String id() default "";

	/**
	 * Whether the spring bean name should be prepended to the name prefix of the monitors. Useful when having multiple
	 * instances of the same class, or an hierarchy of objects where multiple instances want to expose the same
	 * monitors.
	 * 
	 * @return
	 */
	boolean prependBeanName() default false;
}

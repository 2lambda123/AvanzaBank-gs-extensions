package com.avanza.gs.support.monitor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;
import org.openspaces.pu.service.ServiceMonitors;

public class MonitorRegistryImplTest {

	private static final String MONITORS_ID = "monitorsId";
	private static final String BEAN_NAME = "beanName";
	private final MonitorRegistryImpl provider = new MonitorRegistryImpl(MONITORS_ID);

	@Test
	public void monitorNameDefaultsToMethodName() {
		Object monitor = new Object() {
			@Monitor
			public int monitorMethodName() {
				return 5;
			}
		};
		provider.registerMonitors(monitor, BEAN_NAME);
		ServiceMonitors sm = getServiceMonitors(provider, MONITORS_ID);
		assertThat(sm, containsMonitor("monitorMethodName", 5));
	}

	@Test
	public void exposesNamedMonitor() {
		Object monitor = new Object() {
			@Monitor("value")
			public int getValue() {
				return 5;
			}
		};
		provider.registerMonitors(monitor, BEAN_NAME);
		ServiceMonitors sm = getServiceMonitors(provider, MONITORS_ID);
		assertThat(sm, containsMonitor("value", 5));
	}

	@Test
	public void prependsBeanNameToMonitorName() {
		Object monitor = new Object() {
			@Monitor(value = "monitor", prependBeanName = true)
			public int monitor() {
				return 5;
			}
		};
		provider.registerMonitors(monitor, BEAN_NAME);
		ServiceMonitors sm = getServiceMonitors(provider, MONITORS_ID);
		assertThat(sm, containsMonitor(BEAN_NAME + ".monitor", 5));
	}

	@Test
	public void exposesMonitorUnderProvidedMontitorsId() {
		Object monitor = new Object() {
			@Monitor(id = "a_monitor_id")
			public int monitor() {
				return 5;
			}
		};
		provider.registerMonitors(monitor, BEAN_NAME);
		ServiceMonitors sm = getServiceMonitors(provider, "a_monitor_id");
		assertThat(sm, containsMonitor("monitor", 5));
	}

	@Test
	public void namePrefixForMonitorsDefaultsToMethodName() {
		Object monitor = new Object() {
			@Monitors
			public Map<String, Integer> monitors() {
				return Collections.singletonMap("key", 1);
			}
		};
		provider.registerMonitors(monitor, BEAN_NAME);
		ServiceMonitors sm = getServiceMonitors(provider, MONITORS_ID);
		assertThat(sm, containsMonitor("monitors.key", 1));
	}

	@Test
	public void exposesNamedMonitors() {
		Object monitor = new Object() {
			@Monitors("namePrefix")
			public Map<String, Integer> getValues() {
				return Collections.singletonMap("key", 1);
			}
		};
		provider.registerMonitors(monitor, BEAN_NAME);
		ServiceMonitors sm = getServiceMonitors(provider, MONITORS_ID);
		assertThat(sm, containsMonitor("namePrefix.key", 1));
	}

	@Test
	public void stringRepresentationsOfMonitorKeysAreUsedAsMonitorNames() {
		Object monitor = new Object() {
			@Monitors("namePrefix")
			public Map<BigDecimal, Integer> getValues() {
				return Collections.singletonMap(BigDecimal.valueOf(1), 2);
			}
		};
		provider.registerMonitors(monitor, BEAN_NAME);
		ServiceMonitors sm = getServiceMonitors(provider, MONITORS_ID);
		assertThat(sm, containsMonitor("namePrefix.1", 2));
	}

	@Test
	public void prependsBeanNameToNamePrefixForMonitors() {
		Object monitor = new Object() {
			@Monitors(value = "namePrefix", prependBeanName = true)
			public Map<String, Integer> getValues() {
				return Collections.singletonMap("foo", 1);
			}
		};
		provider.registerMonitors(monitor, BEAN_NAME);
		ServiceMonitors sm = getServiceMonitors(provider, MONITORS_ID);
		assertThat(sm, containsMonitor(BEAN_NAME + ".namePrefix.foo", 1));
	}

	@Test
	public void exposesMonitorsUnderProvidedMontitorsId() {
		Object monitor = new Object() {
			@Monitors(id = "a_monitor_id")
			public Map<String, Integer> monitor() {
				return Collections.singletonMap("foo", 1);
			}
		};
		provider.registerMonitors(monitor, BEAN_NAME);
		ServiceMonitors sm = getServiceMonitors(provider, "a_monitor_id");
		assertThat(sm, containsMonitor("monitor.foo", 1));
	}

	@Test
	public void monitorDoesNotHideOtherMonitorWithSameId() {
		final String monitorId = "X";
		Object monitor1 = new Object() {
			@Monitor(id = monitorId, value = "monitor1")
			public int getValue() {
				return 1;
			}
		};
		Object monitor2 = new Object() {
			@Monitor(id = monitorId, value = "monitor2")
			public int getValue() {
				return 2;
			}
		};
		Object monitor3 = new Object() {
			@SuppressWarnings("serial")
			@Monitors(id = monitorId, value = "monitor3Prefix")
			public Map<String, Integer> getValue() {
				return new HashMap<String, Integer>() {
					{
						put("monitor3", 3);
					}
				};
			}
		};
		provider.registerMonitors(monitor1, BEAN_NAME);
		provider.registerMonitors(monitor2, BEAN_NAME);
		provider.registerMonitors(monitor3, BEAN_NAME);
		ServiceMonitors sm = getServiceMonitors(provider, monitorId);
		assertThat(sm, containsMonitor("monitor1", 1));
		assertThat(sm, containsMonitor("monitor2", 2));
		assertThat(sm, containsMonitor("monitor3Prefix.monitor3", 3));
	}

	@Test
	public void ignoresMonitorWithVoidReturnType() {
		Object monitor = new Object() {
			@Monitor
			public void monitor() {
			}
		};
		provider.registerMonitors(monitor, BEAN_NAME);
		assertEquals(0, provider.getServicesMonitors().length);
	}

	@Test
	public void ignoresMonitorWithArguments() throws Exception {
		Object monitor = new Object() {
			@Monitor
			public int monitor(int foo) {
				return 5;
			}
		};
		provider.registerMonitors(monitor, BEAN_NAME);
		assertEquals(0, provider.getServicesMonitors().length);
	}

	@Test
	public void doesNotPropagateExceptionWhenMonitorMethodThrowsException() {
		Object monitor = new Object() {
			@Monitor
			public int monitor() {
				throw new IllegalArgumentException();
			}
		};
		provider.registerMonitors(monitor, BEAN_NAME);
		assertEquals(0, provider.getServicesMonitors().length);
	}

	private Matcher<ServiceMonitors> containsMonitor(final String name, final Object value) {
		return new TypeSafeMatcher<ServiceMonitors>() {
			@Override
			public void describeTo(Description description) {
				description.appendText("ServiceMonitors with monitor: " + name + "[" + value + "]");
			}

			@Override
			protected boolean matchesSafely(ServiceMonitors item) {
				return value.equals(item.getMonitors().get(name));
			}
		};
	}

	private ServiceMonitors getServiceMonitors(
			MonitorRegistry monitorRegistry, String monitorId) {
		ServiceMonitors[] serviceMonitors = monitorRegistry.getServicesMonitors();
		for (ServiceMonitors serviceMontor : serviceMonitors) {
			if (monitorId.equals(serviceMontor.getId())) {
				return serviceMontor;
			}
		}
		throw new AssertionError("Expected a service monitor with id: " + monitorId);
	}

}

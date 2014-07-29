package org.score.samples.openstack.actions;

import com.hp.score.lang.SystemContext;
import org.apache.log4j.Logger;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * Date: 7/22/2014
 *
 * @author Bonczidai Levente
 */
public class OOActionRunner {
	private final static Logger logger = Logger.getLogger(OOActionRunner.class);
	private static int eventCount = 0; // to be removed

	/**
	 * Wrapper method for running actions. A method is a valid action if it returns a Map<String, String>.
	 *
	 * @param executionContext current Execution Context
	 * @param className        full path of the actual action class
	 * @param methodName       method name of the actual action
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws InvocationTargetException
	 */
	public void run(Map<String, Serializable> executionContext,
					String className,
					String methodName
	)
			throws ClassNotFoundException, IllegalAccessException, InstantiationException, InvocationTargetException {
		logger.info("run method invocation");

		//get the action class
		Class actionClass = Class.forName(className);

		//get the Method object
		Method actionMethod = getMethodByName(actionClass, methodName);

		//get the parameter names of the action method
		ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();
		String[] parameterNames = parameterNameDiscoverer.getParameterNames(actionMethod);

		//extract the parameters from execution context
		Object[] actualParameters = getParametersFromExecutionContext(executionContext, parameterNames);

		// invoke method
		Map<String, String> results = invokeActionMethod(actionMethod, actionClass.newInstance(), actualParameters);

		//merge back the results of the action in the flow execution context
		mergeBackResults(executionContext, results);
	}

	//todo test when method will be finished, change SystemContext to ExecutionRuntimeServices
	public void runWithServices(Map<String, Serializable> executionContext,
								SystemContext systemContext,
								String className,
								String methodName
	)
			throws ClassNotFoundException, IllegalAccessException, InstantiationException, InvocationTargetException {
		logger.info("runWithServices method invocation");

		//get the action class
		Class actionClass = Class.forName(className);

		//get the Method object
		Method actionMethod = getMethodByName(actionClass, methodName);

		//get the parameter names of the action method
		ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();
		String[] parameterNames = parameterNameDiscoverer.getParameterNames(actionMethod);

		//extract the parameters from execution context
		Object[] actualParameters = getParametersFromExecutionContext(executionContext, parameterNames);

		// invoke method
		Map<String, String> results = invokeActionMethod(actionMethod, actionClass.newInstance(), actualParameters);

		//merge back the results of the action in the flow execution context
		mergeBackResults(executionContext, results);

		//events
		if ((eventCount++) % 2 == 0) {
			systemContext.addEvent("type1", "event type1 data");
		} else {
			systemContext.addEvent("type2", "event type2 data");
		}
	}

	/**
	 * Extracts the actual method of the action's class
	 *
	 * @param actionClass Class object that represents the actual action class
	 * @param methodName  method name of the actual action
	 * @return actual method represented by Method object
	 * @throws ClassNotFoundException
	 */
	private Method getMethodByName(Class actionClass, String methodName) throws ClassNotFoundException {
		Method[] methods = actionClass.getDeclaredMethods();
		Method actionMethod = null;
		for (Method m : methods) {
			if (m.getName().equals(methodName)) {
				actionMethod = m;
			}
		}
		return actionMethod;
	}

	/**
	 * Retrieves a list of parameters from the execution context
	 *
	 * @param executionContext current Execution Context
	 * @param parameterNames   list of parameter names to be retrieved
	 * @return parameters from the execution context represented as Object list
	 */
	private Object[] getParametersFromExecutionContext(Map<String, Serializable> executionContext, String[] parameterNames) {
		int nrParameters = parameterNames.length;
		Object[] actualParameters = null;
		if (nrParameters > 0) {
			actualParameters = new Object[nrParameters];
			//actual parameter is passed from the execution context
			//if not present will be null
			for (int i = 0; i < nrParameters; i++) {
				if (executionContext.containsKey(parameterNames[i])) {
					actualParameters[i] = executionContext.get(parameterNames[i]);
				} else {
					actualParameters[i] = null;
				}
			}
		}
		return actualParameters;
	}

	/**
	 * Invokes the actual action method with the specified parameters
	 *
	 * @param actionMethod action method represented as Method object
	 * @param instance     an instance of the invoker class
	 * @param parameters   method parameters
	 * @return results if the action
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 */
	@SuppressWarnings("unchecked")
	private Map<String, String> invokeActionMethod(Method actionMethod, Object instance, Object... parameters)
			throws InvocationTargetException, IllegalAccessException {
		return (Map<String, String>) actionMethod.invoke(instance, parameters);
	}

	/**
	 * Merges back the results in the execution context
	 *
	 * @param executionContext current Execution Context
	 * @param results          results to be merged back
	 */
	private void mergeBackResults(Map<String, Serializable> executionContext, Map<String, String> results) {
		if (results != null) {
			executionContext.putAll(results);
		}
	}

	@SuppressWarnings("unused") //todo test when method will be finished
	public Long navigate(Map<String, Serializable> executionContext, String nextStep) {
		logger.info("navigate method invocation");

		if (nextStep != null && !nextStep.isEmpty()) {
			if (nextStep.equals("null")) {
				return null;
			} else {
				try {
					Long nextStepId;
					nextStepId = Long.parseLong(nextStep);
					return nextStepId;
				} catch (NumberFormatException ex) {
					return null;
				}
			}
		} else {
			return null;
		}
	}
}
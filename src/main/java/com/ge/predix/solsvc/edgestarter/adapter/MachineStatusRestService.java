/*
* Copyright (c) 2014 General Electric Company. All rights reserved.
*
* The copyright to the computer software herein is the property of
* General Electric Company. The software may be used and/or copied only
* with the written permission of General Electric Company or in accordance
* with the terms and conditions stipulated in the agreement/contract
* under which the software has been supplied.
*/

package com.ge.predix.solsvc.edgestarter.adapter;

import java.io.IOException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ge.predix.solsvc.edgestarter.rest.IHttpRestServer;
import com.ge.predix.solsvc.edgestarter.rest.IMachineStatusService;

/**
 *
 * @author Predix Machine Sample
 */
@Path(IHttpRestServer.PATH)
@Component(name = MachineStatusRestService.SERVICE_PID, service = {
		IHttpRestServer.class
}
)
public class MachineStatusRestService implements IHttpRestServer
{
	

	/** Service PID for Sample Machine Adapter */
	public static final String SERVICE_PID = "com.ge.predix.solsvc.edgestarter.processor"; //$NON-NLS-1$
	/**
	 * The regular expression used to split property values into String array.
	 */
	public final static String SPLIT_PATTERN = "\\s*\\|\\s*"; //$NON-NLS-1$

	public final static String MACHINE_HOME = System.getProperty("predix.home.dir"); //$NON-NLS-1$
	// Create logger to report errors, warning massages, and info messages
	// (runtime Statistics)
	private static final Logger _logger = LoggerFactory.getLogger(MachineStatusRestService.class);

	private IMachineStatusService machineStatus;
	
	
	/*
	 * ############################################### # OSGi service lifecycle
	 * management # ###############################################
	 */

	/**
	 * OSGi component lifecycle activation method
	 *
	 * @param ctx
	 *            component context
	 * @throws IOException
	 *             on fail to load/set configuration properties
	 */
	@Activate
	public void activate(ComponentContext ctx) throws IOException {
		_logger.info("Starting sample " + ctx.getBundleContext().getBundle().getSymbolicName()); //$NON-NLS-1$	
	}

	/**
	 * OSGi component lifecycle deactivation method
	 *
	 * @param ctx
	 *            component context
	 */
	@Deactivate
	public void deactivate(ComponentContext ctx) {
		// Put your clean up code here when container is shutting down
		if (_logger.isDebugEnabled()) {
			_logger.debug("Stopped sample for " + ctx.getBundleContext().getBundle().getSymbolicName()); //$NON-NLS-1$
		}
	}

	/**
	 * @return -
	 */
	@GET
	@Path("/status")
	@Produces(MediaType.APPLICATION_JSON)
	public MachineStatusResponse listExistingConfig() {
		return machineStatus.getMachineStatusResponse();
	}

	public IMachineStatusService getMachineStatus() {
		return machineStatus;
	}

	@Reference
	public void setMachineStatus(IMachineStatusService machineStatus) {
		this.machineStatus = machineStatus;
	}

	
	
}

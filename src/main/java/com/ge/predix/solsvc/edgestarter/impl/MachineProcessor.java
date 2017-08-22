/*
 * Copyright (c) 2016 General Electric Company. All rights reserved.
 *
 * The copyright to the computer software herein is the property of
 * General Electric Company. The software may be used and/or copied only
 * with the written permission of General Electric Company or in accordance
 * with the terms and conditions stipulated in the agreement/contract
 * under which the software has been supplied.
 */

package com.ge.predix.solsvc.edgestarter.impl;

import java.time.Duration;
import java.time.Instant;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ge.dspmicro.hoover.api.processor.IProcessor;
import com.ge.dspmicro.hoover.api.processor.ProcessorException;
import com.ge.dspmicro.hoover.api.spillway.ITransferData;
import com.ge.dspmicro.machinegateway.types.ITransferable;
import com.ge.dspmicro.river.api.IPingConstants;
import com.ge.dspmicro.validateroute.api.IPingMessage;
import com.ge.dspmicro.validateroute.api.IPongNotification;
import com.ge.dspmicro.validateroute.api.IValidateRoute;
import com.ge.dspmicro.validateroute.api.PongMessage;
import com.ge.dspmicro.validateroute.api.PongMessage.PongStatus;
import com.ge.dspmicro.websocketriver.send.api.IWebsocketSend;
import com.ge.predix.solsvc.edgestarter.api.ISampleProcessor;
import com.ge.predixmachine.datamodel.datacomm.EdgeDataList;

/**
 * This class provides a Processor implementation which will process the data as
 * per configuration on the spillway.
 */
@Component(immediate = true, 
	name = MachineProcessor.SERVICE_PID, 
	service = { 
		IProcessor.class, 
		ISampleProcessor.class,
	}
)
public class MachineProcessor implements IProcessor, ISampleProcessor {

	/**
	 * Create logger to report errors, warning massages, and info messages
	 * (runtime Statistics)
	 */
	private static Logger _logger = LoggerFactory.getLogger(MachineProcessor.class);

	/** Service PID for Sample Machine Adapter */
	public static final String SERVICE_PID = "com.ge.predix.solsvc.edgestarter.processor"; //$NON-NLS-1$

	/*
	 * Hold the last time data was sent to Timeseries
	 */
	private Calendar lastDataSent;

	private IWebsocketSend websocketSend;	
	
	/**
	 * @param ctx
	 *            context of the bundle.
	 */
	@Activate
	public void activate(ComponentContext ctx) {
		_logger.info("Starting MachineProcessor " + SERVICE_PID); //$NON-NLS-1$	
	}

	/**
	 * @param ctx
	 *            context of the bundle.
	 */
	@Deactivate
	public void deactivate(ComponentContext ctx) {
		_logger.info("Spillway service deactivated."); //$NON-NLS-1$
		
	}

	@Override
	public void processValues(String processType, List<ITransferable> values, ITransferData transferData)
			throws ProcessorException {
		lastDataSent = Calendar.getInstance();
		_logger.info("processType : " + processType + " || VALUES :" + values.toString()); //$NON-NLS-1$
		processValues(processType, new HashMap<String, String>(), values, transferData);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void processValues(String processType, Map<String, String> map, List<ITransferable> values,
			ITransferData transferData) throws ProcessorException {
		lastDataSent = Calendar.getInstance();
		_logger.info("processType : " + processType + " || VALUES :" + values.toString()); //$NON-NLS-1$
		transferData.transferData(values);
	}

	@Override
	public void processValues(String processType, Map<String, String> map, EdgeDataList values,
			ITransferData transferData) throws ProcessorException {
		lastDataSent = Calendar.getInstance();
	}

	@Override
	public Calendar getLastDataSent() {
		return lastDataSent;
	}

	@Override
	public boolean isConnectedToTimeseries() {
		// get validate route
		IValidateRoute route = this.websocketSend.getValidateRoute();

		// River Name to sent to
		String riverName = this.websocketSend.getRiverName();
		_logger.info("River : "+riverName);
		// Create Key Map
		Map<String, Object> params = new HashMap<>();

		// Add River to use in map
		params.put(IPingConstants.RIVER_NAME_KEY, riverName);

		// create pong notification
		TestPongNotification pongNotification = new TestPongNotification();

		//Create the ping message
		IPingMessage ping = route.createPingMessage(params, pongNotification);
		route.ping(ping);
		//Read the status of the ping message
		PongStatus status = pongNotification.status();
		Instant start = Instant.now();
		//keep looping if start and now is less than one min or status is SUCCESSFUL
		while (
			(Duration.between(Instant.ofEpochMilli(start.toEpochMilli()),Instant.now()).toMinutes() < 1 ) &&
			(status == null || !status.equals(PongStatus.SUCCESSFUL) || !status.equals(PongStatus.COMPLETED) || !status.equals(PongStatus.FAILED))) {
			try {
				//_logger.info("sssssss");
				Thread.sleep(1000);
			}catch(Exception ex){
				
			}
			status = pongNotification.status();
			if (status != null) {
				break;
			}
		}	
		return status != null && status.equals(PongStatus.SUCCESSFUL);
	}

	public IWebsocketSend getWebsocketSend() {
		return websocketSend;
	}

	@Reference
	public void setWebsocketSend(IWebsocketSend websocketSend) {
		this.websocketSend = websocketSend;
	}

	private static class TestPongNotification implements IPongNotification {
		private PongStatus status = null;

		@Override
		public void notify(IPingMessage pingMessage, PongMessage pongMessage) {
			status = pongMessage.getPongStatus();
		}

		public PongStatus status() {
			return status;
		}

	}
}

package com.ge.predix.solsvc.edgestarter.impl;

import java.time.Duration;
import java.time.Instant;

import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ge.predix.solsvc.edgestarter.api.IMachineStatusService;
import com.ge.predix.solsvc.edgestarter.api.IPredixKitService;
import com.ge.predix.solsvc.edgestarter.api.ISampleProcessor;
import com.ge.predix.solsvc.edgestarter.model.MachineStatusResponse;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;

@Component(name=MachineStatusService.SERVICE_PID,provide = {IMachineStatusService.class})
public class MachineStatusService implements IMachineStatusService{
	
	/** Service PID for Sample Machine Adapter */
	protected static final String SERVICE_PID = "com.ge.predix.solsvc.edgestarter.statusservice"; //$NON-NLS-1$
	
	/** Create logger to report errors, warning massages, and info messages (runtime Statistics) */
    protected static Logger                         _logger          = LoggerFactory.getLogger(MachineStatusService.class);
    
	private ISampleProcessor processor;
	
	private IPredixKitService predixKitService;
	
	/**
	 * @param ctx
	 *            context of the bundle.
	 */
	@Activate
	public void activate(ComponentContext ctx) {
		_logger.info("Starting MachineStatusService " + SERVICE_PID); //$NON-NLS-1$	
	}
	
	@Override
	public MachineStatusResponse getMachineStatusResponse() {
		MachineStatusResponse response = new MachineStatusResponse();
		response.setLastDataSent(processor.getLastDataSent());
		if (processor.getLastDataSent() == null) {
			response.setStatus(MachineStatus.LAUNCHED_NOT_SENDING_DATA.status);
		}else {
			long mins = Duration.between(Instant.ofEpochMilli(processor.getLastDataSent().getTimeInMillis()),Instant.now()).toMinutes();
			_logger.info("Minutes : "+mins);
			boolean connected = processor.isConnectedToTimeseries();
			if (mins > 0 || !connected) {
				if (!connected) {
					response.setStatus(MachineStatus.LAUNCHED_NOT_CONNECTED.status);
				}else {
					response.setStatus(MachineStatus.LAUNCHED_NOT_SENDING_DATA.status);
				}
				response.setLastDataSent(null);
			}else{
				if (predixKitService.isRegistered()) {
					response.setStatus(MachineStatus.LAUNCHED_SENDING_DATA.status);
				}else {
					response.setStatus(MachineStatus.LAUNCHED_SEND_DATA_NOTREGISTERED.status);
				}			
			}	
		}
		return response;
	}

	public ISampleProcessor getProcessor() {
		return processor;
	}

	@Reference
	public void setProcessor(ISampleProcessor processor) {
		this.processor = processor;
	}
	
	@Reference
	public void setPredixKitService(IPredixKitService predixKitService) {
		this.predixKitService = predixKitService;
	}

	public enum MachineStatus {
		LAUNCHED("Launched"),LAUNCHED_SENDING_DATA("Launched - Sending Data"),
		LAUNCHED_NOT_SENDING_DATA("Launched - Not Sending Data"),
		LAUNCHED_SEND_DATA_NOTREGISTERED("Launched - Sending Data, Device not registered. Connected to custom Predix Timeseries"),
		LAUNCHED_NOT_CONNECTED("Launched - Not connected to Predix"),
		LAUNCHED_REGISTRATION_EXPIRED("Launched - Registration Expired");
		
		private String status;
		MachineStatus(String status){
			this.setStatus(status);
		}
		public String getStatus() {
			return status;
		}
		public void setStatus(String status) {
			this.status = status;
		}
	}
}

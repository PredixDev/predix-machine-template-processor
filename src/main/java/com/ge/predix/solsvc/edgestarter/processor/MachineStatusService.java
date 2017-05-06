package com.ge.predix.solsvc.edgestarter.processor;

import java.time.Duration;
import java.time.Instant;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ge.predix.solsvc.edgestarter.adapter.MachineStatusResponse;
import com.ge.predix.solsvc.edgestarter.rest.IMachineStatusService;
import com.ge.predix.solsvc.edgestarter.rest.ISampleProcessor;

@Component(service = {IMachineStatusService.class})
public class MachineStatusService implements IMachineStatusService{
	
	/** Create logger to report errors, warning massages, and info messages (runtime Statistics) */
    protected static Logger                         log          = LoggerFactory.getLogger(MachineStatusService.class);
    
	private ISampleProcessor processor;
		
	@Override
	public MachineStatusResponse getMachineStatusResponse() {
		MachineStatusResponse response = new MachineStatusResponse();
		response.setLastDataSent(processor.getLastDataSent());
		long mins = Duration.between(Instant.ofEpochMilli(processor.getLastDataSent().getTimeInMillis()),Instant.now()).toMinutes();
		log.info("Minutes : "+mins);
		boolean connected = processor.isConnectedToTimeseries();
		if (mins > 0 || !connected) {
			if (!connected) {
				response.setStatus(MachineStatus.LAUNCHED_NOT_CONNECTED.status);
			}else {
				response.setStatus(MachineStatus.LAUNCHED_NOT_SENDING_DATA.status);
			}
			response.setLastDataSent(null);
		}else{
			response.setStatus(MachineStatus.LAUNCHED_SENDING_DATA.status);
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
	
	public enum MachineStatus {
		LAUNCHED("Launched"),LAUNCHED_SENDING_DATA("Launched - Sending Data"),
		LAUNCHED_NOT_SENDING_DATA("Launched - Not Sending Data"),
		LAUNCHED_NOT_CONNECTED("Launched - Not connected to Predix");
		
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

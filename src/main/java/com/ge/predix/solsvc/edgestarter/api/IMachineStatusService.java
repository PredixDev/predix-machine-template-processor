package com.ge.predix.solsvc.edgestarter.api;

import com.ge.predix.solsvc.edgestarter.model.MachineStatusResponse;
/*
 * This interface provides method to get the Status of Predix Machine's websocket river.
 * 
 * 
 */
public interface IMachineStatusService {
	
	public MachineStatusResponse getMachineStatusResponse();
}

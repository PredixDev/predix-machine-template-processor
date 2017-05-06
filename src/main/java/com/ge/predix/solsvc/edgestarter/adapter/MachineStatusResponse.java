package com.ge.predix.solsvc.edgestarter.adapter;

import java.util.Calendar;

public class MachineStatusResponse {
	//Launched/Stopped/Error/Sending
	private String status = "Stopped";
	private Calendar lastDataSent;

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Calendar getLastDataSent() {
		return lastDataSent;
	}

	public void setLastDataSent(Calendar lastDataSent) {
		this.lastDataSent = lastDataSent;
	}
	
}

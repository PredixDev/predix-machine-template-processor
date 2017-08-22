package com.ge.predix.solsvc.edgestarter.api;

import java.util.Calendar;

import org.osgi.service.component.ComponentContext;

public interface ISampleProcessor {
	/*
	 * Get the Last time when data was sent
	 */
	public Calendar getLastDataSent();

	/*
	 * Check if Predix Machine is connected to Time series using web socket river
	 */
	public boolean isConnectedToTimeseries();

}

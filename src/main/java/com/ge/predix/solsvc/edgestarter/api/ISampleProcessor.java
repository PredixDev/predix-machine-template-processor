package com.ge.predix.solsvc.edgestarter.api;

import java.util.Calendar;

import org.osgi.service.component.ComponentContext;

public interface ISampleProcessor {

	public Calendar getLastDataSent();

	public boolean isConnectedToTimeseries();

}

package com.ge.predix.solsvc.edgestarter.api;

public interface IPredixKitService {
	/*
	 * validate the Device if activation is expired
	 */
	public void validateDevice();
	
	/*
	 * Return true if the device is registered with Kit service
	 */
	public Boolean isRegistered();
}

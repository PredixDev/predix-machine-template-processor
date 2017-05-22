package com.ge.predix.solsvc.edgestarter.impl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.util.Dictionary;

import org.codehaus.jackson.map.ObjectMapper;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ge.dspmicro.httpclient.api.HttpResponseWrapper;
import com.ge.dspmicro.httpclient.api.IHttpClient;
import com.ge.dspmicro.httpclient.api.IPredixCloudHttpClientFactory;
import com.ge.predix.solsvc.edgestarter.api.IPredixKitService;
import com.ge.predix.solsvc.edgestarter.api.ITemplateProcessorConfig;
import com.ge.predix.solsvc.edgestarter.model.RegisterDevice;
import com.ge.predix.solsvc.edgestarter.model.TemplateProcessorResources;

@Component(name = PredixKitService.SERVICE_PID,service={
		IPredixKitService.class
})
public class PredixKitService extends Thread implements IPredixKitService{
	
	private static final Logger _logger = LoggerFactory.getLogger(PredixKitService.class);
	
	private TemplateProcessorResources _resources = TemplateProcessorResources.getInstance();
	
	/** Service PID for Sample Machine Adapter */
	public static final String SERVICE_PID = "com.ge.predix.solsvc.edgestarter.kitservice"; //$NON-NLS-1$
	
	private IPredixCloudHttpClientFactory cloudHttpClientFactory;

    /** A reserved IHttpClient that supports Predix Cloud authenticated communication. */
    private IHttpClient                   cloudHttpClient;
    
    private ITemplateProcessorConfig config;
    
    private volatile boolean isShutdown = false;
	private volatile boolean isRetryMode = false;
	
	private volatile int pollingInterval = 30;
	private volatile int retryInterval = 0;
	private volatile int currentInterval;
	private int retries = 0;
	private int maxRetries = 0;
	
	private ConfigurationAdmin configurationAdmin;
	
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
		_logger.info("Starting PredixKitService " + SERVICE_PID); //$NON-NLS-1$	
		this.currentInterval = Integer.valueOf(this.config.getTaskInterval());
		this.start();
	}

	/**
     * This method is called when the bundle is stopped.
     * 
     * @param ctx Component Context
     */
    @Deactivate
    public void deactivate(ComponentContext ctx)
    {
        // Put your clean up code here when container is shutting down

        if ( _logger.isDebugEnabled() )
        {
            _logger.debug("Stopped sample for " + ctx.getBundleContext().getBundle().getSymbolicName()); //$NON-NLS-1$
        }
        shutdown();
    }
	/**
     * Dependency injection for IPredixCloudHttpClientFactory
     * 
     * @param clientFactory The IPredixCloudHttpClientFactory to inject
     */
    @Reference
    public void setPredixCloudHttpClientFactory(IPredixCloudHttpClientFactory clientFactory)
    {
        this.cloudHttpClientFactory = clientFactory;
        try
        {
            this.cloudHttpClient = this.cloudHttpClientFactory.createPredixCloudHttpClient();
        }
        catch (ConfigurationException e)
        {
            _logger.error("Error occurred in creating authenticated HTTP client", e); //$NON-NLS-1$
        }
    }

    /**
     * Clear the injected IPredixCloudHttpClientFactory
     * 
     * @param clientFactory The factory to clear.
     */
    public void unsetPredixCloudHttpClientFactory(IPredixCloudHttpClientFactory clientFactory)
    {
        if ( this.cloudHttpClientFactory == clientFactory )
        {
            this.cloudHttpClientFactory.deleteHttpClient(this.cloudHttpClient);
            this.cloudHttpClient = null;
            this.cloudHttpClientFactory = null;
        }
    }

    @Reference
	public void setConfig(ITemplateProcessorConfig config) {
		this.config = config;
	}
    
    public void validateDevice(){
    	if (!isShutdown
    			&& this.cloudHttpClient != null
    			&& this.config.getPredixKitGetDeviceURL() != null 
				&& !"".equals(this.config.getPredixKitGetDeviceURL())) {
			try
	        {
	            // Sending a GET request to the cloud.
				_logger.info("Get Device Info");
				_logger.info("Kit Device URL : "+this.config.getPredixKitGetDeviceURL()+InetAddress.getLocalHost().getHostName());
	            URI getDeviceURL = new URI(this.config.getPredixKitGetDeviceURL()+InetAddress.getLocalHost().getHostName());
	
	            HttpResponseWrapper httpResponse = this.cloudHttpClient.get(getDeviceURL);
	            _logger.info("GetDeviceResponse : "+httpResponse.getStatusCode());
	            String content = httpResponse.getContent();
	            _logger.info("Response : "+content);
	            if (!"".equals(content)) 
	            {
	            	ObjectMapper mapper = new ObjectMapper();
	            	RegisterDevice device = mapper.readValue(content, RegisterDevice.class);
	                _logger.info("Activation date : "+device.getActivationDate());
	            }
	        }catch(Exception e){
	        	_logger.error("Device activation expired. Disconecting machine from Predix.");
	        	disconnectingMachineFromPredix();
	        }
		}
    }

	@Override
	public void run() {
		if (this.pollingInterval == 0) {
			shutdown();
			return;
		}
		if (_logger.isDebugEnabled()) {
			// _logger.debug(_resources.getString("GatewayThread.gateway_starting"));
		}
		validateDevice();
		while (!this.isShutdown) {
			try {
				sleep(this.currentInterval * 1000);
			} catch (InterruptedException e) {
				if (this.isRetryMode) {
					if (this.retries >= this.maxRetries) {
						if (_logger.isDebugEnabled()) {
							// _logger.debug(_resources.getString("GatewayThread.max.retries.reached"));
						}
						normalMode();
						continue;
					}
					this.retries += 1;
					if (_logger.isDebugEnabled()) {
						_logger.debug(_resources.getString("GatewayThread.retry.count",
								new Object[] { Integer.valueOf(this.retries), Integer.valueOf(this.maxRetries) }));
					}
				}
			}
			validateDevice();
		}
		if (_logger.isDebugEnabled()) {
			_logger.debug(_resources.getString("GatewayThread.gateway_stopping"));
		}
		
	}
	public void normalMode() {
		if (this.isRetryMode) {
			this.isRetryMode = false;
			this.retries = 0;
			setCurrentInterval(this.pollingInterval);
			if (_logger.isDebugEnabled()) {
				_logger.debug(_resources.getString("GatewayThread.normal.mode"));
			}
		}
	}

	public void retryMode() {
		if ((!this.isRetryMode) && (this.maxRetries > 0) && (this.retryInterval > 0)) {
			this.isRetryMode = true;
			setCurrentInterval(this.retryInterval);
			if (_logger.isDebugEnabled()) {
				_logger.debug(_resources.getString("GatewayThread.retry.mode",
						new Object[] { Integer.valueOf(this.maxRetries) }));
			}
		}
	}

	public void setRetryParams(int interval, int maxRetries) {
		this.retryInterval = interval;
		this.maxRetries = maxRetries;
	}

	public void setPollingInterval(int duration) {
		this.pollingInterval = duration;
		setCurrentInterval(this.pollingInterval);
	}

	private void setCurrentInterval(int interval) {
		if (_logger.isDebugEnabled()) {
			_logger.debug(_resources.getString("GatewayThread.set.current.interval",
					new Object[] { Integer.valueOf(interval) }));
		}
		this.currentInterval = interval;
		interrupt();
	}
	
	public void shutdown() {
		this.isShutdown = true;
		interrupt();
	}

	public boolean isShutdown() {
		return this.isShutdown;
	}
	
	private void disconnectingMachineFromPredix() {
		// TODO Auto-generated method stub
		_logger.info("Disconnecting .....");
		if (this.configurationAdmin != null) {
			try {
				Configuration[] configs = this.configurationAdmin.listConfigurations(null);
				for (Configuration config : configs) {
					
					if ("com.ge.dspmicro.websocketriver.send".equals(config.getFactoryPid())) {
						_logger.info("factory : "+config.getFactoryPid());
						Dictionary<String, Object> props = config.getProperties();
						props.put("com.ge.dspmicro.websocketriver.send.destination.url", "");
						props.put("com.ge.dspmicro.websocketriver.send.header.zone.name", "");
						props.put("com.ge.dspmicro.websocketriver.send.header.zone.value", "");
						config.update(props);
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvalidSyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Reference
	public void setConfigurationAdmin(ConfigurationAdmin configurationAdmin) {
		this.configurationAdmin = configurationAdmin;
	}
}

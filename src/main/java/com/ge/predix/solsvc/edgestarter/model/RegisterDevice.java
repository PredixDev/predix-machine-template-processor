package com.ge.predix.solsvc.edgestarter.model;

import java.util.Map;
import java.util.Set;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;


/**
 * 
 * @author 212421693 -
 */
public class RegisterDevice
{
    /**
     *  -
     */
    public RegisterDevice()
    {
        super();
    }
    /**
     * 
     */
    String uri;
   
   
    /**
     * 
     */
    String deviceName;
    /**
     * 
     */
    String deviceAddress;
    /**
     * 
     */
    String deviceType;
    /**
     * 
     */
    String activationDate;
    /**
     * 
     */
    String groupRef;
    /**
     * 
     */
    private String userId;
    /**
     * 
     */
    private String createdDate;
    
    /**
     * 
     */
    private String updateDate;
    
    private String error;
    
    private String error_description;
    
    private String scope;
    
    private Map<?, ?> deviceConfig;
    
    private Set<?> tags;
    
    private String message;
    
    private String status;
    
    private String timeStamp;
    
    /**
     * @return the uri
     */
    public String getUri()
    {
        return this.uri;
    }
    /**
     * @param uri the uri to set
     */
    public void setUri(String uri)
    {
        this.uri = uri;
    }
    /**
     * @return the deviceName
     */
    public String getDeviceName()
    {
        return this.deviceName;
    }
    /**
     * @param deviceName the deviceName to set
     */
    public void setDeviceName(String deviceName)
    {
        this.deviceName = deviceName;
    }
    /**
     * @return the deviceAddress
     */
    public String getDeviceAddress()
    {
        return this.deviceAddress;
    }
    /**
     * @param deviceAddress the deviceAddress to set
     */
    public void setDeviceAddress(String deviceAddress)
    {
        this.deviceAddress = deviceAddress;
    }
    /**
     * @return the deviceType
     */
    public String getDeviceType()
    {
        return this.deviceType;
    }
    /**
     * @param deviceType the deviceType to set
     */
    public void setDeviceType(String deviceType)
    {
        this.deviceType = deviceType;
    }
    /**
     * @return the activationDate
     */
    public String getActivationDate()
    {
        return this.activationDate;
    }
    /**
     * @param activationDate the activationDate to set
     */
    public void setActivationDate(String activationDate)
    {
        this.activationDate = activationDate;
    }
    /**
     * @return the groupRef
     */
    public String getGroupRef()
    {
        return this.groupRef;
    }
    /**
     * @param groupRef the groupRef to set
     */
    public void setGroupRef(String groupRef)
    {
        this.groupRef = groupRef;
    }
    /**
     * @return the createdDate
     */
    public String getCreatedDate()
    {
        return this.createdDate;
    }
    /**
     * @param createdDate the createdDate to set
     */
    public void setCreatedDate(String createdDate)
    {
        this.createdDate = createdDate;
    }
    
    /**
     * @return the userId
     */
    public String getUserId()
    {
        return this.userId;
    }
    /**
     * @param userId the userId to set
     */
    public void setUserId(String userId)
    {
        this.userId = userId;
    }
    /**
     * @return the deviceConfig
     */
    public Map<?, ?> getDeviceConfig()
    {
        return this.deviceConfig;
    }
    /**
     * @param deviceConfig the deviceConfig to set
     */
    public void setDeviceConfig(Map<?, ?> deviceConfig)
    {
        this.deviceConfig = deviceConfig;
    }
    /**
     * @return the updateDate
     */
    public String getUpdateDate()
    {
        return this.updateDate;
    }
    /**
     * @param updateDate the updateDate to set
     */
    public void setUpdateDate(String updateDate)
    {
        this.updateDate = updateDate;
    }
    
	public Set<?> getTags() {
		return tags;
	}
	
	public void setTags(Set<?> tags) {
		this.tags = tags;
	}
	
	@JsonProperty("error")
	public String getError() {
		return error;
	}
	public void setError(String error) {
		this.error = error;
	}
	
	@JsonProperty("scope")
	public String getScope() {
		return scope;
	}
	
	public void setScope(String scope) {
		this.scope = scope;
	}
	public String getError_description() {
		return error_description;
	}
	public void setError_description(String error_description) {
		this.error_description = error_description;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getTimeStamp() {
		return timeStamp;
	}
	public void setTimeStamp(String timeStamp) {
		this.timeStamp = timeStamp;
	}
    
    
}
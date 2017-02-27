package com.pi.pf.pingdirectorydatastorage;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Client
{
    private String clientId;
    private String[] redirectUris = new String [0];
    
	private Set<String> grantTypes = new HashSet<>();
    private String clientSecret;
    private String name;
    private String description;
    private String logoUrl;
    private boolean refreshRolling;
    private Long persistentGrantExpirationTime;
    private String persistentGrantExpirationTimeUnit;

    private boolean bypassApprovalPage;
    private boolean restrictScopes;
    private String[]  restrictedScopes = new String [0];

    // client cert values
    private String clientCertIssuerDn;
    private String clientCertSubjectDn;

    private Map<String, String> supplementalInfo = new HashMap<>();

    private boolean pingAccessLogoutCapable = false;
    private String[] logoutUris = new String [0];

    public Client()
    {
    }
    
    public String[] getRedirectUris() {
		return redirectUris;
	}

	public void setRedirectUris(String[] redirectUris) {
		this.redirectUris = redirectUris;
	}

    public String[] getLogoutUris() {
		return logoutUris;
	}

	public void setLogoutUris(String[] logoutUris) {
		this.logoutUris = logoutUris;
	}
	
	
	
	
	public boolean isRefreshRolling() {
		return refreshRolling;
	}

	public void setRefreshRolling(boolean refreshRolling) {
		this.refreshRolling = refreshRolling;
	}

	public String getClientId()
    {
        return clientId;
    }

    public void setClientId(String clientId)
    {
        this.clientId = (clientId != null) ? clientId.trim() : null;
    }

   

    public void setGrantTypes(Set<String> grantTypes)
    {
        this.grantTypes = grantTypes;
    }

    public Set<String> getGrantTypes()
    {
        return grantTypes;
    }

    
    
    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getLogoUrl()
    {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl)
    {
        this.logoUrl = (logoUrl != null) ? logoUrl.trim() : null;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

	

	public String getClientSecret() {
		return clientSecret;
	}

	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}

	

	public Long getPersistentGrantExpirationTime() {
		return persistentGrantExpirationTime;
	}

	public void setPersistentGrantExpirationTime(Long persistentGrantExpirationTime) {
		this.persistentGrantExpirationTime = persistentGrantExpirationTime;
	}

	public String getPersistentGrantExpirationTimeUnit() {
		return persistentGrantExpirationTimeUnit;
	}

	public void setPersistentGrantExpirationTimeUnit(String persistentGrantExpirationTimeUnit) {
		this.persistentGrantExpirationTimeUnit = persistentGrantExpirationTimeUnit;
	}

	public boolean isBypassApprovalPage() {
		return bypassApprovalPage;
	}

	public void setBypassApprovalPage(boolean bypassApprovalPage) {
		this.bypassApprovalPage = bypassApprovalPage;
	}

	public boolean isRestrictScopes() {
		return restrictScopes;
	}

	public void setRestrictScopes(boolean restrictScopes) {
		this.restrictScopes = restrictScopes;
	}

	public String getClientCertIssuerDn() {
		return clientCertIssuerDn;
	}

	public void setClientCertIssuerDn(String clientCertIssuerDn) {
		this.clientCertIssuerDn = clientCertIssuerDn;
	}

	public String getClientCertSubjectDn() {
		return clientCertSubjectDn;
	}

	public void setClientCertSubjectDn(String clientCertSubjectDn) {
		this.clientCertSubjectDn = clientCertSubjectDn;
	}

	public Map<String, String> getSupplementalInfo() {
		return supplementalInfo;
	}

	public void setSupplementalInfo(Map<String, String> supplementalInfo) {
		this.supplementalInfo = supplementalInfo;
	}

	public boolean isPingAccessLogoutCapable() {
		return pingAccessLogoutCapable;
	}

	public void setPingAccessLogoutCapable(boolean pingAccessLogoutCapable) {
		this.pingAccessLogoutCapable = pingAccessLogoutCapable;
	}

	public String[] getRestrictedScopes() {
		return restrictedScopes;
	}

	public void setRestrictedScopes(String[] restrictedScopes) {
		this.restrictedScopes = restrictedScopes;
	}

	

    
}

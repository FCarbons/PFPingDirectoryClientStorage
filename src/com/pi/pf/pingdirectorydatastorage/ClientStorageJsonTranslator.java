package com.pi.pf.pingdirectorydatastorage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.sourceid.config.GlobalRegistry;
import org.sourceid.oauth.client.xmlbinding.ClientDocument;
import org.sourceid.oauth.client.xmlbinding.ClientType;
import org.sourceid.oauth.client.xmlbinding.GrantTypeType;
import org.sourceid.oauth.client.xmlbinding.SupplementalInfoType;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pingidentity.common.util.Base64URL;
import com.pingidentity.common.util.xml.XmlBeansUtil;
import com.pingidentity.configservice.XmlLoader;
import com.pingidentity.sdk.oauth20.ClientData;

public class ClientStorageJsonTranslator {

	private final Log log = LogFactory.getLog(ClientStorageJsonTranslator.class);
	private XmlLoader xmlLoader = GlobalRegistry.getService(XmlLoader.class);

	public String clientDataToJson(String clientData) throws XmlException, JsonProcessingException {

		// parse ClientData data (data =
		// “1.asdfasdfawer234234234234234234sdfssgzdfgasg")
		String[] dataParts = clientData.split("\\.");
		String encodedXmlClientDocumentString = dataParts[1];

		// decode the xml client
		String xmlString = Base64URL.decodeToString(encodedXmlClientDocumentString, StandardCharsets.UTF_8.name());

		log.debug("xmlString: " + xmlString);

		ByteArrayInputStream inStream;
		try {
			inStream = new ByteArrayInputStream(xmlString.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new XmlException(e);
		}
		XmlObject xmlObj = xmlLoader.load(inStream);

		ClientDocument clientDoc = null;
		if (xmlObj instanceof ClientDocument) {
			clientDoc = (ClientDocument) xmlObj;
		}

		ClientType clientType = clientDoc.getClient();

		ObjectMapper mapper = new ObjectMapper();
		return mapper.writeValueAsString(getClientFromClientType(clientType));
	}

	public ClientData jsonToClientData(String json) throws JsonParseException, JsonMappingException, IOException {
		String formatId = "1";

		XmlOptions opts = XmlBeansUtil.getXmlOptions();
		opts.setUseDefaultNamespace();

		ClientDocument clientDoc = ClientDocument.Factory.newInstance();
		ClientType xmlClient = clientDoc.addNewClient();

		ObjectMapper mapper = new ObjectMapper();
		Client client = mapper.readValue(json, Client.class);

		xmlClient.setBypassApprovalPage(client.isBypassApprovalPage());
		xmlClient.setClientCertIssuerDN(client.getClientCertIssuerDn());
		xmlClient.setClientCertSubjectDN(client.getClientCertSubjectDn());
		xmlClient.setClientId(client.getClientId());
		if (StringUtils.isNotBlank(client.getClientSecret())) {
			xmlClient.setSecret(client.getClientSecret());
		}
		xmlClient.setDescription(client.getDescription());
		for (String grant : client.getGrantTypes()) {
			GrantTypeType gtt = xmlClient.addNewGrant();
			gtt.setType(grant);

		}

		xmlClient.setLogo(client.getLogoUrl());
		for (String logoutUri : client.getLogoutUris()) {
			xmlClient.addLogoutUri(logoutUri);
		}
		xmlClient.setName(client.getName());
		xmlClient.setPersistentGrantExpirationTime(client.getPersistentGrantExpirationTime());
		xmlClient.setPersistentGrantExpirationTimeUnit(client.getPersistentGrantExpirationTimeUnit());
		for (String redirect : client.getRedirectUris()) {
			xmlClient.addRedirectUri(redirect);
		}
		xmlClient.setRefreshRolling(client.isRefreshRolling());

		if (client.isRestrictScopes()) {
			for (String scope : client.getRestrictedScopes()) {
				xmlClient.addRestrictedScope(scope);
			}
		}

		xmlClient.setRestrictScopes(client.isRestrictScopes());
		for (Map.Entry<String, String> entry : client.getSupplementalInfo().entrySet()) {
			if (entry.getValue() != null) {
				SupplementalInfoType info = xmlClient.addNewSupplementalInfo();
				info.setKey(entry.getKey());
				info.setValue(entry.getValue());
			}
		}

		String xml = clientDoc.xmlText(opts);

		log.debug("Xml: " + xml);
		String encodedClientDocument = Base64URL.encodeToString(xml.getBytes(StandardCharsets.UTF_8));
		String clientDataDataString = formatId + "." + encodedClientDocument;

		ClientData clientData = new ClientData();
		clientData.setId(client.getClientId());
		clientData.setData(clientDataDataString);

		return clientData;
	}

	protected Client getClientFromClientType(ClientType clientType) {
		Client client = new Client();
		GrantTypeType[] grantType = clientType.getGrantArray();
		Set<String> grants = new HashSet<String>();
		for (int i = 0; i < grantType.length; i++) {
			grants.add(grantType[i].getType());
		}

		SupplementalInfoType[] supplementalInfoType = clientType.getSupplementalInfoArray();
		Map<String, String> supplementalInfo = new HashMap<String, String>();

		for (int i = 0; i < supplementalInfoType.length; i++) {
			supplementalInfo.put(supplementalInfoType[i].getKey(), supplementalInfoType[i].getValue());
		}

		client.setBypassApprovalPage(clientType.getBypassApprovalPage());
		client.setClientCertIssuerDn(clientType.getClientCertIssuerDN());
		client.setClientCertSubjectDn(clientType.getClientCertSubjectDN());
		client.setClientId(clientType.getClientId());
		client.setClientSecret(clientType.getSecret());
		client.setDescription(clientType.getDescription());
		client.setGrantTypes(grants);
		client.setLogoUrl(clientType.getLogo());
		client.setLogoutUris(clientType.getLogoutUriArray());
		client.setName(clientType.getName());
		client.setPersistentGrantExpirationTime(clientType.getPersistentGrantExpirationTime());
		client.setPersistentGrantExpirationTimeUnit(clientType.getPersistentGrantExpirationTimeUnit());
		client.setRedirectUris(clientType.getRedirectUriArray());
		client.setRefreshRolling(clientType.getRefreshRolling());
		client.setRestrictScopes(clientType.getRestrictScopes());
		client.setRestrictedScopes(clientType.getRestrictedScopeArray());
		client.setSupplementalInfo(supplementalInfo);
		return client;
	}

	public static void main(String[] args) throws XmlException, IOException {
		String clientData = "1.PENsaWVudCBjbGllbnRfaWQ9IlRlc3QxIiBuYW1lPSJUZXN0MSIgbG9nbz0iIiBzZWNyZXQ9IjAyQnRWSEJLZ3lBdHNzdVE1Mlh6cHhnbGFkZVJSWjZDbU9NRS1JbWRZc2suZHluUzNiQUwuMiIgcGVyc2lzdGVudEdyYW50RXhwaXJhdGlvblRpbWU9IjEiIHBlcnNpc3RlbnRHcmFudEV4cGlyYXRpb25UaW1lVW5pdD0iZCIgbGFzdE1vZGlmaWVkPSIyMDE3LTAyLTI3VDE0OjQ2OjUxLjE4NCswMTowMCIgYnlwYXNzQXBwcm92YWxQYWdlPSJ0cnVlIiByZXN0cmljdFNjb3Blcz0idHJ1ZSIgeG1sbnM9InVybjpwaW5naWRlbnRpdHkuY29tOnBmOm9hdXRoOmNsaWVudCI-PERlc2NyaXB0aW9uPk1ZRGVzY3JpdHB0aW9uPC9EZXNjcmlwdGlvbj48R3JhbnQgdHlwZT0icmVmcmVzaF90b2tlbiIvPjxHcmFudCB0eXBlPSJpbXBsaWNpdCIvPjxHcmFudCB0eXBlPSJwYXNzd29yZCIvPjxHcmFudCB0eXBlPSJjbGllbnRfY3JlZGVudGlhbHMiLz48R3JhbnQgdHlwZT0iZXh0ZW5zaW9uIi8-PEdyYW50IHR5cGU9ImF1dGhvcml6YXRpb25fY29kZSIvPjxHcmFudCB0eXBlPSJ1cm46cGluZ2lkZW50aXR5LmNvbTpvYXV0aDI6Z3JhbnRfdHlwZTp2YWxpZGF0ZV9iZWFyZXIiLz48UmVkaXJlY3RVcmk-aHR0cHM6Ly9teXJlcmlyZWN0dXJpMS5jb208L1JlZGlyZWN0VXJpPjxSZWRpcmVjdFVyaT5odHRwczovL215cmVyaXJlY3R1cmkyLmNvbTwvUmVkaXJlY3RVcmk-PFJlc3RyaWN0ZWRTY29wZT5hZGRyZXNzPC9SZXN0cmljdGVkU2NvcGU-PFN1cHBsZW1lbnRhbEluZm8ga2V5PSJBQ0NFU1NfU0VTU0lPTl9SRVZPQ0FUSU9OX0FQSSIgdmFsdWU9InRydWUiLz48U3VwcGxlbWVudGFsSW5mbyBrZXk9IlZBTElEQVRFX1VTSU5HX0FMTF9FTElHSUJMRV9BVE1TIiB2YWx1ZT0idHJ1ZSIvPjxTdXBwbGVtZW50YWxJbmZvIGtleT0iREVGQVVMVF9BVE1fSUQiIHZhbHVlPSJkZWZhdWx0Ii8-PFN1cHBsZW1lbnRhbEluZm8ga2V5PSJ2bmQucGluZy5wb2xpY3lfZ3JvdXBfaWQiIHZhbHVlPSJPQXV0aFBsYXlncm91bmQiLz48U3VwcGxlbWVudGFsSW5mbyBrZXk9IlBJTkdfQUNDRVNTX0xPR09VVF9DQVBBQkxFIiB2YWx1ZT0iZmFsc2UiLz48U3VwcGxlbWVudGFsSW5mbyBrZXk9ImlkX3Rva2VuX3NpZ25lZF9yZXNwb25zZV9hbGciIHZhbHVlPSJub25lIi8-PC9DbGllbnQ-";
		System.out.println("ClientData: " + clientData);
		String json = new ClientStorageJsonTranslator().clientDataToJson(clientData);
		System.out.println("Json: " + json);
		System.out.println("ClientData: " + new ClientStorageJsonTranslator().jsonToClientData(json).getData());
	}

}

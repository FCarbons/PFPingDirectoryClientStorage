package com.pi.pf.pingdirectorydatastorage;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.ldap.LdapName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sourceid.config.ConfigStore;
import org.sourceid.config.ConfigStoreFarm;
import org.sourceid.saml20.domain.LdapDataSource;
import org.sourceid.saml20.domain.mgmt.MgmtFactory;
import org.sourceid.util.log.AttributeMap;

import com.pingidentity.common.util.Base64URL;
import com.pingidentity.common.util.ldap.LDAPUtil;
import com.pingidentity.common.util.ldap.LDAPUtilOptions;
import com.pingidentity.sdk.oauth20.ClientData;
import com.pingidentity.sdk.oauth20.ClientStorageManagementException;
import com.pingidentity.sdk.oauth20.ClientStorageManager;

/**
 * This class provides a sample implementation for client storage. It uses a
 * HashMap to store the clients with the key being the client id. All CRUD
 * operations, such as read, add, delete, update etc. are done on this HashMap.
 *
 * To use this implementation, modify
 * ./server/default/conf/META-INF/hivemodule.xml and modify 2 service points
 * "ClientManager" and "ClientStorageManager" to become:
 *
 * <!-- Service for storing OAuth client configuration. Part 1 (of 3) -->
 * <service-point id="ClientStorageManager"
 * interface="com.pingidentity.sdk.oauth20.ClientStorageManager">
 * <invoke-factory> <construct
 * class="com.pingidentity.clientstorage.SampleClientStorage"/>
 * </invoke-factory> </service-point>
 *
 * <!-- Service for storing OAuth client configuration. Part 2 (of 3) -->
 * <service-point id="ClientManager"
 * interface="org.sourceid.oauth20.domain.ClientManager"> <invoke-factory>
 * <construct class="org.sourceid.oauth20.domain.ClientManagerGenericImpl"/>
 * </invoke-factory> </service-point>
 *
 * <!-- Part 3 (of 3) --> Follow the instructions in the SDK Developer's Guide
 * for building and deploying.
 */

public class PingDirectoryClientStorage implements ClientStorageManager {

	private final Log log = LogFactory.getLog(PingDirectoryClientStorage.class);

	protected ConfigStore configStore;
	protected String jndiName;
	protected String searchBase;
	protected String clientObjectClassName;
	protected LdapDataSource ldapSource;

	private final String FIELDNAME_CLIENT_ID = "clientId";
	private final String FIELDNAME_CLIENT_ATTRIBUTES = "clientAttributes";

	public PingDirectoryClientStorage() {
		log.debug("Creating PingDirectoryClientStorage");
		configStore = ConfigStoreFarm.getConfig(this.getClass());
		jndiName = configStore.getStringValue("PingFederateDSJNDIName", null);
		searchBase = configStore.getStringValue("SearchBase", "");
		clientObjectClassName = configStore.getStringValue("ClientObjectClassName", null);

		if (ldapSource == null) {
			ldapSource = MgmtFactory.getDataSourceManager().getLdapDataSource(jndiName);
		}

		log.debug("JndiName " + jndiName);
		log.debug("searchBase " + searchBase);
		log.debug("clientObjectClassName " + clientObjectClassName);
	}

	/**
	 * Retrieves a client record by client id.
	 *
	 * @param String
	 *            clientId The client id.
	 * @return A ClientData with the clientId matching the param. Returns null
	 *         if the clientId is not found.
	 * @throws com.pingidentity.sdk.oauth20.ClientStorageManagementException
	 *             Checked exception to indicate the retrieval of client record
	 *             has failed.
	 */
	@Override
	public ClientData getClient(String clientId) throws ClientStorageManagementException {
		log.debug("Starting getClient: " + clientId);
		try {
			String clientDN = FIELDNAME_CLIENT_ID + "=" + clientId + "," + searchBase;
			log.debug("Loading clinet for DN: " + clientDN);
			AttributeMap clientAttributes = LDAPUtil.getInstance(ldapSource).getAttributesOfMatchingObject(clientDN);

			log.debug("Client attributes: " + clientAttributes);
			return getClientData(clientAttributes);
		} catch (Exception e) {
			log.error(e);
			throw new ClientStorageManagementException(e);
		}
	}

	/**
	 * Retrieves all client records.
	 *
	 * @return A collection of all client records.
	 * @throws com.pingidentity.sdk.oauth20.ClientStorageManagementException
	 *             Checked exception to indicate the retrieval of client records
	 *             has failed.
	 */
	@Override
	public Collection<ClientData> getClients() throws ClientStorageManagementException {
		log.debug("Starting getClients");
		List<ClientData> results = new ArrayList<ClientData> ();

		try {
			String searchCriteria = "(objectClass=" + clientObjectClassName + ")";
			LDAPUtilOptions ldapOptions = new LDAPUtilOptions(searchBase, searchCriteria, SearchControls.SUBTREE_SCOPE);
			List<AttributeMap> attributeMapList = LDAPUtil.getInstance(ldapSource).getAttributesOfMatchingObjects(ldapOptions);
			for (AttributeMap attributeMap : attributeMapList) {
				results.add(getClientData(attributeMap));
			}

		} catch (Exception e) {
			log.error(e);
			throw new ClientStorageManagementException(e);
		}
		return results;
	}

	/**
	 * Add a client record.
	 *
	 * @param ClientData
	 *            The client object.
	 * @throws com.pingidentity.sdk.oauth20.ClientStorageManagementException
	 *             Checked exception to indicate that the operation of adding a
	 *             client record has failed.
	 */
	@Override
	public void addClient(ClientData client) throws ClientStorageManagementException {
		log.debug("Starting addClient: " + client.getId() + " data " + decodeClientData(client.getData()));
		try {
			LdapName ldapName = new LdapName(FIELDNAME_CLIENT_ID + "=" + client.getId() + "," + searchBase);

			Attributes attrsToLdap = new BasicAttributes();
			attrsToLdap.put("objectclass", "top");
			attrsToLdap.put("objectclass", clientObjectClassName);

			attrsToLdap.put(FIELDNAME_CLIENT_ID, client.getId());
			attrsToLdap.put(FIELDNAME_CLIENT_ATTRIBUTES, client.getData());

			LDAPUtil.getInstance(ldapSource).addAccessGrant(ldapName, attrsToLdap);
		} catch (Exception e) {
			log.error(e);
			throw new ClientStorageManagementException(e);
		}
	}

	/**
	 * Delete a client record.
	 *
	 * @param clientId
	 *            The client id.
	 * @throws com.pingidentity.sdk.oauth20.ClientStorageManagementException
	 *             Checked exception to indicate that the operation of removing
	 *             a client record has failed.
	 */
	@Override
	public void deleteClient(String clientId) throws ClientStorageManagementException {
		log.debug("Starting deleteClient: " + clientId);
		try {
			LdapName ldapName = new LdapName(FIELDNAME_CLIENT_ID + "=" + clientId + "," + searchBase);
			LDAPUtil.getInstance(ldapSource).deleteAccessGrant(ldapName);
		} catch (Exception e) {
			log.error(e);
			throw new ClientStorageManagementException(e);
		}
	}

	/**
	 * Updating a client record.
	 *
	 * @param ClientData
	 *            The client object.
	 * @throws com.pingidentity.sdk.oauth20.ClientStorageManagementException
	 *             Checked exception to indicate that the operation of updating
	 *             a client record has failed.
	 */
	@Override
	public void updateClient(ClientData client) throws ClientStorageManagementException {
		log.debug("Starting updateClient: " + client.getId() + " data " + decodeClientData(client.getData()));
		try {
			LdapName ldapName = new LdapName(FIELDNAME_CLIENT_ID + "=" + client.getId() + "," + searchBase);
			Attributes attributes = new BasicAttributes();
			attributes.put(FIELDNAME_CLIENT_ATTRIBUTES, client.getData());
			LDAPUtil.getInstance(ldapSource).modifyItem(ldapName, attributes, DirContext.REPLACE_ATTRIBUTE);
		} catch (Exception e) {
			log.error(e);
			throw new ClientStorageManagementException(e);
		}
	}

	private String decodeClientData(String clientData) {
		// parse ClientData data (data =
		// “1.asdfasdfawer234234234234234234sdfssgzdfgasg")
		String[] dataParts = clientData.split("\\.");
		String encodedXmlClientDocumentString = dataParts[1];

		// decode the xml client
		String xmlString = Base64URL.decodeToString(encodedXmlClientDocumentString, StandardCharsets.UTF_8.name());
		return xmlString;
	}

	private String encodeClientData(String clientData) {
		String formatId = "1";
		String encodedClientDocument = Base64URL.encodeToString(clientData.getBytes(StandardCharsets.UTF_8));
		String encodedClientDataString = formatId + "." + encodedClientDocument;
		return encodedClientDataString;
	}

	private ClientData getClientData(AttributeMap attributeMap) {

		if (attributeMap == null) {
			return null;

		}
		ClientData currentClientData = new ClientData();
		currentClientData.setId(attributeMap.getSingleValue(FIELDNAME_CLIENT_ID));
		currentClientData.setData(attributeMap.getSingleValue(FIELDNAME_CLIENT_ATTRIBUTES));
		return currentClientData;
	}
}

/****************************************************************************** 
 * Licensed Materials - Property of IBM 
 * "Restricted Materials of IBM"
 * © Copyright IBM Corp. 2014 All Rights Reserved. 
 * 
 * Copying, redistribution and/or modification is prohibited. 
 * U.S. Government Users Restricted Rights - Use, duplication or 
 * disclosure restricted by GSA ADP Schedule Contract with IBM Corp. 
 *****************************************************************************/
package com.ibm.haac.hx.engine.tool.mongodb;

import org.mongodb.morphia.Datastore;

/**
 * MongoDB access definition
 * 
 * @author antonioj
 * 
 */
public interface IDBAccess {

	/**
	 * Gets data store for data processing
	 * 
	 * @return Data store, if created successfully
	 */
	Datastore getDatastore();

	/**
	 * Maps entity to database. Entity is created based on property and
	 * annotation processing
	 * 
	 * @param clazz
	 *            Entity class
	 */
	void mapEntity(Class<?>... clazz);
}

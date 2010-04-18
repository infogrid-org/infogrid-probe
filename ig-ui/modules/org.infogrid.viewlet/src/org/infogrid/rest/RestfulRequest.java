//
// This file is part of InfoGrid(tm). You may not use this file except in
// compliance with the InfoGrid license. The InfoGrid license and important
// disclaimers are contained in the file LICENSE.InfoGrid.txt that you should
// have received with InfoGrid. If you have not received LICENSE.InfoGrid.txt
// or you do not consent to all aspects of the license and the disclaimers,
// no license is granted; do not use this file.
// 
// For more information about InfoGrid go to http://infogrid.org/
//
// Copyright 1998-2010 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.rest;

import java.text.ParseException;
import java.util.Map;
import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.MeshObjectIdentifier;
import org.infogrid.mesh.NotPermittedException;
import org.infogrid.meshbase.MeshBaseIdentifier;
import org.infogrid.meshbase.MeshObjectAccessException;
import org.infogrid.util.http.SaneRequest;
import org.infogrid.util.http.SaneRequestUtils;

/**
 * Encapsulates parameter parsing according to InfoGrid REST conventions.
 */
public interface RestfulRequest
{
    /**
     * Obtain the underlying SaneRequest.
     *
     * @return the SaneRequest
     */
    public SaneRequest getSaneRequest();

    /**
     * Determine the identifier of the requested MeshBase.
     * 
     * @return the MeshBaseIdentifier
     * @throws ParseException thrown if the request URI could not be parsed
     */
    public MeshBaseIdentifier determineRequestedMeshBaseIdentifier()
            throws
                ParseException;

    /**
     * Determine the identifier of the requested MeshObject.
     * 
     * @return the MeshObjectIdentifier
     * @throws ParseException thrown if the request URI could not be parsed
     */
    public MeshObjectIdentifier determineRequestedMeshObjectIdentifier()
            throws
                ParseException;

    /**
     * Determine the requested MeshObject.
     * 
     * @return the MeshObject, or null if not found
     * @throws MeshObjectAccessException thrown if the requested MeshObject could not be accessed
     * @throws NotPermittedException thrown if the caller did not have the permission to perform this operation
     * @throws ParseException thrown if the request URI could not be parsed
     */
    public MeshObject determineRequestedMeshObject()
            throws
                MeshObjectAccessException,
                NotPermittedException,
                ParseException;

    /**
     * Determine the requested traversal parameters, if any.
     * 
     * @return the traversal parameters
     */
    public String [] getRequestedTraversalParameters();
    
    /**
     * Obtain the name of the requested Viewlet type, if any.
     *
     * @return type name of the requested Viewlet
     */
    public String getRequestedViewletTypeName();
    
    /**
     * Obtain the requested MIME type, if any.
     * 
     * @return the requuested MIME type, if any
     */
    public String getRequestedMimeType();

    /**
     * Obtain the parameters for the Viewlet, if any. They can be multi-valued.
     * 
     * @return the parameters, if any
     */
    public Map<String,String[]> getViewletParameters();

    /**
     * Name of the request attribute that contains an instance of this type.
     */
    public static final String RESTFUL_REQUEST_ATTRIBUTE_NAME
            = SaneRequestUtils.classToAttributeName( RestfulRequest.class );

    /**
     * Name of the LID format parameter.
     */
    public static final String LID_FORMAT_PARAMETER_NAME = "lid-format";

    /**
     * The prefix in the lid-format string that indicates the name of a viewlet.
     */
    public static final String VIEWLET_PREFIX = "viewlet:";
    
    /**
     * The prefix in the lid-format string that indicates the name of a MIME type.
     */
    public static final String MIME_PREFIX = "mime:";

    /**
     * Name of the LID traversal parameter.
     */
    public static final String LID_TRAVERSAL_PARAMETER_NAME = "lid-traversal";
}
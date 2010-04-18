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

package org.infogrid.mesh.a;

import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.MeshObjectIdentifier;
import org.infogrid.mesh.text.MeshStringRepresentationParameters;
import org.infogrid.meshbase.MeshBase;
import org.infogrid.util.AbstractIdentifier;
import org.infogrid.util.Identifier;
import org.infogrid.util.text.StringRepresentation;
import org.infogrid.util.text.StringRepresentationParameters;
import org.infogrid.util.text.StringifierException;

/**
 * Implements MeshObjectIdentifier for the "A" implementation.
 */
public class DefaultAMeshObjectIdentifier
        extends
             AbstractIdentifier
        implements
             MeshObjectIdentifier
{
    /**
     * Factory method.
     *
     * @param factory the DefaultAMeshObjectIdentifierFactory that created this identifier
     * @param localId the localId of the to-be-DefaultAMeshObjectIdentifier
     * @param asEnteredByUser String form as entered by the user, if any. This helps with error messages.
     * @return the created DefaultAMeshObjectIdentifier
     */
    static DefaultAMeshObjectIdentifier create(
            DefaultAMeshObjectIdentifierFactory factory,
            String                              localId,
            String                              asEnteredByUser )
    {
        // all correctness checking is being moved into the factory.

        if( localId == null || localId.length() == 0 ) {
            return factory.HOME_OBJECT;
        }

        return new DefaultAMeshObjectIdentifier( factory, localId, asEnteredByUser );
    }

    /**
     * Private constructor, use factory method.
     * 
     * @param factory the DefaultAMeshObjectIdentifierFactory that created this identifier
     * @param localId the localId of the to-be-created DefaultAMeshObjectIdentifier
     * @param asEnteredByUser String form of this Identifier as entered by the user, if any. This helps with error messages.
     */
    protected DefaultAMeshObjectIdentifier(
            DefaultAMeshObjectIdentifierFactory factory,
            String                              localId,
            String                              asEnteredByUser )
    {
        super( asEnteredByUser );

        theFactory = factory;
        theLocalId = localId;
    }

    /**
     * Obtain the factory that created this identifier.
     *
     * @return the factory
     */
    public DefaultAMeshObjectIdentifierFactory getFactory()
    {
        return theFactory;
    }

    /**
     * Obtain the localId component.
     *
     * @return the localId component
     */
    public String getLocalId()
    {
        return theLocalId;
    }

    /**
     * Determine whether this MeshObjectIdentifier identifies a Home Object.
     *
     * @return true if it identifies a Home Object
     */
    public boolean identifiesHomeObject()
    {
        return toExternalForm().length() == 0;
    }

    /**
     * Obtain the external form of the MeshObjectIdentifier relative to some path.
     *
     * @param relativePath the relative path
     * @param assembleAsPartOfLongerId if true, escape properly so that the produced String can become part of a longer identifier
     * @return the local external form
     */
    public String toLocalExternalForm(
            String  relativePath,
            boolean assembleAsPartOfLongerId )
    {
        return theLocalId;
    }

    /**
      * Determine hashCode.
      *
      * @return the hash code
      */
    @Override
    public final int hashCode()
    {
        return toExternalForm().hashCode();
    }

    /**
      * Determine equality of two objects.
      *
      * @param otherValue the object to test against
      * @return true if the objects are equal
      */
    @Override
    public final boolean equals(
            Object otherValue )
    {
        if( otherValue instanceof Identifier ) {
            Identifier realValue = (Identifier) otherValue;
            
            if( !toExternalForm().equals( realValue.toExternalForm() )) {
                return false;
            }
            return true;
        }
        return false;
    }

    /**
      * Obtain as string representation, for debugging.
      *
      * @return string representation of this object
      */
    @Override
    public String toString()
    {
        return "&" + toExternalForm();
    }

    /**
     * Obtain an external form for this Identifier, similar to
     * <code>java.net.URL.toExternalForm()</code>.
     *
     * @return external form of this Identifier
     */
    public String toExternalForm()
    {
        if( theLocalId != null ) {
            return theLocalId;
        } else {
            return "";
        }
    }

    /**
     * Obtain a String representation of this instance that can be shown to the user.
     *
     * @param rep the StringRepresentation
     * @param pars collects parameters that may influence the String representation
     * @throws StringifierException thrown if there was a problem when attempting to stringify
     * @return String representation
     */
    public String toStringRepresentation(
            StringRepresentation           rep,
            StringRepresentationParameters pars )
        throws
            StringifierException
    {
        MeshObject meshObject  = null;
        String     contextPath = null;
        MeshBase   meshBase    = null;

        if( pars != null ) {
            meshObject  = (MeshObject) pars.get( MeshStringRepresentationParameters.MESHOBJECT_KEY );
            contextPath = (String) pars.get(  StringRepresentationParameters.WEB_RELATIVE_CONTEXT_KEY );
            meshBase    = meshObject.getMeshBase();
        }

        boolean isDefaultMeshBase = false;
        if( meshBase != null && pars != null ) {
            isDefaultMeshBase = meshBase.equals( pars.get( MeshStringRepresentationParameters.DEFAULT_MESHBASE_KEY ));
        }
        boolean isHomeObject;
        if( meshBase != null ) {
            isHomeObject = meshObject == meshBase.getHomeObject();
        } else {
            isHomeObject = identifiesHomeObject();
        }

        String key;
        if( isDefaultMeshBase ) {
            if( isHomeObject ) {
                key = DEFAULT_MESH_BASE_HOME_ENTRY;
            } else {
                key = DEFAULT_MESH_BASE_ENTRY;
            }
        } else {
            if( isHomeObject ) {
                key = NON_DEFAULT_MESH_BASE_HOME_ENTRY;
            } else {
                key = NON_DEFAULT_MESH_BASE_ENTRY;
            }
        }

        String ret = rep.formatEntry(
                getClass(), // dispatch to the right subtype
                key,
                pars,
        /* 0 */ this,
        /* 1 */ contextPath,
        /* 2 */ meshBase );

        return ret;
    }

    /**
     * Obtain the start part of a String representation of this object that acts
     * as a link/hyperlink and can be shown to the user.
     *
     * @param rep the StringRepresentation
     * @param pars collects parameters that may influence the String representation
     * @throws StringifierException thrown if there was a problem when attempting to stringify
     * @return String representation
     */
    public String toStringRepresentationLinkStart(
            StringRepresentation           rep,
            StringRepresentationParameters pars )
        throws
            StringifierException
    {
        MeshObject meshObject          = null;
        String     contextPath         = null;
        MeshBase   meshBase            = null;
        String     additionalArguments = null;
        String     target              = null;
        String     title               = null;

        if( pars != null ) {
            meshObject  = (MeshObject) pars.get( MeshStringRepresentationParameters.MESHOBJECT_KEY );
            contextPath = (String) pars.get(  StringRepresentationParameters.WEB_RELATIVE_CONTEXT_KEY );
            meshBase    = meshObject.getMeshBase();
            target              = (String) pars.get( StringRepresentationParameters.LINK_TARGET_KEY );
            title               = (String) pars.get( StringRepresentationParameters.LINK_TITLE_KEY );
            additionalArguments = (String) pars.get( StringRepresentationParameters.HTML_URL_ADDITIONAL_ARGUMENTS );
        }

        boolean isDefaultMeshBase = false;
        if( meshBase != null && pars != null ) {
            isDefaultMeshBase = meshBase.equals( pars.get( MeshStringRepresentationParameters.DEFAULT_MESHBASE_KEY ));
        }
        boolean isHomeObject;
        if( meshBase != null ) {
            isHomeObject = meshObject == meshBase.getHomeObject();
        } else {
            isHomeObject = identifiesHomeObject();
        }

        String key;
        if( isDefaultMeshBase ) {
            if( isHomeObject ) {
                key = DEFAULT_MESH_BASE_HOME_LINK_START_ENTRY;
            } else {
                key = DEFAULT_MESH_BASE_LINK_START_ENTRY;
            }
        } else {
            if( isHomeObject ) {
                key = NON_DEFAULT_MESH_BASE_HOME_LINK_START_ENTRY;
            } else {
                key = NON_DEFAULT_MESH_BASE_LINK_START_ENTRY;
            }
        }
        if( target == null ) {
            target = "_self";
        }

        String ret = rep.formatEntry(
                getClass(), // dispatch to the right subtype
                key,
                pars,
        /* 0 */ this,
        /* 1 */ contextPath,
        /* 2 */ meshBase,
        /* 3 */ additionalArguments,
        /* 4 */ target,
        /* 5 */ title,
        /* 6 */ theAsEntered );

        return ret;
    }

    /**
     * Obtain the end part of a String representation of this object that acts
     * as a link/hyperlink and can be shown to the user.
     *
     * @param rep the StringRepresentation
     * @param pars collects parameters that may influence the String representation
     * @return String representation
     * @throws StringifierException thrown if there was a problem when attempting to stringify
     */
    public String toStringRepresentationLinkEnd(
            StringRepresentation           rep,
            StringRepresentationParameters pars )
        throws
            StringifierException
    {
        MeshObject meshObject  = null;
        String     contextPath = null;
        MeshBase   meshBase    = null;

        if( pars != null ) {
            meshObject  = (MeshObject) pars.get( MeshStringRepresentationParameters.MESHOBJECT_KEY );
            contextPath = (String) pars.get( StringRepresentationParameters.WEB_RELATIVE_CONTEXT_KEY );
            meshBase    = meshObject.getMeshBase();
        }

        boolean isDefaultMeshBase = false;
        if( meshBase != null && pars != null ) {
            isDefaultMeshBase = meshBase.equals( pars.get( MeshStringRepresentationParameters.DEFAULT_MESHBASE_KEY ));
        }
        boolean isHomeObject;
        if( meshBase != null ) {
            isHomeObject = meshObject == meshBase.getHomeObject();
        } else {
            isHomeObject = identifiesHomeObject();
        }

        String key;
        if( isDefaultMeshBase ) {
            if( isHomeObject ) {
                key = DEFAULT_MESH_BASE_HOME_LINK_END_ENTRY;
            } else {
                key = DEFAULT_MESH_BASE_LINK_END_ENTRY;
            }
        } else {
            if( isHomeObject ) {
                key = NON_DEFAULT_MESH_BASE_HOME_LINK_END_ENTRY;
            } else {
                key = NON_DEFAULT_MESH_BASE_LINK_END_ENTRY;
            }
        }

        String ret = rep.formatEntry(
                getClass(), // dispatch to the right subtype
                key,
                pars,
        /* 0 */ this,
        /* 1 */ contextPath,
        /* 2 */ meshBase,
        /* 3 */ theAsEntered );

        return ret;
    }

    /**
     * The factory that created this identifier.
     */
    protected DefaultAMeshObjectIdentifierFactory theFactory;

    /**
     * The real value for the localId.
     */
    protected String theLocalId;

    /**
     * Entry in the resource files, prefixed by the StringRepresentation's prefix.
     */
    public static final String DEFAULT_MESH_BASE_ENTRY = "DefaultMeshBaseString";

    /**
     * Entry in the resource files, prefixed by the StringRepresentation's prefix.
     */
    public static final String DEFAULT_MESH_BASE_HOME_ENTRY = "DefaultMeshBaseHomeString";

    /**
     * Entry in the resource files, prefixed by the StringRepresentation's prefix.
     */
    public static final String DEFAULT_MESH_BASE_LINK_START_ENTRY = "DefaultMeshBaseLinkStartString";

    /**
     * Entry in the resource files, prefixed by the StringRepresentation's prefix.
     */
    public static final String DEFAULT_MESH_BASE_HOME_LINK_START_ENTRY = "DefaultMeshBaseHomeLinkStartString";

    /**
     * Entry in the resource files, prefixed by the StringRepresentation's prefix.
     */
    public static final String DEFAULT_MESH_BASE_LINK_END_ENTRY = "DefaultMeshBaseLinkEndString";

    /**
     * Entry in the resource files, prefixed by the StringRepresentation's prefix.
     */
    public static final String DEFAULT_MESH_BASE_HOME_LINK_END_ENTRY = "DefaultMeshBaseHomeLinkEndString";

    /**
     * Entry in the resource files, prefixed by the StringRepresentation's prefix.
     */
    public static final String NON_DEFAULT_MESH_BASE_ENTRY = "NonDefaultMeshBaseString";

    /**
     * Entry in the resource files, prefixed by the StringRepresentation's prefix.
     */
    public static final String NON_DEFAULT_MESH_BASE_HOME_ENTRY = "NonDefaultMeshBaseHomeString";

    /**
     * Entry in the resource files, prefixed by the StringRepresentation's prefix.
     */
    public static final String NON_DEFAULT_MESH_BASE_LINK_START_ENTRY = "NonDefaultMeshBaseLinkStartString";

    /**
     * Entry in the resource files, prefixed by the StringRepresentation's prefix.
     */
    public static final String NON_DEFAULT_MESH_BASE_HOME_LINK_START_ENTRY = "NonDefaultMeshBaseHomeLinkStartString";

    /**
     * Entry in the resource files, prefixed by the StringRepresentation's prefix.
     */
    public static final String NON_DEFAULT_MESH_BASE_LINK_END_ENTRY = "NonDefaultMeshBaseLinkEndString";

    /**
     * Entry in the resource files, prefixed by the StringRepresentation's prefix.
     */
    public static final String NON_DEFAULT_MESH_BASE_HOME_LINK_END_ENTRY = "NonDefaultMeshBaseHomeLinkEndString";
}
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
// Copyright 1998-2009 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.jee.viewlet;

import org.infogrid.util.context.Context;
import org.infogrid.viewlet.AbstractViewedMeshObjects;
import org.infogrid.viewlet.CannotViewException;
import org.infogrid.viewlet.DefaultViewedMeshObjects;
import org.infogrid.viewlet.MeshObjectsToView;
import org.infogrid.viewlet.Viewlet;
import org.infogrid.viewlet.ViewletFactoryChoice;

/**
 * A Viewlet Class that can impersonate any other Viewlet, as long as the Viewlet
 * does not override or add any methods.
 */
public class DefaultJspViewlet
        extends
            AbstractJeeViewlet
{
    /**
     * Factory method.
     *
     * @param pseudoClassName the fully-qualified class name of the class that will be impersonated
     * @param c the application context
     * @return the created Viewlet
     */
    public static DefaultJspViewlet create(
            String  pseudoClassName,
            Context c )
    {
        DefaultViewedMeshObjects viewed = new DefaultViewedMeshObjects();
        DefaultJspViewlet         ret    = new DefaultJspViewlet( pseudoClassName, viewed, c );

        viewed.setViewlet( ret );

        return ret;
    }

    /**
     * Factory method for a ViewletFactoryChoice that instantiates this Viewlet.
     *
     * @param pseudoClassName the fully-qualified class name of the class that will be impersonated
     * @param matchQuality the match quality
     * @return the ViewletFactoryChoice
     */
    public static ViewletFactoryChoice choice(
            final String pseudoClassName,
            double       matchQuality )
    {
        return new DefaultJspViewletFactoryChoice( pseudoClassName, matchQuality ) {
                public Viewlet instantiateViewlet(
                        MeshObjectsToView        toView,
                        Context                  c )
                    throws
                        CannotViewException
                {
                    return create( pseudoClassName, c );
                }
        };
    }

    /**
     * Constructor. This is protected: use factory method or subclass.
     *
     * @param pseudoClassName the fully-qualified class name of the class that will be impersonated
     * @param viewed the AbstractViewedMeshObjects implementation to use
     * @param c the application context
     */
    protected DefaultJspViewlet(
            String                    pseudoClassName,
            AbstractViewedMeshObjects viewed,
            Context                   c )
    {
        super( viewed, c );

        thePseudoClassName = pseudoClassName;
    }

    /**
     * Obtain the Html class name for this Viewlet. By default, it is the Java class
     * name, having replaced all periods with hyphens.
     * 
     * @return the HTML class name
     */
    @Override
    public String getHtmlClass()
    {
        String ret = thePseudoClassName;

        ret = ret.replaceAll( "\\.", "-" );
        
        return ret;
    }

    /**
     * Obtain the path to the Servlet for this JeeViewlet.
     * 
     * @return the Servlet path
     */
    @Override
    public String getServletPath()
    {
        String ret = constructDefaultDispatcherUrl( thePseudoClassName );
        return ret;
    }

    /**
     * Obtain the computable name of the Viewlet.
     * 
     * @return the Viewet's name
     */
    @Override
    public String getName()
    {
        return thePseudoClassName;
    }

    /**
     * The name of the Viewlet class this would have been if it had been created as a separate
     * Viewlet class.
     */
    protected String thePseudoClassName;
}
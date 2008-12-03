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
// Copyright 1998-2008 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.jee.net.testapp;

import java.net.URISyntaxException;
import org.infogrid.jee.rest.net.local.defaultapp.m.AbstractMNetLocalRestfulAppInitializationFilter;
import org.infogrid.mesh.MeshObjectIdentifierNotUniqueException;
import org.infogrid.mesh.NotPermittedException;
import org.infogrid.mesh.RelatedAlreadyException;
import org.infogrid.mesh.net.NetMeshObject;
import org.infogrid.meshbase.net.DefaultNetMeshBaseIdentifierFactory;
import org.infogrid.meshbase.net.NetMeshBase;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.NetMeshBaseIdentifierFactory;
import org.infogrid.meshbase.net.NetMeshBaseLifecycleManager;
import org.infogrid.meshbase.net.NetMeshObjectAccessException;
import org.infogrid.meshbase.net.NetMeshObjectIdentifierFactory;
import org.infogrid.meshbase.transaction.Transaction;
import org.infogrid.meshbase.transaction.TransactionException;
import org.infogrid.probe.ProbeDirectory;
import org.infogrid.probe.m.MProbeDirectory;
import org.infogrid.util.context.Context;
import org.infogrid.util.logging.Log;
import org.infogrid.viewlet.ViewletFactory;

/**
 * Initializes application-level functionality
 */
public class TestAppInitializationFilter
        extends
            AbstractMNetLocalRestfulAppInitializationFilter
{
    private Log log = Log.getLogInstance( TestAppInitializationFilter.class );
    
    /**
     * Constructor for subclasses only, use factory method.
     */
    public TestAppInitializationFilter()
    {
        // nothing right now
    }

    /**
     * Overridable method to create the NetMeshBaseIdentifierFactory appropriate for this
     * application.
     *
     * @return the created NetMeshBaseIdentifierFactory
     */
    @Override
    protected NetMeshBaseIdentifierFactory createNetMeshBaseIdentifierFactory()
    {
        NetMeshBaseIdentifierFactory ret = DefaultNetMeshBaseIdentifierFactory.create(
                new DefaultNetMeshBaseIdentifierFactory.Protocol [] {
                        new DefaultNetMeshBaseIdentifierFactory.Protocol( "http",   true ),
                        new DefaultNetMeshBaseIdentifierFactory.Protocol( "https",  true ),
                        new DefaultNetMeshBaseIdentifierFactory.Protocol( "custom", false ),
        });
        return ret;
    }

    /**
     * Overridable method to create and populate a ProbeDirectory apporpriate for this
     * application.
     *
     * @param meshBaseIdentifierFactory the NetMeshBaseIdentifierFactory to us
     * @return the created and populated ProbeDirectory
     * @throws URISyntaxException thrown if an identifier could not be parsed
     */
    @Override
    protected ProbeDirectory createAndPopulateProbeDirectory(
            NetMeshBaseIdentifierFactory meshBaseIdentifierFactory )
        throws
            URISyntaxException
    {
        MProbeDirectory ret = MProbeDirectory.create();
        toAccess = new NetMeshBaseIdentifier[] {
            meshBaseIdentifierFactory.fromExternalForm( "custom://example.com/" ),
            meshBaseIdentifierFactory.fromExternalForm( "custom://example.org/a/?foo=bar&argl=brgl" ),
        };

        for( NetMeshBaseIdentifier current : toAccess ) {
            ret.addExactUrlMatch( new ProbeDirectory.ExactMatchDescriptor( current.toExternalForm(), TestProbe1.class ));
        }
        return ret;
    }

    /**
     * Initialize the initial content of the NetMeshBase.
     *
     * @param mb the NetMeshBase to initialize
     */
    @Override
    protected void populateNetMeshBase(
            NetMeshBase mb )
    {
        Transaction tx = null;
        try {
            NetMeshBaseLifecycleManager    life = mb.getMeshBaseLifecycleManager();
            NetMeshObjectIdentifierFactory fact = mb.getMeshObjectIdentifierFactory();

            NetMeshObject [] resolved = new NetMeshObject[ toAccess.length ];
            for( int i=0 ; i<resolved.length ; ++i ) {
                resolved[i] = mb.accessLocally( toAccess[i] );
                resolved[i].traverseToNeighborMeshObjects(); // get the related objects into the main MeshBase, too
            }
        
            tx = mb.createTransactionNow();
            
            NetMeshObject a = life.createMeshObject( fact.fromExternalForm( "#aaaa" )); // make sure there are long enough to pass the Regex filter
            NetMeshObject b = life.createMeshObject( fact.fromExternalForm( "#bbbb" ));
            NetMeshObject c = life.createMeshObject( fact.fromExternalForm( "#cccc" ));
            
            a.relate( b );
            c.relate( mb.getHomeObject() );
            
            for( int i=0 ; i<resolved.length ; ++i ) {
                if( resolved[i] != null ) {
                    c.relate( resolved[i] );
                }
            }
            
        } catch( TransactionException ex ) {
            log.error( ex );
        } catch( MeshObjectIdentifierNotUniqueException ex ) {
            log.error( ex );
        } catch( NetMeshObjectAccessException ex ) {
            log.error( ex );
        } catch( RelatedAlreadyException ex ) {
            log.error( ex );
        } catch( NotPermittedException ex ) {
            log.error( ex );
        } catch( URISyntaxException ex ) {
            log.error( ex );
        } finally {
            if( tx != null ) {
                tx.commitTransaction();
            }
        }
    }
    
    /**
     * Initialize context objects.
     *
     * @param context the Context
     */
    @Override
    protected void initializeContextObjects(
            Context context )
    {
        super.initializeContextObjects( context );
        
        ViewletFactory vlFact = new TestAppViewletFactory();
        context.addContextObject( vlFact );
    }

    /**
     * THe identifiers of the Probe home objects to be accessLocally'd.
     */
    protected NetMeshBaseIdentifier [] toAccess;
}

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
// Copyright 1998-2015 by Johannes Ernst
// All rights reserved.
//

package org.infogrid.probe.store.test;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ScheduledExecutorService;
import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.net.NetMeshObject;
import org.infogrid.meshbase.net.CoherenceSpecification;
import org.infogrid.meshbase.net.DefaultNetMeshObjectAccessSpecificationFactory;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.local.store.IterableLocalNetStoreMeshBase;
import org.infogrid.meshbase.net.local.store.LocalNetStoreMeshBase;
import org.infogrid.model.Probe.ProbeSubjectArea;
import org.infogrid.model.primitives.IntegerValue;
import org.infogrid.store.prefixing.IterablePrefixingStore;
import org.infogrid.testharness.AbstractTest;
import org.infogrid.util.logging.Log;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * Tests that shadows resume running probes at the right time after a reboot.
 */
@RunWith(Parameterized.class)
public class StoreShadowMeshBaseTest9
        extends
            AbstractStoreProbeTest
{
    /**
     * Test parameters.
     * 
     * @return test parameters
     */
    @Parameterized.Parameters
    public static Collection<Object[]> parameters()
    {
        return Arrays.asList( new Object[][] {
                {
                    AbstractTest.fileSystemFileName( StoreShadowMeshBaseTest9.class, "StoreShadowMeshBaseTest9.xml" )
                }
        });
    }

    /**
     * Run the test.
     *
     * @throws Exception all sorts of things may go wrong in tests
     */
    @Test
    public void run()
        throws
            Exception
    {
        //
        
        log.info( "Creating Stores" );

        IterablePrefixingStore theMeshStore        = IterablePrefixingStore.create( "Mesh",        theSqlStore );
        IterablePrefixingStore theProxyStore       = IterablePrefixingStore.create( "Proxy",       theSqlStore );
        IterablePrefixingStore theShadowStore      = IterablePrefixingStore.create( "Shadow",      theSqlStore );
        IterablePrefixingStore theShadowProxyStore = IterablePrefixingStore.create( "ShadowProxy", theSqlStore );
        
        //
        
        log.info( "Creating MeshBase" );
        
        NetMeshBaseIdentifier             baseIdentifier     = theMeshBaseIdentifierFactory.fromExternalForm( "http://here.local/" );
        
        IterableLocalNetStoreMeshBase base = IterableLocalNetStoreMeshBase.create(
                baseIdentifier,
                DefaultNetMeshObjectAccessSpecificationFactory.create(
                        baseIdentifier,
                        theMeshBaseIdentifierFactory ),
                theModelBase,
                null,
                theMeshStore,
                theProxyStore,
                theShadowStore,
                theShadowProxyStore,
                theProbeDirectory,
                exec,
                true,
                rootContext );
        
        checkEquals( base.getShadowMeshBases().size(), 0, "Wrong number of shadows" );
        
        startClock();
        
        //
        
        log.info( "Doing AccessLocally" );
        
        NetMeshObject found = base.accessLocally(theTestFile1Id, new CoherenceSpecification.Periodic( 10000L ));
        
        checkObject( found, "Object not found" );
        checkCondition( found.isBlessedBy( ProbeSubjectArea.PROBEUPDATESPECIFICATION ), "Not blessed correctly" );
        checkEquals( found.getPropertyValue( ProbeSubjectArea.PROBEUPDATESPECIFICATION_PROBERUNCOUNTER ), IntegerValue.create( 1 ), "Wrong ProbeRunCounter" );

        //
        
        log.info( "Shutting down the MeshBase" );

        WeakReference<LocalNetStoreMeshBase> baseRef  = new WeakReference<LocalNetStoreMeshBase>( base );
        WeakReference<MeshObject>            foundRef = new WeakReference<MeshObject>( found );

        found         = null;
        base.die();
        base          = null;

        sleepUntilIsGone( baseRef, 20000L, "base still here" );
        
        //
        
        log.info( "Checking that MeshBase is gone" );
        
        checkCondition( baseRef.get()          == null, "MeshBase still here" );
        checkCondition( foundRef.get()         == null, "MeshObject still here" );

        Thread.sleep( 3000L );

        //
        
        log.info( "Re-creating Meshbase" );

        IterableLocalNetStoreMeshBase base2 = IterableLocalNetStoreMeshBase.create(
                baseIdentifier,
                DefaultNetMeshObjectAccessSpecificationFactory.create(
                        baseIdentifier,
                        theMeshBaseIdentifierFactory ),
                theModelBase,
                null,
                theMeshStore,
                theProxyStore,
                theShadowStore,
                theShadowProxyStore,
                theProbeDirectory,
                exec,
                true,
                rootContext );
        
        NetMeshObject found2 = base2.accessLocally( theTestFile1Id, (CoherenceSpecification) null );

        checkObject( found2, "Object not found" );

        checkCondition( found2.isBlessedBy( ProbeSubjectArea.PROBEUPDATESPECIFICATION ), "Not blessed correctly" );
        checkEquals( found2.getPropertyValue( ProbeSubjectArea.PROBEUPDATESPECIFICATION_PROBERUNCOUNTER ), IntegerValue.create( 1 ), "Wrong ProbeRunCounter" );

        //
        
        log.info( "Waiting a bit for second Probe run and checking again" );
        
        sleepUntil( 14000L );
        checkEquals( found2.getPropertyValue( ProbeSubjectArea.PROBEUPDATESPECIFICATION_PROBERUNCOUNTER ), IntegerValue.create( 2 ), "Wrong ProbeRunCounter" );
    }
        
    /**
     * Constructor with parameters.
     * 
     * @param testFile1 filename of test
     */
    public StoreShadowMeshBaseTest9(
            String testFile1 )
    {
        theTestFile1 = testFile1;
    }

    /**
     * Setup.
     * 
     * @throws Exception all sorts of things may go wrong in tests
     */
    @Before
    @Override
    public void setup()
        throws
            Exception
    {
        super.setup();

        collectGarbage();

        theTestFile1Id  = theMeshBaseIdentifierFactory.obtain( new File( theTestFile1 ) );

        //
        
        log.info( "Deleting old database and creating new database" );
        
        theSqlStore.initializeHard();
        
        exec = createThreadPool( 1 );
    }

    /**
     * Clean up after the test.
     */
    @After
    public void cleanup()
    {
        exec.shutdown();
        exec = null;
    }

    // Our Logger
    private static Log log = Log.getLogInstance( StoreShadowMeshBaseTest9.class);

    /**
     * Our ThreadPool.
     */
    protected ScheduledExecutorService exec;

    /**
     * File name of the first test file.
     */
    protected String theTestFile1;

    /**
     * The NetworkIdentifer of the first test file.
     */
    protected NetMeshBaseIdentifier theTestFile1Id;
}

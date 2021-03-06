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

package org.infogrid.probe.test.forwardreference;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import org.diet4j.core.ModuleException;
import org.infogrid.mesh.EntityBlessedAlreadyException;
import org.infogrid.mesh.EntityNotBlessedException;
import org.infogrid.mesh.IllegalPropertyTypeException;
import org.infogrid.mesh.IllegalPropertyValueException;
import org.infogrid.mesh.IsAbstractException;
import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.MeshObjectIdentifierNotUniqueException;
import org.infogrid.mesh.NotPermittedException;
import org.infogrid.mesh.NotRelatedException;
import org.infogrid.mesh.RelatedAlreadyException;
import org.infogrid.mesh.net.NetMeshObject;
import org.infogrid.meshbase.net.CoherenceSpecification;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.transaction.TransactionException;
import org.infogrid.model.Test.TestSubjectArea;
import org.infogrid.model.primitives.EntityType;
import org.infogrid.model.primitives.StringValue;
import org.infogrid.probe.ApiProbe;
import org.infogrid.probe.ProbeDirectory;
import org.infogrid.probe.ProbeException;
import org.infogrid.probe.StagingMeshBase;
import org.infogrid.probe.StagingMeshBaseLifecycleManager;
import org.infogrid.testharness.util.IteratorElementCounter;
import org.infogrid.util.logging.Log;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * Tests resolving a ForwardReference that is already resolved.
 */
@RunWith(Parameterized.class)
public class ForwardReferenceTest7
        extends
            AbstractForwardReferenceTest
{
    /**
     * Run the test.
     *
     * @throws Exception all sorts of things can go wrong during a test
     */
    @Test
    public void run()
        throws
            Exception
    {
        log.info( "accessing inner probe" );

        MeshObject already = base.accessLocally(
                INNER_URL,
                base.getMeshObjectIdentifierFactory().fromExternalForm( INNER_URL, INNER_NON_HOME_LOCAL_IDENTIFIER ),
                theCoherence );

        checkObject( already, "already not found" );
        checkEquals( IteratorElementCounter.countIteratorElements( base.proxies()), 1, "wrong number of proxies in main NetMeshBase" );
        // if wait, this is the proxy to the old Shadow, if !wait, the proxy to the new Shadow

        //
        
        log.info( "accessing outer probe" );

        MeshObject abc = base.accessLocally( OUTER_URL, CoherenceSpecification.ONE_TIME_ONLY );

        checkObject( abc, "abc not found" );
        checkEquals( IteratorElementCounter.countIteratorElements( base.proxies()), 2, "wrong number of proxies in main NetMeshBase" );

        //

        log.info( "Finding ForwardReference" );

        NetMeshObject fwdReference = (NetMeshObject) abc.traverseToNeighborMeshObjects().getSingleMember();
        checkObject( fwdReference, "fwdReference not found" );
        if( theWait ) {
            checkIdentity( already, fwdReference, "Not the same instance" );

            checkEquals( fwdReference.getPropertyValue( TestSubjectArea.A_X ), StringValue.create( "resolved" ), "ForwardReference was not successfully resolved: " + fwdReference.getIdentifier().toExternalForm() );

            checkEquals(    fwdReference.getAllProxies().length, 1, "Wrong number of proxies on forward reference" );
            checkCondition( fwdReference.getAllProxies()[0].getPartnerMeshBaseIdentifier().equals( INNER_URL ), "Wrong proxy on forward reference" );
            checkCondition( !fwdReference.isBlessedBy( TestSubjectArea.A,  false ), "Blessed still by old type" );
            checkCondition(  fwdReference.isBlessedBy( TestSubjectArea.AA, false ), "Not blessed by the right type (AA)" );
            checkCondition(  fwdReference.isBlessedBy( TestSubjectArea.B,  false ), "Not blessed by the right type (B)" );

            // wait some

            Thread.sleep( PINGPONG_ROUNDTRIP_DURATION * 3L );
        }
        NetMeshObject fwdReference2 = (NetMeshObject) abc.traverseToNeighborMeshObjects().getSingleMember();
        checkObject( fwdReference2, "fwdReference2 not found" );
        checkIdentity( already, fwdReference2, "Not the same instance" );

        checkEquals( fwdReference.getPropertyValue( TestSubjectArea.A_X ), StringValue.create( "resolved" ), "ForwardReference was not successfully resolved: " + fwdReference.getIdentifier().toExternalForm() );

        checkEquals(    fwdReference2.getAllProxies().length, 1, "Wrong number of proxies on forward reference" );
        checkCondition( fwdReference2.getAllProxies()[0].getPartnerMeshBaseIdentifier().equals( INNER_URL ), "Wrong proxy on forward reference" );
        checkCondition( !fwdReference2.isBlessedBy( TestSubjectArea.A,  false ), "Blessed still by old type" );
        checkCondition(  fwdReference2.isBlessedBy( TestSubjectArea.AA, false ), "Not blessed by the right type (AA)" );
        checkCondition(  fwdReference2.isBlessedBy( TestSubjectArea.B,  false ), "Not blessed by the right type (B)" );
    }

    /**
     * Constructor with parameters.
     * 
     * @param wait if true, wait for the Probe
     * @throws Exception all sorts of things can go wrong during a test
     */
    public ForwardReferenceTest7(
            boolean wait )
        throws
            Exception
    {
        super( wait );
    }

    /**
     * Setup.
     * 
     * @throws Exception all sorts of things can go wrong during a test
     */
    @Before
    @Override
    public void setup()
        throws
            Exception
    {
        super.setup();

        theProbeDirectory.addExactUrlMatch( new ProbeDirectory.ExactMatchDescriptor(
                OUTER_URL.toExternalForm(),
                OuterTestApiProbe.class ));

        theProbeDirectory.addExactUrlMatch( new ProbeDirectory.ExactMatchDescriptor(
                INNER_URL.toExternalForm(),
                InnerTestApiProbe.class ));
    }

    // Our Logger
    private static Log log = Log.getLogInstance( ForwardReferenceTest5.class);

    /**
     * URL for the outer Probe.
     */
    public static final NetMeshBaseIdentifier OUTER_URL;
    static {
        NetMeshBaseIdentifier temp = null;
        try {
            temp = theMeshBaseIdentifierFactory.fromExternalForm( PROTOCOL_NAME + "://some.example.com/outer" );
            // temp = NetMeshBaseIdentifier.create( "=foo" );

        } catch( ParseException ex ) {
            log.error( ex );
        }
        OUTER_URL = temp;
    }

    /**
     * URL for the inner Probe.
     */
    public static final NetMeshBaseIdentifier INNER_URL;
    static {
        NetMeshBaseIdentifier temp = null;
        try {
            temp = theMeshBaseIdentifierFactory.fromExternalForm( PROTOCOL_NAME + "://some.example.com/inner" );

        } catch( ParseException ex ) {
            log.error( ex );
        }
        INNER_URL = temp;
    }

    /**
     * Identifier of the inner MeshObject to reference.
     */
    public static final String INNER_NON_HOME_LOCAL_IDENTIFIER = "#non-home";

    /**
     * The Probe to the outer data feed.
     */
    public static class OuterTestApiProbe
            implements
                ApiProbe
    {
        public void readFromApi(
                NetMeshBaseIdentifier  networkId,
                CoherenceSpecification coherence,
                StagingMeshBase        mb )
            throws
                IsAbstractException,
                EntityBlessedAlreadyException,
                EntityNotBlessedException,
                RelatedAlreadyException,
                NotRelatedException,
                MeshObjectIdentifierNotUniqueException,
                IllegalPropertyTypeException,
                IllegalPropertyValueException,
                TransactionException,
                NotPermittedException,
                ProbeException,
                IOException,
                ModuleException,
                URISyntaxException,
                ParseException
        {
            StagingMeshBaseLifecycleManager life = mb.getMeshBaseLifecycleManager();

            mb.getHomeObject().bless( TestSubjectArea.AA );

            MeshObject fwdRef = life.createForwardReference(
                    INNER_URL,
                    mb.getMeshObjectIdentifierFactory().fromExternalForm( INNER_URL, INNER_NON_HOME_LOCAL_IDENTIFIER ),
                    TestSubjectArea.A );
            fwdRef.setPropertyValue( TestSubjectArea.A_X, StringValue.create( "forwardreference" ));

            mb.getHomeObject().relateAndBless( TestSubjectArea.AR1A.getSource(), fwdRef );
        }
    }

    /**
     * The Probe to the inner data feed.
     */
    public static class InnerTestApiProbe
            implements
                ApiProbe
    {
        public void readFromApi(
                NetMeshBaseIdentifier  networkId,
                CoherenceSpecification coherence,
                StagingMeshBase        mb )
            throws
                IsAbstractException,
                EntityBlessedAlreadyException,
                EntityNotBlessedException,
                RelatedAlreadyException,
                NotRelatedException,
                MeshObjectIdentifierNotUniqueException,
                IllegalPropertyTypeException,
                IllegalPropertyValueException,
                TransactionException,
                NotPermittedException,
                ProbeException,
                IOException,
                ModuleException,
                URISyntaxException,
                ParseException
        {
            MeshObject home = mb.getHomeObject();

            MeshObject nonHome = mb.getMeshBaseLifecycleManager().createMeshObject(
                    mb.getMeshObjectIdentifierFactory().fromExternalForm( INNER_NON_HOME_LOCAL_IDENTIFIER ),
                    new EntityType [] {
                            TestSubjectArea.AA,
                            TestSubjectArea.B,
                    });

            nonHome.setPropertyValue( TestSubjectArea.A_X, StringValue.create( "resolved" ) );
        }
    }
}

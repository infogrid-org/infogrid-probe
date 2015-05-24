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

package org.infogrid.probe.feeds.test.rss;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import org.infogrid.mesh.MeshObject;
import org.infogrid.meshbase.net.CoherenceSpecification;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.model.Feeds.FeedsSubjectArea;
import org.infogrid.model.primitives.EntityType;
import org.infogrid.model.Probe.ProbeSubjectArea;
import org.infogrid.probe.ProbeDirectory;
import org.infogrid.probe.feeds.test.AbstractFeedTest;
import org.infogrid.probe.feeds.rss.RssProbe;
import org.infogrid.probe.shadow.ShadowMeshBase;
import org.infogrid.util.logging.Log;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * Tests the RSS Probe with RssTest1.xml.
 */
@RunWith(Parameterized.class)
public class RssTest1
        extends
            AbstractFeedTest
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
                 { "src/test/resources/org/infogrid/probe/feeds/test/rss/RssTest1.xml", 2 }
        });
    }

    /**
     * Run the test.
     *
     * @throws Exception all sorts of things may go wrong during a test
     */
    @Test
    public void run()
        throws
            Exception
    {
        log.info( "accessing test file with meshBase" );
        
        ShadowMeshBase meshBase1 = theProbeManager1.obtainFor(theTestFileId, CoherenceSpecification.ONE_TIME_ONLY );

        checkObject( meshBase1, "could not find meshBase1" );
        checkCondition( meshBase1.size() > 1, "meshBase1 is empty" );
        dumpMeshBase( meshBase1, "meshBase1", log );

        MeshObject home1 = meshBase1.getHomeObject();

        checkEqualsOutOfSequence(
                home1.getTypes(),
                new EntityType[] {
                        FeedsSubjectArea.RSSFEED,
                        ProbeSubjectArea.ONETIMEONLYPROBEUPDATESPECIFICATION },
                "home object has wrong type" );

        checkEquals( home1.getPropertyValue( FeedsSubjectArea.FEED_TITLE ),       FeedsSubjectArea.FEED_TITLE_type.createBlobValue( "This title is plain text", "text/plain" ), "wrong feed title" );
        checkEquals( home1.getPropertyValue( FeedsSubjectArea.FEED_DESCRIPTION ), null,                                                                                         "wrong feed description" );

        checkEquals( home1.traverseToNeighborMeshObjects().size(), 1, "wrong number of neighbors" );
        
        MeshObject entry11 = home1.traverse( FeedsSubjectArea.FEED_CONTAINS_FEEDITEM.getSource() ).getSingleMember();
        checkObject( entry11, "no entry object found" );
        
        checkEqualsOutOfSequence(
                entry11.getTypes(),
                new EntityType[] {
                        FeedsSubjectArea.RSSFEEDITEM },
                "wrong entry type" );
        
        checkEquals( entry11.getPropertyValue( FeedsSubjectArea.FEEDITEM_TITLE ), FeedsSubjectArea.FEEDITEM_TITLE_type.createBlobValue( "This entry title 1 is plain text", "text/plain" ), "wrong entry title" );

        checkEquals( meshBase1.size(), theExpectedNumberMeshObjects, "Wrong number of MeshObjects found" );
    }

    /**
     * Constructor that takes parameters.
     * 
     * @param fileName the XRD file to read
     * @param expectedNumberMeshObjects the number of MeshObjects in the file
     * @throws Exception all sorts of things may happen during a test
     */
    public RssTest1(
            String fileName,
            int    expectedNumberMeshObjects )
        throws
            Exception
    {
        theTestFileId = theMeshBaseIdentifierFactory.obtain( new File( fileName ));

        theExpectedNumberMeshObjects = expectedNumberMeshObjects;
    }
    
    /**
     * Setup.
     * 
     * @throws Exception all sorts of things may happen during a test
     */
    @Before
    @Override
    public void setup()
        throws
            Exception
    {
        super.setup();

        theProbeDirectory.addXmlDomProbe( new ProbeDirectory.XmlDomProbeDescriptor( null, null, "rss", RssProbe.class ));
    }

    /**
     * The expected number of MeshObjects in the test file.
     */
    protected int theExpectedNumberMeshObjects;

    /**
     * The NetworkIdentifer of the first test file.
     */
    protected NetMeshBaseIdentifier theTestFileId;

    // Our Logger
    private static Log log = Log.getLogInstance( RssTest1.class);
}

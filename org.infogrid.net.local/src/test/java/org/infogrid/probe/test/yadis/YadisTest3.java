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

package org.infogrid.probe.test.yadis;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import org.infogrid.httpd.HttpEntity;
import org.infogrid.httpd.HttpEntityResponse;
import org.infogrid.httpd.HttpErrorResponse;
import org.infogrid.httpd.HttpRequest;
import org.infogrid.httpd.HttpResponse;
import org.infogrid.httpd.HttpResponseFactory;
import org.infogrid.mesh.net.NetMeshObject;
import org.infogrid.meshbase.net.CoherenceSpecification;
import org.infogrid.meshbase.net.local.LocalNetMeshBase;
import org.infogrid.probe.shadow.ShadowMeshBase;
import org.infogrid.util.logging.Log;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * Tests XRDS discovery via HTTP-equiv.
 */
@RunWith(Parameterized.class)
public class YadisTest3
        extends
            AbstractYadisDiscoveryTest
{
    /**
     * Run one test scenario.
     *
     * @param mb the NetMeshBase to use for this scenario
     * @param mode the mode
     * @throws Exception all sorts of things may happen during a test
     */
    protected void run(
            LocalNetMeshBase mb,
            boolean          mode )
        throws
            Exception
    {
        log.info( "Accessing test data source (1) - " + mode );

        theWithYadis = mode;

        NetMeshObject  shadowHome = mb.accessLocally( theIdentityIdentifier, CoherenceSpecification.ONE_TIME_ONLY );
        ShadowMeshBase shadow     = mb.getShadowMeshBaseFor( theIdentityIdentifier );

        if( mode ) {
            checkYadisResultsIndirect( shadowHome, 3 );
        } else {
            checkNoYadisResults( shadowHome );
        }

        //

        log.info(  "Accessing test data source (2) - " + mode );

        theWithYadis = !mode;
        shadow.doUpdateNow();
        sleepFor( PINGPONG_ROUNDTRIP_DURATION );

        if( !mode ) {
            checkYadisResultsIndirect( shadowHome, 3 );
        } else {
            checkNoYadisResults( shadowHome );
        }

        //

        log.info( "Accessing test data source (3) - " + mode );

        theWithYadis = mode;
        shadow.doUpdateNow();
        sleepFor( PINGPONG_ROUNDTRIP_DURATION );

        if( mode ) {
            checkYadisResultsIndirect( shadowHome, 3 );
        } else {
            checkNoYadisResults( shadowHome );
        }
    }

    /**
     * Setup.
     * 
     * @throws IOException thrown if the HttpServer could not be started
     */
    @Before
    public void setup()
        throws
            IOException
    {
        super.setup();

        theServer.setResponseFactory( new MyResponseFactory( theDelay ));
    }

    /**
     * Constructor with parameters.
     * 
     * @param delay the web server time delay
     * @throws Exception all sorts of things may happen during a test
     */
    public YadisTest3(
            long delay )
        throws
            Exception
    {
        theDelay = delay;
    }

    /**
     * The Web server delay.
     */
    protected long theDelay;

    // Our Logger
    private static Log log = Log.getLogInstance( YadisTest3.class );

    /**
      * A HttpResponseFactory that acts as the RelyingParty.
      */
    static class MyResponseFactory
        implements
            HttpResponseFactory
    {
        /**
         * Constructor.
         *
         * @param delay the delay by the web server
         */
        public MyResponseFactory(
                long delay )
        {
            theDelay = delay;
        }

        /**
          * Factory method for a HttpResponse.
          *
          * @param request the HttpRequest for which we create a HttpResponse
          * @return the created HttpResponse
          */
        public HttpResponse createResponse(
                HttpRequest request )
        {
            HttpResponse ret;
            if( "GET".equals( request.getMethod() ) && ( "/" + IDENTITY_LOCAL_IDENTIFIER ).equals( request.getRelativeBaseUri() )) {
                HttpEntity entity = new HttpEntity() {
                        public boolean canRead() {
                            return true;
                        }
                        public InputStream getAsStream() {
                            try {
                                if( theWithYadis ) {
                                    return new ByteArrayInputStream( HTML_WITH_EQUIV.getBytes( "UTF-8" ));
                                } else {
                                    return new ByteArrayInputStream( HTML.getBytes( "UTF-8" ));
                                }
                            } catch( UnsupportedEncodingException ex ) {
                                log.error( ex );
                                return null;
                            }
                        }
                        public String getMime() {
                            return "text/html";
                        }
                };                
                ret = HttpEntityResponse.create( request, true, entity );

            } else if( "GET".equals( request.getMethod() ) && ( "/" + XRDS_LOCAL_IDENTIFIER ).equals( request.getRelativeBaseUri() )) {
                HttpEntity entity = new HttpEntity() {
                        public boolean canRead() {
                            return true;
                        }
                        public InputStream getAsStream() {
                            try {
                                return new ByteArrayInputStream( XRDS.getBytes( "UTF-8" ));
                            } catch( UnsupportedEncodingException ex ) {
                                log.error( ex );
                                return null;
                            }
                        }
                        public String getMime() {
                            return "application/xrds+xml";
                        }
                };                
                ret = HttpEntityResponse.create( request, true, entity );

            } else {
                ret = HttpErrorResponse.create( request, "500", null );
            }

            if( theDelay > 0L ) {
                try {
                    Thread.sleep( theDelay );
                } catch( Throwable t ) {
                    log.error( t );
                }
            }
            return ret;
        }

        /**
         * Captures the slowness of a web server.
         */
        protected long theDelay;
    }
}

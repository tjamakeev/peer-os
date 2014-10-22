package org.safehaus.subutai.plugin.lucene.impl.handler;

/* TODO Rewrite tests
import org.junit.Ignore;
import org.junit.Test;
import org.safehaus.subutai.api.lucene.Config;
import org.safehaus.subutai.impl.lucene.LuceneImpl;
import org.safehaus.subutai.impl.lucene.handler.mock.LuceneImplMock;
import org.safehaus.subutai.product.common.test.unit.mock.CommonMockBuilder;
import org.safehaus.subutai.shared.operation.AbstractOperationHandler;
import org.safehaus.subutai.shared.operation.ProductOperationState;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;


public class InstallOperationHandlerTest {

    @Test( expected = NullPointerException.class )
    public void testWithNullConfig() {
        new LuceneImplMock().installCluster( null );
    }


    @Test
    public void testWithMalformedConfiguration() {
        Config      config = new Config();
        config.setClusterName( "test" );
        AbstractOperationHandler operationHandler = new InstallOperationHandler( new LuceneImplMock(), config );

        operationHandler.run();

        assertTrue( operationHandler.getTrackerOperation().getLog().contains( "Malformed configuration" ) );
        assertEquals( operationHandler.getTrackerOperation().getState(), ProductOperationState.FAILED );
    }


    @Test
    @Ignore
    public void testWithExistingCluster() {
        Config config = new Config();
        config.setClusterName( "test-cluster" );
        config.getNodes().add( CommonMockBuilder.createAgent() );

        LuceneImpl impl = new LuceneImplMock().setClusterConfig( new Config() );
        AbstractOperationHandler operationHandler = new InstallOperationHandler( impl, config );

        operationHandler.run();

        assertTrue( operationHandler.getTrackerOperation().getLog().contains( "test-cluster" ) );
        assertTrue( operationHandler.getTrackerOperation().getLog().contains( "already exists" ) );
        assertEquals( operationHandler.getTrackerOperation().getState(), ProductOperationState.FAILED );
    }
}
*/

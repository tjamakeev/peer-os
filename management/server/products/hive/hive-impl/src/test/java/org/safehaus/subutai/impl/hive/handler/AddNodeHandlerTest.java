package org.safehaus.subutai.impl.hive.handler;

import org.junit.*;
import org.safehaus.subutai.api.hive.Config;
import org.safehaus.subutai.impl.hive.handler.mock.HiveImplMock;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.shared.operation.ProductOperationState;

public class AddNodeHandlerTest {

    private HiveImplMock mock;
    private AbstractHandler handler;

    @Before
    public void setUp() {
        mock = new HiveImplMock();
        handler = new AddNodeHandler(mock, "test-cluster", "test-host");
    }

    @Test
    public void testWithoutConfig() {
        handler.run();

        ProductOperation po = handler.getProductOperation();
        Assert.assertTrue(po.getLog().toLowerCase().contains("not exist"));
        Assert.assertEquals(po.getState(), ProductOperationState.FAILED);
    }

    @Test
    public void testNotConnected() {
        mock.setConfig(new Config());
        handler.run();

        ProductOperation po = handler.getProductOperation();
        Assert.assertTrue(po.getLog().toLowerCase().contains("not connected"));
        Assert.assertEquals(po.getState(), ProductOperationState.FAILED);
    }

}

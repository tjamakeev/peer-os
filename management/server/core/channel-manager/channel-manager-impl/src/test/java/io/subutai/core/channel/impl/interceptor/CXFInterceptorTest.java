package io.subutai.core.channel.impl.interceptor;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import io.subutai.core.channel.impl.ChannelManagerImpl;
import io.subutai.core.channel.impl.interceptor.CXFInInterceptor;

import org.apache.cxf.message.Message;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class CXFInterceptorTest
{
    private CXFInInterceptor cxfInterceptor;


    @Mock
    ChannelManagerImpl channelManager;
    @Mock
    Message message;
    @Mock
    Object object;

    @Before
    public void setUp() throws Exception
    {
        cxfInterceptor = new CXFInInterceptor( channelManager );
    }


    @Test
    public void testHandleMessageException() throws Exception
    {
        cxfInterceptor.handleMessage( message );
    }

    @Test
    public void testHandleMessage() throws Exception
    {
        when(message.get( any(Message.class) )).thenReturn( "http://example.com/pages/" );

        cxfInterceptor.handleMessage( message );
    }

}
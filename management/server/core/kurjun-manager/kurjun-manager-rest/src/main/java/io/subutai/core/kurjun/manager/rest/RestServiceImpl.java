package io.subutai.core.kurjun.manager.rest;


import java.util.List;

import javax.ws.rs.FormParam;
import javax.ws.rs.core.Response;

import com.google.common.base.Strings;

import io.subutai.common.util.JsonUtil;
import io.subutai.core.kurjun.manager.api.KurjunManager;
import io.subutai.core.kurjun.manager.api.model.Kurjun;


public class RestServiceImpl implements RestService
{
    private KurjunManager kurjunManager;


    @Override
    public Response getKurjunUrl()
    {
        List<Kurjun> urls = kurjunManager.getDataService().getAllKurjunData();

        String info = JsonUtil.GSON.toJson( urls );

        return Response.status( Response.Status.OK ).entity( info ).build();
    }


    @Override
    public Response register( final String url, final int type )
    {
        String authId = "";
        if ( Strings.isNullOrEmpty( kurjunManager.getUser( url, type ) ) )
        {
            authId = kurjunManager.registerUser( url, type );
        }
        else
        {
            authId = kurjunManager.getDataService().getKurjunData( url ).getAuthID();
        }
        return Response.status( Response.Status.OK ).entity( authId ).build();
    }


    @Override
    public Response getSignedMessage( final String signedMsg, final String url, final int type )
    {

        if ( kurjunManager.authorizeUser( url, type, signedMsg ) == null )
        {
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).build();
        }
        else
        {
            return Response.status( Response.Status.OK ).build();
        }
    }


    public void setKurjunManager( final KurjunManager kurjunManager )
    {
        this.kurjunManager = kurjunManager;
    }
}

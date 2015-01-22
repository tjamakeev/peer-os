package org.safehaus.subutai.core.peer.impl.dao;


import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.safehaus.subutai.common.dao.DaoManager;
import org.safehaus.subutai.common.util.GsonInterfaceAdapter;
import org.safehaus.subutai.core.environment.api.exception.EnvironmentPersistenceException;
import org.safehaus.subutai.core.peer.api.ManagementHost;
import org.safehaus.subutai.core.peer.impl.entity.PeerData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import sun.rmi.runtime.Log;


/**
 * PluginDAO is used to manage cluster configuration information in database
 */
public class PeerDAO
{

    private static final Logger LOG = LoggerFactory.getLogger( PeerDAO.class.getName() );
    private Gson gson;
    private DaoManager daoManager;


    public PeerDAO( final DaoManager daoManager) throws SQLException
    {
        Preconditions.checkNotNull( daoManager, "DaoManager is null" );

        this.daoManager = daoManager;
        GsonBuilder gsonBuilder = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping();
        gsonBuilder.registerTypeAdapter( ManagementHost.class, new GsonInterfaceAdapter<ManagementHost>() ).create();
        gson = gsonBuilder.create();
    }

    public boolean saveInfo( String source, String key, Object info )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( source ), "Source is null or empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( key ), "Key is null or empty" );
        Preconditions.checkNotNull( info, "Info is null" );

        EntityManager entityManager = daoManager.getEntityManagerFromFactory();

        try
        {   /*
            String json = gson.toJson( info );
            dbUtil.update( "merge into peer (source, id, info) values (?, ? ,?)", source, UUID.fromString( key ),
                    json );
            */
            String json = gson.toJson( info );
            PeerData peerData = new PeerData();
            peerData.setId( key );
            peerData.setSource( source );
            peerData.setInfo( json );

            daoManager.startTransaction( entityManager );
            entityManager.merge( peerData );
            daoManager.commitTransaction( entityManager );
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage() );

            daoManager.rollBackTransaction( entityManager );
            return false;
        }
        finally
        {
            daoManager.closeEntityManager( entityManager );
        }
        return true;
    }


    /**
     * Returns all POJOs from DB identified by source key
     *
     * @param source - source key
     * @param clazz - class of POJO
     *
     * @return - list of POJOs
     */
    public <T> List<T> getInfo( String source, Class<T> clazz )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( source ), "Source is null or empty" );
        Preconditions.checkNotNull( clazz, "Class is null" );

        EntityManager entityManager = daoManager.getEntityManagerFromFactory();

        List<T> list = new ArrayList<>();
        try
        {
            /*
            ResultSet rs = dbUtil.select( "select info from peer where source = ?", source );
            while ( rs != null && rs.next() )
            {
                Clob infoClob = rs.getClob( "info" );
                if ( infoClob != null && infoClob.length() > 0 )
                {
                    String info = infoClob.getSubString( 1, ( int ) infoClob.length() );
                    list.add( gson.fromJson( info, clazz ) );
                }
            }*/

            Query query;
            query = entityManager.createQuery( "SELECT pd FROM PeerData "
                    + "                             AS pd WHERE pd.source = :source" );
            query.setParameter( "source" ,source );
            List<PeerData> results = query.getResultList();

            for (PeerData pd : results)
            {
                list.add( gson.fromJson( pd.getInfo(), clazz ) );
            }
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage() );
        }
        finally
        {
            daoManager.closeEntityManager( entityManager );
        }
        return list;
    }


    /**
     * Returns POJO from DB
     *
     * @param source - source key
     * @param key - pojo key
     * @param clazz - class of POJO
     *
     * @return - POJO
     */
    public <T> T getInfo( String source, String key, Class<T> clazz )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( source ), "Source is null or empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( key ), "Key is null or empty" );
        Preconditions.checkNotNull( clazz, "Class is null" );

        EntityManager entityManager = daoManager.getEntityManagerFromFactory();

        try
        {
            /*
            ResultSet rs = dbUtil.select( "select info from peer where source = ? and id = ?", source,
                    UUID.fromString( key ) );
            if ( rs != null && rs.next() )
            {
                Clob infoClob = rs.getClob( "info" );
                if ( infoClob != null && infoClob.length() > 0 )
                {
                    String info = infoClob.getSubString( 1, ( int ) infoClob.length() );
                    return gson.fromJson( info, clazz );
                }
            }*/

            Query query;
            query = entityManager.createQuery( "SELECT pd FROM PeerData AS pd WHERE pd.source = :source and pd.id=:id" );
            query.setParameter( "source" ,source );
            query.setParameter( "id", key );
            PeerData pd = (PeerData) query.getSingleResult();

            if(pd !=null)
            {
                return gson.fromJson( pd.getInfo(), clazz );
            }
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage() );
        }
        finally
        {
            daoManager.closeEntityManager( entityManager );
        }

        return null;
    }


    /**
     * deletes POJO from DB
     *
     * @param source - source key
     * @param key - POJO key
     */
    public boolean deleteInfo( String source, String key )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( source ), "Source is null or empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( key ), "Key is null or empty" );
        EntityManager entityManager = daoManager.getEntityManagerFromFactory();

        try
        {
            //dbUtil.update( "delete from peer where source = ? and id = ?", source, UUID.fromString( key ) );

            daoManager.startTransaction( entityManager );

            Query query;
            query = entityManager.createQuery( "delete FROM PeerData "
                    + "                         AS pd WHERE pd.source = :source and pd.id=:id" );
            query.setParameter( "source" ,source );
            query.setParameter( "id", key  );
            query.executeUpdate();
            daoManager.commitTransaction( entityManager );

        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
            daoManager.rollBackTransaction( entityManager );
            return false;
        }
        finally
        {
            daoManager.closeEntityManager( entityManager );
            return true;
        }


    }


    public boolean updateInfo( String source, String key, Object info )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( source ), "Source is null or empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( key ), "Key is null or empty" );
        Preconditions.checkNotNull( info, "Info is null" );

        EntityManager entityManager = daoManager.getEntityManagerFromFactory();

        try
        {
            String json = gson.toJson( info );
            PeerData peerData = new PeerData();
            peerData.setId( key );
            peerData.setSource( source );
            peerData.setInfo( json );

            daoManager.startTransaction( entityManager );
            entityManager.merge( peerData );
            daoManager.commitTransaction( entityManager );
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage() );

            daoManager.rollBackTransaction( entityManager );

            return false;
        }
        finally
        {
            daoManager.closeEntityManager( entityManager );
        }
        return true;
    }
}

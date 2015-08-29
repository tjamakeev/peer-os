package io.subutai.common.protocol.impl;


import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import io.subutai.common.datatypes.TemplateVersion;
import io.subutai.common.exception.DaoException;
import io.subutai.common.protocol.Template;
import io.subutai.common.protocol.api.TemplateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;


public class TemplateServiceImpl implements TemplateService
{
    private static final Logger LOGGER = LoggerFactory.getLogger( TemplateServiceImpl.class.getName() );


    private EntityManagerFactory entityManagerFactory;


    public void setEntityManagerFactory( final EntityManagerFactory entityManagerFactory )
    {
        Preconditions.checkNotNull( entityManagerFactory, "EntityManagerFactory cannot be null value" );
        this.entityManagerFactory = entityManagerFactory;
        LOGGER.info( "EntityManagerFactory is assigned" );
    }


    /**
     * Saves template to database
     *
     * @param template - template to save
     */
    @Override
    public Template saveTemplate( Template template ) throws DaoException
    {
        Template savedTemplate = null;

        EntityManager entityManager = null;
        try
        {
            entityManager = entityManagerFactory.createEntityManager();

            if ( template.getParentTemplateName() != null && !template.getParentTemplateName()
                                                                      .equals( template.getTemplateName() ) )
            {
                Template parent = getTemplate( template.getParentTemplateName(), template.getLxcArch() );
                if ( parent != null )
                {
                    entityManager.getTransaction().begin();
                    savedTemplate = entityManager.merge( template );
                    entityManager.flush();
                    entityManager.getTransaction().commit();
                    parent.addChildren( Arrays.asList( template ) );
                    saveTemplate( parent );
                }
                else
                {
                    throw new DaoException( "Parent template is null: " + template.getParentTemplateName() );
                }
            }
            else
            {
                entityManager.getTransaction().begin();
                savedTemplate = entityManager.merge( template );
                entityManager.flush();
                entityManager.getTransaction().commit();
            }
        }
        catch ( Exception ex )
        {
            LOGGER.warn( "Exception thrown in saveTemplate: ", ex );
            if ( entityManager != null && entityManager.getTransaction().isActive() )
            {
                entityManager.getTransaction().rollback();
            }
            throw new DaoException( ex );
        }
        finally
        {
            if ( entityManager != null )
            {
                entityManager.close();
            }
        }
        return savedTemplate;
    }


    /**
     * Deletes template from database
     *
     * @param template - template to delete
     */
    @Override
    public void removeTemplate( Template template ) throws DaoException
    {
        EntityManager entityManager = null;
        try
        {
            entityManager = entityManagerFactory.createEntityManager();
            entityManager.getTransaction().begin();
            Query query = entityManager.createNamedQuery( Template.QUERY_REMOVE_TEMPLATE_BY_NAME_ARCH );
            query.setParameter( "templateName", template.getTemplateName() );
            query.setParameter( "lxcArch", template.getLxcArch() );
            query.executeUpdate();
            entityManager.getTransaction().commit();
            LOGGER.info( String.format( "Template deleted : %s", template.getTemplateName() ) );
        }
        catch ( Exception ex )
        {

            LOGGER.error( "Exception deleting template : %s", template.getTemplateName() );
            if ( entityManager != null && entityManager.getTransaction().isActive() )
            {
                entityManager.getTransaction().rollback();
            }
            throw new DaoException( ex );
        }
        finally
        {
            if ( entityManager != null )
            {
                entityManager.close();
            }
        }
    }


    /**
     * Returns template by name
     *
     * @param templateName - template name
     * @param lxcArch -- lxc arch of template
     *
     * @return {@code Template}
     */
    @Override
    public Template getTemplate( String templateName, String lxcArch ) throws DaoException
    {
        EntityManager entityManager = null;
        Template template = null;
        try
        {
            entityManager = entityManagerFactory.createEntityManager();
            TypedQuery<Template> query = entityManager.createQuery(
                    "SELECT t FROM Template t WHERE t.pk.templateName = :templateName AND t.pk.lxcArch = :lxcArch",
                    Template.class );
            query.setParameter( "templateName", templateName );
            query.setParameter( "lxcArch", lxcArch );
            List<Template> templates = query.getResultList();
            if ( !templates.isEmpty() )
            {
                template = templates.iterator().next();
            }

            return template;
        }
        catch ( Exception ex )
        {
            throw new DaoException( ex );
        }
        finally
        {
            if ( entityManager != null )
            {
                entityManager.close();
            }
        }
    }


    /**
     * Returns template by name
     *
     * @param templateName - template name
     * @param lxcArch -- lxc arch of template
     * @param md5sum -- lxc md5sum of template
     * @param templateVersion -- lxc templateVersion of template
     *
     * @return {@code Template}
     */
    @Override
    public Template getTemplate( String templateName, String lxcArch, String md5sum, TemplateVersion templateVersion )
            throws DaoException
    {
        return getTemplate( templateName, lxcArch );
        //TODO this method is temporarily replaced by another one till we find a solution with templates versions
        // Don't delete these
        //        EntityManager entityManager = null;
        //        Template template = null;
        //        try
        //        {
        //            entityManager = entityManagerFactory.createEntityManager();
        //            TypedQuery<Template> query = entityManager
        //                    .createNamedQuery( Template.QUERY_GET_TEMPLATE_BY_NAME_ARCH_MD5_VERSION, Template.class );
        //            query.setParameter( "templateName", templateName );
        //            query.setParameter( "lxcArch", lxcArch );
        //            query.setParameter( "md5sum", md5sum );
        //            query.setParameter( "templateVersion", templateVersion );
        //
        //            List<Template> templates = query.getResultList();
        //            if ( templates.isEmpty() )
        //            {
        //                template = templates.get( 0 );
        //            }
        //
        //            return template;
        //        }
        //        catch ( NoResultException | NonUniqueResultException e )
        //        {
        //            return null;
        //        }
        //        catch ( Exception ex )
        //        {
        //            throw new DaoException( ex );
        //        }
        //        finally
        //        {
        //            if ( entityManager != null )
        //            {
        //                entityManager.close();
        //            }
        //        }
    }


    /**
     * Returns template by name
     *
     * @param templateName - template name
     * @param lxcArch -- lxc arch of template
     * @param templateVersion -- lxc templateVersion of template
     *
     * @return {@code Template}
     */
    @Override
    public Template getTemplate( String templateName, TemplateVersion templateVersion, String lxcArch )
            throws DaoException
    {
        return getTemplate( templateName, lxcArch );
        //TODO this method is temporarily replaced by another one till we find a solution with templates versions
        // Don't delete these
        //        EntityManager entityManager = null;
        //        Template template = null;
        //        try
        //        {
        //            entityManager = entityManagerFactory.createEntityManager();
        //            TypedQuery<Template> query = entityManager.createQuery(
        //                    "SELECT t FROM Template t WHERE t.pk.templateName = :templateName AND t.pk.lxcArch =
        // :lxcArch AND"
        //                            + " t.pk"
        //                            + ".templateVersion = :templateVersion", Template.class );
        //            query.setParameter( "templateName", templateName );
        //            query.setParameter( "lxcArch", lxcArch );
        //            query.setParameter( "templateVersion", templateVersion.toString() );
        //            List<Template> templates = query.getResultList();
        //            if ( !templates.isEmpty() )
        //            {
        //                template = templates.get( 0 );
        //            }
        //
        //            return template;
        //        }
        //        catch ( Exception ex )
        //        {
        //            throw new DaoException( ex );
        //        }
        //        finally
        //        {
        //            if ( entityManager != null )
        //            {
        //                entityManager.close();
        //            }
        //        }
    }


    /**
     * Returns all registered templates from database
     *
     * @return {@code List<Template>}
     */
    @Override
    public List<Template> getAllTemplates() throws DaoException
    {
        EntityManager entityManager;

        try
        {
            entityManager = entityManagerFactory.createEntityManager();
            return entityManager.createNamedQuery( Template.QUERY_GET_ALL, Template.class ).getResultList();
        }
        catch ( Exception ex )
        {
            throw new DaoException( ex );
        }
    }


    /**
     * Returns child templates of supplied parent
     *
     * @param parentTemplateName - name of parent template
     * @param lxcArch - lxc arch of template
     *
     * @return {@code List<Template>}
     */
    @Override
    public List<Template> getChildTemplates( String parentTemplateName, String lxcArch ) throws DaoException
    {
        try
        {
            Template template = this.getTemplate( parentTemplateName, lxcArch );
            if ( template != null )
            {
                return template.getChildren();
            }
            else
            {
                return Collections.emptyList();
            }
        }
        catch ( Exception ex )
        {
            throw new DaoException( ex );
        }
    }


    /**
     * Returns child templates of supplied parent
     *
     * @param parentTemplateName - name of parent template
     * @param lxcArch - lxc arch of template
     *
     * @return {@code List<Template>}
     */
    @Override
    public List<Template> getChildTemplates( String parentTemplateName, TemplateVersion templateVersion,
                                             String lxcArch ) throws DaoException
    {
        try
        {
            Template template = this.getTemplate( parentTemplateName, templateVersion, lxcArch );
            if ( template != null )
            {
                return template.getChildren();
            }
            else
            {
                return Collections.emptyList();
            }
        }
        catch ( Exception ex )
        {
            throw new DaoException( ex );
        }
    }


    public EntityManagerFactory getEntityManagerFactory()
    {
        return entityManagerFactory;
    }
}
package org.sonatype.nexus.proxy.repository;

import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.configuration.AbstractConfigurable;
import org.sonatype.nexus.configuration.Configurator;
import org.sonatype.nexus.configuration.CoreConfiguration;
import org.sonatype.nexus.configuration.Validator;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryCoreConfiguration;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.mirror.DefaultPublishedMirrors;
import org.sonatype.nexus.proxy.mirror.PublishedMirrors;

public class ConfigurableRepository
    extends AbstractConfigurable
{
    private PublishedMirrors pMirrors;

    @Override
    protected CRepository getCurrentConfiguration( boolean forWrite )
    {
        return (CRepository) super.getCurrentConfiguration( forWrite );
    }

    @Override
    protected Configurator getConfigurator()
    {
        return null;
    }

    @Override
    protected Validator getValidator()
    {
        return null;
    }

    @Override
    protected ApplicationConfiguration getApplicationConfiguration()
    {
        return null;
    }

    @Override
    protected CoreConfiguration wrapConfiguration( Object configuration )
    {
        return new CRepositoryCoreConfiguration( (CRepository) configuration );
    }

    public String getId()
    {
        return getCurrentConfiguration( false ).getId();
    }

    public void setId( String id )
    {
        getCurrentConfiguration( true ).setId( id );
    }

    public String getName()
    {
        return getCurrentConfiguration( false ).getName();
    }

    public void setName( String name )
    {
        getCurrentConfiguration( true ).setName( name );
    }

    public String getPathPrefix()
    {
        // a "fallback" mechanism: id's must be unique now across nexus,
        // but some older systems may have groups/reposes with same ID. To clear out the ID-clash, we will need to
        // change IDs, but we must _not_ change the published URLs on those systems.
        String pathPrefix = getCurrentConfiguration( false ).getPathPrefix();

        if ( !StringUtils.isBlank( pathPrefix ) )
        {
            return pathPrefix;
        }
        else
        {
            return getId();
        }
    }

    public void setPathPrefix( String prefix )
    {
        getCurrentConfiguration( true ).setPathPrefix( prefix );
    }

    public boolean isIndexable()
    {
        return getCurrentConfiguration( false ).isIndexable();
    }

    public void setIndexable( boolean indexable )
    {
        getCurrentConfiguration( true ).setIndexable( indexable );
    }

    public String getLocalUrl()
    {
        return getCurrentConfiguration( false ).getLocalStorage().getUrl();
    }

    public void setLocalUrl( String localUrl )
        throws StorageException
    {
        String trstr = localUrl.trim();

        if ( trstr.endsWith( RepositoryItemUid.PATH_SEPARATOR ) )
        {
            trstr = trstr.substring( 0, trstr.length() - 1 );
        }

        getCurrentConfiguration( true ).getLocalStorage().setUrl( trstr );
    }

    public LocalStatus getLocalStatus()
    {
        return LocalStatus.valueOf( getCurrentConfiguration( false ).getLocalStatus() );
    }

    public void setLocalStatus( LocalStatus localStatus )
    {
        getCurrentConfiguration( true ).setLocalStatus( localStatus.toString() );
    }

    public boolean isAllowWrite()
    {
        return getCurrentConfiguration( false ).isAllowWrite();
    }

    public void setAllowWrite( boolean allowWrite )
    {
        getCurrentConfiguration( true ).setAllowWrite( allowWrite );
    }

    public boolean isBrowseable()
    {
        return getCurrentConfiguration( false ).isBrowseable();
    }

    public void setBrowseable( boolean browseable )
    {
        getCurrentConfiguration( true ).setBrowseable( browseable );
    }

    public boolean isUserManaged()
    {
        return getCurrentConfiguration( false ).isUserManaged();
    }

    public void setUserManaged( boolean userManaged )
    {
        getCurrentConfiguration( true ).setUserManaged( userManaged );
    }

    public boolean isExposed()
    {
        return getCurrentConfiguration( false ).isExposed();
    }

    public void setExposed( boolean exposed )
    {
        getCurrentConfiguration( true ).setExposed( exposed );
    }

    public int getNotFoundCacheTimeToLive()
    {
        return getCurrentConfiguration( false ).getNotFoundCacheTTL();
    }

    public void setNotFoundCacheTimeToLive( int notFoundCacheTimeToLive )
    {
        getCurrentConfiguration( true ).setNotFoundCacheTTL( notFoundCacheTimeToLive );
    }

    public boolean isNotFoundCacheActive()
    {
        return getCurrentConfiguration( false ).isNotFoundCacheActive();
    }

    public void setNotFoundCacheActive( boolean notFoundCacheActive )
    {
        getCurrentConfiguration( true ).setNotFoundCacheActive( notFoundCacheActive );
    }

    public PublishedMirrors getPublishedMirrors()
    {
        if ( pMirrors == null )
        {
            pMirrors = new DefaultPublishedMirrors( (CRepositoryCoreConfiguration) getCurrentCoreConfiguration() );
        }

        return pMirrors;
    }
}
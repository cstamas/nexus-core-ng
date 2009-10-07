/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.rest.repositories;

import org.codehaus.plexus.component.annotations.Component;
import org.restlet.data.Request;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.sonatype.nexus.index.context.IndexingContext;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.rest.AbstractIndexContentPlexusResource;
import org.sonatype.nexus.rest.NoSuchRepositoryAccessException;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

/**
 * Repository index content resource.
 * 
 * @author dip
 */
@Component( role = PlexusResource.class, hint = "repoIndexResource" )
public class RepositoryIndexContentPlexusResource
    extends AbstractIndexContentPlexusResource
{
    public static final String REPOSITORY_ID_KEY = "repositoryId";

    @Override
    public String getResourceUri()
    {
        return "/repositories/{" + REPOSITORY_ID_KEY + "}/index_content";
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( "/repositories/*/index_content/**", "authcBasic,tiperms" );
    }

    protected IndexingContext getIndexingContext( Request request )
        throws ResourceException
    {
        String repositoryId = String.valueOf( request.getAttributes().get( REPOSITORY_ID_KEY ) );
        
        try
        {
            // make sure the repo exists and we have access to it.
            Repository repository = this.getRepositoryRegistry().getRepository( repositoryId );
            return indexerManager.getRepositoryBestIndexContext( repository.getId() );
        }
        catch ( NoSuchRepositoryAccessException e )
        {
            getLogger().warn( "Repository access denied, id=" + repositoryId );

            throw new ResourceException( Status.CLIENT_ERROR_FORBIDDEN, "Access Denied to Repository" );
        }
        catch ( NoSuchRepositoryException e )
        {
            throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, e );
        }
    }
}
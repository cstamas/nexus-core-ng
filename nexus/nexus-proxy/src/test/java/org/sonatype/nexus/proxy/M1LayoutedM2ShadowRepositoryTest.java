/**
 * Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdPartyUrl}.
 *
 * This program is licensed to you under Version 3 only of the GNU
 * General Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.proxy;

import java.io.IOException;

import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.sonatype.jettytestsuite.ServletServer;
import org.sonatype.nexus.configuration.ConfigurationChangeEvent;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.item.StorageLinkItem;
import org.sonatype.nexus.proxy.maven.maven1.M1LayoutedM2ShadowRepository;
import org.sonatype.nexus.proxy.repository.IncompatibleMasterRepositoryException;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.repository.ShadowRepository;

public class M1LayoutedM2ShadowRepositoryTest
    extends AbstractProxyTestEnvironment
{

    private M2TestsuiteEnvironmentBuilder jettyTestsuiteEnvironmentBuilder;

    @Override
    protected EnvironmentBuilder getEnvironmentBuilder()
        throws Exception
    {
        ServletServer ss = (ServletServer) lookup( ServletServer.ROLE );
        this.jettyTestsuiteEnvironmentBuilder = new M2TestsuiteEnvironmentBuilder( ss );
        return jettyTestsuiteEnvironmentBuilder;
    }

    protected void addShadowReposes()
        throws IOException,
            ComponentLookupException,
            IncompatibleMasterRepositoryException
    {
        for ( ProxyRepository master : getRepositoryRegistry().getRepositoriesWithFacet( ProxyRepository.class ) )
        {
            M1LayoutedM2ShadowRepository shadow = (M1LayoutedM2ShadowRepository) getContainer().lookup(
                ShadowRepository.class,
                "m2-m1-shadow" );

            // shadow.enableLogging( getLogger().getChildLogger( "SHADOW " + master.getId() ) );
            shadow.setMasterRepository( master );
            shadow.setId( master.getId() + "-m1" );
            shadow.setLocalUrl( getApplicationConfiguration()
                .getWorkingDirectory( shadow.getId() ).toURI().toURL().toString() );

            shadow.setLocalStorage( getLocalRepositoryStorage() );
            // shadow.setCacheManager( getCacheManager() );
            shadow.synchronizeWithMaster();

            getRepositoryRegistry().addRepository( shadow );
        }

    }

    public void testM1Shadows()
        throws Exception
    {
        // this will add shadows manually for all registered reposes
        // changes in master should propagate to shadows
        addShadowReposes();

        // get some content to masters
        StorageItem item = getRootRouter().retrieveItem(
            new ResourceStoreRequest( "/repositories/repo1/activemq/activemq-core/1.2/activemq-core-1.2.jar", false ) );
        checkForFileAndMatchContents( item );

        item = getRootRouter().retrieveItem(
            new ResourceStoreRequest( "/repositories/repo2/xstream/xstream/1.2.2/xstream-1.2.2.pom", false ) );
        checkForFileAndMatchContents( item );

        // we will check stuff on M1 places but,
        // we will get links as responses, since shadow reposes contains links only
        getApplicationConfiguration().getConfiguration().getRouting().setFollowLinks( false );
        getApplicationConfiguration().notifyProximityEventListeners(
            new ConfigurationChangeEvent( getApplicationConfiguration() ) );

        item = getRootRouter().retrieveItem(
            new ResourceStoreRequest( "/repositories/repo1-m1/activemq/jars/activemq-core-1.2.jar", false ) );
        assertTrue( StorageLinkItem.class.isAssignableFrom( item.getClass() ) );

        item = getRootRouter().retrieveItem(
            new ResourceStoreRequest( "/repositories/repo2-m1/xstream/poms/xstream-1.2.2.pom", false ) );
        assertTrue( StorageLinkItem.class.isAssignableFrom( item.getClass() ) );

        // and now we will force the router itself to resolve links
        // and will expect the original contents
        getApplicationConfiguration().getConfiguration().getRouting().setFollowLinks( true );
        getApplicationConfiguration().notifyProximityEventListeners(
            new ConfigurationChangeEvent( getApplicationConfiguration() ) );

        item = getRootRouter().retrieveItem(
            new ResourceStoreRequest( "/repositories/repo1-m1/activemq/jars/activemq-core-1.2.jar", false ) );
        // it comes from repo1 even if we requested it from repo1-m1
        assertTrue( "repo1".equals( item.getRepositoryId() ) );
        // and the content is correct
        checkForFileAndMatchContents( item );

        item = getRootRouter().retrieveItem(
            new ResourceStoreRequest( "/repositories/repo2-m1/xstream/poms/xstream-1.2.2.pom", false ) );
        // it comes from repo1 even if we requested it from repo1-m1
        assertTrue( "repo2".equals( item.getRepositoryId() ) );
        // and the content is correct
        checkForFileAndMatchContents( item );

    }

    public void testM1ShadowSync()
        throws Exception
    {
        StorageItem item = getRootRouter().retrieveItem(
            new ResourceStoreRequest( "/repositories/repo1/activemq/activemq-core/1.2/activemq-core-1.2.jar", false ) );
        checkForFileAndMatchContents( item );

        item = getRootRouter().retrieveItem(
            new ResourceStoreRequest( "/repositories/repo2/xstream/xstream/1.2.2/xstream-1.2.2.pom", false ) );
        checkForFileAndMatchContents( item );

        // this will add shadows manually for all registered reposes
        // and sync them
        addShadowReposes();

        // and after sync, we will check stuff on M1 places but,
        // we will get links as responses, since shadow reposes contains links only
        getApplicationConfiguration().getConfiguration().getRouting().setFollowLinks( false );
        getApplicationConfiguration().notifyProximityEventListeners(
            new ConfigurationChangeEvent( getApplicationConfiguration() ) );

        item = getRootRouter().retrieveItem(
            new ResourceStoreRequest( "/repositories/repo1-m1/activemq/jars/activemq-core-1.2.jar", false ) );
        assertTrue( StorageLinkItem.class.isAssignableFrom( item.getClass() ) );

        item = getRootRouter().retrieveItem(
            new ResourceStoreRequest( "/repositories/repo2-m1/xstream/poms/xstream-1.2.2.pom", false ) );
        assertTrue( StorageLinkItem.class.isAssignableFrom( item.getClass() ) );

        // and now we will force the router itself to resolve links
        // and will expect the original contents
        getApplicationConfiguration().getConfiguration().getRouting().setFollowLinks( true );
        getApplicationConfiguration().notifyProximityEventListeners(
            new ConfigurationChangeEvent( getApplicationConfiguration() ) );

        item = getRootRouter().retrieveItem(
            new ResourceStoreRequest( "/repositories/repo1-m1/activemq/jars/activemq-core-1.2.jar", false ) );
        // it comes from repo1 even if we requested it from repo1-m1
        assertTrue( "repo1".equals( item.getRepositoryId() ) );
        // and the content is correct
        checkForFileAndMatchContents( item );

        item = getRootRouter().retrieveItem(
            new ResourceStoreRequest( "/repositories/repo2-m1/xstream/poms/xstream-1.2.2.pom", false ) );
        // it comes from repo1 even if we requested it from repo1-m1
        assertTrue( "repo2".equals( item.getRepositoryId() ) );
        // and the content is correct
        checkForFileAndMatchContents( item );

    }

}

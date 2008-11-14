package org.sonatype.nexus.index.treeview;

import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.index.ArtifactInfo;
import org.sonatype.nexus.index.context.IndexingContext;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.ResourceStore;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.access.AccessManager;
import org.sonatype.nexus.proxy.attributes.inspectors.DigestCalculatingInspector;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;

/**
 * A default implementation of TreeNodeFactory, that is failry simple to extend.
 * 
 * @author cstamas
 */
public class DefaultMergedTreeNodeFactory
    extends DefaultTreeNodeFactory
{
    private ResourceStore store;

    public DefaultMergedTreeNodeFactory( IndexingContext ctx, ResourceStore store )
    {
        super( ctx );

        this.store = store;
    }

    public ResourceStore getResourceStore()
    {
        return store;
    }

    protected TreeNode decorateArtifactNode( IndexTreeView tview, ArtifactInfo ai, String path, TreeNode node )
    {
        ResourceStoreRequest request = new ResourceStoreRequest( path, true );

        DefaultMergedTreeNode mnode = (DefaultMergedTreeNode) node;

        // default it to not available
        mnode.setLocallyAvailable( false );

        try
        {
            StorageItem item = getResourceStore().retrieveItem( request );

            if ( item instanceof StorageFileItem )
            {
                mnode.setLocallyAvailable( true );

                mnode.setArtifactTimestamp( item.getModified() );

                mnode.setArtifactMd5Checksum( item.getAttributes().get( DigestCalculatingInspector.DIGEST_MD5_KEY ) );

                mnode.setArtifactSha1Checksum( item.getAttributes().get( DigestCalculatingInspector.DIGEST_SHA1_KEY ) );

                mnode.setInitiatorUserId( item.getAttributes().get( AccessManager.REQUEST_USER ) );

                mnode.setInitiatorIpAddress( item.getAttributes().get( AccessManager.REQUEST_REMOTE_ADDRESS ) );

                mnode.setArtifactOriginUrl( item.getRemoteUrl() );

                if ( !StringUtils.isEmpty( mnode.getArtifactOriginUrl() ) )
                {
                    mnode.setArtifactOriginReason( "cached" );
                }
                else
                {
                    mnode.setArtifactOriginReason( "deployed" );
                }
            }
        }
        catch ( ItemNotFoundException e )
        {
            // mute it, probaly not available locally
        }
        catch ( Exception e )
        {
            // TODO: do something here?
        }

        return node;
    }

    protected TreeNode createNode( IndexTreeView tview, String path, boolean leaf, String nodeName )
    {
        TreeNode result = super.createNode( tview, path, leaf, nodeName );

        result.setRepositoryId( getResourceStore().getId() );

        return result;
    }

    protected TreeNode instantiateNode( IndexTreeView tview, String path, boolean leaf, String nodeName )
    {
        return new DefaultMergedTreeNode( tview, this );
    }

}

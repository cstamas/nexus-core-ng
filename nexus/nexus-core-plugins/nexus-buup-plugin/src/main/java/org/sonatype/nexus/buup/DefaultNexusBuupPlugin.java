package org.sonatype.nexus.buup;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Configuration;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.buup.api.dto.UpgradeFormRequest;
import org.sonatype.nexus.buup.checks.FSPermissionChecker;
import org.sonatype.nexus.buup.invoke.NexusBuupInvocationException;
import org.sonatype.nexus.buup.invoke.NexusBuupInvocationRequest;
import org.sonatype.nexus.buup.invoke.NexusBuupInvoker;
import org.sonatype.nexus.scheduling.NexusScheduler;

@Component( role = NexusBuupPlugin.class )
public class DefaultNexusBuupPlugin
    implements NexusBuupPlugin
{
    @Requirement
    private NexusBuupInvoker invoker;

    @Requirement
    private NexusScheduler nexusScheduler;

    @Requirement
    private FSPermissionChecker permissionChecker;

    @Requirement
    private UpgradeFormProcessor upgradeFormProcessor;

    @Configuration( value = "${basedir}" )
    private File basedir;

    @Configuration( value = "${nexus-app}" )
    private File nexusAppDir;

    @Configuration( value = "${nexus-work}" )
    private File nexusWorkDir;

    @Configuration( value = "${nexus-work}/upgrade-bundle" )
    private File upgradeBundleDir;

    private BundleDownloadTask downloadTask;

    private Collection<IOException> failures;
    
    private UpgradeFormRequest form;
    
    private UpgradeProcessStatus status = UpgradeProcessStatus.UNUSED;

    public void initiateBundleDownload( UpgradeFormRequest form )
        throws NexusUpgradeException
    {
        if ( !getUpgradeProcessStatus().isStartingState() )
        {
            throw new NexusUpgradeException(
                "The upgrade process is in wrong state, it is still downloading the bundle!" );
        }
        
        this.form = null;

        if ( !upgradeFormProcessor.processForm( form ) )
        {
            throw new NexusUpgradeException( "The upgrade process is not started, form is not filled in properly!" );
        }

        ArrayList<IOException> failures = new ArrayList<IOException>();

        // check FS permissions
        performAndCollectIOException( failures, new KindaClosure()
        {
            public void perform()
                throws IOException
            {
                permissionChecker.checkFSPermissionsOnDirectory( basedir );
            }
        } );
        performAndCollectIOException( failures, new KindaClosure()
        {
            public void perform()
                throws IOException
            {
                permissionChecker.checkFSPermissionsOnDirectory( nexusAppDir );
            }
        } );
        performAndCollectIOException( failures, new KindaClosure()
        {
            public void perform()
                throws IOException
            {
                permissionChecker.checkFSPermissionsOnDirectory( nexusWorkDir );
            }
        } );
        performAndCollectIOException( failures, new KindaClosure()
        {
            public void perform()
                throws IOException
            {
                if ( !upgradeBundleDir.exists() && !upgradeBundleDir.mkdirs() )
                {
                    throw new IOException( "Cannot create directory \"" + upgradeBundleDir.getAbsolutePath()
                        + "\" for unzipped bundle content!" );
                }
            }
        } );

        // check results
        if ( failures.isEmpty() )
        {
            this.failures = null;
            this.form = form;

            // start download thread
            downloadTask = nexusScheduler.createTaskInstance( BundleDownloadTask.class );
            downloadTask.setTargetDirectory( upgradeBundleDir );
            nexusScheduler.submit( "Bundle Download", downloadTask );
        }
        else
        {
            this.failures = failures;
            this.form = null;

            throw new NexusUpgradeException( "Not all conditions are met to perform upgrade!" );
        }
    }

    public UpgradeProcessStatus getUpgradeProcessStatus()
    {
        return status;
/*        if ( downloadTask == null && failures == null )
        {
            return UpgradeProcessStatus.UNUSED;
        }
        else if ( failures != null && failures.size() > 0 )
        {
            return UpgradeProcessStatus.FAILED;
        }
        else if ( downloadTask.isFinished() && downloadTask.isSuccessful() && form != null )
        {
            return UpgradeProcessStatus.READY_TO_RUN;
        }
        else if ( downloadTask.isFinished() && !downloadTask.isSuccessful() )
        {
            return UpgradeProcessStatus.FAILED;
        }
        else if ( !downloadTask.isFinished() )
        {
            return UpgradeProcessStatus.DOWNLOADING;
        }
        else
        {
            // ??
            return UpgradeProcessStatus.FAILED;
        }*/
    }
    
    public void setUpgradeProcessStatus( UpgradeProcessStatus status )
    {
        this.status = status;
    }

    public Collection<IOException> getFailureReasons()
    {
        return failures;
    }

    public void initiateUpgradeProcess()
        throws NexusUpgradeException, NexusBuupInvocationException
    {
        if ( !UpgradeProcessStatus.READY_TO_RUN.equals( getUpgradeProcessStatus() ) )
        {
            throw new NexusUpgradeException(
                "Upgrade process is not ready to be run yet. Please check the download status or logs!" );
        }

        NexusBuupInvocationRequest request = new NexusBuupInvocationRequest( upgradeBundleDir );

        // simulate we have params for now
        request.setNexusBundleXms( form.getXmsInMbs() );

        request.setNexusBundleXmx( form.getXmxInMbs() );

        invoker.invokeBuup( request );
    }

    // ==

    public static interface KindaClosure
    {
        void perform()
            throws IOException;
    }

    protected void performAndCollectIOException( List<IOException> failures, KindaClosure kinda )
    {
        try
        {
            kinda.perform();
        }
        catch ( IOException e )
        {
            failures.add( e );
        }
    }
}

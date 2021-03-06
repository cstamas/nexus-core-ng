/**
 * Copyright (c) 2007-2008 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package org.sonatype.nexus.index.locator;

import java.io.File;

import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.artifact.GavCalculator;

/**
 * A POM locator to locate POM artifact. 
 */
public class PomLocator
    implements GavHelpedLocator
{
    public File locate( File source, GavCalculator gavCalculator, Gav gav )
    {
        String artifactName = gav.getArtifactId() + "-" + gav.getVersion() + ".pom";

        return new File( source.getParent(), artifactName );
    }
}

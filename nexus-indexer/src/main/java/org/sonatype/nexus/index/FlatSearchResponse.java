/**
 * Copyright (c) 2007-2008 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package org.sonatype.nexus.index;

import java.util.Set;

import org.apache.lucene.search.Query;

/**
 * A flat search response.
 * 
 * @see NexusIndexer#searchFlat(FlatSearchRequest)
 */
public class FlatSearchResponse
    extends AbstractSearchResponse
{
    private final Set<ArtifactInfo> results;

    public FlatSearchResponse( Query query, int totalHits, Set<ArtifactInfo> results )
    {
        super( query, totalHits );

        this.results = results;
    }

    public Set<ArtifactInfo> getResults()
    {
        return results;
    }
}

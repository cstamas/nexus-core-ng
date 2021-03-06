/**
 * Copyright (c) 2007-2008 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package org.sonatype.nexus.index;

import java.util.Map;

import org.apache.lucene.search.Query;

/**
 * A grouped search response.
 * 
 * @see NexusIndexer#searchGrouped(GroupedSearchRequest)
 */
public class GroupedSearchResponse
    extends AbstractSearchResponse
{
    private final Map<String, ArtifactInfoGroup> results;

    public GroupedSearchResponse( Query query, int totalHits, Map<String, ArtifactInfoGroup> results )
    {
        super( query, totalHits );

        this.results = results;
    }

    public Map<String, ArtifactInfoGroup> getResults()
    {
        return results;
    }
}

/*
 * The baseCode project
 * 
 * Copyright (c) 2006 University of British Columbia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package ubic.basecode.datafilter;

import java.util.List;
import java.util.Set;
import java.util.Vector;

import ubic.basecode.dataStructure.matrix.NamedMatrix2D;

/**
 * Remove or retain rows that are on a list.
 * 
 * @author Paul Pavlidis
 * @version $Id$
 */
public class RowNameFilter extends AbstractFilter implements Filter {

    private boolean exclude = false;
    private Set filterNames;

    /**
     * @param namesToFilter
     * @param exclude Set to true if you want the list to indicate items to be skipped, rather than selected.
     */
    public RowNameFilter( Set namesToFilter, boolean exclude ) {
        this( namesToFilter );
        this.exclude = exclude;
    }

    /**
     * @param namesToFilter
     */
    public RowNameFilter( Set namesToFilter ) {
        filterNames = namesToFilter;
    }

    public RowNameFilter() {
        filterNames = null;
    }

    public void setFilterNames( Set namesToFilter, boolean exclude ) {
        this.filterNames = namesToFilter;
        this.exclude = exclude;
    }

    /**
     * Filter according to row names.
     * 
     * @param data
     * @return
     */
    public NamedMatrix2D filter( NamedMatrix2D data ) {
        List MTemp = new Vector();
        List rowNames = new Vector();
        int numRows = data.rows();
        int numCols = data.columns();
        int numNeeded = filterNames.size();
        int kept = 0;
        for ( int i = 0; i < numRows; i++ ) {
            String name = data.getRowName( i ).toString();

            // apply the rules.
            if ( filterNames.contains( name ) ) {
                if ( exclude ) {
                    continue;
                }
                MTemp.add( data.getRowObj( i ) );
                rowNames.add( name );
                kept++;
                if ( kept >= numNeeded ) {
                    break; // no use in continuing.
                }
            }

            if ( exclude ) {
                MTemp.add( data.getRowObj( i ) );
                rowNames.add( name );
                kept++;
            }
        }

        NamedMatrix2D returnval = getOutputMatrix( data, MTemp.size(), numCols );

        for ( int i = 0; i < MTemp.size(); i++ ) {
            for ( int j = 0; j < numCols; j++ ) {
                returnval.set( i, j, ( ( Object[] ) MTemp.get( i ) )[j] );
            }
        }
        returnval.setColumnNames( data.getColNames() );
        returnval.setRowNames( rowNames );

        log.info( "There are " + kept + " rows left after filtering." );

        return ( returnval );
    }

}
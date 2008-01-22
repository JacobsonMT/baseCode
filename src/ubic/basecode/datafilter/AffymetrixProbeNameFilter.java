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
import java.util.Vector;

import ubic.basecode.dataStructure.matrix.NamedMatrix;

/**
 * Remove probes that have names meeting certain rules indicating they may have low reliability. This is targeted at
 * cases like "AFFX", "_st", "_f_at" and so forth.
 * 
 * @author Paul Pavlidis
 * @version $Id$
 */
public class AffymetrixProbeNameFilter<R, C> extends AbstractFilter<R, C> {

    private boolean skip_ST = false;
    private boolean skip_AFFX = false;
    private boolean skip_F = false;
    private boolean skip_X = false;
    private boolean skip_G = false;

    /**
     * Filter probes that contain the '_st' (sense strand) tag
     */
    public static final int ST = 1;

    /**
     * Filter probes that have the AFFX prefix.
     */
    public static final int AFFX = 2;

    /**
     * Filter probes that have the "_f_at" (family) tag.
     */
    public static final int F = 3;

    /**
     * Filter probes that have the "_x_at" tag.
     */
    public static final int X = 4;

    /**
     * Filter probes that have the "_g_at" (group) tag.
     */
    public static final int G = 5;

    /**
     * @param criteria int[] of constants indicating the criteria to use.
     */
    public AffymetrixProbeNameFilter( int[] criteria ) {
        this.setCriteria( criteria );
    }

    /**
     * Filter probes with all criteria switched on.
     */
    public AffymetrixProbeNameFilter() {
        this.setCriteria( new int[] { 1, 2, 3, 4, 5 } );
    }

    private void setCriteria( int[] criteria ) {
        for ( int i = 0; i < criteria.length; i++ ) {
            switch ( criteria[i] ) {
                case ST: {
                    skip_ST = true;
                }
                case AFFX: {
                    skip_AFFX = true;
                }
                case F: {
                    skip_F = true;
                }
                case X: {
                    skip_X = true;
                }
                case G: {
                    skip_G = true;
                }
                default: {
                    break;
                }
            }
        }
    }

    public NamedMatrix<R, C> filter( NamedMatrix<R, C> data ) {
        List<Object[]> MTemp = new Vector<Object[]>();
        List<R> rowNames = new Vector<R>();
        int numRows = data.rows();
        int numCols = data.columns();

        int kept = 0;
        for ( int i = 0; i < numRows; i++ ) {
            String name = data.getRowName( i ).toString();

            // apply the rules.
            if ( skip_ST && name.contains( "_st" ) ) { // 'st' means sense strand.
                continue;
            }

            if ( skip_AFFX && name.contains( "AFFX" ) ) {
                continue;
            }

            if ( skip_F && name.contains( "_f_at" ) ) { // gene family. We don't
                // like.
                continue;
            }

            if ( skip_X && name.contains( "_x_at" ) ) {
                continue;
            }
            if ( skip_G && name.contains( "_g_at" ) ) {
                continue;
            }
            MTemp.add( data.getRowObj( i ) );
            rowNames.add( data.getRowName( i ) );
            kept++;
        }

        NamedMatrix<R, C> returnval = getOutputMatrix( data, MTemp.size(), numCols );

        for ( int i = 0; i < MTemp.size(); i++ ) {
            for ( int j = 0; j < numCols; j++ ) {
                returnval.set( i, j, MTemp.get( i )[j] );
            }
        }
        returnval.setColumnNames( data.getColNames() );
        returnval.setRowNames( rowNames );
        log.info( "There are " + kept + " rows left after filtering." );

        return returnval;

    }
}
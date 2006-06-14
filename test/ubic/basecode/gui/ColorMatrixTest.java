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
package ubic.basecode.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import ubic.basecode.dataStructure.matrix.DenseDoubleMatrix2DNamed;
import ubic.basecode.dataStructure.matrix.DoubleMatrixNamed;

/**
 * @author keshav
 * @version $Id$
 */
public class ColorMatrixTest extends TestCase {

    /**
     * 
     *
     */
    public void testColorMatrix() {

        double[][] array = new double[5][5];

        double[] row0 = { 3, 2, 5, 6, 9 };
        double[] row1 = { 100, 13, 0, 12, 0 };
        double[] row2 = { 7, 78, 23, 98, 4 };
        double[] row3 = { 54, 7, 8, 3, 1 };
        double[] row4 = { 13, 2, 9, 7, 0 };

        array[0] = row0;
        array[1] = row1;
        array[2] = row2;
        array[3] = row3;
        array[4] = row4;

        List<String> rowNames = new ArrayList();
        rowNames.add( "A" );
        rowNames.add( "B" );
        rowNames.add( "C" );
        rowNames.add( "D" );
        rowNames.add( "E" );

        List<String> colNames = new ArrayList();
        colNames.add( "0_at" );
        colNames.add( "1_at" );
        colNames.add( "2_at" );
        colNames.add( "3_at" );
        colNames.add( "4_at" );

        // DoubleMatrix2D matrix = new DenseDoubleMatrix2D( array );
        DoubleMatrixNamed matrix = new DenseDoubleMatrix2DNamed( array );
        matrix.setRowNames( rowNames );
        matrix.setColumnNames( colNames );
        ColorMatrix colorMatrix = new ColorMatrix( matrix );
        JMatrixDisplay display = new JMatrixDisplay( colorMatrix );
        try {
            display.saveImage( "test/ubic/basecode/gui/outfile.png" );
            display.setLabelsVisible( true );
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }

}

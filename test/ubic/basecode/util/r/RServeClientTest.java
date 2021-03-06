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
package ubic.basecode.util.r;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;

import ubic.basecode.io.reader.DoubleMatrixReader;

/**
 * @author pavlidis
 * 
 */
public class RServeClientTest extends AbstractRClientTest {

    @Before
    public void setUp() throws Exception {
        try {
            rc = new RServeClient();
            connected = rc.isConnected();
        } catch ( IOException e ) {
            connected = false;
        }

        DoubleMatrixReader reader = new DoubleMatrixReader();
        tester = reader.read( this.getClass().getResourceAsStream( "/data/testdata.txt" ) );
    }

    @After
    public void tearDown() {
        tester = null;
        if ( rc != null && rc.isConnected() ) rc.disconnect();
    }

}
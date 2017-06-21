/*
 * The baseCode project
 * 
 * Copyright (c) 2010 University of British Columbia
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
package ubic.basecode.util.r.type;

/**
 * @author paul
 */
public abstract class AnovaResult {

    protected Double residualDf = null;

    private Object key = null;

    @Override
    public boolean equals( Object obj ) {
        if ( this == obj ) return true;
        if ( obj == null ) return false;
        if ( getClass() != obj.getClass() ) return false;
        AnovaResult other = ( AnovaResult ) obj;
        if ( key == null ) {
            if ( other.key != null ) return false;
        } else if ( !key.equals( other.key ) ) return false;
        return true;
    }

    /**
     * @return value used to track the identity of this
     */
    public Object getKey() {
        return key;
    }

    /**
     * @return the residualDf
     */
    public Double getResidualDf() {
        return residualDf;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( key == null ) ? 0 : key.hashCode() );
        return result;
    }

    /**
     * @param key optional value that can be used to track the identity of this
     */
    public void setKey( Object key ) {
        this.key = key;
    }
}

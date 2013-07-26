package ubic.basecode.dataStructure;

import java.util.HashMap;
import java.util.Map;

/**
 * Similar to ubic.gemma.util.CountingMap. Except instead it adds the values of the keys.
 * 
 * @author ?
 * @version $Id$
 */
public class DoubleAddingMap<K> extends HashMap<K, Double> {

    private static final long serialVersionUID = 2;

    /**
     * adds the current value to the value of this key. If the current value is null then value becomes the d parameter.
     * 
     * @return the value at this key.
     */
    public Double addPut( K key, Double d ) {

        Double current = get( key );
        if ( current == null ) {
            put( key, d );
            return d;
        }
        double sum = current + d;
        put( key, sum );
        return sum;
    }

    public void addPutAll( Map<? extends K, ? extends Double> map ) {
        for ( Map.Entry<? extends K, ? extends Double> entry : map.entrySet() ) {
            addPut( entry.getKey(), entry.getValue() );
        }
    }
}

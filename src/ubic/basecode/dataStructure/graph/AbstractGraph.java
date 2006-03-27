/*
 * Created on Jun 20, 2004
 */
package ubic.basecode.dataStructure.graph;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ubic.basecode.dataStructure.Visitable;

/**
 * <p>
 * Copyright (c) Columbia University
 * 
 * @author Paul Pavlidis
 * @version $Id$
 */
public abstract class AbstractGraph implements Graph {

    private static Log log = LogFactory.getLog( AbstractGraph.class.getName() );
    protected Map items;

    public AbstractGraph() {
        items = new LinkedHashMap();
    }

    /**
     * Create a new graph from a set of nodes. This allows us to easily make subtrees.
     * 
     * @param nodes Set of AbstractGraphNodes.
     */
    public AbstractGraph( Set nodes ) {
        items = new LinkedHashMap();
        for ( Iterator it = nodes.iterator(); it.hasNext(); ) {
            GraphNode n = ( GraphNode ) it.next();
            this.addNode( n.getKey(), n.getItem() );
        }
    }

    /**
     * @param node GraphNode
     */
    public void addNode( GraphNode node ) {
        node.setGraph( this );
        items.put( node.getKey(), node );
    }

    /**
     * Retrieve a node by key. To get the contents of a node use getNodeContents(key)
     * 
     * @param key Object
     * @see #getNodeContents(Object)
     * @return AbstractGraphNode referenced by the key.
     */
    public GraphNode get( Object key ) {
        return ( GraphNode ) items.get( key );
    }

    /**
     * @return Map
     */
    public Map getItems() {
        return items;
    }

    /**
     * Retrieve the contents of a node by key.
     * 
     * @see #get
     * @param key Object
     * @return The object contained by a node, not the node itself.
     */
    public Object getNodeContents( Object key ) {
        if ( !items.containsKey( key ) ) return null;
        return ( ( GraphNode ) items.get( key ) ).getItem();
    }

    /**
     * @param key Object
     * @return true if the graph contains an item referenced by key, false otherwise.
     */
    public boolean containsKey( Object key ) {
        return items.containsKey( key );
    }

    /**
     * Reset the 'visited' marks of the graph to false.
     */
    public void unmarkAll() {
        for ( Iterator it = items.keySet().iterator(); it.hasNext(); ) {
            Object item = it.next();
            if ( !( item instanceof Visitable ) ) {
                log.debug( "Got " + item.getClass().getName() );
                break;
            }
            ( ( Visitable ) item ).unMark();
        }
    }
}
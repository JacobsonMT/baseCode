package baseCode.dataStructure.graph;

/**
 * 
 * 
 * <p>Copyright (c) Columbia University
 * @author Paul Pavlidis
 * @version $Id$
 */
public abstract class AbstractGraphNode implements Comparable {
   protected Object key;
   protected Object item;
   protected AbstractGraph graph; // the graph this belongs to.
   protected boolean visited = false;

   /**
    * Get the actual contents of the node.
    * @return
    */
   public Object getItem() {
      return item;
   }

   /**
    * Create a new node with key and value given. The key is stored by the graph
    * and is used to retrieve nodes. Keys and nodes can be any kind of object.
    * @param key
    * @param value
    */
   public AbstractGraphNode(Object key, Object value, AbstractGraph graph) {
      this.setValue(key, value);
      this.graph = graph;
   }

   /**
    * Create a new node when given only a key.
    * @param key
    */
   public AbstractGraphNode(Object key) {
      this.key = key;
      this.item = null;
   }

   /**
    * Set the contents of the node
    * @param j
    */
   public void setValue(Object key, Object value) {
      this.item = value;
      this.key = key;
   }

   /**
    * Set the item this node holds
    * @param value
    */
   public void setItem(Object value) {
      this.item = value;
   }

   public String toString() {
      return item.toString();
   }

   /**
    * Return the graph that contains this node instance.
    * @return
    */
   protected AbstractGraph getGraph() {
      return graph;
   }

   /**
    * Mark this node as visited.
    */
   protected void mark() {
      visited = true;
   }

   /**
    * Unmark this node so it is 'unvisited'.
    *
    */
   protected void unMark() {
      visited = false;
   }

   /**
    * Return true if the node has been visited.
    * @return
    */
   protected boolean isVisited() {
      return visited;
   }

}

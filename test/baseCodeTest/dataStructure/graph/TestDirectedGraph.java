/*
 * Created on Jun 20, 2004
 *
 */
package baseCodeTest.dataStructure.graph;

import baseCode.dataStructure.graph.DirectedGraph;
import junit.framework.TestCase;
import baseCode.dataStructure.graph.DirectedGraphNode;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

/**
 *
 * <p>Copyright (c) Columbia University
 * @author Paul Pavlidis
 * @version $Id$
 */
public class TestDirectedGraph
    extends TestCase {
   DirectedGraph testGraph;
   DirectedGraph testGraphCycle; // has a cycle.
   /**
    * Constructor for TestDirectedGraph.
    * @param arg0
    */
   public TestDirectedGraph( String arg0 ) {
      super( arg0 );
      testGraph = new DirectedGraph();

      testGraph.addNode( "b", "bee." );
      testGraph.addNode( "a", "aaa." );
      testGraph.addNode( "c", "cee." );
      testGraph.addNode( "d", "dee." );
      testGraph.addNode( "f", "eff." );
      testGraph.addNode( "e", "eee." );

      testGraph.addParentTo( "b", "a" );
      testGraph.addParentTo( "c", "a" );
      testGraph.addChildTo( "a", "c" ); // redundant
      testGraph.addChildTo( "a", "b" ); // redundant

      testGraph.addChildTo( "c", "d" ); // top down
      testGraph.addChildTo( "c", "e" ); // top down
      testGraph.addParentTo( "f", "c" ); // bottom up

      testGraphCycle = new DirectedGraph();

      testGraphCycle.addNode( "b", "bee." );
      testGraphCycle.addNode( "a", "aaa." );
      testGraphCycle.addNode( "c", "cee." );
      testGraphCycle.addNode( "d", "dee." );
      testGraphCycle.addNode( "f", "eff." );
      testGraphCycle.addNode( "e", "eee." );

      testGraphCycle.addParentTo( "b", "a" );
      testGraphCycle.addParentTo( "c", "a" );
      testGraphCycle.addChildTo( "a", "c" ); // redundant
      testGraphCycle.addChildTo( "a", "b" ); // redundant

      testGraphCycle.addChildTo( "c", "d" ); // top down
      testGraphCycle.addChildTo( "c", "e" ); // top down
      testGraphCycle.addParentTo( "f", "c" ); // bottom up
      testGraphCycle.addParentTo( "f", "e" ); // cycle
   }

   /*
    * @see TestCase#setUp()
    */
   protected void setUp() throws Exception {
      super.setUp();
   }

   /*
    * @see TestCase#tearDown()
    */
   protected void tearDown() throws Exception {
      super.tearDown();
   }

   public void testToString() {
      String expectedReturn = "aaa.\n\tbee.\n\tcee.\n\t\tdee.\n\t\teee.\n\t\teff.\n";
      String actualReturn = testGraph.toString();
      assertEquals( "return", expectedReturn, actualReturn );
   }

    public void testTopoSort() {
       testGraph.topoSort();
       List nodes = new ArrayList( testGraph.getItems().values() );
       Collections.sort( nodes );
       StringBuffer buf = new StringBuffer();
       for (Iterator it = nodes.iterator(); it.hasNext(); ) {
          buf.append(it.next().toString());
       }
       String actualReturn = buf.toString();
       String expectedReturn = "aaa.bee.cee.dee.eee.eff.";

       assertEquals( "return", expectedReturn, actualReturn );
    }


   public void testGetChildren() {
      DirectedGraphNode n = ( DirectedGraphNode ) testGraph.get( "c" );
      String actualReturn = n.getChildGraph().toString();
      String expectedReturn = "cee.\n\tdee.\n\teee.\n\teff.\n";
      assertEquals( "return", expectedReturn, actualReturn );
   }

}

package baseCode.util;


/**
 * Copyright (c) 2004 Columbia University
 * @author Paul Pavlidis
 * @version $Id$
 */
public class StatusStderr implements StatusViewer {

   public void setStatus( String s ) {
      System.err.println(s);
   }
   
   public void setError( String s ) {
      System.err.println("Error:" + s);
   }

   public void clear() {
   }

}

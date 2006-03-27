package ubic.basecode.gui.file;

import java.io.File;

import javax.swing.filechooser.FileFilter;

import ubic.basecode.util.FileTools;

/**
 * <hr>
 * <p>
 * Copyright (c) 2004 Columbia University
 * 
 * @author Will Braynen
 * @version $Id$
 */
public class ImageFileFilter extends FileFilter {

    private String description = "image files";

    /**
     * 
     */
    public boolean accept( File f ) {

        if ( f.isDirectory() ) {
            return true;
        }

        return FileTools.hasImageExtension( f.getName() );

    } // end accept

    /**
     * 
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description
     */
    public void setDescription( String description ) {
        this.description = description;
    }
}
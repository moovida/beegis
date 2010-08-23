package eu.hydrologis.jgrass.geonotes.util;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

import eu.hydrologis.jgrass.geonotes.GeonotesHandler;

public class GeonotesNameSorter extends ViewerSorter {
    @Override
    public int compare( Viewer viewer, Object e1, Object e2 ) {
        String title1 = ((GeonotesHandler) e1).getTitle();
        String title2 = ((GeonotesHandler) e2).getTitle();

        return title1.compareTo(title2);
    }
}

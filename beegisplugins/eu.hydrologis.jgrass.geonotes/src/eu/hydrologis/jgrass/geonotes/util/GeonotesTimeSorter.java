package eu.hydrologis.jgrass.geonotes.util;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.joda.time.DateTime;

import eu.hydrologis.jgrass.geonotes.GeonotesHandler;

public class GeonotesTimeSorter extends ViewerSorter {
    @Override
    public int compare( Viewer viewer, Object e1, Object e2 ) {
        DateTime time1 = ((GeonotesHandler) e1).getCreationDate();
        DateTime time2 = ((GeonotesHandler) e2).getCreationDate();

        int compareTo = time1.compareTo(time2);
        if (compareTo == 0) {
            return 0;
        }
        return -compareTo;
    }
}
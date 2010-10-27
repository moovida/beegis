package eu.hydrologis.jgrass.gpsnmea.gps;

public interface IGpsObserver {
    
    public void updateGpsPoint(AbstractGps gpsEngine, GpsPoint gpsPoint);

}

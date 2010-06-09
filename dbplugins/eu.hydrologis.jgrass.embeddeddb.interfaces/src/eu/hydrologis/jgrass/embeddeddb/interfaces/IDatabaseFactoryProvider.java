package eu.hydrologis.jgrass.embeddeddb.interfaces;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;

public interface IDatabaseFactoryProvider {

    /**
     * Creates the {@link SessionFactory} for the connection.
     * 
     * @return the {@link SessionFactory}
     * @throws Exception
     */
    public abstract SessionFactory getSessionFactory() throws Exception;

    /**
     * Closes the {@link SessionFactory}.
     * @throws Exception 
     */
    public abstract void closeSessionFactory() throws Exception;
    
    
    /**
     * Supplies the annotation configuration.
     * 
     * @return the {@link AnnotationConfiguration}.
     */
    public AnnotationConfiguration getConfiguration() throws Exception;

}
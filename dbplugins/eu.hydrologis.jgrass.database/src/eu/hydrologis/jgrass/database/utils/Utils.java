package eu.hydrologis.jgrass.database.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Utils {

    /**
     * Generates minimum config file that needs to be on disk.
     * 
     * @throws IOException
     */
    @SuppressWarnings("nls")
    public static File generateConfigFile() throws IOException {
        StringBuilder sB = new StringBuilder();
        sB.append("<?xml version='1.0' encoding='utf-8'?>").append("\n");
        sB.append("<!DOCTYPE hibernate-configuration PUBLIC").append("\n");
        sB.append("    \"-//Hibernate/Hibernate Configuration DTD//EN\"").append("\n");
        sB.append("    \"http://hibernate.sourceforge.net/hibernate-configuration-3.0.dtd\">").append("\n");
        sB.append("<hibernate-configuration>").append("\n");
        sB.append(" <session-factory>").append("\n");
        sB.append("     <!-- Use the C3P0 connection pool provider -->").append("\n");
        sB.append("     <property name=\"hibernate.c3p0.min_size\">5</property>").append("\n");
        sB.append("     <property name=\"hibernate.c3p0.max_size\">20</property>").append("\n");
        sB.append("     <property name=\"hibernate.c3p0.timeout\">300</property>").append("\n");
        sB.append("     <property name=\"hibernate.c3p0.max_statements\">50</property>").append("\n");
        sB.append("     <property name=\"hibernate.c3p0.idle_test_period\">3000</property>").append("\n");
        // sB.append("     <property name=\"hibernate.query.factory_class\">org.hibernate.hql.classic.ClassicQueryTranslatorFactory</property>").append("\n");
        sB.append(" </session-factory>").append("\n");
        sB.append("</hibernate-configuration>").append("\n");

        File tempFile = File.createTempFile("jgrass_hibernate", null);
        BufferedWriter bW = new BufferedWriter(new FileWriter(tempFile));
        bW.write(sB.toString());
        bW.close();

        return tempFile;
    }
}

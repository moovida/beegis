package eu.hydrologis.jgrass.featureeditor.xml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import eu.hydrologis.jgrass.featureeditor.xml.annotated.Form;
import eu.hydrologis.jgrass.featureeditor.xml.annotated.OrderedElement;
import eu.hydrologis.jgrass.featureeditor.xml.annotated.Tab;

@SuppressWarnings("nls")
public class FormParser {

    public static Form parseXML( String xml ) {
        try {
            JAXBContext jc = JAXBContext.newInstance("eu.hydrologis.jgrass.featureeditor.xml.annotated");
            Unmarshaller um = jc.createUnmarshaller();
            StringReader sr = new StringReader(xml);
            return (Form) um.unmarshal(sr);
        } catch (JAXBException e) {
            System.err.println("Exception parsing xml " + xml);
            e.printStackTrace();
        }

        return null;
    }

    public static void writeXML( Form form, File file ) {
        try {
            JAXBContext jc = JAXBContext.newInstance(Form.class);
            Marshaller marshaller = jc.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true); // pretty print XML
            marshaller.marshal(form, file);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

    public static void main( String[] args ) throws IOException {

        BufferedReader r = new BufferedReader(
                new FileReader(
                        "/home/moovida/development/beegis-hg/beegis/beegisplugins/eu.hydrologis.jgrass.formeditor/src/eu/hydrologis/jgrass/featureeditor/test.xml"));
        StringBuilder xml = new StringBuilder();

        String line = null;
        while( (line = r.readLine()) != null ) {
            xml.append(line).append("\n");
        }
        r.close();

        String xmlString = xml.toString();
        System.out.println(xmlString);
        Form form = parseXML(xmlString);

        List<Tab> tabList = form.getOrderedTabs();
        for( Tab tab : tabList ) {
            System.out.println("tab: " + tab.name);
            List< ? extends OrderedElement> orderedElements = tab.getOrderedElements();
            for( OrderedElement orderedElement : orderedElements ) {
                String name = orderedElement.getName();
                System.out.println("Element: " + name);
            }

        }

        System.out.println(form.toString());

        // Tab t1 = new Tab();
        // t1.name = "tab1";
        // t1.text = "generalita'";
        // t1.x = 0;
        // t1.y = 0;
        // t1.w = 300;
        // t1.h = 120;
        // Tab t2 = new Tab();
        // t2.name = "tab2";
        // t2.text = "anagrafica";
        // t2.x = 0;
        // t2.y = 0;
        // t2.w = 300;
        // t2.h = 120;
        //
        // Form f = new Form();
        // f.tab = new ArrayList<Tab>();
        // f.tab.add(t1);
        // f.tab.add(t2);
        //
        // String path =
        // "/home/moovida/development/hydrologis-hg/hydrologis/hydrologis_extentions/jgrass4udig12/eu.hydrologis.jgrass.formeditor/src/eu/hydrologis/jgrass/featureeditor/smalltest2.xml";
        // File file = new File(path);
        //
        // writeXML(f, file);
        //
        System.out.println();

    }
}
package eu.hydrologis.jgrass.featureeditor.xml;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import net.miginfocom.swt.MigLayout;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import eu.hydrologis.jgrass.featureeditor.xml.annotated.Form;
import eu.hydrologis.jgrass.featureeditor.xml.annotated.FormElement;
import eu.hydrologis.jgrass.featureeditor.xml.annotated.Tab;
import eu.hydrologis.jgrass.featureeditor.xml.annotatedguis.FormGuiElement;
import eu.hydrologis.jgrass.featureeditor.xml.annotatedguis.FormGuiFactory;

@SuppressWarnings("nls")
public class FormGuiCreator {

    private Control makeGui( Composite mainComposite, Form form ) {
        CTabFolder folder = new CTabFolder(mainComposite, SWT.BOTTOM);
        folder.setUnselectedCloseVisible(false);
        // folder.setSimple(false);
        folder.setLayout(new MigLayout("fill"));
        folder.setLayoutData("grow");

        List<Tab> orderedTabs = form.getOrderedTabs();
        for( Tab tab : orderedTabs ) {
            CTabItem item = new CTabItem(folder, SWT.NONE);
            item.setText(tab.text);

            Composite tabComposite = new Composite(folder, SWT.NONE);
            tabComposite.setLayoutData("grow");
            tabComposite.setLayout(new MigLayout(tab.layoutConstraints, tab.colConstraints));
            item.setControl(tabComposite);

            List< ? extends FormElement> orderedElements = tab.getOrderedElements();
            for( FormElement orderedGuiElement : orderedElements ) {

                FormGuiElement formGui = FormGuiFactory.createFormGui(orderedGuiElement);
                formGui.makeGui(tabComposite);
            }

        }

        folder.setSize(400, 200);

        return null;
    }

    public static void main( String[] args ) throws IOException {
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setLayout(new FillLayout());

        Composite mainComposite = new Composite(shell, SWT.NONE);
        mainComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        mainComposite.setLayout(new MigLayout("fill"));

        Form form = getForm();
        Control c = new FormGuiCreator().makeGui(mainComposite, form);

        shell.setSize(400, 400);
        shell.open();
        while( !shell.isDisposed() ) {
            if (!display.readAndDispatch())
                display.sleep();
        }
        display.dispose();
    }

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

    private static Form getForm() throws IOException {
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
        return form;
    }

}

/*
 * JGrass - Free Open Source Java GIS http://www.jgrass.org 
 * (C) HydroloGIS - www.hydrologis.com 
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Library General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Library General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Library General Public License
 * along with this library; if not, write to the Free Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package eu.hydrologis.jgrass.featureeditor.xml.annotatedguis;

import java.util.List;

import net.miginfocom.swt.MigLayout;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import eu.hydrologis.jgrass.featureeditor.xml.annotated.Form;
import eu.hydrologis.jgrass.featureeditor.xml.annotated.FormElement;
import eu.hydrologis.jgrass.featureeditor.xml.annotated.Tab;

/**
 * A class representing the main tabbed component gui.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class FormGui extends FormGuiElement {
    private final Form form;

    public FormGui( Form form ) {
        this.form = form;
    }

    @Override
    public Control makeGui( Composite parent ) {
        CTabFolder folder = new CTabFolder(parent, SWT.BOTTOM);
        folder.setUnselectedCloseVisible(false);
        // folder.setSimple(false);
        folder.setLayout(new MigLayout("fill"));
        folder.setLayoutData("grow");

        List<Tab> orderedTabs = form.getOrderedTabs();
        boolean first = true;
        for( Tab tab : orderedTabs ) {
            CTabItem item = new CTabItem(folder, SWT.NONE);
            item.setText(tab.text);
            if (first) {
                folder.setSelection(item);
                first = false;
            }

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

        return folder;
    }
}

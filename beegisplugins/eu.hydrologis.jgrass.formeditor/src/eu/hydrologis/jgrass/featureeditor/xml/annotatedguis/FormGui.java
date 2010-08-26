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
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
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
        // parent has FillLayout

        // create the tab folder
        final CTabFolder folder = new CTabFolder(parent, SWT.BOTTOM);
        folder.setUnselectedCloseVisible(false);
        folder.setLayout(new FillLayout());
        
        // for every Tab object create a tab
        List<Tab> orderedTabs = form.getOrderedTabs();
        boolean first = true;
        for( Tab orderedTab : orderedTabs ) {

            // the tabitem
            CTabItem tab = new CTabItem(folder, SWT.NONE);
            tab.setText(orderedTab.text);
            if (first) {
                // select the first tab
                folder.setSelection(tab);
                first = false;
            }
            
            // we want the content to scroll
            final ScrolledComposite scroller = new ScrolledComposite(folder, SWT.V_SCROLL);
            scroller.setLayout(new FillLayout());
            
            // the actual content of the tab
            Composite tabComposite = new Composite(scroller, SWT.NONE);
            tabComposite.setLayout(new MigLayout(orderedTab.layoutConstraints, orderedTab.colConstraints));

            // which goes as content to the scrolled composite
            scroller.setContent(tabComposite);
            scroller.setExpandVertical(true);
            scroller.setExpandHorizontal(true);
            scroller.setMinHeight(folder.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
            scroller.addControlListener(new ControlAdapter(){
                public void controlResized( ControlEvent e ) {
                    // recalculate height in case the resize makes texts 
                    // wrap or things happen that require it
                    Rectangle r = scroller.getClientArea();
                    scroller.setMinHeight(folder.computeSize(SWT.DEFAULT, r.height).y);
                }
            });
            
            // the scroller gets the control of the tab item
            tab.setControl(scroller);

            
            // add things to the tab composite
            List< ? extends FormElement> orderedElements = orderedTab.getOrderedElements();
            for( FormElement orderedGuiElement : orderedElements ) {
                FormGuiElement formGui = FormGuiFactory.createFormGui(orderedGuiElement);
                formGui.makeGui(tabComposite);
            }
        }

        return folder;
    }
}

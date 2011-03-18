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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import net.miginfocom.swt.MigLayout;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.opengis.feature.simple.SimpleFeature;

import eu.hydrologis.jgrass.featureeditor.xml.annotated.AForm;
import eu.hydrologis.jgrass.featureeditor.xml.annotated.ATab;
import eu.hydrologis.jgrass.featureeditor.xml.annotated.FormElement;

/**
 * A class representing the main tabbed component gui.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class AFormGui {
    private final AForm form;

    private FormGuiFactory formGuiFactory = new FormGuiFactory();

    public AFormGui( AForm form ) {
        this.form = form;
    }

    public Control makeGui( Composite parent ) {
        // parent has FillLayout

        // create the tab folder
        final CTabFolder folder = new CTabFolder(parent, SWT.BOTTOM);
        folder.setUnselectedCloseVisible(false);
        folder.setLayout(new FillLayout());

        // for every Tab object create a tab
        List<ATab> orderedTabs = form.getOrderedTabs();
        boolean first = true;
        for( ATab orderedTab : orderedTabs ) {

            // the tabitem
            CTabItem tab = new CTabItem(folder, SWT.NONE);
            tab.setText(orderedTab.text);
            if (first) {
                // select the first tab
                folder.setSelection(tab);
                first = false;
            }

            // we want the content to scroll
            final ScrolledComposite scroller = new ScrolledComposite(folder, SWT.V_SCROLL | SWT.H_SCROLL);
            scroller.setLayout(new FillLayout());
            GridData scrollerGD = new GridData(SWT.FILL, SWT.FILL, true, false);
            scrollerGD.widthHint = SWT.DEFAULT;
            scrollerGD.heightHint = SWT.DEFAULT;
            scroller.setLayoutData(scrollerGD);

            // the actual content of the tab
            Composite tabComposite = new Composite(scroller, SWT.NONE);
            tabComposite.setLayout(new MigLayout(orderedTab.layoutConstraints, orderedTab.colConstraints));

            // which goes as content to the scrolled composite
            scroller.setContent(tabComposite);
            scroller.setExpandVertical(true);
            scroller.setExpandHorizontal(true);

            // the scroller gets the control of the tab item
            tab.setControl(scroller);

            // add things to the tab composite
            List< ? extends FormElement> orderedElements = orderedTab.getOrderedElements();
            fixEmptyShifts(orderedElements);

            for( FormElement orderedGuiElement : orderedElements ) {
                FormGuiElement formGui = formGuiFactory.createFormGui(orderedGuiElement);
                formGui.makeGui(tabComposite);
            }

            Point size = tabComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT);
            scroller.setMinHeight(size.y);
            scroller.setMinWidth(size.x);
        }

        return folder;
    }

    public void setFeature( SimpleFeature feature ) {
        // set in kids
        HashMap<String, FormGuiElement> fieldNames2GuiElementsMap = formGuiFactory.getFieldNames2GuiElementsMap();
        Collection<FormGuiElement> guiElements = fieldNames2GuiElementsMap.values();
        for( FormGuiElement formGuiElement : guiElements ) {
            formGuiElement.setFeature(feature);
        }
    }

    public FormElement getFormElement() {
        return form;
    }

    private void fixEmptyShifts( List< ? extends FormElement> orderedElements ) {
        /*
         * cut off the empty cells before, based on the fact that
         * the positional factor has cell x y xspan, yspan in it.
         * If it doesn't, the form is supposed to have been created 
         * manually and is ok like that.
         */
        if (orderedElements.size() > 0) {
            int xShift = 0;
            int yShift = 0;
            for( int i = 0; i < orderedElements.size(); i++ ) {
                if (i == 0) {
                    // get the shift
                    FormElement formElement = orderedElements.get(i);
                    String constraints = formElement.getConstraints();
                    String[] constraintsSplit = constraints.split(",");
                    for( int j = 0; j < constraintsSplit.length; j++ ) {
                        String candidate = constraintsSplit[j].trim();
                        if (candidate.startsWith("cell")) {
                            String[] cellSplit = candidate.split("\\s+");
                            xShift = Integer.parseInt(cellSplit[1]);
                            yShift = Integer.parseInt(cellSplit[2]);
                            cellSplit[1] = "0";
                            cellSplit[2] = "0";
                            String newCandidate = arrayToString(cellSplit, " ");
                            constraintsSplit[j] = newCandidate;
                        }
                    }
                    String newConstraints = arrayToString(constraintsSplit, ",");
                    formElement.setConstraints(newConstraints);
                } else {
                    // shift any other
                    FormElement formElement = orderedElements.get(i);
                    String constraints = formElement.getConstraints();
                    String[] constraintsSplit = constraints.split(",");
                    for( int j = 0; j < constraintsSplit.length; j++ ) {
                        String candidate = constraintsSplit[j].trim();
                        if (candidate.startsWith("cell")) {
                            String[] cellSplit = candidate.split("\\s+");
                            int tmpxShift = Integer.parseInt(cellSplit[1]);
                            int tmpyShift = Integer.parseInt(cellSplit[2]);
                            tmpxShift = tmpxShift - xShift;
                            tmpyShift = tmpyShift - yShift;
                            cellSplit[1] = "" + tmpxShift;
                            cellSplit[2] = "" + tmpyShift;
                            String newCandidate = arrayToString(cellSplit, " ");
                            constraintsSplit[j] = newCandidate;
                        }
                    }
                    String newConstraints = arrayToString(constraintsSplit, ",");
                    formElement.setConstraints(newConstraints);
                }
            }
        }
    }

    public static String arrayToString( String[] strs, String delimiter ) {
        if (strs.length == 0) {
            return "";
        }
        StringBuffer sbuf = new StringBuffer();
        sbuf.append(strs[0]);
        for( int idx = 1; idx < strs.length; idx++ ) {
            sbuf.append(delimiter);
            sbuf.append(strs[idx]);
        }
        return sbuf.toString();
    }
}

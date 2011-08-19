/*
 * JGrass - Free Open Source Java GIS http://www.jgrass.org 
 * (C) HydroloGIS - www.hydrologis.com 
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package eu.hydrologis.jgrass.annotationlayer;

import java.awt.Color;
import java.util.List;

import net.refractions.udig.project.ui.ApplicationGIS;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import eu.hydrologis.jgrass.annotationlayer.mapgraphic.AnnotationLayerMapGraphic;
import eu.hydrologis.jgrass.beegisutils.BeegisUtilsPlugin;
import eu.hydrologis.jgrass.beegisutils.jgrassported.DressedWorldStroke;
import eu.hydrologis.jgrass.beegisutils.jgrassported.ImageCombo;
import eu.hydrologis.jgrass.beegisutils.jgrassported.JGrassCatalogUtilities;

/**
 * The view for properties annotations.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class AnnotationsPropertiesView extends ViewPart {

    public static final String ID = "eu.hydrologis.jgrass.annotationlayer.AnnotationsPropertiesView"; //$NON-NLS-1$

    /**
     * The list of possible strokes to choose.
     */
    private final int[] STROKES = {1, 3, 10, 20, 40};
    /**
     * The list of possible stroke transparencies to choose.
     */
    private final int[] ALPHAS = {20, 40, 60, 80, 100};

    public AnnotationsPropertiesView() {
    }

    public void createPartControl( Composite parent ) {

        Composite propsComposite = new Composite(parent, SWT.None);
        propsComposite.setLayout(new RowLayout());
        propsComposite.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL));

        // stroke width
        Image img1 = AbstractUIPlugin.imageDescriptorFromPlugin(BeegisUtilsPlugin.PLUGIN_ID, "/icons/strokewidth_1.png")
                .createImage();
        Image img2 = AbstractUIPlugin.imageDescriptorFromPlugin(BeegisUtilsPlugin.PLUGIN_ID, "/icons/strokewidth_2.png")
                .createImage();
        Image img3 = AbstractUIPlugin.imageDescriptorFromPlugin(BeegisUtilsPlugin.PLUGIN_ID, "/icons/strokewidth_3.png")
                .createImage();
        Image img4 = AbstractUIPlugin.imageDescriptorFromPlugin(BeegisUtilsPlugin.PLUGIN_ID, "/icons/strokewidth_4.png")
                .createImage();
        Image img5 = AbstractUIPlugin.imageDescriptorFromPlugin(BeegisUtilsPlugin.PLUGIN_ID, "/icons/strokewidth_5.png")
                .createImage();

        Composite strokeComposite = new Composite(propsComposite, SWT.None);
        strokeComposite.setLayout(new GridLayout(2, false));
        final ImageCombo strokeWidthCombo = new ImageCombo(strokeComposite, SWT.READ_ONLY);
        GridData gridDataWidth = new GridData(SWT.FILL, SWT.FILL, true, true);
        gridDataWidth.widthHint = 30;
        strokeWidthCombo.setLayoutData(gridDataWidth);
        strokeWidthCombo.add("1", img1);
        strokeWidthCombo.add("2", img2);
        strokeWidthCombo.add("3", img3);
        strokeWidthCombo.add("4", img4);
        strokeWidthCombo.add("5", img5);
        strokeWidthCombo.select(0);
        strokeWidthCombo.setToolTipText("stroke width");
        strokeWidthCombo.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected( SelectionEvent e ) {
                int selectedIndex = strokeWidthCombo.getSelectionIndex();
                int strokeWidth = STROKES[selectedIndex];

                AnnotationPlugin.getDefault().setCurrentStrokeWidth(strokeWidth);
                double scale = ApplicationGIS.getActiveMap().getViewportModel().getScaleDenominator();
                AnnotationPlugin.getDefault().setCurrentScale(scale);
            }
        });

        // alpha
        final Combo alphaCombo = new Combo(strokeComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
        GridData gridData2 = new GridData(SWT.FILL, SWT.FILL, true, true);
        alphaCombo.setLayoutData(gridData2);
        String[] items = new String[ALPHAS.length];
        for( int i = 0; i < items.length; i++ ) {
            items[i] = ALPHAS[i] + "%";
        }
        alphaCombo.setItems(items);
        alphaCombo.select(ALPHAS.length - 1);
        alphaCombo.setToolTipText("stroke alpha");
        alphaCombo.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected( SelectionEvent e ) {
                int selectedIndex = alphaCombo.getSelectionIndex();
                int alphaInPercent = ALPHAS[selectedIndex];
                int strokeAlpha = 255 * alphaInPercent / 100;

                Color c = AnnotationPlugin.getDefault().getCurrentStrokeColor();
                AnnotationPlugin.getDefault()
                        .setCurrentStrokeColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), strokeAlpha));
            }
        });

        // color
        // final Label strokeColLabel = new Label(propsComposite, SWT.NONE);
        // strokeColLabel.setText("Stroke color");
        final ColorSelector cs = new ColorSelector(propsComposite);
        RowData rd1 = new RowData();
        rd1.height = 30;
        rd1.width = 50;
        cs.getButton().setLayoutData(rd1);
        // new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
        int[] cc = AnnotationPlugin.getDefault().getCurrentStrokeColorInt();
        cs.setColorValue(new RGB(cc[0], cc[1], cc[2]));
        cs.getButton().setToolTipText("stroke color");
        cs.getButton().addSelectionListener(new SelectionAdapter(){
            public void widgetSelected( SelectionEvent e ) {
                RGB rgb = cs.getColorValue();
                int alpha = AnnotationPlugin.getDefault().getCurrentStrokeColorInt()[3];
                AnnotationPlugin.getDefault().setCurrentStrokeColor(new Color(rgb.red, rgb.green, rgb.blue, alpha));
            }
        });

        // clear all
        ImageDescriptor clearID = AbstractUIPlugin.imageDescriptorFromPlugin(AnnotationPlugin.PLUGIN_ID, "icons/trash.gif"); //$NON-NLS-1$
        final Button clearButton = new Button(propsComposite, SWT.PUSH);
        // GridData gd1 = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
        // clearButton.setLayoutData(gd1);
        RowData rd2 = new RowData();
        rd2.height = 30;
        rd2.width = 50;
        clearButton.setLayoutData(rd2);
        clearButton.setImage(clearID.createImage());
        clearButton.setToolTipText("clear the area from all drawings");
        clearButton.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected( SelectionEvent e ) {
                boolean answer = MessageDialog.openQuestion(clearButton.getShell(), "Removal warning",
                        "Do you really want to remove all annotations?");
                if (!answer) {
                    return;
                }

                AnnotationPlugin.getDefault().getStrokes().clear();
                JGrassCatalogUtilities.getMapgraphicLayerByClass(AnnotationLayerMapGraphic.class).refresh(null);
            }
        });

        // clear last
        ImageDescriptor clearLast = AbstractUIPlugin.imageDescriptorFromPlugin(AnnotationPlugin.PLUGIN_ID, "icons/trashlast.gif"); //$NON-NLS-1$
        Button clearLastButton = new Button(propsComposite, SWT.PUSH);
        RowData rd3 = new RowData();
        rd3.height = 30;
        rd3.width = 50;
        clearLastButton.setLayoutData(rd3);
        // GridData gd2 = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
        // clearLastButton.setLayoutData(gd2);
        clearLastButton.setImage(clearLast.createImage());
        clearLastButton.setToolTipText("remove last stroke from annotations layer");
        clearLastButton.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected( SelectionEvent e ) {
                List<DressedWorldStroke> strokes = AnnotationPlugin.getDefault().getStrokes();
                int size = strokes.size();
                if (size < 1)
                    return;
                strokes.remove(size - 1);
                JGrassCatalogUtilities.getMapgraphicLayerByClass(AnnotationLayerMapGraphic.class).refresh(null);
            }
        });

    }
    @Override
    public void setFocus() {
    }

}

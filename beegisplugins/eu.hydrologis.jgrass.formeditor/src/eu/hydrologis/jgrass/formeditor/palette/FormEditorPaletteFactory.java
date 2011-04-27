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
package eu.hydrologis.jgrass.formeditor.palette;

import org.eclipse.gef.palette.CombinedTemplateCreationEntry;
import org.eclipse.gef.palette.MarqueeToolEntry;
import org.eclipse.gef.palette.PaletteContainer;
import org.eclipse.gef.palette.PaletteDrawer;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.palette.PaletteToolbar;
import org.eclipse.gef.palette.PanningSelectionToolEntry;
import org.eclipse.gef.palette.ToolEntry;
import org.eclipse.gef.requests.SimpleFactory;
import org.eclipse.jface.resource.ImageDescriptor;

import eu.hydrologis.jgrass.formeditor.FormEditorPlugin;
import eu.hydrologis.jgrass.formeditor.model.widgets.CheckBoxWidget;
import eu.hydrologis.jgrass.formeditor.model.widgets.ComboBoxWidget;
import eu.hydrologis.jgrass.formeditor.model.widgets.LabelWidget;
import eu.hydrologis.jgrass.formeditor.model.widgets.RadioButtonWidget;
import eu.hydrologis.jgrass.formeditor.model.widgets.SeparatorWidget;
import eu.hydrologis.jgrass.formeditor.model.widgets.TextAreaWidget;
import eu.hydrologis.jgrass.formeditor.model.widgets.TextFieldWidget;
import eu.hydrologis.jgrass.formeditor.utils.ImageCache;

public final class FormEditorPaletteFactory {

    /** Create the "Shapes" drawer. */
    private static PaletteContainer createWidgetsDrawer() {
        PaletteDrawer componentsDrawer = new PaletteDrawer("Widgets");

        ImageDescriptor separatorId = FormEditorPlugin.imageDescriptorFromPlugin(FormEditorPlugin.PLUGIN_ID,
                ImageCache.SEPARATOR_ICON_16);
        ImageDescriptor separatorId24 = FormEditorPlugin.imageDescriptorFromPlugin(FormEditorPlugin.PLUGIN_ID,
                ImageCache.SEPARATOR_ICON_24);

        CombinedTemplateCreationEntry component = new CombinedTemplateCreationEntry("Separator", "Create a spacer or separator",
                SeparatorWidget.class, new SimpleFactory(SeparatorWidget.class), separatorId, separatorId24);
        componentsDrawer.add(component);

        ImageDescriptor labelId = FormEditorPlugin
                .imageDescriptorFromPlugin(FormEditorPlugin.PLUGIN_ID, ImageCache.LABEL_ICON_16);
        ImageDescriptor labelId24 = FormEditorPlugin.imageDescriptorFromPlugin(FormEditorPlugin.PLUGIN_ID,
                ImageCache.LABEL_ICON_24);

        component = new CombinedTemplateCreationEntry("Label", "Create a label", LabelWidget.class,
                new SimpleFactory(LabelWidget.class), labelId, labelId24);
        componentsDrawer.add(component);

        ImageDescriptor textId = FormEditorPlugin.imageDescriptorFromPlugin(FormEditorPlugin.PLUGIN_ID, ImageCache.TEXT_ICON_16);
        ImageDescriptor textId24 = FormEditorPlugin
                .imageDescriptorFromPlugin(FormEditorPlugin.PLUGIN_ID, ImageCache.TEXT_ICON_24);

        component = new CombinedTemplateCreationEntry("Textfield", "Create an text field", TextFieldWidget.class,
                new SimpleFactory(TextFieldWidget.class), textId, textId24);
        componentsDrawer.add(component);

        ImageDescriptor textAreaId = FormEditorPlugin.imageDescriptorFromPlugin(FormEditorPlugin.PLUGIN_ID,
                ImageCache.TEXTAREA_ICON_16);
        ImageDescriptor textAreaId24 = FormEditorPlugin.imageDescriptorFromPlugin(FormEditorPlugin.PLUGIN_ID,
                ImageCache.TEXTAREA_ICON_24);

        component = new CombinedTemplateCreationEntry("Textarea", "Create a textarea", TextAreaWidget.class, new SimpleFactory(
                TextAreaWidget.class), textAreaId, textAreaId24);
        componentsDrawer.add(component);

        ImageDescriptor comboId = FormEditorPlugin
                .imageDescriptorFromPlugin(FormEditorPlugin.PLUGIN_ID, ImageCache.COMBO_ICON_16);
        ImageDescriptor comboId24 = FormEditorPlugin.imageDescriptorFromPlugin(FormEditorPlugin.PLUGIN_ID,
                ImageCache.COMBO_ICON_24);

        component = new CombinedTemplateCreationEntry("Combobox", "Create a combo", ComboBoxWidget.class, new SimpleFactory(
                ComboBoxWidget.class), comboId, comboId24);
        componentsDrawer.add(component);

        ImageDescriptor checkId = FormEditorPlugin
                .imageDescriptorFromPlugin(FormEditorPlugin.PLUGIN_ID, ImageCache.CHECK_ICON_16);
        ImageDescriptor checkId24 = FormEditorPlugin.imageDescriptorFromPlugin(FormEditorPlugin.PLUGIN_ID,
                ImageCache.CHECK_ICON_24);

        component = new CombinedTemplateCreationEntry("Checkbox", "Create a check", CheckBoxWidget.class, new SimpleFactory(
                CheckBoxWidget.class), checkId, checkId24);
        componentsDrawer.add(component);

        ImageDescriptor radioId = FormEditorPlugin
                .imageDescriptorFromPlugin(FormEditorPlugin.PLUGIN_ID, ImageCache.RADIO_ICON_16);
        ImageDescriptor radioId24 = FormEditorPlugin.imageDescriptorFromPlugin(FormEditorPlugin.PLUGIN_ID,
                ImageCache.RADIO_ICON_24);

        component = new CombinedTemplateCreationEntry("Radio", "Create a radio button", RadioButtonWidget.class,
                new SimpleFactory(RadioButtonWidget.class), radioId, radioId24);
        componentsDrawer.add(component);

        // palette open by default at the begin
        componentsDrawer.setInitialState(PaletteDrawer.INITIAL_STATE_OPEN);
        
        return componentsDrawer;
    }

    /**
     * Creates the PaletteRoot and adds all palette elements.
     * Use this factory method to create a new palette for your graphical editor.
     * @return a new PaletteRoot
     */
    public static PaletteRoot createPalette() {
        PaletteRoot palette = new PaletteRoot();
        palette.add(createToolsGroup(palette));
        palette.add(createWidgetsDrawer());
        return palette;
    }

    /** Create the "Tools" group. */
    private static PaletteContainer createToolsGroup( PaletteRoot palette ) {
        PaletteToolbar toolbar = new PaletteToolbar("Tools");

        // Add a selection tool to the group
        ToolEntry tool = new PanningSelectionToolEntry();
        toolbar.add(tool);
        palette.setDefaultEntry(tool);

        // Add a marquee tool to the group
        toolbar.add(new MarqueeToolEntry());

        // ImageDescriptor connect16 = FormEditorPlugin.imageDescriptorFromPlugin(
        // FormEditorPlugin.PLUGIN_ID, "icons/connection_s16.gif");
        // ImageDescriptor connect24 = FormEditorPlugin.imageDescriptorFromPlugin(
        // FormEditorPlugin.PLUGIN_ID, "icons/connection_s24.gif");
        //
        // // Add (solid-line) connection tool
        // tool = new ConnectionCreationToolEntry("Solid connection",
        // "Create a solid-line connection", new CreationFactory(){
        // public Object getNewObject() {
        // return null;
        // }
        // // see ShapeEditPart#createEditPolicies()
        // // this is abused to transmit the desired line style
        // public Object getObjectType() {
        // return Connection.SOLID_CONNECTION;
        // }
        // }, connect16, connect24);
        // toolbar.add(tool);

        // Add (dashed-line) connection tool
        // tool = new ConnectionCreationToolEntry("Dashed connection",
        // "Create a dashed-line connection", new CreationFactory(){
        // public Object getNewObject() {
        // return null;
        // }
        // // see ShapeEditPart#createEditPolicies()
        // // this is abused to transmit the desired line style
        // public Object getObjectType() {
        // return Connection.DASHED_CONNECTION;
        // }
        // }, ImageDescriptor.createFromFile(ShapesPlugin.class, "icons/connection_d16.gif"),
        // ImageDescriptor.createFromFile(ShapesPlugin.class, "icons/connection_d24.gif"));
        // toolbar.add(tool);

        return toolbar;
    }

    /** Utility class. */
    private FormEditorPaletteFactory() {
        // Utility class
    }

}
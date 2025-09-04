
package com.mdwriter.app.plugins;

import java.util.List;
import org.pf4j.CompoundPluginDescriptorFinder;
import org.pf4j.DefaultPluginManager;
import org.pf4j.ManifestPluginDescriptorFinder;
import org.pf4j.PluginManager;
import com.mdwriter.api.ToolBarButton;

public class ButtonPlugin {

  public List<ToolBarButton> buttons;

  /**
   * MenuBar class to load toolbar buttons using PF4J plugins.
   * 
   * This class initializes buttons with their respective icons and
   * functionalities.
   *
   *
   */
  public ButtonPlugin() {

    final PluginManager pluginManager = new DefaultPluginManager() {
      @Override
      protected CompoundPluginDescriptorFinder createPluginDescriptorFinder() {
        return new CompoundPluginDescriptorFinder()
            .add(new ManifestPluginDescriptorFinder());
      }
    };

    // load the plugins
    pluginManager.loadPlugins();

    pluginManager.startPlugins();

    buttons = pluginManager.getExtensions(ToolBarButton.class);
    pluginManager.stopPlugins();
  }
}

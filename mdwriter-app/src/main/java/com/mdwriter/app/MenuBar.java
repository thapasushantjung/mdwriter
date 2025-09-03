package com.mdwriter.app;

import org.slf4j.Logger;
import java.util.List;
import org.pf4j.CompoundPluginDescriptorFinder;
import org.pf4j.DefaultPluginManager;
import org.pf4j.ManifestPluginDescriptorFinder;
import org.pf4j.PluginManager;
import com.mdwriter.api.ToolBarButton;

public class MenuBar {

  List<ToolBarButton> buttons;

  public MenuBar() {

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

/*
 * Copyright (C) 2012-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.pf4j.demo.hello;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mdwriter.api.ToolBarButton;

import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.feather.Feather;
import org.pf4j.Extension;
import org.pf4j.Plugin;
import org.pf4j.PluginWrapper;

/**
 * A very simple plugin.
 *
 * @author Decebal Suiu
 */
public class PluginItalics extends Plugin {
  private static final Logger logger = LoggerFactory.getLogger(PluginItalics.class);

  public PluginItalics(PluginWrapper wrapper) {
    super(wrapper);
  }

  @Override
  public void start() {
    logger.info("ItalicsPlugin.start()");
  }

  @Override
  public void stop() {
    logger.info("ItalicsPlugin.stop()");
  }

  @Extension(ordinal = 1)
  public static class BoldButton implements ToolBarButton {
    @Override
    public Ikon getIcon() {
      return Feather.ITALIC;

    }

    @Override
    public String changeText(String text) {
      return "*" + text + "*";
    }
  }

}

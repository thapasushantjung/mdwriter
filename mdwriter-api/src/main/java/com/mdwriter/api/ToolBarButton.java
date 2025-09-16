
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
package com.mdwriter.api;

import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.javafx.FontIcon;
import org.pf4j.ExtensionPoint;
import atlantafx.base.theme.Styles;
import javafx.scene.control.Button;

/**
 * @author Decebal Suiu
 */
public interface ToolBarButton extends ExtensionPoint {

  Ikon getIcon();

  String changeText(String text);

  default Button iconButton() {
    var btn = new Button(null);
    if (this.getIcon() != null) {
      btn.setGraphic(new FontIcon(this.getIcon()));
    }
    btn.getStyleClass().addAll(Styles.BUTTON_ICON);
    btn.setUserData(this);
    return btn;
  }
}

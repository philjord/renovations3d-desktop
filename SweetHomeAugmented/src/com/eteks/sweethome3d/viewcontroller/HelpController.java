/*
 * HelpController.java 20 juil. 07
 *
 * Sweet Home 3D, Copyright (c) 2024 Space Mushrooms <info@sweethome3d.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package com.eteks.sweethome3d.viewcontroller;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import com.eteks.sweethome3d.model.UserPreferences;

/**
 *Buggered with to make proguard work, this class no longer does anything useful
 */
public class HelpController implements Controller {

	  public enum Property {HELP_PAGE, BROWSER_PAGE, 
	      PREVIOUS_PAGE_ENABLED, NEXT_PAGE_ENABLED, HIGHLIGHTED_TEXT}
  private final UserPreferences       preferences;
  private final ViewFactory           viewFactory;
  private final PropertyChangeSupport propertyChangeSupport;
  private int                         historyIndex;
  private HelpView                    helpView;
  

  
  public HelpController(UserPreferences preferences, 
                        ViewFactory viewFactory) {
    this.preferences = preferences;
    this.viewFactory = viewFactory;    
    this.propertyChangeSupport = new PropertyChangeSupport(this);
    this.historyIndex = -1;

  }

  /**
   * Returns the view associated with this controller.
   */
  public HelpView getView() {
    if (this.helpView == null) {
      this.helpView = this.viewFactory.createHelpView(this.preferences, this);

    }
    return this.helpView;
  }

  /**
   * Displays the help view controlled by this controller. 
   */
  public void displayView() {
    getView().displayView();
  }

  /**
   * Adds the property change <code>listener</code> in parameter to this controller.
   */
  public void addPropertyChangeListener(Property property, PropertyChangeListener listener) {
    this.propertyChangeSupport.addPropertyChangeListener(property.name(), listener);
  }

  /**
   * Removes the property change <code>listener</code> in parameter from this controller.
   */
  public void removePropertyChangeListener(Property property, PropertyChangeListener listener) {
    this.propertyChangeSupport.removePropertyChangeListener(property.name(), listener);
  }



}

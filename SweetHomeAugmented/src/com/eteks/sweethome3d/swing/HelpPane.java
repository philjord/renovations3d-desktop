/*
 * HelpPane.java 20 juil. 07
 *
 * Sweet Home 3D, Copyright (c) 2007 Emmanuel PUYBARET / eTeks <info@eteks.com>
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
package com.eteks.sweethome3d.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.Locale;

import javax.swing.ActionMap;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;

import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.tools.OperatingSystem;
import com.eteks.sweethome3d.viewcontroller.HelpController;
import com.eteks.sweethome3d.viewcontroller.HelpView;

/**
 *Buggered with to make proguard work, this class no longer does anything useful
 * @author Emmanuel Puybaret
 */
public class HelpPane extends JRootPane implements HelpView {
  private enum ActionType {SHOW_PREVIOUS, SHOW_NEXT, SEARCH, CLOSE}

  private final UserPreferences preferences;
  private JFrame                frame;
  private JLabel                searchLabel;
  private JTextField            searchTextField;
  private JEditorPane           helpEditorPane;
  
  public HelpPane(UserPreferences preferences, 
                  final HelpController controller) {
    this.preferences = preferences;
    createActions(preferences, controller);
    createComponents(preferences, controller);
    setMnemonics(preferences);
    layoutComponents();
    addLanguageListener(preferences);
    if (controller != null) {
      addHyperlinkListener(controller);
      installKeyboardActions();
    }
    
   
  }

  /** 
   * Creates actions bound to <code>controller</code>.
   */
  private void createActions(UserPreferences preferences, 
                             final HelpController controller) {

  }

  /**
   * Adds a property change listener to <code>preferences</code> to update
   * actions when preferred language changes.
   */
  private void addLanguageListener(UserPreferences preferences) {
    preferences.addPropertyChangeListener(UserPreferences.Property.LANGUAGE, 
        new LanguageChangeListener(this));
  }

  /**
   * Preferences property listener bound to this component with a weak reference to avoid
   * strong link between preferences and this component.  
   */
  private static class LanguageChangeListener implements PropertyChangeListener {
    private WeakReference<HelpPane> helpPane;

    public LanguageChangeListener(HelpPane helpPane) {
      this.helpPane = new WeakReference<HelpPane>(helpPane);
    }
    
    public void propertyChange(PropertyChangeEvent ev) {
      // If help pane was garbage collected, remove this listener from preferences
      HelpPane helpPane = this.helpPane.get();
      UserPreferences preferences = (UserPreferences)ev.getSource();
      if (helpPane == null) {
        preferences.removePropertyChangeListener(
            UserPreferences.Property.LANGUAGE, this);
      } else {
        // Update frame title and search label with new locale
        if (helpPane.frame != null) {
          helpPane.frame.setTitle(preferences.getLocalizedString(HelpPane.class, "helpFrame.title"));
          helpPane.frame.applyComponentOrientation(
              ComponentOrientation.getOrientation(Locale.getDefault()));
        }
        helpPane.searchLabel.setText(SwingTools.getLocalizedLabelText(preferences, HelpPane.class, "searchLabel.text"));
        helpPane.searchTextField.setText("");
        helpPane.setMnemonics(preferences);
      }
    }
  }
  
  /**
   * Creates the components displayed by this view.
  */
  private void createComponents(UserPreferences preferences, final HelpController controller) {
   
  }


  /**
   * Sets components mnemonics and label / component associations.
   */
  private void setMnemonics(UserPreferences preferences) {
    if (!OperatingSystem.isMacOSX()) {
      this.searchLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(
          preferences.getLocalizedString(HelpPane.class, "searchLabel.mnemonic")).getKeyCode());
      this.searchLabel.setLabelFor(this.searchTextField);
    }
  }
  
  /**
   * Layouts the components displayed by this view.
   */
  private void layoutComponents() {
    final JToolBar toolBar = new JToolBar();
    toolBar.setFloatable(false);
    // Change layout because BoxLayout glue doesn't work well under Linux
    toolBar.setLayout(new GridBagLayout());
    ActionMap actions = getActionMap();    
    final JButton previousButton = new JButton(actions.get(ActionType.SHOW_PREVIOUS));
    final JButton nextButton = new JButton(actions.get(ActionType.SHOW_NEXT));
    toolBar.add(previousButton);
    toolBar.add(nextButton);
    layoutToolBarButtons(toolBar, previousButton, nextButton);
    toolBar.addPropertyChangeListener("componentOrientation", 
        new PropertyChangeListener () {
          public void propertyChange(PropertyChangeEvent evt) {
            layoutToolBarButtons(toolBar, previousButton, nextButton);
          }
        });
    toolBar.add(new JLabel(),
        new GridBagConstraints(2, 0, 1, 1, 1, 0, GridBagConstraints.CENTER, 
            GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    
    int standardGap = Math.round(5 * SwingTools.getResolutionScale());
    if (!OperatingSystem.isMacOSXLeopardOrSuperior()) {
      toolBar.add(this.searchLabel,
          new GridBagConstraints(3, 0, 1, 1, 0, 0, GridBagConstraints.CENTER, 
              GridBagConstraints.NONE, new Insets(0, 0, 0, standardGap), 0, 0));
    }
    toolBar.add(this.searchTextField,
        new GridBagConstraints(4, 0, 1, 1, 0, 0, GridBagConstraints.CENTER, 
            GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
    this.searchTextField.setMaximumSize(this.searchTextField.getPreferredSize());
    // Ignore search button under Mac OS X 10.5 (it's included in the search field)
    if (!OperatingSystem.isMacOSXLeopardOrSuperior()) {
      toolBar.add(new JButton(actions.get(ActionType.SEARCH)),
          new GridBagConstraints(5, 0, 1, 1, 0, 0, GridBagConstraints.CENTER, 
              GridBagConstraints.NONE, new Insets(0, standardGap, 0, 0), 0, 0));
    }
    // Remove focusable property on buttons
    for (int i = 0, n = toolBar.getComponentCount(); i < n; i++) {      
      Component component = toolBar.getComponent(i);
      if (component instanceof JButton) {
        component.setFocusable(false);
      }
    }
    
    getContentPane().add(toolBar, BorderLayout.NORTH);
    getContentPane().add(new JScrollPane(this.helpEditorPane), BorderLayout.CENTER);
  }

  /**
   * Updates buttons layout and under Mac OS X 10.5 use segmented buttons with properties 
   * depending on toolbar orientation.
   */
  private void layoutToolBarButtons(JToolBar toolBar, 
                                    JButton previousButton,
                                    JButton nextButton) {
    int buttonPadY;
    int buttonsTopBottomInset;
    if (OperatingSystem.isMacOSXLeopardOrSuperior() 
        && OperatingSystem.isJavaVersionGreaterOrEqual("1.7")) {
      // Ensure the top and bottom of segmented buttons are correctly drawn 
      buttonPadY = 6;
      buttonsTopBottomInset = -2;
    } else {
      buttonPadY = 0;
      buttonsTopBottomInset = 0;
    }
    ComponentOrientation orientation = toolBar.getComponentOrientation();
    GridBagLayout layout = (GridBagLayout)toolBar.getLayout();
    GridBagConstraints firstButtonConstraints = new GridBagConstraints(
        0, 0, 1, 1, 0, 0, GridBagConstraints.CENTER, 
        GridBagConstraints.NONE, new Insets(buttonsTopBottomInset, 0, buttonsTopBottomInset, 0), 0, buttonPadY);
    GridBagConstraints secondButtonContraints = new GridBagConstraints(
        1, 0, 1, 1, 0, 0, GridBagConstraints.CENTER, 
        GridBagConstraints.NONE, new Insets(buttonsTopBottomInset, 0, buttonsTopBottomInset, 5), 0, buttonPadY);
    layout.setConstraints(orientation.isLeftToRight() ? previousButton : nextButton, 
        firstButtonConstraints);
    layout.setConstraints(orientation.isLeftToRight() ? nextButton : previousButton, 
        secondButtonContraints);
    // Use segmented buttons under Mac OS X 10.5
    if (OperatingSystem.isMacOSXLeopardOrSuperior()) {
      previousButton.putClientProperty("JButton.buttonType", "segmentedTextured");
      previousButton.putClientProperty("JButton.segmentPosition", "first");
      nextButton.putClientProperty("JButton.buttonType", "segmentedTextured");
      nextButton.putClientProperty("JButton.segmentPosition", "last");
    }
    toolBar.revalidate();
  }
    
  /**
   * Adds an hyperlink listener on the editor pane displayed by this pane.
   */
  private void addHyperlinkListener(final HelpController controller) {
    
  }

  /**
   * Installs keys bound to actions. 
   */
  private void installKeyboardActions() {
  
  }

  /**
   * Displays this pane in a frame.
   */
  public void displayView() {
   
  }
  
  /**
   * Computes <code>frame</code> size and location to fit into screen.
   */
  private void computeFrameBounds(JFrame frame) {
    frame.setLocationByPlatform(true);
    Dimension screenSize = getToolkit().getScreenSize();
    Insets screenInsets = getToolkit().getScreenInsets(getGraphicsConfiguration());
    screenSize.width -= screenInsets.left + screenInsets.right;
    screenSize.height -= screenInsets.top + screenInsets.bottom;
    frame.setSize(Math.min(2 * screenSize.width / 3, Math.round(800 * SwingTools.getResolutionScale())), 
        screenSize.height * 4 / 5);
  }
  
  /**
   * Displays <code>url</code> in this pane.
   */
  private void setPage(URL url) {
    try {
      this.helpEditorPane.setPage(url);
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
  }
}

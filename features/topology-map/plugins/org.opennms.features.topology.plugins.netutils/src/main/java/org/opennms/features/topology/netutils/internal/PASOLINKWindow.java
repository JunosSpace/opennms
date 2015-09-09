package org.opennms.features.topology.netutils.internal;

import java.net.URL;

import org.opennms.features.topology.api.support.InfoWindow;

/**
 * The PASOLINKWindow class constructs a custom Window component that contains an embedded
 * browser displaying the SSH information of the currently selected node
 * @author Yonghua
 * @author Philip Grenon
 * @version 1.0
 */
public class PASOLINKWindow extends InfoWindow {

  private static final long serialVersionUID = -9008855502553868301L;

  /**
   * Label given to vertexes that have no real label.
   */
  private static final String NO_LABEL_TEXT = "no such label";

  /**
     * The PASOLINKWindow method constructs a sub-window instance which can be added to a main window.
     * The sub-window contains an embedded browser which displays the Node Info page of the currently selected
     * node.
     * @param node Selected node
     * @param width Width of the main window
     * @param height Height of the main window
     */
   public PASOLINKWindow(final Node node, final URL nodeURL) {
     super(nodeURL, new LabelCreator() {

      @Override
      public String getLabel() {
        String label = node == null ? "" : node.getLabel();

            /*Sets up window settings*/
            if (label == null || label.equals("") || label.equalsIgnoreCase(NO_LABEL_TEXT)) {
                label = "";
            } else {
              label = " To iPASO Device: " + label;
            }
            return "Go" + label;
      }
    });
   }
}

package org.lrima.laop.utils;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;

import java.io.IOException;
import java.net.URL;

public class Utils {

    /**
     * Returns the loader of the specified .fxml file
     *
     * @param file the filename (without the .fxml)
     * @return the loader
     */
    public static FXMLLoader load(String file) {
        URL url = Utils.class.getResource("/views/configuration/" + file + ".fxml");
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(url);
            loader.load();

            return loader;
        } catch (IOException e) {
            System.err.println("Could not load " + file +".fxml");
            return null;
        }
    }
}

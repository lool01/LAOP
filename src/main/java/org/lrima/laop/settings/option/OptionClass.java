package org.lrima.laop.settings.option;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;

public class OptionClass implements Option<Class<?>> {
    Class<?> value;

    public OptionClass(Class<?> value) {
    }

    @Override
    public Class<?> getValue() {
        return value;
    }

    @Override
    public boolean setValue(Class<?> value) {
        this.value = value;
        return true;
    }

    @Override
    public Node generateComponent() {
        // TODO : take the global liste instead
        ObservableList<String> observableList = FXCollections.emptyObservableList();
        observableList.add(value.toString());

        return new ComboBox<String>(observableList);
    }
}
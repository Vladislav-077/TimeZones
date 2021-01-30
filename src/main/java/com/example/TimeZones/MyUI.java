package com.example.TimeZones;

import com.vaadin.annotations.Theme;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@SpringUI
@Theme("valo")
public class MyUI extends UI {
    private VerticalLayout verticalLayout;

    @Override
    protected void init(VaadinRequest vaadinRequest) {
        setupLayout(); // добавили вертикальный слой.

    }

    private void setupLayout(){
        verticalLayout = new VerticalLayout();
        setContent(verticalLayout);
    }
}

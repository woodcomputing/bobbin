/*
 * FXSlang
 * Copyright 2016 Jonathan Wood
 * Licensed under the Apache License, Version 2.0
 */
package com.woodcomputing.bobbin;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.woodcomputing.bobbin.controller.MainPane;
import com.woodcomputing.bobbin.module.BobbinModule;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.extern.log4j.Log4j2;

/**
 *
 * @author Jonathan Wood
 * 
 */
@Log4j2
public class Bobbin extends Application {

    private final int width = 1200;
    private final int height = 800;
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Bobbin");
        Injector injector = Guice.createInjector(new BobbinModule());
        MainPane mainPane = injector.getInstance(MainPane.class);
        primaryStage.setWidth(width);
        primaryStage.setHeight(height);
        primaryStage.setScene(new Scene(mainPane));
        primaryStage.show();
    }

}

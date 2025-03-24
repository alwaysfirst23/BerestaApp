module org.example.demo {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires annotations;
    requires okhttp3.java.net.cookiejar;
    requires org.json;
    requires jbcrypt;
    requires okhttp3;
    requires spring.boot;
    requires spring.context;
    requires spring.security.config;
    requires spring.security.web;
    requires spring.security.crypto;
    requires spring.security.core;
    requires spring.data.commons;
    requires jakarta.persistence;
    requires jakarta.validation;
    requires spring.data.jpa;
    requires static lombok;
    requires spring.web;
    requires spring.boot.autoconfigure;

    opens org.example.demo to javafx.fxml, org.hibernate.orm.core, spring.core;
    exports org.example.demo;
}
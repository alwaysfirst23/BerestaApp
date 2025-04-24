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
    requires javafx.graphics;

    exports org.example.demo.domain;
    opens org.example.demo.domain to javafx.fxml, org.hibernate.orm.core, spring.core;
    exports org.example.demo.infrastructure;
    opens org.example.demo.infrastructure to javafx.fxml, org.hibernate.orm.core, spring.core;
    exports org.example.demo.domain.exceptions;
    opens org.example.demo.domain.exceptions to javafx.fxml, org.hibernate.orm.core, spring.core;
    exports org.example.demo.presentation.auth;
    opens org.example.demo.presentation.auth to javafx.fxml, org.hibernate.orm.core, spring.core, javafx.graphics;
    exports org.example.demo.services;
    opens org.example.demo.services to javafx.fxml, org.hibernate.orm.core, spring.core;
    opens org.example.demo to javafx.graphics, javafx.fxml, org.hibernate.orm.core, spring.core;
    opens org.example.demo.presentation.main to javafx.fxml, javafx.graphics;
    exports org.example.demo.presentation.main;
}
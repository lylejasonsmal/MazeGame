/**
 *
 */
module lylesmal.openworldgame {
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
    requires libtiled;
    requires java.desktop;
    requires com.almasb.fxgl.entity;
    requires javafx.media;

    opens lylesmal.openworldgame to javafx.fxml;
    opens assets.textures;
    opens assets.music;
    opens assets.sounds;
    exports lylesmal.openworldgame;
}
package lylesmal.openworldgame;

import lylesmal.openworldgame.views.LoadingScreen;

public class Main {
    public static void main(String[] args) {
        LoadingScreen frame = new LoadingScreen();
        frame.setGUI();
        frame.initActionListeners(args);
    }
}
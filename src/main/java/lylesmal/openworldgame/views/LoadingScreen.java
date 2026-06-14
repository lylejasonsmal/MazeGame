package lylesmal.openworldgame.views;

import com.almasb.fxgl.app.GameApplication;
import lylesmal.openworldgame.MapApplication;
import lylesmal.openworldgame.util.Helper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class LoadingScreen extends JFrame {

    JLabel logo;
    JLabel gameName;
    JLabel tradeMarkSymbol;
    JPanel pnlMain;
    JPanel pnlGameName;
    JButton btnStart = new JButton("Start");
    JButton btnExit = new JButton("Exit");

    JButton[] buttons = {btnStart, btnExit};

    Font font = new Font(Helper.fontName, Font.PLAIN, 14);
    Font font_2 = new Font(Helper.fontName, Font.PLAIN, 30);

    ImageIcon icon = new ImageIcon(getClass().getResource("/assets/textures/island-wall.png"));
    ImageIcon iconImg = new ImageIcon(getClass().getResource("/assets/textures/island-wall.png"));

    public LoadingScreen() {
        super("a-Mazed™");
        Image logoImg = iconImg.getImage().getScaledInstance(250, 250, Image.SCALE_SMOOTH);
        ImageIcon logoIcon = new ImageIcon(logoImg);

        logo = new JLabel(logoIcon);


        gameName = new JLabel("a-Mazed");
        gameName.setForeground(Color.WHITE);
        gameName.setFont(font_2);

        tradeMarkSymbol = new JLabel("™");
        tradeMarkSymbol.setForeground(Color.WHITE);

        for(int i = 0; i < buttons.length; i++){
            buttons[i].setFont(font);
            buttons[i].setForeground(Color.WHITE);
            buttons[i].setBackground(new Color(61, 67, 74));
            buttons[i].setBorderPainted(false);
            buttons[i].setFocusable(false);
            initMouseListeners(buttons[i]);
        }

        pnlMain = new JPanel();
        pnlMain.setBackground(new Color(0,0,0,0));

        pnlGameName = new JPanel();
        pnlGameName.setBackground(new Color(0,0,0,0));
    }

    public void initActionListeners(String[] args){
        btnStart.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                GameApplication.launch(MapApplication.class, args);
            }
        });

        btnExit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
    }

    public void initMouseListeners(JButton btn){

        btn.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {

            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setForeground(Color.BLACK);
                btn.setBackground(new Color(157, 162, 167));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setForeground(Color.WHITE);
                btn.setBackground(new Color(61, 67, 74));
            }
        });
    }

    public void setGUI() {
        pnlGameName.add(gameName);
        pnlGameName.add(tradeMarkSymbol);

        pnlMain.add(pnlGameName);
        pnlMain.add(logo);
        pnlMain.add(btnStart);
        pnlMain.add(btnExit);

        this.setUndecorated(true);
        this.add(pnlMain);
        this.pack();
        this.setSize(300, 400);
        this.setVisible(true);
        this.setResizable(false);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.setIconImage(icon.getImage());
        this.setBackground(new Color(0, 0, 0, 0));
    }
}

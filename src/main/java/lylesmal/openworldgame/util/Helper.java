package lylesmal.openworldgame.util;

import com.almasb.fxgl.entity.Entity;
import java.awt.geom.Point2D;

public class Helper {


    public static String fontName = "Ninja Naruto";

    public static Point2D fixEntityToGrid(Entity player){
        Point2D location = new Point2D.Double();
        double modulusX = player.getX()%16;
        double modulusY = player.getY()%16;
        double x;
        double y;

        switch ((int) modulusX){
            case 0:
                x = player.getX()-16;
                break;
            default:
                x = (player.getX()-modulusX) -16;
                break;
        }

        switch ((int) modulusY){
            case 0:
                y = player.getY()-16;
                break;
            default:
                y = (player.getY()-modulusY) - 16;
                break;
        }

        location.setLocation(x, y);
        return location;
    }

}

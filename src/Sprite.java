import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

interface ISprite {
    
    // EFFECT: changes the given graphics g by adding this sprite onto it
    // Renders this sprite onto the given graphics g
    void draw(Graphics2D g2d);
    
    // EFFECT: modifies all kinetic variables after one tick:
    //          1. Increases x y position by adding velocities once.
    //          2. Increases vX vY velocity by adding velocities once. TODO
    void step(Terrain given);
    
    // EFFECT: modifies this sprite's acceleration accX and accY
    void acc(double aX, double aY);
    
    // determines whether this sprite is in contact with the given Terrain
    boolean isOnTerrain(Terrain given);
    
    // returns the repaint area of this sprite
    Rectangle getRepaintArea();
    
}

abstract class ASprite implements ISprite{

    
    Point coord; // origin at bot left; anchor @topleft
    Point vel_vector;
    Point acc_vector;
    double friction;
    BufferedImage image;
    boolean stopped;
    
    
    ASprite(double x, double y, double vX, double vY, double accX, double accY, double friction, BufferedImage image) {
        this.coord = new Point(x, y);
        this.vel_vector = new Point(vX, vY);
        this.acc_vector = new Point(accX, accY);
        this.friction = friction;
        this.image = image;
        this.stopped = true;
    }
    
    // determines whether this sprite has contact with the given Terrain
    @Override
    public boolean isOnTerrain(Terrain given) {
        return given.isOnTerrain(this.coord);
    }
    
    // EFFECT: modifies all kinetic variables after one tick:
    //          1. Increases x y position by adding velocities once.
    //          2. Alters velocity:
    //              - first, add accXY to vXY
    //              - if vX is not in contact with terrain (mid-air), vX doesn't change
    //              - else vX is decreased by friction
    //              - vY is decreased by gravity
    //          3. Resets all acceleration, as all accelerations of this ball is instant.
    @Override
    public void step(Terrain given) {
        
        this.vel_vector.increaseBy(this.acc_vector);
        this.coord.increaseBy(this.vel_vector);
        
        // Case 1: Flying in air
        if (!this.isOnTerrain(given) && !stopped) {
            this.vel_vector.grav(-0.08); // TODO create gravitation value
        }
        // Case 2: On the ground
        else if (!stopped) {
            
            this.vel_vector.y = 0;
            
            // 1. X velocity should slow down
            this.vel_vector.slowByFric(this.friction);
            
            // 2. Y coordinate should be on curve
            this.coord.y = given.inputXGetY(this.coord.x);
            
            /*
             * 1. Get Slope as a vector
             * 2. Find the angle of slope using atan
             * 3. Calculate acc vector of angled gravity force
             */

            Point slope = given.inputXGetSlope(this.coord.x);
            
            double theta_rad = Math.atan(slope.y / slope.x);

            //double acc = 0.0179 * Math.sin(theta_rad) * one_rad;
            double acc = 1 * Math.sin(theta_rad);
            
            // slowBySlope
            this.vel_vector.x -= acc;
            //this.vel_vector.y = acc;
            
            if (Math.abs(this.vel_vector.x) <= 0.02 && acc < 0.02) {
                this.stopped = true;
                this.vel_vector.x = 0;
                this.vel_vector.y = 0;
            }
            
            System.out.println("Slope = (" + slope.x + ", " + slope.y +")");
            //System.out.println("theta = " + theta_rad * one_rad);
            System.out.println("acc   = " + acc);
            System.out.println("vel   = " + this.vel_vector.x + ", " + this.vel_vector.y);
        }
        
        this.acc_vector.x = 0; // TODO
        this.acc_vector.y = 0; // TODO
        
    }
    
    // EFFECT: modifies this sprite's acceleration accX and accY
    @Override
    public void acc(double aX, double aY) {
        this.acc_vector.increaseBy(new Point(aX, aY));
    }
    
    // Abstract method that returns the rectange of the repaint area
    public abstract Rectangle getRepaintArea();
    
}

// to represent the ball sprite in this game
class Ball extends ASprite {
    
    int radius;
    int hover;
    double strokeWidth;
    
    Ball(double x, double y) {
        super(x, y, 0, 0, 0, 0, 0.45, null); // 2, 2 min ; radius = 7 ; was 
        this.radius = 5;
        this.hover = 1;
        this.strokeWidth = 1.5;
    }
    
    // EFFECT: changes the given graphics g by adding this Ball onto it
    // Renders this Ball onto the given graphics g
    @Override
    public void draw(Graphics2D g2d) {
        
        g2d.setColor(Color.WHITE);
        g2d.fill(new Ellipse2D.Double(
                        this.coord.x - radius,       // graphically: moves ball left
                        600 - this.coord.y - radius - this.hover, // graphically: moves ball up
                        this.radius * 2,
                        this.radius * 2));
        
        g2d.setColor(Color.BLACK);
        BasicStroke circleStroke =
                new BasicStroke((float) this.strokeWidth,
                                BasicStroke.CAP_BUTT,
                                BasicStroke.JOIN_BEVEL);
        g2d.setStroke(circleStroke);
        g2d.draw(
                new Ellipse2D.Double(
                        this.coord.x  - radius,
                        600 - this.coord.y - radius - this.hover,
                        this.radius * 2,
                        this.radius * 2));
    }

    @Override
    public Rectangle getRepaintArea() {
        int paintWidth = (int) (this.radius * 2 + this.strokeWidth * 2 + 1 + this.hover);
        return new Rectangle(
                (int) (this.coord.x - this.radius - this.strokeWidth),
                (int) (600 - this.coord.y - this.radius - this.strokeWidth - this.hover),
                paintWidth,
                paintWidth);
    }
    
}

// to represent the GolfStick
class Arrow extends ASprite {
    
    int hover;
    float stroke;
    Point coord2 = new Point(0, 0);
    
    Arrow(double x, double y, int hover) {
        super(x, y, 0, 0, 0, 0, 0, null);
        this.hover = hover;
        this.stroke = 4f;
    }

    void updateArrow(Point acc, Point ball) {
        this.coord = acc.scale(1.5).add(ball);
        this.coord2 = acc.scale(5).add(ball);
    }

    @Override
    public void draw(Graphics2D g2d) {
        
        Line2D.Double stick = new Line2D.Double(
                this.coord.x,
                600 - this.coord.y,
                this.coord2.x,
                600 - coord2.y);
        
        BasicStroke stickStroke =
                new BasicStroke(4f,
                                BasicStroke.CAP_BUTT,
                                BasicStroke.JOIN_BEVEL);
        g2d.setColor(Color.WHITE);
        g2d.setStroke(stickStroke);
        g2d.draw(stick);
    }

    @Override
    public Rectangle getRepaintArea() {
        
        float strokeNudge = this.stroke + 2;
        
        // Case 1: Coord.x < Coord2.x (further one)
        if (this.coord.x < this.coord2.x) {
            
            // Case 1A: Coord.y < Coord2.y
            if (this.coord.y < this.coord2.y) {
                return new Rectangle(
                        (int) (this.coord.x - strokeNudge),
                        (int) (600 - this.coord2.y - strokeNudge),
                        (int) (this.coord2.x - this.coord.x + 2 * strokeNudge),
                        (int) (this.coord2.y - this.coord.y + 2 * strokeNudge));
            }
            // Case 1B: Coord.y >= Coord2.y
            else {
                return new Rectangle(
                        (int) (this.coord.x - strokeNudge),
                        (int) (600 - this.coord.y - strokeNudge),
                        (int) (this.coord2.x - this.coord.x + 2 * strokeNudge),
                        (int) (this.coord.y - this.coord2.y + 2 * strokeNudge));
            }
        }
        // Case 2: Coord.x >= Coord2.x
        else {
            // Case 2A: Coord.y < Coord2.y
            if (this.coord.y < this.coord2.y) {
                return new Rectangle(
                        (int) (this.coord2.x - strokeNudge),
                        (int) (600 - this.coord2.y - strokeNudge),
                        (int) (this.coord.x - this.coord2.x + 2 * strokeNudge),
                        (int) (this.coord2.y - this.coord.y + 2 * strokeNudge));
            }
            // Case 2B: Coord.y >= Coord2.y
            else {
                return new Rectangle(
                        (int) (this.coord2.x - strokeNudge),
                        (int) (600 - this.coord.y - strokeNudge),
                        (int) (this.coord.x - this.coord2.x + 2 * strokeNudge),
                        (int) (this.coord.y - this.coord2.y + 2 * strokeNudge));
            }
        }
    }
}

class Graphic extends ASprite {

    int hover;
    
    Graphic(double x, double y, int hover, BufferedImage image) {
        super(x, y, 0, 0, 0, 0, 0, image);
        this.hover = hover;
    }

    @Override
    public void draw(Graphics2D g2d) {
        g2d.drawImage(this.image, (int) this.coord.x, (int) (600 - this.coord.y - this.hover), null);
    }

    @Override
    public Rectangle getRepaintArea() {
        return new Rectangle(
                (int) this.coord.x,
                (int) (600 - this.coord.y - this.hover),
                this.image.getWidth(),
                this.image.getHeight());
    }
    
}

class TextGraphic extends ASprite {

    String text;
    Font font;
    int stringWidth = 0;
    int stringAccent = 0;
    
    TextGraphic(String text, Font font, double x, double y) {
        super(x, y, 0, 0, 0, 0, 0, null);
        this.text = text;
        this.font = font;
    }

    @Override
    public void draw(Graphics2D g2d) {
        g2d.setFont(this.font);
        g2d.setColor(Color.WHITE);
        
        FontMetrics fm = g2d.getFontMetrics();
        this.stringWidth = fm.stringWidth(this.text);
        this.stringAccent = fm.getAscent();
        g2d.drawString(
                this.text,
                (int) (this.coord.x - stringWidth / 2),
                (int) (this.coord.y + stringAccent / 4));
    }

    void setText(String text) {
        this.text = text;
    }
    
    @Override
    public Rectangle getRepaintArea() {

        //System.out.println(this.coord.p());
        //System.out.println("(" + bounds.getCenterX() + ", " +bounds.getCenterY());
        
        return new Rectangle(
                (int) (this.coord.x - this.stringWidth / 2),
                (int) (this.coord.y - this.stringAccent / 1.7),
                (int) this.stringWidth,
                (int) this.stringAccent);
    }
    
}
    
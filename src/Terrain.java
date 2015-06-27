import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.util.ArrayList;

import javax.swing.Timer;

import tester.Tester;

// to represent a Terrain (level) in the golf game
public class Terrain {
    
    final int WIDTH;      // width of game board
    final int HEIGHT;     // height of game board
    final int NUM_CURVES; // total curves contained in this terrain
    
    // List of all curves segments used to create this Terrain
    ArrayList<Curve> curveSegments = new ArrayList<Curve>();
    
    // Polygon shape that represents area the integral of all the curves
    // the method: render() will use this Polygon to fill the bottom filling
    // of the terrain. All points are in Java-ready coords.
    Polygon terrainShape = new Polygon();
    
    Path2D.Double stroke;
    
    // Constructs a new Terrain (level) using the given list of curveSegments
    Terrain(ArrayList<Curve> curveSegments, int w, int h) {
        this.curveSegments = curveSegments;
        this.WIDTH = w;
        this.HEIGHT = h;
        this.NUM_CURVES = curveSegments.size();
        this.createTerrain();
        this.generatePath(h);
    }
    
    // EFFECT:
    // sets the polygon area of the terrain to this.terrainShape
    
    // Part 1: 
    // Traversing this terrain's curve segments:
    // For each Curve:
    //    For t = [0, 1); t0 = 0, t1 = 0.001..., t1000 = 0.999...
    //       add point (FX(t), FY(t)) to Polygon terrainShape.
    //           where: (FX and FY are cubic bezier curve functions of that curve)
    
    // Part 2:
    //    1. add point @ (X of last pt of last curve, HEIGHT)
    //    2. add point @ (0, HEIGHT)
    //    3. add point @ (X of 1st curve's 1st pt, Y of 1st curve's 1st pt)
    void createTerrain() {
        for (int index = 0; index < this.curveSegments.size(); index += 1) {
            for (int size = 0; size < 1000; size += 1) { // 0 to 999
                
                Curve current_curve = this.curveSegments.get(index);
                
                double t = (double) size / 1000;
                
                double outputX = current_curve.inputTGetX(t);
                double outputY = current_curve.inputTGetY(t);
                
                double outputYDrawn = this.HEIGHT - outputY; // changes to g2d ready y coord
                                                             // x doesn't need chanage.
                this.terrainShape.addPoint((int) outputX,
                                            (int) outputYDrawn);
            }
        }
        this.terrainShape.addPoint(
                (int) CurvesUtil.endingX(this.curveSegments),
                this.HEIGHT);
        this.terrainShape.addPoint(
                (int) CurvesUtil.startingX(this.curveSegments),
                this.HEIGHT);
        this.terrainShape.addPoint(
                (int) CurvesUtil.startingX(this.curveSegments),
                this.HEIGHT - (int) CurvesUtil.startingY(this.curveSegments));
    }
    
    // returns y value of the terrain piecewise curves by:
    // 1. Finding the correct curve where x is within domain of
    // 2. Return the y value of the correct curve by inputing x
    double inputXGetY(double givenX) {
        for (Curve c : this.curveSegments) {
            if (c.withinDomain(givenX)) {
                return c.inputXGetY(givenX);
            }
        }
        return 0;
    }
    
    Point inputXGetSlope(double givenX) {
        for (Curve c : this.curveSegments) {
            if (c.withinDomain(givenX)) {
                return c.inputXGetSlope(givenX);
            }
        }
        return null;
    }
    
    // is given Point:
    //    Either:
    //       y == y value of correct curve (at this terrain)
    //       y <  y value of correct curve (over this terrain) ?
    boolean isOnTerrain(Point given) {
        for (Curve c : this.curveSegments) {
            if (c.withinDomain(given.x)) {
                return c.withinRange(given.x, given.y);
            }
        }
        return false;
        
    }
    
    // EFFECT:
    // renders this curve in on to the given g2d
    // (fills the terrain area, strokes the top portion)
    void render(int w, int h, float o, Graphics2D g2d) {
        
        // Render terrain area:
            Composite a = g2d.getComposite();
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, o / 2));
            g2d.setColor(new Color(50, 60, 61)); // teal 2
            // polygon is ready for java-coords.
            g2d.fill(this.terrainShape);
            g2d.setComposite(a);
            
        // Render terrain top-stroke:
            BasicStroke grassStroke =
                    new BasicStroke(10.0f,
                                    BasicStroke.CAP_BUTT,
                                    BasicStroke.CAP_BUTT);
            g2d.setColor(new Color(182, 206, 204));
            g2d.setStroke(grassStroke);
            g2d.draw(this.stroke);
    }
    
    int width = 0;
    Rectangle getRepaintArea() {
        return new Rectangle(
                0,
                0,
                this.width,
                GameBoard.HEIGHT);
    }
    
    Timer gen;
    boolean isShowingMap = false;
    void animateGeneration() {
        this.isShowingMap = true;
        this.gen = new Timer(16, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                int incre = 3;
                if (width + incre >= GameBoard.WIDTH) {
                    gen.stop();
                    width = GameBoard.WIDTH;
                    isShowingMap = false;
                } else {
                    width += incre;
                }
            }
        });
        this.gen.start();
    }
    
    void generatePath(int h) {
        Path2D.Double cur = new Path2D.Double();
        for (Curve c : this.curveSegments) {
            cur.append(c.createBezierPath(h), false);
        }
        this.stroke = cur;
    }
}

// to represent a Curve segment (cubic bezier) with four points
class Curve {
    
    Point p0, p1, p2, p3;
    
    // x values as t increases by 0.001 each element
    double[] xvalues = new double[1000];
    // y values of this curve as t increases by 0.001 each element
    double[] yvalues = new double[1000];
    
    Curve(Point p0, Point p1, Point p2, Point p3) {
        this.p0 = p0;
        this.p1 = p1;
        this.p2 = p2;
        this.p3 = p3;
        // the for-loop below will store all x and y values of this curve
        // in 1/1000 increments of t
        for (int index = 0; index < 1000; index++) {
            //System.out.println(index);
            this.xvalues[index] = this.inputTGetX((double) index / 1000);
            this.yvalues[index] = this.inputTGetY((double) index / 1000);
        }
    }
    
    // is given Point's y value is lower or equal to the range of this curve
    boolean withinRange(double x, double y) {
        return y <= this.inputXGetY(x);
    }
    
    // determines whether the given double x is within the domain of this curve
    boolean withinDomain(double given) {
        return this.p0.isXEqualLessGiven(given)
            && this.p3.isXEqualMoreGiven(given);
    }
    
    // TODO this method works, but feels weird...
    
    // returns a double of Y after plugging in given X to this curve
    double inputXGetY(double givenX) {
        
        int closest_index = 0;
        double closest_estimate_x = 0;
        
        // 1. Finds the position of the closest x in this.xvalues
        for (int index = 0; index < 1000; index++) {
            
            // |givenX - xvalues[index]| < |givenX - closest_estimate_x|
            if (Math.abs(givenX - this.xvalues[index]) < Math.abs(givenX - closest_estimate_x)) {
                closest_estimate_x = this.xvalues[index];
                closest_index = index;
            }
        }
        // 2. Finds the yvalue that is the position got from the first step
        return this.yvalues[closest_index];
    }
    
    // returns a new Point after plugging in t [0, 1) to the curve equation
    Point inputT(double t) {
        return new Point(this.inputTGetX(t), this.inputTGetY(t));
    }
    
    // returns the x value after plugging in t [0, 1) to the curve equation
    double inputTGetX(double t) {
        double u = 1-t;
        return u*u*u*this.p0.x + 3*u*u*t*this.p1.x + 3*u*t*t*this.p2.x + t*t*t*this.p3.x;
    }
    
    // returns the y value after plugging in t [0, 1) to the curve equation
    double inputTGetY(double t) {
        double u = 1-t;
        return u*u*u*this.p0.y + 3*u*u*t*this.p1.y + 3*u*t*t*this.p2.y + t*t*t*this.p3.y;
    }
    
    // returns a Path2D.Double that represents this curve
    Path2D.Double createBezierPath(int givenHeight) {
        Path2D.Double path = new Path2D.Double(
                                new Line2D.Double(
                                        this.p0.x - 0.00003,  // this fixes a slight glitch in g2d
                                        givenHeight - this.p0.y,
                                        this.p0.x,
                                        givenHeight - this.p0.y));
        path.curveTo(         
                this.p1.x,
                givenHeight - this.p1.y,
                this.p2.x,
                givenHeight - this.p2.y,
                this.p3.x,
                givenHeight - this.p3.y);
        return path;
    }
    
    // TODO this is what to do next (bouncing, rolling, sliding)
    // returns a new Point(actually vector) after plugging in t in the DERIVATIVE of
    // the cubic equation
    // dP(t)/dt = - 3(1-t)^2 * P0
    //            + 3(1-t)^2 * P1
    //            - 6t(1-t) * P1
    //            - 3t^2 * P2
    //            + 6t(1-t) * P2
    //            + 3t^2 * P3
    Point slopeT(double t) {
        double u = 1 - t;
        return p0.scale(-3 * u * u).add(
                    p1.scale(3*u*u).add(
                            p1.scale(-6*t*u).add(
                                    p2.scale(-3*t*t).add(
                                            p2.scale(6*t*u).add(
                                                    p3.scale(3*t*t))))));
    }
    
    Point inputXGetSlope(double givenX) {
        int closest_index = 0;
        double closest_estimate_x = 0;
        
        // 1. Finds the position of the closest x in this.xvalues
        for (int index = 0; index < 1000; index++) {
            
            // |givenX - xvalues[index]| < |givenX - closest_estimate_x|
            if (Math.abs(givenX - this.xvalues[index]) < Math.abs(givenX - closest_estimate_x)) {
                closest_estimate_x = this.xvalues[index];
                closest_index = index;
            }
        }
        
        // 2. Finds the yvalue that is the position got from the first step
        return this.slopeT((double) closest_index / 1000);
    }
    
}

// represents a Point/Vector with double x and y values
class Point {
    
    double x, y;
    
    Point(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    // Returns a new Point by adding x y of this with x y of given point
    Point add(Point given) {
        return new Point(this.x + given.x, this.y + given.y);
    }
    
    // Returns a new Point by scaling this point by given mag
    Point scale(double mag) {
        return new Point(this.x * mag, this.y * mag);
    }
    
    // Returns a double that represents the magnitude of this vector
    double getMagnitude() {
        return Math.sqrt(Math.pow(this.x, 2) + Math.pow(this.y, 2));
    }
    
    // EFFECT:
    // changes this point by adding x y of given point to this x y
    void increaseBy(Point given) {
        this.x += given.x;
        this.y += given.y;
    }
    
    // EFFECT:
    // changes this point by scaling this x and y by given factor
    void scaleBy(double mag) {
        this.x *= mag;
        this.y *= mag;
    }
    
    // EFFECT:
    // changes this velocity vector by friction
    void slowByFric(double friction) {
        
        // moving @ < 1 pixel per frame
        if (Math.abs(this.x) < 1.0) {
            this.x = 0;
        }
        // moving @ <= -1 pixel/frame
        else if (this.x <= -1) {
            this.x = x + friction / Math.abs(x*2); //TODO change friction to the friction of the Terrain
        }
        // moving @ >= 1 pixel/frame
        else if (this.x >= 1) {
            this.x = x - friction / Math.abs(x*2);
        }
        
    }
    
    // EFFECT:
    // changes this velocity vector by gravity (a negative value)
    void grav(double g) {
        this.y += g;
    }
    
    // EFFECT:
    // changes this velocity vector by changing its vy
    void bounce(double e) {
        this.y = e; // TODO
        this.x = -e;
    }
    
    // Returns the slope of this vector; y
    double slope() {
        return this.y / this.x;
    }
    
    // determines whether this x is equal or less than given x
    boolean isXEqualLessGiven(double given) {
        return this.x <= given;
    }
    
    // determines whether this x is equal or more than given x
    boolean isXEqualMoreGiven(double given) {
        return this.x >= given;
    }
    
    // Returns a String representative of this Point
    String p() {
        return "("+this.x+", "+this.y+")";
    }
    
}

class ExamplePoints {
    
    Point p00 = new Point(0,   187);
    Point p01 = new Point(125, 187);
    Point p02 = new Point(125, 187);
    Point p03 = new Point(125, 187);
    Curve c0 = new Curve(this.p00, this.p01, this.p02, this.p03);
    
    Point p10 = new Point(125, 187);
    Point p11 = new Point(125+50, 237-50);
    Point p12 = new Point(200, 40);
    Point p13 = new Point(334-40, 40);
    Curve c1 = new Curve(p10, p11, p12, p13);
    
    Point p20 = new Point(334-40, 40);
    Point p21 = new Point(400, 40);
    Point p22 = new Point(400, 436);
    Point p23 = new Point(468, 436);
    Curve c2 = new Curve(p20, p21, p22, p23);
    
    Point p30 = new Point(468, 436);
    Point p31 = new Point(400+68+68, 436);
    Point p32 = new Point(400+68, 280);
    Point p33 = new Point(334+300, 280);
    Curve c3 = new Curve(p30, p31, p32, p33);
    
    
    ArrayList<Curve> curves = new ArrayList<Curve>();
    Terrain terrain;
    
    void setup() {
        this.curves.add(this.c0);
        this.curves.add(this.c1);
        this.curves.add(this.c2);
        this.curves.add(this.c3);
        terrain = new Terrain(this.curves, 1000, 600);
    }
    
    void testIsPointOnOrOver(Tester t) {
        setup();
        
        t.checkExpect(this.c0.inputT(0), new Point(0.0, 187.0));
        t.checkExpect(this.c1.inputT(0), new Point(125, 237-50));
        
        t.checkExpect(this.c0.withinRange(0.0, 187.0), true);
        t.checkExpect(this.c0.withinRange(0.0, 187.0001), false);
        t.checkExpect(this.c2.withinRange(468, 436), true);
        t.checkExpect(this.c2.withinRange(468, 436.0001), false);
        t.checkExpect(this.c2.withinRange(468, -10), true);
        
        t.checkExpect(this.terrain.isOnTerrain(new Point(0.0, 187)), true);
        t.checkExpect(this.terrain.isOnTerrain(new Point(0.0, 468)), false);
        t.checkExpect(this.terrain.isOnTerrain(new Point(0.0, 186.99)), true);
        t.checkExpect(this.terrain.isOnTerrain(new Point(468, 436)), true);
        t.checkExpect(this.terrain.isOnTerrain(new Point(468, 436.0001)), false);
        t.checkExpect(this.terrain.isOnTerrain(new Point(468, -10)), true);
        
    }
    
    
    void testWithinDomain(Tester t) {
        
        t.checkExpect(this.c0.withinDomain(0), true);
        t.checkExpect(this.c0.withinDomain(4), true);
        t.checkExpect(this.c0.withinDomain(125), true);
        t.checkExpect(this.c0.withinDomain(-0.001), false);
        t.checkExpect(this.c0.withinDomain(125.01), false);
    }
    
}

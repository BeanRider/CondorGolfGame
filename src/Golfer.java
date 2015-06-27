import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.io.IOException;

import javax.imageio.ImageIO;


// to represent the current player of the game
// A player has a:
/* - golfBall
 * - arrow of direction
 * - number of strokes
 */
public class Golfer {
    
    // Graphical objects
    ASprite golfBall;
    ASprite arrow;
    
    // Scoring
    int strokes;
    
    // Hitting
    Point strokeAcc;
    double strokeStrength;
    double strokeAngle;
    
    Golfer() {
        this.golfBall = new Ball(20, 187);
        this.arrow = new Arrow(0, 0, 10);
        
        // Scoring
        this.strokes = 0;
        
        // Physics
        this.strokeStrength = 2;
        this.strokeAngle = 45;
        this.strokeAcc = new Point(0, 0);
        this.updateAcc();
        this.updateStick();
        
    }
    
    // EFFECT: changes the arrow's coordinates according to the angle of the hit
    void updateStick() {        
        Arrow a = (Arrow) this.arrow;
        a.updateArrow(this.strokeAcc, this.golfBall.coord);
    }
    
    // TODO leftkey hit, change hitting angle and strokeAcc
    void increaseAngle() {
        this.strokeAngle += 3;
        this.updateAcc();
        this.updateStick();
    }
    
    
    // TODO rightkey hit, change hitting angle and strokeAcc
    void decreaseAngle() {
        this.strokeAngle -= 3;
        this.updateAcc();
        this.updateStick();
    }
    
    // TODO upkey hit, increase hitting strength [max = 7]
    void increaseStrength() {
        if (this.strokeStrength + 0.25 <= 6) {
            this.strokeStrength += 0.25;
        }
        this.updateAcc();
        this.updateStick();
    }
    
    // TODO downkey hit, decrease hitting strength [low = 1]
    void decreaseStrength() {
        if (this.strokeStrength - 0.25 >= 1) {
            this.strokeStrength -= 0.25;
        }
        this.updateAcc();
        this.updateStick();
    }
    
    void updateAcc() {
        double degreeToradian = Math.PI / 180.0;
        // Update stroke accelation values before hitting
        this.strokeAcc.x = this.strokeStrength * Math.cos(this.strokeAngle * degreeToradian) * Math.sqrt(2);
        this.strokeAcc.y = this.strokeStrength * Math.sin(this.strokeAngle * degreeToradian) * Math.sqrt(2);
    }
    
    // TODO spacebar hit, accelerate golfball
    void hitBall() {
        
        System.out.println(this.golfBall.vel_vector.x + ","
                + this.golfBall.vel_vector.y + ","
                + this.golfBall.acc_vector.x + ","
                + this.golfBall.acc_vector.y);
        
        if (this.golfBall.stopped) {
            new Sound("resources/hit.wav").play();
            this.golfBall.stopped = false;
            this.golfBall.acc(this.strokeAcc.x, this.strokeAcc.y);
            this.strokes += 1;
        }
        
    }
    
    // EFFECT: draws the golfBall sprite and the golfStick sprite onto the given G2D
    void draw(Graphics2D g2d) {
        this.golfBall.draw(g2d);
        if (this.golfBall.stopped) {
            this.arrow.draw(g2d);
        }
    }
    
    Rectangle getRepaintArea() {
        return this.golfBall.getRepaintArea().union(this.arrow.getRepaintArea());
    }
    
}

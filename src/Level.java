import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.Timer;


public class Level {
    
    Golfer golfer;
    ASprite ball;
    ASprite flag;
    ASprite strokeDisplay;
    ASprite title;
    Terrain map;
    BufferedImage flagI, bkI, restartI;
    
    // Animation 1: Show Level Title
    Timer showLevelName;
    float levelNameOpacity = 0f;
    boolean isShowingName = true;
    
    // Animate 2: Show Map
    Timer showMap;
    float mapOpacity = 0f;
    boolean isShowingMap = false;
    
    // Animate 3: Restart Screen
    Timer blacken;
    float darkness = 0f;
    boolean hasWon;
    
    // Animate 4: Fade away ball
    Timer showBall;
    float ballOpacity = 1f;
    boolean isHidingBall = false;
    
    
    Level(Terrain t) {
        
        this.hasWon = false;
        
        this.golfer = new Golfer();
        
        this.ball = golfer.golfBall;
        
        this.map = t;
        
        try{
            this.bkI = ImageIO.read(this.getClass().getResource("resources/bk.png"));
            this.flagI = ImageIO.read(this.getClass().getResource("resources/flag.png"));
            this.restartI = ImageIO.read(this.getClass().getResource("resources/restart.png"));
        } catch(IOException ioe){
            ioe.printStackTrace();
        }
        
        double flagX = 800;
        
        this.flag = new Graphic(flagX, this.map.inputXGetY(flagX), 40, this.flagI);
        this.strokeDisplay = new TextGraphic(
                "Stroke: 0",
                new Font("Champagne & Limousines", Font.TRUETYPE_FONT, 20),
                GameBoard.WIDTH - 100,
                GameBoard.HEIGHT - 40);
        this.title = new TextGraphic(
                "Hole 1",
                new Font("Dancing Script", Font.BOLD, 180),
                GameBoard.WIDTH / 2,
                GameBoard.HEIGHT / 2);
    }
    
    void draw(Graphics2D g2d) {
        g2d.drawImage(this.bkI, 0, 0, null);
        
//        // For testing:
//        if (this.isShowingName && !this.map.isShowingMap) {
//            g2d.fill(this.title.getRepaintArea());
//        } else if (this.isShowingMap || this.map.isShowingMap) {
//            System.out.println("Showing terrain");
//            g2d.fill(this.map.getRepaintArea());
//            g2d.fill(this.title.getRepaintArea());
//        } else {
//            g2d.fill(this.strokeDisplay.getRepaintArea());
//        }
        
        if (isShowingName && !this.map.isShowingMap) {
            // Level Name
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, this.levelNameOpacity));
            this.title.draw(g2d);
        } 
        else {
            // Map, Flag, Ball & Arrow
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, this.mapOpacity));
            this.map.render(GameBoard.WIDTH, GameBoard.HEIGHT, this.mapOpacity, g2d);
            
            this.flag.draw(g2d);
            
            if (this.isHidingBall) {
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, this.ballOpacity));
            }
            this.golfer.draw(g2d);
            
            // score display
            this.strokeDisplay.draw(g2d);
        }
        
        
        if (hasWon) {
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, this.darkness));
            g2d.drawImage(this.restartI, 0, 0, null);
        }
        
    }    
    
    // First, fade in the title name; End: activate map animations and fadeout name
    void fadeInName() {
        this.isShowingName = true;
        
        this.showLevelName = new Timer(16, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                float incre = 0.005f;
                if (levelNameOpacity + incre >= 1) {
                    showLevelName.stop();
                    levelNameOpacity = 1f;
                   
                    // 3. Fade out name
                    fadeOutName();
                    
                } else {
                    levelNameOpacity += incre;
                }
            }
        });
        this.showLevelName.setInitialDelay(2000);
        this.showLevelName.start();
    }
    
    void fadeOutName() {
        this.showLevelName = new Timer(16, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                float incre = 0.005f;
                if (levelNameOpacity - incre <= 0) {
                    showLevelName.stop();
                    levelNameOpacity = 0f;
                    isShowingName = false;
                    

                    // 1. Fade in Map
                    fadeInMap();
                    // 2. Map cool animation
                    map.animateGeneration();
                    
                    
                } else {
                    levelNameOpacity -= incre;
                }
            }
        });
        this.showLevelName.setInitialDelay(1000);
        this.showLevelName.start();
    }
    
    void fadeInMap() {
        
        this.showMap = new Timer(16, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                float incre = 0.005f;
                if (mapOpacity + incre >= 1) {
                    showMap.stop();
                    mapOpacity = 1f;
                } else {
                    mapOpacity += incre;
                }
            }
        });
        this.showMap.start();
    }
    
    void fadeOutBall() {
        this.isHidingBall = true;
        this.showBall = new Timer(16, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                float incre = 0.008f;
                if (ballOpacity - incre <= 0) {
                    showBall.stop();
                    ballOpacity = 0;
                    fadeInBlack();
                    //hasWon = true;
                } else {
                    ballOpacity -= incre;
                }
            }
        });
        this.showBall.start();
    }
    
    void fadeInBlack() {
        this.blacken = new Timer(16, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                float incre = 0.005f;
                if (darkness + incre >= 1f) {
                    blacken.stop();
                    darkness = 1f;
                } else {
                    darkness += incre;
                }
            }
        });
        this.blacken.setInitialDelay(200);
        this.blacken.start();
    }
    
    
    void updateStep() {
        // Ball in hole
        if (checkWin()) {
            new Sound("/resources/in.wav").play();
            if (this.golfer.strokes == 1) {
                new Sound("/resources/applause.wav").play();
            }
            // 1. Switch to won mode
            this.hasWon = true;
            // 2. Fade out the ball
            this.fadeOutBall();
            
        } else {
            
            this.ball.step(this.map);
            this.golfer.updateStick();
            
        }
    }
    
    boolean checkWin() {
        return Math.abs((this.ball.coord.x - 5 - this.flag.coord.x)) < 2
            && Math.abs(this.ball.vel_vector.x) < 4
            && this.ball.isOnTerrain(this.map);
    }
    
    void updateScore() {
        ((TextGraphic) this.strokeDisplay).setText("Stroke: " + this.golfer.strokes);
    }
    
    void interactKey(int keyCode) {
        if (keyCode == KeyEvent.VK_SPACE) {
            this.golfer.hitBall();
            this.updateScore();
        } else if (keyCode == KeyEvent.VK_LEFT) {
            this.golfer.increaseAngle();
            System.out.println(golfer.strokeAngle);
        } else if (keyCode == KeyEvent.VK_RIGHT) {
            this.golfer.decreaseAngle();
            System.out.println(golfer.strokeAngle);
        } else if (keyCode == KeyEvent.VK_UP) {
            this.golfer.increaseStrength();
            System.out.println(golfer.strokeStrength);
        } else if (keyCode == KeyEvent.VK_DOWN) {
            this.golfer.decreaseStrength();
            System.out.println(golfer.strokeStrength);
        }
    }
    
    void repaintThis(GameBoard g) {
        // Case 1: Won Game
        if (hasWon) {
            g.repaint();
        }
        // Case 2: Showing name
        else if (this.isShowingName) {
            //System.out.println("Showing Name");
            g.repaint(this.title.getRepaintArea());
        }
        // Case 3: If showing map
        else if (!this.isShowingMap && this.map.isShowingMap) {
            //System.out.println("Showing terrain");
            g.repaint(this.map.getRepaintArea());
            
        }
        // Case 4: Ready and In game
        else {
            g.repaint(this.golfer.getRepaintArea());
            g.repaint(this.strokeDisplay.getRepaintArea());
            g.repaint(this.title.getRepaintArea());
            //g.repaint(this.map.getRepaintArea());
        }
    }
    
}

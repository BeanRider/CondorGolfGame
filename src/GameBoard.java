import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SpringLayout;
import javax.swing.Timer;

public class GameBoard extends JPanel implements ActionListener {
    
    private static final long serialVersionUID = 997296011586776250L;

    // game loop
    Timer loop;
    
    static final int WIDTH = 1000;
    static final int HEIGHT = 600; // 1000 x 600
    
    GameBoard g = this;
    
    Level hole0;
    
    // GUI Elements
    BufferedImage titleI;
    
    ASprite logo;
    
    JButton startB;
    boolean isInGame = false;
    Timer fadeTitle;
    float titleOpacity = 0f; // opacity of the title screen
    
    Timer fadeGame;
    float gameOpacity = 0f; // opacity of the game
    
    Timer showHoleNum;
    float holeNumOpacity = 0f; // opacity of the hole string
    
    // Sound
    Sound sdtk;
    
    GameBoard() {
        
        this.setLayout(new SpringLayout());
        
        // J-GUIs
        this.startB = new JButton(new ImageIcon(GameBoard.class.getResource("resources/startB.png")));
        startB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                fadeTitle.stop();
                fadeOutTitle();
            }
        });
        this.startB.setBorderPainted(false);
        this.startB.setContentAreaFilled(false);
        this.startB.setFocusPainted(false);
        this.startB.setRolloverEnabled(false);
        this.add(startB);
        
        SpringLayout spL = (SpringLayout) this.getLayout();
        spL.putConstraint(
                SpringLayout.HORIZONTAL_CENTER, this.startB,
                GameBoard.WIDTH / 2,
                SpringLayout.WEST, this);
        spL.putConstraint(
                SpringLayout.NORTH, this.startB,
                GameBoard.HEIGHT / 2 + 100,
                SpringLayout.NORTH, this);
        
        // Game Loop
        loop = new Timer(16, this);
        loop.setInitialDelay(1000);
        
        this.hole0 = new Level(new CurvesUtil().genHole0());
        
        // BufferedImages
        try{
            this.titleI = ImageIO.read(this.getClass().getResource("resources/titleI.png"));
        } catch (IOException ioe){
            ioe.printStackTrace();
        }        
        this.fadeInTitle();
        
        this.sdtk = new Sound("resources/Trial and Error.wav");
        sdtk.play();
        
        this.addKeyListener(new ControlsAdapter());
        this.setFocusable(true);
        this.requestFocus();
        this.setVisible(true);
        
    }
    
    // EFFECT: fades in the title screen, then sets the start button visible
    void fadeInTitle() {
        this.fadeTitle = new Timer(10, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                float increment = 0.005f;
                if (titleOpacity + increment >= 1) {
                    titleOpacity = 1f;
                    startB.setVisible(true);
                    fadeTitle.stop();
                    isInGame = false;
                    repaint();
                }
                else {
                    titleOpacity += increment;
                    repaint();
                }
                
            }
        });
        this.fadeTitle.start();
    }
    
    // EFFECT: fades out the tile screen, then sets the start button invisible
    void fadeOutTitle() {
        this.fadeTitle = new Timer(10, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                float increment = 0.005f;
                if (titleOpacity - increment <= 0) {
                    startB.setVisible(false);
                    fadeTitle.stop();
                    titleOpacity = 0f;
                    // Show game
                    fadeInGame();
                    // Show name in game
                    hole0.fadeInName();
                    isInGame = true;
                    loop.start(); // start game
                    
                    repaint();
                }
                else {
                    titleOpacity -= increment;
                    repaint();
                }
            }
        });
        this.fadeTitle.start();
    }
    
    // EFFECT: fades in the game screen
    void fadeInGame() {
        this.fadeGame = new Timer(16, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                float increment = 0.005f;
                if (gameOpacity + increment >= 1) {
                    gameOpacity = 1f;
                    fadeGame.stop();
                    repaint();
                }
                else {
                    gameOpacity += increment;
                    repaint();
                }
            }
        });
        this.fadeGame.start();
    }
    
    // renders the entire game's sprites, terrain
    @Override
    public void paintComponent(Graphics g){
        
        // Set up
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Bottom Layer: Black Fill
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, WIDTH, HEIGHT);
        
        // Draw the title screen
        if (!this.isInGame) {
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, this.titleOpacity));
            g2d.drawImage(this.titleI, 0, 0, this);
        }
        else if (this.isInGame) {
            // Draw the level
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, this.gameOpacity));
            this.hole0.draw(g2d);
        }
        
    }
    
    // returns the Dimension of this JPanel
    @Override
    public Dimension getPreferredSize(){
        return(new Dimension(WIDTH, HEIGHT));
    }

    // this is the main game loop; the Timer loop calls this every delay
    @Override
    public void actionPerformed(ActionEvent e) {
        
        
        this.hole0.repaintThis(this);
        
        // Steps the ball, the arrow,
        if (this.hole0.hasWon) {
            
        } else {
            this.hole0.updateStep();
        }
        
        
        this.hole0.repaintThis(this);
        
        
    }
    
    private class ControlsAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent ke) {
            int keyCode = ke.getKeyCode();
            if (keyCode == KeyEvent.VK_R) {
                hole0 = new Level(new CurvesUtil().genHole0());
                hole0.fadeInName();
                repaint();
            }
            
            hole0.interactKey(keyCode);
            repaint(hole0.golfer.arrow.getRepaintArea());
        }
    }
}

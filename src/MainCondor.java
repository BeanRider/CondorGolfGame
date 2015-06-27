
import java.awt.GraphicsEnvironment;

import javax.swing.*;

class MainCondor {
    
    public MainCondor() {
        // None; container for main method
    }
    
    // runs the game
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable(){
            public void run(){
                createAndShowGUI();
            }
        });
        
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String []fontNames= ge.getAvailableFontFamilyNames();
        
        for (int i = 0; i < fontNames.length; i++) {
            //System.out.println(fontNames[i]);
        }
        
    }

    // creates the main frame, adds the main board, sets all required attributes of frame,
    // makes frame visible.
    public static void createAndShowGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        } catch (InstantiationException ex) {
            ex.printStackTrace();
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        } catch (UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }
        
        JFrame gameWindow = new JFrame("Condor");
        gameWindow.getContentPane().add(new GameBoard());
        
        gameWindow.setResizable(false);
        gameWindow.setUndecorated(true);
        gameWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        gameWindow.pack();
        gameWindow.setLocationRelativeTo(null);
        gameWindow.setVisible(true);
        
    }
    
    
    
    
}
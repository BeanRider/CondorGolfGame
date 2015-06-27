import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import sun.audio.AudioPlayer;
import sun.audio.AudioStream;

public class Sound {
    
    InputStream is;
    AudioStream as;
    
    public Sound(String s){
        is = this.getClass().getResourceAsStream(s);
        try {
            as = new AudioStream(is);
        } catch (IOException ex) {
            Logger.getLogger(Sound.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    public void play(){
        AudioPlayer.player.start(as);
    }
    
    public void stop(){
        AudioPlayer.player.stop(as);
    }
    
    
    
}
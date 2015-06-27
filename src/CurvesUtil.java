import java.util.ArrayList;


public class CurvesUtil {
    
    ArrayList<Curve> curves;
    Curve c0;
    Curve c1;
    Curve c2;
    Curve c3;
    Curve c4;
    Curve c5;
    Curve c6;
    Curve c7;
    
    static double startingX(ArrayList<Curve> given) {
        return given.get(0).p0.x;
    }
    
    static double startingY(ArrayList<Curve> given) {
        return given.get(0).p0.y;
    }
    
    static double endingX(ArrayList<Curve> given) {
        return given.get(given.size() - 1).p3.x;
    }
    
    static double endingY(ArrayList<Curve> given) {
        return given.get(given.size() - 1).p3.y;
    }
    
    public Terrain genHole0() {
        
        // x, y both in non-computer coords
        Point p00 = new Point(0, 187);
        Point p01 = new Point(20, 187);
        Point p02 = new Point(20, 187);
        Point p03 = new Point(125, 187);
        this.c0 = new Curve(p00, p01, p02, p03);
        
        Point p10 = new Point(125, 187);
        Point p11 = new Point(175, 187);
        Point p12 = new Point(200, 40);
        Point p13 = new Point(294, 40);
        this.c1 = new Curve(p10, p11, p12, p13);
        
        Point p20 = new Point(294, 40);
        Point p21 = new Point(400, 40);
        Point p22 = new Point(400, 436);
        Point p23 = new Point(468, 436);
        
        this.c2 = new Curve(p20, p21, p22, p23);
        
        Point p30 = new Point(468, 436);
        Point p31 = new Point(400+68+68, 436);
        Point p32 = new Point(400+68, 190);
        Point p33 = new Point(634, 190);
        this.c3 = new Curve(p30, p31, p32, p33);
        
        Point p40 = new Point(634, 190);
        Point p41 = new Point(704, 190);
        Point p42 = new Point(664, 230);
        Point p43 = new Point(700, 230);
        this.c4 = new Curve(p40, p41, p42, p43);
        
        Point p50 = new Point(700, 230);
        Point p51 = new Point(700, 230);
        Point p52 = new Point(700, 230);
        Point p53 = new Point(820, 230);
        this.c5 = new Curve(p50, p51, p52, p53);
        
        Point p60 = new Point(820, 230);
        Point p61 = new Point(856, 230);
        Point p62 = new Point(816, 190);
        Point p63 = new Point(886, 190);
        this.c6 = new Curve(p60, p61, p62, p63);
        
        Point p70 = new Point(886, 190);
        Point p71 = new Point(1000, 190);
        Point p72 = new Point(950, 410);
        Point p73 = new Point(1000, 410);
        this.c7 = new Curve(p70, p71, p72, p73);
        
        this.curves = new ArrayList<Curve>();
        this.curves.add(this.c0);
        this.curves.add(this.c1);
        this.curves.add(this.c2);
        this.curves.add(this.c3);
        this.curves.add(this.c4);
        this.curves.add(this.c5);
        this.curves.add(this.c6);
        this.curves.add(this.c7);
        
        return new Terrain(this.curves, GameBoard.WIDTH, GameBoard.HEIGHT);
    }
    
}

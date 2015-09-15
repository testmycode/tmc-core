import org.junit.Test;

import static org.junit.Assert.*;
import fi.helsinki.cs.tmc.edutestutils.Points;

@Points("1.1")
public class MainTest {
    

    public void testaaPieni(String mjono, boolean tulos) {
        if (tulos)
            assertTrue("Merkkijonossa " + mjono + " on kaksi samaa merkkiä peräkkäin, mutta metodisi palauttaa 'false'.",
                       Main.kaksiSamaa(mjono) == tulos);
        else
            assertTrue("Merkkijonossa " + mjono + " ei ole kahta samaa merkkiä peräkkäin, mutta metodisi palauttaa 'true'.",
                       Main.kaksiSamaa(mjono) == tulos);
    }
    
    public void testaaSuuri(String mjono, boolean tulos) {
        assertTrue("Metodisi toimii väärin suurella syötteellä.",
                   Main.kaksiSamaa(mjono) == tulos);
        
    }
    
    @Test(timeout=1000)
    public void esimerkit() {
        testaaPieni("ABAABA", true);
        testaaPieni("XXXXX", true);
        testaaPieni("ABCABC", false);
        testaaPieni("ABABA", false);
    }

    @Test(timeout=1000)
    public void pienet() {
        testaaPieni("A", false);
        testaaPieni("ABABABAB", false);
        testaaPieni("ABABABAA", true);
        testaaPieni("AABABABA", true);
        testaaPieni("AABBAABB", true);
        testaaPieni("ABCDEFGH", false);
        testaaPieni("ABCDCBA", false);
        testaaPieni("ABCDDCBA", true);
    }

    @Test(timeout=1000)
    public void suuri1() {
        int n = 100000;
        char[] t = new char[n];
        for (int i = 0; i < n; i++) t[i] = (char)('A'+(i%2));
        testaaSuuri(new String(t), false);
    }

    @Test(timeout=1000)
    public void suuri2() {
        int n = 100000;
        char[] t = new char[n];
        for (int i = 0; i < n; i++) t[i] = (char)('A'+(i%2));
        t[0] = t[1];
        testaaSuuri(new String(t), true);
    }

    @Test(timeout=1000)
    public void suuri3() {
        int n = 100000;
        char[] t = new char[n];
        for (int i = 0; i < n; i++) t[i] = (char)('A'+(i%2));
        t[n-1] = t[n-2];
        testaaSuuri(new String(t), true);
    }

    @Test(timeout=1000)
    public void suuri4() {
        int n = 100000;
        char[] t = new char[n];
        for (int i = 0; i < n; i++) t[i] = (char)('A'+(i%2));
        t[n/2] = t[n/2-1];
        testaaSuuri(new String(t), true);
    }

}
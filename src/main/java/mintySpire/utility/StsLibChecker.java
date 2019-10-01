package mintySpire.utility;

import com.evacipated.cardcrawl.mod.stslib.powers.interfaces.InvisiblePower;

public class StsLibChecker {
    public static boolean checkInvisiblePower(Object o) {
        return o instanceof InvisiblePower;
    }
}

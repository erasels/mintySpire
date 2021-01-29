package mintySpire.utility;

import com.evacipated.cardcrawl.mod.stslib.powers.interfaces.InvisiblePower;
import com.evacipated.cardcrawl.mod.stslib.powers.abstracts.TwoAmountPower;

public class StsLibChecker {
    public static boolean checkInvisiblePower(Object o) {
        return o instanceof InvisiblePower;
    }
    public static boolean checkTwoAmountPower(Object o) {
        return o instanceof TwoAmountPower;
    }
}

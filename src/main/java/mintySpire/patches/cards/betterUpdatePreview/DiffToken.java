package mintySpire.patches.cards.betterUpdatePreview;

public class DiffToken {
    public boolean isOld;
    public boolean isNew;
    public boolean isSpecial;
    public String value;

    public DiffToken(boolean isOld, boolean isNew, boolean isSpecial, String value) {
        this.isOld = isOld;
        this.isNew = isNew;
        this.isSpecial = isSpecial;
        this.value = value;
    }

    public String toString() {
        return value;
    }

    public boolean isWhitespace() {
        return value.matches("\\s+");
    }
}

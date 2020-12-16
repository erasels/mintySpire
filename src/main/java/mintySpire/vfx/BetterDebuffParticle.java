package mintySpire.vfx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.megacrit.cardcrawl.helpers.Hitbox;
import com.megacrit.cardcrawl.vfx.DebuffParticleEffect;

public class BetterDebuffParticle extends DebuffParticleEffect {
    public BetterDebuffParticle(Hitbox hb, Color c) {
        this(MathUtils.random(hb.x, hb.x+hb.width), MathUtils.random(hb.y, hb.y+hb.height), c);
    }

    public BetterDebuffParticle(float x, float y, Color c) {
        super(x, y);
        color = c.cpy();
    }
}

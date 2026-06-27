package me.dongmyeng.waffle;

import com.destroystokyo.paper.profile.PlayerProfile;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.UUID;
import org.bukkit.profile.PlayerTextures;

public final class WaffleCapes {

    private static final UUID TARGET = UUID.fromString("7eb763a4-7abb-4755-b1f4-186d2e5ac9d2");
    private static final URL CAPE_URL =
        url("http://textures.minecraft.net/texture/9e507afc56359978a3eb3e32367042b853cddd0995d17d0da995662913fb00f7");

    private WaffleCapes() {
    }

    public static void apply(final PlayerProfile profile) {
        if (!TARGET.equals(profile.getId())) {
            return;
        }
        final PlayerTextures textures = profile.getTextures();
        if (textures.getSkin() == null) {
            return; // offline/proxy: no authenticated skin to preserve
        }
        textures.setCape(CAPE_URL);
        profile.setTextures(textures);
    }

    private static URL url(final String value) {
        try {
            return URI.create(value).toURL();
        } catch (final MalformedURLException e) {
            throw new IllegalStateException(e);
        }
    }
}

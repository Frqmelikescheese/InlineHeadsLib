package frqme.isa.headlib;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.Style;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.net.URI;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Renders player heads as inline text components using custom fonts.
 * <p>
 * Requires the Pixelized resource pack to be installed on the client.
 * All components use shadowColor(0,0,0,0) to remove text shadows on MC 1.21.4+.
 * </p>
 */
public class HeadRenderer {

    private static final Key FONT_KEY = Key.key("pixelized", "pixelized");
    private static final String MINOTAR_URL = "https://minotar.net/avatar/%s/8.png";
    private static final Executor EXECUTOR = Executors.newCachedThreadPool();
    private static final Style NO_SHADOW;
    static {
        Style.Builder builder = Style.style();
        try {
            var method = builder.getClass().getMethod("shadowColor", TextColor.class);
            TextColor transparent = TextColor.color(0x00000000);
            method.invoke(builder, transparent);
        } catch (Exception ignored) {
        }
        NO_SHADOW = builder.build();
    }

    private final LoadingCache<String, Component> cache;

    /**
     * Creates a new HeadRenderer with default settings.
     *
     * @param cacheDuration How long to cache heads (default: 10 minutes)
     */
    public HeadRenderer(Duration cacheDuration) {
        this.cache = CacheBuilder.newBuilder()
                .expireAfterAccess(cacheDuration)
                .build(new CacheLoader<>() {
                    @Override
                    public Component load(String playerName) throws Exception {
                        return fetchAndRender(playerName);
                    }
                });
    }

    /**
     * Creates a new HeadRenderer with 10-minute cache.
     */
    public HeadRenderer() {
        this(Duration.ofMinutes(10));
    }

    /**
     * Gets a player's head synchronously. Blocks the calling thread if not cached.
     * <p><b>Warning:</b> Do NOT call from the main server thread!</p>
     *
     * @param playerName The player's name
     * @return The head as a Component, or Component.empty() on error
     */
    public Component getHead(String playerName) {
        try {
            return cache.get(playerName);
        } catch (ExecutionException e) {
            return Component.empty();
        }
    }

    /**
     * Gets a player's head asynchronously. Safe to call from any thread.
     *
     * @param playerName The player's name
     * @return CompletableFuture containing the head Component
     */
    public CompletableFuture<Component> getHeadAsync(String playerName) {
        return CompletableFuture.supplyAsync(() -> getHead(playerName), EXECUTOR);
    }

    /**
     * Pre-caches a player's head. Call this async on join.
     *
     * @param playerName The player's name to pre-cache
     */
    public void warmCache(String playerName) {
        CompletableFuture.runAsync(() -> cache.getUnchecked(playerName), EXECUTOR);
    }

    /**
     * Invalidates a player's cached head. Call on skin change etc.
     *
     * @param playerName The player's name
     */
    public void invalidate(String playerName) {
        cache.invalidate(playerName);
    }

    /**
     * Clears the entire head cache.
     */
    public void invalidateAll() {
        cache.invalidateAll();
    }

    private Component fetchAndRender(String playerName) throws Exception {
        URI uri = new URI(String.format(MINOTAR_URL, playerName));
        BufferedImage image = ImageIO.read(uri.toURL());

        if (image == null) {
            throw new IllegalStateException("Failed to fetch head image for " + playerName);
        }

        TextComponent.Builder component = Component.text("").style(NO_SHADOW).toBuilder();

        for (int i = 1; i <= 64; i++) {
            int row = i == 64 ? 0 : 7 - (i / 8);
            int col = i == 64 ? 7 : (i - 1) % 8;

            if (col == 7 && i < 64) row++;

            int rgb = image.getRGB(col, row) & 0x00FFFFFF;

            component.append(
                    Component.translatable("pixel.eighth-" + i)
                            .font(FONT_KEY)
                            .color(TextColor.color(rgb))
                            .style(NO_SHADOW)
            );
            component.append(
                    Component.translatable("space.-" + ((i % 8) + 1))
                            .style(NO_SHADOW)
            );

            if (i >= 8 && i % 8 == 0 && i != 64) {
                component.append(
                        Component.translatable("space.-8")
                                .style(NO_SHADOW)
                );
            }
        }

        return component.build();
    }

    /**
     * Wraps any component with no-shadow style.
     *
     * @param component The component to wrap
     * @return The component with shadow removed
     */
    public static Component noShadow(Component component) {
        return component.style(NO_SHADOW);
    }

    /**
     * Creates a text component with no shadow.
     *
     * @param text The text content
     * @return A shadow-free text component
     */
    public static Component text(String text) {
        return Component.text(text).style(NO_SHADOW);
    }

    /**
     * Creates a text component with no shadow and color.
     *
     * @param text  The text content
     * @param color The text color
     * @return A shadow-free colored text component
     */
    public static Component text(String text, TextColor color) {
        return Component.text(text, color).style(NO_SHADOW);
    }
}

package frqme.isa.headlib;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Generates the Pixelized resource pack ZIP dynamically.
 * Can extract the built-in pack or generate a fresh one.
 */
public class ResourcePackGenerator {

    private static final String PACK_DESCRIPTION = "Adds the pixels and spacing required for InlineHeads.";
    private static final int PACK_FORMAT = 46;

    /**
     * Generates the resource pack ZIP and writes it to the given output stream.
     *
     * @param out The output stream to write the ZIP to
     * @throws IOException If an I/O error occurs
     */
    public static void generate(OutputStream out) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(out)) {
            writeEntry(zos, "pack.mcmeta", generatePackMcmeta().getBytes(StandardCharsets.UTF_8));
            writeEntry(zos, "assets/pixelized/font/pixelized.json",
                    generatePixelizedFontJson().getBytes(StandardCharsets.UTF_8));
            writeEntry(zos, "assets/space/font/default.json",
                    generateSpaceFontJson().getBytes(StandardCharsets.UTF_8));
            writeEntry(zos, "assets/minecraft/font/default.json",
                    generateMinecraftFontJson().getBytes(StandardCharsets.UTF_8));
            writeEntry(zos, "assets/pixelized/lang/en_us.json",
                    generatePixelizedLangJson().getBytes(StandardCharsets.UTF_8));
            writeEntry(zos, "assets/space/lang/en_us.json",
                    generateSpaceLangJson().getBytes(StandardCharsets.UTF_8));

            byte[] pixelizedPng = loadBundledTexture("pixelized.png");
            if (pixelizedPng != null) {
                writeEntry(zos, "assets/pixelized/textures/font/pixelized.png", pixelizedPng);
            }

            byte[] splitterPng = loadBundledTexture("splitter.png");
            if (splitterPng != null) {
                writeEntry(zos, "assets/space/textures/font/splitter.png", splitterPng);
            }
        }
    }

    /**
     * Generates the resource pack ZIP and saves it to a file.
     *
     * @param file The file to write to
     * @throws IOException If an I/O error occurs
     */
    public static void generate(File file) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            generate(fos);
        }
    }

    /**
     * Generates the resource pack as a byte array.
     *
     * @return The ZIP bytes
     * @throws IOException If an I/O error occurs
     */
    public static byte[] generateBytes() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        generate(baos);
        return baos.toByteArray();
    }

    /**
     * Computes SHA-1 hash of the generated resource pack.
     *
     * @return Hex string of the hash
     * @throws Exception If hashing fails
     */
    public static String getHash() throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        digest.update(generateBytes());
        return HexFormat.of().formatHex(digest.digest());
    }

    private static void writeEntry(ZipOutputStream zos, String name, byte[] data) throws IOException {
        zos.putNextEntry(new ZipEntry(name));
        zos.write(data);
        zos.closeEntry();
    }

    private static byte[] loadBundledTexture(String filename) {
        try (InputStream is = ResourcePackGenerator.class.getResourceAsStream(
                "/assets/pixelized/textures/font/" + filename)) {
            if (is == null) return null;
            return is.readAllBytes();
        } catch (Exception e) {
            return null;
        }
    }

    private static String generatePackMcmeta() {
        return """
                {
                    "pack": {
                        "pack_format": %d,
                        "description": "%s"
                    }
                }
                """.formatted(PACK_FORMAT, PACK_DESCRIPTION);
    }

    private static String generatePixelizedFontJson() {
        StringBuilder chars = new StringBuilder();
        String[] rows = {
                "\uE000\uE001\uE002\uE003\uE004\uE005\uE006\uE007",
                "\uE008\uE009\uE010\uE011\uE012\uE013\uE014\uE015",
                "\uE016\uE017\uE018\uE019\uE020\uE021\uE022\uE023",
                "\uE024\uE025\uE026\uE027\uE028\uE029\uE030\uE031",
                "\uE032\uE033\uE034\uE035\uE036\uE037\uE038\uE039",
                "\uE040\uE041\uE042\uE043\uE044\uE045\uE046\uE047",
                "\uE048\uE049\uE050\uE051\uE052\uE053\uE054\uE055",
                "\uE056\uE057\uE058\uE059\uE060\uE061\uE062\uE063",
                "\uE064\uE065\uE066\uE067\uE068\u0000\u0000\u0000"
        };

        chars.append("[");
        for (int i = 0; i < rows.length; i++) {
            chars.append("\"").append(escapeJson(rows[i])).append("\"");
            if (i < rows.length - 1) chars.append(",");
        }
        chars.append("]");

        return """
                {
                  "providers": [
                    {
                      "type": "bitmap",
                      "file": "pixelized:font/pixelized.png",
                      "ascent": 8,
                      "height": 8,
                      "chars": %s
                    }
                  ]
                }
                """.formatted(chars);
    }

    private static String generateSpaceFontJson() {
        StringBuilder advances = new StringBuilder();
        advances.append("\"-\":-6765,\"9\":1,\"8\":3,\"7\":8,\"6\":21,\"5\":55,\"4\":144,\"3\":377,\"2\":988,\"1\":2584,\"0\":0");

        String[] spaceChars = generateSpaceChars();
        for (int i = 0; i < spaceChars.length; i++) {
            int width = 2584 - (i * 38);
            advances.append(",\"").append(escapeJson(spaceChars[i])).append("\":").append(Math.max(width, 1));
        }

        return """
                {
                  "providers": [
                    {
                      "type": "space",
                      "advances": {%s}
                    }
                  ]
                }
                """.formatted(advanceBuilder());
    }

    private static String advanceBuilder() {
        StringBuilder sb = new StringBuilder();
        sb.append("\"-\":-6765,\"9\":1,\"8\":3,\"7\":8,\"6\":21,\"5\":55,\"4\":144,\"3\":377,\"2\":988,\"1\":2584,\"0\":0");

        String[] spaceChars = generateSpaceChars();
        for (int i = 0; i < spaceChars.length; i++) {
            int width = 2584 - (i * 38);
            sb.append(",\"").append(escapeJson(spaceChars[i])).append("\":").append(Math.max(width, 1));
        }

        return sb.toString();
    }

    private static String[] generateSpaceChars() {
        String[] chars = new String[256];
        for (int i = 0; i < 256; i++) {
            chars[i] = new String(Character.toChars(0xF0000 + i));
        }
        return chars;
    }

    private static String generateMinecraftFontJson() {
        return """
                {
                  "providers": [
                    {
                      "type": "space",
                      "advances": {
                        "-1": 1
                      }
                    }
                  ]
                }
                """;
    }

    private static String generatePixelizedLangJson() {
        StringBuilder entries = new StringBuilder();
        String[] pixelChars = generatePixelChars();

        for (int i = 0; i < pixelChars.length && i < 64; i++) {
            entries.append("\"pixel.eighth-").append(i + 1).append("\": \"")
                    .append(escapeJson(pixelChars[i])).append("\"");
            if (i < Math.min(pixelChars.length, 64) - 1) entries.append(",");
            entries.append("\n");
        }

        return "{\n%s\n}".formatted(entries);
    }

    private static String generateSpaceLangJson() {
        StringBuilder entries = new StringBuilder();
        for (int i = 1; i <= 256; i++) {
            String unicode = new String(Character.toChars(0xF0000 + (i - 1)));
            entries.append("\"space.-").append(i).append("\": \"")
                    .append(escapeJson(unicode)).append("\"");
            if (i < 256) entries.append(",");
            entries.append("\n");
        }
        return "{\n%s\n}".formatted(entries);
    }

    private static String[] generatePixelChars() {
        String[] chars = new String[64];
        for (int i = 0; i < 64; i++) {
            chars[i] = new String(Character.toChars(0xE000 + i));
        }
        return chars;
    }

    private static String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}

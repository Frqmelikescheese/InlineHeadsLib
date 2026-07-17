package frqme.isa.headlib;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

/**
 * Built-in HTTP server for serving the resource pack and preview page.
 * <p>
 * Usage:
 * <pre>
 * PackServer server = new PackServer(8080);
 * server.start();
 * System.out.println("Pack URL: " + server.getPackUrl());
 * </pre>
 */
public class PackServer {

    private final int port;
    private HttpServer server;
    private byte[] packBytes;
    private String packHash;

    /**
     * Creates a new PackServer on the given port.
     *
     * @param port The port to listen on
     */
    public PackServer(int port) {
        this.port = port;
    }

    /**
     * Starts the server and generates the resource pack.
     *
     * @throws IOException If the server fails to start
     */
    public void start() throws IOException {
        packBytes = ResourcePackGenerator.generateBytes();
        try {
            packHash = ResourcePackGenerator.getHash();
        } catch (Exception e) {
            packHash = "";
        }

        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/pack.zip", this::handlePack);
        server.createContext("/hash", this::handleHash);
        server.createContext("/", this::handlePreview);
        server.setExecutor(null);
        server.start();
    }

    /**
     * Stops the server.
     */
    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }

    /**
     * Gets the URL to download the resource pack.
     *
     * @return Pack URL string
     */
    public String getPackUrl() {
        return "http://localhost:" + port + "/pack.zip";
    }

    /**
     * Gets the SHA-1 hash of the resource pack.
     *
     * @return Hex string of the hash
     */
    public String getPackHash() {
        return packHash;
    }

    /**
     * Gets the port the server is listening on.
     *
     * @return The port number
     */
    public int getPort() {
        return port;
    }

    private void handlePack(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/zip");
        exchange.getResponseHeaders().set("Content-Disposition", "attachment; filename=\"Pixelized.zip\"");
        exchange.sendResponseHeaders(200, packBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(packBytes);
        }
    }

    private void handleHash(HttpExchange exchange) throws IOException {
        byte[] response = packHash.getBytes();
        exchange.getResponseHeaders().set("Content-Type", "text/plain");
        exchange.sendResponseHeaders(200, response.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response);
        }
    }

    private void handlePreview(HttpExchange exchange) throws IOException {
        byte[] response = getPreviewHtml().getBytes();
        exchange.getResponseHeaders().set("Content-Type", "text/html");
        exchange.sendResponseHeaders(200, response.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response);
        }
    }

    private String getPreviewHtml() {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <title>InlineHeads Preview</title>
                    <style>
                        body { font-family: monospace; background: #1a1a2e; color: #eee; padding: 20px; }
                        h1 { color: #e94560; }
                        input { padding: 8px; font-size: 16px; background: #16213e; color: #eee; border: 1px solid #e94560; }
                        button { padding: 8px 16px; font-size: 16px; background: #e94560; color: white; border: none; cursor: pointer; }
                        #preview { margin-top: 20px; padding: 20px; background: #16213e; border-radius: 8px; display: inline-block; }
                        .info { margin-top: 20px; padding: 15px; background: #0f3460; border-radius: 8px; }
                        a { color: #e94560; }
                    </style>
                </head>
                <body>
                    <h1>InlineHeads Preview</h1>
                    <p>Enter a player name to preview their head rendering:</p>
                    <input type="text" id="playerName" placeholder="Player name" value="Notch">
                    <button onclick="fetchHead()">Preview</button>
                    <div id="preview"></div>
                    <div class="info">
                        <h3>Quick Start</h3>
                        <p>1. Download: <a href="/pack.zip">Pixelized Resource Pack</a></p>
                        <p>2. Host the pack and set in <code>server.properties</code>:</p>
                        <pre>resource-pack=%s</pre>
                        <pre>resource-pack-sha1=%s</pre>
                        <p>3. Add to your plugin:</p>
                        <pre>implementation("frqme.isa:InlineHeadsLib:1.0.0")</pre>
                    </div>
                    <script>
                        async function fetchHead() {
                            const name = document.getElementById('playerName').value;
                            const preview = document.getElementById('preview');
                            preview.innerHTML = '<p>Loading...</p>';

                            try {
                                const resp = await fetch('https://minotar.net/avatar/' + name + '/8.png');
                                const blob = await resp.blob();
                                const img = new Image();
                                img.src = URL.createObjectURL(blob);
                                img.onload = () => {
                                    const canvas = document.createElement('canvas');
                                    canvas.width = 128;
                                    canvas.height = 128;
                                    canvas.style.imageRendering = 'pixelated';
                                    canvas.style.width = '128px';
                                    canvas.style.height = '128px';
                                    const ctx = canvas.getContext('2d');
                                    ctx.imageSmoothingEnabled = false;
                                    ctx.drawImage(img, 0, 0, 128, 128);
                                    preview.innerHTML = '<p><b>' + name + '</b> head preview (8x8 upscaled):</p>';
                                    preview.appendChild(canvas);
                                };
                            } catch (e) {
                                preview.innerHTML = '<p>Error: ' + e.message + '</p>';
                            }
                        }
                        fetchHead();
                    </script>
                </body>
                </html>
                """.formatted(getPackUrl(), packHash);
    }
}

package network;

import packets.*;
import java.io.*;
import java.net.*;
import java.util.concurrent.*;

/**
 * ⭐ CLIENTE DEL JUEGO (JUGADOR 2) - CON SOPORTE PARA TIENDA
 */
public class GameClient {
    
    private Socket socket;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    
    private boolean running = false;
    private boolean conectado = false;
    
    private String ip;
    private int puerto;
    private String nombreJugador;
    
    private GameStatePacket ultimoEstado = null;
    private final Object estadoLock = new Object();
    
    private LevelChangePacket ultimoCambioNivel = null;
    private final Object nivelLock = new Object();
    
    private PlayerDeathPacket ultimaMuerte = null;
    private final Object muerteLock = new Object();
    
    // ⭐ COLA PARA CONFIRMACIONES DE COMPRA
    private ConcurrentLinkedQueue<ShopPurchasePacket> colaConfirmacionesCompra = new ConcurrentLinkedQueue<>();
    
    private ExecutorService executor;
    private NetworkCallback callback;
    
    public interface NetworkCallback {
        void onConexionExitosa();
        void onConexionFallida(String razon);
        void onEstadoRecibido(GameStatePacket estado);
        void onDesconectado(String razon);
        void onError(String error);
        void onCambioNivel(LevelChangePacket cambioNivel);
        void onMuerteJugador(PlayerDeathPacket muerte);
        void onConfirmacionCompra(ShopPurchasePacket compra);
    }
    
    public GameClient(String ip, int puerto, String nombreJugador, NetworkCallback callback) {
        this.ip = ip;
        this.puerto = puerto;
        this.nombreJugador = nombreJugador;
        this.callback = callback;
        this.executor = Executors.newSingleThreadExecutor();
    }
    
    public void conectar() {
        executor.submit(() -> {
            try {
                System.out.println("🔌 Conectando a " + ip + ":" + puerto + "...");
                
                socket = new Socket();
                socket.connect(new InetSocketAddress(ip, puerto), 5000);
                
                outputStream = new ObjectOutputStream(socket.getOutputStream());
                outputStream.flush();
                inputStream = new ObjectInputStream(socket.getInputStream());
                
                ConnectionPacket conexion = new ConnectionPacket(nombreJugador, 1, false);
                outputStream.writeObject(conexion);
                outputStream.flush();
                
                Object obj = inputStream.readObject();
                if (obj instanceof ConnectionPacket) {
                    ConnectionPacket confirmacion = (ConnectionPacket) obj;
                    if (confirmacion.isAceptado()) {
                        conectado = true;
                        running = true;
                        
                        System.out.println("✅ Conectado exitosamente como " + nombreJugador);
                        
                        if (callback != null) {
                            callback.onConexionExitosa();
                        }
                        
                        iniciarEscucha();
                    } else {
                        throw new IOException("Conexión rechazada por el servidor");
                    }
                }
                
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("❌ Error al conectar: " + e.getMessage());
                if (callback != null) {
                    callback.onConexionFallida(e.getMessage());
                }
            }
        });
    }
    
    private void iniciarEscucha() {
        executor.submit(() -> {
            while (running && conectado) {
                try {
                    Object obj = inputStream.readObject();
                    
                    if (obj instanceof GameStatePacket) {
                        GameStatePacket estado = (GameStatePacket) obj;
                        synchronized (estadoLock) {
                            ultimoEstado = estado;
                        }
                        
                        if (callback != null) {
                            callback.onEstadoRecibido(estado);
                        }
                    
                    } else if (obj instanceof LevelChangePacket) {
                        LevelChangePacket cambioNivel = (LevelChangePacket) obj;
                        synchronized (nivelLock) {
                            ultimoCambioNivel = cambioNivel;
                        }
                        
                        System.out.println("📥 Cambio de nivel recibido del servidor");
                        
                        if (callback != null) {
                            callback.onCambioNivel(cambioNivel);
                        }
                    
                    } else if (obj instanceof PlayerDeathPacket) {
                        PlayerDeathPacket muerte = (PlayerDeathPacket) obj;
                        synchronized (muerteLock) {
                            ultimaMuerte = muerte;
                        }
                        
                        System.out.println("💀 Muerte recibida del servidor");
                        
                        if (callback != null) {
                            callback.onMuerteJugador(muerte);
                        }
                    
                    } else if (obj instanceof ShopPurchasePacket) {
                        ShopPurchasePacket compra = (ShopPurchasePacket) obj;
                        System.out.println("🛒 [CLIENTE] Confirmación de compra recibida");
                        
                        colaConfirmacionesCompra.offer(compra);
                        
                        if (callback != null) {
                            callback.onConfirmacionCompra(compra);
                        }
                        
                    } else if (obj instanceof DisconnectPacket) {
                        DisconnectPacket disconnect = (DisconnectPacket) obj;
                        System.out.println("⚠️ Desconectado del servidor: " + disconnect.getRazon());
                        conectado = false;
                        
                        if (callback != null) {
                            callback.onDesconectado(disconnect.getRazon());
                        }
                    }
                    
                } catch (EOFException e) {
                    System.out.println("⚠️ Servidor cerró la conexión");
                    conectado = false;
                    if (callback != null) {
                        callback.onDesconectado("Servidor cerrado");
                    }
                    break;
                    
                } catch (IOException | ClassNotFoundException e) {
                    if (running && conectado) {
                        System.err.println("❌ Error al recibir datos: " + e.getMessage());
                        conectado = false;
                        if (callback != null) {
                            callback.onDesconectado("Error de red");
                        }
                    }
                    break;
                }
            }
        });
    }
    
    public void enviarInput(PlayerInputPacket input) {
        if (!conectado || outputStream == null) {
            return;
        }
        
        try {
            outputStream.writeObject(input);
            outputStream.flush();
            
        } catch (IOException e) {
            System.err.println("❌ Error al enviar input: " + e.getMessage());
            conectado = false;
            if (callback != null) {
                callback.onDesconectado("Error al enviar datos");
            }
        }
    }
    
    /**
     * ⭐ NUEVO: Envía una compra al servidor
     */
    public void enviarCompra(ShopPurchasePacket compra) {
        if (!conectado || outputStream == null) {
            System.err.println("⚠️ No se puede enviar compra: no conectado");
            return;
        }
        
        try {
            outputStream.writeObject(compra);
            outputStream.flush();
            System.out.println("📤 [CLIENTE] Compra enviada al servidor");
            
        } catch (IOException e) {
            System.err.println("❌ Error al enviar compra: " + e.getMessage());
            conectado = false;
            if (callback != null) {
                callback.onDesconectado("Error al enviar compra");
            }
        }
    }
    
    public GameStatePacket obtenerEstadoJuego() {
        synchronized (estadoLock) {
            return ultimoEstado;
        }
    }
    
    public boolean estaConectado() {
        return conectado;
    }
    
    public LevelChangePacket obtenerCambioNivel() {
        synchronized (nivelLock) {
            LevelChangePacket cambio = ultimoCambioNivel;
            ultimoCambioNivel = null;
            return cambio;
        }
    }
    
    public PlayerDeathPacket obtenerMuerte() {
        synchronized (muerteLock) {
            PlayerDeathPacket muerte = ultimaMuerte;
            ultimaMuerte = null;
            return muerte;
        }
    }
    
    /**
     * ⭐ NUEVO: Obtiene la siguiente confirmación de compra
     */
    public ShopPurchasePacket obtenerConfirmacionCompra() {
        return colaConfirmacionesCompra.poll();
    }
    
    public void desconectar() {
        running = false;
        conectado = false;
        
        System.out.println("🛑 Desconectando cliente...");
        
        // Enviar paquete de desconexión
        try {
            if (outputStream != null) {
                DisconnectPacket disconnect = new DisconnectPacket(1, "Cliente desconectado");
                outputStream.writeObject(disconnect);
                outputStream.flush();
            }
        } catch (IOException e) {
            // Ignorar - el servidor puede estar cerrado
        }
        
        // 🔥 CERRAR EN ORDEN CORRECTO
        try {
            if (inputStream != null) {
                inputStream.close();
                System.out.println("✅ InputStream cerrado");
            }
        } catch (IOException e) {
            System.err.println("⚠️ Error al cerrar inputStream: " + e.getMessage());
        }
        
        try {
            if (outputStream != null) {
                outputStream.close();
                System.out.println("✅ OutputStream cerrado");
            }
        } catch (IOException e) {
            System.err.println("⚠️ Error al cerrar outputStream: " + e.getMessage());
        }
        
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
                System.out.println("✅ Socket cerrado");
            }
        } catch (IOException e) {
            System.err.println("⚠️ Error al cerrar socket: " + e.getMessage());
        }
        
        // 🔥 APAGAR EXECUTOR AL FINAL
        if (executor != null) {
            try {
                executor.shutdownNow();
                if (!executor.awaitTermination(2, java.util.concurrent.TimeUnit.SECONDS)) {
                    System.err.println("⚠️ Executor no terminó a tiempo");
                } else {
                    System.out.println("✅ Executor detenido");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("⚠️ Interrupción al detener executor");
            }
        }
        
        System.out.println("🛑 Cliente completamente desconectado");
    }
}
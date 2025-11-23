package network;

import packets.*;
import java.io.*;
import java.net.*;
import java.util.concurrent.*;

/**
 * ⭐ SERVIDOR DEL JUEGO (HOST - JUGADOR 1) - CON SOPORTE PARA TIENDA
 */
public class GameServer {
    
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    
    private boolean running = false;
    private boolean clienteConectado = false;
    
    private int puerto;
    private String nombreCliente = "Jugador 2";
    
    private PlayerInputPacket ultimoInputCliente = null;
    private final Object inputLock = new Object();
    
    // ⭐ COLA PARA COMPRAS DE LA TIENDA
    private ConcurrentLinkedQueue<ShopPurchasePacket> colaCompras = new ConcurrentLinkedQueue<>();
    
    private ExecutorService executor;
    private NetworkCallback callback;
    
    public interface NetworkCallback {
        void onClienteConectado(String nombreCliente);
        void onClienteDesconectado(String razon);
        void onInputRecibido(PlayerInputPacket input);
        void onError(String error);
        void onCompraRecibida(ShopPurchasePacket compra);
    }
    
    public GameServer(int puerto, NetworkCallback callback) {
        this.puerto = puerto;
        this.callback = callback;
        this.executor = Executors.newSingleThreadExecutor();
    }
    
    public void iniciar() {
        try {
            // 🔥🔥🔥 HABILITAR SO_REUSEADDR ANTES DE BIND 🔥🔥🔥
            serverSocket = new ServerSocket();
            serverSocket.setReuseAddress(true);
            serverSocket.bind(new InetSocketAddress(puerto));
            running = true;
            
            System.out.println("🎮 Servidor iniciado en puerto " + puerto + " (SO_REUSEADDR habilitado)");
            System.out.println("⏳ Esperando que un jugador se conecte...");
            
            executor.submit(() -> {
                try {
                    clientSocket = serverSocket.accept();
                    
                    outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
                    outputStream.flush();
                    inputStream = new ObjectInputStream(clientSocket.getInputStream());
                    
                    Object obj = inputStream.readObject();
                    if (obj instanceof ConnectionPacket) {
                        ConnectionPacket conexion = (ConnectionPacket) obj;
                        nombreCliente = conexion.getNombreJugador();
                        
                        ConnectionPacket confirmacion = new ConnectionPacket(nombreCliente, 1, true);
                        outputStream.writeObject(confirmacion);
                        outputStream.flush();
                        
                        clienteConectado = true;
                        
                        System.out.println("✅ Cliente conectado: " + nombreCliente);
                        System.out.println("🌐 IP: " + clientSocket.getInetAddress().getHostAddress());
                        
                        if (callback != null) {
                            callback.onClienteConectado(nombreCliente);
                        }
                        
                        iniciarEscucha();
                    }
                    
                } catch (IOException | ClassNotFoundException e) {
                    System.err.println("❌ Error al aceptar conexión: " + e.getMessage());
                    if (callback != null) {
                        callback.onError("Error de conexión: " + e.getMessage());
                    }
                }
            });
            
        } catch (IOException e) {
            System.err.println("❌ Error al iniciar servidor: " + e.getMessage());
            if (callback != null) {
                callback.onError("Error al iniciar servidor: " + e.getMessage());
            }
        }
    }
    
    private void iniciarEscucha() {
        executor.submit(() -> {
            while (running && clienteConectado) {
                try {
                    Object obj = inputStream.readObject();
                    
                    if (obj instanceof PlayerInputPacket) {
                        PlayerInputPacket input = (PlayerInputPacket) obj;
                        synchronized (inputLock) {
                            ultimoInputCliente = input;
                        }
                        
                        if (callback != null) {
                            callback.onInputRecibido(input);
                        }
                        
                    } else if (obj instanceof ShopPurchasePacket) {
                        ShopPurchasePacket compra = (ShopPurchasePacket) obj;
                        System.out.println("🛒 [SERVIDOR] Compra recibida del cliente:");
                        System.out.println("   → Tipo: " + compra.getTipoCompra());
                        System.out.println("   → Item: " + compra.getNombreItem());
                        System.out.println("   → Precio: " + compra.getPrecio());
                        
                        colaCompras.offer(compra);
                        
                        if (callback != null) {
                            callback.onCompraRecibida(compra);
                        }
                        
                    } else if (obj instanceof DisconnectPacket) {
                        DisconnectPacket disconnect = (DisconnectPacket) obj;
                        System.out.println("⚠️ Cliente desconectado: " + disconnect.getRazon());
                        clienteConectado = false;
                        
                        if (callback != null) {
                            callback.onClienteDesconectado(disconnect.getRazon());
                        }
                    }
                    
                } catch (EOFException e) {
                    System.out.println("⚠️ Cliente cerró la conexión");
                    clienteConectado = false;
                    if (callback != null) {
                        callback.onClienteDesconectado("Conexión cerrada");
                    }
                    break;
                    
                } catch (IOException | ClassNotFoundException e) {
                    if (running && clienteConectado) {
                        System.err.println("❌ Error al recibir datos: " + e.getMessage());
                        clienteConectado = false;
                        if (callback != null) {
                            callback.onClienteDesconectado("Error de red");
                        }
                    }
                    break;
                }
            }
        });
    }
    
    public void enviarEstadoJuego(GameStatePacket estado) {
        if (!clienteConectado || outputStream == null) {
            return;
        }
        
        try {
            outputStream.reset();
            outputStream.writeObject(estado);
            outputStream.flush();
            
        } catch (IOException e) {
            System.err.println("❌ Error al enviar estado del juego: " + e.getMessage());
            clienteConectado = false;
            if (callback != null) {
                callback.onClienteDesconectado("Error al enviar datos");
            }
        }
    }
    
    public void enviarCambioNivel(LevelChangePacket cambioNivel) {
        if (!clienteConectado || outputStream == null) {
            System.err.println("⚠️ No se puede enviar cambio de nivel: cliente no conectado");
            return;
        }
        
        try {
            outputStream.reset();
            outputStream.writeObject(cambioNivel);
            outputStream.flush();
            System.out.println("📤 Cambio de nivel enviado al cliente");
            
        } catch (IOException e) {
            System.err.println("❌ Error al enviar cambio de nivel: " + e.getMessage());
            clienteConectado = false;
            if (callback != null) {
                callback.onClienteDesconectado("Error al enviar cambio de nivel");
            }
        }
    }
    
    public void enviarMuerteJugador(PlayerDeathPacket muerte) {
        if (!clienteConectado || outputStream == null) {
            System.err.println("⚠️ No se puede enviar muerte: cliente no conectado");
            return;
        }
        
        try {
            outputStream.reset();
            outputStream.writeObject(muerte);
            outputStream.flush();
            System.out.println("💀 Muerte enviada al cliente");
            
        } catch (IOException e) {
            System.err.println("❌ Error al enviar muerte: " + e.getMessage());
            clienteConectado = false;
            if (callback != null) {
                callback.onClienteDesconectado("Error al enviar muerte");
            }
        }
    }
    
    public void enviarConfirmacionCompra(ShopPurchasePacket compra) {
        if (!clienteConectado || outputStream == null) {
            System.err.println("⚠️ No se puede enviar confirmación de compra");
            return;
        }
        
        try {
            outputStream.reset();
            outputStream.writeObject(compra);
            outputStream.flush();
            System.out.println("📤 Confirmación de compra enviada al cliente");
            
        } catch (IOException e) {
            System.err.println("❌ Error al enviar confirmación de compra: " + e.getMessage());
        }
    }
    
    public PlayerInputPacket obtenerInputCliente() {
        synchronized (inputLock) {
            return ultimoInputCliente;
        }
    }
    
    public ShopPurchasePacket obtenerCompraPendiente() {
        return colaCompras.poll();
    }
    
    public boolean hayComprasPendientes() {
        return !colaCompras.isEmpty();
    }
    
    public boolean hayClienteConectado() {
        return clienteConectado;
    }
    
    public String getNombreCliente() {
        return nombreCliente;
    }
    
    // 🔥🔥🔥 MÉTODO DETENER CORREGIDO 🔥🔥🔥
    public void detener() {
        running = false;
        clienteConectado = false;
        
        System.out.println("🛑 Deteniendo servidor...");
        
        // Enviar paquete de desconexión al cliente
        try {
            if (outputStream != null) {
                DisconnectPacket disconnect = new DisconnectPacket(0, "Servidor cerrado");
                outputStream.writeObject(disconnect);
                outputStream.flush();
            }
        } catch (IOException e) {
            // Ignorar - el cliente puede estar desconectado
        }
        
        // 🔥 CERRAR STREAMS EN ORDEN
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
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
                System.out.println("✅ Cliente socket cerrado");
            }
        } catch (IOException e) {
            System.err.println("⚠️ Error al cerrar clientSocket: " + e.getMessage());
        }
        
        // 🔥🔥🔥 CRUCIAL: Cerrar ServerSocket ANTES de apagar executor 🔥🔥🔥
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                System.out.println("✅ ServerSocket cerrado - Puerto " + puerto + " liberado");
            }
        } catch (IOException e) {
            System.err.println("⚠️ Error al cerrar serverSocket: " + e.getMessage());
        }
        
        // 🔥 APAGAR EXECUTOR AL FINAL
        if (executor != null) {
            try {
                executor.shutdownNow();
                if (!executor.awaitTermination(2, TimeUnit.SECONDS)) {
                    System.err.println("⚠️ Executor no terminó a tiempo");
                } else {
                    System.out.println("✅ Executor detenido");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("⚠️ Interrupción al detener executor");
            }
        }
        
        System.out.println("🛑 Servidor completamente detenido - Puerto libre");
    }
}
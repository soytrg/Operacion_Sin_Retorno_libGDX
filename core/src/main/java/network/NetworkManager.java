package network;

import packets.*;
import configuracion.ConfiguracionJuego;
import jugador.Jugador;

/**
 * ⭐ GESTOR DE RED - CON SOPORTE COMPLETO PARA TIENDA
 */
public class NetworkManager {
    
    private GameServer servidor;
    private GameClient cliente;
    
    private boolean esHost;
    private boolean conectado;
    private boolean iniciado;
    
    private ConfiguracionJuego config;
    
    private NetworkCallback callback;
    
    public interface NetworkCallback {
        void onClienteConectado(String nombreCliente);
        void onClienteDesconectado(String razon);
        void onConexionExitosa();
        void onConexionFallida(String razon);
        void onCambioNivel(LevelChangePacket cambioNivel);
        void onMuerteJugador(PlayerDeathPacket muerte);
        void onCompraEnTienda(ShopPurchasePacket compra);
    }
    
    public NetworkManager(ConfiguracionJuego config, NetworkCallback callback) {
        this.config = config;
        this.callback = callback;
        this.conectado = false;
        this.iniciado = false;
    }
    
    public void iniciar() {
        if (iniciado) return;
        
        if (config.esHost()) {
            iniciarComoHost(9999);
        } else {
            String nombreJugador = config.getNombreJugador() != null ? config.getNombreJugador() : "Jugador 2";
            conectarComoCliente(config.getIpServidor(), 9999, nombreJugador);
        }
        
        iniciado = true;
    }
    
    private void iniciarComoHost(int puerto) {
        esHost = true;
        
        servidor = new GameServer(puerto, new GameServer.NetworkCallback() {
            @Override
            public void onClienteConectado(String nombreCliente) {
                conectado = true;
                System.out.println("✅ NetworkManager: Cliente conectado - " + nombreCliente);
                if (callback != null) {
                    callback.onClienteConectado(nombreCliente);
                }
            }
            
            @Override
            public void onClienteDesconectado(String razon) {
                conectado = false;
                System.out.println("⚠️ NetworkManager: Cliente desconectado - " + razon);
                if (callback != null) {
                    callback.onClienteDesconectado(razon);
                }
            }
            
            @Override
            public void onInputRecibido(PlayerInputPacket input) {
                // Se maneja directamente en GameServer
            }
            
            @Override
            public void onError(String error) {
                System.err.println("❌ NetworkManager: Error en servidor - " + error);
            }
            
            @Override
            public void onCompraRecibida(ShopPurchasePacket compra) {
                System.out.println("🛒 NetworkManager: Compra recibida del cliente");
                if (callback != null) {
                    callback.onCompraEnTienda(compra);
                }
            }
        });
        
        servidor.iniciar();
    }
    
    private void conectarComoCliente(String ip, int puerto, String nombreJugador) {
        esHost = false;
        
        cliente = new GameClient(ip, puerto, nombreJugador, new GameClient.NetworkCallback() {
            @Override
            public void onConexionExitosa() {
                conectado = true;
                System.out.println("✅ NetworkManager: Conexión exitosa");
                if (callback != null) {
                    callback.onConexionExitosa();
                }
            }
            
            @Override
            public void onConexionFallida(String razon) {
                conectado = false;
                System.err.println("❌ NetworkManager: Conexión fallida - " + razon);
                if (callback != null) {
                    callback.onConexionFallida(razon);
                }
            }
            
            @Override
            public void onEstadoRecibido(GameStatePacket estado) {
                // Se maneja directamente en GameClient
            }
            
            @Override
            public void onCambioNivel(LevelChangePacket cambioNivel) {
                System.out.println("🔄 NetworkManager: Cambio de nivel recibido - Nivel " + cambioNivel.getSiguienteNivel());
                if (callback != null) {
                    callback.onCambioNivel(cambioNivel);
                }
            }
            
            @Override
            public void onMuerteJugador(PlayerDeathPacket muerte) {
                System.out.println("💀 NetworkManager: Muerte recibida - Jugador " + (muerte.getIdJugadorMuerto() + 1));
                if (callback != null) {
                    callback.onMuerteJugador(muerte);
                }
            }
            
            @Override
            public void onConfirmacionCompra(ShopPurchasePacket compra) {
                System.out.println("🛒 NetworkManager: Confirmación de compra recibida");
                if (callback != null) {
                    callback.onCompraEnTienda(compra);
                }
            }
            
            @Override
            public void onDesconectado(String razon) {
                conectado = false;
                System.out.println("⚠️ NetworkManager: Desconectado - " + razon);
                if (callback != null) {
                    callback.onClienteDesconectado(razon);
                }
            }
            
            @Override
            public void onError(String error) {
                System.err.println("❌ NetworkManager: Error en cliente - " + error);
            }
        });
        
        cliente.conectar();
    }
    
    public void enviarEstadoJuego(GameStatePacket estado) {
        if (esHost && servidor != null && conectado) {
            servidor.enviarEstadoJuego(estado);
        }
    }
    
    public void enviarCambioNivel(int nivelActual, int siguienteNivel, int jugadorQueCompleto) {
        if (esHost && servidor != null && conectado) {
            LevelChangePacket cambioNivel = new LevelChangePacket(nivelActual, siguienteNivel, jugadorQueCompleto);
            servidor.enviarCambioNivel(cambioNivel);
            System.out.println("📤 NetworkManager: Enviando cambio de nivel al cliente");
        } else {
            System.err.println("⚠️ No se puede enviar cambio de nivel");
        }
    }
    
    public void enviarMuerteJugador(int idJugadorMuerto, boolean ambosJugadoresMuertos) {
        if (esHost && servidor != null && conectado) {
            PlayerDeathPacket muerte = new PlayerDeathPacket(idJugadorMuerto, ambosJugadoresMuertos);
            servidor.enviarMuerteJugador(muerte);
            System.out.println("📤 NetworkManager: Enviando muerte al cliente");
        }
    }
    
    /**
     * ⭐ ENVIAR COMPRA EN LA TIENDA
     */
    public void enviarCompraTienda(ShopPurchasePacket compra) {
        if (esHost && servidor != null && conectado) {
            servidor.enviarConfirmacionCompra(compra);
            System.out.println("📤 NetworkManager [HOST]: Notificación de compra enviada");
        } else if (!esHost && cliente != null && conectado) {
            cliente.enviarCompra(compra);
            System.out.println("📤 NetworkManager [CLIENTE]: Solicitud de compra enviada");
        }
    }
    
    /**
     * ⭐ OBTENER COMPRA PENDIENTE DEL CLIENTE (SOLO HOST)
     */
    public ShopPurchasePacket obtenerCompraPendiente() {
        if (esHost && servidor != null) {
            return servidor.obtenerCompraPendiente();
        }
        return null;
    }
    
    /**
     * ⭐ VERIFICAR SI HAY COMPRAS PENDIENTES (SOLO HOST)
     */
    public boolean hayComprasPendientes() {
        if (esHost && servidor != null) {
            return servidor.hayComprasPendientes();
        }
        return false;
    }

    public LevelChangePacket obtenerCambioNivel() {
        if (!esHost && cliente != null) {
            return cliente.obtenerCambioNivel();
        }
        return null;
    }
    
    public PlayerDeathPacket obtenerMuerte() {
        if (!esHost && cliente != null) {
            return cliente.obtenerMuerte();
        }
        return null;
    }
    
    public void enviarInput(PlayerInputPacket input) {
        if (!esHost && cliente != null && conectado) {
            cliente.enviarInput(input);
        }
    }
    
    public PlayerInputPacket obtenerInputCliente() {
        if (esHost && servidor != null) {
            return servidor.obtenerInputCliente();
        }
        return null;
    }
    
    public GameStatePacket obtenerEstadoJuego() {
        if (!esHost && cliente != null) {
            return cliente.obtenerEstadoJuego();
        }
        return null;
    }
    
    public boolean estaConectado() {
        return conectado;
    }
    
    public boolean esHost() {
        return esHost;
    }
    
    public void detener() {
        System.out.println("🛑 NetworkManager.detener() llamado");
        
        conectado = false;
        iniciado = false;
        
        if (esHost && servidor != null) {
            System.out.println("🔌 Deteniendo servidor...");
            servidor.detener();
            servidor = null;
            System.out.println("✅ Servidor detenido");
        }
        
        if (!esHost && cliente != null) {
            System.out.println("🔌 Desconectando cliente...");
            cliente.desconectar();
            cliente = null;
            System.out.println("✅ Cliente desconectado");
        }
        
        System.out.println("🛑 NetworkManager completamente detenido");
    }
    
    public String getNombreOtroJugador() {
        if (esHost && servidor != null) {
            return servidor.getNombreCliente();
        } else {
            return "Host";
        }
    }
    
    public static PlayerDataPacket crearPlayerDataPacket(Jugador jugador, int idJugador) {
        return new PlayerDataPacket(
            idJugador,
            jugador.getX(),
            jugador.getY(),
            jugador.getVidas(),
            jugador.getSaludActual(),
            jugador.getSaludMaxima(),
            jugador.getSaludMental(),
            jugador.getMunicionActual(),
            jugador.getMunicionMaxima(),
            jugador.getMunicionReserva(),
            jugador.getNombreArmaActual(),
            jugador.estaRecargando(),
            jugador.getProgresoRecarga(),
            jugador.getMirandoDerecha(),
            jugador.estaAgachado(),
            jugador.estaEnSuelo(),
            jugador.estaAgarradoPalmera(),
            jugador.getPuntos(), // ⭐⭐⭐ PUNTOS AGREGADOS ⭐⭐⭐
            jugador.tienePowerUpRecargaRapida(),
            jugador.getTiempoRecargaRapidaRestante(),
            jugador.tieneInstaKill(),
            jugador.getTiempoInstaKillRestante(),
            jugador.estaOcultoEnArbusto()
        );
    }
}
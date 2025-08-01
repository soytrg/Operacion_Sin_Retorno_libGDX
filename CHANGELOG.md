# Changelog

Todos los cambios notables en este proyecto se documentarán en este archivo.

El formato sigue el estándar de [Keep a Changelog](https://keepachangelog.com/es/1.0.0/)
y este proyecto sigue [SemVer](https://semver.org/lang/es/).

## [0.2.0] - 2025-08-1
### Añadido
- Mapa: Se añadio una porcion del mapa del 1er nivel
- Mecanicas del jugador: saltar, moverse, agacharse, y disparar (faltaria la textura de la bala)
- Power ups: Se añadio el primer power up "Recarga Rapida"
- Menus: Ahora se puede ir desde el modo Un jugador al menu de pausa que te puede llevar al menu principal. tambien esta el menu de opciones donde se puede configurar el audio de la musica y de los efectos de sonido.
- Camara: Se implemento una camara la cual sigue el movimiento del jugador.
- Hud: se agrego un hud con el cual se puede ver cierta informacion relativamente importante para el usuario.
- Colisiones: Entre el jugador, el mapa, y los power up.

### Cambios importantes en el codigo:
- Creacion de paquetes: se crearon distintos tipos de paquetes para ordenar cada una de las clases dentro de proyecto, como ejemplo "audio" para guardar todo tipo de clases relacionadas al audio.
- Creacion de clases: se crearon muchas clases a comparacion de la 1era entrega, todas mu utiles, algunas especificamente para el manejo del audio o la configuracion del juego. Algunas otras clases sirvieron para manejar cosas especificas de esas caracteristicas como por ejemplo la clase HUD, para manejar todo acerca del hud en una sola clase.

### Correcion de errores
- A comparacion de la version anterior, no se corrigio ningun error. Aunque para lograr la version actual hubo que arreglar ciertos errores al manejar los menus y el audio del juego debido a errores logicos que se tuvo en la implementacion de ambas cosas.

## [0.1.0] - 2025-05-21
### Añadido
- Creación inicial del proyecto con estructura base generada por LibGDX.
- Configuración de plataformas objetivo: escritorio.
- Creación de archivos iniciales: README.md, CHANGELOG.md, gitignore.
- Creacion de la wiki.

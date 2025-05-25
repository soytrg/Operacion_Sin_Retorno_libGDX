Operacion Sin Retorno Juego 2D de plataformas
Desarollado por:
  - Luca Amodeo    
  - Tiziano Exequiel Roberti Gallardo

Descripcion: “Operación Sin Retorno” es un juego en el cual te adentrarás en la piel de un soldado ruso llamado Dimitri Tripaloski, quien fue asignado a una misión que lo llevará a un mundo salvaje y perturbador, donde la cordura, el entorno hostil y los enemigos humanos o mutados amenazan constantemente al jugador.

Tecnologias utilizadas y plataformas objetivo: LibGDX, Eclipse, Escritorio.
-----------------------------------------------------------------------------------------------------------------------------------
Como compilar y ejecutar: 
  1. Abrir terminal o git bash en windows
  2. navergar hacia la carpeta donde quieras clonar el repositorio
  comando: cd ruta/de/tu/carpeta
  3. Usá el comando git clone seguido de la URL del repositorio:
  comando: git clone https://github.com/soytrg/Operacion_Sin_Retorno_libGDX

Esto serviria para obtener la carpeta con los archivos, ahora para importar todo esto a Eclipse se deben de seguir estos pasos:
  1. Anda a "File" > "Import...".
  2. Selecciona "Gradle" > "Existing Gradle Project".
  3. Navega hasta la carpeta raíz del proyecto generado y seguí los pasos del asistente.
     
Ahora para verificar la ejecucion del programa siga estos pasos:
  1. Una vez importado (puede tardar un rato en importar el proyecto, aunque veas que ya se importó debajo de todo puede aparecer un         mensaje que dice “importing x Gradle Project”. Hasta que este mensaje no desaparezca el proyecto no estará importado), busca el           módulo/subproyecto de escritorio. Nota: Con Liftoff, en general se llama lwjgl3 en lugar de desktop.
  2. Dentro de ese módulo, busca la clase lanzadora (ej. Lwjgl3Launcher.java) y ejecutala (Run As > Java Application o similar).
  3. Deberías ver la pantalla de ejemplo de la plantilla que elegiste (o la imagen por defecto de LibGDX si no elegiste plantilla). Es      posible que necesites configurar el "Working Directory" en la configuración de ejecución para que apunte a la carpeta assets/ del         proyecto core/ si las imágenes no cargan.
-----------------------------------------------------------------------------------------------------------------------------------
Estado actual del proyecto: Configuracion Inicial.

[Ver la propuesta del proyecto aqui](https://github.com/soytrg/Operacion_Sin_Retorno_libGDX/wiki/Propuesta-del-Proyecto-%E2%80%90-Operacion-Sin-Retorno)



/* 
   Este archivo .java (hasta donde sé) debe ser abierto en un proyecto de Netbeans, 
   dentro del paquete laberintomonolitico, y las siguientes imagenes deben estar
   dentro del mismo paquete:

   fin.png (16x16)
   inicio.png (16x16)
   jugador.png (16x16)
   laser1.png (16x16)
   laser2.png (16x16)
   llave.png  (16x16)
   pared.png (16x16)
   piso.png (16x16)
   sombra.png (Overlay, cuadrado negro de 768x576 con un círculo transparente en el centro.)
   zom.png (16x16)
    
   Así como un archivo laberinto_grande.txt. El formato de este son lineas de
   números entre 0 y 3, donde 0 es piso, 1 es pared, 2 es casilla de inicio y
   3 es casilla de salida. La posición del jugador y la llave se ponen manual-
   mente en sus constructores. El juego se gana al recoger la llave; por lo ta
   nto, al diseñar el laberinto, se espera que la llave se ponga al lado de este.

   El requerimiento del IDE probablemente tenga que ver con el hecho de que se usa
   getClass().getResourceAsStream() para cargar los archivos: esto implica que deben
   estar en el classpath, cosa que no sé manejar manualmente fuera del IDE. 
   Sin embargo, dentro de este, funciona perfectamente.

   El movimiento es en grid, lo que significa que un toque a las teclas de
   movimiento te alinean perfectamente con la siguiente teja, o tile. Tocar
   enemigos, o las paredes, te devuelve a la posición de inicio del juegador.
 */
package laberintomonolitico;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Random;
import javax.imageio.ImageIO;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 *
 * @author JESUS
 */
public class LaberintoMonolitico {

    public static void main(String[] args) {
        //Crear la ventana que contendrá al juego.
        JFrame ventana = new JFrame();
        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ventana.setResizable(false);
        ventana.setTitle("Juego de Laberinto");
        //Hacer una pequeña introducción, inicializar el juego, y hacerlo visible.
        JOptionPane.showMessageDialog(ventana, "No sabes qué te persigue. No sabes donde estás. Solo sabes que al final de este laberinto hay una puerta cerrada, y algo te está siguiendo.", "Introducción.", JOptionPane.PLAIN_MESSAGE);
        JOptionPane.showMessageDialog(ventana, "Encuentra la llave para ganar. La llave está al lado de la salida. Ignora las puertas cerradas.", "Introducción", JOptionPane.INFORMATION_MESSAGE);
        ventana.add(new PanelJuego());
        ventana.pack();
        ventana.setLocationRelativeTo(null);
        ventana.setVisible(true);
    }

}

/* 

Clase abstracta que representa a una entidad: jugadores y enemigos.

> panel: JPanel donde se está ejecutando el juego.
> nombre: Identificador de tipo de entidad, util al colisionas con otras entidades.
> laberintoX, laberintoY: Posición global en el area de juego.
> laberintoXInicial,laberintoYInicial: Posición inicial, en caso de ser necesario volver a ella.
> velocidad: Velocidad a la que se desplaza la entidad, en pixeles por segundo.
> areaSolida: Area de colisión de la entidad.
> areaSolidaOriginalX, areaSolidaOriginalY: Para resetear la posición original del area de colisión de ser necesario.
> direccion: Direccion en la que se está moviendo.
> enMovimiento: Para determinar si la entidad está en movimiento (movimiento en grid.)
> contadorPixel: Contador de pixeles que se han movido desde la ultima vez (movimiento en grid.)
> colisionEncendida: Para determinar si ha ocurrido una colisión.
> detenido: Para determinar si la entidad debe ser borrada (lifetime)
> image: Imagen de la entidad. 

 */
class Entidad {

    public PanelJuego panel;
    public String nombre;
    public int laberintoX, laberintoY;
    public int laberintoXInicial, laberintoYInicial;
    public int velocidad;
    public Rectangle areaSolida;
    public int areaSolidaOriginalX;
    public int areaSolidaOriginalY;
    public String direccion;
    public boolean enMovimiento;
    public boolean colisionEncendida = false;
    public int contadorPixel;
    public boolean detenido = false;
    public BufferedImage imagen;

    public Entidad(PanelJuego p) {
        panel = p;
    }

    public void pintar(Graphics2D pincel) {
    }

    public void update() {
    }

    public void cargarImagen() {
    }
}

/*
Clase que representa a un Jugador.

> manejadorTeclas: Instancia de KeyHandler que corre en su propio hilo, revisa el input.
> pantallaX, pantallaY: Permiten dibujar al jugador siempre en el centro de la pantalla (camara)
> victoria: Bandera de victoria, lo revisa una instancia de la clase VictoryChecker que corre en otro hilo.
 */
class Jugador extends Entidad {

    KeyHandler manejadorTeclas;
    public final int pantallaX;
    public final int pantallaY;
    public boolean victoria;

    public void inicializarValores() {
        velocidad = 4;
        laberintoX = (panel.tamañoTejas) * 1;
        laberintoY = (panel.tamañoTejas) * 1;
        laberintoXInicial = laberintoX;
        laberintoYInicial = laberintoY;
        direccion = "ninguna";
        enMovimiento = false;
        contadorPixel = 0;
        nombre = "jugador";
        areaSolidaOriginalX = 0;
        areaSolidaOriginalY = 0;
        areaSolida = new Rectangle(0, 0, panel.tamañoTejas - 1, panel.tamañoTejas - 1);
    }

    public void cargarImagen() {
        try {
            imagen = ImageIO.read(getClass().getResourceAsStream("./jugador.png"));
        } catch (IOException ex) {
            Logger.getLogger(Jugador.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Jugador(PanelJuego p, KeyHandler k) {
        super(p);
        this.manejadorTeclas = k;
        // Dibujar al jugador siempre en el centro de la pantalla.
        this.pantallaX = (panel.resolucionHorizontal / 2) - (panel.tamañoTejas / 2);
        this.pantallaY = (panel.resolucionVertical / 2) - (panel.tamañoTejas / 2);
        //Establecer valores iniciales.
        inicializarValores();
        cargarImagen();
    }

    public void update() {
        /*Si el jugador no está actualmente en movimiento, permitir cambiar la 
        dirección, y marcar a la entidad como 'en movimiento'.*/
        if (!enMovimiento) {
            if (manejadorTeclas.arribaPulsado) {
                direccion = "arriba";
            } else if (manejadorTeclas.abajoPulsado) {
                direccion = "abajo";
            } else if (manejadorTeclas.derechaPulsado) {
                direccion = "derecha";
            } else if (manejadorTeclas.izquierdaPulsado) {
                direccion = "izquierda";
            } else {
                direccion = "ninguna";
            }
            if (!direccion.equals("ninguna")) {
                enMovimiento = true;
            }
        } else {
            /* Revisar si se choca contra algo en el camino. */
            colisionEncendida = false;
            /* Colision con tejas. */
            panel.checker.revisarTeja(this);
            /* Colision con items. */
            int indiceItem = panel.checker.revisarItem(this, true);
            /* Si se choca con un item, manejarlo en su propia función. */
            if (indiceItem != 100) {
                recogerItem(indiceItem);
            }
            /* Colisión con enemigos. */
            panel.checker.revisarEntidad(this, panel.enemigos);
            /* Si no se ha chocado con nada que cuente como solido, mover al jugador. */
            if (!colisionEncendida) {
                switch (direccion) {
                    case "arriba":
                        laberintoY = laberintoY - velocidad;
                        break;
                    case "abajo":
                        laberintoY = laberintoY + velocidad;
                        break;
                    case "derecha":
                        laberintoX = laberintoX + velocidad;
                        break;
                    case "izquierda":
                        laberintoX = laberintoX - velocidad;
                        break;
                }
            } else {
                /* Ha habido una colisión: devolvemos al jugador a la posición
                inicial, y detenemos el movimiento en grid.*/
                contadorPixel = panel.tamañoTejas - velocidad;
                laberintoX = laberintoXInicial;
                laberintoY = laberintoYInicial;
            }
            /* Forzar movimiento en grid: no permitir que el jugador se mueva
            en otra dirección hasta que no se haya alineado perfectamente con
            alguna teja. */
            contadorPixel += velocidad;
            if (contadorPixel == panel.tamañoTejas) {
                contadorPixel = 0;
                enMovimiento = false;
            }
        }
    }

    public void pintar(Graphics2D pincel) {
        /* Dibujar la imagen del jugador. */
        pincel.drawImage(imagen, pantallaX, pantallaY, panel.tamañoTejas, panel.tamañoTejas, null);
        /* Dibujar area de colision */
        //pincel.setColor(Color.RED);
        //pincel.drawRect(pantallaX+areaSolida.x, pantallaY+areaSolida.x,areaSolida.width,areaSolida.height);
    }

    /*Realizar acciones dependientes del item que se haya recogido. Como solamente
    hemos hecho una llave, al recoger esta, el jugador ha logrado la victoria.*/
    public void recogerItem(int indice) {
        /*Obtener el nombre del item recogido.*/
        String nombre = panel.items[indice].nombre;

        switch (nombre) {
            case "llave":
                victoria = true;
                break;
            default:
                break;
        }
        /* Borrar el item del mundo. */
        panel.items[indice] = null;
    }
}

/*
Clase que representa a un Laser, un enemigo. 

Su comportamiento es que obtiene una dirección aleatoriamente al aparecer 
(o se le es dada) y se mueve en esta hasta que se choca con una pared. Cuando esto 
ocurre, se detiene, y luego es eliminado por la lógica del juego.

 */
class Laser extends Entidad {

    public void inicializarValores() {
        velocidad = 6;
        areaSolida = new Rectangle(0, 0, panel.tamañoTejas - 1, panel.tamañoTejas - 1);
        areaSolidaOriginalX = 0;
        areaSolidaOriginalY = 0;
        contadorPixel = 0;
        enMovimiento = false;
        detenido = false;
        nombre = "laser";
    }

    public void cargarImagen() {
        try {
            if (direccion.equals("arriba") || direccion.equals("abajo")) {
                imagen = ImageIO.read(getClass().getResourceAsStream("./laser2.png"));
            } else {
                imagen = ImageIO.read(getClass().getResourceAsStream("./laser1.png"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void determinarDireccion() {
        Random random = new Random();
        switch (random.nextInt(4)) {
            case 0:
                direccion = "arriba";
                break;
            case 1:
                direccion = "abajo";
                break;
            case 2:
                direccion = "derecha";
                break;
            case 3:
                direccion = "izquierda";
                break;
        }
    }

    public Laser(PanelJuego p, String dir) {
        super(p);
    }

    public Laser(PanelJuego p) {
        super(p);
        inicializarValores();
        determinarDireccion();
        cargarImagen();
    }

    public void update() {
        /*El laser solo se mueve cuando no 'detenido'*/
        if (!detenido) {
            /* Revisar colisiones con paredes y el jugador. */
            colisionEncendida = false;
            panel.checker.revisarTeja(this);
            panel.checker.revisarJugador(this);
            /* Si se ha colisionado con una pared, detener al laser.*/
            if (colisionEncendida) {
                detenido = true;
            } else {
                /*if (!colisionEncendida) {*/
                switch (direccion) {
                    case "arriba":
                        laberintoY = laberintoY - velocidad;
                        break;
                    case "abajo":
                        laberintoY = laberintoY + velocidad;
                        break;
                    case "derecha":
                        laberintoX = laberintoX + velocidad;
                        break;
                    case "izquierda":
                        laberintoX = laberintoX - velocidad;
                        break;
                }
            }
        }
    }

    public void pintar(Graphics2D pincel) {
        /* Dibujar imagen. */
        int pantallaX = laberintoX - panel.jug.laberintoX + panel.jug.pantallaX;
        int pantallaY = laberintoY - panel.jug.laberintoY + panel.jug.pantallaY;
        pincel.drawImage(imagen, pantallaX, pantallaY, panel.tamañoTejas, panel.tamañoTejas, null);
        /* Dibujar rectángulo de colision. */
        //pincel.setColor(Color.RED);
        //pincel.drawRect(pantallaX+areaSolida.x, pantallaY+areaSolida.y, areaSolida.width,areaSolida.height);
    }

}

/*

Clase que representa a un Zombie, un enemigo.

Su comportamiento es el equivalente a la "bola roja" del enunciado, rebota contra
las paredes una cierta cantidad de veces y luego es eliminada por el ciclo de juego.
La dirección inicial es aleatoria, o puede ser establecida manualmente.

 */
class Zombie extends Entidad {

    public int rebote;
    public boolean esperando;
    public int contadorEspera;

    public void inicializarValores() {
        velocidad = 2;
        areaSolida = new Rectangle(0, 0, panel.tamañoTejas - 1, panel.tamañoTejas - 1);
        areaSolidaOriginalX = 0;
        areaSolidaOriginalY = 0;
        contadorPixel = 0;
        enMovimiento = false;
        rebote = 0;
        esperando = true;
        contadorEspera = 0;
        nombre = "zombie";
    }

    public void cargarImagen() {
        try {
            imagen = ImageIO.read(getClass().getResourceAsStream("./zom.png"));
        } catch (IOException ex) {
            Logger.getLogger(Zombie.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void determinarDireccion() {
        Random random = new Random();
        switch (random.nextInt(4)) {
            case 0:
                direccion = "arriba";
                break;
            case 1:
                direccion = "abajo";
                break;
            case 2:
                direccion = "derecha";
                break;
            case 3:
                direccion = "izquierda";
                break;
        }
    }

    public Zombie(PanelJuego panel, String dir) {
        super(panel);
        inicializarValores();
        cargarImagen();
        direccion = dir;
    }

    public Zombie(PanelJuego panel) {
        super(panel);
        inicializarValores();
        determinarDireccion();
        cargarImagen();
    }

    public void update() {
        /* Revisar colision con paredes y jugador. */
        colisionEncendida = false;
        panel.checker.revisarTeja(this);
        panel.checker.revisarJugador(this);
        /* Si se ha colisionado con una pared, cambiar de direccion y contar las ocurrencias.*/
        if (colisionEncendida) {
            if (rebote <= 10) {
                rebote++;
            } else {
                detenido = true;
            }
            switch (direccion) {
                case "arriba":
                    direccion = "abajo";
                    break;
                case "abajo":
                    direccion = "arriba";
                    break;
                case "derecha":
                    direccion = "izquierda";
                    break;
                case "izquierda":
                    direccion = "derecha";
                    break;
            }
        }
        //enMovimiento = true;
        switch (direccion) {
            case "arriba":
                laberintoY = laberintoY - velocidad;
                break;
            case "abajo":
                laberintoY = laberintoY + velocidad;
                break;
            case "derecha":
                laberintoX = laberintoX + velocidad;
                break;
            case "izquierda":
                laberintoX = laberintoX - velocidad;
                break;
        }
    }

    public void pintar(Graphics2D pincel) {
        /* Dibujar el zombie relativo a la posición del jugador y la pantalla. */
        int pantallaX = laberintoX - panel.jug.laberintoX + panel.jug.pantallaX;
        int pantallaY = laberintoY - panel.jug.laberintoY + panel.jug.pantallaY;
        pincel.drawImage(imagen, pantallaX, pantallaY, panel.tamañoTejas, panel.tamañoTejas, null);
        /* Dibujar el rectangulo de colision. */
        //pincel.setColor(Color.RED);
        //pincel.drawRect(pantallaX+areaSolida.x, pantallaY+areaSolida.x,areaSolida.width,areaSolida.height);
    }
}

/*
Clase que representa a un Item, un objeto recogible.

Atributos.
imagen: La imagen del Item.
nombre: Identificador del item, util al ser manejado por una entidad.
laberintoX, laberintoY: La posición del item en el mundo (laberinto)
colision: Si el item es solido o no (si actúa como una pared.)
areaSolida: Rectángulo de colision del item.
areaSolidaDefaultX, areaSolidaDefaultY: Offset original del item, para resetearlo al revisar colisiones.

 */
class Item {

    public BufferedImage imagen;
    public String nombre;
    public int laberintoX;
    public int laberintoY;
    public boolean colision = false;
    public Rectangle areaSolida = new Rectangle(0, 0, 64, 64);
    public int areaSolidaDefaultX = 0;
    public int areaSolidaDefaultY = 0;

    public void cargarImagen(String path) {
    }

    ;
    
    /* Pintar el item relativo a la posición del jugador y la pantalla. */
    public void pintar(Graphics2D pincel, PanelJuego panel) {
        int pantallaX = laberintoX - panel.jug.laberintoX + panel.jug.pantallaX;
        int pantallaY = laberintoY - panel.jug.laberintoY + panel.jug.pantallaY;

        pincel.drawImage(imagen, pantallaX, pantallaY, panel.tamañoTejas, panel.tamañoTejas, null);
    }
}


/*

Clase que representa a una Llave, el item que debe recogerse para ganar.

La Llave se pone al lado de la Teja de salida, que es una puerta cerrada con
candado. Al recogerla el jugador, se borra y pone la bandera de victoria en
verdadero. Este es su único propósito.

 */
class Llave extends Item {

    public Llave() {
        nombre = "llave";
    }

    @Override
    public void cargarImagen(String path) {
        try {
            imagen = ImageIO.read(getClass().getResourceAsStream(path));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

/*
Clase que representa a una Teja, las graficas del suelo y las paredes del
laberinto.

Atributos:

imagen: La imagen de la teja.
colision: Si la teja es solida o no (pared, o piso)

"Teja" viene del inglés "Tile"

 */
class Teja {

    public BufferedImage imagen;
    public boolean colision = false;
}

/*
Clase que se encarga de manejar todo lo correspondiente al mapa: cargar la
representación en texto, guardarla en memoria, dibujarla, etcétera. Aquí empecé
a mezclar inglés y español por "TejaManager" ya sonaba feo.

Atributos.

panel: El panel donde se está ejecutando el juego.
tejas: Un arreglo de tejas, para referencia futura por esta y otras clases.
matrizMapa: La representación matricial del mapa del laberinto.

 */
class TileManager {

    PanelJuego panel;
    public Teja[] tejas;
    public int matrizMapa[][];

    public TileManager(PanelJuego pa) {
        this.panel = pa;
        tejas = new Teja[4];
        matrizMapa = new int[panel.maxColumnasLaberinto][panel.maxFilasLaberinto];
        getImagenesTejas();
        cargarMapa("./laberinto_grande.txt");
    }

    /*Inicializar el arreglo de tejas con todas las imagenes de la carpeta local.*/
    public void getImagenesTejas() {
        try {
            tejas[0] = new Teja();
            tejas[0].imagen = ImageIO.read(getClass().getResourceAsStream("./piso.png"));

            tejas[1] = new Teja();
            tejas[1].imagen = ImageIO.read(getClass().getResourceAsStream("./pared.png"));
            tejas[1].colision = true;

            tejas[2] = new Teja();
            tejas[2].imagen = ImageIO.read(getClass().getResourceAsStream("./inicio.png"));
            tejas[2].colision = true;

            tejas[3] = new Teja();
            tejas[3].imagen = ImageIO.read(getClass().getResourceAsStream("./fin.png"));
            tejas[3].colision = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* Recorrer la matriz para obtener los numeros de tejas contenidos en ellas.
    Luego, obtener la posición mundial de estas tejas al multiplicarlas por su
    tamaño definido en el panel, y dibujarlas relativas al
    jugador y la camara centrada en este.*/
    public void pintar(Graphics2D pincel) {
        int colLaberinto;
        int filLaberinto;
        int numTeja;

        for (colLaberinto = 0; colLaberinto < panel.maxColumnasLaberinto; colLaberinto++) {
            for (filLaberinto = 0; filLaberinto < panel.maxFilasLaberinto; filLaberinto++) {
                numTeja = matrizMapa[colLaberinto][filLaberinto];

                int laberintoX = colLaberinto * panel.tamañoTejas;
                int laberintoY = filLaberinto * panel.tamañoTejas;
                int pantallaX = laberintoX - panel.jug.laberintoX + panel.jug.pantallaX;
                int pantallaY = laberintoY - panel.jug.laberintoY + panel.jug.pantallaY;

                pincel.drawImage(tejas[numTeja].imagen, pantallaX, pantallaY, panel.tamañoTejas, panel.tamañoTejas, null);

            }

        }
    }

    /* Cargar el mapa del archivo local y traspasarlo a memoria RAM. */
    public void cargarMapa(String path) {
        try {
            InputStream is = getClass().getResourceAsStream(path);
            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            int col;
            int fil;

            for (fil = 0; fil < panel.maxFilasLaberinto; fil++) {
                String lineaMapa = br.readLine();
                String[] numeros = lineaMapa.split(" ");
                for (col = 0; col < panel.maxColumnasLaberinto; col++) {
                    int numeroTeja = Integer.parseInt(numeros[col]);
                    matrizMapa[col][fil] = numeroTeja;
                }
            }
            br.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

/* Clase general que se encarga de cargar enemigos, y de delegar la carga de 
tejas a la instancia de TileManager del panel de juego. */
class Cargador {

    PanelJuego panel;

    public Cargador(PanelJuego p) {
        panel = p;
    }

    public void cargarItems() {
        panel.cargadorItems.inicializarItems();
    }

    public void cargarEnemigos() {
        
    }

    public void recargarEnemigos() {
        //TODO
    }
}

/*

Clase que se encarga de revisar las colisiones de cualquier tipo: entidad-teja,
entidad-item, jugador-entidad y entidad-jugador.

Atributos

panel: El panel de juego.

 */
class CollisionChecker {

    PanelJuego panel;

    public CollisionChecker(PanelJuego p) {
        this.panel = p;
    }

    /*Para revisar si una entidad está chocando con una pared, obtenemos las
    coordenadas de la entidad en cuestión tomando en cuenta también su rectángulo
    de colisiones.
    
    Solo nos interesan saber las cuatro esquinas de la entidad, ya que con ellas
    es suficiente para determinar si se está entrando en colisión.
    
    Dividimos las coordenadas de las esquinas entre el tamaño de las tejas para
    determinar las filas y columnas que están alrededor de la entidad, y revisamos
    la dirección en la que se está moviendo.
    
    Se intenta predecir el movimiento de la entidad, y se obtienen la información
    de las tejas en la dirección que se quiere mover. Si estas son sólidas, ha
    habido un choque.*/
    public void revisarTeja(Entidad entidad) {

        //Coordenadas para encontrar la fila y columna inicial de la entidad.
        int izquierdaEntidadLaberintoX = entidad.laberintoX + entidad.areaSolida.x;
        int derechaEntidadLaberintoX = entidad.laberintoX + entidad.areaSolida.x + entidad.areaSolida.width;
        int arribaEntidadLaberintoY = entidad.laberintoY + entidad.areaSolida.y;
        int abajoEntidadLaberintoY = entidad.laberintoY + entidad.areaSolida.y + entidad.areaSolida.height;
        //Fila y columna adyacente de la entidad.
        int columnaIzquierdaEntidad = izquierdaEntidadLaberintoX / panel.tamañoTejas;
        int columnaDerechaEntidad = derechaEntidadLaberintoX / panel.tamañoTejas;
        int filaArribaEntidad = arribaEntidadLaberintoY / panel.tamañoTejas;
        int filaAbajoEntidad = abajoEntidadLaberintoY / panel.tamañoTejas;
        //Tejas contra las que posiblemente se esté colisionando.
        int numeroTeja1, numeroTeja2;

        switch (entidad.direccion) {
            //"Predecir" el movimiento de la entidad en cada dirección y revisar
            //si la teja es sólida.
            case "arriba":
                filaArribaEntidad = (arribaEntidadLaberintoY - entidad.velocidad) / panel.tamañoTejas;
                numeroTeja1 = panel.tileM.matrizMapa[columnaIzquierdaEntidad][filaArribaEntidad];
                numeroTeja2 = panel.tileM.matrizMapa[columnaDerechaEntidad][filaArribaEntidad];
                if (panel.tileM.tejas[numeroTeja1].colision || panel.tileM.tejas[numeroTeja2].colision) {
                    entidad.colisionEncendida = true;
                }
                break;
            case "abajo":
                filaAbajoEntidad = (abajoEntidadLaberintoY + entidad.velocidad) / panel.tamañoTejas;
                numeroTeja1 = panel.tileM.matrizMapa[columnaIzquierdaEntidad][filaAbajoEntidad];
                numeroTeja2 = panel.tileM.matrizMapa[columnaDerechaEntidad][filaAbajoEntidad];
                if (panel.tileM.tejas[numeroTeja1].colision || panel.tileM.tejas[numeroTeja2].colision) {
                    entidad.colisionEncendida = true;
                }
                break;
            case "derecha":
                columnaDerechaEntidad = (derechaEntidadLaberintoX + entidad.velocidad) / panel.tamañoTejas;
                numeroTeja1 = panel.tileM.matrizMapa[columnaDerechaEntidad][filaArribaEntidad];
                numeroTeja2 = panel.tileM.matrizMapa[columnaDerechaEntidad][filaAbajoEntidad];
                if (panel.tileM.tejas[numeroTeja1].colision || panel.tileM.tejas[numeroTeja2].colision) {
                    entidad.colisionEncendida = true;
                }
                break;
            case "izquierda":
                columnaIzquierdaEntidad = (izquierdaEntidadLaberintoX - entidad.velocidad) / panel.tamañoTejas;
                numeroTeja1 = panel.tileM.matrizMapa[columnaIzquierdaEntidad][filaArribaEntidad];
                numeroTeja2 = panel.tileM.matrizMapa[columnaIzquierdaEntidad][filaAbajoEntidad];
                if (panel.tileM.tejas[numeroTeja1].colision || panel.tileM.tejas[numeroTeja2].colision) {
                    entidad.colisionEncendida = true;
                }
                break;
        }
    }

    /*
    La lógica de items es muy similar a la lógica de tejas, con la diferencia
    de que iteramos sobre el arreglo de items y revisamos todos y cada uno de 
    ellos. Como solo tenemos 1 item, no hay gran perdida de eficiencia.
    
    Simulamos el movimiento de la entidad, pero esta vez movemos físicamente el 
    área de colisión de la misma. Esto es para poder utilizar el metodo 
    .intersects() de la clase Rectangulo. Si las areas de colisión de la entidad 
    y el item están solapadas, y la entidad es el jugador, se devuelve el indice
    del item para ser manejado por el jugador.
    
    Al final de cada chequeo, reseteamos la posición del area de colisión para
    evitar efectos secundarios.
     */
    public int revisarItem(Entidad entidad, boolean jugador) {
        int indice = 100;

        for (int i = 0; i < panel.items.length; i++) {
            if (panel.items[i] != null) {
                //Obtener la posición en pantalla de la entidad.
                entidad.areaSolida.x = entidad.laberintoX + entidad.areaSolida.x;
                entidad.areaSolida.y = entidad.laberintoY + entidad.areaSolida.y;
                //Obtener la posición en pantalla del objeto.
                panel.items[i].areaSolida.x = panel.items[i].laberintoX + panel.items[i].areaSolida.x;
                panel.items[i].areaSolida.y = panel.items[i].laberintoY + panel.items[i].areaSolida.y;
                //Simular movimiento y revisar si las entidades se colisionan.
                switch (entidad.direccion) {
                    case "arriba":
                        entidad.areaSolida.y = entidad.areaSolida.y - entidad.velocidad;
                        if (entidad.areaSolida.intersects(panel.items[i].areaSolida)) {
                            if (panel.items[i].colision) {
                                entidad.colisionEncendida = true;
                            }
                            if (jugador) {
                                indice = i;
                            }
                        }
                        break;
                    case "abajo":
                        entidad.areaSolida.y = entidad.areaSolida.y + entidad.velocidad;
                        if (entidad.areaSolida.intersects(panel.items[i].areaSolida)) {
                            if (panel.items[i].colision) {
                                entidad.colisionEncendida = true;
                            }
                            if (jugador) {
                                indice = i;
                            }
                        }
                        break;
                    case "derecha":
                        entidad.areaSolida.x = entidad.areaSolida.x + entidad.velocidad;
                        if (entidad.areaSolida.intersects(panel.items[i].areaSolida)) {
                            if (panel.items[i].colision) {
                                entidad.colisionEncendida = true;
                            }
                            if (jugador) {
                                indice = i;
                            }
                        }
                        break;
                    case "izquierda":
                        entidad.areaSolida.x = entidad.areaSolida.x - entidad.velocidad;
                        if (entidad.areaSolida.intersects(panel.items[i].areaSolida)) {
                            if (panel.items[i].colision) {
                                entidad.colisionEncendida = true;
                            }
                            if (jugador) {
                                indice = i;
                            }
                        }
                        break;
                    case "default":
                        if (entidad.areaSolida.intersects(panel.items[i].areaSolida)) {
                            if (panel.items[i].colision) {
                                entidad.colisionEncendida = true;
                            }
                            if (jugador) {
                                indice = i;
                            }
                        }
                        break;
                }
                entidad.areaSolida.x = entidad.areaSolidaOriginalX;
                entidad.areaSolida.y = entidad.areaSolidaOriginalY;
                panel.items[i].areaSolida.x = panel.items[i].areaSolidaDefaultX;
                panel.items[i].areaSolida.y = panel.items[i].areaSolidaDefaultY;
            }
        }
        return indice;
    }

    /*La lógica para revisar colisiones con entidades es idéntica a la de
    colisiones con items, solo que iteramos sobre el arreglo de enemigos en vez
    de el de items.*/
    public int revisarEntidad(Entidad entidad, Entidad[] enemigos) {
        int indice = 100;

        for (int i = 0; i < enemigos.length; i++) {
            if (enemigos[i] != null) {
                //Obtener la posición en pantalla de la entidad.
                entidad.areaSolida.x = entidad.laberintoX + entidad.areaSolida.x;
                entidad.areaSolida.y = entidad.laberintoY + entidad.areaSolida.y;
                //Obtener la posición en pantalla de la otra entidad.
                enemigos[i].areaSolida.x = enemigos[i].laberintoX + enemigos[i].areaSolida.x;
                enemigos[i].areaSolida.y = enemigos[i].laberintoY + enemigos[i].areaSolida.y;
                //Simular movimiento y revisar si las entidades se colisionan.
                switch (entidad.direccion) {
                    case "arriba":
                        entidad.areaSolida.y = entidad.areaSolida.y - entidad.velocidad;
                        if (entidad.areaSolida.intersects(enemigos[i].areaSolida)) {
                            entidad.colisionEncendida = true;
                            indice = i;
                        }
                        break;
                    case "abajo":
                        entidad.areaSolida.y = entidad.areaSolida.y + entidad.velocidad;
                        if (entidad.areaSolida.intersects(enemigos[i].areaSolida)) {
                            entidad.colisionEncendida = true;
                            indice = i;
                        }
                        break;
                    case "derecha":
                        entidad.areaSolida.x = entidad.areaSolida.x + entidad.velocidad;
                        if (entidad.areaSolida.intersects(enemigos[i].areaSolida)) {
                            entidad.colisionEncendida = true;
                            indice = i;
                        }
                        break;
                    case "izquierda":
                        entidad.areaSolida.x = entidad.areaSolida.x - entidad.velocidad;
                        if (entidad.areaSolida.intersects(enemigos[i].areaSolida)) {
                            entidad.colisionEncendida = true;
                            indice = i;
                        }
                        break;
                    case "default":
                        if (entidad.areaSolida.intersects(enemigos[i].areaSolida)) {
                            entidad.colisionEncendida = true;
                            indice = i;
                        }
                        break;
                }
                entidad.areaSolida.x = entidad.areaSolidaOriginalX;
                entidad.areaSolida.y = entidad.areaSolidaOriginalY;
                enemigos[i].areaSolida.x = enemigos[i].areaSolidaOriginalX;
                enemigos[i].areaSolida.y = enemigos[i].areaSolidaOriginalY;
            }
        }
        return indice;
    }

    /*
    
    Una vez más, la lógica es identica a los dos métodos anteriors, pero este
    método se llama desde los enemigos y no afectan la colisión de estos, si no
    que si detectan una colision con el jugador, lo mandan a su posición inicial.
    
     */
    public void revisarJugador(Entidad entidad) {
        boolean jugadorColisionado = false;
        //Obtener la posición en pantalla de la entidad.
        entidad.areaSolida.x = entidad.laberintoX + entidad.areaSolida.x;
        entidad.areaSolida.y = entidad.laberintoY + entidad.areaSolida.y;
        //Obtener la posición en pantalla del jugador.
        panel.jug.areaSolida.x = panel.jug.laberintoX + panel.jug.areaSolida.x;
        panel.jug.areaSolida.y = panel.jug.laberintoY + panel.jug.areaSolida.y;
        //Simular movimiento y revisar si las entidades se colisionan.
        switch (entidad.direccion) {
            case "arriba":
                entidad.areaSolida.y = entidad.areaSolida.y - entidad.velocidad;
                if (entidad.areaSolida.intersects(panel.jug.areaSolida)) {
                    jugadorColisionado = true;
                }
                break;
            case "abajo":
                entidad.areaSolida.y = entidad.areaSolida.y + entidad.velocidad;
                if (entidad.areaSolida.intersects(panel.jug.areaSolida)) {
                    jugadorColisionado = true;
                }
                break;
            case "derecha":
                entidad.areaSolida.x = entidad.areaSolida.x + entidad.velocidad;
                if (entidad.areaSolida.intersects(panel.jug.areaSolida)) {
                    jugadorColisionado = true;
                }
                break;
            case "izquierda":
                entidad.areaSolida.x = entidad.areaSolida.x - entidad.velocidad;
                if (entidad.areaSolida.intersects(panel.jug.areaSolida)) {
                    jugadorColisionado = true;
                }
                break;
        }

        entidad.areaSolida.x = entidad.areaSolidaOriginalX;
        entidad.areaSolida.y = entidad.areaSolidaOriginalY;
        panel.jug.areaSolida.x = panel.jug.areaSolidaOriginalX;
        panel.jug.areaSolida.y = panel.jug.areaSolidaOriginalY;

        if (jugadorColisionado) {
            panel.jug.laberintoX = panel.jug.laberintoXInicial;
            panel.jug.laberintoY = panel.jug.laberintoYInicial;
        }
    }
}

/*Clase que corre en su propio hilo y espera a que se acabe el tiempo de juego.

Está esperando a que se dispare el evento de un Timer Swing en el panel de juego.*/
class DefeatListener implements ActionListener {

    PanelJuego panel;

    public DefeatListener(PanelJuego p) {
        panel = p;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        panel.derrota = true;
        panel.vChecker = null;
        panel.terminarJuego();
    }

}

/*Clase que se encarga de cargar e inicializar los items del juego.*/
class ItemLoader {

    PanelJuego panel;

    public ItemLoader(PanelJuego p) {
        this.panel = p;
    }

    public void inicializarItems() {
        panel.items[0] = new Llave();
        panel.items[0].laberintoX = 39 * panel.tamañoTejas;
        panel.items[0].laberintoY = 1 * panel.tamañoTejas;
        panel.items[0].cargarImagen("./llave.png");
    }
}

/*Clase que corre en su propio hilo y maneja el input para el jugador.*/
class KeyHandler implements KeyListener {

    public boolean arribaPulsado, abajoPulsado, izquierdaPulsado, derechaPulsado;

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_UP) {
            arribaPulsado = true;
        } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            abajoPulsado = true;
        } else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            izquierdaPulsado = true;
        } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            derechaPulsado = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_UP) {
            arribaPulsado = false;
        } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            abajoPulsado = false;
        } else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            izquierdaPulsado = false;
        } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            derechaPulsado = false;
        }
    }

}

/* Clase que, cada medio segundo, revisa si se ha ganado el juego (busy waiting, no
es eficiente.) y de ser así lo da por terminado.*/
class VictoryChecker extends Thread {

    Jugador jug;
    PanelJuego panel;

    public VictoryChecker(PanelJuego p) {
        this.panel = p;
        this.jug = panel.jug;
    }

    @Override
    public void run() {
        while (panel.temporizador != null) {
            if (jug.victoria) {
                panel.terminarJuego();
            } else {
                try {
                    sleep(500);
                } catch (InterruptedException ex) {
                    Logger.getLogger(VictoryChecker.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

}

/*Clase que se encarga de dibujar la interfaz gráfica de juego, en este caso el
tiempo restante.*/
class UI implements ActionListener{

    PanelJuego panel;
    Font fuente;

    public int tiempoJuego;

    public UI(PanelJuego p) {
        panel = p;
        fuente = new Font("Arial", Font.PLAIN, 40);
        tiempoJuego = 90;
    }

    public void pintar(Graphics2D pincel) {
        pincel.setFont(fuente);
        pincel.setColor(Color.WHITE);
        pincel.drawString("Tiempo: " + tiempoJuego, panel.tamañoTejas, panel.tamañoTejas);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        tiempoJuego--;
    }

}

/* Clase principal, donde se ejecuta toda la lógica del juego.

Atributos.

tamañoOriginalTejas: El tamaño original de las tejas del mapa en pixeles.
factorEscalado: El factor de escalado de dichas tejas.
tamañoTejas: El tamaño final de las tejas del mapa.
cantidadColumnas: El numero de columnas que se van a mostrar en pantalla a la vez.
cantidadFilas: El numero de filas que se van a mostrar en pantalla a la vez.
resolucionHorizontal,resolucionVertical: La resolucion del panel.
FPS: Fotogramas por segundo, dicta la velocidad del game loop.
maxFilasLaberinto, maxColumnasLaberinto: Tamaño del mapa completo en celdas.
anchoLaberinto, altoLaberinto: Dimensiones en pixeles del mapa completo.
temporizador: Reloj que corre en su propio hilo, y cuando se acaba señala que el jugador ha perdido.
kh: Manejador de input del jugador.
jug: Jugador
TileManager: Manejador de tejas.
CollisionChecker: Manejador de colisiones.
VictoryChecker: Manejador de victoria.
items: Arreglo de objetos en el laberinto.
enemigos: Arreglo de enemigos en el laberinto.
cargadorGeneral: Carga enemigos y delega funciones a cargadorItems
cargadorItems: Inicializa los items del laberinto.
interfaz: Dibuja la UI
defeatL: Da por terminado el juego cuando temporizador dispara su evento al acabarse el tiempo.
derrota: Bandera de estado de perdida, activada por defeatL
 */
class PanelJuego extends JPanel implements Runnable {

    //Opciones de pantalla.
    final int tamañoOriginalTejas = 16;
    final int factorEscalado = 4;
    final public int tamañoTejas = tamañoOriginalTejas * factorEscalado;

    final public int cantidadColumnas = 12;
    final public int cantidadFilas = 9;

    final public int resolucionHorizontal = cantidadColumnas * tamañoTejas;
    final public int resolucionVertical = cantidadFilas * tamañoTejas;

    //Parametros del juego - RENDIMIENTO.
    int FPS = 60;

    //Parametros del juego - LABERINTO.
    public final int maxColumnasLaberinto = 41;
    public final int maxFilasLaberinto = 25;
    public final int anchoLaberinto = maxColumnasLaberinto * tamañoTejas;
    public final int altoLaberinto = maxFilasLaberinto * tamañoTejas;

    //Hilos del juego.
    public Thread temporizador;
    public KeyHandler kh = new KeyHandler();

    //Elementos del juego
    public Jugador jug = new Jugador(this, kh);
    public TileManager tileM = new TileManager(this);
    public CollisionChecker checker = new CollisionChecker(this);
    public BufferedImage overlay;
    public VictoryChecker vChecker = new VictoryChecker(this);
    public Item[] items = new Item[1];
    public Entidad[] enemigos = new Entidad[8];
    public Cargador cargadorGeneral = new Cargador(this);
    public ItemLoader cargadorItems = new ItemLoader(this);
    public UI interfaz = new UI(this);
    public DefeatListener defeatL = new DefeatListener(this);
    public Timer gameTimer;
    public Timer UITimer;
    public EnemySpawner spawner = new EnemySpawner(this);
    public boolean derrota = false;

    public PanelJuego() {
        this.setPreferredSize(new Dimension(resolucionHorizontal, resolucionVertical));
        this.setBackground(Color.BLACK);
        this.setDoubleBuffered(true);
        this.addKeyListener(kh);
        this.setFocusable(true);
        this.cargarOverlay();
        this.inicializarComponentesJuego();
        this.inicializarTimers();
        this.empezarHiloVictoria();
        this.empezarHiloJuego();
    }

    /* Carga la imagen de overlay (la sombra alrededor del jugador)*/
    public void cargarOverlay() {
        try {
            overlay = ImageIO.read(getClass().getResourceAsStream("./sombra.png"));
        } catch (IOException ex) {
            Logger.getLogger(PanelJuego.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /*Carga los items y los enemigos iniciales.*/
    public void inicializarComponentesJuego() {
        cargadorGeneral.cargarItems();
        cargadorGeneral.cargarEnemigos();
    }

    /* Inicializa los parametros del timer de juego. */
    public void inicializarTimers() {
        int delay = 1000; //Milisegundos
        gameTimer = new Timer(92 * delay, defeatL);
        gameTimer.setRepeats(false);
        UITimer = new Timer(1000, interfaz);
        UITimer.setRepeats(true);
        
    }

    /* Pone a trabajar al hilo que revisa si se ha ganado.*/
    public void empezarHiloVictoria() {
        vChecker.start();
    }

    /* Empieza el juego, y el gameloop con el.*/
    public void empezarHiloJuego() {
        temporizador = new Thread(this);
        temporizador.start();
    }

    /* Da por terminado el juego. */
    public void terminarJuego() {
        UITimer.stop();
        gameTimer.stop();
        temporizador = null;
    }

    /* Como PanelJuego implementa Runnable, corre a temporizador en otro hilo. */
    @Override
    public void run() {
        //Inicializacion de variables de control de FPS del juego (Método delta)
        double intervaloPintado = 1000000000 / FPS;
        double delta = 0;
        long ultimoTiempo = System.nanoTime();
        long tiempoActual;
        repaint();
        //Ciclo del juego.
        gameTimer.start();
        UITimer.start();
        while (temporizador != null) {
            /* Llevar el conteo del tiempo */
            tiempoActual = System.nanoTime();
            delta = delta + ((tiempoActual - ultimoTiempo) / intervaloPintado);
            ultimoTiempo = tiempoActual;

            if (delta >= 1) {
                /* Cuando la diferencia de tiempo sea lo suficientemente grande, 
                actualizar el juego y volver a pintar. Esto ocurre aprox. 60
                veces por segundo.*/
                update();
                repaint();
                delta--;
            }
        }

        /*Mostrar mensaje de derrota o victoria dependiendo sea el caso.*/
        if (!derrota) {
            JOptionPane.showMessageDialog(this, "¡Has obtenido la llave de la salida, y escapado del laberinto!", "¡Victoria!", JOptionPane.INFORMATION_MESSAGE);
            SwingUtilities.getWindowAncestor(this).dispose();
        } else {
            SwingUtilities.getWindowAncestor(this).setVisible(false);
            JOptionPane.showMessageDialog(this, "Has tardado demasiado, y las criaturas que te persiguen han entrado al laberinto. Nunca saldrás de aqui. Fin del juego", "Derrota.", JOptionPane.ERROR_MESSAGE);
            SwingUtilities.getWindowAncestor(this).dispose();
        }

    }

    /*Actualizar al jugador y a las entidades.*/
    public void update() {
        int nullCount = 0;
        jug.update();
        for (int i = 0; i < enemigos.length; i++) {
            if (enemigos[i] != null) {
                if (!enemigos[i].detenido) {
                    enemigos[i].update();
                } else {
                    enemigos[i] = null;
                }
            } else {
                nullCount++;
                if (nullCount == enemigos.length){
                    spawner.repopulate();
                }
            }
        }
    }

    /*Pintar el juego.*/
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D pincel = (Graphics2D) g;
        //Dibujar bloques.
        tileM.pintar(pincel);
        //Dibujar objetos.
        for (Item item : items) {
            if (item != null) {
                item.pintar(pincel, this);
            }
        }
        //Dibujar enemigos.
        for (Entidad en : enemigos) {
            if (en != null) {
                en.pintar(pincel);
            }
        }
        //Dibujar jugador.
        jug.pintar(pincel);
        pincel.drawImage(overlay, 0, 0, resolucionHorizontal, resolucionVertical, null);
        //Dibujar elementos de la UI
        interfaz.pintar(pincel);
    }
}


/* Clase que se encarga de manejar la reaparición de enemigos. 
Atributos.

panel: El panel del juego en cuestion.
rand: Generador de numeros aleatorios.
upperEnemyLimit: Cantidad de enemigos que se pueden hacer aparecer a la vez.
limitReached: Bandera que evita que el atributo anterior se incremente más allá del limite de arreglo.
closenessLowerLimit: Lo más cerca que pueden llegar aparecer los enemigos del jugador, antes del ajuste.
closenessLimit: Lo cerca que aparecen los enemigos del jugador inicialmente, antes del ajuste.
closeness: Lo cerca que aparecen los enemigos del jugador.
closenessLimitReached: Bandera que evita que los enemigos aparezcan al lado del jugador.
*/


class EnemySpawner {
    PanelJuego panel;
    Random rand = new Random();
    int upperEnemyLimit = 1;
    boolean limitReached;
    int closenessLowerLimit = 1;
    int closenessLimit = 6;
    int closeness;
    boolean closenessLimitReached = false;
    
    public EnemySpawner(PanelJuego p) {
        panel = p;
    }
    /* Cuando se llama a este metodo, significa que todos los enemigos en el juego
    han 'muerto' y debe repopularse el laberinto. Se obtiene la posición del jugador
    en terminos del mapa logico, y dependiendo de cuantas veces se ha llamado
    antes el metodo, se reduce el espacio minimo entre el jugador y los enemigos
    que aparecen, y se aumenta la cantidad de enemigos que aparecen al mismo tiempo
    con cada llamada al metodo. Esto reemplaza el que aparezcan enemigos mas seguido,
    ya que este metodo ocurre en el mismo hilo que el juego principal, lo cual causa,
    a veces, ralentización.
    
    Se determina la distancia que tiene el nuevo enemigo al jugador, y luego se 
    decide aleatoriamente si se va a poner al enemigo en la misma fila o en la
    misma columna que el jugador, así como si será en la izquierda, derecha, arriba
    o abajo del mismo dependiendo del caso. Se revisa si la posición elegida es
    válida, y si es un espacio vacio, y si en ambos casos eso es verdad, se hace
    aparecer al enemigo en esta posición.
    */
    public void repopulate(){
        /*Hacer aparecer enemigos.*/
        int numTile;
        int enemyLimit;
        /*Obtener la fila y columna del jugador.*/
        int yJugCelda = panel.jug.laberintoX/panel.tamañoTejas;
        int xJugCelda = panel.jug.laberintoY/panel.tamañoTejas;
        int dist;
        int direccion;
        int lineamiento;
        
        if (!limitReached) {
            if (upperEnemyLimit < panel.enemigos.length) {
                enemyLimit = upperEnemyLimit;
            } else {
                enemyLimit = panel.enemigos.length;
                limitReached = true;
            }
        } else {
            enemyLimit = panel.enemigos.length;
        }
        
        if (!closenessLimitReached) {
            if(closenessLimit > closenessLowerLimit) {
                closeness = closenessLimit;
                closenessLimit--;
            } else {
                closeness = closenessLowerLimit;
                closenessLimitReached = true;
            }
        } else {
            closeness = closenessLowerLimit;
        }
        
        
        for(int i = 0; i < enemyLimit; i++) {
            /*Decidir si se va a poner arriba o abajo, izquierda o derecha*/
            direccion = determinarDireccion();
            /* Decidir si se va a poner en la misma fila o columna. */
            lineamiento = rand.nextInt(2);
            if (lineamiento == 0) {
                /* Fila */
                if (direccion == 0) {
                    /* Derecha */
                    dist = rand.nextInt(closeness)+3; //Ponerlo al menos a 3 espacios del jugador, pero cerca.
                    if (xJugCelda + dist > panel.tileM.matrizMapa[0].length-1) {
                        /*Si no es posible, saltarse este enemigo en la iteracion actual.*/
                        continue;
                    } else {
                        numTile = panel.tileM.matrizMapa[yJugCelda][xJugCelda+dist];
                        if (numTile != 0) {
                            /*Es un bloque solido*/
                            continue;
                        }
                        /* Decidir si se va a poner un Zombie o un laser */
                        spawn(yJugCelda, xJugCelda+dist,i);
                    }
                } else {
                    /* Izquierda */
                    dist = rand.nextInt(closeness)+3; //Ponerlo al menos a 3 espacios del jugador, pero cerca.
                    if (xJugCelda - dist < panel.tileM.matrizMapa[0].length-1) {
                        /*Se sale de los limites de la matriz mapa.*/
                        continue;
                    } else {
                        
                        numTile = panel.tileM.matrizMapa[yJugCelda][xJugCelda-dist];
                        if (numTile != 0) {
                            /*Es un bloque solido*/
                            continue;
                        }
                        /* Decidir si se va a poner un Zombie o un laser */
                        spawn(yJugCelda, xJugCelda-dist,i);
                    }
                }
            } else {
                /* Columna */
                if(direccion == 0) {
                    /* Abajo */
                    dist = rand.nextInt(closeness)+3;
                    if (yJugCelda + dist > panel.tileM.matrizMapa.length-1) {
                        continue;
                    } else {
                        numTile = panel.tileM.matrizMapa[yJugCelda+dist][xJugCelda];
                        if(numTile != 0) {
                            continue;
                        }
                        /* Decidir si se va a poner un Zombie o un laser */
                        spawn(yJugCelda+dist, xJugCelda,i);
                    }
                } else {
                    /* Arriba */
                    dist = rand.nextInt(closeness)+3;
                    if (xJugCelda - dist < panel.tileM.matrizMapa.length-1) {
                        continue;
                    } else {
                        numTile = panel.tileM.matrizMapa[yJugCelda-dist][xJugCelda];
                        if(numTile != 0) {
                            continue;
                        }
                        /* Decidir si se va a poner un Zombie o un laser */
                        spawn(yJugCelda-dist, xJugCelda,i);
                    }
                }
            }
        }  
    }
    
    public int determinarDireccion(){
        return rand.nextInt(2);
    }
    
    
    public void spawn(int yCelda, int xCelda, int pos) {
        if(rand.nextInt(2) == 0) {
            panel.enemigos[pos] = new Zombie(panel);
            panel.enemigos[pos].laberintoX = yCelda*panel.tamañoTejas;
            panel.enemigos[pos].laberintoY = xCelda*panel.tamañoTejas;
        } else {
            panel.enemigos[pos] = new Laser(panel);
            panel.enemigos[pos].laberintoX = yCelda*panel.tamañoTejas;
            panel.enemigos[pos].laberintoY = xCelda*panel.tamañoTejas;
        }
    }
}

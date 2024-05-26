import Application.FlappyBird;

import javax.swing.*;

/**
 * The Main class sets up the JFrame for the Flappy Bird game.
 */
public class Main extends JFrame {

    int width = 960;
    int height = 540;

    /**
     * Constructs a new Main object and sets up the game window.
     */
    public Main() {
        setSize(width, height);

        add(new FlappyBird(width, height));
        pack();

        setLocationRelativeTo(null);
        setResizable(false);
        setTitle("Flappy Bird");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
    }

    /**
     * The main method to start the Flappy Bird game.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        new Main();
    }
}
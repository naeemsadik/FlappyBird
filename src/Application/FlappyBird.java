package Application;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

/**
 * The FlappyBird class is the main game panel.
 */
public class FlappyBird extends JPanel implements ActionListener, KeyListener {
    private final int boardWidth, boardHeight;
    private BufferedImage backgroundImage, birdImage, pillarUpImage, pillarDownImage;
    private Bird bird;
    private final ArrayList<Pillar> pillars;
    private static final int GRAVITY = 2;
    private static final int BIRD_JUMP_HEIGHT = 30;
    private static final int BIRD_MOVE_AMOUNT = 30;
    private static final int PILLAR_MOVE_SPEED = 2;
    private static final int PILLAR_GAP_HEIGHT = 100;
    private static final int PILLAR_WIDTH = 60;
    private static final int PILLAR_HEIGHT = 500;
    private static boolean gameOver = false;
    private int score = 0;
    private static int hScore;
    Timer gameLoop = new Timer(1000 / 60, this);

    /**
     * Constructs a new FlappyBird game panel.
     *
     * @param boardWidth  the width of the game board
     * @param boardHeight the height of the game board
     */
    public FlappyBird(int boardWidth, int boardHeight) {
        this.boardWidth = boardWidth;
        this.boardHeight = boardHeight;
        setPreferredSize(new Dimension(boardWidth, boardHeight));

        gameLoop.start();
        pillars = new ArrayList<>();

        try {
            backgroundImage = ImageIO.read(getClass().getResource("/Application/background.png"));
        } catch (IOException e) {
            System.out.println("Background image not found: " + e.getMessage());
        }

        try {
            birdImage = ImageIO.read(getClass().getResource("/Application/bird.png"));
            if (birdImage != null) {
                bird = new Bird(birdImage, boardWidth / 8, (boardHeight / 2) - 20);
            } else {
                throw new IOException("Failed to load bird image");
            }
        } catch (IOException e) {
            System.out.println("Bird image not found or could not be loaded: " + e.getMessage());
        }

        try {
            pillarUpImage = ImageIO.read(getClass().getResource("/Application/pillar-down.png"));
            pillarDownImage = ImageIO.read(getClass().getResource("/Application/pillar-up.png"));
        } catch (IOException e) {
            System.out.println("Pillar images not found: " + e.getMessage());
        }

        if (pillarDownImage != null && pillarUpImage != null) {
            for (int i = 0; i < 3; i++) {
                int pillarX = boardWidth + i * 400;
                int randomHeight = generateRandomHeight();
                pillars.add(new Pillar(pillarX, randomHeight));
            }
        }

        addKeyListener(this);
        setFocusable(true);
        requestFocusInWindow();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    /**
     * Draws the game components on the screen.
     *
     * @param g the Graphics object
     */
    public void draw(Graphics g) {
        g.drawImage(backgroundImage, 0, 0, boardWidth, boardHeight, null);
        if (bird != null) {
            bird.draw(g);
        }
        for (Pillar pillar : pillars) {
            pillar.draw(g);
        }
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Score: " + score, 10, 25);

        if (score > hScore) hScore = score;
        g.setColor(Color.RED);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Highest Score: " + hScore, boardWidth - 200, 25);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gameOver) {
            moveBird();
            movePillars();
            checkCollision();
            updateScore();
            repaint();
        }
    }

    /**
     * Moves the bird down due to gravity.
     */
    public void moveBird() {
        if (bird != null && (bird.birdY + bird.birdHeight) < boardHeight) {
            bird.birdY += GRAVITY;
        }
    }

    /**
     * Moves the pillars to the left.
     */
    public void movePillars() {
        for (Pillar pillar : pillars) {
            pillar.pillarX -= PILLAR_MOVE_SPEED;
        }

        if (!pillars.isEmpty()) {
            Pillar lastPillar = pillars.get(pillars.size() - 1);
            if (lastPillar.pillarX < boardWidth - 400) {
                int randomHeight = generateRandomHeight();
                pillars.add(new Pillar(lastPillar.pillarX + 400, randomHeight));
                if (pillars.size() > 3) {
                    pillars.remove(0);
                }
            }
        }
    }

    /**
     * Generates a random height for the pillars.
     *
     * @return a random height for the pillar
     */
    public int generateRandomHeight() {
        Random random = new Random();
        return random.nextInt(boardHeight - PILLAR_GAP_HEIGHT - 100) + 50;
    }

    /**
     * Checks for collisions between the bird and the pillars or the edges of the screen.
     */
    public void checkCollision() {
        if (bird != null) {
            Rectangle birdBounds = bird.getBounds();

            for (Pillar pillar : pillars) {
                Rectangle pillarUpBounds = pillar.getUpperBounds();
                Rectangle pillarDownBounds = pillar.getLowerBounds();

                if (birdBounds.intersects(pillarUpBounds) || birdBounds.intersects(pillarDownBounds) || bird.birdY <= 0 || bird.birdY + bird.birdHeight >= boardHeight) {
                    gameOver();
                    return;
                }
            }
        }
    }

    /**
     * Updates the score when the bird successfully passes through a pillar.
     */
    public void updateScore() {
        if (bird != null) {
            for (Pillar pillar : pillars) {
                if (pillar.pillarX + PILLAR_WIDTH / 2 < bird.birdX && !pillar.pointScored) {
                    score++;
                    pillar.pointScored = true;
                }
            }
        }
    }

    /**
     * Ends the game when a collision occurs.
     */
    public void gameOver() {
        System.out.println("Game Over");
        gameLoop.stop();
        gameOver = true;
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE && bird != null) {
            bird.birdY -= BIRD_JUMP_HEIGHT;
        }
        if (e.getKeyCode() == KeyEvent.VK_Q && bird != null) {
            restartGame();
        }

        if (e.getKeyCode() == KeyEvent.VK_RIGHT && bird != null) {
            bird.birdX += BIRD_MOVE_AMOUNT;
        } else if (e.getKeyCode() == KeyEvent.VK_LEFT && bird != null) {
            bird.birdX -= BIRD_MOVE_AMOUNT;
        } else if (e.getKeyCode() == KeyEvent.VK_UP && bird != null) {
            bird.birdY -= BIRD_JUMP_HEIGHT;
        } else if (e.getKeyCode() == KeyEvent.VK_DOWN && bird != null) {
            bird.birdY += BIRD_JUMP_HEIGHT;
        }
    }

    /**
     * Restarts the game, resetting the score and bird position.
     */
    private void restartGame() {
        score = 0;
        pillars.clear();
        bird = new Bird(birdImage, boardWidth / 8, (boardHeight / 2) - 20);

        for (int i = 0; i < 3; i++) {
            int pillarX = boardWidth + i * 400;
            int randomHeight = generateRandomHeight();
            pillars.add(new Pillar(pillarX, randomHeight));
        }

        gameLoop.start();
        gameOver = false;
    }

    /**
     * The Bird class represents the bird in the game.
     */
    class Bird {
        int birdX;
        int birdY;
        int birdWidth = 40;
        int birdHeight = 28;
        Image birdImage;

        /**
         * Constructs a new Bird object.
         *
         * @param birdImage the image of the bird
         * @param birdX     the initial x position of the bird
         * @param birdY     the initial y position of the bird
         */
        public Bird(Image birdImage, int birdX, int birdY) {
            this.birdImage = birdImage;
            this.birdX = birdX;
            this.birdY = birdY;
        }

        /**
         * Draws the bird on the screen.
         *
         * @param g the Graphics object
         */
        public void draw(Graphics g) {
            g.drawImage(birdImage, birdX, birdY, birdWidth, birdHeight, null);
        }

        /**
         * Gets the bounds of the bird for collision detection.
         *
         * @return a Rectangle representing the bounds of the bird
         */
        public Rectangle getBounds() {
            return new Rectangle(birdX, birdY, birdWidth, birdHeight);
        }
    }

    /**
     * The Pillar class represents a pair of pillars in the game.
     */
    class Pillar {
        int pillarX;
        int gapY;
        boolean pointScored = false;

        /**
         * Constructs a new Pillar object.
         *
         * @param pillarX the initial x position of the pillar
         * @param gapY    the y position of the gap between the pillars
         */
        public Pillar(int pillarX, int gapY) {
            this.pillarX = pillarX;
            this.gapY = gapY;
        }

        /**
         * Draws the pillar pair on the screen.
         *
         * @param g the Graphics object
         */
        public void draw(Graphics g) {
            g.drawImage(pillarUpImage, pillarX, gapY - PILLAR_HEIGHT, PILLAR_WIDTH, PILLAR_HEIGHT, null);
            g.drawImage(pillarDownImage, pillarX, gapY + PILLAR_GAP_HEIGHT, PILLAR_WIDTH, PILLAR_HEIGHT, null);
        }

        /**
         * Gets the bounds of the upper pillar for collision detection.
         *
         * @return a Rectangle representing the bounds of the upper pillar
         */
        public Rectangle getUpperBounds() {
            return new Rectangle(pillarX, gapY - PILLAR_HEIGHT, PILLAR_WIDTH, PILLAR_HEIGHT);
        }

        /**
         * Gets the bounds of the lower pillar for collision detection.
         *
         * @return a Rectangle representing the bounds of the lower pillar
         */
        public Rectangle getLowerBounds() {
            return new Rectangle(pillarX, gapY + PILLAR_GAP_HEIGHT, PILLAR_WIDTH, PILLAR_HEIGHT);
        }
    }
}

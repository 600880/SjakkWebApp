package brett;

import javax.imageio.ImageIO;
import javax.swing.*;

import brikke.Bonde;
import brikke.Brikke;
import brikke.Dronning;
import brikke.Farge;
import brikke.Hest;
import brikke.Konge;
import brikke.Loeper;
import brikke.Taarn;
import spill.Parti;
import spill.Spiller;
import spill.Trekk;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ChessBoard extends JPanel {

    private static final int TILE_SIZE = 80;
    private static final int BOARD_SIZE = 8;
    private final JFrame frame = new JFrame();
    private Object lock = new Object();
    
    private Parti parti;
    private Brett brett;
    private Spiller hvit;
    private Spiller svart;
    private int selX = -1;
    private int selY = -1;
    private boolean isFlipped = false;
    
    private Image bondeHvit;
    private Image bondeSvart;
    private Image hestHvit;
    private Image hestSvart;
    private Image loeperHvit;
    private Image loeperSvart;
    private Image taarnHvit;
    private Image taarnSvart;
    private Image dronningHvit;
    private Image dronningSvart;
    private Image kongeHvit;
    private Image kongeSvart;

    
    public ChessBoard(Parti parti, Spiller hvit, Spiller svart) {
    	this.parti = parti;
    	this.brett = parti.getBrett();
    	this.hvit = hvit;
    	this.svart = svart;
    	
        hvit.setChessBoard(this);
        svart.setChessBoard(this);
      
        frame.setLayout(null); // Absolute layout for precise positioning
        this.setBounds(0, 0, 80*8, 80*8);
        frame.add(this);
    	
        createButtons();
    	loadImages();
    	
        frame.setSize(82 * 11, 85 * 8);
        frame.setVisible(true);
        
        synchronized (lock) {
            try {
                lock.wait(); // Main thread waits here
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    	
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Get the raw click position
                int x = e.getX();
                int y = e.getY();

                // Calculate column and row in 0-based coordinates
                int col = BOARD_SIZE - x / TILE_SIZE - 1;
                int row = y / TILE_SIZE;

                // Adjust for flipped board orientation
                if (isFlipped) {
                    col = x / TILE_SIZE; // Flip horizontally
                    row = BOARD_SIZE - row - 1; // Flip vertically
                }

                // Convert to 1-based indexing
                selX = BOARD_SIZE - col;
                selY = BOARD_SIZE - row; // Reverse row order for proper orientation
            }
        });
    }
    
    private void loadImages() {
        try {
            bondeHvit = ImageIO.read(getClass().getResource("/resources/bondeHvit.png"));
            bondeSvart = ImageIO.read(getClass().getResource("/resources/bondeSvart.png"));
            hestHvit = ImageIO.read(getClass().getResource("/resources/hestHvit.png"));
            hestSvart = ImageIO.read(getClass().getResource("/resources/hestSvart.png"));
            loeperHvit = ImageIO.read(getClass().getResource("/resources/loeperHvit.png"));
            loeperSvart = ImageIO.read(getClass().getResource("/resources/loeperSvart.png"));
            taarnHvit = ImageIO.read(getClass().getResource("/resources/taarnHvit.png"));
            taarnSvart = ImageIO.read(getClass().getResource("/resources/taarnSvart.png"));
            dronningHvit = ImageIO.read(getClass().getResource("/resources/dronningHvit.png"));
            dronningSvart = ImageIO.read(getClass().getResource("/resources/dronningSvart.png"));
            kongeHvit = ImageIO.read(getClass().getResource("/resources/kongeHvit.png"));
            kongeSvart = ImageIO.read(getClass().getResource("/resources/kongeSvart.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void createButtons() {
    	JLabel nameLabel = new JLabel("Player: " + svart.toString());
    	nameLabel.setBounds(700, 30, 200, 30);
    	frame.add(nameLabel);
    	
    	JLabel nameLabel2 = new JLabel("Player: " + hvit.toString());
    	nameLabel2.setBounds(700, 580, 200, 30);
    	frame.add(nameLabel2);
    	
    	// Create the slider (1 to 7)
        JSlider slider = new JSlider(1, 7); // Min: 1, Max: 7
        slider.setBounds(670, 200, 200, 50);
        slider.setMajorTickSpacing(1); // Major ticks spaced by 1
        slider.setPaintTicks(true); // Show tick marks
        slider.setPaintLabels(true); // Show labels for the ticks
        slider.setValue(svart.getDybde());
        frame.add(slider);
        
        // Create the slider (1 to 7)
        JSlider slider2 = new JSlider(1, 7); // Min: 1, Max: 7
        slider2.setBounds(670, 420, 200, 50);
        slider2.setMajorTickSpacing(1); // Major ticks spaced by 1
        slider2.setPaintTicks(true); // Show tick marks
        slider2.setPaintLabels(true); // Show labels for the ticks
        slider2.setValue(hvit.getDybde());
        frame.add(slider2);  
        
        // Create the button and set its position
        JButton actionButton = new JButton("Start");
        actionButton.setBounds(650, 310, 78, 50);
        actionButton.addActionListener(e -> {
            svart.setDybde(slider.getValue());
            hvit.setDybde(slider2.getValue());
        });
        actionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                synchronized (lock) {
                    lock.notify(); // Notify the waiting thread
                }
            }
        });
        frame.add(actionButton);
        
        // Create a button for flipping the board
        JButton flipBoardButton = new JButton("Flip");
        flipBoardButton.setBounds(728, 310, 78, 50);
        flipBoardButton.addActionListener(e -> toggleFlipBoard());
        frame.add(flipBoardButton);
        
        // Create a button for displaying the evaluation
        JButton terminateButton = new JButton("Close");
        terminateButton.setBounds(802, 310, 78, 50);
        terminateButton.addActionListener(e -> System.exit(0));
        frame.add(terminateButton);
        
        // Create the button and set its position
        JButton blackButton = new JButton("Human");
        blackButton.setBounds(670, 100, 100, 50);
        blackButton.addActionListener(e -> svart.setCPU(false));
        frame.add(blackButton);
        JButton blackButton2 = new JButton("CPU");
        blackButton2.setBounds(770, 100, 100, 50);
        blackButton2.addActionListener(e -> svart.setCPU(true));
        frame.add(blackButton2);
        JButton whiteButton = new JButton("Human");
        whiteButton.setBounds(670, 500, 100, 50);
        whiteButton.addActionListener(e -> hvit.setCPU(false));
        frame.add(whiteButton);
        JButton whiteButton2 = new JButton("CPU");
        whiteButton2.setBounds(770, 500, 100, 50);
        whiteButton2.addActionListener(e -> hvit.setCPU(true));
        frame.add(whiteButton2);
    }
    
    public int getX() {
    	return selX;
    }
    
    public void setX(int x) {
    	this.selX = x;
    }

    public int getY() {
    	return selY;
    }
    
    public void setY(int y) {
    	this.selY = y;
    }
    
    private void toggleFlipBoard() {
        isFlipped = !isFlipped;
        repaint(); // Redraw the board after flipping
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        this.setBackground(Color.WHITE);
        Color light = new Color(0xf7efe6);
        Color dark = new Color(0x786255);

        // Draw the board squares
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                // Determine the color of the square
            	g.setColor((row + col) % 2 != 0 ? dark : light);
                g.fillRect(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            }
        }

        // Draw highlights for the last move
        Trekk lastMove = parti.getSisteTrekk();
        if (lastMove != null) {
            int startX = isFlipped ? BOARD_SIZE - lastMove.getStartPos().getX() : lastMove.getStartPos().getX() - 1;
            int startY = isFlipped ? lastMove.getStartPos().getY() - 1 : BOARD_SIZE - lastMove.getStartPos().getY();
            int newX = isFlipped ? BOARD_SIZE - lastMove.getNyPos().getX() : lastMove.getNyPos().getX() - 1 ;
            int newY = isFlipped ? lastMove.getNyPos().getY() - 1 : BOARD_SIZE - lastMove.getNyPos().getY();

            g.setColor(new Color(255, 255, 100));
            g.fillRect(startX * TILE_SIZE, startY * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            g.fillRect(newX * TILE_SIZE, newY * TILE_SIZE, TILE_SIZE, TILE_SIZE);
        }

        // Highlight selected square
        if (selX != -1 && selY != -1) {
            int displaySelX = isFlipped ? BOARD_SIZE - selX : selX - 1;
            int displaySelY = isFlipped ? selY - 1 : BOARD_SIZE - selY;

            g.setColor(Color.YELLOW);
            g.fillRect(displaySelX * TILE_SIZE, displaySelY * TILE_SIZE, TILE_SIZE, TILE_SIZE);
        }

        // Draw pieces
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                int displayRow = isFlipped ? row : BOARD_SIZE - row - 1;
                int displayCol = isFlipped ? BOARD_SIZE - col - 1 : col ;

                Rute rute = brett.finnRute(col + 1, row + 1); // 1-based indexing
                if (rute.harBrikke()) {
                    Brikke brikke = rute.getBrikke();
                    Image pieceImage = null;

                    if (brikke instanceof Bonde) {
                        pieceImage = brikke.getFarge() == Farge.HVIT ? bondeHvit : bondeSvart;
                    } else if (brikke instanceof Hest) {
                        pieceImage = brikke.getFarge() == Farge.HVIT ? hestHvit : hestSvart;
                    } else if (brikke instanceof Loeper) {
                        pieceImage = brikke.getFarge() == Farge.HVIT ? loeperHvit : loeperSvart;
                    } else if (brikke instanceof Taarn) {
                        pieceImage = brikke.getFarge() == Farge.HVIT ? taarnHvit : taarnSvart;
                    } else if (brikke instanceof Dronning) {
                        pieceImage = brikke.getFarge() == Farge.HVIT ? dronningHvit : dronningSvart;
                    } else if (brikke instanceof Konge) {
                        pieceImage = brikke.getFarge() == Farge.HVIT ? kongeHvit : kongeSvart;
                    }

                    if (pieceImage != null) {
                        g.drawImage(pieceImage, displayCol * TILE_SIZE, displayRow * TILE_SIZE, TILE_SIZE, TILE_SIZE, this);
                    }
                }
            }
        }
    }
        
}
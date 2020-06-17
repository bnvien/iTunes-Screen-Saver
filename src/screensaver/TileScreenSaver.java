package screensaver;

import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JPanel;

public class TileScreenSaver {

	private JFrame frame;
	private final int ROW_NUM = 5;
	private final int TILE_SELECTING_INTERVAL = 2000;
	private final int FADING_INTERVAL = 100;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					TileScreenSaver window = new TileScreenSaver();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public TileScreenSaver() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.getContentPane().setBackground(Color.BLACK);
		frame.setBackground(Color.BLACK);
		frame.setAlwaysOnTop(true);
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		frame.setUndecorated(true);
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		/*
		 * TODO: Calculate the tile size and the number of columns based on ROW_NUM Then
		 * set up the layout to hold all the tiles in the center of the screen
		 */
		GraphicsEnvironment environment = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice device = environment.getDefaultScreenDevice();
		GraphicsConfiguration config = device.getDefaultConfiguration();
		Rectangle screenSize = config.getBounds();
		int tileHeight = screenSize.height / ROW_NUM;
		int tileWidth = tileHeight;
		int colNum = screenSize.width / tileWidth;
		int tileNum = ROW_NUM * colNum;
		int sideWidth = (screenSize.width - tileWidth * colNum) / 2;

		// Add a black empty left panel
		JPanel panelLeft = new JPanel();
		panelLeft.setPreferredSize(new Dimension(sideWidth, screenSize.height));
		panelLeft.setBackground(Color.BLACK);
		frame.getContentPane().add(panelLeft, BorderLayout.WEST);

		// Add a black empty right panel
		JPanel panelRight = new JPanel();
		panelRight.setPreferredSize(new Dimension(sideWidth, screenSize.height));
		panelRight.setBackground(Color.BLACK);
		frame.getContentPane().add(panelRight, BorderLayout.EAST);

		// Add a center panel to whole all the tiles
		JPanel panelCenter = new JPanel();
		panelCenter.setBackground(Color.BLACK);
		frame.getContentPane().add(panelCenter, BorderLayout.CENTER);
		panelCenter.setLayout(new GridLayout(ROW_NUM, colNum));

		/*
		 * TODO: Get all available images in the 'images' folder Then place them in the
		 * tiles
		 */
		File folder = new File("src/images");
		File[] listOfFiles = folder.listFiles();
		BufferedImage[] bufImages = new BufferedImage[listOfFiles.length];
		for (int i = 0; i < listOfFiles.length; i++) {
			try {
				bufImages[i] = ImageIO.read(new File("src/images/" + listOfFiles[i].getName()));
			} catch (IOException e) {
				System.err.println("Error retrieving file: " + "src/images/" + listOfFiles[i].getName());
			}
		}

		// In case there aren't enough images for all the tiles, duplicate the images in
		// hand
		int multiple = (int) Math.ceil((double) tileNum / listOfFiles.length);
		BufferedImage[] imageBank = new BufferedImage[bufImages.length * multiple];
		for (int i = 0; i < multiple; i++) {
			System.arraycopy(bufImages, 0, imageBank, bufImages.length * i, bufImages.length);
		}

		JLabel[] labels = new JLabel[tileNum];

		for (int i = 0; i < tileNum; i++) {
			ImageIcon icon = new ImageIcon(new ImageIcon(imageBank[i]).getImage().getScaledInstance(tileWidth,
					tileHeight, Image.SCALE_SMOOTH));
			JLabel label = new JLabel(icon);
			panelCenter.add(label);
			labels[i] = label;
		}

		/*
		 * TODO: Randomly select a tile to change its background image after
		 * TILE_SELECTING_INTERVAL Create fade-in effect when changing the background
		 * image
		 */
		// Create a timer to change background images
		Timer tileTimer = new Timer(TILE_SELECTING_INTERVAL, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Random rand = new Random();
				int currentIndex = rand.nextInt(tileNum);
				int newIndex = rand.nextInt(tileNum);

				JLabel currentLabel = labels[currentIndex];
				BufferedImage currentImage = imageBank[currentIndex];
				BufferedImage newImage = imageBank[newIndex];

				// Create a timer for fade-in effect
				Timer fadingTimer = new Timer(FADING_INTERVAL, new ActionListener() {
					float alpha = .0f;
					float difference = .1f;

					@Override
					public void actionPerformed(ActionEvent e) {
						alpha += difference;

						// Stop the fading effect when the new image is fully displayed
						if (alpha >= 1.0f) {
							alpha = 1.0f; // Handle floating point imprecision problem
							((Timer) e.getSource()).stop();
						}

						// Create a composite image of the current image and the new image
						BufferedImage compositeImage = new BufferedImage(tileWidth, tileHeight,
								BufferedImage.TYPE_INT_ARGB);
						Graphics2D g = compositeImage.createGraphics();
						g.setComposite(AlphaComposite.SrcOver.derive(1.0f - alpha));
						g.drawImage(new ImageIcon(currentImage).getImage().getScaledInstance(tileWidth, tileHeight,
								Image.SCALE_SMOOTH), 0, 0, null);
						g.setComposite(AlphaComposite.SrcOver.derive(alpha));
						g.drawImage(new ImageIcon(newImage).getImage().getScaledInstance(tileWidth, tileHeight,
								Image.SCALE_SMOOTH), 0, 0, null);
						g.dispose();

						// Apply the composite background to the selected tile
						ImageIcon compositeIcon = new ImageIcon(compositeImage);
						currentLabel.setIcon(compositeIcon);
					}
				});
				fadingTimer.start();
			}
		});
		tileTimer.start();
	}
}

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.Image;           
import java.awt.GridLayout;      
import java.io.*;
import java.util.*;
import javax.imageio.ImageIO;
import java.util.stream.Collectors;
import java.util.function.Consumer;

public class MessageEntryTool extends JFrame {
    private int messageCounter = 0;
    private JTextField faceSetField;
    private JTextField faceIndexField;
    private JTextArea textField;
    private JTextField fileNameField;
    private JTextArea previewArea;
    private JLabel facePreviewLabel;
    private BufferedImage loadedFaceset;
    private File configFile;
    private Properties config;
    private int sizeStepsFromNormal = 0;
    private JFrame facesetFrame;

    public MessageEntryTool() {
        setTitle("Message Entry Tool");
        setSize(900, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
        inputPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel topButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        JButton textMacrosButton = new JButton("Text Macros");
        textMacrosButton.addActionListener(e -> openMacroMenu());
        topButtonPanel.add(textMacrosButton);
        inputPanel.add(topButtonPanel);
        inputPanel.add(Box.createRigidArea(new Dimension(0, 8)));

		JPanel fileNamePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
		fileNamePanel.add(new JLabel("YAML File Name (no ext):"));
		fileNameField = new JTextField("dialogue_output", 15);
		fileNamePanel.add(fileNameField);
		JButton openYamlButton = new JButton("Load");
		openYamlButton.addActionListener(e -> openYamlFileChooser());
		fileNamePanel.add(openYamlButton);
		inputPanel.add(fileNamePanel);
		inputPanel.add(Box.createRigidArea(new Dimension(0, 8)));


        JPanel facesetPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        facesetPanel.add(new JLabel("Faceset (PNG):"));
        faceSetField = new JTextField(12);
        faceSetField.setEditable(true);
        facesetPanel.add(faceSetField);
        JButton loadFacesetButton = new JButton("Load");
        loadFacesetButton.addActionListener(e -> loadFaceset());
        facesetPanel.add(loadFacesetButton);
        inputPanel.add(facesetPanel);
        inputPanel.add(Box.createRigidArea(new Dimension(0, 8)));

        JPanel faceIndexPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        faceIndexPanel.add(new JLabel("Face Index:"));
        faceIndexField = new JTextField(4);
        faceIndexPanel.add(faceIndexField);
        inputPanel.add(faceIndexPanel);
        inputPanel.add(Box.createRigidArea(new Dimension(0, 8)));

        // Portrait + Macro/Non-Macro section
        facePreviewLabel = new JLabel();
        facePreviewLabel.setPreferredSize(new Dimension(106, 106));
        facePreviewLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        JPanel facePreviewRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        facePreviewRow.add(facePreviewLabel);

        JPanel nameBoxesPanel = new JPanel();
        nameBoxesPanel.setLayout(new BoxLayout(nameBoxesPanel, BoxLayout.Y_AXIS));

        // Macro Name Box
        JPanel macroPanel = new JPanel();
        macroPanel.setLayout(new BoxLayout(macroPanel, BoxLayout.Y_AXIS));
        macroPanel.add(new JLabel("Macro Name:"));
        JTextField macroNameField = new JTextField(10);
        macroPanel.add(macroNameField);
        JButton macroButton = new JButton("Insert Macro Name");
        macroButton.addActionListener(e -> {
            String name = macroNameField.getText().trim();
            if (!name.isEmpty()) {
                insertAtCursor("\\" + name);
            }
        });
        macroPanel.add(macroButton);
        nameBoxesPanel.add(macroPanel);
        nameBoxesPanel.add(Box.createRigidArea(new Dimension(0, 5)));

        // Non-Macro Name Box
        JPanel nonMacroPanel = new JPanel();
        nonMacroPanel.setLayout(new BoxLayout(nonMacroPanel, BoxLayout.Y_AXIS));
        nonMacroPanel.add(new JLabel("Non-Macro Name:"));
        JTextField nonMacroNameField = new JTextField(10);
        nonMacroPanel.add(nonMacroNameField);
        JButton nonMacroButton = new JButton("Insert Non-Macro Name");
        nonMacroButton.addActionListener(e -> {
            String name = nonMacroNameField.getText().trim();
            if (!name.isEmpty()) {
                insertAtCursor("\\n<\"" + name + "\">");
            }
        });
        nonMacroPanel.add(nonMacroButton);
        nameBoxesPanel.add(nonMacroPanel);

        facePreviewRow.add(nameBoxesPanel);
        inputPanel.add(facePreviewRow);
        inputPanel.add(Box.createRigidArea(new Dimension(0, 8)));

        inputPanel.add(new JLabel("Text:"));
        textField = new JTextArea(5, 20);
        JScrollPane textScroll = new JScrollPane(textField);
        inputPanel.add(textScroll);
        inputPanel.add(Box.createRigidArea(new Dimension(0, 8)));

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        JButton submitButton = new JButton("Add Message");
        JButton refreshPreviewButton = new JButton("Refresh Preview");
        buttonsPanel.add(submitButton);
        buttonsPanel.add(refreshPreviewButton);
        inputPanel.add(buttonsPanel);

        add(inputPanel, BorderLayout.WEST);

        previewArea = new JTextArea();
        previewArea.setEditable(false);
        JScrollPane previewScroll = new JScrollPane(previewArea);
        add(previewScroll, BorderLayout.CENTER);

        submitButton.addActionListener(e -> addMessage());
        refreshPreviewButton.addActionListener(e -> refreshPreview());

        loadLastConfig();
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                saveConfig();
            }
        });
        refreshPreview();
    }

	private void loadFaceset() {
		File facesetDir = new File("faceset");
		if (!facesetDir.exists()) {
			facesetDir.mkdirs(); // Create faceset folder if it doesn't exist
		}

		JFileChooser chooser = new JFileChooser(facesetDir); // Open chooser in faceset folder
		chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("PNG Images", "png"));
		int result = chooser.showOpenDialog(this);
		if (result == JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();
			faceSetField.setText(file.getName());
			try {
				loadedFaceset = ImageIO.read(file);
			} catch (IOException ex) {
				JOptionPane.showMessageDialog(this, "Failed to load image.");
				loadedFaceset = null;
				facePreviewLabel.setIcon(null);
				return;
			}
			openFacesetWindow();
		}
	}

    private class FacesetPanel extends JPanel {
    private final BufferedImage originalImage;
    private final Consumer<Integer> onFaceSelected;

    public FacesetPanel(BufferedImage img, Consumer<Integer> onFaceSelected) {
        this.originalImage = img;
        this.onFaceSelected = onFaceSelected;

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int panelWidth = getWidth();
                int panelHeight = getHeight();

                int cols = originalImage.getWidth() / 106;
                int rows = originalImage.getHeight() / 106;

                double cellWidth = panelWidth / (double) cols;
                double cellHeight = panelHeight / (double) rows;

                int col = (int)(e.getX() / cellWidth);
                int row = (int)(e.getY() / cellHeight);

                if (col >= cols || row >= rows) return;

                int index = row * cols + col;
                onFaceSelected.accept(index);
                updateFacePreview(col, row);
            }

            private void updateFacePreview(int col, int row) {
                int x = col * 106;
                int y = row * 106;
                if (x + 106 > originalImage.getWidth() || y + 106 > originalImage.getHeight()) return;

                BufferedImage subImg = originalImage.getSubimage(x, y, 106, 106);
                ImageIcon icon = new ImageIcon(subImg.getScaledInstance(106, 106, Image.SCALE_SMOOTH));
                facePreviewLabel.setIcon(icon);
            }
        });
    }

    @Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (originalImage == null) return;

		int panelWidth = getWidth();
		int panelHeight = getHeight();

		int imageWidth = originalImage.getWidth();
		int imageHeight = originalImage.getHeight();

		// Calculate scale while preserving aspect ratio
		double scaleX = panelWidth / (double) imageWidth;
		double scaleY = panelHeight / (double) imageHeight;
		double scale = Math.min(scaleX, scaleY); // Keep aspect ratio

		int drawWidth = (int) (imageWidth * scale);
		int drawHeight = (int) (imageHeight * scale);

		// Center the image in the panel
		int drawX = (panelWidth - drawWidth) / 2;
		int drawY = (panelHeight - drawHeight) / 2;

		// Draw the scaled image
		g.drawImage(originalImage, drawX, drawY, drawWidth, drawHeight, null);

		// Draw grid lines on top
		int cols = imageWidth / 106;
		int rows = imageHeight / 106;

		double cellWidth = drawWidth / (double) cols;
		double cellHeight = drawHeight / (double) rows;

		g.setColor(new Color(255, 255, 255, 100));
		for (int x = 0; x <= cols; x++) {
			int gx = (int) (drawX + x * cellWidth);
			g.drawLine(gx, drawY, gx, drawY + drawHeight);
		}
		for (int y = 0; y <= rows; y++) {
			int gy = (int) (drawY + y * cellHeight);
			g.drawLine(drawX, gy, drawX + drawWidth, gy);
		}
	}
}


	private void openFacesetWindow() {
		if (loadedFaceset == null) return;

			facesetFrame = new JFrame("Faceset Selector");
			facesetFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			facesetFrame.setLayout(new BorderLayout());

			String facesetFileName = faceSetField.getText().trim();

			FacesetPanel panel = new FacesetPanel(loadedFaceset, index -> {
			faceIndexField.setText(String.valueOf(index));
			faceSetField.setText(facesetFileName);
		});

    facesetFrame.add(panel, BorderLayout.CENTER);

    // ✅ Add this block to repaint on resize
    facesetFrame.addComponentListener(new ComponentAdapter() {
        @Override
        public void componentResized(ComponentEvent e) {
            panel.repaint();
        }
    });

    facesetFrame.setSize(600, 600);
    facesetFrame.setLocationRelativeTo(this);
    facesetFrame.setVisible(true);
}



	private void addMessage() {
		String faceSetRaw = faceSetField.getText().trim();
		String faceSet = faceSetRaw.endsWith(".png") ? faceSetRaw.substring(0, faceSetRaw.length() - 4) : faceSetRaw;
		String faceIndexText = faceIndexField.getText().trim();
		boolean faceSetEmpty = faceSet.isEmpty();
		boolean faceIndexEmpty = faceIndexText.isEmpty();

		// Enforce both-or-none logic
		if (faceSetEmpty ^ faceIndexEmpty) { // XOR: only one is filled
			if (faceSetEmpty) {
				JOptionPane.showMessageDialog(this, "Faceset is blank.");
			} else {
				JOptionPane.showMessageDialog(this, "Faceindex is blank.");
			}
			return;
		}

		int index = -1;
		if (!faceIndexEmpty) {
			try {
				index = Integer.parseInt(faceIndexText);
			} catch (NumberFormatException ex) {
				JOptionPane.showMessageDialog(this, "Face Index must be a valid integer.");
				return;
			}
		}

		String text = textField.getText();
		String fileBaseName = fileNameField.getText().trim();
		if (fileBaseName.isEmpty()) {
			JOptionPane.showMessageDialog(this, "Please enter a YAML filename.");
			return;
		}

		File saveDir = new File("save");
		if (!saveDir.exists() && !saveDir.mkdirs()) {
			JOptionPane.showMessageDialog(this, "Failed to create save folder.");
			return;
		}

		File outFile = new File(saveDir, fileBaseName + ".yaml");

		int nextIndex = 0;
		if (outFile.exists()) {
			try {
				List<String> lines = java.nio.file.Files.readAllLines(outFile.toPath());
				for (String line : lines) {
					line = line.trim();
					if (line.startsWith("message_")) {
						try {
							int number = Integer.parseInt(line.substring(8, line.indexOf(":")));
							if (number >= nextIndex) {
								nextIndex = number + 1;
							}
						} catch (Exception ignored) {}
					}
				}
			} catch (IOException ex) {
				JOptionPane.showMessageDialog(this, "Error reading existing YAML: " + ex.getMessage());
				return;
			}
		}

		StringBuilder yamlEntry = new StringBuilder();
			yamlEntry.append(String.format("message_%d:\n", nextIndex));

		if (!faceSetEmpty && index >= 0) {
			yamlEntry.append(String.format("  faceset: \"%s\"\n", faceSet));
			yamlEntry.append(String.format("  faceindex: %d\n", index));
		}

		yamlEntry.append(String.format("  text: %s\n\n", indentText(text, 0)));

		try (FileWriter fw = new FileWriter(outFile, true)) {
			fw.write(yamlEntry.toString());
		} catch (IOException ex) {
			JOptionPane.showMessageDialog(this, "Error writing to file: " + ex.getMessage());
			return;
		}

		textField.setText("");
		messageCounter++;
		refreshPreview();
	}


    private static String indentText(String text, int spaces) {
        String indent = " ".repeat(spaces);
        return Arrays.stream(text.split("\\n"))
                .map(line -> indent + line)
                .collect(Collectors.joining("\n"));
    }

	private void refreshPreview() {
		String filename = fileNameField.getText().trim();  // ✅ Declare it here

		if (filename.isEmpty()) {
			previewArea.setText("Please enter a YAML filename.");
			return;
		}

		File file = new File("save", filename + ".yaml");  // ✅ optional: also fix path
		if (!file.exists()) {
			previewArea.setText("File does not exist yet.");
			return;
		}

    try {
        String content = new String(java.nio.file.Files.readAllBytes(file.toPath()));
        previewArea.setText(content);
    } catch (IOException e) {
        previewArea.setText("Error reading file: " + e.getMessage());
    }
}

		private void openYamlFileChooser() {
			JFileChooser chooser = new JFileChooser(new File("save"));
		chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("YAML files", "yaml"));
		int result = chooser.showOpenDialog(this);
		if (result == JFileChooser.APPROVE_OPTION) {
			File selectedFile = chooser.getSelectedFile();
			// Load file content into previewArea
			try {
				String content = new String(java.nio.file.Files.readAllBytes(selectedFile.toPath()));
				previewArea.setText(content);

				// Set filenameField without extension and path
				String fileName = selectedFile.getName();
				if (fileName.endsWith(".yaml")) {
					fileName = fileName.substring(0, fileName.length() - 5);
				}
				fileNameField.setText(fileName);
			} catch (IOException ex) {
				JOptionPane.showMessageDialog(this, "Error reading YAML file: " + ex.getMessage());
			}
		}
	}

    private void loadLastConfig() {
        configFile = new File("message_entry_tool.properties");
        config = new Properties();
        if (configFile.exists()) {
            try (FileInputStream in = new FileInputStream(configFile)) {
                config.load(in);
                fileNameField.setText(config.getProperty("filename", "dialogue_output"));
                faceSetField.setText(config.getProperty("faceset", ""));
                faceIndexField.setText(config.getProperty("faceindex", "0"));
            } catch (IOException e) {
                // ignore
                // ignore
            }
        }
    }

    private void saveConfig() {
        config.setProperty("filename", fileNameField.getText());
        config.setProperty("faceset", faceSetField.getText());
        config.setProperty("faceindex", faceIndexField.getText());
        try (FileOutputStream out = new FileOutputStream(configFile)) {
            config.store(out, "MessageEntryTool config");
        } catch (IOException e) {
            // ignore
        }
    }

    private void openMacroMenu() {
        JDialog macroDialog = new JDialog(this, "Text Macros", false);
        macroDialog.setLayout(new GridLayout(0, 1));
        JButton colorButton = new JButton("Color Text");
        colorButton.addActionListener(e -> {
            macroDialog.dispose();
            openColorMenu();
        });
        JButton moneyButton = new JButton("Show Money");
        moneyButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "This shows the player's balance in the top-right. Continue?",
                    "Confirm",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                insertAtCursor("\\$");
            }
        });
        JButton sizeButton = new JButton("Size");
        sizeButton.addActionListener(e -> openSizeMenu());
        JButton waitButton = new JButton("Wait");
        waitButton.addActionListener(e -> openWaitMenu());
        java.util.List<JButton> buttons = Arrays.asList(colorButton, moneyButton, sizeButton, waitButton);
        buttons.sort(Comparator.comparing(AbstractButton::getText));
        for (JButton b : buttons) macroDialog.add(b);
        macroDialog.pack();
        macroDialog.setLocationRelativeTo(this);
        macroDialog.setVisible(true);
    }

    private void insertAtCursor(String text) {
        textField.insert(text, textField.getCaretPosition());
    }

    private void openColorMenu() {
        JDialog colorDialog = new JDialog(this, "Color Text", false);
        colorDialog.setLayout(new GridLayout(0, 2));
        String[][] colors = {
            {"Default", "0"}, {"Blue", "1"}, {"Peach", "2"},
            {"Green", "3"}, {"Orange", "4"}, {"Grey", "7"},
            {"Red", "8"}, {"Teal", "11"}, {"Greyblue", "12"}, {"Pink", "13"}
        };
        for (String[] color : colors) {
            JButton btn = new JButton(color[0]);
            btn.addActionListener(e -> {
                insertAtCursor("\\\\c[" + color[1] + "]");
                colorDialog.dispose();
            });
            colorDialog.add(btn);
        }
        colorDialog.pack();
        colorDialog.setLocationRelativeTo(this);
        colorDialog.setVisible(true);
    }

    private void openSizeMenu() {
        JDialog sizeDialog = new JDialog(this, "Size", false);
        sizeDialog.setLayout(new GridLayout(1, 3));
        JButton bigger = new JButton("Bigger (\\\\{)");
        JButton normal = new JButton("Normal");
        JButton smaller = new JButton("Smaller (\\\\})");
        bigger.addActionListener(e -> {
            if (sizeStepsFromNormal < 2) {
                insertAtCursor("\\\\{");
                sizeStepsFromNormal++;
            } else {
                JOptionPane.showMessageDialog(this, "Maximum size reached.");
            }
        });
        normal.addActionListener(e -> {
            while (sizeStepsFromNormal > 0) {
                insertAtCursor("\\\\}");
                sizeStepsFromNormal--;
            }
            while (sizeStepsFromNormal < 0) {
                insertAtCursor("\\\\{");
                sizeStepsFromNormal++;
            }
        });
        smaller.addActionListener(e -> {
            if (sizeStepsFromNormal > -2) {
                insertAtCursor("\\\\}");
                sizeStepsFromNormal--;
            } else {
                JOptionPane.showMessageDialog(this, "Minimum size reached.");
            }
        });
        sizeDialog.add(bigger);
        sizeDialog.add(normal);
        sizeDialog.add(smaller);
        sizeDialog.pack();
        sizeDialog.setLocationRelativeTo(this);
        sizeDialog.setVisible(true);
    }

    private void openWaitMenu() {
        JDialog waitDialog = new JDialog(this, "Wait", false);
        waitDialog.setLayout(new GridLayout(0, 1));
        String[][] waits = {
            {"Wait Player Input (\\\\!)", "!"}, {"Wait 15 ticks (\\\\.)", "."},
            {"Wait 60 ticks (\\\\|)", "|"}, {"Don't Wait (\\\\^)", "^"}
        };
        for (String[] w : waits) {
            JButton btn = new JButton(w[0]);
            btn.addActionListener(e -> {
                insertAtCursor("\\\\" + w[1]);
                waitDialog.dispose();
            });
            waitDialog.add(btn);
        }
        JButton customWait = new JButton("Wait Custom (\\\\w[number])");
        customWait.addActionListener(e -> {
            String input = JOptionPane.showInputDialog(this, "Enter number of ticks:");
            if (input != null && input.matches("\\d+")) {
                insertAtCursor("\\\\w[" + input + "]");
            }
        });
        waitDialog.add(customWait);
        waitDialog.pack();
        waitDialog.setLocationRelativeTo(this);
        waitDialog.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MessageEntryTool tool = new MessageEntryTool();
            tool.setVisible(true);
        });
    }
}
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

public class PokeTrack extends JFrame {

	private static final long serialVersionUID = 1L;

	private static final int WIDTH = 600;
	private static final int HEIGHT = WIDTH / 2 + WIDTH / 6;
	private static final String TITLE = "PokéTrack";

	private String name1 = "Ash";
	private String name2 = "Brock";
	private int fontSize = 12;
	private int columns = 14;
	private int margin = fontSize / 2 + columns / 2;

	private List<Pokemon> pokemon;
	private DefaultTableModel model;

	public PokeTrack() throws IOException {
		this.pokemon = new ArrayList<Pokemon>();
		this.model = new DefaultTableModel() {

			private static final long serialVersionUID = 1L;

			Class<?>[] types = new Class<?>[] { String.class, String.class, String.class };
			boolean[] editable = new boolean[] { false, false, true };

			@Override
			public Class<?> getColumnClass(int columnIndex) {
				return this.types[columnIndex];
			}

			@Override
			public boolean isCellEditable(int rowIndex, int columnIndex) {
				return this.editable[columnIndex];
			}

		};

		JPanel panel = new JPanel(null);

		JLabel trainerLabel1 = new JLabel(name1 + "'s Pokémon");
		trainerLabel1.setBounds(margin, margin, 116, 30);
		panel.add(trainerLabel1);

		JTextField pokemonNameField1 = new HintTextField("name");
		pokemonNameField1.setBounds(trainerLabel1.getX() + trainerLabel1.getWidth(), margin, columns * 10, columns * 2);
		panel.add(pokemonNameField1);

		JLabel trainerLabel2 = new JLabel(name2 + "'s Pokémon");
		trainerLabel2.setBounds(margin, fontSize + margin * 2, 116, 30);
		panel.add(trainerLabel2);

		JTextField pokemonName2 = new HintTextField("name");
		pokemonName2.setBounds(trainerLabel2.getX() + trainerLabel2.getWidth(), fontSize + margin * 2, columns * 10, columns * 2);
		panel.add(pokemonName2);

		JTextField pokemonRoute = new HintTextField("route");
		pokemonRoute.setBounds(pokemonNameField1.getX() + pokemonNameField1.getWidth(), margin * 2, columns * 10, columns * 2);
		panel.add(pokemonRoute);

		JButton okButton = new JButton("OK");
		okButton.setBounds(pokemonRoute.getX() + pokemonRoute.getWidth() + margin / 4, pokemonRoute.getY() + pokemonRoute.getHeight() / 4, 48, pokemonRoute.getHeight() / 2);
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				addToTable(new Pokemon(pokemonNameField1.getText().isEmpty() ? Pokemon.NAMELESS : name1 + "'s " + pokemonNameField1.getText(), pokemonRoute.getText().isEmpty() ? Pokemon.ROUTELESS : pokemonRoute.getText()));
				addToTable(new Pokemon(pokemonName2.getText().isEmpty() ? Pokemon.NAMELESS : name2 + "'s " + pokemonName2.getText(), pokemonRoute.getText().isEmpty() ? Pokemon.ROUTELESS : pokemonRoute.getText()));
				pokemonNameField1.setText("");
				pokemonName2.setText("");
				pokemonRoute.setText("");
			}
		});
		panel.add(okButton);

		JTable t = new JTable(model);

		t.setRowSelectionAllowed(true);
		t.setBounds(margin - 1, pokemonName2.getY() + pokemonName2.getHeight() + margin, WIDTH - (margin * 2), HEIGHT - 100 - margin);
		t.setDefaultRenderer(String.class, new CustomTableRenderer());
		
		t.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_DELETE || e.getKeyCode() == KeyEvent.VK_BACK_SPACE) removeFromTable(t);
			}
		});

		model.addColumn("Name");
		model.addColumn("Route");
		model.addColumn("Dead");

		t.setRowHeight(16);
		TableColumnModel columnModel = t.getColumnModel();
		columnModel.getColumn(0).setPreferredWidth(200);
		columnModel.getColumn(1).setPreferredWidth(100);
		columnModel.getColumn(2).setPreferredWidth(5);

		load("data.csv");

		JScrollPane sp = new JScrollPane(t);
		sp.setBounds(t.getX(), t.getY(), t.getWidth(), t.getHeight());
		panel.add(sp);

		JLabel icon = new JLabel(new ImageIcon(ImageIO.read(new File("ico.png")).getScaledInstance(64, 64, Image.SCALE_SMOOTH)));

		int x = okButton.getX() + okButton.getWidth() + margin;
		int y = margin / 2 + 2;

		icon.setBounds(x, y, WIDTH - x - margin, HEIGHT - t.getHeight() - margin * 4 + 1);
		panel.add(icon);

		setAllFont(panel, new Font("Menlo", Font.PLAIN, fontSize));

		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				save(t);
				System.exit(0);
			}
		});

		setTitle(TITLE);
		Dimension size = new Dimension(WIDTH, HEIGHT);
		getContentPane().setMinimumSize(size);
		getContentPane().setMaximumSize(size);
		getContentPane().setPreferredSize(size);
		getContentPane().add(panel);
		pack();
		setLocationByPlatform(true);
		setLocationRelativeTo(null);
		setResizable(false);
		setVisible(true);
	}

	public void load(String f) {
		try (BufferedReader br = Files.newBufferedReader(Paths.get(f), StandardCharsets.US_ASCII)) {
			String line = br.readLine();

			while (line != null) {
				String[] attributes = line.split(",");
				String name = attributes[0];
				String route = attributes[1];
				boolean dead = (attributes[2].contains("false") ? false : true);

				addToTable(new Pokemon(name.substring(6, name.length()), route.substring(7, route.length()), dead));

				line = br.readLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void save(JTable t) {
		if (!isEmpty(t)) {
			Vector<?> row = (Vector<?>) model.getDataVector().elementAt(1);

			for (int i = 0; i < t.getRowCount(); i++) {
				row = (Vector<?>) model.getDataVector().elementAt(i);
				Pokemon p = pokemon.get(i);
				/** synchronizing list of pokemon with table representation */
				p.shoudlDie((row.get(2).toString().toLowerCase().equals("false")));
			}
		}

		try (BufferedWriter writer = new BufferedWriter(new FileWriter("data.csv"))) {
			pokemon.forEach(pokemon -> {
				try {
					writer.append(pokemon.toString() + "\n");
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	void addToTable(Pokemon p) {
		model.addRow(new String[] { p.getName(), p.getRoute(), (p.isDead()) ? "true" : "false" });
		pokemon.add(p);
	}

	public static boolean isEmpty(JTable t) {
		if (t != null && t.getModel() != null) return t.getModel().getRowCount() <= 0 ? true : false;
		return false;
	}

	public void removeFromTable(JTable t) {
		int[] rows = t.getSelectedRows();
		if (t.getSelectedRow() >= 0) pokemon.remove(t.getSelectedRow());
		for (int i = 0; i < rows.length; i++) model.removeRow(rows[i] - i);
	}

	public void setAllFont(Component c, Font f) {
		c.setFont(f);
		if (c instanceof Container) for (Component child : ((Container) c).getComponents()) {
			setAllFont(child, (child instanceof JButton) ? new Font("Menlo", Font.PLAIN, 11) : f);
		}
	}

	public static void main(String[] args) throws IOException {
		try {
			for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) if ("Metal".equals(info.getName())) {
				UIManager.setLookAndFeel(info.getClassName());
				break;
			}
		} catch (Exception e) {
			try {
				UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
				ex.printStackTrace();
			}
		}
		new PokeTrack();
	}

	class CustomTableRenderer extends DefaultTableCellRenderer {

		private static final long serialVersionUID = 1L;

		@Override
		public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			Component component = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
			String data = t.getValueAt(row, 0).toString();

			if (data.contains(name1)) component.setForeground(Color.RED);
			else if (data.contains(name2)) component.setForeground(Color.BLUE);
			else component.setForeground(Color.BLACK);
			return component;
		}

	}

	/** https://stackoverflow.com/a/24571681 */
	class HintTextField extends JTextField {

		private static final long serialVersionUID = 1L;

		private final String hint;

		public HintTextField(String hint) {
			this.hint = hint;
		}

		@Override
		public void paint(Graphics g) {
			super.paint(g);
			if (getText().length() == 0) {
				int h = getHeight();
				((Graphics2D) g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
				Insets insects = getInsets();
				FontMetrics fm = g.getFontMetrics();
				int backgroundColor = getBackground().getRGB();
				int foregroundColor = getForeground().getRGB();
				int hintColor = 0xfefefefe;
				int color = ((backgroundColor & hintColor) >>> 1) + ((foregroundColor & hintColor) >>> 1);
				g.setColor(new Color(color, true));
				g.drawString(hint, insects.left, h / 2 + fm.getAscent() / 2 - 2);
			}
		}

	}

	class Pokemon {

		public static final String NAMELESS = "???";
		public static final String ROUTELESS = "???";

		private String name;
		private String route;
		private boolean dead;

		public Pokemon(String name, String route, boolean dead) {
			this.name = name;
			this.route = route;
			this.dead = dead;
		}

		public Pokemon(String name, String route) {
			this.name = name;
			this.route = route;
			this.dead = false;
		}

		public Pokemon(String name) {
			this.name = name;
			this.route = ROUTELESS;
			this.dead = false;

		}

		public Pokemon() {
			this.name = NAMELESS;
			this.route = ROUTELESS;
			this.dead = false;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getRoute() {
			return route;
		}

		public void setRoute(String route) {
			this.route = route;
		}

		public boolean isDead() {
			return dead;
		}

		public void shoudlDie(boolean dead) {
			this.dead = dead;
		}

		@Override
		public String toString() {
			return "name: " + name + ",route: " + route + ",dead: " + dead + ",";
		}

	}

}
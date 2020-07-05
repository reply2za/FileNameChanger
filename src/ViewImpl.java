import java.awt.Color;
import java.awt.Dialog;
import java.awt.FileDialog;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

public class ViewImpl extends JFrame {

  private final JTextArea logTextArea;
  private int cutoff;
  private JPanel mainPanel;
  private JButton commitButton;
  private JTextField keywordTextField;
  private JButton selectDirectoryButton;
  private JLabel directoryNameLabel;
  private JTextField cutoffTextField;
  private JLabel keywordLabel;
  private JLabel cutoffLabel;
  private JTextField extensionTextField;
  private JLabel extensionLabel;
  private JLabel commitLabel;
  private JFileChooser fileChooser;
  private JButton logButton;
  private String directoryPathSelected;

  public ViewImpl() {
    super("Batch File Name Changer");
    this.setDefaultCloseOperation(EXIT_ON_CLOSE);
    selectDirectoryButton.addActionListener(e -> chooseDirectoryFileDialog());
    keywordTextField.addActionListener(e -> {
      if (!commitLabel.getText().contains("Make changes:")) {
        commitLabel.setForeground(Color.BLACK);
        commitLabel.setText("Make changes:");
      }
      keywordLabel.setText("Keyword: " + keywordTextField.getText());
    });
    cutoffTextField.addActionListener(e -> {
      if (!commitLabel.getText().contains("Make changes:")) {
        commitLabel.setForeground(Color.BLACK);
        commitLabel.setText("Make changes:");
      }
      cutoffLabel.setText("Cutoff (int): " + cutoffTextField.getText());
      try {
        cutoff = Integer.parseInt(cutoffTextField.getText());
      } catch (NumberFormatException nfe) {
        cutoffLabel.setText("Please insert a valid number:");
        cutoff = 0;
      }
      if (cutoff < 1) {
        cutoffLabel.setText("Please insert a valid number:");
      }
    });

    JMenuItem closeMenuItem = new JMenuItem();
    closeMenuItem.addActionListener(e -> System.exit(0));
    closeMenuItem.setAccelerator(KeyStroke
        .getKeyStroke(KeyEvent.VK_W, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
    this.add(closeMenuItem);
    logTextArea = new JTextArea(20, 25);

    JScrollPane jScrollPane = new JScrollPane(logTextArea);

    logTextArea.setText("Select a directory to show contents here:");
    logTextArea.setEditable(false);
    logTextArea.setLineWrap(true);
    logTextArea.setWrapStyleWord(true);

    logButton.addActionListener(e -> {
      jScrollPane.setSize(logTextArea.getPreferredScrollableViewportSize());
      logTextArea.setSize(logTextArea.getPreferredScrollableViewportSize());
      JOptionPane.showMessageDialog(null, jScrollPane, "Log", JOptionPane.PLAIN_MESSAGE);
      if (logTextArea.getSelectedText() != null) {
        commitLabel.setForeground(Color.BLACK);
        commitLabel.setText(
            "Make changes: (" + Integer.toString(logTextArea.getSelectedText().length())
                + " highlighted)");
      }
      this.setSize(mainPanel.getPreferredSize().width + 20,
          mainPanel.getPreferredSize().height + 20);
    });

    extensionTextField.addActionListener(e -> {
      if (!commitLabel.getText().contains("Make changes:")) {
        commitLabel.setForeground(Color.BLACK);
        commitLabel.setText("Make changes:");
      }
      extensionLabel.setText("Extension: " + extensionTextField.getText());
    });
    commitButton.addActionListener(e -> {
      if (extensionTextField.getText().isBlank() || cutoff < 1 || directoryNameLabel
          .getText().equals("Directory: none selected")) {
        commitLabel.setText("Please ensure that all values are set");
        commitLabel.setForeground(new Color(180, 70, 70));
        this.setSize(mainPanel.getPreferredSize().width + 20,
            mainPanel.getPreferredSize().height + 20);
        return;
      }
      ModelImpl m = new ModelImpl();
      m.performChange(directoryPathSelected, keywordTextField.getText(), cutoff,
          extensionTextField.getText());
      commitLabel.setForeground(Color.BLACK);
      if (m.wasErrorOnExit()) {
        commitLabel.setText("Made " + m.getNumberOfChanges() + " changes. (stopped due to error)");
      } else {
        commitLabel.setText("Made " + m.getNumberOfChanges() + " changes.");
      }
      StringBuilder logSB = new StringBuilder();
      logSB.append("Original file names:\n");
      int i = 0;
      for (String s : m.oldContents) {
        i++;
        logSB.append(i).append(". ").append(s).append("\n");
      }
      logSB.append("\nNew file names:\n");
      int j = 0;
      for (String s : m.newContents) {
        j++;
        if (j == i && m.wasErrorOnExit()) {
          logSB.append(j).append(". (Did not change file name)\n").append(s).append("\n");
        } else {
          logSB.append(j).append(". ").append(s).append("\n");
        }
      }
      logSB.append("\n");
      logTextArea.setText(logSB.toString());
    });

    this.add(mainPanel);
    this.pack();
    this.setSize(this.getPreferredSize().width + 20, this.getPreferredSize().height + 20);
    this.setLocationRelativeTo(null);
    this.setVisible(true);
  }

  /**
   * The UI to choose a file (depreciated).
   */
  public void chooseDirectory() {
    commitLabel.setForeground(Color.BLACK);
    commitLabel.setText("Make changes:");
    fileChooser = new JFileChooser();
    fileChooser.setDialogTitle("Choose a directory");
    fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    fileChooser.setAcceptAllFileFilterUsed(false);
    if (new File("/Users").exists()) {
      fileChooser.setCurrentDirectory(new File("/Users"));
    } else {
      fileChooser.setCurrentDirectory(new File("."));
    }
    if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
      directoryNameLabel.setText("Directory: " + fileChooser.getSelectedFile().toString());
      String[] directoryContents = fileChooser.getSelectedFile().list();
      StringBuilder sb = new StringBuilder("Contents of the directory selected:\n");
      if (directoryContents != null) {
        for (String s : directoryContents) {
          sb.append(s).append("\n");
        }
        logTextArea.setText(sb.toString());
      } else {
        sb.append("(empty)");
        logTextArea.setText(sb.toString());
      }
      this.directoryPathSelected = fileChooser.getSelectedFile().toString();
      this.setSize(this.getPreferredSize().width + 20, this.getPreferredSize().height + 20);
    }
  }

  /**
   * The UI to choose a file.
   */
  public void chooseDirectoryFileDialog() {
    commitLabel.setForeground(Color.BLACK);
    commitLabel.setText("Make changes:");
    FileDialog fileDialog = new FileDialog(new Dialog(this), "Select directory");
    fileDialog.setMode(FileDialog.LOAD);
    fileDialog.setMultipleMode(false);
    try {
      System.setProperty("apple.awt.fileDialogForDirectories", "true");
    } catch (Exception e) {
      e.printStackTrace();
      fileDialog.setTitle("Select a directory only - NOT A FILE");
    }
    fileDialog.setVisible(true);
    if (fileDialog.getDirectory() != null) {
      this.directoryPathSelected = fileDialog.getDirectory().concat(fileDialog.getFile());
      directoryNameLabel.setText("Directory: " + directoryPathSelected);
      String[] directoryContents = new File(directoryPathSelected).list();
      StringBuilder sb = new StringBuilder("Contents of the directory selected:\n");
      if (directoryContents != null) {
        for (String s : directoryContents) {
          sb.append(s).append("\n");
        }
        logTextArea.setText(sb.toString());
      } else {
        sb.append("(empty)");
        logTextArea.setText(sb.toString());
      }
    } else {
      directoryNameLabel.setText("Directory: " + "none selected");
    }
    this.setSize(this.getPreferredSize().width + 20, this.getPreferredSize().height + 20);

  }


}

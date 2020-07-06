import java.awt.Color;
import java.awt.Dialog;
import java.awt.FileDialog;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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
  private final JScrollPane jScrollPane;
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
  private JButton logButton;
  private JCheckBox includeHiddenFilesCheckBox;
  private String directoryPathSelected;
  private boolean includeHiddenFiles;

  public ViewImpl() {
    super("Batch File Name Changer");
    this.setDefaultCloseOperation(EXIT_ON_CLOSE);
    this.includeHiddenFiles = false;

    JMenuItem closeMenuItem = new JMenuItem();
    closeMenuItem.addActionListener(e -> System.exit(0));
    closeMenuItem.setAccelerator(KeyStroke
        .getKeyStroke(KeyEvent.VK_W, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
    logTextArea = new JTextArea(20, 25);

    jScrollPane = new JScrollPane(logTextArea);

    logTextArea.setText("Select a directory to show contents here:");
    logTextArea.setEditable(false);
    logTextArea.setLineWrap(true);
    logTextArea.setWrapStyleWord(true);

    initializeActionListeners();

    this.add(closeMenuItem);
    this.add(mainPanel);
    this.pack();
    this.setSize(this.getPreferredSize().width + 20, this.getPreferredSize().height + 20);
    this.setLocationRelativeTo(null);
    this.setVisible(true);
  }

  /**
   * Initializes the action listeners.
   */
  private void initializeActionListeners() {
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
      if (cutoff < 1 || directoryNameLabel
          .getText().equals("Directory: none selected")) {
        commitLabel.setText("Please ensure that all values are set");
        commitLabel.setForeground(new Color(180, 70, 70));
        this.setSize(mainPanel.getPreferredSize().width + 20,
            mainPanel.getPreferredSize().height + 20);
        return;
      }
      int num;
      if (extensionTextField.getText().isBlank()) {
        num = JOptionPane
            .showConfirmDialog(this, "Are you sure you want to leave the extension field blank?");
        if (num != 0) {
          return;
        }
      }
      ModelImpl m = new ModelImpl();
      m.performChange(directoryPathSelected, keywordTextField.getText(), cutoff,
          extensionTextField.getText(), includeHiddenFiles);
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

    includeHiddenFilesCheckBox.addActionListener(e -> {
      includeHiddenFiles = !includeHiddenFiles;
      if (directoryPathSelected != null) {
        updateDirectoryContentsInLog(includeHiddenFiles);
      }
    });

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
      fileDialog.setTitle("Select a directory only - NOT A FILE");
    }
    fileDialog.setVisible(true);
    if (fileDialog.getDirectory() != null) {
      this.directoryPathSelected = fileDialog.getDirectory().concat(fileDialog.getFile());
      directoryNameLabel.setText("Directory: " + directoryPathSelected);
      updateDirectoryContentsInLog(includeHiddenFiles);
    } else {
      directoryNameLabel.setText("Directory: " + "none selected");
    }
    this.setSize(this.getPreferredSize().width + 20, this.getPreferredSize().height + 20);

  }

  /**
   * Places the contents of the directory in the text log.
   *
   * @param includeHiddenFiles if to include hidden files in the log
   */
  private void updateDirectoryContentsInLog(boolean includeHiddenFiles) {
    if (directoryPathSelected == null) {
      return;
    }
    String[] directoryContents = new File(directoryPathSelected).list();
    StringBuilder sb = new StringBuilder("Contents of the directory selected:\n");
    if (directoryContents != null) {
      if (includeHiddenFiles) {
        for (String s : directoryContents) {
          sb.append(s).append("\n");
        }
      } else {
        for (String s : directoryContents) {
          if (!s.startsWith(".")) {
            sb.append(s).append("\n");
          }
        }
      }
    } else {
      sb.append("(empty)");
    }
    logTextArea.setText(sb.toString());
  }


}

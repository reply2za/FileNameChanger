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
  private JButton undoButton;
  private String directoryPathSelected;
  private boolean includeHiddenFiles;
  private ModelImpl m;

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
    keywordLabel.setToolTipText("The keyword that each file must contain to perform the change");
    cutoffLabel.setToolTipText("The number of characters to remove from the end of each file");
    extensionLabel.setToolTipText("Optional: Apply a new extension to all of the renamed files");
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
            "Make changes: (" + logTextArea.getSelectedText().length()
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
      extensionLabel.setText("Unified extension (opt.): " + extensionTextField.getText());
    });
    commitButton.addActionListener(e -> commitButtonAction());

    includeHiddenFilesCheckBox.addActionListener(e -> {
      includeHiddenFiles = !includeHiddenFiles;
      if (directoryPathSelected != null) {
        updateDirectoryContentsInLog(includeHiddenFiles);
      }
    });

    undoButton.addActionListener(e -> {
      commitLabel.setText("Undo attempted.");
      logTextArea.setText(m.UndoNameChange());
    });

  }

  private void commitButtonAction() {
    if (cutoff < 1 || directoryNameLabel
        .getText().equals("Directory: none selected")) {
      commitLabel.setText("Please ensure that all values are set");
      commitLabel.setForeground(new Color(180, 70, 70));
      this.setSize(mainPanel.getPreferredSize().width + 20,
          mainPanel.getPreferredSize().height + 20);
      return;
    }
    int num;
    boolean hasProvidedExtension = false;
    if (!extensionTextField.getText().isEmpty()) {
      num = JOptionPane
          .showConfirmDialog(this,
              "<HTML><b>Are you sure you want to provide a universal extension?</b> \nThis will "
                  + "make each file that matches the given keyword use this extension. \n\nLeaving "
                  + "the extension field empty will maintain the original file extensions.");
      if (num != 0) {
        return;
      } else {
        hasProvidedExtension = true;
      }
    }
    m = new ModelImpl();
    m.performChange(directoryPathSelected, keywordTextField.getText(), cutoff,
        extensionTextField.getText(), includeHiddenFiles, hasProvidedExtension);
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
      String displayDirectoryText = directoryPathSelected;
      if (displayDirectoryText.length() > 76 && !(displayDirectoryText.length() < 80)) {
        displayDirectoryText =
            "..." + displayDirectoryText.substring(displayDirectoryText.length() - 76);
        directoryNameLabel.setToolTipText(directoryPathSelected);
      }
      directoryNameLabel.setText("Directory: " + displayDirectoryText);
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

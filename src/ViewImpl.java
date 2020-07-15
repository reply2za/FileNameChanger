import java.awt.Color;
import java.awt.Dialog;
import java.awt.FileDialog;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
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
  private final JScrollPane jScrollPane;
  private final boolean isMacOS;
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
  private JComboBox modeComboBox;
  private String directoryPathSelected;
  private boolean includeHiddenFiles;
  private ModelImpl m;
  private int modeType;
  private String keyWordString;

  public ViewImpl(boolean b) {
    super();
    this.setTitle("File Name Changer - Remove File Ending Mode");
    this.setDefaultCloseOperation(EXIT_ON_CLOSE);
    this.includeHiddenFiles = false;
    this.isMacOS = b;

    JMenuItem closeMenuItem = new JMenuItem();
    closeMenuItem.addActionListener(e -> System.exit(0));
    closeMenuItem.setAccelerator(KeyStroke
        .getKeyStroke(KeyEvent.VK_W, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
    logTextArea = new JTextArea(23, 25);

    cutoff = -1;
    modeType = 0;
    jScrollPane = new JScrollPane(logTextArea);

    logTextArea.setText("Select a directory to show contents here:");
    logTextArea.setEditable(false);
    logTextArea.setLineWrap(true);
    logTextArea.setWrapStyleWord(true);

    keyWordString = keywordLabel.getText() + " ";
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
    keywordLabel.setToolTipText(
        "The keyword (case-sensitive) that a file must contain to perform the change");
    cutoffLabel.setToolTipText(
        "The number of characters to remove from the end of each file, does not include extension");
    extensionLabel.setToolTipText(
        "Optional unless cutoff is 0: Apply a new extension to all of the renamed files");
    selectDirectoryButton.addActionListener(e -> chooseDirectoryFileDialog());
    keywordTextField.addKeyListener(new KeyListener() {
      @Override
      public void keyTyped(KeyEvent e) {
        if (!commitLabel.getText().contains("Make changes:")) {
          commitLabel.setForeground(Color.BLACK);
          commitLabel.setText("Make changes:");
        }
        keywordLabel.setText(keyWordString + keywordTextField.getText() + e.getKeyChar());
      }

      @Override
      public void keyPressed(KeyEvent e) {

      }

      @Override
      public void keyReleased(KeyEvent e) {

      }
    });

    cutoffTextField.addKeyListener(new KeyListener() {
      @Override
      public void keyTyped(KeyEvent e) {
        String newString;
        if (e.getKeyChar() == '\b') {
          newString = cutoffTextField.getText();
        } else {
          newString = cutoffTextField.getText() + e.getKeyChar();
        }
        cutoffAction(newString);
      }

      @Override
      public void keyPressed(KeyEvent e) {
        // intentionally left blank
      }

      @Override
      public void keyReleased(KeyEvent e) {
        // intentionally left blank
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
          this.getHeight());
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

    modeComboBox.addActionListener(e -> {
      modeType = modeComboBox.getSelectedIndex();
      cutoffLabel.setForeground(Color.BLACK);
      if (modeType == 0) {
        this.setTitle("File Name Changer - Remove File Ending Mode");
        cutoffAction(cutoffTextField.getText());
      } else {
        this.setTitle("File Name Changer - Batch File Rename Mode");
        cutoffAction(cutoffTextField.getText());
      }
    });

  }

  private void resizeMainFrame() {
    this.setSize(mainPanel.getPreferredSize().width + 20,
        this.getHeight());
  }

  /**
   * What to do when the cutoff values are changed
   *
   * @param ending the ending to the label
   */
  private void cutoffAction(String ending) {
    if (!commitLabel.getText().contains("Make changes:")) {
      commitLabel.setForeground(Color.BLACK);
      commitLabel.setText("Make changes:");
    }
    if (modeType == 0) {
      cutoffLabel.setToolTipText(
          "The number of characters to remove from the end of each file, does not include "
              + "extension");
      if (ending.isBlank()) {
        cutoffLabel.setForeground(Color.BLACK);
        cutoffLabel.setText("Cutoff amount: ");
      } else {
        try {
          cutoff = Integer.parseInt(ending);
        } catch (NumberFormatException nfe) {
          cutoff = -1;
        }
        if (cutoff < 0) {
          cutoffLabel.setForeground(new Color(180, 70, 70));
          cutoffLabel.setText("Must be a positive integer");
        } else {
          cutoffLabel.setForeground(Color.BLACK);
          cutoffLabel.setText("Cutoff amount: " + ending);
        }
      }
    } else {
      cutoffLabel.setToolTipText(
          "The name of the new files followed by a number. The files will be sorted "
              + "alphabetically.");
      if (ending.length() > 60) {
        cutoffLabel.setText("Rename to: " + ending.substring(ending.length() - 60));
      } else if (ending.length() > 20) {
        resizeMainFrame();
        cutoffLabel.setText("Rename to: " + ending);
      } else {
        cutoffLabel.setText("Rename to: " + ending);
      }
    }
  }

  /**
   * The action for when the 'make changes' button is pressed.
   */
  private void commitButtonAction() {
    if (modeType == 0) {
      if (cutoff == 0 && extensionTextField.getText().isEmpty()) {
        errorMessage("Extension field cannot be empty when 'cutoff' is 0.");
        return;
      }
      if (cutoff < 0) {
        errorMessage("Must provide a valid cutoff amount.");
        return;
      }
      if (directoryNameLabel.getText().equals("Directory: none selected")) {
        errorMessage("No directory selected.");
        return;
      }
    } else if (cutoffTextField.getText().isBlank()) {
      errorMessage("Must provide a new file name.");
    }
    int jOPInt;
    boolean hasProvidedExtension = false;
    if (!extensionTextField.getText().isEmpty()) {
      jOPInt = JOptionPane
          .showConfirmDialog(this,
              "<HTML><b>Are you sure you want to provide a universal extension?</b> \nThis will "
                  + "make each file that matches the given keyword use this extension. \n\nLeaving "
                  + "the extension field empty will maintain the original file extensions.");
      if (jOPInt != 0) {
        return;
      } else {
        hasProvidedExtension = true;
      }
    }
    m = new ModelImpl();
    if (modeType == 0) {
      m.performChange(directoryPathSelected, keywordTextField.getText(), cutoff,
          extensionTextField.getText(), includeHiddenFiles, hasProvidedExtension);
    } else {
      m.performBatchRename(directoryPathSelected, keywordTextField.getText(),
          cutoffTextField.getText(), extensionTextField.getText(), includeHiddenFiles,
          hasProvidedExtension);
    }
    commitLabel.setForeground(Color.BLACK);

    // main info label after changes have been made
    if (m.wasErrorOnExit()) {
      commitLabel.setText("Made " + m.getNumberOfChanges() + " changes. (stopped due to error)");
    } else {
      commitLabel.setText("Made " + m.getNumberOfChanges() + " changes.");
    }
    // applying information to the new log
    StringBuilder logSB = new StringBuilder();
    if (m.oldContents.size() > 0) {
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
    } else {
      logSB.append(
          "No changes made.\n----------------------"
              + "\nIf you were expecting changes to be made, please ensure that the "
              + "typed parameters are accurate.\n\nTroubleshooting tips:\n----------------------"
              + "\nMake sure to press 'enter' to submit any typed value into the program "
              + "(must be done for every text field).\n\nKeyword section:"
              + "\n-Keyword is case-sensitive."
              + "\n-There should be no unintentional spaces before or after the word when "
              + "typing into the program.\n\nCutoff section:\n-Cutoff value must be less than the "
              + "name of the file (not including its extension)");
    }
    logSB.append("\n");
    logTextArea.setText(logSB.toString());
  }


  /**
   * A standard error message for when there is a problem.
   */
  private void errorMessage() {
    errorMessage("Please ensure that all values are set");
  }

  /**
   * A custom error message for when there is a problem.
   *
   * @param msg the error message to be displayed
   */
  private void errorMessage(String msg) {
    commitLabel.setText(msg);
    commitLabel.setForeground(new Color(180, 70, 70));
    this.setSize(mainPanel.getPreferredSize().width + 20,
        this.getHeight());
  }

  /**
   * The UI to choose a directory.
   */
  public void chooseDirectoryFileDialog() {
    commitLabel.setForeground(Color.BLACK);
    commitLabel.setText("Make changes:");
    if (!isMacOS) {
      chooseDirectoryFileForNonMac();
      return;
    }
    FileDialog fileDialog = new FileDialog(new Dialog(this), "Select directory");
    fileDialog.setMode(FileDialog.LOAD);
    fileDialog.setMultipleMode(false);
    try {
      System.setProperty("apple.awt.fileDialogForDirectories", "true");
    } catch (Exception e) {
      // intentionally left blank
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
   * A UI to choose files for non-macOS based systems. Helper function for
   * 'chooseDirectoryFileDialog'.
   */
  private void chooseDirectoryFileForNonMac() {
    JFileChooser fileDialog = new JFileChooser("Select directory");
    fileDialog.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    if (fileDialog.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
      this.directoryPathSelected = fileDialog.getSelectedFile().toString();
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

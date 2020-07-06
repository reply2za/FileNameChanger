import java.io.File;
import java.util.ArrayList;

public class ModelImpl {

  ArrayList<String> newContents;
  ArrayList<String> oldContents;
  private int numberOfChanges;
  private boolean isErrorOnExit;
  private boolean canUndo;
  private String directoryPathString;
  private String extension;

  public ModelImpl() {
    newContents = new ArrayList<>();
    oldContents = new ArrayList<>();
    numberOfChanges = 0;
  }


  public void performChange(String directoryPathString, String fileNameMustContain,
      int howMuchToCutoff, String extension, boolean includeHiddenFiles) {
    isErrorOnExit = false;
    this.directoryPathString = directoryPathString;
    this.extension = extension;
    howMuchToCutoff += extension.length();
    File directoryPathFile = new File(directoryPathString);
    String[] directoryContents = directoryPathFile.list();
    if (directoryContents != null) {
      canUndo = true;
      for (String nameOfExistingFile : directoryContents) {
        File oldFile = new File(directoryPathString + "/" + nameOfExistingFile);
        // this if statement contains the code that makes the actual change w/ error avoidance
        if (contentsClearedForChange(nameOfExistingFile, fileNameMustContain, howMuchToCutoff,
            includeHiddenFiles)) {
          oldContents.add(nameOfExistingFile);
          String newFileEnding = nameOfExistingFile
              .substring(0, nameOfExistingFile.length() - howMuchToCutoff)
              .concat(extension);
          File newFile = new File(directoryPathString + "/" + newFileEnding);
          if (newFile.exists()) {
            newContents.add("\nError: File name conflict. New file name already exists. "
                + "Ended program to avoid data corruption.\n \nConflicting file name: "
                + newFileEnding);
            isErrorOnExit = true;
            return;
          }
          // attempting the rename
          boolean success = oldFile.renameTo(newFile);
          if (!success) {
            newContents.add("Failed name change: " + nameOfExistingFile);
          } else {
            numberOfChanges++;
            newContents.add(newFileEnding);
          }
        }
      }
    }
  }

  /**
   * What to look for in the file you want to change.
   *
   * @param nameOfFile          the name of the file with extension, not including the path
   * @param fileNameMustContain the string that the file must contain
   * @param howMuchToCutoff     how much to cutoff from the end of the file, includes extension
   *                            length
   * @param includeHiddenFiles  if to also check and change hidden files
   * @return whether the file (nameOfFile) matches the criteria given to it and is to be changed
   */
  private boolean contentsClearedForChange(String nameOfFile, String fileNameMustContain,
      int howMuchToCutoff, boolean includeHiddenFiles) {
    if (includeHiddenFiles) {
      return nameOfFile.contains(fileNameMustContain) && nameOfFile.length() > howMuchToCutoff;
    } else {
      return nameOfFile.contains(fileNameMustContain) && nameOfFile.length() > howMuchToCutoff
          && !nameOfFile.startsWith(".");
    }
  }

  public int getNumberOfChanges() {
    return numberOfChanges;
  }

  public boolean wasErrorOnExit() {
    return isErrorOnExit;
  }

  public String UndoNameChange() {
    StringBuilder outputLog = new StringBuilder("Changes:\n");
    if (canUndo) {
      File directoryPathFile = new File(directoryPathString);
      String[] directoryContents = directoryPathFile.list();
      if (directoryContents != null) {
        int i = 0;
        for (String nameOfExistingFile : newContents) {
          File oldFile = new File(directoryPathString + "/" + nameOfExistingFile);
          if (oldFile.exists()) {
            String newFileName = oldContents.get(i);
            File newFile = new File(directoryPathString + "/" + newFileName);
            if (newFile.exists()) {
              outputLog.append("\nError: File name conflict. New file name already exists. "
                  + "Ended program to avoid data corruption.\n \nConflicting file name: ")
                  .append(newFileName);
              isErrorOnExit = true;
              return outputLog.toString();
            }
            // attempting the rename
            boolean success = oldFile.renameTo(newFile);
            outputLog.append((i + 1)).append(". ");
            if (!success) {
              outputLog.append("Failed name change: ").append(nameOfExistingFile).append("\n");
            } else {
              numberOfChanges++;
              outputLog.append(nameOfExistingFile).append(" -> ")
                  .append(newFileName).append("\n");
            }
            i++;
          }
        }
      }
      return outputLog.append("\n\nAttempted: Previously altered file names have been undone.")
          .toString();
    } else {
      return "Files could not be found. No changes made.";
    }
  }

}

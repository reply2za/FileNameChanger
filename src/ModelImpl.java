import java.io.File;
import java.util.ArrayList;

public class ModelImpl {

  ArrayList<String> newContents;
  ArrayList<String> oldContents;
  private int numberOfChanges;
  private boolean isErrorOnExit;

  public ModelImpl() {
    newContents = new ArrayList<>();
    oldContents = new ArrayList<>();
    numberOfChanges = 0;
  }


  public void performChange(String directoryPathString, String fileNameMustContain,
      int howMuchToCutoff, String extension, boolean includeHiddenFiles) {
    isErrorOnExit = false;
    howMuchToCutoff += extension.length();
    File directoryPathFile = new File(directoryPathString);
    String[] directoryContents = directoryPathFile.list();
    if (directoryContents != null) {
      for (String nameOfFile : directoryContents) {
        File oldFile = new File(directoryPathString + "/" + nameOfFile);
        // this if statement contains the code that makes the actual change w/ error avoidance
        if (contentsClearedForChange(nameOfFile, fileNameMustContain, howMuchToCutoff,
            includeHiddenFiles)) {
          oldContents.add(nameOfFile);
          String newFileEnding = nameOfFile.substring(0, nameOfFile.length() - howMuchToCutoff)
              .concat(extension);
          File newFile = new File(
              directoryPathString + "/" + newFileEnding);
          if (newFile.exists()) {
            newContents
                .add(
                    "\nError: File name conflict. New file name already exists. "
                        + "Ended program to avoid data corruption.\n \nConflicting file name: "
                        + newFileEnding);
            isErrorOnExit = true;
            return;
          }
          // attempting the rename
          boolean success = oldFile.renameTo(newFile);
          if (!success) {
            newContents.add("Failed name change: " + nameOfFile);
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

}

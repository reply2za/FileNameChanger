import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import org.apache.commons.io.FilenameUtils;

public class ModelImpl {

  ArrayList<String> newContents;
  ArrayList<String> oldContents;
  private int numberOfChanges;
  private boolean isErrorOnExit;
  private boolean canUndo;
  private String directoryPathString;

  public ModelImpl() {
    newContents = new ArrayList<>();
    oldContents = new ArrayList<>();
    numberOfChanges = 0;
  }


  /**
   * Performs the file change
   *
   * @param directoryPathString the path to the directory
   * @param fileNameMustContain a string that all files must contain in order to make the change
   * @param howMuchToCutoff     the amount of characters to remove from the end (does not include
   *                            the file extension)
   * @param newExtension        a common extension for all the files, make 'null' to keep file
   *                            defaults
   * @param includeHiddenFiles  whether to also check and change hidden "." files
   */
  public void performChange(String directoryPathString, String fileNameMustContain,
      int howMuchToCutoff, String newExtension, boolean includeHiddenFiles,
      boolean extensionProvided) {
    isErrorOnExit = false;
    this.directoryPathString = directoryPathString;
    File directoryPathFile = new File(directoryPathString);
    String[] directoryContents = directoryPathFile.list();
    if (directoryContents != null) {
      canUndo = true;
      int totalFileCutoff;
      for (String nameOfExistingFile : directoryContents) {
        String oldFilePath = directoryPathString + "/" + nameOfExistingFile;
        String existingFileExtension = "";
        if (!FilenameUtils.getExtension(nameOfExistingFile).isBlank()) {
          existingFileExtension = "." + FilenameUtils.getExtension(nameOfExistingFile);
        }
        totalFileCutoff = existingFileExtension.length() + howMuchToCutoff;
        if (!extensionProvided) {
          newExtension = existingFileExtension;
        }
        File oldFile = new File(oldFilePath);
        // this if statement contains the code that makes the actual change w/ error avoidance
        if (contentsClearedForChange(nameOfExistingFile, fileNameMustContain, totalFileCutoff,
            includeHiddenFiles)) {
          oldContents.add(nameOfExistingFile);
          String newFileEnding = nameOfExistingFile
              .substring(0, nameOfExistingFile.length() - totalFileCutoff)
              .concat(newExtension);
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
   * Performs the file change
   *
   * @param directoryPathString         the path to the directory
   * @param fileNameMustContain         a string that all files must contain in order to make the
   *                                    change
   * @param newFileNameWithoutExtension the name of the new file without it's "." extension, all
   *                                    subsequent files will be the same name with a number
   *                                    attached to it
   * @param newExtension                a common extension for all the files, make 'null' to keep
   *                                    file defaults
   * @param includeHiddenFiles          whether to also check and change hidden "." files
   */
  public void performBatchRename(String directoryPathString, String fileNameMustContain,
      String newFileNameWithoutExtension, String newExtension, boolean includeHiddenFiles,
      boolean extensionProvided) {
    isErrorOnExit = false;
    this.directoryPathString = directoryPathString;
    File directoryPathFile = new File(directoryPathString);
    String[] directoryContents = directoryPathFile.list();
    if (directoryContents != null) {
      ArrayList<String> directoryContentsAL = new ArrayList<String>(
          Arrays.asList(directoryContents));
      // sorts the directory contents
      java.util.Collections.sort(directoryContentsAL);
      canUndo = true;
      int totalFileCutoff;
      // apply to each file
      int numberAtTheEndOfFile = 1;
      for (String nameOfExistingFile : directoryContentsAL) {
        String oldFilePath = directoryPathString + "/" + nameOfExistingFile;
        String existingFileExtension = "";
        // put the extension in the variable existingFileExtension
        if (!FilenameUtils.getExtension(nameOfExistingFile).isBlank()) {
          existingFileExtension = "." + FilenameUtils.getExtension(nameOfExistingFile);
        }
        // if no new extension was provided then makes the existing file extension the new one
        if (!extensionProvided) {
          newExtension = existingFileExtension;
        }
        totalFileCutoff = existingFileExtension.length();
        File oldFile = new File(oldFilePath);
        // this if statement contains the code that makes the actual change w/ error avoidance
        if (contentsClearedForChange(nameOfExistingFile, fileNameMustContain, totalFileCutoff,
            includeHiddenFiles)) {
          oldContents.add(nameOfExistingFile);
          String newFileNameWithExtension = newFileNameWithoutExtension.concat("-")
              .concat(String.valueOf(numberAtTheEndOfFile)).concat(newExtension);
          File newFile = new File(directoryPathString + "/" + newFileNameWithExtension);
          numberAtTheEndOfFile++;
          while (newFile.exists()) {
            newFileNameWithExtension = newFileNameWithoutExtension
                .concat(String.valueOf(numberAtTheEndOfFile)).concat(newExtension);
            newFile = new File(directoryPathString + "/" + newFileNameWithExtension);
            numberAtTheEndOfFile++;
          }
          // attempting the rename
          boolean success = oldFile.renameTo(newFile);
          if (!success) {
            newContents.add("Failed name change: " + nameOfExistingFile);
          } else {
            numberOfChanges++;
            newContents.add(newFileNameWithExtension);
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
              outputLog.append(nameOfExistingFile).append("\n->\n")
                  .append(newFileName).append("\n-------\n");
            }
            i++;
          }
        }
      }
      canUndo = false;
      return outputLog.append(
          "\n\nAttempted: Each previously altered file name has been renamed to its original.")
          .toString();
    } else {
      return "Files could not be found. No changes made.";
    }
  }

}

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
      int howMuchToCutoff, String extension) {
    isErrorOnExit = false;
    howMuchToCutoff += extension.length();
    File directoryPath = new File(directoryPathString);
    String[] contents = directoryPath.list();
    if (contents != null) {
      for (String content : contents) {
        File oldFile = new File(directoryPathString + "/" + content);
        // what to look for in the file you want to change //content.length() > 23
        // try 24 first
        if (content.contains(fileNameMustContain) && content.length() > howMuchToCutoff) {
          oldContents.add(content);
          String newFileEnding = content.substring(0, content.length() - howMuchToCutoff)
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
            newContents.add("Failed name change: " + content);
          } else {
            numberOfChanges++;
            newContents.add(newFileEnding);
          }
        }
      }
    }
  }

  public int getNumberOfChanges() {
    return numberOfChanges;
  }

  public boolean wasErrorOnExit() {
    return isErrorOnExit;
  }

}

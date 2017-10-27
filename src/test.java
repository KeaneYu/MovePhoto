import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;

public class test {

	public static void main(String[] args) {
		
		long totalFiles = 0;
		long processedFiles = 0;
		long unsupportedFiles = 0;
		
		File[] media;
		media = GetListOfFiles();
				
		if(media != null){
			totalFiles = media.length;
			
			for (int i = 0; i < media.length; i++) {
				System.out.println("File: " + media[i]);
				String targetFolder = GetTargetFolder(media[i]);
				if(targetFolder != null){
					moveFile(media[i],targetFolder);
					processedFiles++;
				} else {
					System.out.println("Failed to get target folder for File: " + media[i].getAbsolutePath());
					unsupportedFiles++;
				}
			}
			
			System.out.println("");
			System.out.println("====================================");
			System.out.println("== Total       Files: "+totalFiles);
			System.out.println("== Processed   Files: "+processedFiles);
			System.out.println("== Upsupported Files: "+unsupportedFiles);
		}
		
	}

	private static File[] GetListOfFiles() {

		// Search sub folder "Media-Original"
		String currentDir = System.getProperty("user.dir");
		System.out.println(currentDir);
		
		String mediaFolder = currentDir + java.io.File.separator + "Media-Original";
		if(folderExists(mediaFolder)){
			File file = new File(mediaFolder);
			File[] files = file.listFiles();
			
			if(files.length > 0){				
				return files;
				
			} else {
				System.out.println("Folder "+mediaFolder + " exist, but it's empty, please move media into it!");
				return null;
			}
			
		} else {
			System.out.println("Folder "+mediaFolder + " does't exist, please create it and move media into it!");
			return null;
		}

	}
	
	private static String GetTargetFolder(File currentFile) {
		String currentDir = System.getProperty("user.dir");
		System.out.println(currentDir);
		
		
		String mediaDate = null;
		try {
			Metadata metadata = ImageMetadataReader.readMetadata(currentFile);

			for (Directory directory : metadata.getDirectories()) {
				String dirName = directory.getName();
				if(dirName.contains("Exif IFD0")){
					for (Tag tag : directory.getTags()) {
	
						String tagName = tag.getTagName();
						if(tagName.equals("Date/Time"))
						{
							mediaDate = tag.getDescription();
							break;
						}						
					}
				}
			}

		} catch (ImageProcessingException | IOException e) {
			e.printStackTrace();
		}
		
		// LocalDateTime need Java 8
		LocalDateTime dateTime = null;
		//ZonedDateTime dateTime = null;
		
		if(mediaDate != null){
			// Example 
			// [Exif IFD0] Date/Time - 2017:10:23 12:31:41
			try{
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss");
				dateTime = LocalDateTime.parse(mediaDate, formatter);
				
			} catch (Exception e){
				e.printStackTrace();
				return null;
			}
			
		} else {
		
			System.out.println("Cannot find date from file metadata "+currentFile.getAbsolutePath());
			
			// Then (best effort) use lastModifiedTime
			Path filePath = currentFile.toPath();
			BasicFileAttributes attr;
			try {
				attr = Files.readAttributes(filePath, BasicFileAttributes.class);
				//System.out.println("creationTime: " + attr.creationTime());
				//System.out.println("lastAccessTime: " + attr.lastAccessTime());
				//System.out.println("lastModifiedTime: " + attr.lastModifiedTime());
				
				Instant modifiedTime = attr.lastModifiedTime().toInstant();
				try{
					
					ZonedDateTime zdt = ZonedDateTime.ofInstant(modifiedTime, ZoneId.of("GMT+8"));
					//dateTime = zdt;
					//String time = "";
					//time = time + "-" + zdt.getYear();
					//time = time + "-" + zdt.getMonthValue();
					//time = time + "-" + zdt.getDayOfMonth();
					//time = time + "-" + zdt.getHour();
					//time = time + "-" + zdt.getMinute();
					//time = time + "-" + zdt.getSecond();
					//System.out.println("Zoned Time is: " + time);
					
					//DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DateTimeFormatter.ISO_INSTANT);
					//dateTime = LocalDateTime.parse(mediaDate, DateTimeFormatter.ISO_INSTANT);
					
					String year = String.valueOf(zdt.getYear());
					String month = String.valueOf(zdt.getMonthValue());
					if(month.length()==1)	month = "0" + month;
					String date = String.valueOf(zdt.getDayOfMonth());
					if(date.length()==1)	date = "0" + date;
					String Folder = currentDir + java.io.File.separator + "Media-Processed" + java.io.File.separator + year + java.io.File.separator + month + java.io.File.separator + date;
					//System.out.println("Return the target folder: "+ Folder);
					return Folder;
					
				} catch (Exception e){
					e.printStackTrace();
					return null;
				}
				
			} catch (IOException e1) {
				
				e1.printStackTrace();
			}
			
		}
		
		if(dateTime != null){
			//System.out.println("dateTime is not null");
			String year = String.valueOf(dateTime.getYear());
			String month = String.valueOf(dateTime.getMonthValue());
			if(month.length()==1)	month = "0" + month;
			String date = String.valueOf(dateTime.getDayOfMonth());
			if(date.length()==1)	date = "0" + date;
			String Folder = currentDir + java.io.File.separator + "Media-Processed" + java.io.File.separator + year + java.io.File.separator + month + java.io.File.separator + date;
			//System.out.println("Return the target folder: "+ Folder);
			return Folder;
		} else {
			System.out.println("dateTime object should not be null!");
			return null;
		}
	}

	private static boolean folderExists(String PathOfFolder) {
		if (PathOfFolder != null && !PathOfFolder.isEmpty()) {
			File file = new File(PathOfFolder);

			if (file.exists()) {
				if (file.isDirectory()) {
					return true;
				} else {
					System.out.println(PathOfFolder + " exists, but it is not a folder!");
					return false;
				}
			} else {
				System.out.println("Folder " + PathOfFolder + " does't exist.");
				return false;
			}
		} else {
			System.out.println("Folder path is emptyy!");
			return false;
		}
	}

	private static boolean createFolder(String PathOfFolder) {
		if (PathOfFolder != null && !PathOfFolder.isEmpty()) {
			if (folderExists(PathOfFolder)) {
				return true;
			} else {
				File file = new File(PathOfFolder);
				try {
					file.mkdirs();
					return true;
				} catch (Exception e) {
					e.printStackTrace();
					return false;
				}
			}
		} else {
			System.out.println("Folder path is empty!");
			return false;
		}
	}

	private static boolean fileExists(String PathOfFile) {
		if (PathOfFile != null && !PathOfFile.isEmpty()) {
			File file = new File(PathOfFile);

			if (file.exists()) {
				if (file.isFile()) {
					return true;
				} else {
					System.out.println(PathOfFile + " exists, but it is not a file!");
					return false;
				}
			} else {
				System.out.println("File " + PathOfFile + " does't exist.");
				return false;
			}
		} else {
			System.out.println("File path is empty!");
			return false;
		}
	}

	private static boolean moveFile(File sourceFile, String destinationFoler) {

		if (sourceFile != null && destinationFoler != null && !destinationFoler.isEmpty()) {

			if (createFolder(destinationFoler)) {

				if (sourceFile.isFile()) {
					
					String targetPathOfFile = destinationFoler + java.io.File.separator + sourceFile.getName();
					File targetFile = new File(targetPathOfFile);
					try {
						return sourceFile.renameTo(targetFile);
					} catch (Exception e) {
						e.printStackTrace();
						return false;
					}

				} else {
					System.out.println("Failed to locate source file " + sourceFile.getAbsolutePath());
					return false;
				}

			} else {
				System.out.println("Failed to locate destination folder " + destinationFoler);
				return false;
			}

		} else {
			System.out.println("File path or destination is empty!");
			return false;
		}
	}
}

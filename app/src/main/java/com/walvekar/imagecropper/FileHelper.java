package com.walvekar.imagecropper;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

public class FileHelper {

    // get file name from uri
    static String getFileName(ContentResolver resolver, Uri uri) {
        Cursor returnCursor =
                resolver.query(uri, null, null, null, null);
        assert returnCursor != null;
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        returnCursor.moveToFirst();
        String name = returnCursor.getString(nameIndex);
        returnCursor.close();
        return name;
    }

    // convert fileSize
    public static String humanReadableByteCountSI(long bytes) {
        if (-1000 < bytes && bytes < 1000) {
            return bytes + " B";
        }
        CharacterIterator ci = new StringCharacterIterator("kMGTPE");
        while (bytes <= -999_950 || bytes >= 999_950) {
            bytes /= 1000;
            ci.next();
        }
        return String.format("%.1f %cB", bytes / 1000.0, ci.current());
    }

    // get file Extension
    public static String getFileExtension(String fileName){
        String extension = "";

        int i = fileName.lastIndexOf('.');
        int p = Math.max(fileName.lastIndexOf('/'), fileName.lastIndexOf('\\'));

        if (i > p) {
            extension = fileName.substring(i+1);
        }
        return extension;
    }

    // save Image
    public static void saveFile(Context context, Uri uri, String fileName) throws IOException, FileNotFoundException {
        // Create and save your file to the Android device
        File outputFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File file = new File(outputFile, fileName);
        if (!file.exists()){
            file.createNewFile();
            file.mkdir();
        }
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
        MediaStore.Images.Media.insertImage(context.getContentResolver(),bitmap,file.getAbsolutePath(),file.getName());
    }

    // get file size
    static String getImageSize(Context context, Uri choosen) throws IOException {
        long fileSizeInKB = 0, fileInMb = 0;
        // get the url convert to bmp
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), choosen);

        // get the byte array
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] imageInByte = stream.toByteArray();
        long lengthbmp = imageInByte.length;

        // get the file in KB if it's greater than 1024
        if (lengthbmp > 1024){
            fileSizeInKB = lengthbmp / 1024;
        }

        // get the file in Mb if it's greater than 1024
        if (fileSizeInKB > 1024 ){
            fileInMb = fileSizeInKB / 1024;
        }

        if (fileInMb > 0) {
            return fileInMb + " MB";
        } else if(fileSizeInKB > 0){
            return fileSizeInKB + " KB";
        } else {
            return lengthbmp + " Bytes";
        }

    }
}

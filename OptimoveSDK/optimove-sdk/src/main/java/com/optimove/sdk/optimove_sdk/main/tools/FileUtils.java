package com.optimove.sdk.optimove_sdk.main.tools;

import android.content.Context;
import androidx.annotation.Nullable;

import com.optimove.sdk.optimove_sdk.main.tools.opti_logger.OptiLogger;
import com.optimove.sdk.optimove_sdk.main.tools.opti_logger.OptiLoggerStreamsContainer;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * Support class for accessing files in the local file system
 */
public class FileUtils {

    /**
     * Starts a chain call that reads a file from the local storage
     *
     * @param context Any {@link Context}
     * @return A {@link Reader} to chain onto and get the data from the stored file
     */
    public  Reader readFile(Context context) {
        return new Reader(context);
    }

    /**
     * Starts a chain call that writes a file to the local storage
     *
     * @param context Any {@link Context}
     * @param content The content that needs to be written
     * @return A {@link Writer} to chain onto and write the data to the local storage
     */
    public  <T> Writer write(Context context, T content) {
        return new Writer<>(context, content);
    }

    /**
     * Starts a chain call that deletes a file from the local storage
     *
     * @param context Any {@link Context}
     * @return A {@link Reader} to chain onto and delete the data from the local storage
     */
    public   Deleter deleteFile(Context context) {
        return new Deleter(context);
    }

    /**
     * Returns the size of a file in the given location
     *
     * @param fileName  the file's name
     * @param sourceDir the {@code directory} into which it was saved
     * @param context   Any {@link Context}
     * @return the file's size in {@code bytes}
     */
    public   long getFileSize(String fileName, SourceDir sourceDir, Context context) {
        File file = null;
        switch (sourceDir) {
            case CACHE:
                file = new File(context.getCacheDir(), getFullFileName(fileName));
                break;
            case INTERNAL:
                file = new File(context.getFilesDir(), getFullFileName(fileName));
                break;
        }
        if (!file.exists())
            return 0;
        long bytes = file.length();
        long bytesInMega = 1073741824;
        return bytes / bytesInMega;
    }

    private String getFullFileName(String fileName) {
        return String.format("com_optimove_sdk_%s", fileName);
    }

    private   boolean createFile(File file) {
        if (file.exists())
            return true;
        if (!file.getParentFile().exists()) {
            boolean didMkdirs = file.mkdirs();
            if (!didMkdirs)
                return false;
        }
        try {
            return file.createNewFile();
        } catch (IOException e) {
            OptiLogger.utilsFailedToCreateNewFile(file.getAbsolutePath(), e.getMessage());
            return false;
        }
    }

    public enum SourceDir {
        CACHE, INTERNAL
    }

    /**
     * Responsible for reading file from local storage.
     * Built by chainable API
     */
    @SuppressWarnings("TryFinallyCanBeTryWithResources")
    public class Reader {

        private Context context;
        private String fileName;
        private FileInputStream fileInputStream;

        private Reader(Context context) {
            this.context = context;
        }

        /**
         * Call first to set the name of the file to read
         *
         * @param fileName the file's name
         * @return {@code this} to chain the next call
         */
        public Reader named(String fileName) {
            this.fileName = getFullFileName(fileName);
            return this;
        }

        /**
         * Call second to set the {@code parent directory} of the file to read
         *
         * @param sourceDir the {@code parent directory}
         * @return {@code this} to chain the next call
         */
        public Reader from(SourceDir sourceDir) {
            if (fileName == null) {
                OptiLoggerStreamsContainer.error("Missing file name to read from");
                return this;
            }
            switch (sourceDir) {
                case CACHE:
                    File file = new File(context.getCacheDir(), fileName);
                    if (!file.exists()) {
                        OptiLoggerStreamsContainer.error("The cache directory has no %s file", fileName);
                        break;
                    }
                    try {
                        fileInputStream = new FileInputStream(file);
                    } catch (FileNotFoundException e) {
                        OptiLoggerStreamsContainer.error(e.getMessage());
                    }
                    break;
                case INTERNAL:
                    try {
                        fileInputStream = context.openFileInput(fileName);
                    } catch (FileNotFoundException e) {
                        OptiLoggerStreamsContainer.error(e.getMessage());
                    }
                    break;
            }
            return this;
        }

        /**
         * Call last to finish the reading chain and get the data as {@link String}
         *
         * @return {@code String} if reading was successful, {@code null} otherwise.
         */
        @Nullable
        public String asString() {
            if (fileInputStream == null)
                return null;
            BufferedReader reader = new BufferedReader(new InputStreamReader(fileInputStream));
            try {
                StringBuilder dataBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    dataBuilder.append(line);
                    dataBuilder.append("\n");
                }
                return dataBuilder.toString();
            } catch (IOException e) {
                OptiLoggerStreamsContainer.error(e.getMessage());
            } finally {
                try {
                    reader.close();
                } catch (IOException e) {
                    OptiLoggerStreamsContainer.error(e.getMessage());
                }
            }
            return null;
        }

        /**
         * Call last to finish the reading chain and get the data as {@link JSONObject}
         *
         * @return {@code JSONObject} of reading was successful, {@code null} otherwise.
         */
        @Nullable
        public JSONObject asJson() {
            if (fileInputStream == null)
                return null;
            try {
                String jsonString = asString();
                if (jsonString != null)
                    return new JSONObject(jsonString);
            } catch (JSONException e) {
                OptiLogger.f163(e.getMessage());
            }
            return null;
        }
    }

    /**
     * Responsible for writing data to a local file.
     * Built by chainable API
     */
    @SuppressWarnings("TryFinallyCanBeTryWithResources")
    public   class Writer<T> {

        private Context context;
        private String fileName;
        private boolean append;
        private T content;
        private FileOutputStream fileOutputStream;

        private Writer(Context context, T content) {
            this.context = context;
            this.content = content;
            this.append = false;
        }

        /**
         * Call first to set the file name into which the data will be saved.
         * Calling this {@code to} overload {@code overwrites} any existing data at the request location.
         *
         * @param fileName the file name to save the data into
         * @return {@code this} to chain the next call
         */
        public Writer<T> to(String fileName) {
            return to(fileName, false);
        }

        /**
         * Call first to set the file name into which the data will be saved.
         *
         * @param fileName the file name to save the data into
         * @param append   should the new saved file {@code overwrite} the current data or {@code append} to it
         * @return {@code this} to chain the next call
         */
        public Writer<T> to(String fileName, boolean append) {
            this.fileName = getFullFileName(fileName);
            this.append = append;
            return this;
        }

        /**
         * Call second to set the {@code parent directory} of the file to write
         *
         * @param sourceDir the {@code parent directory}
         * @return {@code this} to chain the next call
         */
        public Writer in(SourceDir sourceDir) {
            if (fileName == null) {
                OptiLogger.f164();
                return this;
            }
            switch (sourceDir) {
                case CACHE:
                    File file = new File(context.getCacheDir(), fileName);
                    boolean fileExists = createFile(file);
                    if (!fileExists) {
                        OptiLogger.f165(fileName);
                        break;
                    }
                    try {
                        fileOutputStream = new FileOutputStream(file, append);
                    } catch (FileNotFoundException e) {
                        OptiLogger.f166(e.getMessage());
                    }
                    break;
                case INTERNAL:
                    try {
                        fileOutputStream = context.openFileOutput(fileName, append ? Context.MODE_APPEND : Context.MODE_PRIVATE);
                    } catch (FileNotFoundException e) {
                        OptiLogger.f167(e.getMessage());
                    }
                    break;
            }
            return this;
        }

        /**
         * Call last to finish the write chain and save the data.
         *
         * @return {@code true} if write was successful, {@code false} otherwise.
         */
        public boolean now() {
            if (fileOutputStream == null)
                return false;
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fileOutputStream));
            try {
                writer.write(content.toString());
                writer.flush();
                return true;
            } catch (IOException e) {
                OptiLogger.f168(e.getMessage());
            } finally {
                try {
                    writer.close();
                } catch (IOException e) {
                    OptiLogger.f169(e.getMessage());
                }
            }
            return false;
        }
    }

    /**
     * Responsible for deleting data from a local file.
     * Built by chainable API
     */
    @SuppressWarnings("TryFinallyCanBeTryWithResources")
    public   class Deleter {

        private Context context;
        private String fileName;
        private SourceDir sourceDir;

        private Deleter(Context context) {
            this.context = context;
        }

        /**
         * Call first to set the file name that will be deleted.
         *
         * @param fileName the file name to delete
         * @return {@code this} to chain the next call
         */
        public Deleter named(String fileName) {
            this.fileName = getFullFileName(fileName);
            return this;
        }

        /**
         * Call second to set the {@code parent directory} of the file to delete
         *
         * @param sourceDir the {@code parent directory}
         * @return {@code this} to chain the next call
         */
        public Deleter from(SourceDir sourceDir) {
            this.sourceDir = sourceDir;
            return this;
        }

        /**
         * Call last to finish the delete chain and delete the data.
         *
         * @return {@code true} if deletion was successful, {@code false} otherwise.
         */
        public boolean now() {
            if (sourceDir == null) {
                OptiLogger.f170();
                return false;
            }
            if (fileName == null) {
                OptiLogger.f171();
                return false;
            }
            switch (sourceDir) {
                case CACHE:
                    File file = new File(context.getCacheDir(), fileName);
                    return !file.exists() || file.delete();
                case INTERNAL:
                    return context.deleteFile(fileName);
            }
            return false;
        }
    }
}

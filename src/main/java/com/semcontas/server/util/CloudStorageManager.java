package com.semcontas.server.util;

import com.google.api.client.util.ByteStreams;
import com.google.cloud.WriteChannel;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageException;
import com.google.cloud.storage.StorageOptions;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.channels.Channels;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class CloudStorageManager {

    private static final String[] ALLOWED_EXTENSIONS = {".jpg", ".jpeg", ".png", ".gif"};

    private static Storage storage = null;

    static {
        storage = StorageOptions.getDefaultInstance().getService();
    }

    public BlobInfo uploadFile(final Path localFile, final String bucketName, final String fileName, final String downloadFileName) throws IOException {
        BlobInfo blobInfo = null;
        String checksum = getMd5(localFile.toFile().getName());
        try (InputStream inputStream = new FileInputStream(localFile.toFile())) {
            blobInfo =
                    BlobInfo.newBuilder(bucketName, fileName)
                            .setContentType("application/octet-stream")
                            .setContentDisposition(String.format("attachment; filename=\"%s\"", downloadFileName))
                            .setMd5(checksum)
                            .build();
            try (WriteChannel writer = storage.writer(blobInfo, Storage.BlobWriteOption.md5Match())) {
                ByteStreams.copy(inputStream, Channels.newOutputStream(writer));
            }
        } catch (StorageException ex) {
            if (!(400 == ex.getCode() && "invalid".equals(ex.getReason()))) {
                throw ex;
            }
        }
        return blobInfo;
    }

    public static String getMd5(String input)
    {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            BigInteger no = new BigInteger(1, messageDigest);
            String hashtext = no.toString(16);
            while (hashtext.length() < 32)
                hashtext = "0" + hashtext;
            return hashtext;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private void checkFileExtension(String fileName) {
        if (fileName != null && !fileName.isEmpty() && fileName.contains(".")) {
            for (String ext : ALLOWED_EXTENSIONS)
                if (fileName.endsWith(ext))
                    return;
            throw new UnsupportedOperationException("The file is not an image");
        }
    }
}

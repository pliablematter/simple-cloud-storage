package com.pliablematter.cloudstorage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.storage.Storage;
import com.google.api.services.storage.StorageScopes;
import com.google.api.services.storage.model.Bucket;
import com.google.api.services.storage.model.StorageObject;

/**
 * Simple wrapper around the Google Cloud Storage API
 */
public class CloudStorage {

	private static Properties properties;
	private static Storage storage;

	private static final String PROJECT_ID_PROPERTY = "project.id";
	private static final String APPLICATION_NAME_PROPERTY = "application.name";
	private static final String ACCOUNT_ID_PROPERTY = "account.id";
	private static final String PRIVATE_KEY_PATH_PROPERTY = "private.key.path";

	/**
	 * Uploads a file to a bucket. Filename and content type will be based on
	 * the original file.
	 * 
	 * @param bucketName
	 *            Bucket where file will be uploaded
	 * @param filePath
	 *            Absolute path of the file to upload
	 * @throws Exception
	 */
	public static void uploadFile(String bucketName, String filePath)
			throws Exception {

		Storage storage = getStorage();

		StorageObject object = new StorageObject();
		object.setBucket(bucketName);

		File file = new File(filePath);

		InputStream stream = new FileInputStream(file);
		try {
			String contentType = URLConnection
					.guessContentTypeFromStream(stream);
			InputStreamContent content = new InputStreamContent(contentType,
					stream);

			Storage.Objects.Insert insert = storage.objects().insert(
					bucketName, null, content);
			insert.setName(file.getName());

			insert.execute();
		} finally {
			stream.close();
		}
	}
	
	public static void downloadFile(String bucketName, String fileName, String destinationDirectory) throws Exception {
		
		File directory = new File(destinationDirectory);
		if(!directory.isDirectory()) {
			throw new Exception("Provided destinationDirectory path is not a directory");
		}
		File file = new File(directory.getAbsolutePath() + "/" + fileName);
		
		Storage storage = getStorage();
		
		Storage.Objects.Get get = storage.objects().get(bucketName, fileName);
		FileOutputStream stream = new FileOutputStream(file);
		try {
			get.executeAndDownloadTo(stream);
		} finally {
			stream.close();
		}
	}

	/**
	 * Deletes a file within a bucket
	 * 
	 * @param bucketName
	 *            Name of bucket that contains the file
	 * @param fileName
	 *            The file to delete
	 * @throws Exception
	 */
	public static void deleteFile(String bucketName, String fileName)
			throws Exception {

		Storage storage = getStorage();
		
		storage.objects().delete(bucketName, fileName).execute();
	}

	/**
	 * Creates a bucket
	 * 
	 * @param bucketName
	 *            Name of bucket to create
	 * @throws Exception
	 */
	public static void createBucket(String bucketName) throws Exception {

		Storage storage = getStorage();

		Bucket bucket = new Bucket();
		bucket.setName(bucketName);

		storage.buckets().insert(
				getProperties().getProperty(PROJECT_ID_PROPERTY), bucket).execute();
	}
	
	/**
	 * Deletes a bucket
	 * 
	 * @param bucketName
	 *            Name of bucket to delete
	 * @throws Exception
	 */
	public static void deleteBucket(String bucketName) throws Exception {

		Storage storage = getStorage();
		
		storage.buckets().delete(bucketName).execute();
	}
	
	/**
	 * Lists the objects in a bucket
	 * 
	 * @param bucketName bucket name to list
	 * @return Array of object names
	 * @throws Exception
	 */
	public static List<String> listBucket(String bucketName) throws Exception {
		
		Storage storage = getStorage();
		
		List<String> list = new ArrayList<String>();
		
		List<StorageObject> objects = storage.objects().list(bucketName).execute().getItems();
		if(objects != null) {
			for(StorageObject o : objects) {
				list.add(o.getName());
			}
		}
		
		return list;
	}
	
	/**
	 * List the buckets with the project
	 * (Project is configured in properties)
	 * 
	 * @return
	 * @throws Exception
	 */
	public static List<String> listBuckets() throws Exception {
		
		Storage storage = getStorage();
		
		List<String> list = new ArrayList<String>();
		
		List<Bucket> buckets = storage.buckets().list(getProperties().getProperty(PROJECT_ID_PROPERTY)).execute().getItems();
		if(buckets != null) {
			for(Bucket b : buckets) {
				list.add(b.getName());
			}
		}
		
		return list;
	}

	private static Properties getProperties() throws Exception {

		if (properties == null) {
			properties = new Properties();
			InputStream stream = CloudStorage.class
					.getResourceAsStream("/cloudstorage.properties");
			try {
				properties.load(stream);
			} catch (IOException e) {
				throw new RuntimeException(
						"cloudstorage.properties must be present in classpath",
						e);
			} finally {
				stream.close();
			}
		}
		return properties;
	}

	private static Storage getStorage() throws Exception {

		if (storage == null) {

			HttpTransport httpTransport = new NetHttpTransport();
			JsonFactory jsonFactory = new JacksonFactory();

			List<String> scopes = new ArrayList<String>();
			scopes.add(StorageScopes.DEVSTORAGE_FULL_CONTROL);

			Credential credential = new GoogleCredential.Builder()
					.setTransport(httpTransport)
					.setJsonFactory(jsonFactory)
					.setServiceAccountId(
							getProperties().getProperty(ACCOUNT_ID_PROPERTY))
					.setServiceAccountPrivateKeyFromP12File(
							new File(getProperties().getProperty(
									PRIVATE_KEY_PATH_PROPERTY)))
					.setServiceAccountScopes(scopes).build();

			storage = new Storage.Builder(httpTransport, jsonFactory,
					credential).setApplicationName(
					getProperties().getProperty(APPLICATION_NAME_PROPERTY))
					.build();
		}

		return storage;
	}
}

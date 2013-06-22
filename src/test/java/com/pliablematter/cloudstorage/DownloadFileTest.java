package com.pliablematter.cloudstorage;


import java.io.File;
import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class DownloadFileTest {
	
	private String bucketName;
	private String destinationDirectory;
	
	@Before
	public void before() throws Exception {
		
		bucketName = "simple-cloud-storage-" + UUID.randomUUID().toString();
		CloudStorage.createBucket(bucketName);
		CloudStorage.uploadFile(bucketName, "src/test/resources/test.txt");
		
		File directory = new File("temp");
		directory.mkdir();
		destinationDirectory = directory.getAbsolutePath();
	}

	@Test
	public void testDownloadFile() throws Exception {
		
		CloudStorage.downloadFile(bucketName, "test.txt", destinationDirectory);
		File file = new File(destinationDirectory + "/test.txt");
		Assert.assertTrue(file.exists());
	}
	
	@After
	public void after() throws Exception {
		
		File file = new File(destinationDirectory + "/test.txt");
		file.delete();
		
		File directory = new File(destinationDirectory);
		directory.delete();
		
		CloudStorage.deleteFile(bucketName, "test.txt");
		CloudStorage.deleteBucket(bucketName);
	}
}
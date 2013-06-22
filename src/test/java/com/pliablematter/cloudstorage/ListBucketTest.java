package com.pliablematter.cloudstorage;

import java.util.List;
import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ListBucketTest {
	
	private String bucketName = null;
	
	@Before
	public void before() throws Exception {
		
		bucketName = "simple-cloud-storage-" + UUID.randomUUID().toString();
		CloudStorage.createBucket(bucketName);
		CloudStorage.uploadFile(bucketName, "src/test/resources/test.txt");
	}

	@Test
	public void testListBucket() throws Exception {
		
		List<String> files = CloudStorage.listBucket(bucketName);
		Assert.assertTrue(files.contains("test.txt"));
	}
	
	@After
	public void after() throws Exception {
		
		CloudStorage.deleteFile(bucketName, "test.txt");
		CloudStorage.deleteBucket(bucketName);
	}
}
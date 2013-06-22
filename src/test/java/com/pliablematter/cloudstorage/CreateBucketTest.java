package com.pliablematter.cloudstorage;

import java.util.List;
import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class CreateBucketTest {
	
	String bucketName = null;
	
	@Before
	public void before() {
		bucketName = "simple-cloud-storage-" + UUID.randomUUID().toString();
	}

	@Test
	public void testCreateBucket() throws Exception {
		
		List<String> buckets = CloudStorage.listBuckets();
		Assert.assertFalse(buckets.contains(bucketName));
		
		CloudStorage.createBucket(bucketName);
		
		buckets = CloudStorage.listBuckets();
		Assert.assertTrue(buckets.contains(bucketName));
	}
	
	@After
	public void after() throws Exception {
		CloudStorage.deleteBucket(bucketName);
	}
}

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

import station.RandomGenerator;

public class App {
	public static void main(String[] args) throws NoSuchAlgorithmException,
			InterruptedException {
//		 RandomGenerator random = new RandomGenerator();
//		
//		 System.out.println(random.nextInt());   
		
		
		Random random = new Random();
		System.out.println(random.nextInt(1));
		
//		try {
//
//			// Create a secure random number generator using the SHA1PRNG
//			// algorithm
//			SecureRandom secureRandomGenerator = SecureRandom
//					.getInstance("SHA1PRNG");
//
//			// Get 128 random bytes
//			byte[] randomBytes = new byte[128];
//			secureRandomGenerator.nextBytes(randomBytes);
//
//			// Create two secure number generators with the same seed
//			int seedByteCount = 5;
//			byte[] seed = secureRandomGenerator.generateSeed(seedByteCount);
//
//			SecureRandom secureRandom1 = SecureRandom.getInstance("SHA1PRNG");
//			secureRandom1.setSeed(seed);
//			SecureRandom secureRandom2 = SecureRandom.getInstance("SHA1PRNG");
//			secureRandom2.setSeed(seed);
//
//			for (int i = 0; i < 10000; i++)
//				System.out.println(secureRandom1.nextDouble());
//
//		} catch (NoSuchAlgorithmException e) {
//		}
	}
}

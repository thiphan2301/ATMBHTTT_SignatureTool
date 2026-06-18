import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class SecurityUtil {
	// phương thức tạo cặp khóa và lưu ra file .txt
	public static void generateAndSaveKeys(String folderPath) throws NoSuchAlgorithmException, IOException {
		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048); // Kích thước khóa 2048 bit
        KeyPair pair = keyGen.generateKeyPair();

        // Chuyển khóa thành chuỗi Base64 để lưu dưới dạng text
        String publicKeyBase64 = Base64.getEncoder().encodeToString(pair.getPublic().getEncoded());
        String privateKeyBase64 = Base64.getEncoder().encodeToString(pair.getPrivate().getEncoded());

        // Lưu ra file .txt
        saveToFile(folderPath + "/public_key.txt", publicKeyBase64);
        saveToFile(folderPath + "/private_key.txt", privateKeyBase64);
	}
	private static void saveToFile(String path, String content) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(path)) {
            fos.write(content.getBytes(StandardCharsets.UTF_8));
        }
    }
	

	//phương thức cho chức năng ký điên tử (mã hóa băm bằng private key)
	public static String signData(String hashValue, File privateKeyFile) throws NoSuchAlgorithmException, NoSuchProviderException, IOException, InvalidKeySpecException, InvalidKeyException, SignatureException {
		PrivateKey privateKey = createPrivKey(privateKeyFile);
		// đổi lại dùng SHA256withRSA thay vi NONEwithRSA vì ko cần băm thủ công nữa
		Signature signRsa = Signature.getInstance("SHA256withRSA");
		signRsa.initSign(privateKey);
		signRsa.update(hashValue.getBytes(StandardCharsets.UTF_8));
		byte[] signByte = signRsa.sign();
		return Base64.getEncoder().encodeToString(signByte);
	}

	// hàm đọc private key
	public static PrivateKey createPrivKey (File privateKeyFile) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
		FileInputStream fis = new FileInputStream(privateKeyFile);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		byte[] bf = new byte[102400];
		int data;
		while((data = fis.read(bf)) != -1) {
			bos.write(bf, 0, data);
		}
		bos.close();
		fis.close();

		String privKeyString = bos.toString(StandardCharsets.UTF_8)
				.replaceAll("\\n", "")
				.replaceAll("\\r", "");
		byte[] keyByte = Base64.getDecoder().decode(privKeyString);
		// X509EncodedKeySpec spec = new X509EncodedKeySpec(keyByte); // Chuẩn format chuyển mảng byte về đối tượng khóa (Key) trong java,chuẩn X509 chỉ dùng cho Piblic key, nên chuyển sang chuẩn PKCS8 để dùng cho private key
		PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyByte);
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");

        return keyFactory.generatePrivate(spec);
	}
}

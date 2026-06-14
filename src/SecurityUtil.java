import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
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
	
	// phương thức băm đơn hàng 
	public static String hashOrderData(String orderData) {
		return null;
	}
	
	//phương thức cho chức năng ký điên tử (mã hóa băm bằng private key)
	public static String signHash(String hashValue, File privateKeyFile) {
		return null;
	}
}

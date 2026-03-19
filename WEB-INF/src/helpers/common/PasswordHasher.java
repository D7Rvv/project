package helpers.common;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public class PasswordHasher{

    private static final String PASS_PATERN = "[a-z,A-Z,0-9,@]+";
    private static final int PASS_LENGTH = 32;

    /**
     * ソルトを生成
     * @return 生成されたソルト
     */
    public static String getSalt(){
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }
    
    /**
     * ハッシュを生成
     * @param password 入力されたパスワード
     * @param salt getSalt()によって生成されたソルト
     * @return 生成されたハッシュ文字列
     */
    public static String hash(String password, String salt) throws Exception, PasswordHasherException {
        if(!password.matches(PasswordHasher.PASS_PATERN)){
            throw new PasswordHasherException("使用できない文字が使われています。");
        } else if(password.length() > PasswordHasher.PASS_LENGTH){
            throw new PasswordHasherException("パスワードは" + PasswordHasher.PASS_LENGTH + "文字以内で入力してください。");
        }
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(Base64.getDecoder().decode(salt));
        byte[] hashedValue = md.digest(password.getBytes());
        for(int i = 0; i < 1000; i++){
            md.reset();
            hashedValue = md.digest(hashedValue);
        }

        return Base64.getEncoder().encodeToString(hashedValue);
    }

    /**
     * パスワードを比較します
     * @param hash 比較先のハッシュ
     * @param salt 比較先のソルト
     * @param password 入力されたパスワード
     * @return 一致した場合TRUE、そうでなければFALSE
     */
    public static boolean equalPasswords(String hash, String salt, String password) throws Exception{
        return hash.equals(PasswordHasher.hash(password, salt));
    }
}
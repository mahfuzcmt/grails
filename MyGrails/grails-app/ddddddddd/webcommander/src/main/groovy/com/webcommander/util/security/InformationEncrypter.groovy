package com.webcommander.util.security

import com.webcommander.util.Base64Coder
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import java.security.MessageDigest
import java.text.SimpleDateFormat

class InformationEncrypter {
    private final String baseKey = "DJKIR46JHSD023REKSDFSDKLF219546JMASDADF70233JASDH7"
    private final String applicationName = "GRAILS_WEB_COMMANDER"
    private final String applicationVersion = "2.0"

    private List hiddenInfos = new ArrayList()
    private long validTimeDuration = 60000

    InformationEncrypter() {}

    InformationEncrypter(String information) {
        verifyInfo(information, true)
    }

    InformationEncrypter(String information, boolean verifyTime) {
        verifyInfo(information, verifyTime)
    }

    InformationEncrypter(String information, long validityDuration) {
        validTimeDuration = validityDuration
        verifyInfo(information, true)
    }

    void hideInfo(String info) {
        hiddenInfos.add(info)
    }

    private void verifyInfo(String information, boolean verifyTime) {
        try {
            String matchKey = String.format("%1\$tY-%1\$tb-%1\$td-%1\$tH-%1\$tM-%1\$tS", [Calendar.getInstance(TimeZone.getTimeZone("GMT"))] as Object[])
            String[] parts = information.replace(" ", "").split("---")
            if(parts.length != 2) {
                throw new Exception()
            }
            if(verifyTime) {
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MMM-dd-HH-mm-ss")
                Date dateInKey = formatter.parse(parts[1])
                Date matchDate = formatter.parse(matchKey)
                if(matchDate.getTime() - dateInKey.getTime() > validTimeDuration) {
                    throw new Exception()
                }
            }
            byte[] ciphertext = Base64Coder.decode(parts[0])

            MessageDigest digest = MessageDigest.getInstance("MD5")
            byte[] originKey = digest.digest((baseKey + "-" + parts[1]).getBytes("UTF-8"))
            byte[] newKey = Arrays.copyOf(originKey, 24)
            for(int h=0; h<8; h++) {
                newKey[h+16] = (byte)(originKey[h] + originKey[h + 8])
            }
            SecretKey sKey = new SecretKeySpec(newKey, "DESede")
            Cipher cipher = Cipher.getInstance("DESede/CBC/PKCS5Padding")
            IvParameterSpec iv = new IvParameterSpec(new byte[8])
            cipher.init(Cipher.DECRYPT_MODE, sKey, iv)
            String joinedText = new String(cipher.doFinal(ciphertext))
            String[] jparts = joinedText.split(",")
            if(jparts.length < 3) {
                throw new Exception()
            }
            for(int h=3; h < jparts.length; h++) {
                hiddenInfos.add(jparts[h])
            }
            if(!jparts[2].equals(parts[1])) {
                throw new Exception()
            }
        } catch(Throwable k) {
            throw new RuntimeException("Could not verify key")
        }
    }

    List getHiddenInfos() {
        return hiddenInfos
    }

    @Override
    String toString() {
        return encryptInfo()
    }

    private String encryptInfo() {
        try {
            String timestamp = String.format("%1\$tY-%1\$tb-%1\$td-%1\$tH-%1\$tM-%1\$tS", [Calendar.getInstance(TimeZone.getTimeZone("GMT"))] as Object[])
            String joinedText = applicationName + "," + applicationVersion + "," + timestamp
            if(hiddenInfos.size() > 0) {
                joinedText += "," + hiddenInfos.join(',')
            }
            MessageDigest digest = MessageDigest.getInstance("MD5")
            byte[] originKey = digest.digest((baseKey + "-" + timestamp).getBytes("UTF-8"))
            byte[] newKey = Arrays.copyOf(originKey, 24)
            for(int h=0; h<8; h++) {
                newKey[h+16] = (byte)(originKey[h] + originKey[h + 8])
            }
            SecretKey sKey = new SecretKeySpec(newKey, "DESede")
            Cipher cipher = Cipher.getInstance("DESede/CBC/PKCS5Padding")
            IvParameterSpec iv = new IvParameterSpec(new byte[8])
            cipher.init(Cipher.ENCRYPT_MODE, sKey, iv)
            byte[] cipherText = cipher.doFinal(joinedText.getBytes("UTF-8"))
            return Base64Coder.encode(cipherText) + "---" + timestamp
        } catch(Throwable t) {
            return null
        }
    }
}

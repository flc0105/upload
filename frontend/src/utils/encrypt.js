import CryptoJS from "crypto-js";

export function encrypt(data) {
  const randomKey = CryptoJS.lib.WordArray.random(16);
  const encrypted = CryptoJS.AES.encrypt(data, randomKey, {
    iv: randomKey,
    mode: CryptoJS.mode.CBC,
    padding: CryptoJS.pad.Pkcs7,
  });
  var encryptedData = CryptoJS.enc.Base64.stringify(encrypted.ciphertext);
  var key = CryptoJS.enc.Base64.stringify(randomKey);
  return { data: encryptedData, key: key };
}

export function decrypt(encryptedData, key) {
  var key = CryptoJS.enc.Base64.parse(key);
  var data = CryptoJS.enc.Base64.parse(encryptedData);
  const decrypted = CryptoJS.AES.decrypt({ ciphertext: data }, key, {
    iv: key,
    mode: CryptoJS.mode.CBC,
    padding: CryptoJS.pad.Pkcs7,
  });
  return decrypted.toString(CryptoJS.enc.Utf8);
}

const crypto = require('crypto');
const pubKeyDer = Buffer.from('MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAl5RkoMTxbcmyTuorendSLFbJDdnopbBIq7C6H0RYLnLwlXmn1PIv+AsxdLrwczCp8L6PIOZj1V4mWowheCoDozcc0RIi2BXX8CzYL9mvp4UXpZQuhe6gw11NlAgvJas7NWipm/VseC/GwnxQK8XWGlMlNw66h7rVVIQqKcbP5NPZw/Wi8jpPrvyRYZ+3DHeHVxLqy4RkXldN5/NnCAFLM+13MjRAd8TBesTrihJzflQLL/3WBdiCn5BTpGTHASd9E8BntnP+hUx4eBABrqeByMAkBoqwJQMkW9P+9QMYkiKTZJHhPWLXMgoq9pH+VBFobtoRIiwtrFuqUMvbqbT3YwIDAQAB', 'base64');

function encrypt(text) {
    const encrypted = crypto.publicEncrypt(
        {
            key: pubKeyDer,
            padding: crypto.constants.RSA_PKCS1_PADDING
        },
        Buffer.from(text)
    );
    return encrypted.toString('base64');
}

const result = {
    username: encrypt('testuser'),
    password: encrypt('Test123456')
};
console.log(JSON.stringify(result));

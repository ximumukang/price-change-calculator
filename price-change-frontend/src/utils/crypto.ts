import JSEncrypt from 'jsencrypt'

let publicKey = ''

export const setPublicKey = (key: string) => {
  publicKey = key
}

export const encrypt = (text: string): string => {
  if (!publicKey) {
    throw new Error('Public key not set')
  }
  const encryptor = new JSEncrypt()
  encryptor.setPublicKey(publicKey)
  const encrypted = encryptor.encrypt(text)
  if (!encrypted) {
    throw new Error('Encryption failed')
  }
  return encrypted
}

export const getPublicKey = () => publicKey

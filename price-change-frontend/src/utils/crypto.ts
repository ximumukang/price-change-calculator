import JSEncrypt from 'jsencrypt'

let publicKey = ''
let encryptor: JSEncrypt | null = null

export const setPublicKey = (key: string) => {
  publicKey = key
  // 创建可复用的加密器实例，避免每次加密都创建新实例
  encryptor = new JSEncrypt()
  
  // jsencrypt 需要 PEM 格式的公钥（带 BEGIN/END 标记）
  // 如果后端返回的是纯 Base64 编码的 DER 格式，需要转换为 PEM 格式
  let pemKey = key
  if (!key.includes('BEGIN')) {
    // 将 Base64 字符串每 64 个字符换行
    const base64Lines = key.match(/.{1,64}/g)?.join('\n') || key
    pemKey = `-----BEGIN PUBLIC KEY-----\n${base64Lines}\n-----END PUBLIC KEY-----`
  }
  
  encryptor.setPublicKey(pemKey)
}

export const encrypt = (text: string): string => {
  if (!publicKey || !encryptor) {
    console.error('[Crypto] Public key not set')
    throw new Error('Public key not set')
  }
  try {
    const encrypted = encryptor.encrypt(text)
    if (!encrypted) {
      console.error('[Crypto] Encryption failed')
      throw new Error('Encryption failed')
    }
    
    // jsencrypt 返回的可能是 PEM 格式（带 BEGIN/END 标记）
    // 需要提取纯 Base64 部分
    let base64Encrypted = encrypted
    if (encrypted.includes('-----')) {
      // 移除 PEM 头尾，只保留 Base64 内容
      base64Encrypted = encrypted
        .replace(/-----BEGIN .*?-----/, '')
        .replace(/-----END .*?-----/, '')
        .replace(/\s/g, '')
    }
    
    return base64Encrypted
  } catch (error) {
    console.error('[Crypto] Encryption error:', error)
    throw error
  }
}

export const getPublicKey = () => publicKey

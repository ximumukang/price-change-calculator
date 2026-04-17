import { get, post } from '../utils/request'
import { encrypt, setPublicKey } from '../utils/crypto'

export interface AuthResponse {
  token: string
  refreshToken: string
  username: string
}

export interface PublicKeyResponse {
  publicKey: string
}

/**
 * 获取 RSA 公钥并设置到加密模块
 */
export const getPublicKey = async () => {
  const response = await get<PublicKeyResponse>('/auth/public-key')
  setPublicKey(response.publicKey)
  return response.publicKey
}

/**
 * 用户登录
 * @param username 用户名（明文）
 * @param password 密码（明文）
 * @returns 认证响应（包含 access token 和 refresh token）
 */
export const login = (username: string, password: string) => {
  return post<AuthResponse>('/auth/login', {
    username: encrypt(username),
    password: encrypt(password)
  })
}

/**
 * 用户注册
 * @param username 用户名（明文）
 * @param password 密码（明文）
 * @returns 认证响应（包含 access token 和 refresh token）
 */
export const register = (username: string, password: string) => {
  return post<AuthResponse>('/auth/register', {
    username: encrypt(username),
    password: encrypt(password)
  })
}

/**
 * 刷新 Access Token
 * @param refreshToken 刷新令牌
 * @returns 新的认证响应
 */
export const refreshToken = (refreshToken: string) => {
  return post<AuthResponse>('/auth/refresh', { refreshToken })
}

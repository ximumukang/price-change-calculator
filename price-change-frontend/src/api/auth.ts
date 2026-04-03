import request from '../utils/request'
import { encrypt, setPublicKey } from '../utils/crypto'

export interface AuthResponse {
  token: string
  username: string
}

export interface PublicKeyResponse {
  publicKey: string
}

interface Result<T> {
  code: number
  message: string
  data: T
}

export const getPublicKey = () => {
  return request.get<never, Result<PublicKeyResponse>>('/auth/public-key').then(res => {
    setPublicKey(res.publicKey)
    return res.publicKey
  })
}

export const login = (username: string, password: string) => {
  return request.post<never, Result<AuthResponse>>('/auth/login', {
    username: encrypt(username),
    password: encrypt(password)
  }).then(res => res)
}

export const register = (username: string, password: string) => {
  return request.post<never, Result<AuthResponse>>('/auth/register', {
    username: encrypt(username),
    password: encrypt(password)
  }).then(res => res)
}

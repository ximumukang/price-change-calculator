import { get, post } from '../utils/request'
import { encrypt, setPublicKey } from '../utils/crypto'

export interface AuthResponse {
  token: string
  username: string
}

export interface PublicKeyResponse {
  publicKey: string
}

export const getPublicKey = () => {
  return get<PublicKeyResponse>('/auth/public-key').then(res => {
    setPublicKey(res.publicKey)
    return res.publicKey
  })
}

export const login = (username: string, password: string) => {
  return post<AuthResponse>('/auth/login', {
    username: encrypt(username),
    password: encrypt(password)
  }).then(res => res)
}

export const register = (username: string, password: string) => {
  return post<AuthResponse>('/auth/register', {
    username: encrypt(username),
    password: encrypt(password)
  }).then(res => res)
}

import { get, post, put, delete_ } from '../utils/request'

export interface PriceItem {
  id: number
  name: string
  currentValue: number
  targetValue: number
  changePercent: number
  createdAt: string
}

export interface PriceItemRequest {
  name: string
  currentValue: number
  targetValue: number
}

export const getPriceItems = (sortOrder: string = 'desc') => {
  return get<PriceItem[]>('/price-items', { params: { sortOrder } })
}

export const createPriceItem = (data: PriceItemRequest) => {
  return post<PriceItem>('/price-items', data)
}

export const deletePriceItem = (id: number) => {
  return delete_<void>(`/price-items/${id}`)
}

export const updatePriceItem = (id: number, data: PriceItemRequest) => {
  return put<PriceItem>(`/price-items/${id}`, data)
}

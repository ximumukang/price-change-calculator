import request from '../utils/request'

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
  return request.get<never, PriceItem[]>('/price-items', { params: { sortOrder } })
}

export const createPriceItem = (data: PriceItemRequest) => {
  return request.post<never, PriceItem>('/price-items', data)
}

export const deletePriceItem = (id: number) => {
  return request.delete<never, void>(`/price-items/${id}`)
}

export const updatePriceItem = (id: number, data: PriceItemRequest) => {
  return request.put<never, PriceItem>(`/price-items/${id}`, data)
}

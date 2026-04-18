import { get, post, put, delete_ } from '../utils/request'

export interface Category {
  id: number
  name: string
  sortOrder: number
  createdAt: string
}

export interface PriceItem {
  id: number
  categoryId: number | null
  categoryName: string | null
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
  categoryId?: number | null
}

export interface CategoryRequest {
  name: string
}

// ===== 价格项 API =====

export const getPriceItems = (sortOrder: string = 'desc', categoryId?: number) => {
  const params: Record<string, string> = { sortOrder }
  if (categoryId) {
    params.categoryId = String(categoryId)
  }
  return get<PriceItem[]>('/price-items', { params })
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

// ===== 分类 API =====

export const getCategories = () => {
  return get<Category[]>('/categories')
}

export const createCategory = (data: CategoryRequest) => {
  return post<Category>('/categories', data)
}

export const updateCategory = (id: number, data: CategoryRequest) => {
  return put<Category>(`/categories/${id}`, data)
}

export const deleteCategory = (id: number) => {
  return delete_<void>(`/categories/${id}`)
}

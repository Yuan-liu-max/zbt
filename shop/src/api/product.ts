/**
 * 商品 API
 */
import http from './index'
import type { ProductItem, CategoryNode, BrandItem, PageResult, ProductQueryParams } from '@/types'

export const productApi = {
  /** 商品列表（支持 keyword/categoryId/brandId/status/minPrice/maxPrice/sortBy/sortOrder） */
  list(params: ProductQueryParams): Promise<PageResult<ProductItem>> {
    return http.get('/products', { params })
  },
  /** 商品详情 */
  detail(id: string | number): Promise<ProductItem> {
    return http.get(`/products/${id}`)
  },
  /** 首页推荐商品 */
  recommend(limit?: number): Promise<ProductItem[]> {
    return http.get('/products/recommend', { params: { limit: limit || 10 } })
  },
  /** 热门搜索关键词 */
  hotSearchKeywords(): Promise<string[]> {
    return http.get('/products/search/hot')
  }
}

export const categoryApi = {
  /** 分类树（从 ProductController 内嵌端点，无权限阻断） */
  tree(): Promise<CategoryNode[]> {
    return http.get('/products/categories/tree')
  }
}

export const brandApi = {
  /** 全部品牌（从 ProductController 内嵌端点，无权限阻断） */
  all(): Promise<BrandItem[]> {
    return http.get('/products/brands/all')
  }
}

import request from './request'

// 获取所有商品 (GET /api/products)
export function getProductList() {
  return request({
    url: '/api/products',
    method: 'get'
  })
}

// 新增商品 (POST /api/products)
export function createProduct(data) {
  return request({
    url: '/api/products',
    method: 'post',
    data
  })
}

// 修改商品 (PUT /api/products/{id})
export function updateProduct(id, data) {
  return request({
    url: `/api/products/${id}`,
    method: 'put',
    data
  })
}

// 删除商品 (DELETE /api/products/{id})
export function deleteProduct(id) {
  return request({
    url: `/api/products/${id}`,
    method: 'delete'
  })
}

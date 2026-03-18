import request from './request'

// 获取盘点历史列表
export function getStocktakeList() {
  return request({
    url: '/api/stocktakes',
    method: 'get'
  })
}

// 提交盘点，系统自动做盈亏处理
export function executeStocktake(itemId, actualQuantity) {
  return request({
    url: '/api/stocktakes',
    method: 'post',
    params: { itemId, actualQuantity }
  })
}

// 删除盘点记录
export function deleteStocktakeRecord(recordId) {
  return request({
    url: `/api/stocktakes/${recordId}`,
    method: 'delete'
  })
}
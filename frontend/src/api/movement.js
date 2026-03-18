import request from './request'

// 执行入库
export function inboundAction(data) {
  return request({
    url: '/api/stock-movements/inbound',
    method: 'post',
    data
  })
}

// 更新入库记录并调库存
export function updateInboundAction(recordId, newAmount) {
  return request({
    url: `/api/stock-movements/inbound/${recordId}`,
    method: 'put',
    data: newAmount,
    headers: { 'Content-Type': 'application/json' }
  })
}

// 执行出库
export function outboundAction(data) {
  return request({
    url: '/api/stock-movements/outbound',
    method: 'post',
    data
  })
}

// 撤销出库
export function cancelOutboundAction(recordId) {
  return request({
    url: `/api/stock-movements/outbound/${recordId}`,
    method: 'delete'
  })
}

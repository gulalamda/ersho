import http from './http';

/** 学生 / 登录态 */
export const studentApi = {
  /** 登录，返回 { id, name, jwt } */
  login: (data) => http.post('/student/login', data, { silent: true }),
  /** 注册，成功返回学生对象 */
  register: (data) => http.post('/student/denlu', data, { silent: true }),
  /** 当前登录用户资料（不含密码） */
  info: () => http.get('/student/info'),
  /** 修改资料 */
  update: (data) => http.put('/student/put', data),
  /** 注销账号 */
  remove: () => http.delete('/student/Delete')
};

/** 商品 */
export const goodsApi = {
  /** 按名称模糊搜索；无结果时后端返回 code 400，视为空 */
  search: (name) => http.get('/goods/select', { params: { name }, silent: true }),
  /** 上架 */
  publish: (data) => http.put('/goods/put', data, { silent: true }),
  /** 我在卖的商品 */
  mine: () => http.get('/goods/mine'),
  /** 别人在卖的商品（市场列表） */
  others: () => http.get('/goods/mina'),
  /** 修改自己发布的商品 */
  update: (data) => http.put('/goods/updatagood', data, { silent: true }),
  /** 下架 */
  remove: (id) => http.delete('/goods/deleteGoods', { params: { id }, silent: true })
};

/** 订单 */
export const ordersApi = {
  /** 下单，body: { goodsId, num, address } */
  create: (data) => http.post('/orders', data, { silent: true }),
  /** 我买到的 */
  bought: () => http.get('/orders/mine'),
  /** 我卖出的 */
  sold: () => http.get('/orders/sold'),
  /** 卖家发货传 1，买家确认收货传 2 */
  setStatus: (id, status) => http.put('/orders/status', null, { params: { id, status }, silent: true })
};

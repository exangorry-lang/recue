// 请求封装（uni.request）
// H5 开发直连后端；APP 打包时改为部署服务器地址（如 http://192.168.x.x:8080）
const BASE_URL = 'http://localhost:8080'

export function request(options) {
  return new Promise((resolve, reject) => {
    const token = uni.getStorageSync('token')
    const header = { 'Content-Type': 'application/json' }
    if (token) {
      header.Authorization = 'Bearer ' + token
    }
    uni.request({
      url: BASE_URL + options.url,
      method: options.method || 'GET',
      data: options.data || {},
      header,
      success: (res) => {
        const body = res.data
        if (res.statusCode === 200 && body && body.code === 200) {
          resolve(body)
        } else if (body && body.code === 401) {
          uni.removeStorageSync('token')
          uni.removeStorageSync('user')
          uni.reLaunch({ url: '/pages/login/login' })
          reject(new Error((body && body.msg) || '未登录'))
        } else {
          reject(new Error((body && body.msg) || '请求失败'))
        }
      },
      fail: () => reject(new Error('网络错误，请检查连接'))
    })
  })
}

export const get = (url, data) => request({ url, method: 'GET', data })
export const post = (url, data) => request({ url, method: 'POST', data })
